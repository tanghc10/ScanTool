package com.yzq.ScanTool.event;

import com.yzq.ScanTool.HttpManage;

/**
 * Created by 73843 on 2017/3/30.
 */

public class HttpPostEvent {
    protected HttpManage.postType type;
    protected String resultStr;
    protected boolean isSuccess;

    public HttpPostEvent(HttpManage.postType type, String resultStr, boolean isSuccess){
        this.type = type;
        this.resultStr = resultStr;
        this.isSuccess = isSuccess;
    }

    public String getResultStr(){
        return resultStr;
    }

    public HttpManage.postType getType(){
        return type;
    }

    public boolean isSuccess(){
        return isSuccess;
    }
}
