package com.uotttawa.lschu105.gcccyclingapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EventFeed extends AppCompatActivity {

    private LinearLayout tagContainerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_feed);

        tagContainerLayout = findViewById(R.id.tagContainerLayout);
        DatabaseReference eventTypesRef = FirebaseDatabase.getInstance().getReference("EventTypes");

        eventTypesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                    String tag = eventSnapshot.child("name").getValue(String.class);
                    View tagView = LayoutInflater.from(EventFeed.this).inflate(R.layout.tag_card_view, tagContainerLayout, false);
                    TextView tagTextView = tagView.findViewById(R.id.tagTextView);
                    tagTextView.setText(tag);
                    tagContainerLayout.addView(tagView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
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
