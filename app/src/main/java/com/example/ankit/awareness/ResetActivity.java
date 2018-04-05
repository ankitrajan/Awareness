package com.example.ankit.awareness;

import android.content.Intent;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetActivity extends AppCompatActivity implements GestureDetector.OnGestureListener{

    private FirebaseAuth firebaseAuth;

    private EditText resetEmailText;

    protected Button resetButton;

    private GestureDetectorCompat detector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset);

        firebaseAuth = FirebaseAuth.getInstance();

        resetButton = (Button) findViewById(R.id.ResetPassword);

        resetEmailText = (EditText) findViewById(R.id.ResetPasswordField);

        detector = new GestureDetectorCompat(this, this);

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
                        //Toast.makeText(ResetActivity.this, "Email not linked to any account", Toast.LENGTH_LONG).show();
                        resetEmailText.setError("Email not linked to an account");
                    else
                    {
                        Snackbar.make(findViewById(android.R.id.content), "Email sent", Snackbar.LENGTH_LONG).show();

                        //Toast.makeText(ResetActivity.this, "Email sent", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ResetActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                }
            });
        } else
            resetEmailText.setError("Email cannot be empty");
           // Toast.makeText(this, "Email field(s) is empty", Toast.LENGTH_SHORT).show();
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
            Intent intent = new Intent(ResetActivity.this, MainActivity.class);
            intent.putExtra("STARTINGACTIVITY", "ResetActivity");
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
