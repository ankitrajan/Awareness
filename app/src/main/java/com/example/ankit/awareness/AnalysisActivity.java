package com.example.ankit.awareness;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import static com.github.mikephil.charting.utils.ColorTemplate.COLORFUL_COLORS;

public class AnalysisActivity extends AppCompatActivity  implements GestureDetector.OnGestureListener{

    private GraphView deviceGraph;

    //private Toolbar mToolbar;

    private TextView applianceName;

    private Button refreshGraphButton;

    private BarChart barChart;
    private GestureDetectorCompat detector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_bottom);

        if(getIntent().getExtras().getString("REFRESH") != null)
        {
            overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_bottom);
        }
        else {
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        }

        String callingActivity = getIntent().getExtras().getString("STARTINGACTIVITY");

        //mToolbar = (Toolbar) findViewById(R.id.nav_action);
        //setSupportActionBar(mToolbar);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        barChart = (BarChart) findViewById(R.id.barchart);

        detector = new GestureDetectorCompat(this, this);

        barChart.setDrawBarShadow(false);
        barChart.setDrawValueAboveBar(true);
        //barChart.setMaxVisibleValueCount(50);
        //barChart.setVisibleXRange(0, 31);
        barChart.setPinchZoom(false);
        //barChart.setDrawGridBackground(false);
        barChart.setFitBars(false);
        barChart.setBackgroundColor(Color.BLUE);

        String currentDevice = getIntent().getExtras().getString("DEVICENAME");

        applianceName = (TextView) findViewById(R.id.ApplianceName);

        //refreshGraphButton = (Button) findViewById(R.id.RefreshGraph);

        applianceName.setText(currentDevice);

        /*refreshGraphButton.setOnClickListener(new View.OnClickListener()                      //removing the refresh button
        {
            @Override
            public void onClick(View v)
            {
                refreshGraph();
            }
        }); */

        DatabaseHelper myDatabase = new DatabaseHelper(getApplicationContext());

        Vector<Long> deviceStamp;
        Vector<Double> deviceData;

        if(!(currentDevice.equals("All")))
        {
            deviceStamp = myDatabase.getSpecificStamp(currentDevice);
            deviceData = myDatabase.getSpecificData(currentDevice);
        }
        else
        {
            deviceStamp = myDatabase.getAllStamp();
            deviceData = myDatabase.getAllData();
            //scaleFactor = 10000L;
        }

        for(int i = 0; i < deviceStamp.size(); i++)
            Log.d("GraphValues", "Device Stamp added: " + deviceStamp.elementAt(i));

        for(int i = 0; i < deviceData.size(); i++)
            Log.d("GraphValues", "Device data added: " + deviceData.elementAt(i));

        Calendar currentDate = Calendar.getInstance();
        int year = currentDate.get(Calendar.YEAR)%1000;
        int month = currentDate.get(Calendar.MONTH) + 1;
        int day = currentDate.get(Calendar.DAY_OF_MONTH);

        ArrayList<Float> thisMonth = new ArrayList<>();
        ArrayList<Float> thisDay = new ArrayList<>();

        ArrayList<BarEntry> barEntries = new ArrayList<>();

        for(int i = 0; i < deviceStamp.size(); i++)
        {
            if(callingActivity.equals("MyAccountActivity"))
            {
                if(((deviceStamp.elementAt(i) %(10000000000L))/100000000L) != month)
                {
                    deviceStamp.remove(i);
                    deviceData.remove(i);
                }
            }
            else if(callingActivity.equals("ConnectedDeviceActivity"))
            {
                if(((deviceStamp.elementAt(i) %(100000000L))/1000000L) != day)
                {
                    deviceStamp.remove(i);
                    deviceData.remove(i);
                }
            }
            else if(callingActivity.equals("LiveActivity"))
            {

            }
        }

        int totalDays = 0;
        int totalHours = 24;

        if (month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12)
        {
            totalDays = 31;
        }
        else if (month == 2)
        {
            if(year%4 != 0)
                totalDays = 28;
            else
                totalDays = 29;
        }
        else
        {
            totalDays = 30;
        }

        Log.d("GraphValues", "TotalDays " + totalDays);


        if(callingActivity.equals("MyAccountActivity")) {

            for (int i = 0; i < totalDays; i++) {
                thisMonth.add(0f);
            }

            Log.d("GraphValues", "thisMonth length = " + thisMonth.size());

            for (int i = 0; i < deviceData.size(); i++) {
                thisMonth.set((((int) (((deviceStamp.elementAt(i) % (100000000L)) / 1000000L))) - 1), thisMonth.get((((int) (((deviceStamp.elementAt(i) % (100000000L)) / 1000000L)))) - 1) + (deviceData.elementAt(i)).floatValue());
            }

            for(int i = 0; i < totalDays; i++)
            {
                Log.d("GraphValues", "Day value " + thisMonth.get(i));
                barEntries.add(new BarEntry(i, thisMonth.get(i)));
            }
        }
        else if(callingActivity.equals("ConnectedDeviceActivity"))
        {
            for (int i = 0; i < totalHours; i++) {
                thisDay.add(0f);
            }


            for (int i = 0; i < deviceData.size(); i++) {
                thisDay.set((((int) (((deviceStamp.elementAt(i) % (1000000L)) / 10000L)))), thisDay.get((((int) (((deviceStamp.elementAt(i) % (1000000L)) / 10000L))))) + (deviceData.elementAt(i)).floatValue());
            }

            for(int i = 0; i < totalHours; i++)
            {
                Log.d("GraphValues", "Day value " + thisDay.get(i));
                barEntries.add(new BarEntry(i, thisDay.get(i)));
            }
        }
        else if(callingActivity.equals("LiveActivity"))
        {

        }

        /*
        for(int i = 0; i < deviceData.size(); i++)
        {
            barEntries.add(new BarEntry((float) (((deviceStamp.elementAt(i) % (100000000L)) / 1000000L)), (deviceData.elementAt(i)).floatValue()));
            Log.d("GraphValues", "Device day " + (float)(((deviceStamp.elementAt(i)%(100000000L))/1000000L)));
            Log.d("GraphValues", "Device data " + (deviceData.elementAt(i)).floatValue());
        }
        */

        BarDataSet barDataSet = new BarDataSet(barEntries, "Data Set1");
        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);

        BarData data = new BarData(barDataSet);
        data.setBarWidth(0.9f);

        barChart.setData(data);

        String[] axisData;
        if(callingActivity.equals("ConnectedDeviceActivity"))
        {
            axisData = new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"};
        }
        else
        {
            axisData = new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23"};
        }

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new MyXAxisValueFormatter(axisData));




        /*
        for(int i = 0; i < deviceData.size(); i++)
        {
            Toast.makeText(getApplicationContext(), "Data: " + deviceData.elementAt(i) + " at " + deviceStamp.elementAt(i)%(100L), Toast.LENGTH_LONG).show();
        }





        barEntries.add(new BarEntry(1,40f));
        barEntries.add(new BarEntry(2,44f));
        barEntries.add(new BarEntry(3,30f));
        barEntries.add(new BarEntry(4,30f));

        BarDataSet barDataSet = new BarDataSet(barEntries, "DataSet1");
        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);

        BarData data = new BarData(barDataSet);
        data.setBarWidth(0.9f);

        barChart.setData(data);

*/






        /*
        Log.d("AnalysisActivity", "Current device: " + currentDevice);

        ArrayList<BarEntry> barEntries = new ArrayList<>(deviceData.size());

        for(int i = 0; i < deviceData.size(); i++)
        {
            //Toast.makeText(getApplicationContext(), "Data: " + deviceData.elementAt(i) + " at " + deviceStamp.elementAt(i)%(100L), Toast.LENGTH_LONG).show();
            double totalValue = 0;

            for (int j = 0; j < (i+1); j++)
                totalValue += (double)(deviceData.elementAt(j));

            Log.d("AnalysisActivity", "Device Stamp: " + deviceStamp.elementAt(i).intValue() + " with value " + (float)totalValue);

            barEntries.add(i, new BarEntry(deviceStamp.elementAt(i).intValue(), (float)totalValue));
        }

        BarDataSet barDataSet = new BarDataSet(barEntries, "DataSet1");
        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);

        BarData data = new BarData(barDataSet);
        data.setBarWidth(0.9f);

        barChart.setData(data);
        */










/*
        DataPoint allData[] = new DataPoint[deviceData.size()];

        for(int i = 0; i < deviceData.size(); i++)
        {
            //Toast.makeText(getApplicationContext(), "Data: " + deviceData.elementAt(i) + " at " + deviceStamp.elementAt(i)%(100L), Toast.LENGTH_LONG).show();
            double totalValue = 0;

            for (int j = 0; j < (i+1); j++)
                totalValue += (double)(deviceData.elementAt(j));

            allData[i] = new DataPoint(deviceStamp.elementAt(i)%(scaleFactor), totalValue);
        }

        LineGraphSeries<DataPoint> deviceSeries = new LineGraphSeries<>(allData);


        deviceGraph.addSeries(deviceSeries);
*/



        /////deviceGraph.addSeries(series);

    }

    void refreshGraph()
    {
        Intent newIntent= new Intent(AnalysisActivity.this, AnalysisActivity.class);
        newIntent.putExtra("DEVICENAME", getIntent().getExtras().getString("DEVICENAME"));
        newIntent.putExtra("STARTINGACTIVITY", getIntent().getExtras().getString("STARTINGACTIVITY"));
        newIntent.putExtra("REFRESH", "Yes");
        startActivity(newIntent);

        Toast.makeText(getApplicationContext(), "Refreshing", Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

        if(e2.getX() > e1.getX()) {
            String callingActivity = getIntent().getExtras().getString("STARTINGACTIVITY");

            if(callingActivity.equals("ConnectedDeviceActivity"))
            {
                Intent intent = new Intent(AnalysisActivity.this, ConnectedDeviceActivity.class);
                startActivity(intent);
                return true;
            }
            else if(callingActivity.equals("LiveActivity"))
            {
                Intent intent = new Intent(AnalysisActivity.this, LiveActivity.class);
                intent.putExtra("STARTINGACTIVITY", "LiveActivity");
                startActivity(intent);
                return true;
            }
            else
            {
                Intent intent = new Intent(AnalysisActivity.this, MyAccountActivity.class);
                startActivity(intent);
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        detector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_toolbar_my_account, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.refresh:
                refreshGraph();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public class MyXAxisValueFormatter implements IAxisValueFormatter
    {
        private String[] mValues;

        public MyXAxisValueFormatter(String[] values)
        {
            this.mValues = values;
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis)
        {
            return mValues[(int)value];
        }

    }
}

