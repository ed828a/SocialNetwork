package com.dew.edward.socialnetwork.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dew.edward.socialnetwork.MainActivity;
import com.dew.edward.socialnetwork.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class ClickPostActivity extends AppCompatActivity {
    private static final String TAG = "ClickPostActivity";

    private ImageView postImage;
    private TextView postDescription;
    private Button deletePostButton, editPostButton;

    private String postKey, description, image;
    private DatabaseReference postRef;
    private FirebaseAuth mAuth;
    private String currentUserId, postOwnerId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_post);

        postKey = getIntent().getExtras().get("PostKey").toString();
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(postKey);
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        postImage = findViewById(R.id.imageView_post);
        postDescription = findViewById(R.id.textView_post_description);
        deletePostButton = findViewById(R.id.button_delete_post);
        editPostButton = findViewById(R.id.button_edit_post);

        postRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    description = dataSnapshot.child("description").getValue().toString();
                    image = dataSnapshot.child("post_image").getValue().toString();
                    postOwnerId = dataSnapshot.child("uid").getValue().toString();

                    postDescription.setText(description);
                    Picasso.get().load(image).into(postImage);
                    if (currentUserId.equals(postOwnerId)) {
                        deletePostButton.setVisibility(View.VISIBLE);
                        editPostButton.setVisibility(View.VISIBLE);
                    } else {
                        deletePostButton.setVisibility(View.INVISIBLE);
                        editPostButton.setVisibility(View.INVISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        deletePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteCurrentPost();
            }
        });

        editPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editCurrentPost(description);
            }
        });
    }

    private void editCurrentPost(String description) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ClickPostActivity.this);
        builder.setTitle("Edit Post");


        final EditText inputField = new EditText(ClickPostActivity.this);
        inputField.setText(description);
        inputField.setTextColor(Color.WHITE);
        builder.setView(inputField);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                postRef.child("description").setValue(inputField.getText().toString());
                Toast.makeText(ClickPostActivity.this, "Post updated successfully.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        Dialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.holo_green_dark);

    }

    private void deleteCurrentPost() {
        //before remove postRef.value, you need to remove the image in Firestorage first.
        StorageReference imageStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl(image);
        imageStorageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "image in Storage has been deleted.");
                postRef.removeValue();
                sendUserToMainActivity();
                Toast.makeText(ClickPostActivity.this,
                        "Post has been deleted.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(ClickPostActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

}
