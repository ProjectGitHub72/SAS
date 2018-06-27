package com.minorproject.admin.sas;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.util.List;

public class AuthorizeAdapter extends ArrayAdapter<AuthorizeInfoCollector> {

    private String mUserNameAu;
    private String mUserEmailAu;
    private List<String> mAdminList;
    private List<String> mTeacherList;
    private boolean isAdminButtonOn;
    private boolean isTeacherButtonOn;

    private FirebaseDatabase mFirebaseDatabaseAu;
    private DatabaseReference mAdminDbReferenceAdapter;
    private DatabaseReference mTeacherDbReferenceAdapter;




    public AuthorizeAdapter(Context context, int resource, List<AuthorizeInfoCollector> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_authorize_page, parent, false);
        }


        TextView mUserNameViewAu = convertView.findViewById(R.id.userNameViewAu);
        TextView mUserEmailViewAu = convertView.findViewById(R.id.userEmailViewAu);

        //TODO:check
        final Button mAdminButton = convertView.findViewById(R.id.adminButtonAu);
        final Button mTeacherButton = convertView.findViewById(R.id.teacherButtonAu);

        mFirebaseDatabaseAu = FirebaseDatabase.getInstance();


        mAdminDbReferenceAdapter = mFirebaseDatabaseAu.getReference().child("Permissions").child("Admin");
        mTeacherDbReferenceAdapter = mFirebaseDatabaseAu.getReference().child("Permissions").child("Teacher");

        AuthorizeInfoCollector authorizeInfo = getItem(position);
        mUserNameAu = authorizeInfo.getUserName();
        mUserEmailAu = authorizeInfo.getUserEmail();

        mUserNameViewAu.setText(mUserNameAu);
        mUserEmailViewAu.setText(mUserEmailAu);

        mAdminList = AuthorizeFragment.infoForAdminButton();
        mTeacherList = AuthorizeFragment.infoForTeacherButton();


        for (int index=0;index<mAdminList.size();index++) {

            if (mAdminList.get(index).contentEquals(mUserEmailAu)) {
                mAdminButton.setEnabled(true);

                mTeacherButton.setEnabled(false);
                isAdminButtonOn=true;
                isTeacherButtonOn=false;
            }
//            else {
//                mAdminButton.setEnabled(false);
//                isAdminButtonOn=false;
//
//            }
        }

        for (int index=0;index<mTeacherList.size();index++) {

            if (mTeacherList.get(index).contentEquals(mUserEmailAu)) {
                mTeacherButton.setEnabled(true);
                mAdminButton.setEnabled(false);
                isTeacherButtonOn=true;
                isAdminButtonOn=false;
            }
//            else {
//                mTeacherButton.setEnabled(false);
//                isTeacherButtonOn=false;
//
//            }
        }



        mAdminButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                for (int index=0;index<mAdminList.size();index++) {

                    if (mAdminList.get(index).contentEquals(mUserEmailAu)) {
                        mAdminButton.setEnabled(true);
                        isAdminButtonOn = true;
                        mTeacherButton.setEnabled(false);
                        isTeacherButtonOn=false;
                    }
                }

//                if(!isAdminButtonOn) {
//
//                        mAdminList.add(mUserEmailAu);
//                        mAdminDbReferenceAdapter.push().setValue(mUserEmailAu);
//                        mAdminButton.setEnabled(true);
//                        isAdminButtonOn=true;
//                    mTeacherButton.setEnabled(false);
//                    isTeacherButtonOn=false;
//                    }


                        if(mTeacherButton.isEnabled()){
                            mTeacherButton.setEnabled(false);
                            isTeacherButtonOn=false;

                            mAdminButton.setEnabled(true);
                            isAdminButtonOn=true;

                            for (int index=0;index<mTeacherList.size();index++) {

                                if (mTeacherList.get(index).contentEquals(mUserEmailAu)) {
                                    mTeacherList.remove(mUserEmailAu);

                                }
                            }


                            mTeacherDbReferenceAdapter.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    Iterable<DataSnapshot> teacherChildren = dataSnapshot.getChildren();
                                    for(DataSnapshot teacherLocalList : teacherChildren){

                                        if((String.valueOf(teacherLocalList.getValue())).contentEquals(mUserEmailAu))
                                        teacherLocalList.getRef().setValue("");

                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                    Toast.makeText(getContext(),"Teacher"+ databaseError.toString(), Toast.LENGTH_SHORT).show();

                                }
                            });

                        }
                    }



        });


        mTeacherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                for (int index=0;index<mTeacherList.size();index++) {

                    if (mTeacherList.get(index).contentEquals(mUserEmailAu)) {
                        mTeacherButton.setEnabled(true);
                        isTeacherButtonOn = true;

                        mAdminButton.setEnabled(false);
                        isAdminButtonOn=false;
                    }
                }

//                if(!isTeacherButtonOn) {
//
//                    mTeacherList.add(mUserEmailAu);
//                    mTeacherDbReferenceAdapter.push().setValue(mUserEmailAu);
//                    mTeacherButton.setEnabled(true);
//                    isTeacherButtonOn=true;
//                }


                if(mAdminButton.isEnabled()){
                    mAdminButton.setEnabled(false);
                    isAdminButtonOn=false;

                    mTeacherButton.setEnabled(true);
                    isTeacherButtonOn=true;

                    for (int index=0;index<mAdminList.size();index++) {

                        if (mAdminList.get(index).contentEquals(mUserEmailAu)) {
                            mAdminList.remove(mUserEmailAu);

                        }
                    }


                    mAdminDbReferenceAdapter.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            Iterable<DataSnapshot> adminChildren = dataSnapshot.getChildren();
                            for(DataSnapshot adminLocalList : adminChildren){

                                if((String.valueOf(adminLocalList.getValue())).contentEquals(mUserEmailAu))
                                    adminLocalList.getRef().removeValue();

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                            Toast.makeText(getContext(),"Admin"+ databaseError.toString(), Toast.LENGTH_SHORT).show();

                        }
                    });

                }
            }



        });


        return convertView;


    }

}