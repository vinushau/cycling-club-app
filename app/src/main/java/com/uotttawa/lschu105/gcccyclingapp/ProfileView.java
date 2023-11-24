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
import android.widget.ImageView;
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
        //loadEventsFromFirebase(); implement this
    }

    //use this to call the event editor on each card
    private void showEventDialog(Event event) {
        EventEditor dialogHelper = new EventEditor();
        dialogHelper.showDialog(ProfileView.this, this, event);
    }
    private void loadEventsFromFirebase() {
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, WelcomePage.class);
        startActivity(intent);
        finish();
    }
}
