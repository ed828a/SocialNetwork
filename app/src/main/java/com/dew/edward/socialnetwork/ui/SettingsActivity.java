package com.dew.edward.socialnetwork.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;



public class SettingsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private EditText userName, userProfileName, userStatus, userCountry, userGender, userRelation, userDOB;
    private Button updateAccountSettingsButton;
    private CircleImageView userProfileImage;

    private DatabaseReference settingsUserRef;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private StorageReference userProfileImageRef;

    private ProgressDialog loadingBar;
    private static int Gallery_Pick = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mToolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Settings");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadingBar = new ProgressDialog(this);

        userName = findViewById(R.id.settings_username);
        userProfileName = findViewById(R.id.settings_profile_full_name);
        userStatus = findViewById(R.id.settings_status);
        userCountry = findViewById(R.id.settings_country);
        userGender = findViewById(R.id.settings_gender);
        userRelation = findViewById(R.id.settings_relationship_status);
        userDOB = findViewById(R.id.settings_dob);
        updateAccountSettingsButton = findViewById(R.id.update_account_settings_button);
        userProfileImage = findViewById(R.id.settings_profile_image);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        settingsUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("profile_images");

        settingsUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
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

        updateAccountSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateAccountInfo();
            }
        });

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Gallery_Pick);
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null){
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)  // launch CropImage Activity
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && data != null){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                loadingBar.setTitle("Profile Image");
                loadingBar.setMessage("please wait, while we are updating your profile image...");
                loadingBar.setCanceledOnTouchOutside(true);
                loadingBar.show();

                Uri resultUri = result.getUri();
                final StorageReference filePath = userProfileImageRef.child(currentUserId + ".jpg");
                // save the image in FirebaseStorage.
                UploadTask uploadTask = filePath.putFile(resultUri);
                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()){
                            throw task.getException();
                        } else {
                            Toast.makeText(SettingsActivity.this,
                                    "Profile Image successfully stored.", Toast.LENGTH_SHORT).show();
                            return filePath.getDownloadUrl();
                        }

                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()){
                            final String downloadUrl = task.getResult().toString();
                            settingsUserRef.child("profile_image").setValue(downloadUrl)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            loadingBar.dismiss();
                                            if (task.isSuccessful()){
                                                Toast.makeText(SettingsActivity.this,
                                                        "Profile image stored in firebase Database successfully.",
                                                        Toast.LENGTH_SHORT).show();

//                                        Intent selfIntent = new Intent(SetupActivity.this, SettingsActivity.class);
//                                        startActivity(selfIntent);
                                            } else {
                                                Toast.makeText(SettingsActivity.this,
                                                        "Error: " + task.getException().getMessage(),
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            loadingBar.dismiss();
                            Toast.makeText(SettingsActivity.this,
                                    "Error: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                Exception error = result.getError();
                Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void validateAccountInfo() {

        String currentUserName = userName.getText().toString().trim();
        String currentUserFullName = userProfileName.getText().toString().trim();
        String currentUserStatus = userStatus.getText().toString().trim();
        String currentUserCountry = userCountry.getText().toString().trim();
        String currentUserGender = userGender.getText().toString().trim();
        String currentUserRelation = userRelation.getText().toString().trim();
        String currentUserDOB = userDOB.getText().toString().trim();

        if (TextUtils.isEmpty(currentUserName)){
            Toast.makeText(this, "Please write your username...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(currentUserFullName)){
            Toast.makeText(this, "Please write your fullname...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(currentUserStatus)){
            Toast.makeText(this, "Please write your status...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(currentUserCountry)){
            Toast.makeText(this, "Please write your country...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(currentUserGender)){
            Toast.makeText(this, "Please write your gender...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(currentUserRelation)){
            Toast.makeText(this, "Please write your relationship...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(currentUserDOB)){
            Toast.makeText(this, "Please write your date of birth...", Toast.LENGTH_SHORT).show();
        } else {
            loadingBar.setTitle("Profile Image");
            loadingBar.setMessage("please wait, while we are updating your profile image...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            Map<String, Object> userMap = new HashMap();
            userMap.put("username", currentUserName);
            userMap.put("fullname", currentUserFullName);
            userMap.put("status", currentUserStatus);
            userMap.put("country", currentUserCountry);
            userMap.put("gender", currentUserGender);
            userMap.put("relationshipstatus", currentUserRelation);
            userMap.put("DOB", currentUserDOB);

            settingsUserRef.updateChildren(userMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            loadingBar.dismiss();
                            if (task.isSuccessful()){
                                Toast.makeText(SettingsActivity.this,
                                        "Account settings updated successfully.",
                                        Toast.LENGTH_SHORT).show();
                                sendUserToMainActivity();
                            } else {
                                Toast.makeText(SettingsActivity.this,
                                        "Error: " + task.getException().getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }


    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
