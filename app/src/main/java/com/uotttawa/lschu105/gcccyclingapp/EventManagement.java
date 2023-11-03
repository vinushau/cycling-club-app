package com.uotttawa.lschu105.gcccyclingapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class EventManagement extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_management);

        // Find and set click listeners for "select" buttons
        Button select0Button = findViewById(R.id.select0);
        Button select1Button = findViewById(R.id.select1);
        Button select2Button = findViewById(R.id.select2);
        Button select3Button = findViewById(R.id.select3);


        select0Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), GroupRides.class);
                startActivity(intent);
            }
        });

        select1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), HillClimb.class);
                startActivity(intent);
            }
        });

        select2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), RoadRace.class);
                startActivity(intent);
            }
        });

        select3Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), RoadStageRace.class);
                startActivity(intent);
            }
        });
    }
}
