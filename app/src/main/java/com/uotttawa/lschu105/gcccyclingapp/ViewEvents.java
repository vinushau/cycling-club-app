package com.uotttawa.lschu105.gcccyclingapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.view.LayoutInflater;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class ViewEvents extends AppCompatActivity {

    private DatabaseReference eventsRef;
    private ListView eventsListView;
    private ArrayList<String> eventNames;
    private String userAccountType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_events);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String username = user.getDisplayName();

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(username);
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userAccountType = dataSnapshot.child("accounttype").getValue(String.class);
                if (userAccountType.equals("Admin")) {
                    // If the user is an admin, display all events
                    eventsRef = FirebaseDatabase.getInstance().getReference().child("Events");
                } else {
                    // If the user is a cyclist, display their own events
                    eventsRef = usersRef.child("Events");
                }

                eventsListView = findViewById(R.id.eventsListView);
                eventNames = new ArrayList<>();

                eventsRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        eventNames.clear();
                        for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                            String eventName;
                            if (userAccountType.equals("Admin")) {
                                eventName = (String) eventSnapshot.getKey();
                            } else {
                                eventName = (String) eventSnapshot.getValue();
                            }
                            eventNames.add(eventName);
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(ViewEvents.this, android.R.layout.simple_list_item_1, eventNames);
                        eventsListView.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, WelcomePage.class);
        startActivity(intent);
    }

    public void removeEvent(){
        //Allow the admin to delete events deliverable 3
    }
}
