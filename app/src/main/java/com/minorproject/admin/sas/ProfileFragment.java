package com.minorproject.admin.sas;


import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    private TextView mName1TV;
    private TextView mName2TV;
    private TextView mEmailTV;
    private TextView mStatusTV;
    private TextView mUidTV;
    private TextView mRollTV;
    private ImageView mImageView;
    private TextView mDbLinkView;

    private String mNameShared;
    private String mPhotoShared;
    private String mIdentifierShared;
    private int mPriority;


    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDbRef;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        getActivity().setTitle("Profile Manager");

        if(!MainActivity.isPersistenceOn)
        {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            MainActivity.isPersistenceOn=true;
        }


        mName1TV = root.findViewById(R.id.profileNameView1);
        mName2TV = root.findViewById(R.id.profileNameView2);
        mEmailTV = root.findViewById(R.id.profileEmailView);
        mStatusTV = root.findViewById(R.id.profileStatusView);
        mUidTV = root.findViewById(R.id.profileUidView);
        mRollTV = root.findViewById(R.id.profileRollView);
        mImageView = root.findViewById(R.id.profileImageView);
        mDbLinkView = root.findViewById(R.id.profileDbLinkView);


        FloatingActionButton fab = root.findViewById(R.id.profileFAB);
        fab.setVisibility(View.GONE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ProfileActivity.class);
                intent.putExtra("NAME",mNameShared);
                intent.putExtra("IMAGE",mPhotoShared);
                intent.putExtra("IDENTIFIER",mIdentifierShared);
                intent.putExtra("PRIORITY",mPriority);

                startActivity(intent);
            }
        });

        if(MainActivity.UserInstanceForFragment()!=null) {

            fab.setVisibility(View.VISIBLE);
            loadDatabase();
        }
        else
            fab.setVisibility(View.GONE);

            return root;
    }



    private void loadDatabase(){

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDbRef = mFirebaseDatabase.getReference().child("app")
                .child("users")
                .child(MainActivity.UserInstanceForFragment().getUid());
        mDbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.getValue()!=null){

                  mNameShared = dataSnapshot.child("info").child("firstName").getValue().toString()
                          + " "
                          + dataSnapshot.child("info").child("lastName").getValue().toString();

                    mName1TV.setText(mNameShared);
                    mName2TV.setText(mNameShared);

                    if(dataSnapshot.child("info").child("photoUrl").exists())
                    mPhotoShared = dataSnapshot.child("info").child("photoUrl").getValue().toString();

                    if(mPhotoShared!=null) {
                        Glide
                                .with(getActivity())
                                .load(Uri.parse(mPhotoShared))
                                .centerCrop()
                                .placeholder(R.drawable.ic_launcher_foreground11)
                                .into(mImageView); //Your imageView variable
                    }


                    mRollTV.setText(dataSnapshot.child("customID").getValue().toString());

                    mUidTV.setText(MainActivity.UserInstanceForFragment().getUid());
                    mEmailTV.setText(MainActivity.UserInstanceForFragment().getEmail());

//                    String priority_node = ((dataSnapshot.child("accountLinks").getValue().toString()).split("="))[1];
//                    if(priority_node.contains("0")) {
//                        mStatusTV.setText("Admin");
//                        mPriority = 0;
//                    }
//                    else if(priority_node.contains("1")) {
//                        mStatusTV.setText("Teacher");
//                        mPriority = 1;
//                    }
//                    else if(priority_node.contains("2")) {
//                        mStatusTV.setText("Student");
//                        mPriority = 2;
//                    }

                    for(DataSnapshot snapshot : dataSnapshot.child("accountLinks").getChildren())
                    {
                        mIdentifierShared = snapshot.getKey();
                        mPriority = Integer.parseInt(snapshot.getValue().toString());
                        if(mPriority==0)
                            mStatusTV.setText("Admin");
                        else  if(mPriority==1)
                            mStatusTV.setText("Teacher");
                        else
                            mStatusTV.setText("Student");
                    }
                    mDbLinkView.setText(mIdentifierShared);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


}
