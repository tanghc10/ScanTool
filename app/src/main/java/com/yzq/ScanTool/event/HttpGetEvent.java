package com.yzq.ScanTool.event;

import com.yzq.ScanTool.HttpManage;

/**
 * Created by 73843 on 2017/6/26.
 */

public class HttpGetEvent {
    protected HttpManage.getType type;
    protected String resultStr;
    protected boolean isSuccess;

    public HttpGetEvent(HttpManage.getType type, String resultStr, boolean isSuccess){
        this.type = type;
        this.resultStr = resultStr;
        this.isSuccess = isSuccess;
    }

    public String getResultStr(){
        return resultStr;
    }

    public HttpManage.getType getType(){
        return type;
    }

    public boolean isSuccess(){
        return isSuccess;
    }
}
