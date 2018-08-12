package com.minorproject.admin.sas;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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

    public ArrayList<Uri> mPhotoUriList = new ArrayList<>();
    public ArrayList<String> mNameList = new ArrayList<>();
    public ArrayList<String> mRollList = new ArrayList<>();

    private ArrayList<String> mRollDisplayList = new ArrayList<>();
    private ArrayList<Uri> mPhotoUriDisplayList = new ArrayList<>();
    private ArrayList<String> mNameDisplayList = new ArrayList<>();

    private Intent mIntent;

    public static MyAppAdapter myAppAdapter;
    public static ViewHolder viewHolder;
    private SwipeFlingAdapterView flingContainer;

    private DatabaseReference mUser_dataAttRef;
    private DatabaseReference mTeacherAttendRef;
    private DataSnapshot mStudentSnapshot;
    private DataSnapshot mTeacherSnapshot;


    private ArrayList<String> mPresentStudentList = new ArrayList<>();

    private String formattedDate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attendance_loader);

        mIntent = getIntent();
        loadIntent();

        setTitle("Attendance Time");

        flingContainer = (SwipeFlingAdapterView) findViewById(R.id.frame);

        mDatabase = FirebaseDatabase.getInstance();

        mUsersInfoRef = mDatabase.getReference()
                .child("app")
                .child("users");


         if(!MainActivity.noInternet)
         setViewContentsFromDb();



        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            @Override
            public void removeFirstObjectInAdapter() {

            }

            @Override
            public void onLeftCardExit(Object dataObject) {
                mNameDisplayList.remove(0);
                mRollDisplayList.remove(0);

                if(mPhotoUriDisplayList.size()!=0)
                    mPhotoUriDisplayList.remove(0);


                myAppAdapter.notifyDataSetChanged();
                //Do something on the left!
                //You also have access to the original object.
                //If you want to use it just cast it (String) dataObject
            }

            @Override
            public void onRightCardExit(Object dataObject) {

                mPresentStudentList.add(mRollDisplayList.get(0));


                mNameDisplayList.remove(0);
                mRollDisplayList.remove(0);

                if(mPhotoUriDisplayList.size()!=0)
                mPhotoUriDisplayList.remove(0);

                myAppAdapter.notifyDataSetChanged();
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {

                if(itemsInAdapter==0)
                atLastAddToDatabase();

            }

            @Override
            public void onScroll(float scrollProgressPercent) {

                View view = flingContainer.getSelectedView();
                view.findViewById(R.id.background).setAlpha(0);
                view.findViewById(R.id.item_swipe_right_indicator).setAlpha(scrollProgressPercent < 0 ? -scrollProgressPercent : 0);
                view.findViewById(R.id.item_swipe_left_indicator).setAlpha(scrollProgressPercent > 0 ? scrollProgressPercent : 0);
            }
        });


        // Optionally add an OnItemClickListener
        flingContainer.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
            @Override
            public void onItemClicked(int itemPosition, Object dataObject) {

                View view = flingContainer.getSelectedView();
                view.findViewById(R.id.background).setAlpha(0);

                myAppAdapter.notifyDataSetChanged();
            }
        });


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


                    sortData();
                    limitRollRange();

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

                myAppAdapter = new MyAppAdapter(mNameDisplayList,mRollDisplayList
                        ,mPhotoUriDisplayList,AttendanceActivity.this);
                flingContainer.setAdapter(myAppAdapter);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(AttendanceActivity.this, "Database Link Failed", Toast.LENGTH_SHORT).show();
            }
        });

    }


    private void sortData(){

        int length = mRollList.size();

        quickSort(0,length-1);

    }

    private void quickSort(int lowerIndex, int higherIndex) {

        int i = lowerIndex;
        int j = higherIndex;
        // calculate pivot number, I am taking pivot as middle index number
        int pivot = Integer.parseInt((mRollList.get(lowerIndex+(higherIndex-lowerIndex)/2)).substring(6,8));
        // Divide into two arrays
        while (i <= j) {

            while (Integer.parseInt((mRollList.get(i)).substring(6,8)) < pivot) {
                i++;
            }
            while (Integer.parseInt((mRollList.get(j)).substring(6,8)) > pivot) {
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


        temp = mNameList.get(i);
        mNameList.set(i, mNameList.get(j));
        mNameList.set(j,temp);

        if(mPhotoUriList.size()!=0 && i<mPhotoUriList.size() && j <mPhotoUriList.size())
            if(mPhotoUriList.get(i)!=null && mPhotoUriList.get(j)!=null) {
                Uri temp2 = mPhotoUriList.get(i);
                mPhotoUriList.set(i, mPhotoUriList.get(j));
                mPhotoUriList.set(j, temp2);
            }
    }


    private void limitRollRange(){
        int i,j;
        for(i=0;i<mRollList.size();i++){

            if(mRollList.get(i).contentEquals(AttendanceActivity.mRoll1I)){

                for(j=i;j<mRollList.size();j++) {

                    if (!mRollList.get(j).contentEquals(AttendanceActivity.mRoll2I)) {

                        mRollDisplayList.add(mRollList.get(j));

                        mNameDisplayList.add(mNameList.get(j));

                        if(mPhotoUriList.size()!=0 && j<mPhotoUriList.size())
                            if(mPhotoUriList.get(j)!=null)
                                mPhotoUriDisplayList.add(mPhotoUriList.get(j));

                    }
                    else
                    {
                        mRollDisplayList.add(mRollList.get(j));

                        mNameDisplayList.add(mNameList.get(j));

                        if(mPhotoUriList.size()!=0 && j<mPhotoUriList.size())
                            if(mPhotoUriList.get(j)!=null)
                                mPhotoUriDisplayList.add(mPhotoUriList.get(j));

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


    private void atLastAddToDatabase(){

        timeLoader();


        mDatabase = FirebaseDatabase.getInstance();
        mUser_dataAttRef = mDatabase.getReference()
                .child("app")
                .child("app_data")
                .child(dbLinkValue)
                .child("users_data")
                .child("students");

        mTeacherAttendRef = mDatabase.getReference()
                .child("app")
                .child("app_data")
                .child(dbLinkValue)
                .child("users_data")
                .child("subjects")
                .child(mSubCodeI)
                .child("attendance")
                .child(selfUniqueIdentifier);


        Toast.makeText(this, "Please Wait...", Toast.LENGTH_LONG).show();
       previousAttendanceRecord();


    }

    private void previousAttendanceRecord() {

        mUser_dataAttRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.getValue()!=null) {
                    mStudentSnapshot = dataSnapshot;

                    mTeacherAttendRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if(dataSnapshot.getValue()!=null) {
                                mTeacherSnapshot = dataSnapshot;
                            }
                            else
                                mTeacherSnapshot = null;

                                startStoringData();


                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                            Toast.makeText(AttendanceActivity.this, "Problem Loading Teacher Record", Toast.LENGTH_SHORT).show();

                        }
                    });


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(AttendanceActivity.this, "Problem Loading Student Record", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void startStoringData() {

        int totalPresentInClass;
        int totalClass;

        if(mPresentStudentList.size()!=0) {
            for (int i = 0; i < mPresentStudentList.size(); i++) {

                if (mStudentSnapshot.child(mPresentStudentList.get(i))
                        .child("attendance")
                        .child(mSubCodeI)
                        .child(selfUniqueIdentifier).exists()) {

                    totalPresentInClass = Integer.parseInt(mStudentSnapshot.child(mPresentStudentList.get(i))
                            .child("attendance")
                            .child(mSubCodeI)
                            .child(selfUniqueIdentifier)
                            .getValue()
                            .toString());

                } else {
                    totalPresentInClass = 0;
                }

                if (mTeacherSnapshot == null)
                    totalClass = 0;
                else
                    totalClass = Integer.parseInt(mTeacherSnapshot.getValue().toString());


                mUser_dataAttRef.child(mPresentStudentList.get(i))
                        .child("attendance")
                        .child(mSubCodeI)
                        .child(formattedDate)
                        .child(selfUniqueIdentifier)
                        .setValue("present");

                mUser_dataAttRef.child(mPresentStudentList.get(i))
                        .child("attendance")
                        .child(mSubCodeI)
                        .child(selfUniqueIdentifier)
                        .setValue(totalPresentInClass+1);

                mTeacherAttendRef.setValue(totalClass+1);




            }
        }


        Intent intent = new Intent(this,MainActivity.class);
        intent.putExtra("FRAGMENT","NONE");
        startActivity(intent);


    }


    public static class ViewHolder {
        public static FrameLayout background;
        public TextView DataText;
        public ImageView cardImage;


    }


    public class MyAppAdapter extends BaseAdapter {


        public Context context;

        private ArrayList<String> mRollDisplayList;
        private ArrayList<Uri> mPhotoUriDisplayList;
        private ArrayList<String> mNameDisplayList;



        private MyAppAdapter(ArrayList<String> NameList,ArrayList<String> RollList,ArrayList<Uri> PhotoList, Context context) {

            this.mPhotoUriDisplayList = PhotoList;
            this.mRollDisplayList = RollList;
            this.mNameDisplayList = NameList;
            this.context = context;
        }

        @Override
        public int getCount() {
            return mRollDisplayList.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            View rowView = convertView;


            if (rowView == null) {

                LayoutInflater inflater = getLayoutInflater();
                rowView = inflater.inflate(R.layout.item, parent, false);
                // configure view holder
                viewHolder = new ViewHolder();
                viewHolder.DataText =  rowView.findViewById(R.id.bookText);
                viewHolder.background =  rowView.findViewById(R.id.background);
                viewHolder.cardImage =  rowView.findViewById(R.id.cardImage);
                rowView.setTag(viewHolder);

            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.DataText.setText("Name: "+mNameDisplayList.get(position) + "\n" + "Roll: "+mRollDisplayList.get(position));

            if(mPhotoUriDisplayList.size()!=0 && position<mPhotoUriDisplayList.size())
            Glide.with(context).load(mPhotoUriDisplayList.get(position)).placeholder(R.drawable.ic_launcher_foreground11).into(viewHolder.cardImage);

            return rowView;
        }
    }


}
