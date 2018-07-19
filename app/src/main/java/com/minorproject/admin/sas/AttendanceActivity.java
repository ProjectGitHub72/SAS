package com.minorproject.admin.sas;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AttendanceActivity extends AppCompatActivity{

    ViewPager mViewPager;
    static String mTeacherI;
    static String mFacultyI;
    static String mSubCodeI;
    static String mYearI;
    static String mRoll1I;
    static String mRoll2I;


    private FirebaseDatabase mDatabase;
    private DatabaseReference mUsersInfoRef;

    public static loginInfo_Collector mUserInfoCollector;
    public static ArrayList<Uri> mPhotoUriList = new ArrayList<>();
    public static ArrayList<String> mNameList = new ArrayList<>();
    public static ArrayList<String> uIdList = new ArrayList<>();
    public static ArrayList<String> mRollList = new ArrayList<>();

    AttendanceAdapter adapter;



    Intent mIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attendance_loader);

        mIntent = getIntent();
        loadIntent();

        setTitle("Attendance");


        mDatabase = FirebaseDatabase.getInstance();

        mUsersInfoRef = mDatabase.getReference()
                .child(AttendanceActivity.mFacultyI)
                .child(AttendanceActivity.mYearI)
                .child("users");



        mViewPager= findViewById(R.id.viewPager_Attend);
        //set the adapter that will create the individual pages

         adapter = new AttendanceAdapter(this);

         setViewContentsFromDb();

    }

    private void loadIntent() {

        mTeacherI = mIntent.getStringExtra("TEACHER");
        mFacultyI = mIntent.getStringExtra("FACULTY");
        mSubCodeI = mIntent.getStringExtra("SUBJECT");
        mYearI = mIntent.getStringExtra("YEAR");
        mRoll1I = mIntent.getStringExtra("ROLL1");
        mRoll2I = mIntent.getStringExtra("ROLL2");

    }


    private void setViewContentsFromDb(){


        mUsersInfoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (mRollList.isEmpty()) {
                    for (DataSnapshot users : dataSnapshot.getChildren()) {

                        uIdList.add(users.getKey());
                        mUserInfoCollector = users.getValue(loginInfo_Collector.class);
                        mNameList.add(mUserInfoCollector.getName());
                        mRollList.add(mUserInfoCollector.getRoll_no());
                        mPhotoUriList.add(Uri.parse(mUserInfoCollector.getPhoto_url()));

                    }

                    mViewPager.setAdapter(adapter);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }





}
