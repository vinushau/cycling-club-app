package com.uotttawa.lschu105.gcccyclingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminEventTypes extends AppCompatActivity {
    ListView listViewEventTypes;
    List<String> eventTypes;
    DatabaseReference databaseProducts;

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
    }

    @Override
    protected void onStart() {
        super.onStart();

        // attach value event listener
        databaseProducts.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // clear the previous products list
                eventTypes.clear();

                // iterate through all the nodes
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    // add product to list
                    eventTypes.add(postSnapshot.getKey());
                }

                // create adapter
                ArrayAdapter<String> adapter = new ArrayAdapter<>(AdminEventTypes.this, android.R.layout.simple_list_item_1, eventTypes);
                // attach adapter to the listview
                listViewEventTypes.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the error
            }
        });
    }

    private void showUpdateDeleteDialog(final String eventName) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.activity_update_dialog, null);
        dialogBuilder.setView(dialogView);

        final EditText editTextName = dialogView.findViewById(R.id.editTextName);
        final EditText editTextDisplay = dialogView.findViewById(R.id.editTextDisplay);
        final EditText editTextDesc = dialogView.findViewById(R.id.editTextDesc);

        final Button buttonUpdate = dialogView.findViewById(R.id.buttonUpdateProduct);
        final Button buttonDelete = dialogView.findViewById(R.id.buttonDeleteProduct);

        dialogBuilder.setTitle(eventName);
        final AlertDialog b = dialogBuilder.create();
        b.show();

        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newName = editTextName.getText().toString().trim();
                String newDisplay = editTextDisplay.getText().toString().trim();
                String newDesc = editTextDesc.getText().toString().trim();

                if (TextUtils.isEmpty(newName)) {
                    // If newName is empty, assume the old name
                    newName = eventName;
                }

                editEventType(eventName, newName, newDisplay, newDesc);
                b.dismiss();
            }
        });

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteEventType(eventName);
                b.dismiss();
            }
        });
    }

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
                // Handle errors if needed
            }
        });
    }

    private void editEventType(final String oldName, final String newName, final String newDisplay, final String newDesc) {
        DatabaseReference dataRef = FirebaseDatabase.getInstance().getReference("EventTypes");
            // Update name, create a new reference to the database, and then update displayname and description
        dataRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(oldName)) {
                    DatabaseReference eventRef = dataRef.child(oldName);
                    Object eventData = snapshot.child(oldName).getValue();

                    dataRef.child(newName).setValue(eventData);
                    if (oldName != newName) {
                        eventRef.removeValue();
                    }

                    // Update eventRef to use the new name
                    DatabaseReference newEventRef = dataRef.child(newName);

                    if (!newDisplay.isEmpty()) {
                        newEventRef.child("name").setValue(newDisplay);
                    }

                    if (!newDesc.isEmpty()) {
                        newEventRef.child("description").setValue(newDesc);
                    }

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
        Intent intent = new Intent(this, WelcomePage.class);
        startActivity(intent);
    }
}
