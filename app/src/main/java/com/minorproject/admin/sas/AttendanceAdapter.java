package com.minorproject.admin.sas;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

class AttendanceAdapter extends PagerAdapter implements View.OnTouchListener {

    private GestureDetector gestureDetector;
    private LayoutInflater inflater;
    private Context context;

    private ArrayList<Uri> mPhotoUriList = new ArrayList<>();
    private ArrayList<String> mNameList = new ArrayList<>();
    private ArrayList<String> uIdList = new ArrayList<>();
    private ArrayList<String> mRollList = new ArrayList<>();

    private ArrayList<String> mRollDisplayList = new ArrayList<>();
    private ArrayList<Uri> mPhotoUriDisplayList = new ArrayList<>();
    private ArrayList<String> mNameDisplayList = new ArrayList<>();
    private ArrayList<String> mUidDisplayList = new ArrayList<>();


    private TextView mSubjectTextViewTop;
    private TextView mTeacherViewTop;
    private TextView mFacultyViewTop;

    private TextView mRollViewMid;
    private TextView mStudentNameViewMid;
    private ImageView mStudentImageViewMid;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mUser_dataAttRef;

    private String formattedDate;
    private int focusedPage = 0;




    AttendanceAdapter(Context context){
        this.context = context;
    }






    @Override
    public int getCount() {

        if(mRollDisplayList.isEmpty()) {
            loadDatabase();
        }

        timeLoader();

        return mRollDisplayList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {

        return view==object ;


    }


    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
        object=null;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        gestureDetector.onTouchEvent(event);
        return true;


    }


    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {

        gestureDetector=new GestureDetector(context,new OnSwipeListener(){

            @Override
            public boolean onSwipe(Direction direction) {



                if (direction==Direction.up){
                    //do your stuff

                    mUser_dataAttRef.child(mUidDisplayList.get(focusedPage))
                            .child("attendance")
                            .child(AttendanceActivity.mSubCodeI)
                            .child(formattedDate)
                            .child(MainActivity.UserInstanceForFragment().getUid())
                            .setValue("present");

                    Toast.makeText(context, "Present", Toast.LENGTH_SHORT).show();
                }

                if (direction==Direction.down){
                    //do your stuff

                    mUser_dataAttRef.child(mUidDisplayList.get(focusedPage))
                            .child("attendance")
                            .child(AttendanceActivity.mSubCodeI)
                            .child(formattedDate)
                            .child(MainActivity.UserInstanceForFragment().getUid())
                            .setValue("absent");

                    Toast.makeText(context, "Absent", Toast.LENGTH_SHORT).show();
                }
                return true;
            }


        });

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);


        View page = inflater.inflate(R.layout.attendance_adapter, null);


            mSubjectTextViewTop = page.findViewById(R.id.subject_permA_view);
            mFacultyViewTop = page.findViewById(R.id.faculty_permA_view);
            mTeacherViewTop = page.findViewById(R.id.teacher_permA_view);

            mRollViewMid = page.findViewById(R.id.attend_roll);
            mStudentNameViewMid = page.findViewById(R.id.attend_name);
            mStudentImageViewMid = page.findViewById(R.id.student_image_attend);

            setViewContentFromIntent();

            if(mRollDisplayList.isEmpty()) {
                loadDatabase();
            }
            mRollViewMid.setText(mRollDisplayList.get(position));
            mStudentNameViewMid.setText(mNameDisplayList.get(position));

        Glide
                .with(context)
                .load(mPhotoUriDisplayList.get(position)) // the uri you got from Firebase
                .centerCrop()
                .into(mStudentImageViewMid); //Your imageView variable



        mStudentNameViewMid.setOnTouchListener(this);

        mRollViewMid.setOnTouchListener(this);

        mStudentImageViewMid.setOnTouchListener(this);

        //Add the page to the front of the queue
        ((ViewPager) container).addView(page, 0);

        ((ViewPager) container).setOnPageChangeListener(new MyPageChangeListener());

        return page;



    }

    private void setViewContentFromIntent() {
        mSubjectTextViewTop.setText(AttendanceActivity.mSubCodeI);
        mFacultyViewTop.setText(AttendanceActivity.mFacultyI);
        mTeacherViewTop.setText(AttendanceActivity.mTeacherI);


    }


    private void loadDatabase(){

        mDatabase = FirebaseDatabase.getInstance();


        mUser_dataAttRef = mDatabase.getReference()
                .child(AttendanceActivity.mFacultyI)
                .child(AttendanceActivity.mYearI)
                .child("user_data")
                .child("students");




        setViewContentsFromDb();


    }

    private void setViewContentsFromDb(){

        if(mRollList.isEmpty()){

            mNameList = AttendanceActivity.mNameList;
            mRollList = AttendanceActivity.mRollList;
            mPhotoUriList = AttendanceActivity.mPhotoUriList;
            uIdList = AttendanceActivity.uIdList;

            sortData();
            limitRollRange();
        }

    }

    private void sortData(){

        int length = mRollList.size();

        quickSort(0,length-1);

    }

    private void quickSort(int lowerIndex, int higherIndex) {

        int i = lowerIndex;
        int j = higherIndex;
        // calculate pivot number, I am taking pivot as middle index number
        int pivot = Integer.parseInt(mRollList.get(lowerIndex+(higherIndex-lowerIndex)/2));
        // Divide into two arrays
        while (i <= j) {

            while (Integer.parseInt(mRollList.get(i)) < pivot) {
                i++;
            }
            while (Integer.parseInt(mRollList.get(j)) > pivot) {
                j--;
            }
            if (i <= j) {
                exchangeNumbers(i, j);
                //move index to next position on both sides
                i++;
                j--;
            }
        }
        // call quickSort() method recursively
        if (lowerIndex < j)
            quickSort(lowerIndex, j);
        if (i < higherIndex)
            quickSort(i, higherIndex);
    }


    private void exchangeNumbers(int i, int j) {

        String temp = mRollList.get(i);
        mRollList.set(i,mRollList.get(j));
        mRollList.set(j,temp);

         temp = uIdList.get(i);
        uIdList.set(i,uIdList.get(j));
        uIdList.set(j,temp);

         temp = mNameList.get(i);
        mNameList.set(i, mNameList.get(j));
        mNameList.set(j,temp);

         Uri temp2 = mPhotoUriList.get(i);
        mPhotoUriList.set(i, mPhotoUriList.get(j));
        mPhotoUriList.set(j,temp2);
    }


    private void limitRollRange(){
        int i,j;
        for(i=0;i<mRollList.size();i++){

            if(mRollList.get(i).contentEquals(AttendanceActivity.mRoll1I)){

                for(j=i;j<mRollList.size();j++) {

                    if (!mRollList.get(j).contentEquals(AttendanceActivity.mRoll2I)) {

                        mRollDisplayList.add(mRollList.get(j));

                        mNameDisplayList.add(mNameList.get(j));
                        mPhotoUriDisplayList.add(mPhotoUriList.get(j));
                        mUidDisplayList.add(uIdList.get(j));

                    }
                    else
                    {
                        mRollDisplayList.add(mRollList.get(j));

                        mNameDisplayList.add(mNameList.get(j));
                        mPhotoUriDisplayList.add(mPhotoUriList.get(j));
                        mUidDisplayList.add(uIdList.get(j));

                        j=mRollList.size();
                        i=mRollList.size();
                    }
                }
            }

        }

    }


    private void timeLoader(){

        Calendar calendar = Calendar.getInstance();
        Date date = new Date(calendar.getTimeInMillis());

        formattedDate = formatDate(date);


    }

    private String formatDate(Date dateObject) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("LLL dd, yyyy");
        return dateFormat.format(dateObject);
    }



    private class MyPageChangeListener extends ViewPager.SimpleOnPageChangeListener {
        @Override
        public void onPageSelected(int position) {
            focusedPage = position;
        }
    }



}
