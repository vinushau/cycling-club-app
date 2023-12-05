package com.uotttawa.lschu105.gcccyclingapp;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.junit.Test;

public class FeedbackTest {

    @Test
    public void testInvalidRating() {
        ClubFeedback clubFeedback = new ClubFeedback();
        Profile profile = new Profile();
        profile.setUsername("greenpainting");

        // Assuming the feedback description is empty
        clubFeedback.addReviewToFirebase(profile.getUsername(), "currentUser", 0, "scribbles");
        DatabaseReference reviewsRef = FirebaseDatabase.getInstance().getReference().child("Reviews")
                .child("cyclingClubName").child(profile.getUsername());

        reviewsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    System.out.println("fail");
                }
                else{
                    System.out.println("pass");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Test
    public void testValidRating() {
        ClubFeedback clubFeedback = new ClubFeedback();
        Profile profile = new Profile();
        profile.setUsername("greenpainting");

        // Assuming the feedback description is empty
        clubFeedback.addReviewToFirebase(profile.getUsername(), "currentUser", 4, "scribbles");
        DatabaseReference reviewsRef = FirebaseDatabase.getInstance().getReference().child("Reviews")
                .child("cyclingClubName").child(profile.getUsername());

        reviewsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    System.out.println("pass");
                }
                else{
                    System.out.println("fail");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Test
    public void testInvalidDescription() {
        ClubFeedback clubFeedback = new ClubFeedback();
        Profile profile = new Profile();
        profile.setUsername("greenpainting");

        // Assuming the feedback description is empty
        clubFeedback.addReviewToFirebase(profile.getUsername(), "currentUser", 4, "");
        DatabaseReference reviewsRef = FirebaseDatabase.getInstance().getReference().child("Reviews")
                .child("cyclingClubName").child(profile.getUsername());

        reviewsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    System.out.println("fail");
                }
                else{
                    System.out.println("pass");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Test
    public void testValidDescription() {
        ClubFeedback clubFeedback = new ClubFeedback();
        Profile profile = new Profile();
        profile.setUsername("greenpainting");

        // Assuming the feedback description is empty
        clubFeedback.addReviewToFirebase(profile.getUsername(), "currentUser", 4, "good job");
        DatabaseReference reviewsRef = FirebaseDatabase.getInstance().getReference().child("Reviews")
                .child("cyclingClubName").child(profile.getUsername());

        reviewsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    System.out.println("pass");
                }
                else{
                    System.out.println("fail");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
