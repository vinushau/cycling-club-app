package com.uotttawa.lschu105.gcccyclingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EventCreation extends AppCompatActivity {

    private EditText eventNameField;
    private EditText ageField;
    private EditText paceField;
    private Spinner levelSpinner;
    private TextView eventTypes;
    private TextView eventDescription;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_creation);
        Intent intent = getIntent();
        String buttonName = intent.getStringExtra("eventType");
        eventTypes = findViewById(R.id.EventType);
        eventTypes.setText(buttonName);
        eventDescription = findViewById(R.id.EventDescription);
        DatabaseReference eventTypeReference = FirebaseDatabase.getInstance().getReference().child("EventTypes").child(buttonName);

        eventTypeReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String groupName = dataSnapshot.child("name").getValue(String.class);
                    String description = dataSnapshot.child("description").getValue(String.class);
                    eventDescription.setText(description);
                    eventTypes.setText(groupName);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        eventNameField = findViewById(R.id.eventNameField);
        ageField = findViewById(R.id.ageField);
        paceField = findViewById(R.id.paceField);
        levelSpinner = findViewById(R.id.levelSpinner);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Events");
        String[] levelOptions = {"Difficulty Level", "Beginner", "Intermediate", "Advanced"};

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, levelOptions) {
            @Override
            public View getDropDownView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                if (position == 0) {
                    ((android.widget.TextView) view).setTextColor(getResources().getColor(android.R.color.darker_gray));
                    view.setEnabled(false);
                } else {
                    ((android.widget.TextView) view).setTextColor(getResources().getColor(android.R.color.black));
                    view.setEnabled(true);
                }
                return view;
            }
        };

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        levelSpinner.setAdapter(adapter);
        levelSpinner.setSelection(0);

        Button selectButton = findViewById(R.id.selectGroupRides);
        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createEvent(buttonName);
            }
        });
    }

    private void createEvent(String buttonName) {
        String eventName = eventNameField.getText().toString();
        String age = ageField.getText().toString();
        String pace = paceField.getText().toString();
        String difficultyLevel = levelSpinner.getSelectedItem().toString();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String username = user.getDisplayName();

        DatabaseReference userEventsReference = FirebaseDatabase.getInstance().getReference().child("Users").child(username).child("Events");

        userEventsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long eventCount = dataSnapshot.getChildrenCount();

                String nextIndex = String.valueOf(eventCount);
                DatabaseReference eventReference = databaseReference.child(eventName);
                eventReference.child("EventType").setValue(buttonName);
                eventReference.child("Age").setValue(age);
                eventReference.child("Pace").setValue(pace);
                eventReference.child("Difficulty Level").setValue(difficultyLevel);
                eventReference.child("CreatedBy").setValue(username);

                DatabaseReference userEventsReference = FirebaseDatabase.getInstance().getReference().child("Users").child(username).child("Events").child(nextIndex);
                userEventsReference.setValue(eventName);

                eventNameField.setText("");
                ageField.setText("");
                paceField.setText("");
                levelSpinner.setSelection(0);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Database Error", "Error: " + databaseError.getMessage());
            }
        });
    }

    public void onBack(View view) {
        Intent intent = new Intent(getApplicationContext(), EventManagement.class);
        startActivity(intent);
        finish();
    }
}