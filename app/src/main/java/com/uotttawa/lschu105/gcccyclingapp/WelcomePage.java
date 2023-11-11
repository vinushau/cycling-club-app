package com.uotttawa.lschu105.gcccyclingapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class WelcomePage extends AppCompatActivity {

    private FirebaseAuth auth;
    private Button button;
    private TextView textView;
    private Button createEventsButton;
    private Button viewEventsButton;
    private Button editEventTypesButton;
    private Button editUsersButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_page);

        auth = FirebaseAuth.getInstance();
        button = findViewById(R.id.logout);
        textView = findViewById(R.id.user_details);
        createEventsButton = findViewById(R.id.createEventsButton);
        viewEventsButton = findViewById(R.id.viewEventsButton);
        editEventTypesButton = findViewById(R.id.editEventTypesButton);
        editUsersButton = findViewById(R.id.editUsers);

        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        } else {
            String username = user.getDisplayName();
            DatabaseReference database = FirebaseDatabase.getInstance().getReference();

            if (username != null && !username.isEmpty()) {
                DatabaseReference accountTypeReference = database.child("Users").child(username).child("accounttype");

                accountTypeReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String accountTypeValue = dataSnapshot.getValue(String.class);

                        if (accountTypeValue != null) {
                            textView.setText("Welcome " + username + ". You are logged in as a " + accountTypeValue.toLowerCase() + " account.");

                            createEventsButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent = new Intent(getApplicationContext(), EventTypeSelector.class);
                                    startActivity(intent);
                                    finish();
                                }
                            });

                            viewEventsButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    // Start the activity for viewing events (replace with the correct activity)
                                    Intent intent = new Intent(getApplicationContext(), ViewEvents.class);
                                    startActivity(intent);
                                    finish();
                                }
                            });

                            if (accountTypeValue.equalsIgnoreCase("admin")) {
                                createEventsButton.setVisibility(View.VISIBLE);
                                viewEventsButton.setVisibility(View.VISIBLE);
                                editEventTypesButton.setVisibility(View.VISIBLE);
                                editUsersButton.setVisibility(View.VISIBLE);

                                editEventTypesButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        // Start the activity for editing event types (replace with the correct activity)
                                        Intent intent = new Intent(getApplicationContext(), AdminEventTypes.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                });

                                editUsersButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        // Start the activity for editing users (replace with the correct activity)
                                        Intent intent = new Intent(getApplicationContext(), AccountManagement.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                            } else if (accountTypeValue.equalsIgnoreCase("cycling club")) {
                                // For Cycling Club accounts
                                createEventsButton.setVisibility(View.VISIBLE);
                                viewEventsButton.setVisibility(View.VISIBLE);
                                editEventTypesButton.setVisibility(View.GONE); // Hide for non-admin users
                                editUsersButton.setVisibility(View.GONE); // Hide for non-admin users
                            } else {
                                // For other account types
                                createEventsButton.setVisibility(View.GONE);
                                viewEventsButton.setVisibility(View.GONE);
                                editEventTypesButton.setVisibility(View.GONE);
                                editUsersButton.setVisibility(View.GONE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        System.err.println("Error: " + databaseError.getMessage());
                    }
                });
            }
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
