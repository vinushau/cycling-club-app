package com.uotttawa.lschu105.gcccyclingapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onLoginButton(View view) {
        /*Test
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("message");
        myRef.setValue("Test");
        */

        //Navigates to login page
        Intent intent = new Intent(getApplicationContext(), login.class);
        startActivityForResult(intent, 0);
    }

    public void onRegisterButton(View view) {
        // Navigates to the register page
        Intent intent = new Intent(getApplicationContext(), register.class);
        startActivityForResult(intent, 0);
    }
}
