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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ParticipantAccount extends AppCompatActivity {
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_participant_account);

        auth = FirebaseAuth.getInstance();
    }

    public void onRegisterParticipant(View view) {
        EditText username = (EditText)findViewById(R.id.usernameField);
        EditText email = (EditText)findViewById(R.id.emailField);
        EditText password = (EditText)findViewById(R.id.passwordField);

        String str_username, str_email, str_password;
        str_username = username.getText().toString();
        str_email = email.getText().toString();
        str_password = password.getText().toString();

        if (str_username.length() == 0 || str_email.length() == 0 || str_password.length() == 0) {
            Toast.makeText(ParticipantAccount.this, "Cannot leave empty field", Toast.LENGTH_SHORT).show();
        }
        else {
            registerAccount(str_username, str_email, str_password);
        }
    }

    public void registerAccount(String username, String email, String password) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(ParticipantAccount.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(ParticipantAccount.this, "Your account is successfully registered", Toast.LENGTH_SHORT).show();

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(username).build();
                    user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(ParticipantAccount.this, "Username added", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference(username);
                    myRef.setValue("Participant");

                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);

                    startActivity(intent);
                }
                else {
                    Toast.makeText(ParticipantAccount.this, "Registration failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}