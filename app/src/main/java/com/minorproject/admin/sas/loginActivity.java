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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


public class loginActivity extends AppCompatActivity{


    private EditText mNameEditText;
    private EditText mRollEditText;
    private EditText mFacultyEditText;
    private EditText mPermissionKeyEditText;
    private Button mSubmitButton;
    private ImageButton mImagePickerButton;
    private EditText mJoinYearEditText;
    private TextView mSelectedImageUrl_textView;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mUserListRef;
    private DatabaseReference mUserInfoDbRef;

    private FirebaseStorage mStorage;
    private StorageReference mUserPicStorageRef;


    private String name;
    private String roll_no;
    private String photo_url;
    private int priority_level;
    private String faculty_symbol;
    private String year;
    private String permission_key;

    private Uri photo_store_uri;
    private static final int RC_PHOTO_PICKER =  2;

    private boolean isImageReady = false;
    private boolean imageUriloaded = false;


    private  String teacherKey = "";
    private  String adminKey = "";

    private FirebaseUser mUser;

    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mPrefEditor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_info_selector);

        setTitle("Login Manager");

        mNameEditText = findViewById(R.id.name_login_editText);
        mRollEditText = findViewById(R.id.roll_login_editText);
        mFacultyEditText = findViewById(R.id.faculty_login_editText);
        mPermissionKeyEditText = findViewById(R.id.priority_login_EditText);
        mSubmitButton = findViewById(R.id.submit_login_button);
        mJoinYearEditText = findViewById(R.id.join_year_editText);
        mSelectedImageUrl_textView = findViewById(R.id.imageUrl_login_textMain);
        mImagePickerButton = findViewById(R.id.imageButton_login);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mPrefEditor = mSharedPreferences.edit();



        mSubmitButton.setEnabled(false);

        mUser = MainActivity.UserInstanceForFragment();
        newLoginLoad();

        setViewLimiters();



    }

    private void setViewLimiters() {


        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if(permission_key.contentEquals(teacherKey))
                    priority_level = 1;
                else if(permission_key.contentEquals(adminKey))
                    priority_level = 2;
                else
                    priority_level = 0;

                    loginInfo_Collector mNewLoginResult;

                    mNewLoginResult = new loginInfo_Collector(
                            name,roll_no,faculty_symbol,year,photo_url,priority_level
                    );


                    addToDatabase(mNewLoginResult);

                // Clear input box
                mNameEditText.setText("");
                mRollEditText.setText("");
                mFacultyEditText.setText("");
                mJoinYearEditText.setText("");
                mSelectedImageUrl_textView.setText("");
                mPermissionKeyEditText.setText("");


                mSubmitButton.setEnabled(false);
                Toast.makeText(loginActivity.this, "Profile Complete", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(loginActivity.this,MainActivity.class);
                    startActivity(intent);

            }
        });




        // ImagePickerButton shows an image picker to upload a image for a message
        mImagePickerButton.setOnClickListener(new View.OnClickListener() {
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


    private void addToDatabase(loginInfo_Collector newLogin) {

        mUserInfoDbRef.child(mUser.getUid()).setValue(newLogin);
            mUserListRef.child(mUser.getUid()).setValue(name);

            preferenceHandler();


    }

    private void editTextListeners() {

        mNameEditText.addTextChangedListener(watcher);
        mRollEditText.addTextChangedListener(watcher);
        mFacultyEditText.addTextChangedListener(watcher);
        mJoinYearEditText.addTextChangedListener(watcher);
        mPermissionKeyEditText.addTextChangedListener(watcher);
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

            if(
                             mNameEditText.getText().toString().length() >0 &&
                              mRollEditText.getText().toString().length() > 0 &&
                            mFacultyEditText.getText().toString().length() >0 &&
                            mJoinYearEditText.getText().toString().length() > 0 &&
                            mPermissionKeyEditText.getText().toString().length() >0 &&
                            isImageReady
                    )            {

                        loadDataFromView();
            }

            else
                mSubmitButton.setEnabled(false);

        }
    };



    private void loadDataFromView() {

        name = mNameEditText.getText().toString();
        roll_no = mRollEditText.getText().toString().trim();
        faculty_symbol = mFacultyEditText.getText().toString().trim().toUpperCase();
        permission_key = mPermissionKeyEditText.getText().toString().trim();
        year = mJoinYearEditText.getText().toString().trim();
//TODo: each node => 2 key for each faculty

        loadDatabase();


        mSubmitButton.setEnabled(true);


    }

    private void loadDatabase() {



        mDatabase = FirebaseDatabase.getInstance();

        mDatabase.getReference().child("Priority_Key").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot key : dataSnapshot.getChildren() ){
                    if(key.getKey().contentEquals("ADMIN"))
                        adminKey = key.getValue().toString();
                    if(key.getKey().contentEquals("TEACHER"))
                        teacherKey = key.getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mUserListRef = mDatabase.getReference().child("UserList");
        mUserInfoDbRef = mDatabase.getReference().child(faculty_symbol).child(year).child("users");

        if(!imageUriloaded) {
            mStorage = FirebaseStorage.getInstance();
            mUserPicStorageRef = mStorage.getReference().child(faculty_symbol).child(year).child("user_pics");

            loadStorageData();
        }
    }

    private void loadStorageData() {
//        final StorageReference photoRef = mUserPicStorageRef.child(mUser.getUid());
//        photoRef.putFile(photo_store_uri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
//            @Override
//            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//
//
//                photo_url = photoRef.getDownloadUrl().toString();
//
//                mSelectedImageUrl_textView.setText(photo_url);
//
//                isImageReady = true;
//                imageUriloaded = true;
//
//
//
//            }
//        });



            final StorageReference photoRef = mUserPicStorageRef.child(mUser.getUid());
            UploadTask uploadTask = photoRef.putFile(photo_store_uri);

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


                        photo_url = task.getResult().toString();

                        mSelectedImageUrl_textView.setText(photo_url);

                        isImageReady = true;
                        imageUriloaded = true;

                    }
                }
            });




        }





    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            photo_store_uri = data.getData();

            isImageReady = true;
        }
        else if(requestCode==RC_PHOTO_PICKER && resultCode == RESULT_CANCELED){
            Toast.makeText(this, "Not an Image", Toast.LENGTH_SHORT).show();

            isImageReady = false;

        }


    }


    @Override
    public void onBackPressed() {

        }
        



    private void newLoginLoad(){
        Toast.makeText(this, "Checking Data", Toast.LENGTH_SHORT).show();

        mDatabase = FirebaseDatabase.getInstance();
        mUserListRef = mDatabase.getReference().child("UserList");


        mUserListRef.addValueEventListener(new ValueEventListener() {
            @Override


            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                int count=0;
                for(DataSnapshot users : dataSnapshot.getChildren()){

                    if(mUser!=null)
                        if(mUser.getUid().contentEquals(users.getKey())){
                            count++;
                        }

                }

                if(count!=0)
                    startActivity(new Intent(loginActivity.this,MainActivity.class));
                else{
                    Toast.makeText(loginActivity.this, "Fill all fields", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void preferenceHandler(){

        mPrefEditor.putString(getString(R.string.NAME),name);
        mPrefEditor.commit();

        mPrefEditor.putString(getString(R.string.FACULTY),faculty_symbol);
        mPrefEditor.commit();

        mPrefEditor.putString(getString(R.string.ROLL),roll_no);
        mPrefEditor.commit();

        mPrefEditor.putString(getString(R.string.YEAR),year);
        mPrefEditor.commit();

        mPrefEditor.putString(getString(R.string.PHOTO_URL),photo_url);
        mPrefEditor.commit();

        mPrefEditor.putInt(getString(R.string.PRIORITY),priority_level);
        mPrefEditor.commit();




    }
        
        
    }

