package com.yzq.ScanTool;

import com.yzq.ScanTool.Util.StreamToStringUtil;
import com.yzq.ScanTool.Util.StringUtil;
import com.yzq.ScanTool.event.HttpPostEvent;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.greenrobot.eventbus.EventBus;
/**
 * Created by 73843 on 2017/3/30.
 */

public class HttpManage {
    public enum postType{
        POST_TYPE_ADD,
        POST_TYPE_UPDATE,
        POST_TYPE_DELETE,
    }
    public static void postHttpResult(final String url, final postType postType, final String body){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection;
                try{
                    URL postURL = new URL(url);
                    connection = (HttpURLConnection) postURL.openConnection();
                    connection.setConnectTimeout(5 * 1000);
                    connection.setRequestMethod("POST");
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    connection.setUseCaches(false);
                    connection.setRequestProperty("Content-Type", "application/json");
                    OutputStream out = connection.getOutputStream();
                    out.write(body.getBytes());
                    out.flush();
                    out.close();
                    String result = StreamToStringUtil.StreamToString(connection.getInputStream());
                    EventBus.getDefault().post(new HttpPostEvent(postType, StringUtil.decodeUnicode(result), true));
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
