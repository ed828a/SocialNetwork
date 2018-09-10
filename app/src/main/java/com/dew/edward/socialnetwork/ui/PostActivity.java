package com.dew.edward.socialnetwork.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.dew.edward.socialnetwork.MainActivity;
import com.dew.edward.socialnetwork.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public class PostActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ProgressDialog loadingBar;
    private ImageButton selectPostImage;
    private Button updatePostButton;
    private EditText postDescription;
    private Uri imageUri;
    private String description;

    private String saveCurrentDate, saveCurrentTime, postRandomName;
    private String downloadUrl;


    private StorageReference postImageReference;
    private DatabaseReference usersRef, postsRef;
    private FirebaseAuth mAuth;
    private String currentUserId;

    private long countPosts = 0;

    private static int Gallery_Pick = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        postImageReference = FirebaseStorage.getInstance().getReference();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        mToolbar = (Toolbar) findViewById(R.id.update_post_page_toolbar);
        selectPostImage = (ImageButton) findViewById(R.id.select_post_image);
        updatePostButton = (Button) findViewById(R.id.update_post_button);
        postDescription = (EditText) findViewById(R.id.post_description);
        loadingBar = new ProgressDialog(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Update Post");

        selectPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });
        updatePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validatePostInfo();
            }
        });

    }

    private void validatePostInfo() {
        description = postDescription.getText().toString();

        if (imageUri == null) {
            Toast.makeText(this, "Please select your post image...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(description)) {
            Toast.makeText(this, "Please write something about your image...", Toast.LENGTH_SHORT).show();
        } else {
            loadingBar.setTitle("Add New Post");
            loadingBar.setMessage("please wait, while we are adding new post...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);
            storingImageToFirebaseStorage();
        }
    }

    private void storingImageToFirebaseStorage() {
        Calendar calFordDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-mm-yyyy");
        saveCurrentDate = currentDate.format(calFordDate.getTime());
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(calFordDate.getTime());
        postRandomName = saveCurrentDate + saveCurrentTime;

        final StorageReference filePath = postImageReference
                .child("post_images")
                .child(imageUri.getLastPathSegment() + postRandomName + ".jpg");

        // save the image in FirebaseStorage.
        filePath.putFile(imageUri)
                .continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        } else {
                            Toast.makeText(PostActivity.this,
                                    "Profile Image successfully stored.", Toast.LENGTH_SHORT).show();
                            return filePath.getDownloadUrl();
                        }
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            downloadUrl = task.getResult().toString();
                            savePostInformationToDatabase();

                        } else {
                            loadingBar.dismiss();
                            Toast.makeText(PostActivity.this,
                                    "Error: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

    private void savePostInformationToDatabase() {
        postsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    countPosts = dataSnapshot.getChildrenCount();
                } else {
                    countPosts = 0;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        usersRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String userFullName = dataSnapshot.child("fullname").getValue().toString();
                    String userProfileImage = dataSnapshot.child("profile_image").getValue().toString();

                    HashMap<String, Object> postsMap = new HashMap();
                    postsMap.put("uid", currentUserId);
                    postsMap.put("date", saveCurrentDate);
                    postsMap.put("time", saveCurrentTime);
                    postsMap.put("description", description);
                    postsMap.put("post_image", downloadUrl);
                    postsMap.put("profile_image", userProfileImage);
                    postsMap.put("full_name", userFullName);
                    postsMap.put("index", countPosts); // using countPosts as an index, which is not a good idea.

                    postsRef.child(currentUserId + postRandomName)
                            .updateChildren((Map<String, Object>) postsMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    loadingBar.dismiss();
                                    if (task.isSuccessful()) {
                                        sendUserToMainActivity();
                                        Toast.makeText(PostActivity.this,
                                                "New post is updated successfully.", Toast.LENGTH_SHORT).show();
                                    } else {

                                        Toast.makeText(PostActivity.this,
                                                "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void openGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, Gallery_Pick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            selectPostImage.setImageURI(imageUri);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                sendUserToMainActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(PostActivity.this, MainActivity.class);
        startActivity(mainIntent);
    }
}
