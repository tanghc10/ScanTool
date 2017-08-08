package com.yzq.ScanTool;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.yzq.ScanTool.event.HttpGetEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
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
    private String deciceId;
    private LineChartView lineChart;
    private ImageButton image_back;
    private EditText start_year;
    private EditText start_month;
    private EditText start_day;
    private EditText end_year;
    private EditText end_month;
    private EditText end_day;
    private Button btn_getHistory;
    private List<PointValue> mPointValues = new ArrayList<PointValue>();
    private List<AxisValue> mAxisXValues = new ArrayList<AxisValue>();
    private ArrayList<String> time;
    private float[] pressure;
    private static final String HttpHost = "https://test.xiaoan110.com/liquid/";

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        deciceId = bundle.getString("deviceId");
        initView();
    }

    public void initView(){
        lineChart = (LineChartView) findViewById(R.id.line_chart);
        image_back = (ImageButton) findViewById(R.id.history_imageview_back);
        start_year = (EditText) findViewById(R.id.input_start_year);
        start_month = (EditText) findViewById(R.id.input_start_month);
        start_day = (EditText) findViewById(R.id.input_start_day);
        end_year = (EditText) findViewById(R.id.input_end_year);
        end_month = (EditText) findViewById(R.id.input_end_month);
        end_day = (EditText) findViewById(R.id.input_end_day);
        btn_getHistory = (Button) findViewById(R.id.btn_getHistory);
        Calendar now = Calendar.getInstance();
        Calendar tomorrow = new GregorianCalendar();
        tomorrow.add(tomorrow.DAY_OF_MONTH, 1);
        start_year.setText(""+now.get(Calendar.YEAR));
        end_year.setText(""+now.get(Calendar.YEAR));
        start_month.setText(""+(now.get(Calendar.MONTH)+1));
        end_month.setText(""+(now.get(Calendar.MONTH)+1));
        start_day.setText(""+now.get(Calendar.DAY_OF_MONTH));
        end_day.setText(""+tomorrow.get(Calendar.DAY_OF_MONTH));
        image_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btn_getHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(pressureHistory.this, "正在获取数据", Toast.LENGTH_SHORT).show();
                getHistory();
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
        Log.d("time.size", " " + time.size());
        for (int i = 0; i < time.size(); i++){
            mAxisXValues.add(new AxisValue(i).setLabel(time.get(i)));
        }
    }

    /*
     *  图表中的每个点的显示
     */
    private void getAxisXPoints(float[] pressure){
        Log.d("pressure.length", " " + pressure.length);
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
        axisX.setMaxLabelChars(10);
        axisX.setValues(mAxisXValues);
        data.setAxisXBottom(axisX);
        axisX.setHasLines(true);

        //y轴
        Axis axisY = new Axis();
        axisY.setName("压力");
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
        String startTime = start_year.getText().toString() + "-" +
                            start_month.getText().toString() + "-" +
                             start_day.getText().toString();
        String endTime = end_year.getText().toString() + "-" +
                          end_month.getText().toString() + "-" +
                           end_day.getText().toString();
        String Url = HttpHost + "get_data/" + deciceId + "?start=" + startTime + "&end=" + endTime;
        Log.d(Tag, Url);
        HttpManage.getHttpResult(Url, HttpManage.getType.GET_TYPE_HISTORY);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHttpGetEvent(HttpGetEvent event){
        try {
            Log.e(Tag, event.getResultStr());
            JSONArray jsonArray = new JSONArray(event.getResultStr());
            JSONObject jsonObject;
            String datetime;
            time = new ArrayList<String>();
            int length = jsonArray.length();
            if (length == 0){
                Toast.makeText(pressureHistory.this, "在此时间段无数据", Toast.LENGTH_LONG).show();
                return;
            }
            Toast.makeText(pressureHistory.this, "查询成功", Toast.LENGTH_LONG).show();
            pressure = new float[length];
            for (int i = 0; i < length; i++){
                jsonObject = jsonArray.getJSONObject(i);
                datetime = jsonObject.getString("Timestamp");
                if (datetime == null | datetime.length() == 0){
                    datetime = jsonObject.getString("datetime");
                }
                time.add(i, datetime);
                pressure[i] = (float)(jsonObject.getDouble("Pressure"));
            }
            mAxisXValues.clear();
            mPointValues.clear();
            getAxisXLables(time);
            getAxisXPoints(pressure);
            initLineChart();
        }catch (JSONException e){
            e.printStackTrace();
        }
    }
}
