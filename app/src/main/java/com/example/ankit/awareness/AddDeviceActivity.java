package com.example.ankit.awareness;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

public class AddDeviceActivity extends AppCompatActivity implements GestureDetector.OnGestureListener{

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;

    private Button linkButton;

    private TextView currentDevice;

    private EditText deviceText;
    private EditText devicePasswordText;

    private String currentUserID;

    private GestureDetectorCompat detector;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        linkButton = (Button) findViewById(R.id.LinkDevice);

        currentDevice = (TextView) findViewById(R.id.CurrentDevice);

        deviceText = (EditText) findViewById(R.id.DeviceField);
        devicePasswordText = (EditText) findViewById(R.id.DevicePasswordField);

        detector = new GestureDetectorCompat(this, this);

        final String currentUser = firebaseAuth.getCurrentUser().getUid().toString();

        DatabaseReference userHasDeviceRef = databaseReference.child("Users").child(currentUser).getRef();

        userHasDeviceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("Linked Device")) {
                    currentDevice.setText("Currently linked device: " + dataSnapshot.child("Linked Device").getValue().toString());
                    linkButton.setText("Change Device");
                }
                else
                {
                    currentDevice.setText("No device currently linked");
                    linkButton.setText("Link Device");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        currentUserID = firebaseAuth.getCurrentUser().getUid().toString();

        linkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linkDevice();
            }
        });
    }


    private void linkDevice()
    {
        final String device = deviceText.getText().toString();
        final String devicePassword = devicePasswordText.getText().toString();

        DatabaseReference deviceRef = databaseReference.child("Devices").child(device);

        if (!(TextUtils.isEmpty(device) || TextUtils.isEmpty(devicePassword)))
        {
            deviceRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    if (dataSnapshot.hasChild("linked"))
                    {
                        if(dataSnapshot.child("password").getValue().equals(devicePassword))
                        {
                            if(dataSnapshot.child("linked").getValue().toString().equals("0"))
                            {
                                Snackbar.make(findViewById(android.R.id.content), "Device linked", Snackbar.LENGTH_LONG).show();

                                //Toast.makeText(getApplicationContext(), "Device linked", Toast.LENGTH_LONG).show();

                                SharedPreferences myPref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
                                SharedPreferences.Editor editor = myPref.edit();
                                editor.putBoolean("First Login", false);
                                editor.apply();

                                verifyState(device);
                            }
                            else
                            {
                                Snackbar.make(findViewById(android.R.id.content), "Device already linked with an account", Snackbar.LENGTH_LONG).show();
                                //Toast.makeText(getApplicationContext(), "Device already linked with an account", Toast.LENGTH_LONG).show();
                            }
                        }
                        else
                        {
                            Snackbar.make(findViewById(android.R.id.content), "Incorrect device password", Snackbar.LENGTH_LONG).show();

                            //Toast.makeText(getApplicationContext(), "Incorrect device password", Toast.LENGTH_LONG).show();

                        }
                    }
                    else
                    {
                        Snackbar.make(findViewById(android.R.id.content), "Device doesn't exist", Snackbar.LENGTH_LONG).show();


                        //Toast.makeText(getApplicationContext(), "Device doesn't exist", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });
        }
    }

    void verifyState(String deviceIdentification)
    {
        SharedPreferences myPref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        Boolean firstLogin = myPref.getBoolean("First Login", true);
        String previousDevice = myPref.getString("deviceID", "No device");

        SharedPreferences.Editor editor = myPref.edit();
        editor.putString("deviceID", deviceIdentification);
        editor.apply();


        if(!firstLogin)
        {
            DatabaseReference linkRef = databaseReference.child("Devices").child(deviceIdentification).child("linked");
            linkRef.setValue(1);

            if (!previousDevice.equals("none"))
                databaseReference.child("Devices").child(previousDevice).child("linked").setValue(0);

            databaseReference.child("Users").child(currentUserID).child("First Login").setValue(false);
            databaseReference.child("Users").child(currentUserID).child("Linked Device").setValue(deviceIdentification);

            currentDevice.setText("Currently linked device: " + deviceIdentification);
            Snackbar.make(findViewById(android.R.id.content), "Device Linked", Snackbar.LENGTH_LONG).show();

            //goToMyAccountActivity();
        }
    }

    void goToMyAccountActivity()
    {
        Intent intent = new Intent(AddDeviceActivity.this, ConnectedDeviceActivity.class);
        startActivity(intent);
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

        if(e2.getX() > e1.getX())
        {
            String callingActivity = getIntent().getExtras().getString("STARTINGACTIVITY");

            if(callingActivity.equals("ConnectedDeviceActivity"))
            {
                Intent intent = new Intent(AddDeviceActivity.this, ConnectedDeviceActivity.class);
                //intent.putExtra("STARTINGACTIVITY", "AddDeviceActivity");
                startActivity(intent);
                return true;
            }
            else if(callingActivity.equals("LiveActivity"))
            {
                Intent intent = new Intent(AddDeviceActivity.this, LiveActivity.class);
                intent.putExtra("STARTINGACTIVITY", "AddDeviceActivity");
                startActivity(intent);
                return true;
            }
            else
            {
                Intent intent = new Intent(AddDeviceActivity.this, MyAccountActivity.class);
                //intent.putExtra("STARTINGACTIVITY", "AddDeviceActivity");
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
}
