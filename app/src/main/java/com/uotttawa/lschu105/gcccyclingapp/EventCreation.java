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
import java.util.ArrayList;
import java.util.List;
import android.content.SharedPreferences;
import android.widget.EditText;
import android.widget.Space;
import android.widget.Spinner;
import android.widget.Toast;
import java.util.HashMap;
import java.util.Map;

public class EventCreation extends AppCompatActivity {
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

    private List<Event> eventTypes;
    private boolean isValidationSuccessful = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_creation);
        eventTypes = new ArrayList<>();

        loadEventTypesFromFirebase();
    }

    private void loadEventTypesFromFirebase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("EventTypes");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                eventTypes.clear(); // Clear existing event types
                LinearLayout containerLayout = findViewById(R.id.eventContainer);

                for (DataSnapshot eventTypeSnapshot : dataSnapshot.getChildren()) {
                    Event eventType = eventTypeSnapshot.getValue(Event.class);
                    System.out.println("1" + eventType.getRequirementsList());

                    if (eventType != null) {
                        eventTypes.add(eventType);

                        View cardView = LayoutInflater.from(EventCreation.this).inflate(R.layout.item_event_types, null);

                        TextView eventTypeNameTextView = cardView.findViewById(R.id.TitleName);
                        eventTypeNameTextView.setText(eventType.getName());

                        Button roundButton = cardView.findViewById(R.id.roundButton);
                        roundButton.setText("Choose");
                        roundButton.setBackgroundColor(Color.parseColor("#7169E4"));
                        roundButton.setText("Create");
                        roundButton.setTextColor(Color.WHITE);
                        roundButton.setOnClickListener(v -> showDialog(eventType));
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle onCancelled event
            }
        });
    }

    private void showDialog(Event eventType) {
        // Create a dialog to display event type details
        Dialog dialog = new Dialog(EventCreation.this);
        dialog.setContentView(R.layout.event_creation_dialog);

        // Set dialog title
        TextView dialogTitle = dialog.findViewById(R.id.dialogTitle);
        dialogTitle.setText(eventType.getName());

        // Create text field
        createTextField(dialog, eventType.getDescription());

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
        View dimOverlay = new View(EventCreation.this);
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

        // Retrieve event type details from Firebase
        String buttonName = eventType.getName().replace(" ", "");
        DatabaseReference eventTypeReference = FirebaseDatabase.getInstance().getReference("EventTypes").child(buttonName);
        String[] levelOptions = {"Difficulty Level", "Beginner", "Intermediate", "Advanced", "All"};
        AdapterView levelSpinner = dialog.findViewById(R.id.levelSpinner);
        levelSpinner.setVisibility(View.VISIBLE);

        // Set up level spinner
        ArrayAdapter<String> adapter = new ArrayAdapter(EventCreation.this, android.R.layout.simple_spinner_item, levelOptions) {
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                ((TextView) view).setTextColor(getResources().getColor(position == 0 ? android.R.color.darker_gray : android.R.color.black));
                view.setEnabled(position != 0);
                return view;
            }
        };

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        levelSpinner.setAdapter(adapter);
        levelSpinner.setSelection(0);

        eventTypeReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String groupName = dataSnapshot.child("name").getValue(String.class);
                    String description = dataSnapshot.child("description").getValue(String.class);
                    createEditTextFields(dialog, dataSnapshot.child("requirementsList"));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        // Handle button click
        dialogButton.setOnClickListener(v -> {
            // Validate the text fields
            TextFieldValidation(dialog);

            // Check if validation is successful
            if (isValidationSuccessful && createEvent(buttonName, dialog)) {
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up number pickers and their containers
        selectedNumberDisplay = dialog.findViewById(R.id.selectedNumberDisplay);
        selectedNumberDisplayMonth = dialog.findViewById(R.id.selectedNumberDisplayMonth);
        selectedNumberDisplayYear = dialog.findViewById(R.id.selectedNumberDisplayYear);
        numberPickerContainer = dialog.findViewById(R.id.numberPickerContainer);
        numberPickerContainerMonth = dialog.findViewById(R.id.numberPickerContainerMonth);
        numberPickerContainerYear = dialog.findViewById(R.id.numberPickerContainerYear);
        numberPicker = dialog.findViewById(R.id.numberPicker);
        numberPickerMonth = dialog.findViewById(R.id.numberPickerMonth);
        numberPickerYear = dialog.findViewById(R.id.numberPickerYear);
        container = dialog.findViewById(R.id.containerMonth);

        LinearLayout number = dialog.findViewById(R.id.number);
        number.setVisibility(View.VISIBLE);
        selectedNumberDisplay = dialog.findViewById(R.id.selectedNumberDisplay);
        selectedNumberDisplayMonth = dialog.findViewById(R.id.selectedNumberDisplayMonth);
        selectedNumberDisplayYear = dialog.findViewById(R.id.selectedNumberDisplayYear);

        // Set up the NumberPicker
        setupNumberPicker();
        setupNumberPickerMonth();
        setupNumberPickerYear();
        selectedNumberDisplay.setText(String.valueOf(numberPicker.getValue()));
        selectedNumberDisplayMonth.setText(String.valueOf(numberPickerMonth.getValue()));
        selectedNumberDisplayYear.setText(String.valueOf(numberPickerYear.getValue()));

        // Set click listeners for number picker containers
        numberPickerContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleNumberPickerVisibility();
            }
        });

        numberPickerContainerMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleNumberPickerVisibilityMonth();
            }
        });

        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleNumberPickerVisibilityMonth();
            }
        });

        numberPickerContainerYear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleNumberPickerVisibilityYear();
            }
        });
    }

    private void toggleNumberPickerVisibilityMonth() {
        if (container.getVisibility() == View.VISIBLE) {
            container.setVisibility(View.GONE);
            selectedNumberDisplayMonth.setVisibility(View.VISIBLE);
        } else {
            container.setVisibility(View.VISIBLE);
            selectedNumberDisplayMonth.setVisibility(View.GONE);
            numberPickerYear.setVisibility(View.GONE);
            selectedNumberDisplayYear.setVisibility(View.VISIBLE);
            numberPicker.setVisibility(View.GONE);
            selectedNumberDisplay.setVisibility(View.VISIBLE);

        }
    }
    private void toggleNumberPickerVisibilityYear() {
        if (numberPickerYear.getVisibility() == View.VISIBLE) {
            numberPickerYear.setVisibility(View.GONE);
            selectedNumberDisplayYear.setVisibility(View.VISIBLE);
        } else {
            container.setVisibility(View.GONE);
            selectedNumberDisplayMonth.setVisibility(View.VISIBLE);
            numberPicker.setVisibility(View.GONE);
            selectedNumberDisplay.setVisibility(View.VISIBLE);
            numberPickerYear.setVisibility(View.VISIBLE);
            selectedNumberDisplayYear.setVisibility(View.GONE);
        }
    }
    private void setupNumberPickerYear() {
        // Set the range of values for the NumberPicker (1 to 31)
        numberPickerYear.setMinValue(2023);
        numberPickerYear.setMaxValue(2100);

        // Set the initial value (day of the month)
        numberPickerYear.setValue(2023);

        // Set a listener for when the value changes
        numberPickerYear.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                // Update the displayed number when the value changes
                selectedNumberDisplayYear.setText(String.valueOf(newVal));
            }
        });


    }

    private void setupNumberPickerMonth() {
        numberPickerMonth.setMinValue(1);
        numberPickerMonth.setMaxValue(12);
        numberPickerMonth.setValue(1);

        numberPickerMonth.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                selectedNumberDisplayMonth.setText(String.valueOf(newVal));
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
    private boolean createEvent(String buttonName, Dialog dialog) {
        Spinner spinner = dialog.findViewById(R.id.levelSpinner);
        TextView selectedNumberDisplay = dialog.findViewById(R.id.selectedNumberDisplay);
        TextView selectedNumberDisplayMonth = dialog.findViewById(R.id.selectedNumberDisplayMonth);
        TextView selectedNumberDisplayYear = dialog.findViewById(R.id.selectedNumberDisplayYear);

        String day = selectedNumberDisplay.getText().toString();
        String month = selectedNumberDisplayMonth.getText().toString();
        String year = selectedNumberDisplayYear.getText().toString();
        String dateFormatted = String.format("%s/%s/%s", day, month, year);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        try {
            LocalDate date = LocalDate.parse(dateFormatted, formatter);

            // Check if the day of the month is valid for the given month and year
            if (date.getDayOfMonth() != Integer.parseInt(day)) {
                Toast.makeText(this, "Invalid date", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (DateTimeException | NumberFormatException e) {
            Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show();
            return false;
        }

        TextFieldValidation(dialog);

        if (!isValidationSuccessful) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return false;
        }

        EditText eventNameEditText = dialog.findViewById(R.id.nameField);
        String eventNameField = eventNameEditText.getText().toString();

        // Check if a valid level is selected
        if (spinner.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select a valid difficulty level", Toast.LENGTH_SHORT).show();
            return false;
        }

        String selectedLevel = spinner.getSelectedItem().toString();

        // Retrieve other necessary data for event creation
        Map<String, String> requirementsMap = getRequirements(dialog);
        SharedPreferences preferences = getSharedPreferences("loginPrefs", Context.MODE_PRIVATE);
        String savedUsername = preferences.getString("username", "");
        int selectedDay = Integer.parseInt(day);
        int selectedMonth = Integer.parseInt(month);
        int selectedYear = Integer.parseInt(year);

        // Create the Event object
        Event eventObject = new Event(savedUsername, selectedLevel, buttonName, eventNameField, requirementsMap, selectedDay, selectedMonth, selectedYear);

        // Create Firebase entry for the event
        return createFirebaseEntry(eventObject, eventNameField);
    }

    private void setupNumberPicker() {
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(31);
        numberPicker.setValue(1);

        numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                selectedNumberDisplay.setText(String.valueOf(newVal));
            }
        });
    }

    private void toggleNumberPickerVisibility() {
        if (numberPicker.getVisibility() == View.VISIBLE) {
            numberPicker.setVisibility(View.GONE);
            selectedNumberDisplay.setVisibility(View.VISIBLE);
        } else {
            container.setVisibility(View.GONE);
            selectedNumberDisplayMonth.setVisibility(View.VISIBLE);
            numberPicker.setVisibility(View.VISIBLE);
            selectedNumberDisplay.setVisibility(View.GONE);
            numberPickerYear.setVisibility(View.GONE);
            selectedNumberDisplayYear.setVisibility(View.VISIBLE);

        }
    }


    private boolean createFirebaseEntry(Event eventObject, String eventName) {
        DatabaseReference eventsReference = FirebaseDatabase.getInstance().getReference("Events");

        boolean success = true;

        DatabaseReference eventReference = eventsReference.child(eventName);
        DatabaseReference userEventsReference = FirebaseDatabase.getInstance().getReference("Accounts")
                .child(eventObject.getCreatedBy()).child("Events").child(eventName);

        try {
            eventReference.setValue(eventObject);
            userEventsReference.setValue(eventName);
        } catch (Exception e) {
            success = false;
        }

        return success;
    }
    private Map<String, String> getRequirements(Dialog dialog) {
        LinearLayout linear = dialog.findViewById(R.id.LinearLayout);
        Map<String, String> requirementsMap = new HashMap<>();

        for (int i = 0; i < linear.getChildCount(); i++) {
            View childView = linear.getChildAt(i);

            if (childView instanceof EditText) {
                EditText editText = (EditText) childView;
                if (!editText.getHint().toString().toLowerCase().equals("name")) {
                    String requirementKey = editText.getHint().toString().toLowerCase();
                    String requirementValue = editText.getText().toString();

                    // Check if the requirement is "age" and validate if it can be converted to an integer
                    if (requirementKey.equals("age")) {
                        try {
                            int ageValue = Integer.parseInt(requirementValue);
                            requirementsMap.put(requirementKey, String.valueOf(ageValue));
                        } catch (NumberFormatException e) {
                            // Show a Toast message if the string cannot be converted to an integer
                            Toast.makeText(dialog.getContext(), "Please enter a valid age.", Toast.LENGTH_SHORT).show();
                            return null; // Return null to indicate an error
                        }
                    } else {
                        requirementsMap.put(requirementKey, requirementValue);
                    }
                }
            }
        }
        System.out.println(requirementsMap);
        return requirementsMap;
    }


    private void createEditTextFields(Dialog dialog, DataSnapshot requirementsSnapshot) {
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

        for (DataSnapshot requirement : requirementsSnapshot.getChildren()) {
            String requirementValue = requirement.getValue(String.class);
            EditText editText = new EditText(dialog.getContext());
            editText.setLayoutParams(new LinearLayout.LayoutParams(
                    editTextWidthPixels,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            editText.setHint(requirementValue);
            editText.setTextColor(dialog.getContext().getResources().getColor(R.color.black));
            editText.setTextSize(20);
            linearLayout.addView(editText, index);
            index++;
        }
    }

    private void TextFieldValidation(Dialog dialog) {
        LinearLayout linear = dialog.findViewById(R.id.LinearLayout);
        for (int i = 0; i < linear.getChildCount(); i++) {
            View childView = linear.getChildAt(i);

            if (childView instanceof EditText) {
                EditText editText = (EditText) childView;
                if (editText.getText().toString().trim().isEmpty()) {
                    isValidationSuccessful = false;
                    return;
                }
            }
        }
        isValidationSuccessful = true;
    }
    private void createTextField(Dialog dialog, String text) {
        int editTextWidthSP = 300;

        int editTextWidthPixels = (int) (editTextWidthSP * dialog.getContext().getResources().getDisplayMetrics().scaledDensity);

        LinearLayout linearLayout = dialog.findViewById(R.id.LinearLayout);

        TextView descriptionEditText = new TextView(dialog.getContext());
        descriptionEditText.setLayoutParams(new LinearLayout.LayoutParams(
                editTextWidthPixels,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        descriptionEditText.setText(text);
        descriptionEditText.setTextColor(dialog.getContext().getResources().getColor(R.color.black));
        descriptionEditText.setTextSize(16);
        descriptionEditText.setEnabled(false);
        linearLayout.addView(descriptionEditText,1);
    }
}
