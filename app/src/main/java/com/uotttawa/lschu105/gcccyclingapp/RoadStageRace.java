package com.uotttawa.lschu105.gcccyclingapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class RoadStageRace extends AppCompatActivity {

    // Define the EditText fields
    EditText ageField, paceField, levelField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_road_stage_race);

        // Find the RoadStageRace button by its ID
        Button roadStageRaceButton = findViewById(R.id.RoadStageRace);

        // Find the back button by its ID
        Button backButton = findViewById(R.id.back);

        // Find the EditText fields by their IDs
        ageField = findViewById(R.id.ageField);
        paceField = findViewById(R.id.paceField);
        levelField = findViewById(R.id.levelField);

        // Set an OnClickListener for the back button to return to the previous activity
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
