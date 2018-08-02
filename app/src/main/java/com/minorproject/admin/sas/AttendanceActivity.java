package com.minorproject.admin.sas;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AttendanceActivity extends AppCompatActivity{

    ViewPager mViewPager;

    public static String mSubCodeI;
    public static String mRoll1I;
    public static String mRoll2I;

    private String dbLinkKey;
    public static String dbLinkValue;
    public static String selfUniqueIdentifier;


    private FirebaseDatabase mDatabase;
    private DatabaseReference mUsersInfoRef;

    public static ArrayList<Uri> mPhotoUriList = new ArrayList<>();
    public static ArrayList<String> mNameList = new ArrayList<>();
    public static ArrayList<String> mRollList = new ArrayList<>();

    private AttendanceAdapter adapter;



    private Intent mIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attendance_loader);

        mIntent = getIntent();
        loadIntent();

        setTitle("Attendance");


        mDatabase = FirebaseDatabase.getInstance();

        mUsersInfoRef = mDatabase.getReference()
                .child("app")
                .child("users");




        mViewPager= findViewById(R.id.viewPager_Attend);
        //set the adapter that will create the individual pages

         adapter = new AttendanceAdapter(this);

         if(!MainActivity.noInternet)
         setViewContentsFromDb();

    }

    private void loadIntent() {


        mSubCodeI = mIntent.getStringExtra("SUBJECT");
        mRoll1I = mIntent.getStringExtra("ROLL1");
        mRoll2I = mIntent.getStringExtra("ROLL2");

    }


    private void setViewContentsFromDb(){



        mUsersInfoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String roll1_split;
                String dbValue_split="";
                String dbValue="";

                roll1_split = mRoll1I.substring(0,6);

                if(dataSnapshot.getValue()!=null){

                if (mRollList.isEmpty()) {
                    for (DataSnapshot users : dataSnapshot.getChildren()) {

                        if(users.child("customID").exists()) {
                            dbValue = users.child("customID").getValue().toString();
                            dbValue_split = dbValue.substring(0, 6);
                        }
                        else{
                            dbValue="";
                            dbValue_split="";
                        }

                        if (roll1_split.contentEquals(dbValue_split)) {

                            mNameList.add(users.child("info").child("firstName").getValue().toString() + " " + users.child("info").child("lastName").getValue().toString());
                            mRollList.add(dbValue);

                            if(users.child("info").child("photoUrl").exists())
                            mPhotoUriList.add(Uri.parse(users.child("info").child("photoUrl").getValue().toString()));

                            for (DataSnapshot USER : users.child("accountLinks").getChildren())
                                dbLinkKey = USER.getKey();

                        }

                        if (users.getKey().contentEquals(MainActivity.UserInstanceForFragment().getUid())) {
                            selfUniqueIdentifier = dbValue;
                        }


                    }

                    loadDatabaseLink();

                }
                }
                else
                    setViewContentsFromDb();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(AttendanceActivity.this, "Database Info Failed", Toast.LENGTH_SHORT).show();

            }
        });


    }

    private void loadDatabaseLink() {

        DatabaseReference dbLinkRef = mDatabase.getReference()
                .child("app")
                .child("database_link")
                .child(dbLinkKey);

        dbLinkRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                dbLinkValue = dataSnapshot.getValue().toString();

                mViewPager.setAdapter(adapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(AttendanceActivity.this, "Database Link Failed", Toast.LENGTH_SHORT).show();
            }
        });

    }


}
