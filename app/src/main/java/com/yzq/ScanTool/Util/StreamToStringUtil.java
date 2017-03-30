package com.yzq.ScanTool.Util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Created by 73843 on 2017/3/30.
 */

public class StreamToStringUtil {
    public static String StreamToString(InputStream inputStream){
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int len = 0;
            byte[] buffer = new byte[1024];
            while ((len = inputStream.read(buffer)) != -1)
            {
                baos.write(buffer, 0, len);
            }
            inputStream.close();
            baos.close();
            byte[] res = baos.toByteArray();
            String tem=new String(res);
            return new String(res);
        } catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
