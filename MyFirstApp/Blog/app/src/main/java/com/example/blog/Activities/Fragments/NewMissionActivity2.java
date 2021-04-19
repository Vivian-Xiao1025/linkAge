package com.example.blog.Activities.Fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import android.os.Bundle;
import android.widget.Toast;

import com.example.blog.Activities.Home;
import com.example.blog.Activities.MissionActivity;
import com.example.blog.Activities.Models.Mission;
import com.example.blog.Activities.Models.MissionExtraInfo;
import com.example.blog.Activities.Models.Post;
import com.example.blog.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class NewMissionActivity2 extends AppCompatActivity {

    private static final int REQUESCODE = 2;
    private static final int PReqCode = 2;
    private Uri pickedImgUri = null;

    FirebaseAuth auth;
    FirebaseUser curUs;
    Button back, submit;
    TextView reward, hour, minute, every;
    Switch repetition; //TODO: Not sure if switch is stored correctly, if not, delete
    ImageButton img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_mission2);

        auth = FirebaseAuth.getInstance();
        curUs = auth.getCurrentUser();

        previousPage();
        submitMission();
        setupPopupImageClick(); //Not sure if this does anything

        back = findViewById(R.id.new_mission_previous);
        submit = findViewById(R.id.new_mission_submit);
    }

    //Submit Mission and store data
    //TODO: Not sure if the image button is mandatory, hopefully not
    private void submitMission() {
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //None of these fields are required to be filled out, so no if method
                reward = findViewById(R.id.mission_reward);
                hour = findViewById(R.id.duration_hr);
                minute = findViewById(R.id.duration_min);
                every = findViewById(R.id.duration_every);
                repetition = findViewById(R.id.mission_repeat);
                img = findViewById(R.id.imageButton);

                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("blog_image");
                StorageReference imageFilePath = storageReference.child(pickedImgUri.getLastPathSegment());
                imageFilePath.putFile(pickedImgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String imageDownloadLink = uri.toString();
                                //create post Object

                                if(curUs.getPhotoUrl() != null) {
                                    MissionExtraInfo extraInfo = new MissionExtraInfo(reward.getText().toString(),
                                            hour.getText().toString(),
                                            minute.getText().toString(),
                                            every.getText().toString(),
                                            repetition.getText().toString(),
                                            imageDownloadLink,
                                            curUs.getUid(),
                                            curUs.getPhotoUrl().toString());

                                    addMissionExtraInfo(extraInfo);
                                }
                                else{

                                    MissionExtraInfo extraInfo = new MissionExtraInfo(reward.getText().toString(),
                                            hour.getText().toString(),
                                            minute.getText().toString(),
                                            every.getText().toString(),
                                            repetition.getText().toString(),
                                            imageDownloadLink,
                                            curUs.getUid(),
                                            null);

                                    //Add post to firebase database

                                    addMissionExtraInfo(extraInfo);

                                }

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                //something goes wrong uploading picture
                                showMessage(e.getMessage());

                            }
                        });

                    }
                });

             //   submitPost();

            }
        });
    }

    private void showMessage(String message) {
        Toast.makeText(NewMissionActivity2.this, message, Toast.LENGTH_LONG).show();
    }

    private void addMissionExtraInfo(MissionExtraInfo extraInfo) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("MissionsExtra").push();

        //get post unique ID and update post key
        String key = myRef.getKey();
        extraInfo.setMissionExtraKey(key);

        //add post data to firebase database

        myRef.setValue(extraInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                showMessage("Mission Posted Succesfully");
            }
        });
    }

    private void submitPost() {
        Intent intent = new Intent(this, MissionActivity.class);
        startActivity(intent);
    }

    //Previous Page
    private void previousPage() {
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(NewMissionActivity2.this, NewMissionActivity1.class));
                //TODO: If it goes back, need to alter database about the previous mission info
            }
        });
    }

    private void setupPopupImageClick() {

        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //here when image clicks we need to open the gallery
                //before we open the gallery we need to check if our app has access to user files
                //we did this before in register activity I'm just going to copy the code to save time ...

                checkAndRequestForPermission();

            }
        });

    }

    private void checkAndRequestForPermission() {

        if (ContextCompat.checkSelfPermission(NewMissionActivity2.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(NewMissionActivity2.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {

                Toast.makeText(NewMissionActivity2.this, "Please accept for required permission", Toast.LENGTH_SHORT).show();

            }

            else {
                ActivityCompat.requestPermissions(NewMissionActivity2.this,
                        new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                        PReqCode);
            }

        }
        else
            //everything goes well : we have permission to access user gallery
            openGallery();
    }

    private void openGallery() {
        //TODO: open gallery intent and wait for user to pick an image

        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,REQUESCODE);

    }

    //when user picked an image ...
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REQUESCODE && data != null) {

            //the user has successfully picked an image
            //we need to save its reference to a Uri variable
            pickedImgUri = data.getData();
            img.setImageURI(pickedImgUri);

        }
    }


}