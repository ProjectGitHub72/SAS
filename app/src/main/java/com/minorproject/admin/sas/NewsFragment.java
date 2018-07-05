package com.minorproject.admin.sas;


import android.content.Intent;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class NewsFragment extends Fragment {


    private static final String ADMIN_EMAIL = "bhattadavid@gmail.com";
    private String mSenderName;


    private ListView mNewsListView;
    private NewsMessageAdapter mNewsAdapter;
    private ProgressBar mProgressBar;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mNewsDatabaseReference;
    private ChildEventListener mChildEventListener;


    private static loginInfo_Collector mLoginResultObject;
    private boolean isAdmin = false;


    public NewsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_news, container, false);


        if (!MainActivity.isPersistenceOn) {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            MainActivity.isPersistenceOn = true;
        }

        mFirebaseDatabase = FirebaseDatabase.getInstance();

        mNewsDatabaseReference = mFirebaseDatabase.getReference()
                .child(mLoginResultObject.getFaculty_symbol())
                .child(mLoginResultObject.getYear())
                .child("notice_node");


        checkPriority();

        mSenderName = mLoginResultObject.getName();


        // Initialize references to views
        mProgressBar = rootView.findViewById(R.id.progressBar);
        mNewsListView = rootView.findViewById(R.id.newsListView);


        // Initialize message ListView and its adapter
        List<NewsMessageInfoCollector> newsMessages = new ArrayList<>();
        mNewsAdapter = new NewsMessageAdapter(getActivity(), R.layout.item_news_message, newsMessages);
        mNewsListView.setAdapter(mNewsAdapter);


        if (isAdmin) {

            rootView.findViewById(R.id.fab).setVisibility(View.VISIBLE);

            // Setup FAB to open EditorActivity
            FloatingActionButton fab = rootView.findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), NewsAdderActivity.class);
                    intent.putExtra("UserValue", mSenderName);
                    startActivity(intent);
                }
            });

        } else {
            rootView.findViewById(R.id.fab).setVisibility(View.GONE);
        }


        attachDatabaseReadListener();

        getActivity().setTitle("Notice");
        return rootView;

    }

    private void checkPriority() {

        mLoginResultObject = loginActivity.login_result();
        if (mLoginResultObject.getPriority_level() == 0) {
            isAdmin = false;
        } else
            isAdmin = true;

    }


    private void attachDatabaseReadListener() {


        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                    NewsMessageInfoCollector newsMessage = dataSnapshot.getValue(NewsMessageInfoCollector.class);
                    mNewsAdapter.add(newsMessage);
                    mNewsListView.smoothScrollToPosition(mNewsAdapter.getCount() - 1);
                    if (mNewsAdapter != null)
                        mProgressBar.setVisibility(ProgressBar.INVISIBLE);

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

            mNewsDatabaseReference.addChildEventListener(mChildEventListener);
        }
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        if (mChildEventListener != null) {
//            mNewsDatabaseReference.addChildEventListener(mChildEventListener);
//
//        }
//    }
}
