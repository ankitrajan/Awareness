package com.example.ankit.awareness;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

public class AnalysisActivity extends AppCompatActivity {

    private GraphView deviceGraph;

    private TextView applianceName;

    private Button refreshGraphButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);





 /*
        String currentDevice = getIntent().getExtras().getString("DEVICENAME");

        graph = (GraphView) findViewById(R.id.DeviceGraph);

        applianceName = (TextView) findViewById(R.id.ApplianceName);

        refreshGraphButton = (Button) findViewById(R.id.RefreshGraph);

        applianceName.setText(currentDevice);


        //generate Dates
        Calendar calendar = Calendar.getInstance();
        Date d1 = calendar.getTime();
        calendar.add(Calendar.DATE, 1);
        Date d2 = calendar.getTime();
        calendar.add(Calendar.DATE, 1);
        Date d3 = calendar.getTime();

/*
        Date d1 = new Date(2018, 12, 8, 20, 38, 15);
        Date d2 = new Date(2018, 12, 9, 20, 38, 15);
        Date d3 = new Date(2018, 12, 10, 20, 38, 15);


        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 1988);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        Date dateRepresentation = cal.getTime();
*/


// you can directly pass Date objects to DataPoint-Constructor
// this will convert the Date to double via Date#getTime()
        /*
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(d1, 1),
                new DataPoint(d2, 5),
                new DataPoint(d3, 3)
        });

        graph.addSeries(series);
*/
// set date label formatter
        //graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(getApplicationContext()));
        //graph.getGridLabelRenderer().setNumHorizontalLabels(3); // only 4 because of the space

// set manual x bounds to have nice steps
        //graph.getViewport().setMinX(d1.getTime());
        //graph.getViewport().setMaxX(d3.getTime());
        //graph.getViewport().setXAxisBoundsManual(true);

// as we use dates as labels, the human rounding to nice readable numbers
// is not necessary
        //graph.getGridLabelRenderer().setHumanRounding(false);



        String currentDevice = getIntent().getExtras().getString("DEVICENAME");

        deviceGraph = (GraphView) findViewById(R.id.DeviceGraph);

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

        long scaleFactor = 10000L;

        if(!(currentDevice.equals("All")))
        {
            deviceStamp = myDatabase.getSpecificStamp(currentDevice);
            deviceData = myDatabase.getSpecificData(currentDevice);
        }
        else
        {
            deviceStamp = myDatabase.getAllStamp();
            deviceData = myDatabase.getAllData();
            scaleFactor = 10000L;
        }
/*
        for(int i = 0; i < deviceData.size(); i++)
        {
            Toast.makeText(getApplicationContext(), "Data: " + deviceData.elementAt(i) + " at " + deviceStamp.elementAt(i)%(100L), Toast.LENGTH_LONG).show();
        }
*/
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



        /*
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[]{
                new DataPoint(0, 1),
                new DataPoint(1, 5),
                new DataPoint(2, 3),
                new DataPoint(3, 2),
                new DataPoint(4, 6),
                new DataPoint(5, 10),
                new DataPoint(6, 8)
        });
        */

        deviceGraph.addSeries(deviceSeries);




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
