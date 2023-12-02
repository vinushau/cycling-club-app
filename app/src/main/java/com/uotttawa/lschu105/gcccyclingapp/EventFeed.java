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
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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

public class EventFeed extends AppCompatActivity {
    private ArrayList<Event> events;
    private LinearLayout containerLayout;
    private ShapeableImageView imageView;
    private ArrayList<CardView> selectedDialogEventTypes;
    private LinearLayout tagContainerLayout;
    private ArrayList<String> selectedTags;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_feed);
        EditText searchField = findViewById(R.id.searchField);

        tagContainerLayout = findViewById(R.id.tagContainerLayout);
        ImageView filterIcon = findViewById(R.id.filterIcon);
        DatabaseReference eventTypesRef = FirebaseDatabase.getInstance().getReference("EventTypes");
        events = new ArrayList<>();
        selectedTags = new ArrayList<>();
        loadEventsFromFirebase();
        filterIcon.setOnClickListener(v -> {
            showDialog();
        });
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

    private void createEventCard(Event event) {
        View cardView = LayoutInflater.from(EventFeed.this).inflate(R.layout.event_card, null);

        TextView eventNameTextView = cardView.findViewById(R.id.TitleName);
        TextView eventCreator = cardView.findViewById(R.id.EventCreator);
        TextView eventLocation = cardView.findViewById(R.id.location);
        eventNameTextView.setText(event.getEventName());
        eventCreator.setText("Organized by: " + event.getCreatedBy());

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
        roundButton.setText("View");
        roundButton.setTextColor(Color.WHITE);
        roundButton.setOnClickListener(v -> {
            EventEditor dialogHelper = new EventEditor();
            dialogHelper.showDialog(EventFeed.this, EventFeed.this, event, event.getCreatedBy());
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

        layoutParams.setMargins(sidemargininPixels, 5, sidemargininPixels, marginInPixels);
        cardView.setLayoutParams(layoutParams);
        ShapeableImageView image = cardView.findViewById(R.id.ProfilePicture);
        loadProfilePicture(event.getCreatedBy(), image);
        containerLayout.addView(cardView);
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
                        Glide.with(EventFeed.this)
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
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Events");
        SharedPreferences preferences = getSharedPreferences("loginPrefs", Context.MODE_PRIVATE);
        String userRole = preferences.getString("role", "");
        Intent intent = getIntent();
        String username = intent.getStringExtra("username");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                containerLayout = findViewById(R.id.eventContainer);

                // Remove all views from the container layout
                containerLayout.removeAllViews();

                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                    Event event = eventSnapshot.getValue(Event.class);
                    events.add(event);
                    createEventCard(event);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle onCancelled event
            }
        });
    }

    private void showDialog() {
        Dialog dialog = new Dialog(EventFeed.this);
        dialog.setContentView(R.layout.search_filter_dialog);

        // Set up dialog button
        Button dialogButton = dialog.findViewById(R.id.dialogButton);
        dialogButton.setBackgroundColor(Color.parseColor("#7169E4"));
        dialogButton.setTextColor(Color.WHITE);
        Button dialogButton2 = dialog.findViewById(R.id.dialogButton2);

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
        View dimOverlay = new View(EventFeed.this);
        dimOverlay.setBackgroundColor(Color.argb(128, 0, 0, 0));
        windowManager.addView(dimOverlay, dimLayoutParams);

        dialog.getWindow().setAttributes(layoutParams);

        dimOverlay.setOnClickListener(v -> {
            dialog.dismiss();
            windowManager.removeView(dimOverlay);
        });
        dialogButton.setOnClickListener(v -> {
            applyFilter(dialog);
            dialog.dismiss();
            windowManager.removeView(dimOverlay);
        });
        dialogButton2.setOnClickListener(v -> {
            selectedTags.clear();
            dialog.dismiss();
            windowManager.removeView(dimOverlay);
        });

        dialog.setCanceledOnTouchOutside(true);
        dialog.setOnDismissListener(dialogInterface -> windowManager.removeView(dimOverlay));

        dialog.show();
        GridLayout eventTypeContainer = dialog.findViewById(R.id.eventTypeContainer);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("EventTypes");
        selectedDialogEventTypes = new ArrayList<CardView>();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Remove all views from the container layout
                int i = 0;
                // Get the container layout where you want to add the dynamic views
                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                    Event event = eventSnapshot.getValue(Event.class);
                    View eventCardView = LayoutInflater.from(EventFeed.this).inflate(R.layout.event_type_card, null);

                    GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                    params.width = 0; // Set width to 0dp
                    params.height = GridLayout.LayoutParams.WRAP_CONTENT;
                    params.setGravity(Gravity.CENTER);
                    params.columnSpec = GridLayout.spec(i % 2, 1f); // Alternating columns (0 or 1) with equal weight
                    params.rowSpec = GridLayout.spec(i / 2);       // Rows (0 or 1)
                    Button button = eventCardView.findViewById(R.id.button5);
                    CardView card = eventCardView.findViewById(R.id.card);

                    button.setText(event.getName());

                    button.setOnClickListener(v -> {
                        if (card.getTag() == null || !(boolean) card.getTag()) {
                            card.setCardBackgroundColor(Color.parseColor("#7169E4"));
                            button.setTextColor(Color.WHITE);
                            selectedTags.add(event.getName().replace(" ", ""));
                            card.setTag(true);
                        } else {
                            card.setCardBackgroundColor(Color.WHITE);
                            button.setTextColor(Color.parseColor("#494848"));
                            selectedTags.remove(event.getName().replace(" ", ""));
                            card.setTag(false);
                        }
                    });
                    if (selectedTags.contains(event.getName().replace(" ", ""))) {
                        card.setCardBackgroundColor(Color.parseColor("#7169E4"));
                        button.setTextColor(Color.WHITE);
                        card.setTag(true);
                    } else{
                        card.setCardBackgroundColor(Color.WHITE);
                        card.setTag(false);
                    }
                    eventTypeContainer.addView(eventCardView, params);
                    i++;
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

    private void applyFilter(Dialog dialog) {
        EditText eventSearchField = dialog.findViewById(R.id.eventsearchfield);
        EditText cyclingclubSearchField = dialog.findViewById(R.id.accountsearchfield);
        String eventSearch = eventSearchField.getText().toString();
        String cyclingclubSearch = cyclingclubSearchField.getText().toString();
        //Basically it should use similar logic to load firebase, just filter out
        //events that dont contain whats in the search fields. So basically
        //just a if statement that checks "event.get name or eventname" == eventsearch.totext
        // use selectedTags arraylist so you can filter out events that dont have the right type,
        // to find the event type jut use event.getEventType in the if statement
    }

    //Filters out events that do not contain whats in the search field
    private void searchBar(String searchField){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Events");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                containerLayout = findViewById(R.id.eventContainer);
                containerLayout.removeAllViews();

                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                    Event event = eventSnapshot.getValue(Event.class);
                    System.out.println(event.getEventName());
                    if (event.getEventName().toLowerCase().contains(searchField.toLowerCase())) {
                        events.add(event);
                        createEventCard(event);
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
        Intent intent = new Intent(this, WelcomePage.class);
        startActivity(intent);
        finish();
    }
}
