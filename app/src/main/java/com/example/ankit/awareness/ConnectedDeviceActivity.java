package com.example.ankit.awareness;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.widget.DrawerLayout;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
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
import java.util.Calendar;
import java.util.Vector;

public class ConnectedDeviceActivity extends AppCompatActivity{

    AnimationDrawable batteryani;
    AnimationDrawable kettleani;

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;

    private DrawerLayout connectedDrawer;
    private NavigationView navConnected;

    private Toolbar mToolbar;

    AnalysisAdapter adapterConnected;

    //private ArrayAdapter<String> adapterConnected;
    private ListView deviceListConnected;

    //////////private DataHelper myDataHelper;

    private static final String TAG = "ConnectedDeviceActivity";

    PieChart pieChartConnected;
    private GestureDetectorCompat detector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connected_device);

        if( getIntent().getExtras() != null)
        {
            if(getIntent().getExtras().getString("STARTINGACTIVITY").equals("ConnectedDeviceActivity"))
                overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_bottom);
            else
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
        else
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        //overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        mToolbar = (Toolbar) findViewById(R.id.nav_action);
        setSupportActionBar(mToolbar);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

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

                    case R.id.daily_consumption:
                        Intent intent = new Intent(ConnectedDeviceActivity.this, ConnectedDeviceActivity.class);
                        intent.putExtra("STARTINGACTIVITY", "ConnectedDeviceActivity");
                        startActivity(intent);
                        return true;

                    case R.id.monthly_consumption:
                        goToMyAccountActivity();
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

        ////////////////myDataHelper = new DataHelper(getApplicationContext());

        ////////////////myDataHelper.emptyData();

        ////final AnalysisAdapter adapterConnected = new TrackAdapter(getApplicationContext(), R.layout.track_item);
        ////listView.setAdapter(friendsAdapter);

        adapterConnected = new AnalysisAdapter(getApplicationContext(), R.layout.analysis_item, "ConnectedDeviceActivity");

        ///adapterConnected = new ArrayAdapter<String>(this,
                //android.R.layout.simple_list_item_1, android.R.id.text1);

        final SharedPreferences myPref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        Boolean firstLogin = myPref.getBoolean("First Login", true);

        if(!firstLogin) {
            adapterConnected.add("All");
            adapterConnected.notifyDataSetChanged();
        }
        else {
            adapterConnected.add("No Connected Device");
            adapterConnected.notifyDataSetChanged();
        }


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
                /*
                if(dataSnapshot.child("status").getValue().toString().equals("connected"))
                    adapterConnected.add(dataSnapshot.getKey().toString());
                 */
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
        DatabaseHelper myDatabase = new DatabaseHelper(getApplicationContext());

        final Vector<String> myConnectedDevices = myDatabase.getAllDayDevice();

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
        /*
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
                    intent.putExtra("DEVICENAME", (String) adapterConnected.getItem(itemPosition));
                    intent.putExtra("STARTINGACTIVITY", "ConnectedDeviceActivity");
                    startActivity(intent);
                    //ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(ConnectedDeviceActivity.this, findViewById(R.id.DeviceListConnected), "graphedDevice");

                    //startActivity(intent, optionsCompat.toBundle());
                }
                else if (myStrings[0].equals("All"))
                {
                    intent.putExtra("DEVICENAME", "All");
                    intent.putExtra("STARTINGACTIVITY", "ConnectedDeviceActivity");
                    startActivity(intent);
                    //ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(ConnectedDeviceActivity.this, findViewById(R.id.DeviceListConnected), "graphedDevice");

                    //startActivity(intent, optionsCompat.toBundle());
                }
            }

        });
        */
    }

    void collectDeviceData(String addedDevice)
    {
        //////////////final int addedPosition = myDataHelper.addDevice(addedDevice);

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

                    Calendar currentDate = Calendar.getInstance();
                    int year = currentDate.get(Calendar.YEAR)%1000;
                    int month = currentDate.get(Calendar.MONTH) + 1;
                    int day = currentDate.get(Calendar.DAY_OF_MONTH);

                    long stamp = Long.parseLong(dataSnapshot.getKey().toString());

                    if(!((((stamp%(100000000L))/1000000L) != day) || (((stamp%(10000000000L))/100000000L) != month) || (((stamp%(1000000000000L))/10000000000L) != year)))
                    {
                        if(!adapterConnected.isAlreadyInList(deviceName)) {
                            adapterConnected.add(deviceName);
                            adapterConnected.notifyDataSetChanged();
                        }
                        //adapterConnected.remove(deviceName);
                        //adapterConnected.add(deviceName);
                    }

                    Log.d("testCase1", deviceName + " connected with value " + Double.parseDouble(dataSnapshot.getValue().toString()) + " at time " + String.format("%02d",Calendar.getInstance().get(Calendar.HOUR)) + String.format("%02d", Calendar.getInstance().get(Calendar.MINUTE)) + String.format("%02d", Calendar.getInstance().get(Calendar.SECOND)));
                    //////////////////myDataHelper.addData(addedPosition, Double.parseDouble(dataSnapshot.getValue().toString()));
                    setDataText();
                }
                else if(dataSnapshot.getKey().toString().equals("status"))
                {
                    myDatabase.changeDeviceStatus(deviceName, dataSnapshot.getValue().toString());

                    /*
                    if(dataSnapshot.getValue().toString().equals("connected"))
                        Toast.makeText(getApplicationContext(),deviceName + " is " + dataSnapshot.getValue().toString(), Toast.LENGTH_LONG).show();
                    */
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s)
            {
                if(dataSnapshot.getKey().toString().equals("status"))
                {
                    myDatabase.changeDeviceStatus(deviceName, dataSnapshot.getValue().toString());
                    //////Toast.makeText(getApplicationContext(),deviceName + " is now " + dataSnapshot.getValue().toString(), Toast.LENGTH_LONG).show();
                    setDataText();

                    /*
                    if(dataSnapshot.getValue().toString().equals("disconnected"))
                        adapterConnected.remove(deviceName);
                    else
                        adapterConnected.add(deviceName);
                        */
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

        /*
        Vector<String> allConnectedDevice = myDatabase.getAllStatusDevice("connected");
        Vector<Double> allConnectedData = myDatabase.getAllStatusData("connected");
        Vector<Double> connectedDeviceData = new Vector<Double>();
        */

        Vector<String> allDayDevice = myDatabase.getAllDayDevice();
        Vector<Double> dayDeviceData = new Vector<Double>();

        /*
        for(int i = 0; i < allConnectedDevice.size(); i++)
        {
            Log.d("DatabaseReturn", "Connected Device name: " + allConnectedDevice.elementAt(i));
            //Log.d("DatabaseReturn", "Connected Device data: " + allConnectedData.elementAt(i));
        }
        */

        ArrayList<PieEntry> yValues = new ArrayList<>();

        for(int i = 0; i < allDayDevice.size(); i++)
        {
            dayDeviceData.add(myDatabase.getSpecificDataTotal(allDayDevice.elementAt(i)));
            yValues.add(new PieEntry(dayDeviceData.elementAt(i).floatValue(), allDayDevice.elementAt(i)));
        }

        pieChartConnected.animateY(0, Easing.EasingOption.EaseInOutCubic); ////////////////////////////////////////////////

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

        //Log.d(TAG, "Total consumption is " + myDatabase.getTotalConsumption());
    }

    void goToAddDeviceActivity()
    {
        Intent intent = new Intent(ConnectedDeviceActivity.this, AddDeviceActivity.class);
        intent.putExtra("STARTINGACTIVITY", "ConnectedDeviceActivity");
        startActivity(intent);
}

    void goToConnectedDeviceActivity()
    {
        Intent intent = new Intent(ConnectedDeviceActivity.this, ConnectedDeviceActivity.class);
        startActivity(intent);
    }

    void goToMyAccountActivity()
    {
        Intent intent = new Intent(ConnectedDeviceActivity.this, MyAccountActivity.class);
        intent.putExtra("STARTINGACTIVITY", "ConnectedDeviceActivity");
        startActivity(intent);
    }

    void goToLiveActivity()
    {
        Intent intent = new Intent(ConnectedDeviceActivity.this, LiveActivity.class);
        intent.putExtra("STARTINGACTIVITY", "ConnectedDeviceActivity");

        startActivity(intent);
    }

    void refresh()
    {
        setDataText();
    }

    void signOut()
    {
        userSignOut();
        Intent intent = new Intent(ConnectedDeviceActivity.this, MainActivity.class);
        intent.putExtra("STARTINGACTIVITY", "ConnectedDeviceActivity");
        startActivity(intent);
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

            case R.id.live:
                goToLiveActivity();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
