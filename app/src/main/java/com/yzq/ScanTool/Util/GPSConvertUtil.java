package com.yzq.ScanTool.Util;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;

/**
 * Created by 73843 on 2017/7/1.
 */

public class GPSConvertUtil {
    public static LatLng convertFromCommToBdll09(LatLng latLng){
        CoordinateConverter converter = new CoordinateConverter();
        converter.from(CoordinateConverter.CoordType.GPS);
        converter.coord(latLng);
        return converter.convert();
    }
}
