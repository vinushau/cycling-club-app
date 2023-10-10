package com.uotttawa.lschu105.gcccyclingapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class WelcomePage extends AppCompatActivity {

    FirebaseAuth auth;
    Button button;
    TextView textView;
    TextView roles; // TextView for displaying roles

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_page);

        // Reference to the Firebase Realtime Database
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        auth = FirebaseAuth.getInstance();
        button = findViewById(R.id.logout);
        textView = findViewById(R.id.user_details);

        FirebaseUser user = auth.getCurrentUser();

        if (user == null){
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        }
        else{
            String username = user.getDisplayName();
            String email = user.getEmail();
            DatabaseReference accountTypeReference = database.child(username);

            accountTypeReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String accountTypeValue = dataSnapshot.getValue(String.class);
                    System.out.println(username + ": " + accountTypeValue);

                    if (username != null && !username.isEmpty()) {
                        textView.setText("Welcome " +username + ". You are logged in as a " + accountTypeValue.toLowerCase() + " account.");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    System.err.println("Error: " + databaseError.getMessage());
                }
            });
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });
    }
}