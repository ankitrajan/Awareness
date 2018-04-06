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
//import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import static com.github.mikephil.charting.utils.ColorTemplate.COLORFUL_COLORS;

public class AnalysisActivity extends AppCompatActivity  implements GestureDetector.OnGestureListener{

    private GraphView deviceGraph;

    //private Toolbar mToolbar;

    private TextView applianceName;
    private TextView applianceTotal;
    private TextView applianceSave;

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

        String currentDevice = getIntent().getExtras().getString("DEVICENAME");

        applianceName = (TextView) findViewById(R.id.ApplianceName);
        applianceTotal = (TextView) findViewById(R.id.ApplianceTotal);
        applianceSave = (TextView) findViewById(R.id.ApplianceSave);

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

        ArrayList<Integer> elementsToRemove = new ArrayList<>();

        for(int i = 0; i < deviceStamp.size(); i++)
        {
            if(callingActivity.equals("MyAccountActivity"))
            {

                if(((deviceStamp.elementAt(i) %(10000000000L))/100000000L) != month)
                {
                    Log.d("GraphValues", "Something removed in monthly");
                    elementsToRemove.add(0, i);

                    /*
                    deviceStamp.remove(i);
                    deviceData.remove(i);
                    */
                }
            }
            else if(callingActivity.equals("ConnectedDeviceActivity"))
            {
                Log.d("StampTests", "Device stamp size = " + deviceStamp.size());
                Log.d("StampTests", "Device stamp " + deviceStamp.elementAt(i) + " has day = " + (deviceStamp.elementAt(i) %(100000000L))/1000000L);

                if(((deviceStamp.elementAt(i) %(100000000L))/1000000L) != day)
                {
                    Log.d("GraphValues", "Something removed in daily");
                    Log.d("StampTests", "Element to remove = " + i);
                    elementsToRemove.add(0, i);

                    /*
                    deviceStamp.remove(i);
                    deviceData.remove(i);
                    */
                }
            }
        }

        Log.d("StampTests", "elementsToRemove size = " + elementsToRemove.size());

        for(int i = 0; i < elementsToRemove.size(); i++)
        {
            Log.d("StampTests", "elementsToRemove at " + i + " = " + elementsToRemove.get(i));

            deviceStamp.removeElementAt(elementsToRemove.get(i));
            deviceData.removeElementAt(elementsToRemove.get(i));
        }

        Log.d("StampTests", "Device stamp size after removal = " + deviceStamp.size());
        Log.d("StampTests", "Device data size after removal = " + deviceData.size());

        Log.d("GraphValues", "deviceStamp size is " + deviceStamp.size());
        Log.d("GraphValues", "deviceData size is " + deviceData.size());

        double total = 0;

        for(int i = 0; i < deviceData.size(); i++)
            total += (deviceData.elementAt(i)/3600000);

        boolean highRate = false;

        if(total > 36)
            highRate = true;

        DecimalFormat newTotal = new DecimalFormat("###,###,###.######");

        applianceTotal.setText("Total: " + newTotal.format(total) + "kWh");

        //applianceTotal.setText("Total: " + String.format("%.4f", total) + "kW");


        if(!currentDevice.equals("All"))
        {
            if (!highRate)
                applianceSave.setText("You can save " + String.format("%.2f", ((total * 5.91)/100)) + "$ by reducing 10% of your " + currentDevice + " usage");
            else
                applianceSave.setText("You can save " + String.format("%.2f", (((36 * 5.91) + ((total - 36) * 9.12))/100)) + "$ by reducing 10% of your " + currentDevice + " usage");
        }
        else
        {
            if (!highRate)
                applianceSave.setText("You can save " + String.format("%.2f", ((total * 5.91)/100)) + "$ by reducing 10% of your overall consumption");
            else
                applianceSave.setText("You can save " + String.format("%.2f", (((36 * 5.91) + ((total - 36) * 9.12))/100)) + "$ by reducing 10% of your overall consumption");
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

                barEntries.add(new BarEntry(i, (thisMonth.get(i)/3600)));
            }

            Log.d("GraphValues", "thisMonth values printed");
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
                barEntries.add(new BarEntry(i, (thisDay.get(i)/3600)));
            }
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
        data.setValueTextColor(Color.WHITE);
        data.setBarWidth(0.9f);

        barChart.setData(data);
        barChart.getData().setDrawValues(true);

        barChart.setDrawBarShadow(false);
        barChart.setDrawValueAboveBar(true);
        barChart.setPinchZoom(false);
        barChart.setDrawGridBackground(false);
        barChart.setFitBars(false);
        barChart.setBackgroundColor(Color.TRANSPARENT);
        barChart.getXAxis().setTextColor(Color.WHITE);
        barChart.getAxisLeft().setTextColor(Color.WHITE);
        barChart.getAxisRight().setTextColor(Color.WHITE);
        barChart.getLegend().setEnabled(false);
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getDescription().setEnabled(false);
        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getXAxis().setDrawGridLines(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.getAxisLeft().setDrawAxisLine(false);

        String[] axisData;

        Log.d("StampTests", "Calling activity = " + callingActivity);

        if(callingActivity.equals("MyAccountActivity")) {
            axisData = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30"};
        }
        else //if(callingActivity.equals("ConnectedDeviceActivity"))
        {
            axisData = new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23"};
        }

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new MyXAxisValueFormatter(axisData));
        xAxis.setDrawAxisLine(false);

        data.setValueFormatter(new MyYValueFormatter());

        ////YAxis yAxis = barChart.getAxisLeft();
        ///yAxis.setValueFormatter(new MyYAxisValueFormatter());


        //barChart.setValueFormatter(new MyYAxisValueFormatter());


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

        //Toast.makeText(getApplicationContext(), "Refreshing", Toast.LENGTH_LONG).show();
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
        inflater.inflate(R.menu.menu_toolbar_my_account2, menu);
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



    public class MyYValueFormatter implements IValueFormatter {

        private DecimalFormat mFormat;

        public MyYValueFormatter() {
            // format values to 1 decimal digit
            mFormat = new DecimalFormat("###,###,###.##");
        }

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            // "value" represents the position of the label on the axis (x or y)
            if(value > 0)
            {
                Log.d("Formatter", value + " returned");
                return mFormat.format(value);
            } else
            {
                return "";
            }
        }
    }


    /*
    public class MyYAxisValueFormatter implements IAxisValueFormatter {

        private DecimalFormat mFormat;

        public MyYAxisValueFormatter() {
            // format values to 1 decimal digit
            mFormat = new DecimalFormat("###,###,##0.0");
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            // "value" represents the position of the label on the axis (x or y)
            String val = "";

            if(value != 0)
            {
                val = String.valueOf(value);
            }
            return mFormat.format(value) + " $";
        }

        /** this is only needed if numbers are returned, else return 0 */
        /*
        @Override
        public int getDecimalDigits() { return 1; }
        */
    //}


    /*
    private class MyValueFormatter implements IAxisValueFormatter {

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            // write your logic here
            if(value > 0)
                return value+"";
            else
                return "";
        }
    }
    */

    /*
    public class MyYAxisValueFormatter implements IAxisValueFormatter {

        private DecimalFormat mFormat;

        public MyYAxisValueFormatter() {
            mFormat = new DecimalFormat("###,###,##0");
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            //String val = ""
            String val = "";

            if (value != 0)
            {
                val = String.valueOf(value);
            }

            return mFormat.format(val);
        }

        /** this is only needed if numbers are returned, else return 0 */
    /*@Override
    public int getDecimalDigits() { return 1; }*/
    //}
}

