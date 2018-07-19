package com.minorproject.admin.sas;


import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.FirebaseDatabase;


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
    private TextView mFacultyTV;
    private TextView mYearTV;
    private ImageView mImageView;
    private SharedPreferences mSharedPref;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        getActivity().setTitle("Profile Manager");

        if(!MainActivity.isPersistenceOn)
        {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            MainActivity.isPersistenceOn=true;
        }

        mSharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());



        mName1TV = root.findViewById(R.id.profileNameView1);
        mName2TV = root.findViewById(R.id.profileNameView2);
        mEmailTV = root.findViewById(R.id.profileEmailView);
        mStatusTV = root.findViewById(R.id.profileStatusView);
        mUidTV = root.findViewById(R.id.profileUidView);
        mRollTV = root.findViewById(R.id.profileRollView);
        mFacultyTV = root.findViewById(R.id.profileFacultyView);
        mImageView = root.findViewById(R.id.profileImageView);
        mYearTV = root.findViewById(R.id.profileYearView);


        FloatingActionButton fab = root.findViewById(R.id.profileFAB);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ProfileActivity.class);
                startActivity(intent);
            }
        });


        Glide
                .with(getActivity())
                .load(Uri.parse(mSharedPref.getString(getString(R.string.PHOTO_URL),""))) // the uri you got from Firebase
                .centerCrop()
                .into(mImageView); //Your imageView variable

        if(MainActivity.UserInstanceForFragment()!=null) {
            mName1TV.setText(mSharedPref.getString(getString(R.string.NAME), MainActivity.UserInstanceForFragment().getDisplayName()));
            mName2TV.setText(mSharedPref.getString(getString(R.string.NAME), MainActivity.UserInstanceForFragment().getDisplayName()));
            mEmailTV.setText(MainActivity.UserInstanceForFragment().getEmail());
            mUidTV.setText(MainActivity.UserInstanceForFragment().getUid());
            fab.setVisibility(View.VISIBLE);
        }
        else{
            mName1TV.setText(mSharedPref.getString(getString(R.string.NAME), ""));
            mName2TV.setText(mSharedPref.getString(getString(R.string.NAME), ""));
            fab.setVisibility(View.GONE);
        }
        mRollTV.setText(mSharedPref.getString(getString(R.string.ROLL),"0"));
        mFacultyTV.setText(mSharedPref.getString(getString(R.string.FACULTY),"Invalid"));
        mYearTV.setText(mSharedPref.getString(getString(R.string.YEAR),""));

        if(mSharedPref.getInt(getString(R.string.PRIORITY),0)==0)
            mStatusTV.setText("Student");
        else if(mSharedPref.getInt(getString(R.string.PRIORITY),0)==1)
            mStatusTV.setText("Teacher");
        else if(mSharedPref.getInt(getString(R.string.PRIORITY),0)==2)
            mStatusTV.setText("Admin");


            return root;
    }

}
