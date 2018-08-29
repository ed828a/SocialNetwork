package com.dew.edward.socialnetwork.ui;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.dew.edward.socialnetwork.R;
import com.dew.edward.socialnetwork.adapter.CommentAdapter;
import com.dew.edward.socialnetwork.model.Comment;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class CommentsActivity extends AppCompatActivity {

    private RecyclerView commentList;
    private EditText commentsInput;
    private ImageButton commentPostButton;

    private DatabaseReference usersRef;

    private String postKey;
    private String currentUserId;

    private CommentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        postKey = getIntent().getExtras().get("PostKey").toString();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        commentsInput = findViewById(R.id.comments_input);
        commentPostButton = findViewById(R.id.comments_post_button);
        commentList = findViewById(R.id.comments_list);
        commentList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        commentList.setLayoutManager(linearLayoutManager);

        Query commentsQuery = FirebaseDatabase.getInstance()
                .getReference().child("Posts").child(postKey)
                .child("comments");
        FirebaseRecyclerOptions<Comment> options =
                new FirebaseRecyclerOptions.Builder<Comment>()
                        .setQuery(commentsQuery, Comment.class)
                        .build();
        adapter = new CommentAdapter(options);
        commentList.setAdapter(adapter);

        commentPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                usersRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String userName = dataSnapshot.child("username").getValue().toString();
                            validateComment(userName);
                            commentsInput.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

    }

    private void validateComment(String userName) {
        String commentText = commentsInput.getText().toString().trim();
        if (TextUtils.isEmpty(commentText)) {
            Toast.makeText(this, "please write text to comment...", Toast.LENGTH_SHORT).show();
        } else {
            Calendar calFordDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-mm-yyyy");
            String saveCurrentDate = currentDate.format(calFordDate.getTime());
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
            String saveCurrentTime = currentTime.format(calFordDate.getTime());

            final String randomKey = currentUserId + saveCurrentDate + saveCurrentTime;
            Map<String, Object> commentsMap = new HashMap<>();
            commentsMap.put("uid", currentUserId);
            commentsMap.put("comment", commentText);
            commentsMap.put("date", saveCurrentDate);
            commentsMap.put("time", saveCurrentTime);
            commentsMap.put("username", userName);

            DatabaseReference commentsRef = FirebaseDatabase.getInstance()
                    .getReference().child("Posts").child(postKey)
                    .child("comments").child(randomKey);
            commentsRef.updateChildren(commentsMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(CommentsActivity.this,
                                "comment saved successfully...",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(CommentsActivity.this,
                                "Error:" + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
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
