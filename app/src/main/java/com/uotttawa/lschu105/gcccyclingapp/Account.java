package com.uotttawa.lschu105.gcccyclingapp;

import android.content.Context;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Account {

    private String username;
    private String role;
    private String email;
    private String password;

    public Account() {
    }

    public Account(String username, String role, String email, String password) {
        this.role = role;
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public boolean isUsernameValid(EditText usernameField, String username) {
        if (username.length() <= 3) {
            usernameField.setError("Username needs to have at least 4 characters.");
            return false;
        } else if (username.contains(" ")) {
            usernameField.setError("Username cannot contain spaces.");
            return false;
        }
        return true;
    }
    public boolean isEmailValid(EditText emailField, String email) {
        if (email.isEmpty()) {
            emailField.setError("Email cannot be empty");
            return false;
        } else if (email.indexOf("@") == -1 || email.indexOf("@") != email.lastIndexOf("@") || email.indexOf("@") == 0 || email.indexOf("@") == email.length() - 1) {
            emailField.setError("Invalid email format");
            return false;
        } else {
            int atIndex = email.indexOf("@");
            int dotIndex = email.indexOf('.', atIndex);
            if (dotIndex == -1 || dotIndex == atIndex + 1 || dotIndex == email.length() - 1) {
                emailField.setError("Invalid email format");
                return false;
            }
        }
        return true;
    }

    public boolean isPasswordValid(EditText passwordField, String password) {
        if (password.length() < 6) {
            passwordField.setError("Password must be at least 6 characters long.");
            return false;
        } else if (!password.matches(".*[0-9].*")) {
            passwordField.setError("Password must contain at least one number (0-9).");
            return false;
        } else if (password.contains(" ")) {
            passwordField.setError("Password cannot contain spaces.");
            return false;
        }
        return true;
    }

    public void checkUsernameAvailability(Context context, String username, AvailabilityCallback callback) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        String usernames = username.replace(".", "_dot_");
        DatabaseReference accountTypeReference = database.child("Accounts").child(usernames);

        accountTypeReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean isAvailable = !dataSnapshot.exists();
                callback.onResult(isAvailable);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.err.println("Error: " + databaseError.getMessage());
                callback.onResult(false); // Handle the error case
            }
        });
    }

    public interface AvailabilityCallback {
        void onResult(boolean isAvailable);
    }

    public void checkEmailAvailability(final Context context, String email, AvailabilityCallback callback) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference accountsReference = database.child("Accounts");

        String emailFixed = email.replace(".", "_dot_");
        accountsReference.orderByChild("email").equalTo(emailFixed).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean isAvailable = !dataSnapshot.exists();
                callback.onResult(isAvailable);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.err.println("Error: " + databaseError.getMessage());
                callback.onResult(false); // Handle the error case
            }
        });
    }


}
