package com.example.ankit.awareness;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Vector;

public class MyAccountActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;

    private DrawerLayout accountDrawer;
    private NavigationView navMyAccount;

    private Toolbar mToolbar;

    AnalysisAdapter adapterMonthly;
    //private ArrayAdapter<String> adapterMonthly;
    private ListView deviceList;
    private TextView monthlyTotal;

    ////////private DataHelper myDataHelper;

    private static final String TAG = "MyAccountActivity";

    private BottomNavigationView bottomNavigationView;

    PieChart pieChart;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setContentView(R.layout.activity_my_account);

        if( getIntent().getExtras() != null)
        {
            if(getIntent().getExtras().getString("STARTINGACTIVITY").equals("ConnectedDeviceActivity"))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            else
                overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_bottom);
        }
        else
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

        mToolbar = (Toolbar) findViewById(R.id.nav_action);
        setSupportActionBar(mToolbar);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        accountDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        navMyAccount = (NavigationView) findViewById(R.id.NavMyAccount);

        navMyAccount.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                item.setChecked(true);
                accountDrawer.closeDrawers();
                item.setChecked(false);

                switch (item.getItemId())
                {
                    case R.id.sign_out:
                        signOut();
                        return true;

                    case R.id.daily_consumption:
                        goToConnectedDeviceActivity();
                        return true;

                    case R.id.monthly_consumption:
                        Intent intent = new Intent(MyAccountActivity.this, MyAccountActivity.class);
                        intent.putExtra("STARTINGACTIVITY", "MyAccountActivity");
                        startActivity(intent);
                        return true;

                    case R.id.configure_device:
                        goToAddDeviceActivity();
                        return true;

                    default:
                        return false;
                }
            }
        });

        monthlyTotal = (TextView) findViewById(R.id.MonthlyTotal);

        pieChart = (PieChart) findViewById(R.id.piechart);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        //pieChart.setExtraOffsets(1,1,100,10);

        pieChart.setDragDecelerationFrictionCoef(0.99f);

        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setTransparentCircleRadius(85f);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        ///////myDataHelper = new DataHelper(getApplicationContext());

        //////////myDataHelper.emptyData();

        adapterMonthly = new AnalysisAdapter(getApplicationContext(), R.layout.analysis_item, "MyAccountActivity");

        //adapterMonthly = new ArrayAdapter<String>(this,
                //android.R.layout.simple_list_item_1, android.R.id.text1);

        final SharedPreferences myPref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        Boolean firstLogin = myPref.getBoolean("First Login", true);

        Log.d(TAG, "First login value for adapter: " + firstLogin);

        if(!firstLogin)
            adapterMonthly.add("All","disconnected");
        else
            adapterMonthly.add("No Connected Device","disconnected");

        //final SharedPreferences myPref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);

        String userID = firebaseAuth.getCurrentUser().getUid();

        deviceList = (ListView) findViewById(R.id.DeviceList);

        final DatabaseHelper myDatabase = new DatabaseHelper(getApplicationContext());

        //DatabaseReference deviceRef = databaseReference.child("Users").child(userID).child("Linked Device").getRef();
        DatabaseReference deviceRef = databaseReference.child("Users").child(userID).getRef();

        deviceRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild("Linked Device")) {
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
                myDatabase.addDevice(dataSnapshot.getKey().toString(), "connected");
                collectDeviceData(dataSnapshot.getKey().toString());
                //adapter.add(dataSnapshot.getKey().toString());
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

        final Vector<String> myDevices = myDatabase.getAllMonthDevice();

        final String myStrings[] = new String[myDevices.size()+1];

        SharedPreferences myPref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        Boolean firstLogin = myPref.getBoolean("First Login", true);

        Log.d(TAG, "First login value for adapter: " + firstLogin);

        if(!firstLogin)
            myStrings[0] = "All";
        else
            myStrings[0] = "No Connected Device";

        for(int j = 1; j < myDevices.size()+1; j++)
        {
            myStrings[j] = myDevices.elementAt(j-1);
        }

        // Assign adapter to ListView
        deviceList.setAdapter(adapterMonthly);

        // ListView Item Click Listener
        deviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // ListView Clicked item index
                int itemPosition = position;

                // ListView Clicked item value
                //String itemValue = (String) deviceList.getItemAtPosition(position);

                Intent intent= new Intent(MyAccountActivity.this, AnalysisActivity.class);
                if(itemPosition != 0)
                {
                    intent.putExtra("DEVICENAME", (String) adapterMonthly.getItem(itemPosition));
                    intent.putExtra("STARTINGACTIVITY", "MyAccountActivity");
                    startActivity(intent);
                }
                else if (myStrings[0].equals("All"))
                {
                    intent.putExtra("DEVICENAME", "All");
                    intent.putExtra("STARTINGACTIVITY", "MyAccountActivity");
                    startActivity(intent);
                }
            }

        });
    }

    void collectDeviceData(String addedDevice)
    {
        ////////final int addedPosition = myDataHelper.addDevice(addedDevice);

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
                    ////////myDataHelper.addData(addedPosition, Double.parseDouble(dataSnapshot.getValue().toString()));


                    Calendar currentDate = Calendar.getInstance();
                    int year = currentDate.get(Calendar.YEAR)%1000;
                    int month = currentDate.get(Calendar.MONTH) + 1;

                    long stamp = Long.parseLong(dataSnapshot.getKey().toString());

                    if(!((((stamp%(10000000000L))/100000000L) != month) || (((stamp%(1000000000000L))/10000000000L) != year)))
                    {
                        if(!adapterMonthly.isAlreadyInList(deviceName)) {
                            adapterMonthly.add(deviceName, "disconnected");
                            adapterMonthly.notifyDataSetChanged();
                        }
                    }

                    //////////////////myDataHelper.addData(addedPosition, Double.parseDouble(dataSnapshot.getValue().toString()));
                    setDataText();


                }
                else if(dataSnapshot.getKey().toString().equals("status"))
                {
                    myDatabase.changeDeviceStatus(deviceName, dataSnapshot.getValue().toString());

                    if(dataSnapshot.getValue().toString().equals("connected"))
                    {
                        adapterMonthly.changeStatus(deviceName, "connected");
                        adapterMonthly.notifyDataSetChanged();
                    }
                    else if(dataSnapshot.getValue().toString().equals("disconnected"))
                    {
                        adapterMonthly.changeStatus(deviceName, "disconnected");
                        adapterMonthly.notifyDataSetChanged();
                    }

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

                    adapterMonthly.changeStatus(deviceName, dataSnapshot.getValue().toString());
                    adapterMonthly.notifyDataSetChanged();

                    //////Toast.makeText(getApplicationContext(), deviceName + " is now " + dataSnapshot.getValue().toString(), Toast.LENGTH_LONG).show();
                    setDataText();
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

        //Vector<Double> allData = myDatabase.getAllData();
        //Vector<String> allDevice = myDatabase.getAllDevice();
        //Vector<Double> deviceData = new Vector<Double>();


        ////get month device
        Vector<String> allMonthDevice = myDatabase.getAllMonthDevice();
        Vector<Double> monthDeviceData = new Vector<Double>();


        ArrayList<PieEntry> yValues = new ArrayList<>();

        /*
        yValues.add(new PieEntry(50f,"heater"));
        yValues.add(new PieEntry(23f,"mixer"));
        yValues.add(new PieEntry(14f,"refrigerator"));
        yValues.add(new PieEntry(35f,"laptop"));
        yValues.add(new PieEntry(40f,"television"));
        yValues.add(new PieEntry(23f,"lamp"));
        */

        double total = 0;

        for(int i = 0; i < allMonthDevice.size(); i++)
        {
            monthDeviceData.add(myDatabase.getSpecificMonthDataTotal(allMonthDevice.elementAt(i)));

            total += monthDeviceData.elementAt(i);

            yValues.add(new PieEntry(monthDeviceData.elementAt(i).floatValue(), allMonthDevice.elementAt(i)));
        }

        monthlyTotal.setText(String.format("%.2f", total));

        pieChart.animateY(2500,Easing.EasingOption.EaseInOutCubic);

        PieDataSet dataSet = new PieDataSet(yValues,"Appliances");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(4f);
        dataSet.setColors(ColorCustomized.DARK_COLORS);

        PieData data = new PieData ((dataSet));
        data.setValueTextSize(10f);
        data.setValueTextColor(Color.WHITE);

        pieChart.getLegend().setTextColor(Color.WHITE); //In case of error delete, changes font to white
        pieChart.setHoleRadius(80);                    //In case of error delete, changing the chart radius
        pieChart.setDrawEntryLabels(false);            //showing description under the percentage for piechart
        dataSet.setDrawValues(false);                  //not showing any values in the piechart

        pieChart.setData(data);

    }

    void goToAddDeviceActivity()
    {
        Intent intent = new Intent(MyAccountActivity.this, AddDeviceActivity.class);
        intent.putExtra("STARTINGACTIVITY", "MyAccountActivity");
        startActivity(intent);
    }

    void goToConnectedDeviceActivity()
    {
        Intent intent = new Intent(MyAccountActivity.this, ConnectedDeviceActivity.class);
        startActivity(intent);
    }

    void goToMyAccountActivity()
    {
        Intent intent = new Intent(MyAccountActivity.this, MyAccountActivity.class);
        startActivity(intent);
    }

    void goToLiveActivity()
    {
        Intent intent = new Intent(MyAccountActivity.this, LiveActivity.class);
        intent.putExtra("STARTINGACTIVITY", "MyAccountActivity");
        startActivity(intent);
    }

    void refresh()
    {
        setDataText();
    }

    void signOut()
    {
        userSignOut();
        Intent intent = new Intent(MyAccountActivity.this, MainActivity.class);
        intent.putExtra("STARTINGACTIVITY", "MyAccountActivity");
        startActivity(intent);

        Snackbar.make(findViewById(android.R.id.content), "Signed out", Snackbar.LENGTH_LONG).show();


        //Toast.makeText(getApplicationContext(), "Signed out", Toast.LENGTH_LONG).show();
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

    private void userSignOut()
    {
        firebaseAuth.signOut();
    }
}
