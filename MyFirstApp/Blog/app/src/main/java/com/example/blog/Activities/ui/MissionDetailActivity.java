package com.example.blog.Activities.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.blog.Adapters.CommentAdapter;
import com.example.blog.Models.Comment;
import com.example.blog.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MissionDetailActivity extends AppCompatActivity {

    ImageView imgCurrentUser;
    TextView txtMissionDesc, txtMissionDateName, txtMissionTitle, txtMissionDate, txtMissionTime, txtMissionSpot, txtMissionLocation;
    EditText editTextComment;
    Button btnAddComment;
    String MissionKey;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;
    RecyclerView RvComment2;
    CommentAdapter commentAdapter;
    List<Comment> listComment;
    static String COMMENT_KEY = "Comment";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mission_detail);

        // let's set the statue bar background to transparent
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getSupportActionBar().hide();

        //ini Views

        RvComment2 = findViewById(R.id.rv_comment2);

        imgCurrentUser = findViewById(R.id.mission_detail_currentuser_img);

        txtMissionTitle = findViewById(R.id.mission_detail_title);
        txtMissionDesc = findViewById(R.id.mission_detail_desc);
        txtMissionDateName = findViewById(R.id.mission_detail_date_name);
        txtMissionDate = findViewById(R.id.mission_detail_date);
        txtMissionTime = findViewById(R.id.mission_detail_time);
        txtMissionSpot = findViewById(R.id.mission_detail_slots);
        txtMissionLocation = findViewById(R.id.mission_detail_location);

        editTextComment = findViewById(R.id.mission_detail_comment);
        btnAddComment = findViewById(R.id.mission_detail_add_comment_btn);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();

        //add Comment button click listener
        btnAddComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                btnAddComment.setVisibility(View.INVISIBLE);
                DatabaseReference commentReference = firebaseDatabase.getReference(COMMENT_KEY).child(MissionKey).push();
                String comment_content = editTextComment.getText().toString();
                String uid = firebaseUser.getUid();
                String uname = firebaseUser.getDisplayName();
                //String uimg = firebaseUser.getPhotoUrl().toString();
                String uimg = "test";
                Comment comment = new Comment(comment_content,uid,uimg,uname);

                commentReference.setValue(comment).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        showMessage("comment added");
                        editTextComment.setText("");
                        btnAddComment.setVisibility(View.VISIBLE);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showMessage("fail to add comment"+e.getMessage());
                    }
                });


            }
        });


        //now we need to bind all data into those views
        // first we need to get post data
        //we need to send post detail data to this activity first ...
        // now we can get post data

        String missionTitle = getIntent().getExtras().getString("title");
        txtMissionTitle.setText(missionTitle);

        //String userpostImage = getIntent().getExtras().getString("userPhoto");

        String missionDescription = getIntent().getExtras().getString("description");
        txtMissionDesc.setText(missionDescription);

        String missionDate = getIntent().getExtras().getString("date");
        txtMissionDate.setText(missionDate);

        String missionTime = getIntent().getExtras().getString("time");
        txtMissionTime.setText(missionTime);

        String missionSpot = getIntent().getExtras().getString("spots");
        txtMissionSpot.setText(missionSpot);

        //set comment user image
        if (firebaseUser.getPhotoUrl() != null) {
            Glide.with(this).load(firebaseUser.getPhotoUrl()).into(imgCurrentUser);
        }
        else
            Glide.with(this).load(R.drawable.avatar).into(imgCurrentUser);

        //get post id
        MissionKey = getIntent().getExtras().getString("missionKey");

        String date = timestampToString(getIntent().getExtras().getLong("missionData"));
        txtMissionDateName.setText(date);

        //ini RecycleviewComment
        iniRvComment();
    }

    private void iniRvComment() {

        RvComment2.setLayoutManager(new LinearLayoutManager(this));

        DatabaseReference commentRef = firebaseDatabase.getReference(COMMENT_KEY).child(MissionKey);
        commentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listComment = new ArrayList<>();
                for (DataSnapshot snap:dataSnapshot.getChildren()) {

                    Comment comment = snap.getValue(Comment.class);
                    listComment.add(comment);

                }

                commentAdapter = new CommentAdapter(getApplicationContext(),listComment);
                RvComment2.setAdapter(commentAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void showMessage(String message) {

        Toast.makeText(this,message,Toast.LENGTH_LONG).show();

    }

    private String timestampToString(long time) {

        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(time);
        String date = DateFormat.format("dd-MM-yyyy",calendar).toString();
        return date;

    }

    }
