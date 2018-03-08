package com.example.ankit.awareness;

import android.content.Intent;
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
import com.google.firebase.auth.FirebaseAuth;

public class ResetActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    private EditText resetEmailText;

    protected Button resetButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset);

        firebaseAuth = FirebaseAuth.getInstance();

        resetButton = (Button) findViewById(R.id.ResetPassword);

        resetEmailText = (EditText) findViewById(R.id.ResetPasswordField);

        resetButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                resetPassword();
            }
        });
    }

    void resetPassword()
    {
        String email = resetEmailText.getText().toString();
        if (!(TextUtils.isEmpty(email)))
        {
            firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task)
                {
                    if (!(task.isSuccessful()))
                        Toast.makeText(ResetActivity.this, "Email not linked to any account", Toast.LENGTH_LONG).show();
                    else
                    {
                        Toast.makeText(ResetActivity.this, "Email sent", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ResetActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                }
            });
        } else
            Toast.makeText(this, "Email field(s) is empty", Toast.LENGTH_SHORT).show();
    }
}
