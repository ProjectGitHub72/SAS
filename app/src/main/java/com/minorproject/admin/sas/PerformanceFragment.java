package com.minorproject.admin.sas;


import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;


///**
// * A simple {@link Fragment} subclass.
// * Activities that contain this fragment must implement the
// * {@link PerformanceFragment.OnFragmentInteractionListener} interface
// * to handle interaction events.
// */



public class PerformanceFragment extends Fragment {



//    private String postUrl = "https://firebasestorage.googleapis.com/v0/b/sas-android-73b28.appspot.com/o/BCTQ_III_IIF_opt.pdf?alt=media&token=7f319d14-efc5-4e58-9255-8e3fb35456ba";
   private String postUrl;
    private ProgressBar mProgressBar;
    private TextView mNoDataView;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference mUserInfoRef;
    private DatabaseReference mDblinkRef;
    private DatabaseReference actualUrlLoadRef;

    private String DblinkKey;
    private String DbLinkValue;
    private String uniqueIdentifier;

    public PerformanceFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_performance, container, false);

        mProgressBar = root.findViewById(R.id.progressBar_performance);
        mNoDataView = root.findViewById(R.id.performanceNoDataView);

        mProgressBar.setVisibility(View.VISIBLE);
        mNoDataView.setVisibility(View.GONE);


        getActivity().setTitle("Performance Viewer");

            if(!MainActivity.noInternet)
                findDblinkKey(root);
            else{

                mProgressBar.setVisibility(View.GONE);
                mNoDataView.setVisibility(View.VISIBLE);
            }


        return root;
    }

    private void findDblinkKey(View root) {

        final View rootView = root;

        firebaseDatabase = FirebaseDatabase.getInstance();
        if(MainActivity.UserInstanceForFragment()!=null) {
            mUserInfoRef = firebaseDatabase.getReference()
                    .child("app")
                    .child("users")
                    .child(MainActivity.UserInstanceForFragment().getUid());

            mUserInfoRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getValue()!=null) {
                        uniqueIdentifier = dataSnapshot.child("customID").getValue().toString();

                        for (DataSnapshot snapshot : dataSnapshot.child("accountLinks").getChildren())
                            DblinkKey = snapshot.getKey();

                        loadDbLinkValue(rootView);

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                    Toast.makeText(getActivity(), "Database Key not found", Toast.LENGTH_SHORT).show();
                    mProgressBar.setVisibility(View.GONE);
                    mNoDataView.setVisibility(View.VISIBLE);
                }
            });


        }

    }

    private void loadDbLinkValue(View root) {

        final View rootView = root;
        mDblinkRef = firebaseDatabase.getReference()
                .child("app")
                .child("database_link")
                .child(DblinkKey);

        mDblinkRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.getValue()!=null)
                DbLinkValue = dataSnapshot.getValue().toString();

                loadUserDownloadUrl(rootView);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(getActivity(), "Database Link Value not available", Toast.LENGTH_SHORT).show();

                mProgressBar.setVisibility(View.GONE);
                mNoDataView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void loadUserDownloadUrl( View root) {

        final View rootView = root;
        actualUrlLoadRef = firebaseDatabase.getReference()
                .child("app")
                .child("app_data")
                .child(DbLinkValue)
                .child("users_data")
                .child("students")
                .child(uniqueIdentifier)
                .child("performance");

        actualUrlLoadRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.getValue()!=null)
                postUrl = dataSnapshot.getValue().toString();

                mProgressBar.setVisibility(View.VISIBLE);
                mNoDataView.setVisibility(View.GONE);

                DownloadAndView(rootView);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(getActivity(), "No Url Found", Toast.LENGTH_SHORT).show();

                mProgressBar.setVisibility(View.GONE);
                mNoDataView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void DownloadAndView(View rootView) {

        final PDFView pdfView = rootView.findViewById(R.id.pdfView);

        if(postUrl!=null) {
            StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(postUrl);
            try {
                final File localFile = File.createTempFile("Doc", "pdf");
                ref.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                        mProgressBar.setVisibility(View.GONE);
                        pdfView.fromFile(localFile).load();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mProgressBar.setVisibility(View.GONE);
                        mNoDataView.setVisibility(View.VISIBLE);
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            mProgressBar.setVisibility(View.GONE);
            mNoDataView.setVisibility(View.VISIBLE);
        }
    }


}
