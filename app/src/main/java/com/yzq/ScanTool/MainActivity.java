package com.yzq.ScanTool;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.yzq.ScanTool.event.HttpPostEvent;
import com.yzq.ScanTool.zxing.android.CaptureActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btn_addDevice;
    private Button btn_updateDevice;
    private Button btn_deleteDevice;
    private TextView resultTv;
    private LocationManager locationManager;
    private String locationProvider;
    private TextView locationTv;
    private Location location;
    private boolean locationOK;
    private double Longitude;
    private double Latitude;
    private static final String DECODED_CONTENT_KEY = "codedContent";
    private static final String DECODED_BITMAP_KEY = "codedBitmap";
    private static final String HttpHost = "http://test.xiaoan110.com:8088/liquid/";
    private static HttpManage.postType type;

    private static final int REQUEST_CODE_SCAN = 0x0000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        location = null;
        if (providers.contains(LocationManager.GPS_PROVIDER)){
            locationProvider = LocationManager.GPS_PROVIDER;
            location = locationManager.getLastKnownLocation(locationProvider);
            if (location != null){
                Log.e("Location","GPS");
            }
        }
        if (location == null && providers.contains(LocationManager.NETWORK_PROVIDER)){
            locationProvider = LocationManager.NETWORK_PROVIDER;
            Log.e("Location","Network");
        }else{
            Toast.makeText(this, "没有可用的位置提供器", Toast.LENGTH_SHORT).show();
            return;
        }
        location = locationManager.getLastKnownLocation(locationProvider);
        if (location != null){
            showLocation(location);
            Toast.makeText(MainActivity.this, "定位成功", Toast.LENGTH_SHORT).show();
            Log.e("定位","lat:"+location.getLatitude()+"lon:"+location.getLongitude());
            locationOK = true;
        }
        else {
            Log.e("定位","失败");
            Toast.makeText(MainActivity.this, "定位失败", Toast.LENGTH_SHORT).show();
            locationOK = false;
        }
        locationManager.requestLocationUpdates(locationProvider, 3000, 1, locationListener);
    }

    private void showLocation(Location location){
        if (location != null){
            this.Latitude = location.getLatitude();
            this.Longitude = location.getLongitude();
        }else {
            locationTv.setText("定位失败");
        }
    }

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            showLocation(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if (locationManager!=null){
            locationManager.removeUpdates(locationListener);
        }
    }


    private void initView() {
        btn_addDevice = (Button) findViewById(R.id.btn_addDevice);
        btn_updateDevice = (Button)findViewById(R.id.btn_updateDevice);
        btn_deleteDevice = (Button)findViewById(R.id.btn_deleteDevice);
        resultTv = (TextView) findViewById(R.id.resultTv);
        locationTv = (TextView) findViewById(R.id.txt_location);
        btn_addDevice.setOnClickListener(this);
        btn_updateDevice.setOnClickListener(this);
        btn_deleteDevice.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        if (locationOK == false){
            Toast.makeText( MainActivity.this, "手机定位失败，无法对设备进行操作", Toast.LENGTH_SHORT).show();
            return;
        }
        switch (v.getId()) {
            case R.id.btn_addDevice:
                this.type = HttpManage.postType.POST_TYPE_ADD;
                intent.setClass(MainActivity.this, CaptureActivity.class);
                startActivityForResult(intent, REQUEST_CODE_SCAN);
                break;
            case R.id.btn_updateDevice:
                this.type = HttpManage.postType.POST_TYPE_UPDATE;
                intent.setClass(MainActivity.this, CaptureActivity.class);
                startActivityForResult(intent, REQUEST_CODE_SCAN);
                break;
            case R.id.btn_deleteDevice:
                this.type = HttpManage.postType.POST_TYPE_DELETE;
                intent.setClass(MainActivity.this, CaptureActivity.class);
                startActivityForResult(intent, REQUEST_CODE_SCAN);
                break;
            default:
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 扫描二维码/条码回传
        if (requestCode == REQUEST_CODE_SCAN && resultCode == RESULT_OK) {
            if (data != null) {

                String content = data.getStringExtra(DECODED_CONTENT_KEY);
                Bitmap bitmap = data.getParcelableExtra(DECODED_BITMAP_KEY);
                String DeviceId = null;
                JSONObject jsonObject = new JSONObject();
                try{
                    JSONObject Device = new JSONObject(content);
                    if (Device.has("DeviceID")){
                        DeviceId = Device.getString("DeviceID");
                        jsonObject.put("DeviceID", DeviceId);
                        jsonObject.put("Longitude", Longitude);
                        jsonObject.put("Latitude", Latitude);
                        HttpManage.postHttpResult(getUrl(type),type, jsonObject.toString());
                    }else {
                        resultTv.setText("扫描结果中无DeviceID");
                    }
                }catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAddDeviceEvent(HttpPostEvent event){
        TextView textView = (TextView)findViewById(R.id.txt_location);
        textView.setText(event.getResultStr());
    }

    public String getUrl(HttpManage.postType type){
        String url = "";
        if (type == HttpManage.postType.POST_TYPE_ADD){
            url = HttpHost + "add_device";
        }else if (type == HttpManage.postType.POST_TYPE_UPDATE){
            url = HttpHost + "update_device";
        }else if (type == HttpManage.postType.POST_TYPE_DELETE){
            url = HttpHost + "delete_device";
        }
        return url;
    }

}
