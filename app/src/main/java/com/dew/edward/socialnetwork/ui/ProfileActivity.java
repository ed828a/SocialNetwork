package com.dew.edward.socialnetwork.ui;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.dew.edward.socialnetwork.MainActivity;
import com.dew.edward.socialnetwork.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private TextView userName, userProfileName, userStatus, userCountry, userGender, userRelation, userDOB;
    private CircleImageView userProfileImage;

    private DatabaseReference profileUserRef, friendsRef, postsRef;
    private FirebaseAuth mAuth;
    private String currentUserId;

    private Button numberOfPosts, numberOfFriends;
    private int countFriends = 0, countPosts = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        profileUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        friendsRef = FirebaseDatabase.getInstance().getReference().child("friends");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        userName = findViewById(R.id.profile_username);
        userProfileName = findViewById(R.id.profile_fullname);
        userStatus = findViewById(R.id.profile_status);
        userCountry = findViewById(R.id.profile_country);
        userGender = findViewById(R.id.profile_gender);
        userRelation = findViewById(R.id.profile_relationship);
        userDOB = findViewById(R.id.profile_dob);
        userProfileImage = findViewById(R.id.profile_image_picture);
        numberOfFriends = findViewById(R.id.number_of_friends_button);
        numberOfPosts = findViewById(R.id.number_of_post_button);

        numberOfFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUserToFriendsActivity();
            }
        });

        numberOfPosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUserToMyPostActivity();
            }
        });

        postsRef.orderByChild("uid")
                .startAt(currentUserId).endAt(currentUserId + "\uf8ff")   // search for currentUser's posts
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            countPosts = (int) dataSnapshot.getChildrenCount();
                            numberOfPosts.setText(Integer.toString(countPosts) + "  Posts");
                        } else {
                            numberOfPosts.setText("No Posts yet");
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        friendsRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) { // user has friends
                    countFriends = (int) dataSnapshot.getChildrenCount();
                    numberOfFriends.setText(Integer.toString(countFriends) + "  Friends");
                } else { // user has no friend.
                    numberOfFriends.setText("No Friends yet");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        profileUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String currentUserProfileImage = dataSnapshot.child("profile_image").getValue().toString();
                    String currentUserName = dataSnapshot.child("username").getValue().toString();
                    String currentUserStatus = dataSnapshot.child("status").getValue().toString();
                    String currentUserRelation = dataSnapshot.child("relationshipstatus").getValue().toString();
                    String currentUserGender = dataSnapshot.child("gender").getValue().toString();
                    String currentUserFullName = dataSnapshot.child("fullname").getValue().toString();
                    String currentUserCountry = dataSnapshot.child("country").getValue().toString();
                    String currentUserDOB = dataSnapshot.child("DOB").getValue().toString();

                    Picasso.get().load(currentUserProfileImage).into(userProfileImage);
                    userName.setText(currentUserName);
                    userProfileName.setText(currentUserFullName);
                    userStatus.setText(currentUserStatus);
                    userCountry.setText(currentUserCountry);
                    userGender.setText(currentUserGender);
                    userRelation.setText(currentUserRelation);
                    userDOB.setText(currentUserDOB);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendUserToFriendsActivity() {
        Intent friendsIntent = new Intent(ProfileActivity.this, FriendsActivity.class);
        startActivity(friendsIntent);
    }

    private void sendUserToMyPostActivity() {
        Intent addNewPostIntent = new Intent(ProfileActivity.this, MyPostActivity.class);
        startActivity(addNewPostIntent);
    }
}
