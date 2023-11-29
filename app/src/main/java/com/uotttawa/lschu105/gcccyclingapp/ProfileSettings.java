package com.uotttawa.lschu105.gcccyclingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileSettings extends AppCompatActivity {

    private Profile profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_settings);

        Intent intent = getIntent();
        String username = intent.getStringExtra("username");
        TextView settings = findViewById(R.id.settings);
        settings.setOnClickListener(v -> {
            Intent profileViewIntent = new Intent(ProfileSettings.this, ProfileView.class);
            profileViewIntent.putExtra("username", username);
            startActivity(profileViewIntent);
            finish();
        });
        Button addTagsButton = findViewById(R.id.addTags);
        addTagsButton.setOnClickListener(v -> onAddTags());

        DatabaseReference profileDetails = FirebaseDatabase.getInstance().getReference().child("Profile").child(username);

        profileDetails.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Profile profile = dataSnapshot.getValue(Profile.class);
                    System.out.println("Profile " + profile.getSocialMediaLinks());

                    UpdatePhoneNumber(profile.getPhoneNumber() != null ? profile.getPhoneNumber() : ""); // Call with an empty string if null

                    UpdateMainContact(profile.getMainContact() != null ? profile.getMainContact() : "");

                    setDisplayName(profile.getDisplayName() != null ? profile.getDisplayName() : "");

                    createEditTextFields(ProfileSettings.this, profile.getSocialMediaLinks() != null ? profile.getSocialMediaLinks(): null);

                    updateLocation(profile.getLocation() != null ? profile.getLocation() : "");
                    createTagFields(ProfileSettings.this,profile.getTags() != null ? profile.getTags(): null);
                    Button UpdateButton = findViewById(R.id.updateButton);
                    UpdateButton.setOnClickListener(v -> updateProfile(profile, username));
                }

                else{
                    Profile profile = new Profile();
                    System.out.println("Profile " + profile.getSocialMediaLinks());

                    UpdatePhoneNumber(profile.getPhoneNumber() != null ? profile.getPhoneNumber() : ""); // Call with an empty string if null

                    UpdateMainContact(profile.getMainContact() != null ? profile.getMainContact() : "");

                    setDisplayName(profile.getDisplayName() != null ? profile.getDisplayName() : "");

                    createEditTextFields(ProfileSettings.this, profile.getSocialMediaLinks() != null ? profile.getSocialMediaLinks(): null);

                    updateLocation(profile.getLocation() != null ? profile.getLocation() : "");
                    createTagFields(ProfileSettings.this,profile.getTags() != null ? profile.getTags(): null);
                    Button UpdateButton = findViewById(R.id.updateButton);
                    UpdateButton.setOnClickListener(v -> updateProfile(profile, username));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle onCancelled event
            }
        });
    }


    private void createEditTextFields(Context context, Map<String, String> socialMediaMap) {
        int editTextWidthSP = 300;
        int spaceHeightPixels = 25;

        int editTextWidthPixels = (int) (editTextWidthSP * context.getResources().getDisplayMetrics().scaledDensity);

        LinearLayout linearLayout = findViewById(R.id.LinearLayout);

        Space space = new Space(context);
        space.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                spaceHeightPixels));
        linearLayout.addView(space);

        if (socialMediaMap == null){
            onAddLink(null);
        }

        if (socialMediaMap != null) {
            for (Map.Entry<String, String> entry : socialMediaMap.entrySet()) {
                String requirementKey = entry.getKey();
                String requirementValue = entry.getValue();

                // Create a TextView for the requirement key
                TextView keyTextView = new TextView(context);
                keyTextView.setLayoutParams(new LinearLayout.LayoutParams(
                        editTextWidthPixels,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                keyTextView.setText(requirementKey + ":");
                keyTextView.setTextColor(context.getResources().getColor(R.color.black));
                keyTextView.setTextSize(20);
                linearLayout.addView(keyTextView);

                // Create an EditText for the requirement value
                EditText editText = new EditText(context);
                editText.setLayoutParams(new LinearLayout.LayoutParams(
                        editTextWidthPixels,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                editText.setText(requirementValue);
                editText.setHint(requirementKey);
                editText.setTextColor(context.getResources().getColor(R.color.black));
                editText.setTextSize(20);
                linearLayout.addView(editText);
            }
        }
    }

    private void createTagFields(Context context, List<String> tags) {
        int editTextWidthSP = 300;
        int spaceHeightPixels = 25;

        int editTextWidthPixels = (int) (editTextWidthSP * context.getResources().getDisplayMetrics().scaledDensity);

        LinearLayout linearLayout = findViewById(R.id.LinearLayout);

        Space space = new Space(context);
        space.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                spaceHeightPixels));
        linearLayout.addView(space);

        if (tags == null) {
            onAddTags();
        } else {
            for (String tag : tags) {
                // Create an EditText for the tag
                EditText editText = new EditText(context);
                editText.setLayoutParams(new LinearLayout.LayoutParams(
                        editTextWidthPixels,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                editText.setText(tag);
                editText.setHint("Tag (optional)");
                editText.setTextColor(context.getResources().getColor(R.color.black));
                editText.setTextSize(20);
                linearLayout.addView(editText);
            }
        }
    }
    private void onAddTags() {
        Context context = ProfileSettings.this;

        int editTextWidthSP = 300;
        int spaceHeightPixels = 25;

        int editTextWidthPixels = (int) (editTextWidthSP * context.getResources().getDisplayMetrics().scaledDensity);

        LinearLayout linearLayout = findViewById(R.id.LinearLayout);

        Space space = new Space(context);
        space.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                spaceHeightPixels));
        linearLayout.addView(space);

        EditText editText = new EditText(context);
        editText.setLayoutParams(new LinearLayout.LayoutParams(
                editTextWidthPixels,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        editText.setHint("Tag (optional)");
        editText.setTextColor(context.getResources().getColor(R.color.black));
        editText.setTextSize(20);
        linearLayout.addView(editText);
    }

    public void onAddLink(View view) {
        // Call the existing method
        onAddLinkInternal(view);
    }
    private void onAddLinkInternal(View view) {

        Context context = ProfileSettings.this;

        int editTextWidthSP = 300;
        int spaceHeightPixels = 25;

        int editTextWidthPixels = (int) (editTextWidthSP * context.getResources().getDisplayMetrics().scaledDensity);

        LinearLayout linearLayout = findViewById(R.id.LinearLayout);

        Space space = new Space(context);
        space.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                spaceHeightPixels));
        linearLayout.addView(space);

        // Create a TextView for the requirement key
        TextView keyTextView = new TextView(context);
        keyTextView.setLayoutParams(new LinearLayout.LayoutParams(
                editTextWidthPixels,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        keyTextView.setText("New Social Media Link" + ":");
        keyTextView.setTextColor(context.getResources().getColor(R.color.black));
        keyTextView.setTextSize(20);
        linearLayout.addView(keyTextView);

        // Create an EditText for the requirement value
        EditText editText = new EditText(context);
        editText.setLayoutParams(new LinearLayout.LayoutParams(
                editTextWidthPixels,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        editText.setHint("add link");
        editText.setTextColor(context.getResources().getColor(R.color.black));
        editText.setTextSize(20);
        linearLayout.addView(editText);
    }

    private void UpdatePhoneNumber(String phoneNumber){

        Context context = ProfileSettings.this;

        int editTextWidthSP = 300;
        int spaceHeightPixels = 25;

        int editTextWidthPixels = (int) (editTextWidthSP * context.getResources().getDisplayMetrics().scaledDensity);

        LinearLayout linearLayout = findViewById(R.id.LinearLayout);

        TextView keyTextView = new TextView(context);
        keyTextView.setLayoutParams(new LinearLayout.LayoutParams(
                editTextWidthPixels,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        keyTextView.setText("Phone Number" + ":");
        keyTextView.setTextColor(context.getResources().getColor(R.color.black));
        keyTextView.setTextSize(20);
        linearLayout.addView(keyTextView);

        Space space = new Space(context);
        space.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                spaceHeightPixels));
        linearLayout.addView(space);

        // Create an EditText for the requirement value
        EditText editText = new EditText(context);
        editText.setLayoutParams(new LinearLayout.LayoutParams(
                editTextWidthPixels,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        editText.setHint("Phone Number");
        editText.setText(phoneNumber);
        editText.setTextColor(context.getResources().getColor(R.color.black));
        editText.setTextSize(20);
        linearLayout.addView(editText);
    }

    private void UpdateMainContact(String mainContact){

        Context context = ProfileSettings.this;

        int editTextWidthSP = 300;
        int spaceHeightPixels = 25;

        int editTextWidthPixels = (int) (editTextWidthSP * context.getResources().getDisplayMetrics().scaledDensity);

        LinearLayout linearLayout = findViewById(R.id.LinearLayout);

        TextView keyTextView = new TextView(context);
        keyTextView.setLayoutParams(new LinearLayout.LayoutParams(
                editTextWidthPixels,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        keyTextView.setText("Main Contact" + ":");
        keyTextView.setTextColor(context.getResources().getColor(R.color.black));
        keyTextView.setTextSize(20);
        linearLayout.addView(keyTextView);

        Space space = new Space(context);
        space.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                spaceHeightPixels));
        linearLayout.addView(space);

        // Create an EditText for the requirement value
        EditText editText = new EditText(context);
        editText.setLayoutParams(new LinearLayout.LayoutParams(
                editTextWidthPixels,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        editText.setHint("Main Contact(optional)");
        editText.setText(mainContact);
        editText.setTextColor(context.getResources().getColor(R.color.black));
        editText.setTextSize(20);
        linearLayout.addView(editText);
    }

    private void setDisplayName(String displayName){
        Context context = ProfileSettings.this;

        int editTextWidthSP = 300;
        int spaceHeightPixels = 25;

        int editTextWidthPixels = (int) (editTextWidthSP * context.getResources().getDisplayMetrics().scaledDensity);

        LinearLayout linearLayout = findViewById(R.id.Header);

        Space space = new Space(context);
        space.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                spaceHeightPixels));
        linearLayout.addView(space);

        // Create an EditText for the requirement value
        EditText editText = new EditText(context);
        editText.setLayoutParams(new LinearLayout.LayoutParams(
                editTextWidthPixels,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        editText.setHint("Enter Display Name");
        editText.setText(displayName);
        editText.setTextColor(context.getResources().getColor(R.color.black));
        editText.setTextSize(20);
        linearLayout.addView(editText);
    }

    private void updateProfile(Profile profile, String username){
        List<String> tags = new ArrayList<>();
        profile.setTags(tags);

        LinearLayout linearLayout = findViewById(R.id.Header);
        for (int i = 0; i < linearLayout.getChildCount(); i++) {
            View childView = linearLayout.getChildAt(i);

            if (childView instanceof EditText) {
                EditText editText = (EditText) childView;
                if (editText.getHint().toString().toLowerCase().equals("enter display name")) {
                    profile.setDisplayName(editText.getText().toString());
                }
                else if (editText.getHint().toString().toLowerCase().equals("enter location")) {
                    profile.setLocation(editText.getText().toString());
                    if (editText.getText().toString().equals("")){
                        Toast.makeText(ProfileSettings.this, "Enter Location", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
        }

        Map<String, String> socialMediaLinks = new HashMap<>();

        LinearLayout linear = findViewById(R.id.LinearLayout);

        for (int i = 0; i < linear.getChildCount(); i++) {
            View childView = linear.getChildAt(i);

            if (childView instanceof EditText) {
                EditText editText = (EditText) childView;
                if (editText.getHint().toString().toLowerCase().equals("phone number")) {
                    profile.setPhoneNumber(editText.getText().toString());
                    if (editText.getText().toString().equals("")){
                        Toast.makeText(ProfileSettings.this, "Enter Phone Number", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } else if (editText.getHint().toString().toLowerCase().equals("main contact(optional)")) {
                    profile.setMainContact(editText.getText().toString());
                } else if (editText.getHint().toString().toLowerCase().equals("tag (optional)")) {
                    String enteredTag = editText.getText().toString();
                    //only adds tag to list if its filled out
                    if (editText.getText().toString().toLowerCase().equals("")) {

                    } else if (!enteredTag.trim().isEmpty()) {
                        tags.add(enteredTag);
                    }
                }
                else{
                    if (editText.getText().toString().equals("")){
                        Toast.makeText(ProfileSettings.this, "Enter Social Media Link", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        URL url = new URL(editText.getText().toString());
                        String host = url.getHost();
                        String subdomain = extractSubdomain(host);
                        socialMediaLinks.put(subdomain, editText.getText().toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        profile.setSocialMediaLinks(socialMediaLinks);
        updateToFirebase(profile, username);
    }

    private static String extractSubdomain(String host) {
        // Split the host into parts using dot as a separator
        String[] parts = host.split("\\.");

        // The domain is the last part (index length-1)
        return parts.length > 1 ? parts[parts.length - 2] : "";
    }

    private void updateToFirebase(Profile profile, String username){
        LinearLayout linearLayout = findViewById(R.id.Header);
        DatabaseReference eventsReference = FirebaseDatabase.getInstance().getReference("Profile");

        DatabaseReference profileref = eventsReference.child(username);

        try {
            profileref.setValue(profile).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Profile updated successfully
                    Toast.makeText(ProfileSettings.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                    // You might want to update the UI here
                } else {
                    // Handle the error
                    Toast.makeText(ProfileSettings.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateLocation(String location){
        Context context = ProfileSettings.this;

        int editTextWidthSP = 300;
        int spaceHeightPixels = 25;

        int editTextWidthPixels = (int) (editTextWidthSP * context.getResources().getDisplayMetrics().scaledDensity);

        LinearLayout linearLayout = findViewById(R.id.Header);

        Space space = new Space(context);
        space.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                spaceHeightPixels));
        linearLayout.addView(space);

        // Create an EditText for the requirement value
        EditText editText = new EditText(context);
        editText.setLayoutParams(new LinearLayout.LayoutParams(
                editTextWidthPixels,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        editText.setHint("Enter Location");
        editText.setText(location);
        editText.setTextColor(context.getResources().getColor(R.color.black));
        editText.setTextSize(20);
        linearLayout.addView(editText);
    }
}