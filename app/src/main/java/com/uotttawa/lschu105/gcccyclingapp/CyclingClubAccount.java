package com.uotttawa.lschu105.gcccyclingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.widget.TextView;

public class CyclingClubAccount extends AppCompatActivity {
    private FirebaseAuth auth;
    TextView textView;

    // Validating fields
    boolean invalid = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cycling_club_account);

        auth = FirebaseAuth.getInstance();

        textView = findViewById(R.id.loginNow);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });
    }

    public void onRegisterCyclingClubAccount(View view) {
        EditText username = (EditText) findViewById(R.id.usernameField);
        EditText email = (EditText) findViewById(R.id.emailField);
        EditText password = (EditText) findViewById(R.id.passwordField);

        String str_username, str_email, str_password;
        str_username = username.getText().toString();
        str_email = email.getText().toString();
        str_password = password.getText().toString();

        // Check username length and spaces
        if (str_username.length() <= 3) {
            Toast.makeText(CyclingClubAccount.this, "Username needs to have at least 4 characters.", Toast.LENGTH_LONG).show();
        } else if (str_username.contains(" ")) {
            Toast.makeText(CyclingClubAccount.this, "Username cannot contain spaces.", Toast.LENGTH_LONG).show();
        } else if (str_password.length() < 6) {
            Toast.makeText(CyclingClubAccount.this, "Password must be at least 6 characters long.", Toast.LENGTH_LONG).show();
        } else if (!str_password.matches(".*[0-9].*")) {
            Toast.makeText(CyclingClubAccount.this, "Password must contain at least one number (0-9).", Toast.LENGTH_LONG).show();
        } else if (str_password.contains(" ")) {
            Toast.makeText(CyclingClubAccount.this, "Password cannot contain spaces.", Toast.LENGTH_LONG).show();
        } else {
            // Proceed to check if the username is taken
            DatabaseReference database = FirebaseDatabase.getInstance().getReference();
            DatabaseReference accountTypeReference = database.child(str_username);

            accountTypeReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Toast.makeText(CyclingClubAccount.this, "Username is already taken.", Toast.LENGTH_LONG).show();
                    } else {
                        // If the username is not taken, proceed with registration
                        registerAccount(str_username, str_email, str_password);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    System.err.println("Error: " + databaseError.getMessage());
                }
            });
        }
    }


    public void registerAccount(String username, String email, String password) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(CyclingClubAccount.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(CyclingClubAccount.this, "Your account is successfully registered", Toast.LENGTH_SHORT).show();

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(username).build();

                    user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(CyclingClubAccount.this, "Username added", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference("Users").child(username); // Update the reference to the 'Users' node

                    // Set the user's account type and registered events
                    myRef.child("accounttype").setValue("Cycling Club"); // Set the account type
                    // You can set registered events here as well if needed

                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(CyclingClubAccount.this, "Email is invalid", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
