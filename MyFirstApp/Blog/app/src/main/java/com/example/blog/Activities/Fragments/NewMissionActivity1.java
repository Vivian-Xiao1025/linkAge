package com.example.blog.Activities.Fragments;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import android.os.Bundle;
import android.widget.Toast;

import com.example.blog.Activities.MissionActivity;
import com.example.blog.Activities.Models.Mission;
import com.example.blog.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class NewMissionActivity1 extends AppCompatActivity {


    FirebaseAuth author;
    FirebaseUser curUser;
    Button cancel, next;
    TextView title, location, date, time, people, description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_mission1);

        author = FirebaseAuth.getInstance();
        curUser = author.getCurrentUser();

        goBack();
        nextPage();

        cancel = findViewById(R.id.cancel);
        next = findViewById(R.id.next_page);

    }

    private void goBack() {
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToMyPost();
            }
        });
    }

    private void backToMyPost() {
        Intent intent = new Intent(this, MissionActivity.class);
        startActivity(intent);
    }

    private void nextPage(){

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                title = findViewById(R.id.mission_title);
                location = findViewById(R.id.mission_location);
                date = findViewById(R.id.mission_date);
                time = findViewById(R.id.mission_time);
                people = findViewById(R.id.mission_spot);
                description = findViewById(R.id.mission_description);

                if (!title.getText().toString().isEmpty()
                        && !location.getText().toString().isEmpty()
                        && !date.getText().toString().isEmpty()
                        && !time.getText().toString().isEmpty()
                        && !people.getText().toString().isEmpty()
                        && !description.getText().toString().isEmpty()) {

                    //TODO: add these inputs to firebase
                    Mission m = new Mission(title.getText().toString(),
                            location.getText().toString(),
                            date.getText().toString(),
                            time.getText().toString(),
                            people.getText().toString(),
                            description.getText().toString());

                    addMission(m);
                }
                else {
                    showMessage("Please fill out all the fields before moving on");
                }

                openSecondPage();

            }
        });
    }

    private void openSecondPage() {
        Intent intent = new Intent(this, NewMissionActivity2.class);
        startActivity(intent);
    }

    private void addMission(Mission m) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Mission").push();

        //get post unique ID and update post key
        String key = myRef.getKey();
        m.setMissionKey(key);

        //add post data to firebase database

        myRef.setValue(m).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                showMessage("Info Saved Succesfully");
            }
        });
    }

    private void showMessage(String message) {
        Toast.makeText(NewMissionActivity1.this, message, Toast.LENGTH_LONG).show();
    }
}