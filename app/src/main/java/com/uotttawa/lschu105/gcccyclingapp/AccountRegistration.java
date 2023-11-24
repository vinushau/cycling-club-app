package com.uotttawa.lschu105.gcccyclingapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AccountRegistration extends AppCompatActivity {

    private TextView textView;
    private boolean isPasswordVisible = false;
    private Account account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_registration);

        textView = findViewById(R.id.loginNow);
        textView.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        });

        account = new Account();

        EditText usernameField = findViewById(R.id.usernameField);
        EditText emailField = findViewById(R.id.emailField);

        usernameField.addTextChangedListener(createTextWatcher(usernameField));
        emailField.addTextChangedListener(createTextWatcher(emailField));
    }

    private TextWatcher createTextWatcher(final EditText editText) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                EditText usernameField = findViewById(R.id.usernameField);
                EditText emailField = findViewById(R.id.emailField);
                String enteredUsername = usernameField.getText().toString();
                String enteredEmail = emailField.getText().toString();

                account.checkUsernameAvailability(AccountRegistration.this, enteredUsername, new Account.AvailabilityCallback() {
                    @Override
                    public void onResult(boolean isAvailable) {
                        if (isAvailable) {
                        } else {
                            usernameField.setError("Username is not available");
                        }
                    }
                });

                account.checkEmailAvailability(AccountRegistration.this, enteredEmail, new Account.AvailabilityCallback() {
                    @Override
                    public void onResult(boolean isAvailable) {
                        if (isAvailable) {
                        } else {
                            emailField.setError("Email is not available");
                        }
                    }
                });
            }
        };
    }

    public void onRegisterAdmin(View view) {
        Intent intent = getIntent();
        String role = intent.getStringExtra("role");
        EditText usernameField = findViewById(R.id.usernameField);
        EditText emailField = findViewById(R.id.emailField);
        EditText passwordField = findViewById(R.id.passwordField);

        String str_username = usernameField.getText().toString();
        String str_email = emailField.getText().toString();
        String str_password = passwordField.getText().toString();

        // Validate username and password
        if (!account.isUsernameValid(usernameField, str_username) || !account.isPasswordValid(passwordField, str_password) || !account.isEmailValid(emailField, str_email)) {
            return;  // Stop registration if validation fails
        }
        SharedPreferences preferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("email", str_email);
        editor.putString("username", str_username);
        editor.putString("password", str_password);
        editor.putString("role", role);
        editor.apply();

        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference accountTypeReference = database.child("Accounts").child(str_username);

        accountTypeReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Toast.makeText(AccountRegistration.this, "Username is already taken.", Toast.LENGTH_LONG).show();
                } else {
                    String email = str_email.toString().replace(".", "_dot_");
                    Account newAccount = new Account(str_username, role, email, str_password);
                    accountTypeReference.setValue(newAccount);

                    saveLoginInfo(str_username,email, str_password);
                    Intent intent = new Intent(getApplicationContext(), WelcomePage.class);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.err.println("Error: " + databaseError.getMessage());
            }
        });
    }

    private void saveLoginInfo(String username,String email, String password) {
        // Save login information
        SharedPreferences preferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("username", username);
        editor.putString("email", email);
        editor.putString("password", password);

        editor.apply();
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
        Intent intent = new Intent(this, AccountSelection.class);
        startActivity(intent);
        finish();
    }

    public void onBackClick(View view){
        Intent intent = new Intent(this, AccountSelection.class);
        startActivity(intent);
        finish();
    }
}
