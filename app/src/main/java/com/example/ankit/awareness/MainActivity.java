package com.example.ankit.awareness;

import android.content.Intent;
import android.content.pm.ActivityInfo;
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
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity
{
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authListener;

    private EditText emailText;
    private EditText passwordText;

    protected Button loginButton;
    //protected Button createAccountButton;
    protected Button resetPasswordButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        firebaseAuth = FirebaseAuth.getInstance();

        emailText = (EditText) findViewById(R.id.EmailField);
        passwordText = (EditText) findViewById(R.id.PasswordField);

        loginButton = (Button) findViewById(R.id.Login);
        //createAccountButton = (Button) findViewById(R.id.CreateAccount);
        resetPasswordButton = (Button) findViewById(R.id.ResetPassword);

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
                        Toast.makeText(MainActivity.this, "attempting to log in", Toast.LENGTH_LONG).show();
                        goToMyAccountActivity();
                    }
                }
            }
        };

        /*
        createAccountButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                goToCreateAccountActivity();
            }
        });
        */

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
                //resetPassword();
            }
        });
    }

    protected void onStart()
    {
        super.onStart();

        firebaseAuth.addAuthStateListener(authListener);
    }

    /*
    void goToCreateAccountActivity()
    {
        Intent intent = new Intent(MainActivity.this, CreateAccountActivity.class);
        startActivity(intent);
    }
    */

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
                        Toast.makeText(MainActivity.this, "Email/Password incorrect", Toast.LENGTH_LONG).show();
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
        else
            Toast.makeText(MainActivity.this, "Field(s) is/are empty", Toast.LENGTH_LONG).show();
    }

    void goToMyAccountActivity()
    {
        Intent intent = new Intent(MainActivity.this, MyAccountActivity.class);
        startActivity(intent);
    }
}
