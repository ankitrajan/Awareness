package com.example.ankit.awareness;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

public class LogoActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    protected Button mainButton;
    protected Button signUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logo);

        mainButton = (Button) findViewById(R.id.Main);
        signUpButton = (Button) findViewById(R.id.SignUp);

        DatabaseHelper myDatabase = new DatabaseHelper(getApplicationContext());

        myDatabase.deleteAllData();
        myDatabase.deleteAllDevice();

        mainButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                goToMainActivity();
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                goToCreateAccountActivity();
            }
        });
    }

    void goToMainActivity()
    {
        Intent intent = new Intent(LogoActivity.this, MainActivity.class);
        startActivity(intent);
    }

    void goToCreateAccountActivity()
    {
        Intent intent = new Intent(LogoActivity.this, CreateAccountActivity.class);
        startActivity(intent);
    }
}
