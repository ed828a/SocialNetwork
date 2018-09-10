package com.dew.edward.socialnetwork.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
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

public class SetupActivity extends AppCompatActivity {

    private EditText userName, fullName, countryName;
    private Button saveInformationButton;
    private CircleImageView profileImage;


    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private StorageReference userProfileImageRef;

    private String currentUserId;
    private ProgressDialog loadingBar;
    final static int Gallery_Pick = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();  // this currentUserId should get from Intent
        // because checkUserExistence() in MainActivity has this information already before calling startActivity()
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("profile_images");

        loadingBar = new ProgressDialog(this);

        userName = (EditText) findViewById(R.id.setup_user_name);
        fullName = (EditText) findViewById(R.id.setup_full_name);
        countryName = (EditText) findViewById(R.id.setup_user_country);
        saveInformationButton = (Button)findViewById(R.id.setup_save_button);
        profileImage = (CircleImageView)findViewById(R.id.setup_user_profile_image);

        saveInformationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveAccountInformation();
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Gallery_Pick);
            }
        });

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    if (dataSnapshot.hasChild("profile_image")) {
                        String image = dataSnapshot.child("profile_image")
                                .getValue().toString();
                        Picasso.get().load(image).placeholder(R.drawable.profile).into(profileImage);
                    }
                    if (dataSnapshot.hasChild("username")){
                        String username = dataSnapshot.child("username").getValue().toString();
                        userName.setText(username);
                    }

                    if (dataSnapshot.hasChild("fullname")){
                        String fullname = dataSnapshot.child("fullname").getValue().toString();
                        fullName.setText(fullname);
                    }

                    if (dataSnapshot.hasChild("country")){
                        String userCountry = dataSnapshot.child("country").getValue().toString();
                        countryName.setText(userCountry);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
         
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null){
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && data != null){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                loadingBar.setTitle("Profile Image");
                loadingBar.setMessage("please wait, while we are updating your profile image...");
                loadingBar.show();
                loadingBar.setCanceledOnTouchOutside(true);

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
                            Toast.makeText(SetupActivity.this,
                                    "Profile Image successfully stored.", Toast.LENGTH_SHORT).show();
                            return filePath.getDownloadUrl();
                        }

                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()){
                            final String downloadUrl = task.getResult().toString();
                            usersRef.child("profile_image").setValue(downloadUrl)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    loadingBar.dismiss();
                                    if (task.isSuccessful()){
                                        Toast.makeText(SetupActivity.this,
                                                "Profile image stored in firebase Database successfully.",
                                                Toast.LENGTH_SHORT).show();
                                        // todo: should using Picasso to update profile image on this activity

                                        Intent selfIntent = new Intent(SetupActivity.this, SetupActivity.class);
                                        startActivity(selfIntent);
                                    } else {
                                        Toast.makeText(SetupActivity.this,
                                                "Error: " + task.getException().getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            loadingBar.dismiss();
                            Toast.makeText(SetupActivity.this,
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

    private void saveAccountInformation() {
        String userAccountName = userName.getText().toString();
        String userfullName = fullName.getText().toString();
        String country = countryName.getText().toString();

        if (TextUtils.isEmpty(userAccountName)){
            Toast.makeText(this, "Please input your user name... ", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(userfullName)){
            Toast.makeText(this, "Please input your full name... ", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(country)){
            Toast.makeText(this, "Please input your country... ", Toast.LENGTH_SHORT).show();
        } else {

            loadingBar.setTitle("Saving information...");
            loadingBar.setMessage("please wait, while we are creating your new account...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            HashMap<String, String> userMap = new HashMap();
            userMap.put("username", userAccountName);
            userMap.put("fullname", userfullName);
            userMap.put("country", country);
            userMap.put("status", "user can put his/her profile here.");
            userMap.put("gender", "none");
            userMap.put("DOB", "none");
            userMap.put("relationshipstatus", "none");

            usersRef.updateChildren((Map)userMap)
                    .addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    loadingBar.dismiss();
                    if (task.isSuccessful()){
                        sendUserToMainActivity();
                        Toast.makeText(SetupActivity.this, "your account has been created successfully.", Toast.LENGTH_SHORT).show();
                    } else {
                        String message = task.getException().getMessage();
                        Toast.makeText(SetupActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
