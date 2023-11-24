package com.uotttawa.lschu105.gcccyclingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AccountManagement extends AppCompatActivity {
    ListView listViewUsers;
    List<String> users;
    DatabaseReference databaseUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_management);

        listViewUsers = findViewById(R.id.listViewUsers);

        databaseUsers = FirebaseDatabase.getInstance().getReference("Accounts");

        users = new ArrayList<>();

        listViewUsers.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                String user = users.get(i);
                showUpdateDeleteDialog(user);
                return true;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        databaseUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    users.add(postSnapshot.getKey());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(AccountManagement.this, android.R.layout.simple_list_item_1, users);
                listViewUsers.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    // Method to show a dialog for deleting a user
    private void showUpdateDeleteDialog(final String userName) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.activity_delete_user, null);
        dialogBuilder.setView(dialogView);

        final Button buttonDelete = dialogView.findViewById(R.id.buttonCreateEventType);

        dialogBuilder.setTitle(userName);
        final AlertDialog b = dialogBuilder.create();
        b.show();

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteUserData(userName);
                b.dismiss();
            }
        });
    }

    private void deleteUserData(final String userName) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Accounts").child(userName);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Delete events associated with the user
                    deleteEventsForUser(userName);

                    // Delete the user
                    dataSnapshot.getRef().removeValue();

                    Toast.makeText(getApplicationContext(), "User and associated events deleted", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "User Not Found", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void deleteEventsForUser(final String userName) {
        DatabaseReference userEventsRef = FirebaseDatabase.getInstance().getReference().child("Accounts").child(userName).child("Events");

        userEventsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                    // Get the name of the event owned by the user
                    String eventName = eventSnapshot.getKey();

                    // Delete the main event node in "Events"
                    DatabaseReference eventsRef = FirebaseDatabase.getInstance().getReference().child("Events").child(eventName);
                    eventsRef.removeValue();

                    Toast.makeText(getApplicationContext(), "Event '" + eventName + "' deleted", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void onBackPressed() {
        Intent intent = new Intent(this, WelcomePage.class);
        startActivity(intent);
    }
}
