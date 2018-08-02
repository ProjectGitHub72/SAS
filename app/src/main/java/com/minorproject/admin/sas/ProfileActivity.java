package com.minorproject.admin.sas;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class ProfileActivity extends AppCompatActivity {

    private EditText mNameET;
    private EditText mEmailET;
    private EditText mImageUriET;
    private EditText mOldPasswordET;
    private EditText mNewPasswordET;
    private ImageButton mImageButton;
    private Button mUpdateButton;
    private EditText mIdentifierEditText;
    private TextView mIdentifierTV;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mDbReference;
    private DatabaseReference mCustomIdRef;
    private FirebaseStorage mStorage;
    private StorageReference mStorageRef;

    private String mUserNameUP;
    private String mImageUP;
    private int mPriority;

    private static final int RC_PHOTO_PICKER =  2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editor_profile);

        setTitle("Edit Profile");

        mNameET = findViewById(R.id.profileNameET);
        mEmailET = findViewById(R.id.profileEmailET);
        mImageUriET = findViewById(R.id.profileImageUriET);
        mOldPasswordET = findViewById(R.id.profilePasswordET);
        mNewPasswordET = findViewById(R.id.profilePasswordNewET);
        mImageButton = findViewById(R.id.profileImageButtonET);
        mUpdateButton = findViewById(R.id.update_profile_button);
        mIdentifierEditText = findViewById(R.id.profileUniqueIDET);
        mIdentifierTV = findViewById(R.id.profileUniqueIDTV);



        loadDBAndStorage();
        loadDataToView();
        setViewLimiters();




    }

    private void loadDataToView(){

        Intent intent = getIntent();

        if(intent.hasExtra("NAME")) {
            mNameET.setText(intent.getStringExtra("NAME"));

            if(intent.getStringExtra("IMAGE")!=null)
            mImageUriET.setText(intent.getStringExtra("IMAGE"));

            mIdentifierEditText.setText(intent.getStringExtra("IDENTIFIER"));


            mPriority = intent.getIntExtra("PRIORITY",2);

            if(mPriority==2) {
                mIdentifierEditText.setVisibility(View.GONE);
                mIdentifierTV.setVisibility(View.GONE);
                mIdentifierEditText.setEnabled(false);
            }
            else {
                mIdentifierEditText.setVisibility(View.VISIBLE);
                mIdentifierTV.setVisibility(View.VISIBLE);
                mIdentifierEditText.setEnabled(true);
            }
        }

        if(MainActivity.UserInstanceForFragment()!=null)
        mEmailET.setText(MainActivity.UserInstanceForFragment().getEmail());




    }


    private void loadDBAndStorage(){
        mDatabase = FirebaseDatabase.getInstance();


        mDbReference = mDatabase.getReference().child("app")
                .child("users")
                .child(MainActivity.UserInstanceForFragment().getUid())
                .child("info");

        mCustomIdRef = mDatabase.getReference().child("app")
                .child("users")
                .child(MainActivity.UserInstanceForFragment().getUid());


        mStorage = FirebaseStorage.getInstance();
        mStorageRef = mStorage.getReference().child("user_photos");



    }


    private void setViewLimiters() {

        mUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String name[];
                String firstName="";
                String lastName="";

                updateEmails();

                name = mUserNameUP.split(" ");
                firstName = name[0];

                for(int i=1;i<name.length;i++){
                    if(i==name.length-1)
                        lastName = lastName + name[i];
                    else
                        lastName = lastName + name[i] + " ";
                }

                mDbReference.child("firstName").setValue(firstName);
                mDbReference.child("lastName").setValue(lastName);
                mDbReference.child("photoUrl").setValue(mImageUP);

                if(mIdentifierEditText.isEnabled()) {
                    mCustomIdRef.child("accountLinks").setValue("");
                    mCustomIdRef.child("accountLinks").child(mIdentifierEditText.getText().toString()).setValue(mPriority);

                }

                // Clear input box
                mNameET.setText("");
                mEmailET.setText("");
                mOldPasswordET.setText("");
                mNewPasswordET.setText("");
                mImageUriET.setText("");
                mIdentifierEditText.setText("");


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
        mOldPasswordET.addTextChangedListener(watcher);
        mNewPasswordET.addTextChangedListener(watcher);
        mIdentifierEditText.addTextChangedListener(watcher);

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


            if(mIdentifierEditText.isEnabled()) {
                if (
                        mNameET.getText().toString().length() > 0 &&
                                mEmailET.getText().toString().length() > 0 &&
                                mImageUriET.getText().toString().length() > 0 &&
                                mNewPasswordET.getText().toString().length() > 5 &&
                                mOldPasswordET.getText().toString().length() > 0 &&
                                mIdentifierEditText.getText().toString().length() > 0
                        ) {

                    mUserNameUP = mNameET.getText().toString();
                    mImageUP = mImageUriET.getText().toString();

                    mUpdateButton.setEnabled(true);

                } else
                    mUpdateButton.setEnabled(false);
            }

            else{

                if (
                        mNameET.getText().toString().length() > 0 &&
                                mEmailET.getText().toString().length() > 0 &&
                                mImageUriET.getText().toString().length() > 0 &&
                                mNewPasswordET.getText().toString().length() > 5 &&
                                mOldPasswordET.getText().toString().length() > 0
                        ) {

                    mUserNameUP = mNameET.getText().toString();
                    mImageUP = mImageUriET.getText().toString();

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
