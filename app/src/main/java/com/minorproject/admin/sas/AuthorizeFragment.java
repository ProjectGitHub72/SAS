package com.minorproject.admin.sas;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class AuthorizeFragment extends Fragment {


    private ListView mAuthorizeListView;
    private AuthorizeAdapter mAuthorizeAdapter;
    private ProgressBar mProgressBarAu;

    private FirebaseDatabase mFirebaseDatabaseAu;
    private DatabaseReference mAuthorizeDatabaseReferenceAu;
    private ChildEventListener mChildEventListenerAu;

    private DatabaseReference mAdminDbReference;
    private ChildEventListener mAdminChildEventListener;

    private DatabaseReference mTeacherDbReference;
    private ChildEventListener mTeacherChildEventListener;

    private static List<String> mAdminList = new ArrayList<>();
    private static List<String> mTeacherList = new ArrayList<>();


    public AuthorizeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       View rootViewAu = inflater.inflate(R.layout.fragment_authorize, container, false);

        mFirebaseDatabaseAu = FirebaseDatabase.getInstance();
        mAuthorizeDatabaseReferenceAu = mFirebaseDatabaseAu.getReference().child("Authorize").child("userIDs").child("BCT_2072");

        mAdminDbReference = mFirebaseDatabaseAu.getReference().child("Permissions").child("Admin");
        mTeacherDbReference = mFirebaseDatabaseAu.getReference().child("Permissions").child("Teacher");

        // Initialize references to views
        mProgressBarAu =  rootViewAu.findViewById(R.id.progressBarAu);
        mAuthorizeListView =  rootViewAu.findViewById(R.id.authorizeListView);

//        mTeacherList=null;
//        mAdminList=null;


        // Initialize message ListView and its adapter
        List<AuthorizeInfoCollector> authorizeInfo = new ArrayList<>();
        mAuthorizeAdapter = new AuthorizeAdapter(getActivity(), R.layout.item_authorize_page, authorizeInfo);
        mAuthorizeListView.setAdapter(mAuthorizeAdapter);


        mAdminDbReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Iterable<DataSnapshot> adminChildren = dataSnapshot.getChildren();
                for(DataSnapshot adminLocalList : adminChildren){

                    mAdminList.add(String.valueOf(adminLocalList.getValue()));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Toast.makeText(getContext(), databaseError.toString(), Toast.LENGTH_SHORT).show();

            }
        });



        mTeacherDbReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Iterable<DataSnapshot> teacherChildren = dataSnapshot.getChildren();
                for(DataSnapshot teacherLocalList : teacherChildren){

                    mTeacherList.add(String.valueOf(teacherLocalList.getValue()));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Toast.makeText(getContext(), databaseError.toString(), Toast.LENGTH_SHORT).show();

            }
        });




        attachAdminDatabaseReadListener();
        attachTeacherDatabaseReadListener();
        attachAuDatabaseReadListener();



        return rootViewAu;

    }


    private void attachAuDatabaseReadListener() {


        if (mChildEventListenerAu == null) {
            mChildEventListenerAu = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                    AuthorizeInfoCollector authorizeInfo = dataSnapshot.getValue(AuthorizeInfoCollector.class);
                    mAuthorizeAdapter.add(authorizeInfo);
                    mAuthorizeListView.smoothScrollToPosition(mAuthorizeAdapter.getCount()-1);
                    if(mAuthorizeAdapter!=null)
                        mProgressBarAu.setVisibility(ProgressBar.INVISIBLE);

                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };

            mAuthorizeDatabaseReferenceAu.addChildEventListener(mChildEventListenerAu);
        }
    }


    private void attachAdminDatabaseReadListener() {


        if (mAdminChildEventListener == null) {
            mAdminChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                    String authorizeInfo = String.valueOf(dataSnapshot.getValue());

                    for(int index=0;index<mAdminList.size();index++){

                        if(mAdminList.get(index).contentEquals(authorizeInfo))
                            break;
                        else if(index==mAdminList.size()-1)
                            mAdminList.add(authorizeInfo);


                    }

                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                    String authorizeInfo = String.valueOf(dataSnapshot.getValue());

                    for(int index=0;index<mAdminList.size();index++){

                        if(mAdminList.get(index).contentEquals(authorizeInfo)) {
                            mAdminList.remove(authorizeInfo);
                            break;
                        }

                    }

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };

            mAdminDbReference.addChildEventListener(mAdminChildEventListener);
        }
    }

    private void attachTeacherDatabaseReadListener() {


        if (mTeacherChildEventListener == null) {
            mTeacherChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                    String authorizeInfo = String.valueOf(dataSnapshot.getValue());

                    for(int index=0;index<mTeacherList.size();index++){

                        if(mTeacherList.get(index).contentEquals(authorizeInfo))
                            break;
                        else if(index==mTeacherList.size()-1)
                            mTeacherList.add(authorizeInfo);

                    }

                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                    String authorizeInfo = String.valueOf(dataSnapshot.getValue());

                    for (int index = 0; index < mTeacherList.size(); index++) {

                        if (mTeacherList.get(index).contentEquals(authorizeInfo)) {
                            mTeacherList.remove(authorizeInfo);
                            break;

                        }

                    }
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };

            mTeacherDbReference.addChildEventListener(mTeacherChildEventListener);
        }
    }

    public static List<String> infoForAdminButton(){

        return mAdminList;
    }

    public static List<String> infoForTeacherButton(){

        return  mTeacherList;
    }


}
