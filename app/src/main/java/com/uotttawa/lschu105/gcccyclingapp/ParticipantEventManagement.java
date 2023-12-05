package com.uotttawa.lschu105.gcccyclingapp;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import android.content.SharedPreferences;
import android.widget.EditText;
import android.widget.Space;
import android.widget.Spinner;
import android.widget.Toast;
import java.util.HashMap;
import java.util.Map;

public class ParticipantEventManagement extends AppCompatActivity {
    private TextView selectedNumberDisplay;
    private TextView selectedNumberDisplayMonth;
    private TextView selectedNumberDisplayYear;

    private RelativeLayout numberPickerContainer;
    private RelativeLayout numberPickerContainerMonth;
    private RelativeLayout numberPickerContainerYear;

    private NumberPicker numberPicker;
    private NumberPicker numberPickerMonth;
    private NumberPicker numberPickerYear;
    private RelativeLayout container;

    private List<Event> events;
    private boolean isValidationSuccessful = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_participant_event_management);
        events = new ArrayList<>();

        loadEventTypesFromFirebase();
    }

    private void loadEventTypesFromFirebase() {
        SharedPreferences preferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        String username = preferences.getString("username", "");
        System.out.println("here");

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Accounts").child(username).child("Events");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                LinearLayout containerLayout = findViewById(R.id.eventContainer);
                for (DataSnapshot eventTypeSnapshot : dataSnapshot.getChildren()) {
                    View cardView = LayoutInflater.from(ParticipantEventManagement.this).inflate(R.layout.item_event_types, null);

                    TextView eventName = cardView.findViewById(R.id.TitleName);
                    eventName.setText(eventTypeSnapshot.getKey());
                    Button roundButton = cardView.findViewById(R.id.roundButton);
                    roundButton.setBackgroundColor(Color.parseColor("#7169E4"));
                    roundButton.setText("Leave");
                    roundButton.setOnClickListener(v -> {
                        leaveEvents(eventTypeSnapshot.getKey());
                    });
                    roundButton.setTextColor(Color.WHITE);
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

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle onCancelled event
            }
        });
    }

    private void leaveEvents(String event) {
        DatabaseReference participantsRef = FirebaseDatabase.getInstance().getReference().child("Events").child(event).child("Participants");
        SharedPreferences preferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        String username = preferences.getString("username", "");
        DatabaseReference userEventsRef = FirebaseDatabase.getInstance().getReference().child("Accounts").child(username).child("Events");
        userEventsRef.child(event).removeValue();

        participantsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                LinearLayout containerLayout = findViewById(R.id.eventContainer);

                for (DataSnapshot participantSnapshot : dataSnapshot.getChildren()) {
                    String participantId = participantSnapshot.getKey();

                    if (participantId != null && participantId.equals(username)) {
                        participantsRef.child(participantId).removeValue();
                        DatabaseReference participantsRef = FirebaseDatabase.getInstance().getReference()
                                .child("Events")
                                .child(event)
                                .child("Participants");
                        SharedPreferences preferences = getSharedPreferences("loginPrefs", Context.MODE_PRIVATE);
                        String userId = preferences.getString("username", ""); // Replace with the actual user ID
                        participantsRef.child(userId).setValue(true);
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
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), WelcomePage.class);
        startActivity(intent);
        finish();
    }

}
