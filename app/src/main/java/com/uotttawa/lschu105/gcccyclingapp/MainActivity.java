package com.uotttawa.lschu105.gcccyclingapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }
    public void onStart() {
        super.onStart();
        // Check if the user is already logged in using SharedPreferences
        SharedPreferences preferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        String savedEmail = preferences.getString("email", "");

        if (!TextUtils.isEmpty(savedEmail)) {
            Intent intent = new Intent(getApplicationContext(), WelcomePage.class);
            startActivity(intent);
            finish();
        }
    }



    public void onLoginButton(View view) {
        //Navigates to login page
        Intent intent = new Intent(getApplicationContext(), Login.class);
        startActivityForResult(intent, 0);
    }

    public void onRegisterButton(View view) {
        // Navigates to the selection page
        Intent intent = new Intent(getApplicationContext(), AccountSelection.class);
        startActivityForResult(intent, 0);
    }
}
