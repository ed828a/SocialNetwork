package com.dew.edward.socialnetwork.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.dew.edward.socialnetwork.R;
import com.dew.edward.socialnetwork.adapter.FindFriendsAdapter;
import com.dew.edward.socialnetwork.adapter.FriendsAdapter;
import com.dew.edward.socialnetwork.model.FindFriends;
import com.dew.edward.socialnetwork.model.Friends;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class FriendsActivity extends AppCompatActivity {

    private RecyclerView friendList;

    private DatabaseReference friendsRef, usersRef;
    private FirebaseAuth mAuth;
    private String ownerUserId;

    private FriendsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        mAuth = FirebaseAuth.getInstance();
        ownerUserId = mAuth.getCurrentUser().getUid();
        friendsRef = FirebaseDatabase.getInstance().getReference().child("friends").child(ownerUserId);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        friendList = findViewById(R.id.friends_list);
        friendList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        friendList.setLayoutManager(linearLayoutManager);

        Query friendsQuery = friendsRef;

        FirebaseRecyclerOptions<Friends> options = new FirebaseRecyclerOptions.Builder<Friends>()
                .setQuery(friendsQuery, Friends.class)
                .build();
        adapter = new FriendsAdapter(options);
        friendList.setAdapter(adapter);

    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
