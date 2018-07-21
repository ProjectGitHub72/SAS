package com.minorproject.admin.sas;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class ProfileActivity extends AppCompatActivity {

    private EditText mNameET;
    private EditText mEmailET;
    private EditText mImageUriET;
    private EditText mPriorityKeyET;
    private EditText mOldPasswordET;
    private EditText mNewPasswordET;
    private EditText mFacultyET;
    private EditText mYearET;
    private ImageButton mImageButton;
    private Button mUpdateButton;

    private SharedPreferences mSharedPref;
    private SharedPreferences.Editor mPrefEditor;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDbReference;
    private FirebaseStorage mStorage;
    private StorageReference mStorageRef;

    private static final int RC_PHOTO_PICKER =  2;
    private int priority_level = 0;
    private String teacherKey = "";
    private String adminKey = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editor_profile);

        mSharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        mPrefEditor = mSharedPref.edit();

        loadDBAndStorage();


        setTitle("Edit Profile");

        mNameET = findViewById(R.id.profileNameET);
        mEmailET = findViewById(R.id.profileEmailET);
        mImageUriET = findViewById(R.id.profileImageUriET);
        mPriorityKeyET = findViewById(R.id.profileStatusET);
        mOldPasswordET = findViewById(R.id.profilePasswordET);
        mNewPasswordET = findViewById(R.id.profilePasswordNewET);
        mImageButton = findViewById(R.id.profileImageButtonET);
        mUpdateButton = findViewById(R.id.update_profile_button);
        mFacultyET = findViewById(R.id.profileFacultyET);
        mYearET = findViewById(R.id.profileYearET);



        loadPreference();
        setViewLimiters();




    }

    private void loadPreference(){

        if(mSharedPref.getInt(getString(R.string.PRIORITY),0)==0){
            mYearET.setVisibility(View.GONE);
            mFacultyET.setVisibility(View.GONE);

            findViewById(R.id.facultyTVProfile).setVisibility(View.GONE);
            findViewById(R.id.yearTVProfile).setVisibility(View.GONE);
        }
        else{
            mYearET.setVisibility(View.VISIBLE);
            mFacultyET.setVisibility(View.VISIBLE);
            findViewById(R.id.facultyTVProfile).setVisibility(View.VISIBLE);
            findViewById(R.id.yearTVProfile).setVisibility(View.VISIBLE);
            mFacultyET.setText(mSharedPref.getString(getString(R.string.FACULTY),""));
            mYearET.setText(mSharedPref.getString(getString(R.string.YEAR),""));
        }

        mNameET.setText(mSharedPref.getString(getString(R.string.NAME),MainActivity.UserInstanceForFragment().getDisplayName()));
        mEmailET.setText(MainActivity.UserInstanceForFragment().getEmail());
        mImageUriET.setText(mSharedPref.getString(getString(R.string.PHOTO_URL),""));
        mPriorityKeyET.setText("0");


    }

    private void setPreference(){

        if(mSharedPref.getInt(getString(R.string.PRIORITY),0)!=0){
            mPrefEditor.putString(getString(R.string.FACULTY),mFacultyET.getText().toString().trim());
            mPrefEditor.commit();

            mPrefEditor.putString(getString(R.string.YEAR),mYearET.getText().toString().trim());
            mPrefEditor.commit();
        }

        mPrefEditor.putString(getString(R.string.NAME),mNameET.getText().toString());
        mPrefEditor.commit();

        mPrefEditor.putString(getString(R.string.PHOTO_URL),mImageUriET.getText().toString());
        mPrefEditor.commit();

        mPrefEditor.putInt(getString(R.string.PRIORITY),priority_level);
        mPrefEditor.commit();



    }

    private void loadDBAndStorage(){
        mDatabase = FirebaseDatabase.getInstance();

        mDatabase.getReference().child("Priority_Key").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot key : dataSnapshot.getChildren() ){
                    if(key.getKey().contentEquals("ADMIN"))
                        adminKey = key.getValue().toString().trim();
                    if(key.getKey().contentEquals("TEACHER"))
                        teacherKey = key.getValue().toString().trim();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        mDbReference = mDatabase.getReference()
                        .child(mSharedPref.getString(getString(R.string.FACULTY),""))
                        .child(mSharedPref.getString(getString(R.string.YEAR),""))
                        .child("users")
                        .child(MainActivity.UserInstanceForFragment().getUid());

        mStorage = FirebaseStorage.getInstance();
        mStorageRef = mStorage.getReference()
                        .child(mSharedPref.getString(getString(R.string.FACULTY),""))
                        .child(mSharedPref.getString(getString(R.string.YEAR),""))
                        .child("user_pics");


    }


    private void setViewLimiters() {


        mUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(mPriorityKeyET.getText().toString().trim().contentEquals(teacherKey))
                    priority_level = 1;
                else if(mPriorityKeyET.getText().toString().trim().contentEquals(adminKey))
                    priority_level = 2;
                else
                    priority_level = 0;

                loginInfo_Collector mNewLoginResult;

                if(mSharedPref.getInt(getString(R.string.PRIORITY),0)!=0) {
                    mNewLoginResult = new loginInfo_Collector(
                            mNameET.getText().toString(),
                            mSharedPref.getString(getString(R.string.ROLL), "0"),
                            mFacultyET.getText().toString().trim().toUpperCase(),
                            mYearET.getText().toString().trim(),
                            mImageUriET.getText().toString(),
                            priority_level
                    );
                }
                else{
                    mNewLoginResult = new loginInfo_Collector(
                            mNameET.getText().toString(),
                            mSharedPref.getString(getString(R.string.ROLL), "0"),
                            mSharedPref.getString(getString(R.string.FACULTY),""),
                            mSharedPref.getString(getString(R.string.YEAR),""),
                            mImageUriET.getText().toString(),
                            priority_level
                    );
                }
                updateEmails();

                mDbReference.setValue(mNewLoginResult);
                setPreference();


                // Clear input box
                mNameET.setText("");
                mEmailET.setText("");
                mOldPasswordET.setText("");
                mNewPasswordET.setText("");
                mPriorityKeyET.setText("");
                mImageUriET.setText("");
                if(mYearET.isEnabled()) {
                    mFacultyET.setText("");
                    mYearET.setText("");
                }


                mUpdateButton.setEnabled(false);
                Toast.makeText(ProfileActivity.this, "Update Complete", Toast.LENGTH_SHORT).show();
                Intent intent =  new Intent(ProfileActivity.this,MainActivity.class);
                intent.putExtra("FRAGMENT","PROFILE");
                startActivity(intent);

            }
        });




        // ImagePickerButton shows an image picker to upload a image for a message
        mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
            }
        });

        editTextListeners();


    }


    private void editTextListeners() {

        mNameET.addTextChangedListener(watcher);
        mEmailET.addTextChangedListener(watcher);
        mImageUriET.addTextChangedListener(watcher);
        mPriorityKeyET.addTextChangedListener(watcher);
        mOldPasswordET.addTextChangedListener(watcher);
        mNewPasswordET.addTextChangedListener(watcher);
        if(mYearET.isEnabled()) {
            mFacultyET.addTextChangedListener(watcher);
            mYearET.addTextChangedListener(watcher);
        }
    }


    private final TextWatcher watcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after)
        { }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count)
        {}
        @Override
        public void afterTextChanged(Editable s) {

            if (mSharedPref.getInt(getString(R.string.PRIORITY),0)!=0) {
                if (
                        mNameET.getText().toString().length() > 0 &&
                                mEmailET.getText().toString().length() > 0 &&
                                mImageUriET.getText().toString().length() > 0 &&
                                mPriorityKeyET.getText().toString().length() > 0 &&
                                mNewPasswordET.getText().toString().length() > 5 &&
                                mOldPasswordET.getText().toString().length() > 0 &&
                                mFacultyET.getText().toString().length() > 0 &&
                                mYearET.getText().toString().length() > 0
                        ) {

                    mUpdateButton.setEnabled(true);

                } else
                    mUpdateButton.setEnabled(false);

            }

                else  {
                if (
                        mNameET.getText().toString().length() > 0 &&
                                mEmailET.getText().toString().length() > 0 &&
                                mImageUriET.getText().toString().length() > 0 &&
                                mPriorityKeyET.getText().toString().length() > 0 &&
                                mNewPasswordET.getText().toString().length() > 5 &&
                                mOldPasswordET.getText().toString().length() > 0
                        ) {

                    mUpdateButton.setEnabled(true);

                } else
                    mUpdateButton.setEnabled(false);

            }

        }
    };




    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            final StorageReference photoRef = mStorageRef.child(MainActivity.UserInstanceForFragment().getUid());
            UploadTask uploadTask = photoRef.putFile(data.getData());

            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return photoRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {

                        mImageUriET.setText(task.getResult().toString());

                    }
                }
            });




        }
        else if(requestCode==RC_PHOTO_PICKER && resultCode == RESULT_CANCELED){
            Toast.makeText(this, "Not an Image", Toast.LENGTH_SHORT).show();


        }

        editTextListeners();

    }


    private void updateEmails(){

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        AuthCredential credential = EmailAuthProvider
                .getCredential(MainActivity.UserInstanceForFragment().getEmail(),
                        mOldPasswordET.getText().toString());

    // Prompt the user to re-provide their sign-in credentials

        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(ProfileActivity.this, "User re-authenticated.", Toast.LENGTH_SHORT).show();
                    }
                });


        user.updateEmail(mEmailET.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ProfileActivity.this, "User email address updated.", Toast.LENGTH_SHORT).show();

                        }
                    }
                });


        user.updatePassword(mNewPasswordET.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ProfileActivity.this, "User password updated.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(mNameET.getText().toString())
                .setPhotoUri(Uri.parse(mImageUriET.getText().toString()))
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ProfileActivity.this, "User profile updated.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


    }




}
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
//            final StorageReference photoRef = mStorageRef.child(MainActivity.UserInstanceForFragment().getUid());
//            photoRef.putFile(data.getData()).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                @Override
//                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//
//
//                    mImageUriET.setText(photoRef.getDownloadUrl().toString());
//
//
//                }
//            });
//        }
//        else if(requestCode==RC_PHOTO_PICKER && resultCode == RESULT_CANCELED){
//            Toast.makeText(this, "Not an Image", Toast.LENGTH_SHORT).show();
//
//
//        }
//
//        editTextListeners();
//
//    }