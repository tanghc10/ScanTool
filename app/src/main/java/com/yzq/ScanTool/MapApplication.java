package com.yzq.ScanTool;

import android.app.Application;

import com.baidu.mapapi.SDKInitializer;

/**
 * Created by 73843 on 2017/7/1.
 */

public class MapApplication extends Application{

    @Override
    public void onCreate(){
        super.onCreate();
        SDKInitializer.initialize(this);
    }
}
