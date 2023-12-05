package com.uotttawa.lschu105.gcccyclingapp;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.shapes.Shape;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.uotttawa.lschu105.gcccyclingapp.Event;
import com.uotttawa.lschu105.gcccyclingapp.WelcomePage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ClubFeedback extends AppCompatActivity {
    private ArrayList<Profile> profiles;
    private LinearLayout containerLayout;
    private ShapeableImageView imageView;
    private ArrayList<CardView> selectedDialogEventTypes;
    private LinearLayout tagContainerLayout;
    private ArrayList<String> selectedTags;
    private String eventSearch;
    private String cyclingclubSearch;
    private int ratings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_club_feedback);
        EditText searchField = findViewById(R.id.searchField);

        tagContainerLayout = findViewById(R.id.tagContainerLayout);
        ImageView filterIcon = findViewById(R.id.filterIcon);
        DatabaseReference eventTypesRef = FirebaseDatabase.getInstance().getReference("EventTypes");
        profiles = new ArrayList<>();
        selectedTags = new ArrayList<>();
        loadEventsFromFirebase();

        eventTypesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                    String tag = eventSnapshot.child("name").getValue(String.class);
                    View tagView = LayoutInflater.from(ClubFeedback.this).inflate(R.layout.tag_card_view, tagContainerLayout, false);
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
        searchField.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchField.getWindowToken(), 0);

                String searchText = searchField.getText().toString();
                searchBar(searchText);
                return true;
            }
            return false;
        });
    }

    private void createEventCard(Profile profile) {
        View cardView = LayoutInflater.from(ClubFeedback.this).inflate(R.layout.club_card, null);

        TextView eventNameTextView = cardView.findViewById(R.id.TitleName);
        eventNameTextView.setText("@" + profile.getUsername());

        ShapeableImageView profilePicture = cardView.findViewById(R.id.ProfilePicture);
        profilePicture.setOnClickListener(v -> {
            Intent newIntent = new Intent(getApplicationContext(), ProfileView.class);
            newIntent.putExtra("username", profile.getUsername());
            startActivityForResult(newIntent, 0);
        });

        Button roundButton = cardView.findViewById(R.id.roundButton);
        roundButton.setText("Rate");
        roundButton.setTextColor(Color.WHITE);
        roundButton.setOnClickListener(v -> {
            RateClub(profile);
        });

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        int marginInDp = 20;
        int marginInPixels = (int) (marginInDp * getResources().getDisplayMetrics().density);
        int sidemarginInDp = 18;
        int sidemargininPixels = (int) (sidemarginInDp * getResources().getDisplayMetrics().density);

        layoutParams.setMargins(sidemargininPixels, 5, sidemargininPixels, marginInPixels);
        cardView.setLayoutParams(layoutParams);
        ShapeableImageView image = cardView.findViewById(R.id.ProfilePicture);
        loadProfilePicture(profile.getUsername(), image);
        containerLayout.addView(cardView);
    }

    private void RateClub(Profile profile) {

        // Create a dialog to display event type details
        Dialog dialog = new Dialog(ClubFeedback.this);
        dialog.setContentView(R.layout.rate);

        // Set dialog title
        TextView dialogTitle = dialog.findViewById(R.id.dialogTitle2);

        // Set up dialog button
        Button dialogButton = dialog.findViewById(R.id.dialogButton);
        dialogButton.setText("Create");
        dialogButton.setBackgroundColor(Color.parseColor("#7169E4"));
        dialogButton.setTextColor(Color.WHITE);

        // Set dialog dimensions
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(dialog.getWindow().getAttributes());

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int dialogWidth = (int) (displayMetrics.widthPixels * 0.8f);
        int dialogHeight = WindowManager.LayoutParams.WRAP_CONTENT;

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        layoutParams.width = dialogWidth;
        layoutParams.height = dialogHeight;

        // Add dim overlay to the window
        WindowManager.LayoutParams dimLayoutParams = new WindowManager.LayoutParams();
        dimLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        dimLayoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        dimLayoutParams.format = PixelFormat.TRANSLUCENT;

        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        View dimOverlay = new View(ClubFeedback.this);
        dimOverlay.setBackgroundColor(Color.argb(128, 0, 0, 0));
        windowManager.addView(dimOverlay, dimLayoutParams);

        dialog.getWindow().setAttributes(layoutParams);

        // Set dismiss listeners and touch behavior
        dialogButton.setOnClickListener(v -> {
            dialog.dismiss();
            windowManager.removeView(dimOverlay);
        });

        dimOverlay.setOnClickListener(v -> {
            dialog.dismiss();
            windowManager.removeView(dimOverlay);
        });

        dialog.setCanceledOnTouchOutside(true);
        dialog.setOnDismissListener(dialogInterface -> windowManager.removeView(dimOverlay));

        dialog.show();

        dialogButton.setOnClickListener(v -> {
            SharedPreferences preferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
            String username = preferences.getString("username", "");
            EditText description = dialog.findViewById(R.id.clubFeedbackField);

            if (ratings == 0){
                Toast.makeText(this,"Invalid Rating", Toast.LENGTH_SHORT).show();
                return;
            }
            profile.addRatings(username, ratings);
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Profile").child(profile.getUsername());
            databaseReference.setValue(profile);
            addReviewToFirebase(profile.getUsername(), username, ratings, description.getText().toString());
            dialog.dismiss();
            windowManager.removeView(dimOverlay);
        });


        ImageButton[] stars = new ImageButton[]{
                dialog.findViewById(R.id.star1),
                dialog.findViewById(R.id.star2),
                dialog.findViewById(R.id.star3),
                dialog.findViewById(R.id.star4),
                dialog.findViewById(R.id.star5)
        };

        for (int i = 0; i < stars.length; i++) {
            int finalI = i + 1; // Ratings start from 1
            stars[i].setOnClickListener(v -> {
                setStarColor(finalI, stars);
                // You can save the selected rating (finalI) for further use
            });
        }
    }

    private void setStarColor(int rating, ImageButton[] stars) {
        for (int i = 0; i < stars.length; i++) {
            if (i < rating) {
                stars[i].setColorFilter(Color.parseColor("#FEBF01"));
                ratings = i + 1;
            } else {
                stars[i].setColorFilter(Color.GRAY);
            }
        }
    }

    private void loadProfilePicture(String username, ShapeableImageView pfp) {
        if (username == null) {
            // Handle the case where username is null
            return;
        }

        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference().child("Profile").child(username);

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Retrieve the image URL from the database
                    String imageUrl = dataSnapshot.child("picture").getValue(String.class);
                    if (isDestroyed()) {
                        // The activity is destroyed, don't proceed
                        return;
                    }

                    if (imageUrl != null) {
                        Glide.with(ClubFeedback.this)
                                .load(imageUrl)
                                .into(pfp);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle onCancelled event
            }
        });
    }

    private void loadEventsFromFirebase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Profile");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                containerLayout = findViewById(R.id.eventContainer);

                // Remove all views from the container layout
                containerLayout.removeAllViews();

                for (DataSnapshot profilesnapshot : dataSnapshot.getChildren()) {
                    Profile profile = profilesnapshot.getValue(Profile.class);
                    profile.setUsername(profilesnapshot.getKey());
                    profiles.add(profile);
                    createEventCard(profile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle onCancelled event
            }
        });
    }

    private ArrayList<String> getSelectedTags() {
        return selectedTags;
    }



    //Filters out events that do not contain whats in the search field
    private void searchBar(String searchField){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Profile");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                containerLayout = findViewById(R.id.eventContainer);
                containerLayout.removeAllViews();

                for (DataSnapshot profileSnapshot : dataSnapshot.getChildren()) {
                    Profile profile = profileSnapshot.getValue(Profile.class);
                    profile.setUsername(profileSnapshot.getKey());
                    System.out.println("Profile" + profileSnapshot.getKey());
                    if (profileSnapshot.getKey().toLowerCase().contains(searchField.toLowerCase())) {
                        profiles.add(profile);
                        createEventCard(profile);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle onCancelled event
            }
        });

    }
    private void addReviewToFirebase(String cyclingClubName, String username, int rating, String feedback) {
        DatabaseReference reviewsRef = FirebaseDatabase.getInstance().getReference().child("Reviews")
                .child(cyclingClubName).child(username);

        Map<String, Object> reviewData = new HashMap<>();
        reviewData.put("rating", rating);
        reviewData.put("feedback", feedback);

        reviewsRef.setValue(reviewData);
    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, WelcomePage.class);
        startActivity(intent);
        finish();
    }
}
