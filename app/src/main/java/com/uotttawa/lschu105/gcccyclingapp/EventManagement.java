package com.uotttawa.lschu105.gcccyclingapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class EventManagement extends AppCompatActivity {
    private List<Event> events;
    private boolean isValidationSuccessful = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_management);

        events = new ArrayList<>();
        loadEventsFromFirebase();
    }

    private void loadEventsFromFirebase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Events");

        SharedPreferences preferences = getSharedPreferences("loginPrefs", Context.MODE_PRIVATE);
        String userRole = preferences.getString("role", "");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                events.clear(); // Clear existing events
                LinearLayout containerLayout = findViewById(R.id.eventContainer);

                // Remove all views from the container layout
                containerLayout.removeAllViews();

                // Get the container layout where you want to add the dynamic views
                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                    Event event = eventSnapshot.getValue(Event.class);

                    // Check if the user is a CyclingClub and the event is created by the same club
                    if (userRole.equals("Admin")){
                        events.add(event);

                        View cardView = LayoutInflater.from(EventManagement.this).inflate(R.layout.item_event_types, null);

                        TextView eventNameTextView = cardView.findViewById(R.id.TitleName);
                        eventNameTextView.setText(event.getEventName());

                        eventNameTextView.setText(event.getEventName());

                        Button roundButton = cardView.findViewById(R.id.roundButton);
                        roundButton.setText("Edit");
                        roundButton.setTextColor(Color.WHITE); // Set text color to white
                        roundButton.setOnClickListener(v -> {
                            EventEditor dialogHelper = new EventEditor();
                            dialogHelper.showDialog(EventManagement.this, EventManagement.this, event);
                        });

                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        int marginInDp = 16;
                        int marginInPixels = (int) (marginInDp * getResources().getDisplayMetrics().density);
                        layoutParams.setMargins(0, 0, 0, marginInPixels);
                        cardView.setLayoutParams(layoutParams);
                        containerLayout.addView(cardView);
                    }
                    if (event != null && userRole.equals("CyclingClub") && event.getCreatedBy().equals(preferences.getString("username", ""))) {
                        events.add(event);

                        View cardView = LayoutInflater.from(EventManagement.this).inflate(R.layout.item_event_types, null);

                        TextView eventNameTextView = cardView.findViewById(R.id.TitleName);
                        eventNameTextView.setText(event.getEventName());

                        eventNameTextView.setText(event.getEventName());

                        Button roundButton = cardView.findViewById(R.id.roundButton);
                        roundButton.setText("Edit");
                        roundButton.setTextColor(Color.WHITE);
                        roundButton.setOnClickListener(v -> {
                            EventEditor dialogHelper = new EventEditor();
                            dialogHelper.showDialog(EventManagement.this, EventManagement.this, event);
                        });

                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        int marginInDp = 16;
                        int marginInPixels = (int) (marginInDp * getResources().getDisplayMetrics().density);
                        layoutParams.setMargins(0, 0, 0, marginInPixels);
                        cardView.setLayoutParams(layoutParams);
                        containerLayout.addView(cardView);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle onCancelled event
            }
        });
    }
    public void onBackPressed() {
        Intent intent = new Intent(this, WelcomePage.class);
        startActivity(intent);
        finish();
    }
}
