package com.dew.edward.socialnetwork.ui;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import com.dew.edward.socialnetwork.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonalProfileActivity extends AppCompatActivity {
    private static final String TAG = "PersonalProfileActivity";

    private CircleImageView personalProfileImage;
    private TextView personalProfileFullname, personalProfileUsername, personalProfileStatus;
    private TextView personalProfileCountry, personalProfileDOB, personalProfileGender;
    private TextView personalProfileRelationship;
    private Button personalSendFriendRequestButton, personalDeclineFriendRequestButton;
    private Toolbar mToolbar;

    private DatabaseReference userProfilesRef, usersRef, friendRequestRef, friendsRef;
    private FirebaseAuth mAuth;
    private String receiverUserId;  // receiver user id
    private String senderUserId; // sender user id
    private String currentFriendshipState = "not_friend";
    private String saveCurrentDate;
    ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_profile);

        mToolbar = findViewById(R.id.personal_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("User Profile");

//        receiverUserId = getIntent().getStringExtra("ListedUserId");
        receiverUserId = getIntent().getExtras().get("ListedUserId").toString();
        mAuth = FirebaseAuth.getInstance();
        senderUserId = mAuth.getCurrentUser().getUid();
        initializeFields(senderUserId, receiverUserId);

        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("friend_request");
        friendsRef = FirebaseDatabase.getInstance().getReference().child("friends");

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        usersRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String receiverUserProfileImage = dataSnapshot.child("profile_image").getValue().toString();
                    String receiverUserName = dataSnapshot.child("username").getValue().toString();
                    String receiverUserStatus = dataSnapshot.child("status").getValue().toString();
                    String receiverUserRelation = dataSnapshot.child("relationshipstatus").getValue().toString();
                    String receiverUserGender = dataSnapshot.child("gender").getValue().toString();
                    String receiverUserFullName = dataSnapshot.child("fullname").getValue().toString();
                    String receiverUserCountry = dataSnapshot.child("country").getValue().toString();
                    String receiverUserDOB = dataSnapshot.child("DOB").getValue().toString();

                    Picasso.get().load(receiverUserProfileImage).into(personalProfileImage);
                    personalProfileUsername.setText(receiverUserName);
                    personalProfileFullname.setText(receiverUserFullName);
                    personalProfileStatus.setText(receiverUserStatus);
                    personalProfileCountry.setText(receiverUserCountry);
                    personalProfileGender.setText(receiverUserGender);
                    personalProfileRelationship.setText(receiverUserRelation);
                    personalProfileDOB.setText(receiverUserDOB);


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        maintenanceOfButtons();

    }

    private void initializeFields(final String senderUserId, final String receiverUserId) {
        personalProfileImage = findViewById(R.id.personal_profile_image);
        personalProfileFullname = findViewById(R.id.personal_profile_fullname);
        personalProfileUsername = findViewById(R.id.personal_profile_username);
        personalProfileStatus = findViewById(R.id.personal_profile_status);
        personalProfileCountry = findViewById(R.id.personal_profile_country);
        personalProfileDOB = findViewById(R.id.personal_profile_dob);
        personalProfileGender = findViewById(R.id.personal_profile_gender);
        personalProfileRelationship = findViewById(R.id.personal_profile_relationship);

        personalSendFriendRequestButton = findViewById(R.id.personal_send_friend_request_button);


        personalDeclineFriendRequestButton = findViewById(R.id.personal_decline_friend_request_button);
        personalDeclineFriendRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


            }
        });
        personalDeclineFriendRequestButton.setVisibility(View.INVISIBLE);
        personalDeclineFriendRequestButton.setEnabled(false);

        if (senderUserId.equals(receiverUserId)) {
            personalSendFriendRequestButton.setVisibility(View.GONE);
        } else {
            personalSendFriendRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "SendFriendRequestButton clicked: " + currentFriendshipState);

                    if (currentFriendshipState.equals("not_friend")) {
                        sendFriendRequestToPerson();
                    } else if (currentFriendshipState.equals("request_sent")){
                        cancelFriendRequest();
                    } else if (currentFriendshipState.equals("request_received")){
                        acceptFriendRequest();
                    } else if (currentFriendshipState.equals("friends")){
                        unfriendExistingFriend();
                    }
                }
            });
        }

    }

    private void unfriendExistingFriend() {
        friendsRef.child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            friendsRef.child(receiverUserId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                personalSendFriendRequestButton.setEnabled(true);
                                                currentFriendshipState = "not_friend";
                                                personalSendFriendRequestButton.setText("send friend request");

                                                personalDeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                personalDeclineFriendRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void acceptFriendRequest() {
        Calendar calFordDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-mm-yyyy");
        saveCurrentDate = currentDate.format(calFordDate.getTime());

        friendsRef.child(senderUserId).child(receiverUserId)
                .child("date").setValue(saveCurrentDate)
        .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    friendsRef.child(receiverUserId).child(senderUserId)
                            .child("date").setValue(saveCurrentDate)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        friendRequestRef.child(senderUserId).child(receiverUserId)
                                                .removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            friendRequestRef.child(receiverUserId).child(senderUserId)
                                                                    .removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()) {
                                                                                currentFriendshipState = "friends";
                                                                                personalSendFriendRequestButton.setText("Unfriend this person");

                                                                                personalDeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                                                personalDeclineFriendRequestButton.setEnabled(false);
                                                                            }
                                                                        }
                                                                    });
                                                        }
                                                    }
                                                });
                                    }
                                }
                            });
                }
            }
        });
    }

    private void cancelFriendRequest() {
        friendRequestRef.child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            friendRequestRef.child(receiverUserId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
//                                                personalSendFriendRequestButton.setEnabled(true);
                                                currentFriendshipState = "not_friend";
                                                personalSendFriendRequestButton.setText("send friend request");

                                                personalDeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                personalDeclineFriendRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void maintenanceOfButtons() {
        friendRequestRef.child(senderUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(receiverUserId)) {
                            String requestType = dataSnapshot.child(receiverUserId)
                                    .child("request_state").getValue().toString();

                            if (requestType.equals("sent")) {
                                currentFriendshipState = "request_sent";
                                personalSendFriendRequestButton.setText("Cancel Friend Request");

                                personalDeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                personalDeclineFriendRequestButton.setEnabled(false);
                            } else if (requestType.equals("received")){
                                currentFriendshipState = "request_received";
                                personalSendFriendRequestButton.setText("Accept Friend Request");

                                personalDeclineFriendRequestButton.setVisibility(View.VISIBLE);
                                personalDeclineFriendRequestButton.setEnabled(true);
                                personalDeclineFriendRequestButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        cancelFriendRequest();
                                    }
                                });
                            }
                        } else {
                            friendsRef.child(senderUserId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChild(receiverUserId)){
                                                currentFriendshipState = "friends";
                                                personalSendFriendRequestButton.setText("Unfriend this person");
                                                personalDeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                personalDeclineFriendRequestButton.setEnabled(false);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void sendFriendRequestToPerson() {
        Log.d(TAG, "sendFriendRequestToPerson called:");
        friendRequestRef.child(senderUserId).child(receiverUserId)
                .child("request_state").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            friendRequestRef.child(receiverUserId).child(senderUserId)
                                    .child("request_state").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                personalSendFriendRequestButton.setEnabled(true);
                                                currentFriendshipState = "request_sent";
                                                personalSendFriendRequestButton.setText("Cancel friend request");

                                                personalDeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                personalDeclineFriendRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }
}
