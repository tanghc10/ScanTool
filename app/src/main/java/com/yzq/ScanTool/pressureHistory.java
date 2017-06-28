package com.yzq.ScanTool;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.yzq.ScanTool.event.HttpGetEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

/**
 * Created by 73843 on 2017/6/27.
 */

public class pressureHistory extends Activity {
    private String Tag = "pressureHistory";
    private LineChartView lineChart;
    private ImageButton image_back;
    private List<PointValue> mPointValues = new ArrayList<PointValue>();
    private List<AxisValue> mAxisXValues = new ArrayList<AxisValue>();
    private ArrayList<String> time;
    private float[] pressure;
    private static final String HttpHost = "http://test.xiaoan110.com:8088/liquid/";

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        getHistory();
    }

    public void initView(){
        lineChart = (LineChartView) findViewById(R.id.line_chart);
        image_back = (ImageButton) findViewById(R.id.history_imageview_back);
        image_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onResume(){
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


    /*
     *  x轴的显示
     */
    private void getAxisXLables(ArrayList<String> time){
        for (int i = 0; i < time.size(); i++){
            mAxisXValues.add(new AxisValue(i).setLabel(time.get(i)));
        }
    }

    /*
     *  图表中的每个点的显示
     */
    private void getAxisXPoints(float[] pressure){
        for (int i = 0; i < pressure.length; i++){
            mPointValues.add(new PointValue(i, pressure[i]));
        }
    }

    /*
     *  画线
     */
    private void initLineChart(){
        initView();
        Line line = new Line(mPointValues).setColor(Color.parseColor("#FFCD41"));
        List<Line> lines = new ArrayList<Line>();
        line.setShape(ValueShape.CIRCLE);
        line.setCubic(false);//设置曲线是否平滑
        line.setStrokeWidth(3);//设置线条粗细
        line.setFilled(true);//设置是否填充曲线面积
        line.setHasLabels(false);//数据是否加上备注
        line.setHasLines(true);
        line.setHasPoints(false);
        lines.add(line);
        LineChartData data = new LineChartData();
        data.setLines(lines);

        //x轴
        Axis axisX = new Axis();
        axisX.setHasTiltedLabels(true);
        axisX.setTextColor(Color.parseColor("#D6D6D9"));
        axisX.setName("时间");
        axisX.setTextSize(10);
        axisX.setMaxLabelChars(8);
        axisX.setValues(mAxisXValues);
        data.setAxisXBottom(axisX);
        axisX.setHasLines(true);

        //y轴
        Axis axisY = new Axis();
        axisY.setName("深度");
        axisY.setTextSize(10);
        data.setAxisYLeft(axisY);

        lineChart.setInteractive(true);
        lineChart.setZoomType(ZoomType.HORIZONTAL);
        lineChart.setMaxZoom((float) 3);
//        lineChart.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
        lineChart.setLineChartData(data);
        lineChart.setVisibility(View.VISIBLE);

        Viewport v = new Viewport(lineChart.getMaximumViewport());
        v.left = 0;
        v.right = 7;
        lineChart.setCurrentViewport(v);
    }

    public void getHistory(){
        String DeviceId = "device001";
        String startTime = "2017-06-26-00:00:00";
        String endTime = "2017-06-26-21:10:10";
        String Url = HttpHost + "get_data/" + DeviceId + "?start=" + startTime + "&end=" + endTime;
        Log.d(Tag, Url);
        HttpManage.getHttpResult(Url, HttpManage.getType.GET_TYPE_HISTORY);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHttpGetEvent(HttpGetEvent event){
        Log.d("History", event.getResultStr());
        try {
            JSONArray jsonArray = new JSONArray(event.getResultStr());
            JSONObject jsonObject;
            String datetime;
            time = new ArrayList<String>();
            int length = jsonArray.length();
            Log.d("jsonArray.length", " " + length);
            pressure = new float[length];
            for (int i = 0; i < length; i++){
                jsonObject = jsonArray.getJSONObject(i);
                datetime = jsonObject.getString("Timestamp");
                if (datetime == null | datetime.length() == 0){
                    datetime = jsonObject.getString("datetime");
                }
                time.add(i, datetime);
                pressure[i] = (float)(jsonObject.getDouble("Pressure"));
                if (pressure[i] > 6){
                    pressure[i] = pressure[i] % 6;
                }
                pressure[i] = pressure[i] * 10;
            }
            getAxisXLables(time);
            getAxisXPoints(pressure);
            initLineChart();
        }catch (JSONException e){
            e.printStackTrace();
        }
    }
}
