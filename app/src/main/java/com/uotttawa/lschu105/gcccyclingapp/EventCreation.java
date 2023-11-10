package com.uotttawa.lschu105.gcccyclingapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class EventCreation extends AppCompatActivity {

    private EditText eventNameField;
    private Spinner levelSpinner;
    private TextView eventTypes, eventDescription;
    private DatabaseReference databaseReference;
    private LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_creation);

        Intent intent = getIntent();
        String buttonName = intent.getStringExtra("eventType");

        eventTypes = findViewById(R.id.EventType);
        eventDescription = findViewById(R.id.EventDescription);

        DatabaseReference eventTypeReference = FirebaseDatabase.getInstance().getReference("EventTypes").child(buttonName);

        eventTypeReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String groupName = dataSnapshot.child("name").getValue(String.class);
                    String description = dataSnapshot.child("description").getValue(String.class);
                    eventDescription.setText(description);
                    eventTypes.setText(groupName);
                    createEditTextFields(dataSnapshot.child("Requirements"));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        eventNameField = findViewById(R.id.eventNameField);
        levelSpinner = findViewById(R.id.levelSpinner);
        linearLayout = findViewById(R.id.linearLayout);

        databaseReference = FirebaseDatabase.getInstance().getReference("Events");
        String[] levelOptions = {"Difficulty Level", "Beginner", "Intermediate", "Advanced"};

        ArrayAdapter<String> adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, levelOptions) {
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                ((TextView) view).setTextColor(getResources().getColor(position == 0 ? android.R.color.darker_gray : android.R.color.black));
                view.setEnabled(position != 0);
                return view;
            }
        };

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        levelSpinner.setAdapter(adapter);
        levelSpinner.setSelection(0);

        Button selectButton = findViewById(R.id.selectGroupRides);
        selectButton.setOnClickListener(v -> createEvent(buttonName));
    }

    private void createEditTextFields(DataSnapshot requirementsSnapshot) {
        int editTextWidthSP = 300;
        int spaceHeightPixels = 25;

        int editTextWidthPixels = (int) (editTextWidthSP * getResources().getDisplayMetrics().scaledDensity);

        Space space = new Space(this);
        space.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                spaceHeightPixels));
        linearLayout.addView(space);

        int index = 5;

        for (DataSnapshot requirement : requirementsSnapshot.getChildren()) {
            String requirementValue = requirement.getValue(String.class);
            EditText editText = new EditText(this);
            editText.setLayoutParams(new LinearLayout.LayoutParams(
                    editTextWidthPixels,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            editText.setHint(requirementValue);
            editText.setTextColor(getResources().getColor(R.color.black));
            editText.setTextSize(20);
            editText.setGravity(Gravity.CENTER);
            linearLayout.addView(editText, index);
            index++;
        }
    }

    private void createEvent(String buttonName) {
        String eventName = eventNameField.getText().toString();
        String difficultyLevel = levelSpinner.getSelectedItem().toString();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String username = user.getDisplayName();

        DatabaseReference userEventsReference = FirebaseDatabase.getInstance().getReference("Users").child(username).child("Events");

        userEventsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long eventCount = dataSnapshot.getChildrenCount();
                String nextIndex = String.valueOf(eventCount);

                DatabaseReference eventReference = databaseReference.child(eventName);
                eventReference.child("EventType").setValue(buttonName);

                for (int i = 0; i < linearLayout.getChildCount(); i++) {
                    if (linearLayout.getChildAt(i) instanceof EditText) {
                        EditText editText = (EditText) linearLayout.getChildAt(i);
                        String requirementKey = editText.getHint().toString().toLowerCase();
                        String requirementValue = editText.getText().toString();
                        eventReference.child(requirementKey).setValue(requirementValue);
                    }
                }

                eventReference.child("Difficulty Level").setValue(difficultyLevel);
                eventReference.child("CreatedBy").setValue(username);

                DatabaseReference userEventsReference = FirebaseDatabase.getInstance().getReference("Users").child(username).child("Events").child(nextIndex);
                userEventsReference.setValue(eventName);

                eventNameField.setText("");
                levelSpinner.setSelection(0);

                Toast.makeText(EventCreation.this, "Event created successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), WelcomePage.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void onBack(View view) {
        Intent intent = new Intent(getApplicationContext(), EventManagement.class);
        startActivity(intent);
        finish();
    }
}
