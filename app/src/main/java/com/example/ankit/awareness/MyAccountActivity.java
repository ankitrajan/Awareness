package com.example.ankit.awareness;

import android.content.Intent;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MyAccountActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;

    private TextView firstDevice;
    private TextView secondDevice;

    private BottomNavigationView bottomNavigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setContentView(R.layout.activity_my_account);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        String userID = firebaseAuth.getCurrentUser().getUid();

        DatabaseReference newRef = databaseReference.child("Users").child(userID).child("First Name").getRef();

        String name;

        newRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                getSupportActionBar().setTitle("Welcome, " + dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        //bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavigationView);
        //Menu menu = bottomNavigationView.getMenu();

        firstDevice = (TextView) findViewById(R.id.device1);
        secondDevice = (TextView) findViewById(R.id.device2);

        firstDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MyAccountActivity.this, AnalysisActivity.class));
            }
        });

        secondDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MyAccountActivity.this, AnalysisActivity.class));
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_my_account, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.sign_out:
                userSignOut();
                startActivity(new Intent(MyAccountActivity.this, MainActivity.class));
                Toast.makeText(getApplicationContext(), "Signed out", Toast.LENGTH_LONG).show();
                return true;

            case R.id.settings:
                startActivity(new Intent(MyAccountActivity.this, SettingsActivity.class));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void userSignOut()
    {
        firebaseAuth.signOut();
    }
}
