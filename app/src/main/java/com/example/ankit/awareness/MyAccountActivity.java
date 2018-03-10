package com.example.ankit.awareness;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Vector;

public class MyAccountActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;

    private ListView deviceList;

    private Button refreshButton;
    private Button signOutButton;

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setContentView(R.layout.activity_my_account);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();


        refreshButton = (Button) findViewById(R.id.Refresh);
        signOutButton = (Button) findViewById(R.id.SignOut);

        refreshButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                refresh();
            }
        });

        signOutButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                signOut();
            }
        });

        final SharedPreferences myPref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);

        String userID = firebaseAuth.getCurrentUser().getUid();

        deviceList = (ListView) findViewById(R.id.DeviceList);

        final DatabaseHelper myDatabase = new DatabaseHelper(getApplicationContext());

        DatabaseReference deviceRef = databaseReference.child("Users").child(userID).child("Linked Device").getRef();

        deviceRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                SharedPreferences myPref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
                SharedPreferences.Editor editor = myPref.edit();
                editor.putString("deviceID", dataSnapshot.getValue().toString());
                editor.apply();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        String deviceID = myPref.getString("deviceID", "No device");

        //Toast.makeText(getApplicationContext(),"DeviceID " + deviceID, Toast.LENGTH_LONG).show();

        DatabaseReference applianceRef = databaseReference.child("Devices").child(deviceID).child("Appliances").getRef();

        applianceRef.addChildEventListener(new ChildEventListener()
        {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s)
            {
                myDatabase.addDevice(dataSnapshot.getKey().toString(), "connected");
                collectDeviceData(dataSnapshot.getKey().toString());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s)
            {
                //Toast.makeText(getApplicationContext(), "child changed in " + dataSnapshot.getKey().toString(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot)
            {
                myDatabase.deleteDevice(dataSnapshot.getKey().toString());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                //Toast.makeText(getApplicationContext(),"In appliance: " + dataSnapshot.getKey().toString() + " moved", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //Toast.makeText(getApplicationContext(), "onCancelled", Toast.LENGTH_LONG).show();

            }
        });

        displayList();
    }

    void displayList()
    {
        DatabaseHelper myDatabase = new DatabaseHelper(getApplicationContext());

        final Vector<String> myDevices = myDatabase.getAllDevice();

        String myStrings[] = new String[myDevices.size()+1];

        myStrings[0] = "All";

        for(int j = 1; j < myDevices.size()+1; j++)
        {
            myStrings[j] = myDevices.elementAt(j-1);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, myStrings);

        // Assign adapter to ListView
        deviceList.setAdapter(adapter);

        // ListView Item Click Listener
        deviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // ListView Clicked item index
                int itemPosition = position;

                // ListView Clicked item value
                String  itemValue    = (String) deviceList.getItemAtPosition(position);

                Intent intent= new Intent(MyAccountActivity.this, AnalysisActivity.class);
                if(itemPosition != 0)
                    intent.putExtra("DEVICENAME", myDevices.elementAt(itemPosition-1));
                else
                    intent.putExtra("DEVICENAME", "All");

                startActivity(intent);
            }

        });
    }

    void collectDeviceData(String addedDevice)
    {

        final SharedPreferences myPref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        final String deviceID = myPref.getString("deviceID", "No device");

        //Toast.makeText(getApplicationContext(), "Device name: " + addedDevice + " deviceID: " + deviceID, Toast.LENGTH_LONG).show();

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
                }
                else if(dataSnapshot.getKey().toString().equals("status"))
                {
                    myDatabase.changeDeviceStatus(deviceName, dataSnapshot.getValue().toString());

                    if(dataSnapshot.getValue().toString().equals("connected"))
                        Toast.makeText(getApplicationContext(),deviceName + " is " + dataSnapshot.getValue().toString(), Toast.LENGTH_LONG).show();
                }
                //Toast.makeText(getApplicationContext(),"In device child added: " + deviceName + " added key " + dataSnapshot.getKey().toString(), Toast.LENGTH_LONG).show();
                //Toast.makeText(getApplicationContext(),"In device child added: " + deviceName + " added value " + dataSnapshot.getValue().toString(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s)
            {
                if(dataSnapshot.getKey().toString().equals("status"))
                {
                    myDatabase.changeDeviceStatus(deviceName, dataSnapshot.getValue().toString());
                }

                Toast.makeText(getApplicationContext(),deviceName + " is now " + dataSnapshot.getValue().toString(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot)
            {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s)
            {
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
            }
        });
    }

    void refresh()
    {
        startActivity(new Intent(MyAccountActivity.this, MyAccountActivity.class));
        Toast.makeText(getApplicationContext(), "Refreshing", Toast.LENGTH_LONG).show();
    }


    void signOut()
    {
        userSignOut();
        startActivity(new Intent(MyAccountActivity.this, MainActivity.class));
        Toast.makeText(getApplicationContext(), "Signed out", Toast.LENGTH_LONG).show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_my_account, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.sign_out:
                userSignOut();
                startActivity(new Intent(MyAccountActivity.this, MainActivity.class));
                Toast.makeText(getApplicationContext(), "Signed out", Toast.LENGTH_LONG).show();
                return true;

            case R.id.settings:
                startActivity(new Intent(MyAccountActivity.this, SettingsActivity.class));
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
