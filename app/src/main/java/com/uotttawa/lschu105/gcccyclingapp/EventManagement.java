package com.uotttawa.lschu105.gcccyclingapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import android.graphics.Color;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

public class EventManagement extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_management);

        final LinearLayout buttonContainer = findViewById(R.id.buttonContainer);

        retrieveEventTypesFromFirebase(new EventTypesCallback() {
            @Override
            public void onEventTypesReceived(List<String> eventTypes) {
                for (int i = 0; i < eventTypes.size(); i++) {
                    final TextView groupNameTextView = new TextView(EventManagement.this);
                    groupNameTextView.setLayoutParams(new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    ));
                    groupNameTextView.setTextColor(Color.parseColor("#DB4D4D"));
                    groupNameTextView.setTextSize(25);
                    groupNameTextView.setGravity(Gravity.CENTER);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        groupNameTextView.setTypeface(getResources().getFont(R.font.sigmarone)); // Apply the custom font
                    }

                    DatabaseReference groupNameReference = FirebaseDatabase.getInstance().getReference().child("EventTypes").child(eventTypes.get(i)).child("name");
                    groupNameReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                String groupName = dataSnapshot.getValue(String.class);
                                groupNameTextView.setText(groupName);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });

                    buttonContainer.addView(groupNameTextView);

                    CardView cardView = new CardView(EventManagement.this);
                    cardView.setLayoutParams(new LinearLayout.LayoutParams(
                            550,
                            115
                    ));
                    cardView.setRadius(80);
                    cardView.setCardBackgroundColor(Color.parseColor("#DB4D4D"));
                    cardView.setClickable(true);

                    final int buttonId = i;

                    cardView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(getApplicationContext(), EventCreation.class);
                            intent.putExtra("eventType", eventTypes.get(buttonId));
                            startActivity(intent);
                        }
                    });

                    TextView selectButton = new TextView(EventManagement.this);
                    selectButton.setText("Select");
                    selectButton.setTextColor(Color.WHITE);
                    selectButton.setGravity(Gravity.CENTER);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        selectButton.setTypeface(getResources().getFont(R.font.sigmarone));
                    }

                    cardView.addView(selectButton);

                    buttonContainer.addView(cardView);

                    LinearLayout separator = new LinearLayout(EventManagement.this);
                    separator.setLayoutParams(new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            50
                    ));
                    buttonContainer.addView(separator);

                }
            }
        });
    }
    private void retrieveEventTypesFromFirebase(final EventTypesCallback callback) {
        final List<String> eventTypes = new ArrayList<>();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("EventTypes");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                eventTypes.clear();
                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                    String eventType = eventSnapshot.getKey();
                    eventTypes.add(eventType);
                }
                callback.onEventTypesReceived(eventTypes);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    interface EventTypesCallback {
        void onEventTypesReceived(List<String> eventTypes);
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, WelcomePage.class);
        startActivity(intent);
    }
}
