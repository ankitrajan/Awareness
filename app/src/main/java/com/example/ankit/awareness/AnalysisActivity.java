package com.example.ankit.awareness;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

public class AnalysisActivity extends AppCompatActivity {

    private GraphView deviceGraph;

    private TextView applianceName;

    private Button refreshGraphButton;

    private BarChart barChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_bottom);

        barChart = (BarChart) findViewById(R.id.barchart);

        barChart.setDrawBarShadow(false);
        barChart.setDrawValueAboveBar(true);
        barChart.setMaxVisibleValueCount(50);
        //barChart.setVisibleXRange(0, 31);
        barChart.setPinchZoom(false);
        barChart.setDrawGridBackground(true);

        String currentDevice = getIntent().getExtras().getString("DEVICENAME");

        applianceName = (TextView) findViewById(R.id.ApplianceName);

        refreshGraphButton = (Button) findViewById(R.id.RefreshGraph);

        applianceName.setText(currentDevice);

        refreshGraphButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                refreshGraph();
            }
        });

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
            Log.d("AnalysisActivity", "Device Stamp added: " + deviceStamp.elementAt(i));

        for(int i = 0; i < deviceData.size(); i++)
            Log.d("AnalysisActivity", "Device Stamp added: " + deviceData.elementAt(i));
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
        startActivity(newIntent);

        Toast.makeText(getApplicationContext(), "Refreshing", Toast.LENGTH_LONG).show();
    }
}
