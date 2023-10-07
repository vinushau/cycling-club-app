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
        Intent intent = new Intent(getApplicationContext(), login.class);
        startActivityForResult(intent, 0);
    }
    public void onRegisterButton(View view) {
        Intent intent = new Intent(getApplicationContext(), register.class);
        startActivityForResult(intent, 0);
    }

}