package com.minorproject.admin.sas;


import android.content.Intent;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class NewsFragment extends Fragment {


    private String mSenderName;


    private ListView mNewsListView;
    private NewsMessageAdapter mNewsAdapter;
    private ProgressBar mProgressBar;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mNewsDatabaseReference;
    private DatabaseReference mDBLinkRef;
    private DatabaseReference userDataRef;
    private ChildEventListener mChildEventListener;

    private String DbLink;
    private String DbLinkKeyValue;


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


        if(!MainActivity.noInternet)
        loadDatabase(rootView);



        getActivity().setTitle("Notice");
        return rootView;

    }

    private void loadDatabase(final View rootView) {

        if(MainActivity.UserInstanceForFragment()!=null) {
            mFirebaseDatabase = FirebaseDatabase.getInstance();
            userDataRef = mFirebaseDatabase.getReference()
                    .child("app")
                    .child("users")
                    .child(MainActivity.UserInstanceForFragment().getUid());


            userDataRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                  for(DataSnapshot users : dataSnapshot.child("accountLinks").getChildren())
                    DbLink = users.getKey();

                  mSenderName = dataSnapshot.child("info").child("firstName").getValue().toString()
                          + " " + dataSnapshot.child("info").child("lastName").getValue().toString();

                    String priority_node = ((dataSnapshot.
                            child("accountLinks").getValue().toString()).split("="))[1];
                    if(priority_node.contains("0"))
                        isAdmin = true;
                    else if(priority_node.contains("1"))
                        isAdmin = false;
                    else if(priority_node.contains("2"))
                        isAdmin=false;


                  loadDbLinkDatabase(rootView);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                    Toast.makeText(getActivity(), "Database account Failed", Toast.LENGTH_SHORT).show();
                }
            });


        }


    }

    private void loadDbLinkDatabase(final View rootView) {

        mDBLinkRef = mFirebaseDatabase.getReference()
                .child("app")
                .child("database_link")
                .child(DbLink);

        mDBLinkRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                DbLinkKeyValue = dataSnapshot.getValue().toString();
                loadNewsRefDatabase(rootView);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(getActivity(), "Database Link Failed", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void loadNewsRefDatabase(View rootView) {


        mNewsDatabaseReference = mFirebaseDatabase.getReference()
                .child("app")
                .child("app_data")
                .child(DbLinkKeyValue)
                .child("news_database");

        afterLoadingDatabase(rootView);
    }


    private void afterLoadingDatabase(View rootView){



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
                    intent.putExtra("USERNAME", mSenderName);
                    intent.putExtra("DBKEY",DbLinkKeyValue);
                    startActivity(intent);
                }
            });

        } else {
            rootView.findViewById(R.id.fab).setVisibility(View.GONE);
        }


        attachDatabaseReadListener();
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



}

