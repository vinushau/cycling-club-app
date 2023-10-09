package com.uotttawa.lschu105.gcccyclingapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class AccountSelection extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_type);
    }
    public void onParticipant(View view) {
        //Navigates to login page
        Intent intent = new Intent(getApplicationContext(), ParticipantAccount.class);
        startActivityForResult(intent, 0);
    }
    public void onCyclingClub(View view) {
        //Navigates to login page
        Intent intent = new Intent(getApplicationContext(), CyclingClubAccount.class);
        startActivityForResult(intent, 0);
    }


}