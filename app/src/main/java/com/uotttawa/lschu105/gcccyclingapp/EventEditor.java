package com.uotttawa.lschu105.gcccyclingapp;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.Space;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventEditor {
    private Context context;
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
    private List<Event> events;
    private boolean isValidationSuccessful = false;

    public void showDialog(Activity activity, Context context, Event event, String redirectUsername) {
        this.context = context;
        SharedPreferences preferences = activity.getSharedPreferences("loginPrefs", Context.MODE_PRIVATE);
        String userRole = preferences.getString("role", "");

        //Only allows Admins and Cycling Clubs accounts to use the event editor dialogue
        if ("Admin".equals(userRole) || "CyclingClub".equals(userRole)){

        } else {
            SharedPreferences.Editor editor = preferences.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(activity, Login.class);
            activity.startActivity(intent);
            activity.finish();
        }

        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.event_creation_dialog);

        TextView dialogTitle = dialog.findViewById(R.id.dialogTitle);
        dialogTitle.setText(event.getEventName());
        createTextField(dialog, "Created by " + event.getCreatedBy());
        Button updateButton = dialog.findViewById(R.id.updateButton);
        updateButton.setVisibility(View.VISIBLE);

        Button dialogButton = dialog.findViewById(R.id.dialogButton);
        dialogButton.setText("Delete");
        dialogButton.setBackgroundColor(Color.parseColor("#DB4D4D"));
        dialogButton.setTextColor(Color.WHITE);
        LinearLayout numberpicker = dialog.findViewById(R.id.number);
        numberpicker.setVisibility(View.VISIBLE);
        EditText TitleField = dialog.findViewById(R.id.nameField);
        TitleField.setText(event.getEventName());

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(dialog.getWindow().getAttributes());

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int dialogWidth = (int) (displayMetrics.widthPixels * 0.8f);
        int dialogHeight = WindowManager.LayoutParams.WRAP_CONTENT;

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        layoutParams.width = dialogWidth;
        layoutParams.height = dialogHeight;

        WindowManager.LayoutParams dimLayoutParams = new WindowManager.LayoutParams();
        dimLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        dimLayoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        dimLayoutParams.format = PixelFormat.TRANSLUCENT;

        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        View dimOverlay = new View(context);
        dimOverlay.setBackgroundColor(Color.argb(128, 0, 0, 0));
        windowManager.addView(dimOverlay, dimLayoutParams);
        DatabaseReference eventTypeReference = FirebaseDatabase.getInstance().getReference("Events").child(event.getEventName());
        createEditTextFields(dialog, event.getRequirements());
        dialog.getWindow().setAttributes(layoutParams);

        dialogButton.setOnClickListener(v -> {
            deleteEventFromFirebase(context, event);
            dialog.dismiss();
            windowManager.removeView(dimOverlay);
            Intent intent = new Intent(activity, activity.getClass());
            intent.putExtra("username", redirectUsername);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            activity.startActivity(intent);
            activity.finish();
        });

        dimOverlay.setOnClickListener(v -> {
            dialog.dismiss();
            windowManager.removeView(dimOverlay);
        });
        updateButton.setOnClickListener(v -> {
            // Validate the text fields
            TextFieldValidation(dialog);

            if (isValidationSuccessful && updateEvent(event, dialog)) {
                dialog.dismiss();
                windowManager.removeView(dimOverlay);
                Intent intent = new Intent(activity, activity.getClass());
                intent.putExtra("username", redirectUsername);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                activity.startActivity(intent);
                activity.finish();
            } else {
                Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.setCanceledOnTouchOutside(true);
        dialog.setOnDismissListener(dialogInterface -> {
            windowManager.removeView(dimOverlay);
            Intent intent = new Intent(activity, activity.getClass());
            intent.putExtra("username", redirectUsername);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            activity.startActivity(intent);
            activity.finish();
        });

        dialog.show();

        String[] levelOptions = {"Difficulty Level", "Beginner", "Intermediate", "Advanced", "All"};
        AdapterView levelSpinner = dialog.findViewById(R.id.levelSpinner);
        levelSpinner.setVisibility(View.VISIBLE);

        ArrayAdapter<String> adapter = new ArrayAdapter(context, android.R.layout.simple_spinner_item, levelOptions) {
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                ((TextView) view).setTextColor(context.getResources().getColor(position == 0 ? android.R.color.darker_gray : android.R.color.black));
                view.setEnabled(position != 0);
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        levelSpinner.setAdapter(adapter);
        int difficultyLevelIndex = getDifficultyLevelIndex(event.getDifficultyLevel(), levelOptions);
        levelSpinner.setSelection(difficultyLevelIndex);

        selectedNumberDisplay = dialog.findViewById(R.id.selectedNumberDisplay);
        String day = String.valueOf(event.getDay());
        selectedNumberDisplay.setText(day);
        selectedNumberDisplayMonth = dialog.findViewById(R.id.selectedNumberDisplayMonth);
        String month = String.valueOf(event.getMonth());
        selectedNumberDisplayMonth.setText(month);
        selectedNumberDisplayYear = dialog.findViewById(R.id.selectedNumberDisplayYear);
        String year = String.valueOf(event.getYear());
        selectedNumberDisplayYear.setText(year);
        numberPickerContainer = dialog.findViewById(R.id.numberPickerContainer);
        numberPickerContainerMonth = dialog.findViewById(R.id.numberPickerContainerMonth);
        numberPickerContainerYear = dialog.findViewById(R.id.numberPickerContainerYear);
        numberPicker = dialog.findViewById(R.id.numberPicker);
        numberPickerMonth = dialog.findViewById(R.id.numberPickerMonth);
        numberPickerYear = dialog.findViewById(R.id.numberPickerYear);
        container = dialog.findViewById(R.id.containerMonth);

        // Set up the NumberPicker
        setupNumberPicker();
        setupNumberPickerMonth();
        setupNumberPickerYear();
        numberPicker.setValue(event.getDay());
        numberPickerMonth.setValue(event.getMonth());
        numberPickerYear.setValue(event.getYear());
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
        descriptionEditText.setTextSize(20);
        descriptionEditText.setEnabled(false);
        linearLayout.addView(descriptionEditText, 1);
    }


    private boolean createFirebaseEntry(Event eventObject, String eventName) {
        DatabaseReference eventsReference = FirebaseDatabase.getInstance().getReference("Events");

        boolean success = true;

        DatabaseReference eventReference = eventsReference.child(eventName);
        DatabaseReference userEventsReference = FirebaseDatabase.getInstance().getReference("Accounts").child(eventObject.getCreatedBy()).child("Events").child(eventName);

        try {
            eventReference.setValue(eventObject);
            userEventsReference.setValue(eventName);
        } catch (Exception e) {
            success = false;
        }

        return success;
    }
    private void deleteEventFromFirebase(Context context, Event event) {
        String eventName = event.getEventName();
        DatabaseReference eventsReference = FirebaseDatabase.getInstance().getReference("Events").child(eventName);

        eventsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Removes event from the main event list
                    eventsReference.removeValue();

                    Toast.makeText(context, "Event deleted", Toast.LENGTH_SHORT).show();

                    // Removes event from the event list associated with the Cycling Club Accounts.
                    if (event.getCreatedBy() != null && !event.getCreatedBy().isEmpty()) {
                        DatabaseReference userEventsRef = FirebaseDatabase.getInstance().getReference().child("Accounts").child(event.getCreatedBy()).child("Events");
                        userEventsRef.child(eventName).removeValue();
                    }
                } else {
                    Toast.makeText(context, "Event not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle onCancelled event
            }
        });
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
    private void createEditTextFields(Dialog dialog, Map<String, String> requirementsMap) {
        if (requirementsMap == null){
            return;
        }
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

        for (Map.Entry<String, String> entry : requirementsMap.entrySet()) {
            String requirementKey = entry.getKey();
            String requirementValue = entry.getValue();

            // Create a TextView for the requirement key
            TextView keyTextView = new TextView(dialog.getContext());
            keyTextView.setLayoutParams(new LinearLayout.LayoutParams(
                    editTextWidthPixels,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            keyTextView.setText(requirementKey + ":");
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
            editText.setHint(requirementKey);
            editText.setTextColor(dialog.getContext().getResources().getColor(R.color.black));
            editText.setTextSize(20);
            linearLayout.addView(editText, index);
            index++;
        }
    }

    private boolean updateEvent(Event event, Dialog dialog) {
        if (getRequirements(dialog) == null) {
            return false;
        }
        Spinner spinner = dialog.findViewById(R.id.levelSpinner);
        TextView selectedNumberDisplay = dialog.findViewById(R.id.selectedNumberDisplay);
        TextView selectedNumberDisplayMonth = dialog.findViewById(R.id.selectedNumberDisplayMonth);
        TextView selectedNumberDisplayYear = dialog.findViewById(R.id.selectedNumberDisplayYear);

        String day = selectedNumberDisplay.getText().toString();
        String month = selectedNumberDisplayMonth.getText().toString();
        String year = selectedNumberDisplayYear.getText().toString();
        String dateFormatted = String.format("%s/%s/%s", day, month, year);

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");
            LocalDate date = LocalDate.parse(dateFormatted, formatter);

            // Check if the day of the month is valid for the given month and year
            if (date.getDayOfMonth() != Integer.parseInt(day)) {
                Toast.makeText(context, "Invalid date", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (DateTimeParseException e) {
            Toast.makeText(context, "Invalid date format", Toast.LENGTH_SHORT).show();
            return false;
        }

        TextFieldValidation(dialog);

        if (!isValidationSuccessful) {
            return false;
        }

        EditText eventNameEditText = dialog.findViewById(R.id.nameField);
        String eventNameField = eventNameEditText.getText().toString();

        // Check if a valid level is selected
        if (spinner.getSelectedItemPosition() == 0) {
            Toast.makeText(dialog.getContext(), "Please select a valid difficulty level", Toast.LENGTH_SHORT).show();
            return false;
        }

        spinner.setVisibility(View.VISIBLE);
        String selectedLevel = spinner.getSelectedItem().toString();

        String eventName = eventNameField;
        String difficultyLevel = selectedLevel;
        Map<String, String> requirementsMap = getRequirements(dialog);

        DatabaseReference oldEventReference = FirebaseDatabase.getInstance().getReference("Events").child(event.getEventName());
        oldEventReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // The old event node exists, delete it
                    oldEventReference.removeValue(new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                // Deletion completed successfully, now proceed with the update
                                DatabaseReference userEventsRef = FirebaseDatabase.getInstance().getReference().child("Accounts").child(event.getCreatedBy()).child("Events");
                                userEventsRef.child(event.getEventName()).removeValue(new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError error, DatabaseReference ref) {
                                        if (error == null) {
                                            // User's event removed successfully
                                            int selectedDay = numberPicker.getValue();
                                            int selectedMonth = numberPickerMonth.getValue();
                                            int selectedYear = numberPickerYear.getValue();

                                            Event eventObject = new Event(event.getCreatedBy(), difficultyLevel, event.getEventType(), eventName, requirementsMap, selectedDay, selectedMonth, selectedYear);
                                            createFirebaseEntry(eventObject, eventName);
                                        } else {
                                            // Handle error when removing the user's event
                                            Toast.makeText(dialog.getContext(), "Error updating event. Please try again.", Toast.LENGTH_SHORT).show();
                                            Log.e("UpdateEvent", "Error removing user's event", error.toException());
                                        }
                                    }
                                });
                            } else {
                                // Handle error when deleting the old event reference
                                Toast.makeText(dialog.getContext(), "Error updating event. Please try again.", Toast.LENGTH_SHORT).show();
                                Log.e("UpdateEvent", "Error deleting old event reference", databaseError.toException());
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle onCancelled event
            }
        });

        return true;
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
                            // Handle the case where the age cannot be converted to an integer
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
    private void setupNumberPickerYear() {
        numberPickerYear.setMinValue(2023);
        numberPickerYear.setMaxValue(2100);
        numberPickerYear.setValue(2023);

        numberPickerYear.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
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
    private int getDifficultyLevelIndex(String difficultyLevel, String[] levelOptions) {
        for (int i = 0; i < levelOptions.length; i++) {
            if (levelOptions[i].equalsIgnoreCase(difficultyLevel)) {
                return i;
            }
        }
        return 0;
    }
}
