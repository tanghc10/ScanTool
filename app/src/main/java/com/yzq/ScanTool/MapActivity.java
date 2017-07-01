package com.yzq.ScanTool;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;
import com.yzq.ScanTool.Util.GPSConvertUtil;

/**
 * Created by 73843 on 2017/6/29.
 */

public class MapActivity extends Activity{
    private final static String Tag = "MapActivity";
    private BaiduMap mBaiduMap;
    private Marker mMarker;
    private MapView mMapView;
    private ImageButton btn_back;
    private double Latitude;
    private double Longitude;
    private String deviceId;
    private String addr;
    private TextView name;
    private TextView location;

    @Override
    protected void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        BMapManager bMapManager = new BMapManager();
        bMapManager.init();
        SDKInitializer.initialize(this.getApplicationContext());
        setContentView(R.layout.activity_map);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        Latitude = bundle.getDouble("Latitude");
        Longitude = bundle.getDouble("Longitude");
        deviceId = bundle.getString("deviceId");
        addr = bundle.getString("addr");
        bindData();
        initView();
    }

    public void bindData(){
        mMapView = (MapView) findViewById(R.id.mapView);
        btn_back = (ImageButton) findViewById(R.id.map_imageview_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        name = (TextView) findViewById(R.id.location_deviceId);
        location = (TextView) findViewById(R.id.txt_location);
    }

    public void initView(){
        name.setText(deviceId);
        location.setText(addr);
        mMapView.showZoomControls(false);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                return true;
            }
        });
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        MapStatus mapStatusPlain = new MapStatus.Builder(mBaiduMap.getMapStatus()).overlook(0).build();
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(mapStatusPlain));
        initMarker();
    }

    public void initMarker(){
        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.img_map_location);
        LatLng point = new LatLng(Latitude, Longitude);
        MarkerOptions option = new MarkerOptions().position(GPSConvertUtil.convertFromCommToBdll09(point)).icon(bitmapDescriptor);
        mMarker = (Marker) mBaiduMap.addOverlay(option);
        moveMapToCenter(GPSConvertUtil.convertFromCommToBdll09(point));
    }

    private void moveMapToCenter(LatLng point){
        MapStatus mapStatus = new MapStatus.Builder().target(point).zoom(18).build();
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mapStatus);
        mBaiduMap.setMapStatus(mapStatusUpdate);
    }
}
