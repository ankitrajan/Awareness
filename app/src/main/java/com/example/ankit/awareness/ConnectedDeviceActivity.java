package com.example.ankit.awareness;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Vector;

public class ConnectedDeviceActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;

    private DrawerLayout connectedDrawer;
    private NavigationView navConnected;

    private ArrayAdapter<String> adapterConnected;
    private ListView deviceListConnected;

    private DataHelper myDataHelper;

    private static final String TAG = "ConnectedDeviceActivity";

    PieChart pieChartConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connected_device);

        connectedDrawer = (DrawerLayout) findViewById(R.id.drawer_layout_connected);

        navConnected = (NavigationView) findViewById(R.id.NavConnected);

        navConnected.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                item.setChecked(true);
                connectedDrawer.closeDrawers();
                item.setChecked(false);

                switch (item.getItemId())
                {
                    case R.id.sign_out:
                        signOut();
                        return true;

                    case R.id.settings:
                        startActivity(new Intent(ConnectedDeviceActivity.this, SettingsActivity.class));
                        return true;

                    case R.id.configure_device:
                        goToAddDeviceActivity();
                        return true;

                    default:
                        return false;
                }
            }
        });



        pieChartConnected = (PieChart) findViewById(R.id.piechartConnected);
        pieChartConnected.setUsePercentValues(true);
        pieChartConnected.getDescription().setEnabled(false);
        //pieChart.setExtraOffsets(1,1,100,10);

        pieChartConnected.setDragDecelerationFrictionCoef(0.99f);

        pieChartConnected.setDrawHoleEnabled(true);
        pieChartConnected.setHoleColor(Color.TRANSPARENT);
        pieChartConnected.setTransparentCircleRadius(85f);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        myDataHelper = new DataHelper(getApplicationContext());

        myDataHelper.emptyData();

        adapterConnected = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1);

        final SharedPreferences myPref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        Boolean firstLogin = myPref.getBoolean("First Login", true);

        if(!firstLogin)
            adapterConnected.add("All");
        else
            adapterConnected.add("No Connected Device");


        String userID = firebaseAuth.getCurrentUser().getUid();

        deviceListConnected = (ListView) findViewById(R.id.DeviceListConnected);

        final DatabaseHelper myDatabase = new DatabaseHelper(getApplicationContext());

        DatabaseReference deviceRef = databaseReference.child("Users").child(userID).getRef();

        deviceRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild("Linked Device"))
                {
                    SharedPreferences myPref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
                    SharedPreferences.Editor editor = myPref.edit();
                    editor.putString("deviceID", dataSnapshot.child("Linked Device").getValue().toString());
                    editor.apply();
                }
                else
                {
                    SharedPreferences myPref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
                    SharedPreferences.Editor editor = myPref.edit();
                    editor.putString("deviceID", "No device");
                    editor.apply();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        String deviceID = myPref.getString("deviceID", "No device");

        DatabaseReference applianceRef = databaseReference.child("Devices").child(deviceID).child("Appliances").getRef();

        applianceRef.addChildEventListener(new ChildEventListener()
        {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s)
            {
                //Log.d(TAG, "Device added: " + dataSnapshot.getKey().toString() + " with status " + dataSnapshot.child("status").getValue().toString());

                myDatabase.addDevice(dataSnapshot.getKey().toString(), dataSnapshot.child("status").getValue().toString());
                collectDeviceData(dataSnapshot.getKey().toString());
                if(dataSnapshot.child("status").getValue().toString().equals("connected"))
                    adapterConnected.add(dataSnapshot.getKey().toString());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot)
            {
                myDatabase.deleteDevice(dataSnapshot.getKey().toString());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        displayList();
    }

    void displayList()
    {
        Log.d(TAG, "In displayList");

        DatabaseHelper myDatabase = new DatabaseHelper(getApplicationContext());

        final Vector<String> myConnectedDevices = myDatabase.getAllStatusDevice("connected");

        Log.d(TAG, "Returned connectedDevice size is " + myConnectedDevices.size());

        for (int i = 0; i < myConnectedDevices.size(); i++)
            Log.d(TAG, "Device added: " + myConnectedDevices.elementAt(i));

        final String myStrings[] = new String[myConnectedDevices.size()+1];

        SharedPreferences myPref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        Boolean firstLogin = myPref.getBoolean("First Login", true);

        if(!firstLogin)
            myStrings[0] = "All";
        else
            myStrings[0] = "No Connected Device";

        for(int j = 1; j < myConnectedDevices.size()+1; j++)
        {
            myStrings[j] = myConnectedDevices.elementAt(j-1);
        }

        deviceListConnected.setAdapter(adapterConnected);

        // ListView Item Click Listener
        deviceListConnected.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // ListView Clicked item index
                int itemPosition = position;

                // ListView Clicked item value
                //String itemValue = (String) deviceList.getItemAtPosition(position);

                Intent intent= new Intent(ConnectedDeviceActivity.this, AnalysisActivity.class);
                if(itemPosition != 0)
                {
                    intent.putExtra("DEVICENAME", adapterConnected.getItem(itemPosition));
                    startActivity(intent);
                }
                else if (myStrings[0].equals("All"))
                {
                    intent.putExtra("DEVICENAME", "All");
                    startActivity(intent);
                }
            }

        });
    }

    void collectDeviceData(String addedDevice)
    {
        final int addedPosition = myDataHelper.addDevice(addedDevice);

        final SharedPreferences myPref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        final String deviceID = myPref.getString("deviceID", "No device");

        final DatabaseHelper myDatabase = new DatabaseHelper(getApplicationContext());

        final String deviceName = addedDevice;
        DatabaseReference deviceRef = databaseReference.child("Devices").child(deviceID).child("Appliances").child(deviceName).getRef();

        deviceRef.addChildEventListener(new ChildEventListener()
        {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s)
            {
                if(!(dataSnapshot.getKey().toString().equals("status")))
                {
                    myDatabase.addData(deviceName, Long.parseLong(dataSnapshot.getKey().toString()), Double.parseDouble(dataSnapshot.getValue().toString()), getApplication());
                    myDataHelper.addData(addedPosition, Double.parseDouble(dataSnapshot.getValue().toString()));
                    setDataText();
                }
                else if(dataSnapshot.getKey().toString().equals("status"))
                {
                    myDatabase.changeDeviceStatus(deviceName, dataSnapshot.getValue().toString());

                    if(dataSnapshot.getValue().toString().equals("connected"))
                        Toast.makeText(getApplicationContext(),deviceName + " is " + dataSnapshot.getValue().toString(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s)
            {
                if(dataSnapshot.getKey().toString().equals("status"))
                {
                    myDatabase.changeDeviceStatus(deviceName, dataSnapshot.getValue().toString());
                    Toast.makeText(getApplicationContext(),deviceName + " is now " + dataSnapshot.getValue().toString(), Toast.LENGTH_LONG).show();
                    setDataText();

                    if(dataSnapshot.getValue().toString().equals("disconnected"))
                        adapterConnected.remove(deviceName);
                    else
                        adapterConnected.add(deviceName);
                }

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    void setDataText()
    {
        DatabaseHelper myDatabase = new DatabaseHelper(getApplicationContext());

        Vector<String> allConnectedDevice = myDatabase.getAllStatusDevice("connected");
        Vector<Double> allConnectedData = myDatabase.getAllStatusData("connected");
        Vector<Double> connectedDeviceData = new Vector<Double>();

        ArrayList<PieEntry> yValues = new ArrayList<>();

        for(int i = 0; i < allConnectedDevice.size(); i++)
        {
            connectedDeviceData.add(myDatabase.getSpecificDataTotal(allConnectedDevice.elementAt(i)));
            yValues.add(new PieEntry(connectedDeviceData.elementAt(i).floatValue(), allConnectedDevice.elementAt(i)));
        }

        double total = 0;

        for(int i = 0; i < allConnectedData.size(); i++)
            total += allConnectedData.elementAt(i);

        pieChartConnected.animateY(2500, Easing.EasingOption.EaseInOutCubic);

        PieDataSet dataSet = new PieDataSet(yValues,"Appliances");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(4f);
        dataSet.setColors(ColorCustomized.DARK_COLORS);

        PieData data = new PieData ((dataSet));
        data.setValueTextSize(10f);
        data.setValueTextColor(Color.WHITE);

        pieChartConnected.getLegend().setTextColor(Color.WHITE); //In case of error delete, changes font to white
        pieChartConnected.setHoleRadius(80);                    //In case of error delete, changing the chart radius
        pieChartConnected.setDrawEntryLabels(false);            //showing description under the percentage for piechart


        pieChartConnected.setData(data);
    }

    void goToAddDeviceActivity()
    {
        Intent intent = new Intent(ConnectedDeviceActivity.this, AddDeviceActivity.class);
        startActivity(intent);
    }

    void refresh()
    {
        setDataText();
    }

    void signOut()
    {
        userSignOut();
        startActivity(new Intent(ConnectedDeviceActivity.this, MainActivity.class));
        Toast.makeText(getApplicationContext(), "Signed out", Toast.LENGTH_LONG).show();
    }

    private void userSignOut()
    {
        firebaseAuth.signOut();
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
                refresh();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
