package com.uotttawa.lschu105.gcccyclingapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {

    EditText editTextEmail, editTextPassword;
    Button buttonLogin;
    TextView onLogin;
    private boolean isPasswordVisible = false;

    ProgressBar progressBar;

    @Override
    public void onStart() {
        super.onStart();
        // Check if the user is already logged in using SharedPreferences
        SharedPreferences preferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        String savedEmail = preferences.getString("email", "");

        if (!TextUtils.isEmpty(savedEmail)) {
            // User is already logged in, proceed to WelcomePage
            Intent intent = new Intent(getApplicationContext(), WelcomePage.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextEmail = findViewById(R.id.emailField);
        editTextPassword = findViewById(R.id.passwordField);
        buttonLogin = findViewById(R.id.registerButton);
        onLogin = findViewById(R.id.registerNow);
        progressBar = findViewById(R.id.progressBar);
        onLogin.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), AccountSelection.class);
            startActivity(intent);
            finish();
        });
        buttonLogin.setOnClickListener(view -> {
            progressBar.setVisibility(View.VISIBLE);
            String email, password;
            email = String.valueOf(editTextEmail.getText()).replace(".","_dot_");
            password = (editTextPassword.getText().toString());

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(Login.this, "Enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            DatabaseReference database = FirebaseDatabase.getInstance().getReference();
            DatabaseReference accountsReference = database.child("Accounts");

            accountsReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot accountsSnapshot) {
                    boolean userFound = false;

                    for (DataSnapshot userSnapshot : accountsSnapshot.getChildren()) {
                        String storedEmail = userSnapshot.child("email").getValue(String.class);

                        if (email.equals(storedEmail)) {
                            userFound = true;
                            String storedUsername = userSnapshot.child("username").getValue(String.class);
                            String storedPassword = userSnapshot.child("password").getValue(String.class);
                            String storedRole = userSnapshot.child("role").getValue(String.class);
                            if (password.equals(storedPassword)) {
                                Toast.makeText(getApplicationContext(), "Login Successful", Toast.LENGTH_SHORT).show();

                                // Save login information for automatic login to SharedPreferences
                                SharedPreferences preferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString("email", storedEmail);
                                editor.putString("username", storedUsername);
                                editor.putString("password", storedPassword);
                                editor.putString("role", storedRole);

                                editor.apply();

                                Intent intent = new Intent(getApplicationContext(), WelcomePage.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(getApplicationContext(), "Incorrect password", Toast.LENGTH_SHORT).show();
                            }

                            break;
                        }
                    }

                    if (!userFound) {
                        Toast.makeText(getApplicationContext(), "User not found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    progressBar.setVisibility(View.GONE);
                    System.err.println("Error: " + databaseError.getMessage());
                }
            });
        });
    }

    public void togglePasswordVisibility(View view) {
        EditText passwordField = findViewById(R.id.passwordField);

        isPasswordVisible = !isPasswordVisible;

        if (isPasswordVisible) {
            passwordField.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {
            passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }

        passwordField.setSelection(passwordField.getText().length());
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void onClickBackButton(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
