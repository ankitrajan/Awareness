package com.example.ankit.awareness;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
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
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;

public class CreateAccountActivity extends AppCompatActivity implements GestureDetector.OnGestureListener
{
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;

    private Button createButton;

    //private EditText firstNameText;
    //private EditText lastNameText;
    //private EditText ageText;
    private EditText emailText;
    private EditText passwordText;
    //private EditText phoneNumberText;

    private GestureDetectorCompat detector;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        createButton = (Button) findViewById(R.id.CreateNewAccount);

        //firstNameText = (EditText) findViewById(R.id.FirstNameField);
        //lastNameText = (EditText) findViewById(R.id.LastNameField);
        //ageText = (EditText) findViewById(R.id.AgeField);
        emailText = (EditText) findViewById(R.id.EmailField);
        passwordText = (EditText) findViewById(R.id.PasswordField);
        //phoneNumberText = (EditText) findViewById(R.id.PhoneNumberField);

        detector = new GestureDetectorCompat(this, this);

        createButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                createAccount();
            }
        });
    }

    private void createAccount()
    {
        //final String firstName = firstNameText.getText().toString();
        //final String lastName = lastNameText.getText().toString();
        //final String age = ageText.getText().toString();
        final String email = emailText.getText().toString();
        final String password = passwordText.getText().toString();
        //final String phoneNumber = phoneNumberText.getText().toString();

        if (!(TextUtils.isEmpty(email) || TextUtils.isEmpty(password)))
        {
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                    {

                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            if(task.isSuccessful())
                            {
                                if (task.isSuccessful())
                                {
                                    final FirebaseUser user = firebaseAuth.getCurrentUser();

                                    user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                Snackbar.make(findViewById(android.R.id.content), "Verification email was sent", Snackbar.LENGTH_LONG).show();

                                                //Toast.makeText(CreateAccountActivity.this, "Verification email was sent", Toast.LENGTH_LONG).show();

                                                String newUser = user.getUid().toString();

                                                SharedPreferences myPref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
                                                SharedPreferences.Editor editor = myPref.edit();
                                                editor.putString("deviceID", "No device");
                                                editor.putString("UserID", newUser);
                                                editor.putBoolean("First Login", true);
                                                editor.apply();

                                                //databaseReference.child("Users").child(newUser).child("First Name").setValue(firstName);

                                                DatabaseReference newUserReference = databaseReference.child("Users").child(newUser).getRef();

                                                //newUserReference.child("Last Name").setValue(lastName);
                                                //newUserReference.child("Age").setValue(age);
                                                newUserReference.child("Email").setValue(email);
                                                newUserReference.child("Password").setValue(password);
                                                //newUserReference.child("Phone Number").setValue(phoneNumber);

                                                firebaseAuth.signOut();

                                                startActivity(new Intent(CreateAccountActivity.this, MainActivity.class));
                                                finish();
                                            }
                                            else
                                            {
                                               // emailText.setError("Email could not be verified");
                                              //  View view = findViewById(android.R.id.content);
                                               // Snackbar.make(view, "Deleted...", Snackbar.LENGTH_LONG).show();
                                                Snackbar.make(findViewById(android.R.id.content), "Email could not be verified", Snackbar.LENGTH_LONG).show();

                                                //Toast.makeText(CreateAccountActivity.this, "Email could not be verified", Toast.LENGTH_LONG).show();
                                                finish();
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    });
        }
        else
        {
            Snackbar.make(findViewById(android.R.id.content), "Field(s) cannot be left empty", Snackbar.LENGTH_LONG).show();

            //Toast.makeText(CreateAccountActivity.this, "Field(s) cannot be left empty", Toast.LENGTH_LONG).show();

        }

           // emailText.setError("Email cannot be empty");
           // passwordText.setError("Password cannot be empty");

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
            Intent intent = new Intent(CreateAccountActivity.this, LogoActivity.class);
            intent.putExtra("STARTINGACTIVITY", "CreateAccountActivity");
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
