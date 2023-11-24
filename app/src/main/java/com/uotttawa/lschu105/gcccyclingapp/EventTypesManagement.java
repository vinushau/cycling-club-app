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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
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
import java.util.List;

import android.widget.EditText;
import android.widget.Space;
import android.widget.Toast;

public class EventTypesManagement extends AppCompatActivity {
    private List<Event> eventTypes;
    private boolean isValidationSuccessful = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_type_management);
        eventTypes = new ArrayList<>();
        ImageButton addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(v -> showAddDialog());

        loadEventTypesFromFirebase();
    }
    EditText descriptionEditText;
    Integer indexRequirement = 3;

    private void loadEventTypesFromFirebase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("EventTypes");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                eventTypes.clear(); // Clear existing event types

                // Get the container layout where you want to add the dynamic views
                LinearLayout eventContainer = findViewById(R.id.eventContainer);

                for (DataSnapshot eventTypeSnapshot : dataSnapshot.getChildren()) {
                    Event eventType = eventTypeSnapshot.getValue(Event.class);
                    if (eventType != null) {
                        eventTypes.add(eventType);

                        // Inflate the XML layout dynamically
                        View cardView = LayoutInflater.from(EventTypesManagement.this).inflate(R.layout.item_event_types, null);

                        // Customize the inflated view based on the event type
                        TextView eventTypeNameTextView = cardView.findViewById(R.id.TitleName);
                        eventTypeNameTextView.setText(eventType.getName());
                        Button roundButton = cardView.findViewById(R.id.roundButton);
                        roundButton.setText("Edit");
                        roundButton.setBackgroundColor(Color.parseColor("#7169E4")); // Set the background color to red                        roundButton.setText("Delete"); // Set the text to "Delete"
                        roundButton.setTextColor(Color.WHITE); // Set text color to white
                        roundButton.setOnClickListener(v -> showUpdateDialog(eventType));
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        int marginInDp = 16; // Set the desired margin in dp
                        int marginInPixels = (int) (marginInDp * getResources().getDisplayMetrics().density);
                        layoutParams.setMargins(0, 0, 0, marginInPixels); // Set margins here
                        cardView.setLayoutParams(layoutParams);

                        // Add the inflated view to the eventContainer layout
                        eventContainer.addView(cardView);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle onCancelled event
            }
        });
    }

    //Creates a dialog window for editing an event type.
    private void showUpdateDialog(Event eventType) {

        // Dialog setup
        Dialog dialog = new Dialog(EventTypesManagement.this);
        dialog.setContentView(R.layout.event_creation_dialog);

        // Setup UI elements
        TextView dialogTitle = dialog.findViewById(R.id.dialogTitle);
        Button dialogButton = dialog.findViewById(R.id.dialogButton);
        dialogTitle.setText(eventType.getName());
        EditText TitleField = dialog.findViewById(R.id.nameField);
        TitleField.setText(eventType.getName());
        Button updateButton = dialog.findViewById(R.id.updateButton);
        updateButton.setVisibility(View.VISIBLE);
        dialogButton.setText("Delete");
        dialogButton.setBackgroundColor(Color.parseColor("#DB4D4D"));
        dialogButton.setTextColor(Color.WHITE);

        // Set dialog dimensions
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(dialog.getWindow().getAttributes());
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int dialogWidth = (int) (displayMetrics.widthPixels * 0.8f);
        int dialogHeight = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.width = dialogWidth;
        layoutParams.height = dialogHeight;

        // Set dialog window properties
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setAttributes(layoutParams);

        // Dim overlay setup
        WindowManager.LayoutParams dimLayoutParams = new WindowManager.LayoutParams();
        dimLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        dimLayoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        dimLayoutParams.format = PixelFormat.TRANSLUCENT;
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        View dimOverlay = new View(EventTypesManagement.this);
        dimOverlay.setBackgroundColor(Color.argb(128, 0, 0, 0));
        windowManager.addView(dimOverlay, dimLayoutParams);

        // Dialog dismiss logic
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

        // Display dialog window
        dialog.show();


        // Setup requirements input fields
        String buttonName = eventType.getName().replace(" ", "");
        DatabaseReference eventTypeReference = FirebaseDatabase.getInstance().getReference("EventTypes").child(buttonName);
        eventTypeReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    createEditTextFields(dialog, eventType.getRequirementsList());
                } else {
                    System.out.println("failed"); //for me
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });


        // Setup button logic
        updateButton.setOnClickListener(v -> {
            if (TextFieldValidation(dialog)) {
                updateEventType(eventType.getName(), dialog);
                Intent intent = new Intent(getApplicationContext(), EventTypesManagement.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(getApplicationContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            }
        });
        dialogButton.setOnClickListener(v -> {
            deleteEventTypeNode(eventType.getName().replace(" ", ""));
            dialog.dismiss();
            windowManager.removeView(dimOverlay);
            Intent intent = new Intent(getApplicationContext(), EventTypesManagement.class);
            startActivity(intent);
            finish();
        });


        // Setup description input field
        int editTextWidthSP = 300;
        int editTextWidthPixels = (int) (editTextWidthSP * dialog.getContext().getResources().getDisplayMetrics().scaledDensity);
        LinearLayout linear = dialog.findViewById(R.id.LinearLayout);
        descriptionEditText = new EditText(dialog.getContext());
        descriptionEditText.setLayoutParams(new LinearLayout.LayoutParams(
                editTextWidthPixels,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        descriptionEditText.setText(eventType.getDescription());
        descriptionEditText.setHint("description");
        descriptionEditText.setId(View.generateViewId());
        descriptionEditText.setTextColor(dialog.getContext().getResources().getColor(R.color.black));
        descriptionEditText.setTextSize(20);
        linear.addView(descriptionEditText, 2);
    }

    private void showAddDialog() {

        // Dialog setup
        Dialog dialog = new Dialog(EventTypesManagement.this);
        dialog.setContentView(R.layout.event_creation_dialog);

        // Setup UI elements
        TextView dialogTitle = dialog.findViewById(R.id.dialogTitle);
        Button dialogButton = dialog.findViewById(R.id.dialogButton);
        dialogTitle.setText("Create Event Type");
        EditText TitleField = dialog.findViewById(R.id.nameField);
        TitleField.setHint("Add Event Type Name");
        dialogButton.setText("Create");
        dialogButton.setTextColor(Color.WHITE);

        // Set dialog dimensions
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(dialog.getWindow().getAttributes());
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int dialogWidth = (int) (displayMetrics.widthPixels * 0.8f);
        int dialogHeight = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.width = dialogWidth;
        layoutParams.height = dialogHeight;

        // Set dialog window properties
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setAttributes(layoutParams);

        // Dim overlay setup
        WindowManager.LayoutParams dimLayoutParams = new WindowManager.LayoutParams();
        dimLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        dimLayoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        dimLayoutParams.format = PixelFormat.TRANSLUCENT;
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        View dimOverlay = new View(EventTypesManagement.this);
        dimOverlay.setBackgroundColor(Color.argb(128, 0, 0, 0));
        windowManager.addView(dimOverlay, dimLayoutParams);

        // Dialog dismiss logic
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

        // Display dialog window
        dialog.show();

        // Setup button logic
        dialogButton.setOnClickListener(v -> {
            if (TextFieldValidation(dialog)) {
                System.out.println("there");
                createEventType(dialog);
                System.out.println("here");
            } else {
                Toast.makeText(getApplicationContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            }
        });


        // Setup description input field
        int editTextWidthSP = 300;
        int editTextWidthPixels = (int) (editTextWidthSP * dialog.getContext().getResources().getDisplayMetrics().scaledDensity);
        LinearLayout linear = dialog.findViewById(R.id.LinearLayout);
        descriptionEditText = new EditText(dialog.getContext());
        descriptionEditText.setLayoutParams(new LinearLayout.LayoutParams(
                editTextWidthPixels,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        descriptionEditText.setHint("Add description");
        descriptionEditText.setId(View.generateViewId());
        descriptionEditText.setTextColor(dialog.getContext().getResources().getColor(R.color.black));
        descriptionEditText.setTextSize(20);
        linear.addView(descriptionEditText, 2);

        Button addRequirement = dialog.findViewById(R.id.addRequirement);
        addRequirement.setVisibility(View.VISIBLE);
        addRequirement.setOnClickListener(v -> {
            // Setup description input field
            EditText text = new EditText(dialog.getContext());
            text.setLayoutParams(new LinearLayout.LayoutParams(
                    editTextWidthPixels,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            text.setHint("Requirement");
            text.setId(View.generateViewId());
            text.setTextColor(dialog.getContext().getResources().getColor(R.color.black));
            text.setTextSize(20);
            linear.addView(text, indexRequirement++);
        });
    }

    private boolean createFirebaseEntry(Event eventObject, String eventName) {
        DatabaseReference eventsReference = FirebaseDatabase.getInstance().getReference("EventTypes");

        try {
            DatabaseReference eventType = eventsReference.child(eventName);
            eventType.setValue(eventObject);
        } catch (Exception e) {
            System.out.println("Here");
            return false;
        }
        return true;
    }
    private void deleteEventTypeNode(String eventName) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("EventTypes");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot eventTypeSnapshot : dataSnapshot.getChildren()) {
                    String eventType = eventTypeSnapshot.getKey();
                    if (eventType != null && eventType.equals(eventName)) {
                        eventTypeSnapshot.getRef().removeValue();
                        Toast.makeText(getApplicationContext(), "Event Type Deleted", Toast.LENGTH_LONG).show();
                        return;
                    }
                }
                Toast.makeText(getApplicationContext(), "Event Type Not Found", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private List<String> getRequirements(Dialog dialog) {
        LinearLayout linear = dialog.findViewById(R.id.LinearLayout);
        List<String> requirementsList = new ArrayList<>();

        for (int i = 0; i < linear.getChildCount(); i++) {
            View childView = linear.getChildAt(i);

            if (childView instanceof EditText) {
                EditText editText = (EditText) childView;
                if (!editText.getHint().toString().toLowerCase().equals("name") && !editText.getHint().toString().toLowerCase().equals("description") && !editText.getHint().toString().equals("Add description") && !editText.getHint().toString().equals("Add Event Type Name")) {
                    String requirement = editText.getText().toString();
                    requirementsList.add(requirement);
                }
            }
        }
        return requirementsList;
    }

    private boolean TextFieldValidation(Dialog dialog) {
        LinearLayout linear = dialog.findViewById(R.id.LinearLayout);
        boolean isValidationSuccessful = true;

        for (int i = 0; i < linear.getChildCount(); i++) {
            View childView = linear.getChildAt(i);

            if (childView instanceof EditText) {
                EditText editText = (EditText) childView;
                if (editText.getText().toString().trim().isEmpty()) {
                    isValidationSuccessful = false;
                    break;
                }
            }
        }

        return isValidationSuccessful;
    }
    private void createEditTextFields(Dialog dialog, List<String> requirementsList) {
        int editTextWidthSP = 300;
        int spaceHeightPixels = 25;

        int editTextWidthPixels = (int) (editTextWidthSP * dialog.getContext().getResources().getDisplayMetrics().scaledDensity);

        LinearLayout linearLayout = dialog.findViewById(R.id.LinearLayout);

        Space space = new Space(dialog.getContext());
        space.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                spaceHeightPixels));
        linearLayout.addView(space);

        int index = 5;

        for (int i = 0; i < requirementsList.size(); i++) {
            String requirementValue = requirementsList.get(i);

            // Create a TextView for the requirement key
            TextView keyTextView = new TextView(dialog.getContext());
            keyTextView.setLayoutParams(new LinearLayout.LayoutParams(
                    editTextWidthPixels,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            keyTextView.setText("Requirement " + (i + 1) + ":");
            keyTextView.setTextColor(dialog.getContext().getResources().getColor(R.color.black));
            keyTextView.setTextSize(20);
            linearLayout.addView(keyTextView, index);
            index++;

            // Create an EditText for the requirement value
            EditText editText = new EditText(dialog.getContext());
            editText.setLayoutParams(new LinearLayout.LayoutParams(
                    editTextWidthPixels,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            editText.setText(requirementValue);
            editText.setHint("Requirement " + (i + 1));
            editText.setTextColor(dialog.getContext().getResources().getColor(R.color.black));
            editText.setTextSize(20);
            linearLayout.addView(editText, index);
            index++;
        }
    }
    private boolean createEventType(Dialog dialog) {
        TextFieldValidation(dialog);

        EditText eventNameEditText = dialog.findViewById(R.id.nameField);
        String eventName = eventNameEditText.getText().toString();
        EditText descriptionEditTexts = dialog.findViewById(descriptionEditText.getId());
        String description = descriptionEditTexts.getText().toString();


        List<String> RequirementList = getRequirements(dialog);

        Event eventObject = new Event(description, eventName, RequirementList);
        createFirebaseEntry(eventObject, eventName.replace(" ", ""));
        return true;
    }

    private boolean updateEventType(String eventtypes, Dialog dialog) {
        TextFieldValidation(dialog);
        if (!isValidationSuccessful) {
            return false;
        }
        EditText eventNameEditText = dialog.findViewById(R.id.nameField);
        String eventNameField = eventNameEditText.getText().toString();
        EditText descriptionEditTexts = dialog.findViewById(descriptionEditText.getId());
        String description = descriptionEditTexts.getText().toString();


        String eventName = eventNameField;
        List<String> RequirementList = getRequirements(dialog);

        DatabaseReference oldEventReference = FirebaseDatabase.getInstance().getReference("EventTypes").child(eventtypes.replace(" ", ""));
        oldEventReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    oldEventReference.removeValue(new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                DatabaseReference userEventsRef = FirebaseDatabase.getInstance().getReference().child("EventTypes");
                                userEventsRef.child(eventtypes).removeValue(new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError error, DatabaseReference ref) {
                                        if (error == null) {
                                            Event eventObject = new Event(description, eventName, RequirementList);
                                            createFirebaseEntry(eventObject, eventName.replace(" ", ""));
                                        } else {
                                        }
                                    }
                                });
                            } else {
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        return true;
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), WelcomePage.class);
        startActivity(intent);
        finish();
    }
}
