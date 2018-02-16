package com.example.ankit.awareness;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class LogoActivity extends AppCompatActivity {

    protected Button mainButton;
    protected Button signUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logo);

        mainButton = (Button) findViewById(R.id.Main);
        signUpButton = (Button) findViewById(R.id.SignUp);

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