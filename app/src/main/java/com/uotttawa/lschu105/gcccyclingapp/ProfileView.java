package com.uotttawa.lschu105.gcccyclingapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileView extends AppCompatActivity {

    private ImageButton btnMoreSocialMedia;
    private LinearLayout layoutAdditionalSocialMedia;
    private List<Event> events;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view);
        events = new ArrayList<>();
        TextView ProfileName = findViewById(R.id.ProfileName);
        TextView ProfileUsername = findViewById(R.id.ProfileUsername);
        TextView profile = findViewById(R.id.profile);
        Intent intent = getIntent();
        String username = intent.getStringExtra("username");
        ProfileName.setText(username);
        ProfileUsername.setText("@" + username);
        profile.setOnClickListener(v -> {
            Intent Intent = new Intent(getApplicationContext(), WelcomePage.class);
            startActivity(Intent);
            finish();
        });
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

                    Intent intent = getIntent();
                    String username = intent.getStringExtra("username");

                    if (event != null && userRole.equals("CyclingClub") && event.getCreatedBy().equals(username) || userRole.equals("Admin") && event.getCreatedBy().equals(username)) {
                        events.add(event);

                        View cardView = LayoutInflater.from(ProfileView.this).inflate(R.layout.event_card, null);

                        TextView eventNameTextView = cardView.findViewById(R.id.TitleName);
                        TextView eventCreator = cardView.findViewById(R.id.EventCreator);
                        eventNameTextView.setText(event.getEventName());
                        eventCreator.setText("Organised by: " + event.getCreatedBy());

                        int day = event.getDay();
                        int month = event.getMonth();
                        int year = event.getYear();

                        String strDay = day > 10 ? Integer.toString(day) : "0" + Integer.toString(day);
                        String strMonth = month > 10 ? Integer.toString(month) : "0" + Integer.toString(month);

                        String dateFormatted = String.format("%s/%s/%d", strDay, strMonth, year);

                        TextView eventDateTextView = cardView.findViewById(R.id.EventDate);
                        eventDateTextView.setText(dateFormatted);

                        ShapeableImageView profilePicture = cardView.findViewById(R.id.ProfilePicture);
                        profilePicture.setOnClickListener(v -> {
                            Intent newIntent = new Intent(getApplicationContext(), ProfileView.class);
                            newIntent.putExtra("username", event.getCreatedBy());
                            startActivityForResult(newIntent, 0);
                        });

                        Button roundButton = cardView.findViewById(R.id.roundButton);
                        roundButton.setText("Edit");
                        roundButton.setTextColor(Color.WHITE);
                        roundButton.setOnClickListener(v -> {
                            EventEditor dialogHelper = new EventEditor();
                            dialogHelper.showDialog(ProfileView.this, ProfileView.this, event);
                        });

                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        int marginInDp = 20;
                        int marginInPixels = (int) (marginInDp * getResources().getDisplayMetrics().density);
                        int sidemarginInDp = 18;
                        int sidemargininPixels = (int) (sidemarginInDp * getResources().getDisplayMetrics().density);

                        layoutParams.setMargins(sidemargininPixels, 0, sidemargininPixels, marginInPixels);
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

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, WelcomePage.class);
        startActivity(intent);
        finish();
    }
}
