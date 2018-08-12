package com.minorproject.admin.sas;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;



public class loginActivity extends AppCompatActivity{


    private EditText mEmailEditText;
    private EditText mDbLinkEditText;
    private EditText mIdentifierEditText;
    private RadioButton mStudentRB;
    private RadioButton mTeacherRB;
    private Button mSubmitButton;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mdbLink_Ref;
    private DatabaseReference mNewUserInfoDbRef;
    private DatabaseReference mPreStoredInfoRef;

    private FirebaseUser mUser;

    private String DbLink_Key;
    private String firstName;
    private String lastName;
    private String photoUrl;
    private int priority=2;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_info_selector);

        setTitle("Login Manager");

        mEmailEditText = findViewById(R.id.Email_Login_ET);
        mDbLinkEditText = findViewById(R.id.DBLink_Login_ET);
        mIdentifierEditText = findViewById(R.id.UIdentifier_Login_ET);
        mStudentRB = findViewById(R.id.RadioStudent_LoginET);
        mTeacherRB = findViewById(R.id.RadioTeacher_LoginET);
        mSubmitButton = findViewById(R.id.SubmitButton_Login_ET);


        mUser = MainActivity.UserInstanceForFragment();


        mSubmitButton.setEnabled(false);
        mStudentRB.setChecked(false);
        mTeacherRB.setChecked(false);

        newLoginLoad();




    }

    private void setViewLimiters() {

        if(mUser!=null)
        mEmailEditText.setText(mUser.getEmail());

        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



            addToUsersDatabase();

                // Clear input box
                mEmailEditText.setText("");
                mDbLinkEditText.setText("");
                mIdentifierEditText.setText("");
                mStudentRB.setChecked(false);
                mTeacherRB.setChecked(false);

                mSubmitButton.setEnabled(false);

                Toast.makeText(loginActivity.this, "Ready to Begin", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(loginActivity.this,MainActivity.class);
                    startActivity(intent);

            }
        });


       editTextListeners();


    }

    private void checkDatabaseLink() {

        mDatabase = FirebaseDatabase.getInstance();

        mdbLink_Ref = mDatabase.getReference().child("app")
                .child("database_link")
                .child(mDbLinkEditText.getText().toString().trim());



        mdbLink_Ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
              if(dataSnapshot.getValue()!=null) {
                  DbLink_Key = dataSnapshot.getValue().toString();
                  copyDatabase();
              }
              else
                  Toast.makeText(loginActivity.this, "Database Link Failed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(loginActivity.this, "Database Link Failed", Toast.LENGTH_SHORT).show();

            }
        });




    }

    private void copyDatabase() {

        if(mStudentRB.isChecked()) {
            mPreStoredInfoRef = mDatabase.getReference().child("app")
                    .child("app_data")
                    .child(DbLink_Key)
                    .child("users_data")
                    .child("students")
                    .child(mIdentifierEditText.getText().toString().trim())
                    .child("infoData");

        }

        else if(mTeacherRB.isChecked()){
            mPreStoredInfoRef = mDatabase.getReference().child("app")
                    .child("app_data")
                    .child(DbLink_Key)
                    .child("users_data")
                    .child("teachers")
                    .child(mIdentifierEditText.getText().toString().trim())
                    .child("infoData");

        }

        mPreStoredInfoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()!=null){

                    if(dataSnapshot.hasChild("accountLinks")
                            &&dataSnapshot.child("accountLinks").hasChild(mDbLinkEditText.getText().toString().trim())) {
                        priority = Integer.parseInt(dataSnapshot
                                .child("accountLinks")
                                .child(mDbLinkEditText.getText().toString().trim())
                                .getValue().toString());
                    }
                    else{
                        Toast.makeText(loginActivity.this, "Data Not Available", Toast.LENGTH_SHORT).show();

                    }
                  if(dataSnapshot.child("info").hasChild("firstName")
                          &&dataSnapshot.child("info").hasChild("lastName")) {
                      firstName = dataSnapshot.child("info").child("firstName").getValue().toString();
                      lastName = dataSnapshot.child("info").child("lastName").getValue().toString();

                      if(dataSnapshot.child("info").child("photoUrl").exists())
                      photoUrl = dataSnapshot.child("info").child("photoUrl").getValue().toString();

                      mSubmitButton.setEnabled(true);
                  }
                  else{
                      Toast.makeText(loginActivity.this, "Data Not Available", Toast.LENGTH_SHORT).show();
                  }
                }

                else {
                    Toast.makeText(loginActivity.this, "No Data Available", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(loginActivity.this, "Database Info Failed", Toast.LENGTH_SHORT).show();
            }
        });







    }


    private void addToUsersDatabase() {

        mNewUserInfoDbRef.child(mUser.getUid())
                .child("accountLinks")
                .child(mDbLinkEditText.getText().toString().trim())
                .setValue(priority);

        mNewUserInfoDbRef.child(mUser.getUid()).child("info").child("firstName").setValue(firstName);
        mNewUserInfoDbRef.child(mUser.getUid()).child("info").child("lastName").setValue(lastName);
        mNewUserInfoDbRef.child(mUser.getUid()).child("info").child("photoUrl").setValue(photoUrl);
        mNewUserInfoDbRef.child(mUser.getUid()).child("customID").setValue(mIdentifierEditText.getText().toString().trim());

        if(mStudentRB.isChecked()) {
            mPreStoredInfoRef = mDatabase.getReference().child("app")
                    .child("app_data")
                    .child(DbLink_Key)
                    .child("users_data")
                    .child("students")
                    .child(mIdentifierEditText.getText().toString().trim());

        }

        else if(mTeacherRB.isChecked()){
            mPreStoredInfoRef = mDatabase.getReference().child("app")
                    .child("app_data")
                    .child(DbLink_Key)
                    .child("users_data")
                    .child("teachers")
                    .child(mIdentifierEditText.getText().toString().trim());

        }

        mPreStoredInfoRef.child("claimedBy").setValue(mUser.getUid());
        mPreStoredInfoRef.child("infoData").setValue(null);




    }

    private void editTextListeners() {

        mEmailEditText.addTextChangedListener(watcher);
        mDbLinkEditText.addTextChangedListener(watcher);
        mIdentifierEditText.addTextChangedListener(watcher);
        mStudentRB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStudentRB.setChecked(true);
                mTeacherRB.setChecked(false);
                checkConditions();
            }
        });

        mTeacherRB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTeacherRB.setChecked(true);
                mStudentRB.setChecked(false);
                checkConditions();
            }
        });
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

         checkConditions();

        }
    };

    private void checkConditions() {
        if(
                mEmailEditText.getText().toString().length() >0 &&
                        mDbLinkEditText.getText().toString().length() > 0 &&
                        mIdentifierEditText.getText().toString().length() >0 &&
                        (mStudentRB.isChecked() || mTeacherRB.isChecked())
                )            {

            checkDatabaseLink();
        }

        else
            mSubmitButton.setEnabled(false);
    }


    @Override
    public void onBackPressed() {

        }
        



    private void newLoginLoad(){
        Toast.makeText(this, "Checking Data", Toast.LENGTH_SHORT).show();

        mDatabase = FirebaseDatabase.getInstance();
        mNewUserInfoDbRef = mDatabase.getReference().child("app").child("users");


        mNewUserInfoDbRef.addValueEventListener(new ValueEventListener() {
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
                    setViewLimiters();
                    Toast.makeText(loginActivity.this, "Fill all fields", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(loginActivity.this, "Problem Accessing Database", Toast.LENGTH_SHORT).show();
            }
        });


    }

        
        
    }

