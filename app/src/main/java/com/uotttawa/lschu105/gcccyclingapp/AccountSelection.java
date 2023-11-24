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
        // Navigates to ParticipantAccount
        Intent intent = new Intent(getApplicationContext(), AccountRegistration.class);
        intent.putExtra("role", "Participant");
        startActivityForResult(intent, 0);
    }

    public void onCyclingClub(View view) {
        // Navigates to CyclingClubAccount
        Intent intent = new Intent(getApplicationContext(), AccountRegistration.class);
        intent.putExtra("role", "CyclingClub");
        startActivityForResult(intent, 0);
    }

    public void onAdmin(View view) {
        // Navigates to AdminAccount
        Intent intent = new Intent(getApplicationContext(), AccountRegistration.class);
        intent.putExtra("role", "Admin");
        startActivityForResult(intent, 0);
    }
}
