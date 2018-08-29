package com.dew.edward.socialnetwork.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.dew.edward.socialnetwork.R;
import com.dew.edward.socialnetwork.adapter.FindFriendsAdapter;
import com.dew.edward.socialnetwork.model.FindFriends;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class FindFriendsActivity extends AppCompatActivity {
    private static final String TAG = "FindFriendsActivity";

    private Toolbar mToolbar;
    private ImageButton searchButton;
    private EditText searchInputText;

    private RecyclerView searchResultList;

    private FindFriendsAdapter adapter;

    private DatabaseReference usersRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        mToolbar = findViewById(R.id.find_friends_appbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Find people and friends");
//        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        searchInputText = findViewById(R.id.search_box_input);
        searchButton = findViewById(R.id.search_people_friends_button);
        searchResultList = findViewById(R.id.search_result_list);

        searchResultList.setHasFixedSize(true);
        searchResultList.setLayoutManager(new LinearLayoutManager(this));

        displayAllUsers();
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(FindFriendsActivity.this, "search Button clicked.", Toast.LENGTH_SHORT).show();
                String searchBoxInput = searchInputText.getText().toString().trim();
                searchPeopleAndFriends(searchBoxInput);
            }
        });
    }

    private void searchPeopleAndFriends(String searchBoxInput) {
        Log.d(TAG, "searchPeopleAndFriends called");
        adapter.stopListening();
        Query searchPeopleAndFriendsQuery = usersRef.orderByChild("fullname")
                .startAt(searchBoxInput)
                .endAt(searchBoxInput + "\uf8ff");

        FirebaseRecyclerOptions<FindFriends> options = new FirebaseRecyclerOptions.Builder<FindFriends>()
                .setQuery(searchPeopleAndFriendsQuery, FindFriends.class)
                .build();
        adapter = new FindFriendsAdapter(options);
        searchResultList.setAdapter(adapter);

        adapter.startListening();
    }

    private void displayAllUsers() {
        Log.d(TAG, "displayAllUsers called.");

        Query query = usersRef.limitToLast(100);
        FirebaseRecyclerOptions<FindFriends> options =
                new FirebaseRecyclerOptions.Builder<FindFriends>()
                        .setQuery(query, FindFriends.class)
                        .build();

        adapter = new FindFriendsAdapter(options);
        searchResultList.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null)
            adapter.stopListening();
    }
}
