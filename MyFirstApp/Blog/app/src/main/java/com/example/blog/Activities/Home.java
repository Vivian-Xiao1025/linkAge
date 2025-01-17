package com.example.blog.Activities;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.example.blog.Activities.Fragments.HomeFragment;
import com.example.blog.Activities.Fragments.ProfileFragment;
import com.example.blog.Activities.Fragments.SettingsFragment;
import com.example.blog.Activities.Models.Post;
import com.example.blog.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class Home extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private static final int REQUESCODE = 2;
    private AppBarConfiguration mAppBarConfiguration;

    FirebaseAuth mAuth;
    FirebaseUser currentuser;
    Dialog popAddPost;
    ImageView popupUserImage,popupPostImage,popupAddBtn;
    TextView popupTitle, popupDescription;
    ProgressBar popupClickProgress;
    private static final int PReqCode = 2;
    private Uri pickedImgUri = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home2);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //ini

        mAuth = FirebaseAuth.getInstance();
        currentuser = mAuth.getCurrentUser();

        //ini popup
        iniPopup();
        setupPopupImageClick();


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popAddPost.show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        /**        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_storyboard, R.id.nav_gallery, R.id.nav_slideshow)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
*/

        updateNavHeader();

        //set the home fragment as the default one

        getSupportFragmentManager().beginTransaction().replace(R.id.container, new HomeFragment()).commit();



    }

    private void setupPopupImageClick() {

        popupPostImage.setOnClickListener(new View.OnClickListener() {
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

        if (ContextCompat.checkSelfPermission(Home.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(Home.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {

                Toast.makeText(Home.this, "Please accept for required permission", Toast.LENGTH_SHORT).show();

            }

            else {
                ActivityCompat.requestPermissions(Home.this,
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
            popupPostImage.setImageURI(pickedImgUri);

        }


    }



    private void iniPopup() {

        popAddPost = new Dialog(this);
        popAddPost.setContentView(R.layout.popup_add_post);
        popAddPost.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popAddPost.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT,Toolbar.LayoutParams.WRAP_CONTENT);
        popAddPost.getWindow().getAttributes().gravity = Gravity.TOP;

        //ini popup widgets
        popupUserImage = popAddPost.findViewById(R.id.popup_user_image);
        popupPostImage = popAddPost.findViewById(R.id.popup_img);
        popupTitle = popAddPost.findViewById(R.id.popup_title);
        popupDescription = popAddPost.findViewById(R.id.popup_description);
        popupAddBtn = popAddPost.findViewById(R.id.popup_add);
        popupClickProgress = popAddPost.findViewById(R.id.popup_progressBar);

        //load Current user profile photo

        Glide.with(Home.this).load(currentuser.getPhotoUrl()).into(popupUserImage);



        //Add post click Listener

        popupAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                popupAddBtn.setVisibility(View.INVISIBLE);
                popupClickProgress.setVisibility(View.VISIBLE);

                //we need to test all input fields (Title and description ) and post image
                if (!popupTitle.getText().toString().isEmpty()
                        && !popupDescription.getText().toString().isEmpty()
                        && pickedImgUri != null) {

                    //everything is okay no empty or null value
                    //TODO: Create post object and add it to firebase database
                    //first we need to upload post Image
                    //access firebase storage
                    StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("bloag_image");
                    StorageReference imageFilePath = storageReference.child(pickedImgUri.getLastPathSegment());
                    imageFilePath.putFile(pickedImgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imageDownloadLink = uri.toString();
                                    //create post Object

                                    if(currentuser.getPhotoUrl() != null) {
                                        Post post = new Post(popupTitle.getText().toString(),
                                                popupDescription.getText().toString(),
                                                imageDownloadLink,
                                                currentuser.getUid(),
                                                currentuser.getPhotoUrl().toString());

                                        //Add post to firebase database

                                        addPost(post);
                                    }
                                    else{

                                        Post post = new Post(popupTitle.getText().toString(),
                                                popupDescription.getText().toString(),
                                                imageDownloadLink,
                                                currentuser.getUid(),
                                                null);

                                        //Add post to firebase database

                                        addPost(post);



                                    }



                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    //something goes wrong uploading picture
                                    showMessage(e.getMessage());
                                    popupClickProgress.setVisibility(View.INVISIBLE);
                                    popupAddBtn.setVisibility(View.VISIBLE);



                                }
                            });

                        }
                    });



                }

                else {
                    showMessage("Please verify all input fields and choose post image");
                    popupAddBtn.setVisibility(View.VISIBLE);
                    popupClickProgress.setVisibility(View.INVISIBLE);
                }
            }

        });




    }

    private void addPost(Post post) {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Posts").push();

        //get post unique ID and update post key
        String key = myRef.getKey();
        post.setPostKey(key);


        //add post data to firebase database

        myRef.setValue(post).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                showMessage("Post Added Succesfully");
                popupClickProgress.setVisibility(View.INVISIBLE);
                popupAddBtn.setVisibility(View.VISIBLE);
                popAddPost.dismiss();
            }
        });




    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void updateNavHeader() {

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView navUsername = headerView.findViewById(R.id.nav_username);
        TextView navUserMail = headerView.findViewById(R.id.nav_user_mail);
        ImageView navUserPhot = headerView.findViewById(R.id.nav_user_photo);


        navUserMail.setText(currentuser.getEmail());
        navUsername.setText(currentuser.getDisplayName());

        // now we will use Glide to load user image
        // first we need to import the library

        if (currentuser.getPhotoUrl() != null) {
            Glide.with(this).load(currentuser.getPhotoUrl()).into(navUserPhot);
        }else
            Glide.with(this).load(R.drawable.avatar).into(navUserPhot);

    }



    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_storyboard) {

            getSupportActionBar().setTitle("Storyboard");
            getSupportFragmentManager().beginTransaction().replace(R.id.container,new HomeFragment()).commit();

        }
        else if (id == R.id.nav_mission) {

            getSupportActionBar().setTitle("My Missions");
            getSupportFragmentManager().beginTransaction().replace(R.id.container,new SettingsFragment()).commit();
            
        }
        else if (id == R.id.nav_profile) {

            getSupportActionBar().setTitle("All Missions");
           // moveToNewActivity(); //the problem this method does not work might be it's not going from a fragment to an activity
            getSupportFragmentManager().beginTransaction().replace(R.id.container,new ProfileFragment()).commit();

        }
        else if (id == R.id.nav_signout) {

            FirebaseAuth.getInstance().signOut();
            Intent loginActivity = new Intent(getApplicationContext(),LoginActivity.class);
            startActivity(loginActivity);
            finish();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;

    }

    private void moveToNewActivity () {

        Intent i = new Intent(this, MissionActivity.class);
        startActivity(i);
        ((Activity) this).overridePendingTransition(0, 0);

    }


    private void showMessage(String message) {
        Toast.makeText(Home.this,message,Toast.LENGTH_LONG).show();

    }

}