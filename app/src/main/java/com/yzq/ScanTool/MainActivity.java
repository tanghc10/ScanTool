package com.yzq.ScanTool;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.renderscript.Sampler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.os.Handler;
import android.widget.Toast;

import com.yzq.ScanTool.event.HttpGetEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener {
    private String Tag = "MainActivity";
    private String deviceId;
    private String department;
    private String addrDetail;
    private String AddrDetail;
    private String locationProvider;
    private Button btn_search;
    private View pic_addDevice;
    private TextView textView;
    private TextView textView1;
    private TextView textView2;
    private EditText txt_deviceId;
    private LocationManager locationManager;
    private Location location;
    private ProgressBar myprogressBar;
    private Handler handler = null;
    private double pressure;
    private boolean locationOK;
    private double Longitude;
    private double Latitude;
    private double Longitude_device;
    private double Latitude_device;
    private static final String HttpHost = "http://test.xiaoan110.com:8088/liquid/";
    private static final String ak = "uWjpG3xOVQjlZeX8I4sOGpZZUZGfwji1";
    private static final String mcode = "C7:AA:6C:22:FB:B6:59:8F:1D:47:88:22:FD:D7:5A:31:CA:C9:AE:33;com.yzq.testzxing";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        handler = new Handler(Looper.getMainLooper());
        Longitude_device = 0;
        Latitude_device = 0;
        /*
         * 手机定位
         */
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        location = null;
        if (providers.contains(LocationManager.GPS_PROVIDER)){
            locationProvider = LocationManager.GPS_PROVIDER;
            location = locationManager.getLastKnownLocation(locationProvider);
            Log.d("定位", "gps");
        }
        if (location == null && providers.contains(LocationManager.NETWORK_PROVIDER)){
            locationProvider = LocationManager.NETWORK_PROVIDER;
            Log.d("定位", "network");
        }
        location = locationManager.getLastKnownLocation(locationProvider);
        if (location != null){
            showLocation(location);
            Toast.makeText(MainActivity.this, "定位成功", Toast.LENGTH_SHORT).show();
            getAddrDetail(Longitude, Latitude);
            locationOK = true;
        }
        else {
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
            Toast.makeText(this, "定位失败", Toast.LENGTH_SHORT).show();
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

    private void initView() {
        btn_search = (Button) findViewById(R.id.btn_search);
        pic_addDevice = (View) findViewById(R.id.pic_addDivice);
        myprogressBar = (ProgressBar) findViewById(R.id.progressBar);
        textView = (TextView) findViewById(R.id.txt_addr);
        textView1 = (TextView) findViewById(R.id.txt_apply);
        textView2 = (TextView) findViewById(R.id.txt_liquid);
        txt_deviceId = (EditText) findViewById(R.id.txt_deviceID);
        btn_search.setOnClickListener(this);
        pic_addDevice.setOnClickListener(this);
        myprogressBar.setOnClickListener(this);
        textView.setOnClickListener(this);
    }

    /*
     *动作监听
     */
    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.btn_search:
                deviceId = txt_deviceId.getText().toString();
                if (deviceId == null || deviceId.length() == 0){
                    Toast.makeText(MainActivity.this, "请输入设备号！", Toast.LENGTH_SHORT).show();
                    break;
                }
                textView.setText("");
                textView1.setText("");
                textView2.setText("");
                Toast.makeText(MainActivity.this, "正在查询", Toast.LENGTH_SHORT).show();
                getDeviceInfo(deviceId);
                break;
            case R.id.pic_addDivice:
                if (locationOK == false){
                    Toast.makeText( MainActivity.this, "手机定位失败，无法对设备进行操作", Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.d(Tag, AddrDetail);
                intent.setClass(MainActivity.this, AddDevice.class);
                Bundle bundle = new Bundle();
                bundle.putDouble("Longtitude", Longitude);
                bundle.putDouble("Latitude", Latitude);
                bundle.putString("AddrDetail", AddrDetail);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            case R.id.progressBar:
                if (deviceId == null || deviceId.length() == 0){
                    Toast.makeText(MainActivity.this, "请先输入设备号查询", Toast.LENGTH_SHORT).show();
                    break;
                }
                intent.setClass(MainActivity.this, pressureHistory.class);
                Bundle bundle1 = new Bundle();
                bundle1.putString("deviceId", deviceId);
                intent.putExtras(bundle1);
                startActivity(intent);
                break;
            case R.id.txt_addr:
                if (Latitude_device == 0 || Longitude_device == 0){
                    Toast.makeText(MainActivity.this, "无设备信息，请查询后再试", Toast.LENGTH_SHORT).show();
                    break;
                }
                intent.setClass(MainActivity.this, MapActivity.class);
                Bundle bundle2 = new Bundle();
                bundle2.putString("deviceId", deviceId);
                bundle2.putDouble("Latitude", Latitude_device);
                bundle2.putDouble("Longitude", Longitude_device);
                bundle2.putString("addr", AddrDetail);
                intent.putExtras(bundle2);
                startActivity(intent);
                break;
            default:
                break;
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

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if (locationManager!=null){
            locationManager.removeUpdates(locationListener);
        }
    }

    public void subscribe(){
        EventBus.getDefault().register(this);
    }

    public void unsubscribe(){
        EventBus.getDefault().unregister(this);
    }

    public void getDeviceInfo(String deviceId){
        String Url = getUrl(deviceId);
        HttpManage.getHttpResult(Url, HttpManage.getType.GET_TYPE_DEVICEINFO);
    }

    public void setMyprogressBar(int hight){
        myprogressBar.setProgress(0);
        myprogressBar.setProgress(hight);
    }

    public void getAddrDetail(double longitude, double latitude){
        String Url = "http://api.map.baidu.com/geocoder/v2/?location=" + latitude +
                "," + longitude + "&output=json&pois=1&ak=" + ak + "&mcode=" + mcode;
        AddrDetail = null;
        HttpManage.getHttpResult(Url, HttpManage.getType.GET_TYPE_ADDR);
    }

    public String getUrl(String deviceID){
        String url = HttpHost + "device/" + deviceID;
        return url;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHttpGetEvent(HttpGetEvent event){
        if (event.getType() == HttpManage.getType.GET_TYPE_ADDR){
            try {
                JSONObject jsonObject = new JSONObject(event.getResultStr());
                if (jsonObject.has("status")){
                    int status = jsonObject.getInt("status");
                    if (status == 0){
                        if (jsonObject.has("result")){
                            JSONObject result = jsonObject.getJSONObject("result");
                            if (result.has("formatted_address")){
                                AddrDetail = result.getString("formatted_address");
                            }else if (result.has("business")){
                                AddrDetail = result.getString("business");
                            }else {
                                //TODO
                            }
                        }
                    }else{
                        Toast.makeText(MainActivity.this, "Error:" + status, Toast.LENGTH_SHORT).show();
                    }
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
        }else if (event.getType() == HttpManage.getType.GET_TYPE_DEVICEINFO){
            try{
                JSONObject result = new JSONObject(event.getResultStr());
                int code = result.getInt("code");
                if (code == 0){
                    Toast.makeText(MainActivity.this, "查询成功", Toast.LENGTH_SHORT).show();
                    JSONObject data = result.getJSONObject("data");
                    department = data.getString("department");
                    addrDetail = data.getString("addrDetail");
                    pressure = data.getDouble("pressure");
                    Longitude_device = data.getDouble("Longitude");
                    Latitude_device = data.getDouble("Latitude");
                    textView1.setText(department);
                    textView.setText(addrDetail);
                    int hight = (int)(pressure * 10);
                    if (hight > 60){
                        hight = hight % 60;
                    }
                    textView2.setText(hight+"cm");
                    hight = hight * 10 / 6;
                    setMyprogressBar(hight);
                }else {
                    dealWithErrorMsg(code);
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
    }

    public void dealWithErrorMsg(int code){
        if (code == 102){
            Toast.makeText(MainActivity.this, "Error:设备无液压数据", Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(MainActivity.this, "Error:设备不存在", Toast.LENGTH_LONG).show();
        }
    }
}
