package com.example.ankit.awareness;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
{

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authListener;
    private DatabaseReference databaseReference;

    private EditText emailText;
    private EditText passwordText;

    protected Button loginButton;
    protected Button resetPasswordButton;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //getActionBar().setDisplayHomeAsUpEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();

        Log.d(TAG,"connected to firebase");

        emailText = (EditText) findViewById(R.id.EmailField);
        passwordText = (EditText) findViewById(R.id.PasswordField);

        loginButton = (Button) findViewById(R.id.Login);
        resetPasswordButton = (Button) findViewById(R.id.ResetPassword);

        DatabaseHelper myDatabase = new DatabaseHelper(getApplicationContext());

        myDatabase.deleteAllData();
        myDatabase.deleteAllDevice();

        firebaseAuth.signOut();

        authListener = new FirebaseAuth.AuthStateListener()
        {

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth)
            {
                //Toast.makeText(MainActivity.this, "Auth changed", Toast.LENGTH_LONG).show();
                if (firebaseAuth.getCurrentUser() != null)
                {
                    if (firebaseAuth.getCurrentUser().isEmailVerified())
                    {
                        databaseReference = FirebaseDatabase.getInstance().getReference();

                        final String currentUser = firebaseAuth.getCurrentUser().getUid().toString();

                        DatabaseReference userHasDeviceRef = databaseReference.child("Users").child(currentUser).getRef();

                        userHasDeviceRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChild("Linked Device")) {
                                    SharedPreferences myPref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
                                    SharedPreferences.Editor editor = myPref.edit();
                                    editor.putString("deviceID", dataSnapshot.child("Linked Device").getValue().toString());
                                    editor.putString("UserID", currentUser);
                                    editor.putBoolean("First Login", false);
                                    editor.apply();
                                    goToMyAccountActivity();
                                }
                                else
                                {
                                    SharedPreferences myPref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
                                    SharedPreferences.Editor editor = myPref.edit();
                                    editor.putString("UserID", currentUser);
                                    editor.putBoolean("First Login", true);
                                    editor.apply();
                                    //goToMyAccountActivity();
                                    goToAddDeviceActivity();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }
        };

        loginButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                startLogin();
            }
        });

        resetPasswordButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                goToResetActivity();
            }
        });
    }


    protected void onStart()
    {
        super.onStart();

        firebaseAuth.addAuthStateListener(authListener);
    }

    void startLogin()
    {
        FirebaseAuth.getInstance().signOut();

        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        if(!(TextUtils.isEmpty(email) || TextUtils.isEmpty(password)))
        {
            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>()
            {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task)
                {
                    if (!(task.isSuccessful()))
                    {
                        try {
                            throw task.getException();
                        } catch(FirebaseAuthInvalidUserException e) {
                            emailText.setError("Email incorrect");
                            //Toast.makeText(MainActivity.this, "Email incorrect", Toast.LENGTH_LONG).show();
                        } catch(Exception e) {
                            //Toast.makeText(MainActivity.this, "Password incorrect", Toast.LENGTH_LONG).show();
                            passwordText.setError("Password incorrect");
                        }
                        //Toast.makeText(MainActivity.this, "Email/Password incorrect", Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                        if (user.isEmailVerified())
                        {
                            // user is verified, so logged in, which will be detected by the AuthStateListener
                            finish();
                        }
                        else
                        {
                            // email is not verified,
                            FirebaseAuth.getInstance().signOut();

                            Toast.makeText(MainActivity.this, "Email not verified", Toast.LENGTH_SHORT).show();

                            //restart this activity
                            Intent intent = new Intent(MainActivity.this, MainActivity.class);
                            startActivity(intent);
                        }
                    }
                }
            });
        }
        //else
            //emailText.setError("Email empty");
           // passwordText.setError("Password empty");
        //    Toast.makeText(MainActivity.this, "Field(s) is/are empty", Toast.LENGTH_LONG).show();
    }

    void goToAddDeviceActivity()
    {
        Intent intent = new Intent(MainActivity.this, AddDeviceActivity.class);
        startActivity(intent);
    }

    void goToMyAccountActivity()
    {
        Intent intent = new Intent(MainActivity.this, MyAccountActivity.class);
        startActivity(intent);
    }

    void goToResetActivity()
    {
        Intent intent = new Intent(MainActivity.this, ResetActivity.class);
        startActivity(intent);
    }
}
