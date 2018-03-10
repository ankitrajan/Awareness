package com.example.ankit.awareness;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class AddDeviceActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;

    private Button linkButton;

    private EditText deviceText;
    private EditText devicePasswordText;

    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        linkButton = (Button) findViewById(R.id.LinkDevice);

        deviceText = (EditText) findViewById(R.id.DeviceField);
        devicePasswordText = (EditText) findViewById(R.id.DevicePasswordField);

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
                                Toast.makeText(getApplicationContext(), "Device linked", Toast.LENGTH_LONG).show();

                                SharedPreferences myPref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
                                SharedPreferences.Editor editor = myPref.edit();
                                editor.putBoolean("First Login", false);
                                editor.apply();

                                verifyState(device);
                            }
                            else
                                Toast.makeText(getApplicationContext(),"Device already linked with another account", Toast.LENGTH_LONG).show();
                        }
                        else
                            Toast.makeText(getApplicationContext(),"Incorrect device password", Toast.LENGTH_LONG).show();
                    }
                    else
                        Toast.makeText(getApplicationContext(),"Device doesn't exist", Toast.LENGTH_LONG).show();
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

        SharedPreferences.Editor editor = myPref.edit();
        editor.putString("deviceID", deviceIdentification);
        editor.apply();


        if(!firstLogin)
        {
            DatabaseReference linkRef = databaseReference.child("Devices").child(deviceIdentification).child("linked");
            linkRef.setValue(1);

            databaseReference.child("Users").child(currentUserID).child("First Login").setValue(false);
            databaseReference.child("Users").child(currentUserID).child("Linked Device").setValue(deviceIdentification);

            goToMyAccountActivity();
        }
    }

    void goToMyAccountActivity()
    {
        Intent intent = new Intent(AddDeviceActivity.this, MyAccountActivity.class);
        startActivity(intent);
    }
}
