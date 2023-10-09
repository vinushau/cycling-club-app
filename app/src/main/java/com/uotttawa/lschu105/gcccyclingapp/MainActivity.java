package com.uotttawa.lschu105.gcccyclingapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

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
        Intent intent = new Intent(getApplicationContext(), Login.class);
        startActivityForResult(intent, 0);
    }

    public void onRegisterButton(View view) {
        // Navigates to the register page
        Intent intent = new Intent(getApplicationContext(), AccountSelection.class);
        startActivityForResult(intent, 0);
    }
}
