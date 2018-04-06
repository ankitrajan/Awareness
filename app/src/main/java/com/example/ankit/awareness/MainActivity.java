package com.example.ankit.awareness;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements GestureDetector.OnGestureListener
{

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authListener;
    private DatabaseReference databaseReference;

    private EditText emailText;
    private EditText passwordText;

    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String SPF_NAME = "vidslogin";
    Switch checkRememberMe;
    EditText etUserName, etPassword;

    protected Button loginButton;
    protected Button resetPasswordButton;

    private static final String TAG = "MainActivity";

    private GestureDetectorCompat detector;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setContentView(R.layout.activity_main);


        if(getIntent().getExtras() != null)
        {
            if(getIntent().getExtras().getString("STARTINGACTIVITY").equals("ResetActivity"))
                overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_bottom);
            else
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
        else
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        checkRememberMe = (Switch) findViewById(R.id.checkbox);
        etUserName = (EditText) findViewById(R.id.EmailField);
        etPassword = (EditText) findViewById(R.id.PasswordField);

        detector = new GestureDetectorCompat(this, this);

        final SharedPreferences loginPreferences = getSharedPreferences(SPF_NAME, Context.MODE_PRIVATE);


        if(loginPreferences.getString("SWITCHSTATUS","").equals("on"))
            checkRememberMe.setChecked(true);
        else if(loginPreferences.getString("SWITCHSTATUS","").equals("off"))
            checkRememberMe.setChecked(false);
        /*
        else
        {
            SharedPreferences loginPreferences = getSharedPreferences(SPF_NAME, Context.MODE_PRIVATE);
            loginPreferences.edit().clear().commit();
            loginPreferences.edit().putString("SWITCHSTATUS", "off").commit();
        }
        */

        checkRememberMe.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked)
                {
                    loginPreferences.edit().clear().commit();
                    loginPreferences.edit().putString("SWITCHSTATUS", "off").commit();
                }
                else if (isChecked)
                {
                    loginPreferences.edit().putString("SWITCHSTATUS", "on").commit();
                }
            }
        });

        etUserName.setText(loginPreferences.getString(USERNAME, ""));
        etPassword.setText(loginPreferences.getString(PASSWORD, ""));

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
                        if(checkRememberMe.isChecked())
                        {
                            loginPreferences.edit().putString(USERNAME, emailText.getText().toString()).putString(PASSWORD, passwordText.getText().toString()).commit();
                            loginPreferences.edit().putString("SWITCHSTATUS", "on").commit();
                        }
                        else
                        {
                            loginPreferences.edit().clear().commit();
                            loginPreferences.edit().putString("SWITCHSTATUS", "off").commit();
                        }

                        databaseReference = FirebaseDatabase.getInstance().getReference();

                        final String currentUser = firebaseAuth.getCurrentUser().getUid().toString();

                        final DatabaseReference userHasDeviceRef = databaseReference.child("Users").child(currentUser).getRef();

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

                                    userHasDeviceRef.child("First Login").setValue(false);

                                    goToConnectedDeviceActivity();
                                    //goToMyAccountActivity();
                                }
                                else
                                {
                                    SharedPreferences myPref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
                                    SharedPreferences.Editor editor = myPref.edit();
                                    editor.putString("deviceID", "No device");
                                    editor.putString("UserID", currentUser);
                                    editor.putBoolean("First Login", true);
                                    editor.apply();

                                    userHasDeviceRef.child("First Login").setValue(true);

                                    goToConnectedDeviceActivity();
                                    //goToMyAccountActivity();
                                    //goToAddDeviceActivity();
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

        final String email = emailText.getText().toString();
        final String password = passwordText.getText().toString();

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
                            String strUserName = etUserName.getText().toString().trim();
                            String strPassword = etPassword.getText().toString().trim();
                            if (null == strUserName || strUserName.length() == 0)
                            {
                                etUserName.requestFocus();
                            } else if (null == strPassword || strPassword.length() == 0)
                            {
                                etPassword.requestFocus();
                            }

                            // user is verified, so logged in, which will be detected by the AuthStateListener
                            finish();
                        }
                        else
                        {
                            // email is not verified,
                            FirebaseAuth.getInstance().signOut();

                            Snackbar.make(findViewById(android.R.id.content), "Email not verified", Snackbar.LENGTH_LONG).show();


                            //Toast.makeText(MainActivity.this, "Email not verified", Toast.LENGTH_SHORT).show();

                            //restart this activity
                            Intent intent = new Intent(MainActivity.this, MainActivity.class);
                            startActivity(intent);
                        }
                    }
                }
            });
        }
      //  else

           // Snackbar snackbar = Snackbar.make(coordinatorLayout, "www.journaldev.com", Snackbar.LENGTH_LONG);
        // snackbar.show();
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

    void goToConnectedDeviceActivity()
    {
        Intent intent = new Intent(MainActivity.this, ConnectedDeviceActivity.class);
        intent.putExtra("STARTINGACTIVITY", "MainActivity");
        startActivity(intent);
    }

    void goToResetActivity()
    {
        Intent intent = new Intent(MainActivity.this, ResetActivity.class);
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
            Intent intent = new Intent(MainActivity.this, LogoActivity.class);
            intent.putExtra("STARTINGACTIVITY", "MainActivity");
            startActivity(intent);
            return true;
        }

        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        detector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }
}
