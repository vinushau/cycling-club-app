package com.uotttawa.lschu105.gcccyclingapp;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.uotttawa.lschu105.gcccyclingapp.Utils.QuickSort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.firebase.storage.FirebaseStorage;

import org.w3c.dom.Text;

public class ProfileView extends AppCompatActivity {
    private TextView sortButton;
    private ArrayList<Event> events;
    private int ascending;
    private LinearLayout containerLayout;
    private static final int PICK_IMAGE = 100;
    private ShapeableImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view);

        events = new ArrayList<>();

        ImageButton();

        TextView ProfileName = findViewById(R.id.ProfileName);
        TextView ProfileUsername = findViewById(R.id.ProfileUsername);
        TextView profile = findViewById(R.id.profile);

        Intent intent = getIntent();
        String username = intent.getStringExtra("username");
        ProfileUsername.setText("@" + username);
        profile.setOnClickListener(v -> {
            Intent Intent = new Intent(getApplicationContext(), WelcomePage.class);
            startActivity(Intent);
            finish();
        });
        TextView rating = findViewById(R.id.rating);
        DatabaseReference ratings = FirebaseDatabase.getInstance().getReference().child("Profile").child(username).child("ratings");
        loadEventsFromFirebase();
        loadProfilePicture(username);
        loadProfile(username);
        loadAndDisplayRatings(username);
        ascending = -1;

        imageView = findViewById(R.id.profilepicture);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });
        LinearLayout tagContainerLayout = findViewById(R.id.TagsContainer);
        CardView moreTagsButton = findViewById(R.id.moreTags); // Assuming you have a button for more tags
        StringBuilder moreTagsStringBuilder = new StringBuilder();

        DatabaseReference eventTypesRef = FirebaseDatabase.getInstance().getReference().child("Profile").child(username).child("tags");
        eventTypesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean firstTag = true;
                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                    String tag = eventSnapshot.getValue(String.class);

                    if (firstTag) {
                        LinearLayout Tags = findViewById(R.id.tags);
                        Tags.setVisibility(View.VISIBLE);
                        View tagView = LayoutInflater.from(ProfileView.this).inflate(R.layout.tag_card_view, tagContainerLayout, false);
                        TextView tagTextView = tagView.findViewById(R.id.tagTextView);
                        tagTextView.setText(tag);
                        tagContainerLayout.addView(tagView, 0);
                        firstTag = false;
                    } else {
                        moreTagsButton.setVisibility(View.VISIBLE);
                        moreTagsStringBuilder.append(tag).append("\n");
                    }
                }

                moreTagsButton.setOnClickListener(v -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ProfileView.this);
                    builder.setTitle("More Tags")
                            .setMessage(moreTagsStringBuilder.toString())
                            .setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
                    AlertDialog dialog = builder.create();
                    dialog.show();
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }
        });
    }


    private void loadAndDisplayRatings(String username) {
        DatabaseReference profileRef = FirebaseDatabase.getInstance().getReference().child("Profile").child(username);

        // Retrieve ratings from Firebase
        profileRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Map<String, Long> ratingsMap = new HashMap<>();

                    // Check if ratings node exists in the profile
                    if (dataSnapshot.hasChild("ratings")) {
                        DataSnapshot ratingsSnapshot = dataSnapshot.child("ratings");

                        for (DataSnapshot ratingSnapshot : ratingsSnapshot.getChildren()) {
                            String eventId = ratingSnapshot.getKey();
                            long ratingValue = (long) ratingSnapshot.getValue();
                            ratingsMap.put(eventId, ratingValue);
                        }
                    }

                    // Calculate average rating
                    double averageRating = calculateAverageRating(ratingsMap);

                    // Display the average rating in your TextView
                    TextView ratingTextView = findViewById(R.id.rating); // Replace with your actual TextView ID
                    String ratingText = String.format("%.1f", averageRating);
                    ratingTextView.setText(ratingText);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }
        });
    }
    private double calculateAverageRating(Map<String, Long> ratingsMap) {
        // Calculate the average rating
        if (ratingsMap.isEmpty()) {
            return 0.0;
        }

        long totalRating = 0;
        for (long ratingValue : ratingsMap.values()) {
            totalRating += ratingValue;
        }

        return (double) totalRating / ratingsMap.size();
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
            if (data != null) {
                Uri imageUri = data.getData();
                if (imageUri != null) {
                    uploadImageToFirebaseStorage(imageUri);
                }
            }
        }
    }

    private void uploadImageToFirebaseStorage(Uri imageUri) {
        String username = getIntent().getStringExtra("username");
        String imagePath = "profilePictures/" + username + ".jpg"; // Set your desired image path
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(imagePath);

        storageReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        saveImageUrlInDatabase(username, uri.toString());
                    });
                })
                .addOnFailureListener(exception -> {
                    Toast.makeText(ProfileView.this, "Image upload failed", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveImageUrlInDatabase(String username, String imageUrl) {
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference().child("Profile").child(username);

        Map<String, Object> profileUpdates = new HashMap<>();
        profileUpdates.put("picture", imageUrl);

        userReference.updateChildren(profileUpdates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ProfileView.this, "Image saved successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ProfileView.this, "Failed to save image URL in the database", Toast.LENGTH_SHORT).show();
                });
    }
    private void loadProfilePicture(String username) {
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
                        Glide.with(ProfileView.this)
                                .load(imageUrl)
                                .into(imageView);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle onCancelled event
            }
        });
    }

    public void OnSortButton(View view) {
        QuickSort.printArray(events);
        ascending*=-1;
        QuickSort.sortEvents(events, ascending);
        containerLayout.removeAllViews();
        System.out.println("Sort button clicked");
        QuickSort.printArray(events);
        Intent intent = getIntent();
        String username = intent.getStringExtra("username");

        for (Event event: events) {
            View cardView = LayoutInflater.from(ProfileView.this).inflate(R.layout.event_card, null);

            TextView eventNameTextView = cardView.findViewById(R.id.TitleName);
            TextView eventCreator = cardView.findViewById(R.id.EventCreator);
            eventNameTextView.setText(event.getEventName());
            eventCreator.setText("Organised by: " + event.getCreatedBy());

            int day = event.getDay();
            int month = event.getMonth();
            int year = event.getYear();

            String strDay = day >= 10 ? Integer.toString(day) : "0" + day;
            String strMonth = month >= 10 ? Integer.toString(month) : "0" + month;

            String dateFormatted = String.format("%s/%s/%d", strDay, strMonth, year);

            TextView eventDateTextView = cardView.findViewById(R.id.EventDate);
            eventDateTextView.setText(dateFormatted);

            ShapeableImageView profilePicture = cardView.findViewById(R.id.ProfilePicture);
            profilePicture.setOnClickListener(v -> {
                Intent newIntent = new Intent(getApplicationContext(), ProfileView.class);
                newIntent.putExtra("username", event.getCreatedBy());
                startActivityForResult(newIntent, 0);
            });

            Button roundButton = cardView.findViewById(R.id.roundButton);
            roundButton.setText("Edit");
            roundButton.setTextColor(Color.WHITE);
            roundButton.setOnClickListener(v -> {
                EventEditor dialogHelper = new EventEditor();
                dialogHelper.showDialog(ProfileView.this, ProfileView.this, event, username);
            });

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            int marginInDp = 20;
            int marginInPixels = (int) (marginInDp * getResources().getDisplayMetrics().density);
            int sidemarginInDp = 18;
            int sidemargininPixels = (int) (sidemarginInDp * getResources().getDisplayMetrics().density);

            layoutParams.setMargins(sidemargininPixels, 0, sidemargininPixels, marginInPixels);
            cardView.setLayoutParams(layoutParams);
            containerLayout.addView(cardView);
        }
    }

    private void loadEventsFromFirebase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Events");
        SharedPreferences preferences = getSharedPreferences("loginPrefs", Context.MODE_PRIVATE);
        String userRole = preferences.getString("role", "");
        Intent intent = getIntent();
        String username = intent.getStringExtra("username");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                events.clear(); // Clear existing events
                containerLayout = findViewById(R.id.eventContainer);

                // Remove all views from the container layout
                containerLayout.removeAllViews();

                // Get the container layout where you want to add the dynamic views
                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                    Event event = eventSnapshot.getValue(Event.class);

                    Intent intent = getIntent();
                    String username = intent.getStringExtra("username");

                    if (event != null && userRole.equals("CyclingClub") && event.getCreatedBy().equals(username) || userRole.equals("Admin") && event.getCreatedBy().equals(username)) {
                        events.add(event);

                        View cardView = LayoutInflater.from(ProfileView.this).inflate(R.layout.event_card, null);

                        TextView eventNameTextView = cardView.findViewById(R.id.TitleName);
                        TextView eventCreator = cardView.findViewById(R.id.EventCreator);
                        eventNameTextView.setText(event.getEventName());
                        eventCreator.setText("Organised by: " + event.getCreatedBy());

                        int day = event.getDay();
                        int month = event.getMonth();
                        int year = event.getYear();

                        String strDay = day >= 10 ? Integer.toString(day) : "0" + day;
                        String strMonth = month >= 10 ? Integer.toString(month) : "0" + month;

                        String dateFormatted = String.format("%s/%s/%d", strDay, strMonth, year);

                        TextView eventDateTextView = cardView.findViewById(R.id.EventDate);
                        eventDateTextView.setText(dateFormatted);

                        ShapeableImageView profilePicture = cardView.findViewById(R.id.ProfilePicture);
                        profilePicture.setOnClickListener(v -> {
                            Intent newIntent = new Intent(getApplicationContext(), ProfileView.class);
                            newIntent.putExtra("username", event.getCreatedBy());
                            startActivityForResult(newIntent, 0);
                        });

                        Button roundButton = cardView.findViewById(R.id.roundButton);
                        roundButton.setText("Edit");
                        roundButton.setTextColor(Color.WHITE);
                        roundButton.setOnClickListener(v -> {
                            EventEditor dialogHelper = new EventEditor();
                            dialogHelper.showDialog(ProfileView.this, ProfileView.this, event, username);
                        });
                        TextView location = cardView.findViewById(R.id.location);
                        try {
                            location.setText(event.getLocation());
                        } catch(Exception e){

                        }
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        int marginInDp = 20;
                        int marginInPixels = (int) (marginInDp * getResources().getDisplayMetrics().density);
                        int sidemarginInDp = 18;
                        int sidemargininPixels = (int) (sidemarginInDp * getResources().getDisplayMetrics().density);

                        layoutParams.setMargins(sidemargininPixels, 0, sidemargininPixels, marginInPixels);
                        cardView.setLayoutParams(layoutParams);
                        containerLayout.addView(cardView);
                    } else if (event.getCreatedBy().equals(username)) {
                        events.add(event);

                        View cardView = LayoutInflater.from(ProfileView.this).inflate(R.layout.event_card, null);

                        TextView eventNameTextView = cardView.findViewById(R.id.TitleName);
                        TextView eventCreator = cardView.findViewById(R.id.EventCreator);
                        eventNameTextView.setText(event.getEventName());
                        eventCreator.setText("Organised by: " + event.getCreatedBy());

                        int day = event.getDay();
                        int month = event.getMonth();
                        int year = event.getYear();

                        String strDay = day >= 10 ? Integer.toString(day) : "0" + day;
                        String strMonth = month >= 10 ? Integer.toString(month) : "0" + month;

                        String dateFormatted = String.format("%s/%s/%d", strDay, strMonth, year);

                        TextView eventDateTextView = cardView.findViewById(R.id.EventDate);
                        eventDateTextView.setText(dateFormatted);

                        ShapeableImageView profilePicture = cardView.findViewById(R.id.ProfilePicture);
                        profilePicture.setOnClickListener(v -> {
                            Intent newIntent = new Intent(getApplicationContext(), ProfileView.class);
                            newIntent.putExtra("username", event.getCreatedBy());
                            startActivityForResult(newIntent, 0);
                        });

                        Button roundButton = cardView.findViewById(R.id.roundButton);
                        roundButton.setText("Join");
                        roundButton.setTextColor(Color.WHITE);
                        roundButton.setOnClickListener(v -> {
                            joinDialog(event);
                        });
                        TextView location = cardView.findViewById(R.id.location);
                        try {
                            location.setText(event.getLocation());
                        } catch(Exception e){

                        }
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        int marginInDp = 20;
                        int marginInPixels = (int) (marginInDp * getResources().getDisplayMetrics().density);
                        int sidemarginInDp = 18;
                        int sidemargininPixels = (int) (sidemarginInDp * getResources().getDisplayMetrics().density);

                        layoutParams.setMargins(sidemargininPixels, 0, sidemargininPixels, marginInPixels);
                        cardView.setLayoutParams(layoutParams);
                        containerLayout.addView(cardView);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle onCancelled event
            }
        });
    }
    private void joinDialog(Event event) {
        Dialog joinDialog = new Dialog(ProfileView.this);
        joinDialog.setContentView(R.layout.event_card); // Reusing the event card layout

        // Initialize dialog views and set up event details
        TextView eventNameTextView = joinDialog.findViewById(R.id.TitleName);
        TextView eventCreator = joinDialog.findViewById(R.id.EventCreator);
        TextView eventLocation = joinDialog.findViewById(R.id.location);

        eventNameTextView.setText(event.getEventName());
        eventCreator.setText("Organized by: " + event.getCreatedBy());

        int day = event.getDay();
        int month = event.getMonth();
        int year = event.getYear();

        String strDay = day >= 10 ? Integer.toString(day) : "0" + day;
        String strMonth = month >= 10 ? Integer.toString(month) : "0" + month;

        String dateFormatted = String.format("%s/%s/%d", strDay, strMonth, year);

        TextView eventDateTextView = joinDialog.findViewById(R.id.EventDate);
        eventDateTextView.setText(dateFormatted);

        // Set up other event details as needed
        String eventName = event.getEventName();

        // Fetch requirements from Firebase
        DatabaseReference requirementsRef = FirebaseDatabase.getInstance().getReference()
                .child("Events")
                .child(eventName)
                .child("requirements");

        requirementsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                LinearLayout requirementsLayout = joinDialog.findViewById(R.id.RequirementsText);
                TextView requirement = joinDialog.findViewById(R.id.requirement);
                requirement.setVisibility(View.VISIBLE);

                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

                int bottomMarginInDp = 15;  // Adjust margin as needed
                int bottomMarginInPixels = (int) (bottomMarginInDp * getResources().getDisplayMetrics().density);
                layoutParams.setMargins(0, 0, 0, bottomMarginInPixels);

                requirementsLayout.setLayoutParams(layoutParams);

                for (DataSnapshot requirementSnapshot : dataSnapshot.getChildren()) {
                    String requirementKey = requirementSnapshot.getKey();
                    String requirementValue = requirementSnapshot.getValue(String.class);

                    // Create a new TextView for each requirement
                    TextView requirementTextView = new TextView(ProfileView.this);
                    requirementTextView.setText(requirementKey + ": " + requirementValue);
                    LinearLayout.LayoutParams textViewLayoutParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    textViewLayoutParams.setMargins(bottomMarginInPixels, 0, 0, 0);

                    // Apply layout parameters to the TextView
                    requirementTextView.setLayoutParams(textViewLayoutParams);


                    // Customize the TextView properties
                    requirementTextView.setTextSize(18);  // Adjust text size as needed

                    // Add the TextView to the RequirementsText LinearLayout
                    requirementsLayout.addView(requirementTextView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }
        });

        // Set up dialog button
        Button joinButton = joinDialog.findViewById(R.id.roundButton);

        joinButton.setText("Join");
        joinButton.setOnClickListener(v -> {
            DatabaseReference participantsRef = FirebaseDatabase.getInstance().getReference()
                    .child("Events")
                    .child(eventName)
                    .child("Participants");

            SharedPreferences preferences = getSharedPreferences("loginPrefs", Context.MODE_PRIVATE);
            String userId = preferences.getString("username", ""); // Replace with the actual user ID
            participantsRef.child(userId).setValue(true);
            DatabaseReference participantsRefs = FirebaseDatabase.getInstance().getReference()
                    .child("Accounts")
                    .child(userId)
                    .child("Events");
            participantsRefs.child(eventName).setValue(eventName);

            joinDialog.dismiss();
        });

        // Set up the dimming overlay
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(joinDialog.getWindow().getAttributes());

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int dialogWidth = (int) (displayMetrics.widthPixels * 0.90f);
        int dialogHeight = WindowManager.LayoutParams.WRAP_CONTENT;

        joinDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        layoutParams.width = dialogWidth;
        layoutParams.height = dialogHeight;

        // Add dim overlay to the window
        WindowManager.LayoutParams dimLayoutParams = new WindowManager.LayoutParams();
        dimLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        dimLayoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        dimLayoutParams.format = PixelFormat.TRANSLUCENT;

        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        View dimOverlay = new View(ProfileView.this);
        dimOverlay.setBackgroundColor(Color.argb(128, 0, 0, 0));
        windowManager.addView(dimOverlay, dimLayoutParams);

        joinDialog.getWindow().setAttributes(layoutParams);

        dimOverlay.setOnClickListener(v -> {
            joinDialog.dismiss();
            windowManager.removeView(dimOverlay);
        });

        // Set dismiss listener to remove dim overlay
        joinDialog.setOnDismissListener(dialogInterface -> windowManager.removeView(dimOverlay));

        // Show the dialog
        joinDialog.show();
    }

    // Set up click listener for the menu profile button
    private void ImageButton() {
        ImageButton menuButton = findViewById(R.id.menuProfileButton);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = getIntent();
                String username = intent.getStringExtra("username");
                Intent intentOne = new Intent(getApplicationContext(), ProfileSettings.class);
                intentOne.putExtra("username", username);
                startActivity(intentOne);
                finish();
            }
        });
    }

    private void loadProfile(String username) {
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference().child("Profile").child(username);

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (isDestroyed()) {
                    // The activity is destroyed, don't proceed
                    return;
                }

                if (dataSnapshot.exists()) {
                    Profile profile = dataSnapshot.getValue(Profile.class);
                    TextView ProfileName = findViewById(R.id.ProfileName);
                    TextView location = findViewById(R.id.location);
                    ProfileName.setText(!profile.getDisplayName().isEmpty() ? profile.getDisplayName() : username);
                    location.setText(!profile.getLocation().isEmpty() ? profile.getLocation() : "None");

                    try {
                        associateSocialMediaLinks(profile.getSocialMediaLinks());
                    } catch (Exception exception) {
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle onCancelled event
            }
        });
    }
    private void associateSocialMediaLinks(Map<String, String> socialMediaLinks) {
        // Get ImageButton references
        ImageButton imageButton1 = findViewById(R.id.imageButton1);
        ImageButton imageButton2 = findViewById(R.id.imageButton2);
        ImageButton imageButton3 = findViewById(R.id.imageButton3);
        ImageButton imageButton4 = findViewById(R.id.imageButton4);

        // Create a list of ImageButton references for easy access
        List<ImageButton> imageButtons = Arrays.asList(imageButton1, imageButton2, imageButton3, imageButton4);

        // Loop through each social media link in the map and set the corresponding icon
        int buttonNumber = 0;
        for (Map.Entry<String, String> entry : socialMediaLinks.entrySet()) {
            if (buttonNumber >= imageButtons.size()) {
                break;
            }

            String socialMediaName = entry.getKey();
            System.out.println(socialMediaName);
            String link = entry.getValue();

            String iconName = socialMediaName;

            downloadAndSetIcon(iconName, link, imageButtons.get(buttonNumber));

            buttonNumber++;
        }
    }

    private void downloadAndSetIcon(String iconName, String link, ImageButton imageButton) {
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference().child("Icons").child("CyclingOttawa");

        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (isDestroyed()) {
                    // The activity is destroyed, don't proceed
                    return;
                }

                if (dataSnapshot.exists()) {
                    // Check if the map is empty
                    if (dataSnapshot.getChildrenCount() == 0) {
                        // Map is empty, display "help" image
                        displayHelpImage(imageButton);
                        return;
                    }

                    // Retrieve the image URL from the database
                    String imageUrl = dataSnapshot.child(iconName).getValue(String.class);

                    // Check if the image URL is null or empty
                    if (imageUrl == null || imageUrl.isEmpty()) {
                        // Image URL is invalid, display "help" image
                        displayHelpImage(imageButton);
                        return;
                    }

                    // Load the image using Glide only if the activity is still valid
                    if (!isDestroyed()) {
                        imageButton.setVisibility(View.VISIBLE);
                        Glide.with(ProfileView.this)
                                .load(imageUrl)
                                .into(imageButton);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle onCancelled event
            }
        });

        // Set a click listener to open the associated link
        imageButton.setOnClickListener(v -> openLinkInBrowser(link));
    }

    private void displayHelpImage(ImageButton imageButton) {
        // Set the "help" image to the ImageButton
        imageButton.setImageResource(R.drawable.baseline_error); // Replace with the actual resource ID of your "help" image
        imageButton.setVisibility(View.VISIBLE);

        // Set a click listener to provide help or handle the case
        imageButton.setOnClickListener(v -> {
            Toast.makeText(ProfileView.this, "Help: Invalid or missing icon", Toast.LENGTH_LONG).show();
        });
    }


    private void openLinkInBrowser(String link) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
            startActivity(intent);
        }catch (ActivityNotFoundException exception){
            Toast.makeText(ProfileView.this, "Invalid link", Toast.LENGTH_LONG).show(); // Add show() method
        }
    }

}
