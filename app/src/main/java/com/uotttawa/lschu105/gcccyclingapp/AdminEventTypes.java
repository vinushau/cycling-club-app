package com.uotttawa.lschu105.gcccyclingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class AdminEventTypes extends AppCompatActivity {
    ListView listViewEventTypes;
    List<String> eventTypes;
    DatabaseReference databaseProducts;
    private LinearLayout updateDialogLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_event_types);

        listViewEventTypes = findViewById(R.id.listViewEventTypes);
        databaseProducts = FirebaseDatabase.getInstance().getReference("EventTypes");
        eventTypes = new ArrayList<>();

        listViewEventTypes.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                String eventType = eventTypes.get(i);
                showUpdateDeleteDialog(eventType);
                return true;
            }
        });

        Button buttonEventType = findViewById(R.id.buttonEventType);
        buttonEventType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddEventTypeDialog();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        databaseProducts.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                eventTypes.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    eventTypes.add(postSnapshot.getKey());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(AdminEventTypes.this, android.R.layout.simple_list_item_1, eventTypes);
                listViewEventTypes.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    // Method to show the update dialog for a selected event type
    private void showUpdateDeleteDialog(String eventType) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.activity_update_dialog, null);
        dialogBuilder.setView(dialogView);

        updateDialogLayout = dialogView.findViewById(R.id.edit_dialog_layout);

        int[] editTextCounter = {0};
        final EditText editTextName = dialogView.findViewById(R.id.editTextName);
        final EditText editTextDisplay = dialogView.findViewById(R.id.editTextDisplay);
        final EditText editTextDesc = dialogView.findViewById(R.id.editTextDesc);
        Button buttonEditName = dialogView.findViewById(R.id.buttonEditName);
        Button buttonEditDisplay = dialogView.findViewById(R.id.buttonEditDisplay);
        Button buttonEditDesc = dialogView.findViewById(R.id.buttonEditDesc);

        buttonEditName.setOnClickListener(v -> {
            editTextName.setFocusableInTouchMode(true);
            editTextName.setClickable(true);
            editTextName.setCursorVisible(true);
            if (buttonEditName.getText() == "Clear"){
                editTextName.setText("");
            }
            buttonEditName.setText("Clear");
        });

        buttonEditDisplay.setOnClickListener(v -> {
            editTextDisplay.setFocusableInTouchMode(true);
            editTextDisplay.setClickable(true);
            editTextDisplay.setCursorVisible(true);
            if (buttonEditDisplay.getText() == "Clear"){
                editTextDisplay.setText("");
            }
            buttonEditDisplay.setText("Clear");
        });

        buttonEditDesc.setOnClickListener(v -> {
            editTextDesc.setFocusableInTouchMode(true);
            editTextDesc.setClickable(true);
            editTextDesc.setCursorVisible(true);
            if (buttonEditDesc.getText() == "Clear"){
                editTextDesc.setText("");
            }
            buttonEditDesc.setText("Clear");
        });

        editTextName.setText(eventType);
        DatabaseReference eventTypeRef = databaseProducts.child(eventType);
        eventTypeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String eventName = dataSnapshot.child("name").getValue(String.class);
                String eventDesc = dataSnapshot.child("description").getValue(String.class);

                editTextDisplay.setText(eventName);
                editTextDesc.setText(eventDesc);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the error if needed
            }
        });

        getRequirementsForEventType(eventType, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot requirementSnapshot : dataSnapshot.getChildren()) {
                        String requirementValue = requirementSnapshot.getValue(String.class);

                        LinearLayout linearLayout = new LinearLayout(getApplicationContext());
                        linearLayout.setOrientation(LinearLayout.HORIZONTAL);

                        EditText editTextRequirement = new EditText(getApplicationContext());
                        editTextRequirement.setText(requirementValue);
                        editTextRequirement.setId(editTextCounter[0]++);

                        editTextRequirement.setFocusable(false);
                        editTextRequirement.setClickable(false);
                        editTextRequirement.setCursorVisible(false);

                        LinearLayout.LayoutParams editTextParams = new LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                1.0f
                        );
                        editTextRequirement.setLayoutParams(editTextParams);

                        Button editButton = new Button(getApplicationContext());
                        editButton.setText("Edit");

                        editButton.setOnClickListener(v -> {
                            editTextRequirement.setFocusableInTouchMode(true);
                            editTextRequirement.setClickable(true);
                            editTextRequirement.setCursorVisible(true);
                            if (editButton.getText() == "Clear"){
                                editTextRequirement.setText("");
                            }
                            editButton.setText("Clear");

                        });

                        linearLayout.addView(editTextRequirement);
                        linearLayout.addView(editButton);

                        updateDialogLayout.addView(linearLayout);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        final Button buttonUpdate = dialogView.findViewById(R.id.buttonAddRequirement);
        final Button buttonDelete = dialogView.findViewById(R.id.buttonCreateEventType);

        dialogBuilder.setTitle(eventType);
        final AlertDialog b = dialogBuilder.create();
        b.show();

        buttonUpdate.setOnClickListener(view -> {
            String newName = editTextName.getText().toString().trim();
            String newDisplay = editTextDisplay.getText().toString().trim();
            String newDesc = editTextDesc.getText().toString().trim();

            if (TextUtils.isEmpty(newName)) {
                newName = eventType;
            }

            if (TextUtils.isEmpty(newDisplay) || TextUtils.isEmpty(newDesc) || !textFieldValidation()) {
                Toast.makeText(getApplicationContext(), "Please enter text in all fields", Toast.LENGTH_LONG).show();
                return;
            }

            Map<String, String> requirements = new HashMap<>();

            for (int i = 0; i < updateDialogLayout.getChildCount(); i++) {
                EditText editTextRequirement = updateDialogLayout.findViewById(i);

                if (editTextRequirement != null) {
                    String requirementText = editTextRequirement.getText().toString();
                    requirements.put("" + (i), requirementText);
                }
            }

            editEventType(eventType, newName, newDisplay, newDesc, requirements);
            b.dismiss();
        });

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteEventType(eventType);
                b.dismiss();
            }
        });
    }

    private void getRequirementsForEventType(String eventType, ValueEventListener valueEventListener) {
        DatabaseReference requirementsRef = databaseProducts.child(eventType).child("Requirements");
        requirementsRef.addListenerForSingleValueEvent(valueEventListener);
    }

    // Method to show the dialog for adding a new event type
    private void showAddEventTypeDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.activity_eventtype_dialog, null);
        dialogBuilder.setView(dialogView);

        final EditText editTextName = dialogView.findViewById(R.id.editTextName);
        final EditText editTextDisplay = dialogView.findViewById(R.id.editTextDisplay);
        final EditText editTextDesc = dialogView.findViewById(R.id.editTextDesc);
        LinearLayout requirementsLayout = dialogView.findViewById(R.id.requirementsLayout);

        Button addRequirementButton = dialogView.findViewById(R.id.buttonAddRequirement);
        Button buttonCreateEventType = dialogView.findViewById(R.id.buttonCreateEventType);

        addRequirementButton.setOnClickListener(view -> {
            EditText newEditText = new EditText(getApplicationContext());
            newEditText.setHint("Requirement");
            newEditText.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));

            requirementsLayout.addView(newEditText);
        });

        dialogBuilder.setTitle("Add Event Type");

        buttonCreateEventType.setOnClickListener(view -> {
            String newName = editTextName.getText().toString().trim();
            String newDisplay = editTextDisplay.getText().toString().trim();
            String newDesc = editTextDesc.getText().toString().trim();

            boolean allFieldsFilled = true;

            // Check if any of the requirements are empty
            for (int j = 0; j < requirementsLayout.getChildCount(); j++) {
                View childView = requirementsLayout.getChildAt(j);
                if (childView instanceof EditText) {
                    EditText editText = (EditText) childView;
                    String requirement = editText.getText().toString().trim();
                    if (TextUtils.isEmpty(requirement)) {
                        editText.setError("This field cannot be empty");
                        allFieldsFilled = false;
                    }
                }
            }

            if (TextUtils.isEmpty(newName)) {
                editTextName.setError("This field cannot be empty");
                allFieldsFilled = false;
            }

            if (TextUtils.isEmpty(newDisplay)) {
                editTextDisplay.setError("This field cannot be empty");
                allFieldsFilled = false;
            }

            if (TextUtils.isEmpty(newDesc)) {
                editTextDesc.setError("This field cannot be empty");
                allFieldsFilled = false;
            }

            if (allFieldsFilled) {
                Map<String, String> requirements = new HashMap<>();
                for (int j = 0; j < requirementsLayout.getChildCount(); j++) {
                    View childView = requirementsLayout.getChildAt(j);
                    if (childView instanceof EditText) {
                        EditText editText = (EditText) childView;
                        String requirement = editText.getText().toString().trim();
                        requirements.put("" + (j), requirement);
                    }
                }

                createEventType(newName, newDisplay, newDesc, requirements);
            }
        });

        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    // Method to create a new event type in Firebase
    private void createEventType(String name, String display, String desc, Map<String, String> requirements) {
        DatabaseReference eventRef = databaseProducts.child(name);
        eventRef.child("name").setValue(display);
        eventRef.child("description").setValue(desc);
        eventRef.child("Requirements").setValue(requirements);
        Toast.makeText(getApplicationContext(), "Event Type Created", Toast.LENGTH_SHORT).show();
    }

    // Method to delete an event type from Firebase
    private void deleteEventType(String eventName) {
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

    private boolean textFieldValidation() {
        return validateEditTexts(updateDialogLayout);
    }

    // Recursive method to validate all EditText fields in a ViewGroup
    private boolean validateEditTexts(ViewGroup viewGroup) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View childView = viewGroup.getChildAt(i);

            if (childView instanceof EditText) {
                EditText editText = (EditText) childView;
                if (editText.getText().toString().trim().isEmpty()) {
                    return false;
                }
            } else if (childView instanceof ViewGroup) {
                // Recursive call for nested ViewGroups
                if (!validateEditTexts((ViewGroup) childView)) {
                    return false;
                }
            }
        }
        return true;
    }

    // Method to edit an existing event type in Firebase
    private void editEventType(final String oldName, final String newName, final String newDisplay, final String newDesc, Map<String, String> requirements) {
        DatabaseReference dataRef = FirebaseDatabase.getInstance().getReference("EventTypes");
        dataRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(oldName)) {
                    DatabaseReference eventRef = dataRef.child(oldName);
                    Object eventData = snapshot.child(oldName).getValue();

                    dataRef.child(newName).setValue(eventData);
                    if (!oldName.equals(newName)) {
                        eventRef.removeValue();
                    }

                    DatabaseReference newEventRef = dataRef.child(newName);

                    if (!newDisplay.isEmpty()) {
                        newEventRef.child("name").setValue(newDisplay);
                    }

                    if (!newDesc.isEmpty()) {
                        newEventRef.child("description").setValue(newDesc);
                    }

                    newEventRef.child("Requirements").setValue(requirements);
                    Toast.makeText(getApplicationContext(), "Event Type Updated", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Event Type not found", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Database Error", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, WelcomePage.class);
        startActivity(intent);
    }
}
