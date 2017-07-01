package com.yzq.ScanTool;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;

import com.yzq.ScanTool.event.HttpPostEvent;
import com.yzq.ScanTool.zxing.android.CaptureActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by 73843 on 2017/6/25.
 */

public class AddDevice extends Activity {
    private TextView deviceId;
    private EditText apply;
    private TextView addr;
    private Button btn_addDevice;
    private ImageButton btn_back;
    private View btn_scanimei;
    private String txt_deviceId;
    private String Tag = "AddDevice";
    private String txt_apply;
    private String addrDetail;
    private double Longitude;
    private double Latitude;
    private Handler handler;
    private static final int REQUEST_CODE_SCAN = 0x0000;
    private static final String DECODED_CONTENT_KEY = "codedContent";
    private static final String DECODED_BITMAP_KEY = "codedBitmap";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adddivice);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        Longitude = bundle.getDouble("Longtitude");
        Latitude = bundle.getDouble("Latitude");
        addrDetail = bundle.getString("AddrDetail");
        handler = new Handler(Looper.getMainLooper());
        initView();
    }

    private void initView(){
        deviceId = (TextView) findViewById(R.id.deviceID);
        apply = (EditText) findViewById(R.id.txt_apply2);
        addr = (TextView) findViewById(R.id.txt_addr2);
        btn_addDevice = (Button) findViewById(R.id.btn_addDevice);
        btn_scanimei = (View) findViewById(R.id.btn_scanImei);
        btn_back = (ImageButton) findViewById(R.id.adddvice_imageview_back);
        btn_scanimei.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(AddDevice.this, CaptureActivity.class);
                startActivityForResult(intent, REQUEST_CODE_SCAN);
            }
        });
        btn_addDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txt_apply = apply.getText().toString();
                if (txt_deviceId == null || txt_deviceId.length() == 0){
                    Toast.makeText(AddDevice.this, "请先扫描设备ID", Toast.LENGTH_SHORT).show();
                }else if (txt_apply == null || txt_apply.length() == 0) {
                    Toast.makeText(AddDevice.this, "请输入应用单位", Toast.LENGTH_SHORT).show();
                }else{
                    JSONObject jsonObject = new JSONObject();
                    try{
                        jsonObject.put("DeviceID", txt_deviceId);
                        jsonObject.put("Longitude", Longitude);
                        jsonObject.put("Latitude", Latitude);
                        jsonObject.put("department", txt_apply);
                        jsonObject.put("addrDetail", addrDetail);
                        HttpManage.postHttpResult(getAddDeviceUrl(), HttpManage.postType.POST_TYPE_ADDDEIVCE, jsonObject.toString());
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            }
        });
        addr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (txt_deviceId != null){
                    deviceId.setText(txt_deviceId);
                    addr.setText(addrDetail);
                }
            }
        });
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        initView();
        subscribe();
    }

    @Override
    protected void onPause(){
        super.onPause();
        unsubscribe();
    }

    public void subscribe(){
        EventBus.getDefault().register(this);
    }

    public void unsubscribe(){
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SCAN && resultCode == RESULT_OK){
            if (data != null){
                String content = data.getStringExtra(DECODED_CONTENT_KEY);
                Bitmap bitmap = data.getParcelableExtra(DECODED_BITMAP_KEY);
                deviceId = null;
                try{
                    JSONObject Device = new JSONObject(content);
                    Log.d(Tag, Device.toString());
                    if (Device.has("DeviceID")){
                        txt_deviceId = Device.getString("DeviceID");
                        if (txt_deviceId != null){
                            handler.post(runnableUi);
                        }
                    }else {
                        Toast.makeText(AddDevice.this, "扫描结果中无DeviceID", Toast.LENGTH_SHORT).show();
                        deviceId.setClickable(true);
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }

            }
        }
    }

    Runnable runnableUi = new Runnable() {
        @Override
        public void run() {
            TextView tv1 = (TextView) findViewById(R.id.deviceID);
            tv1.setText(txt_deviceId);
            TextView tv2 = (TextView) findViewById(R.id.txt_addr2);
            tv2.setText(addrDetail);
        }
    };

    public String getAddDeviceUrl(){
        return "http://test.xiaoan110.com:8088/liquid/add_device";
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHttpPostEvent(HttpPostEvent event){
        Toast.makeText(AddDevice.this, "添加完成", Toast.LENGTH_LONG).show();
    }
}
