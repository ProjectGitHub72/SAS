package com.minorproject.admin.sas;


import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;


/**
 * A simple {@link Fragment} subclass.
 */
public class DashboardFragment extends Fragment {


    private CardView mDashboard;
    private CardView mNotice;
    private CardView mPerformance;
    private CardView mProfile;
    private CardView mLogout;

    MyInterface mCallback;


    public DashboardFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_dashboard, container, false);

        getActivity().setTitle("Dashboard");


        mDashboard = rootView.findViewById(R.id.DashboardCV);
        mNotice = rootView.findViewById(R.id.NoticeCV);
        mPerformance = rootView.findViewById(R.id.PerformanceCV);
        mProfile = rootView.findViewById(R.id.ProfileCV);
        mLogout = rootView.findViewById(R.id.LogoutCV);



        mDashboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mCallback.setNAvFromFrag("DASHBOARD");
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new DashboardFragment()).commit();
            }
        });

        mProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mCallback.setNAvFromFrag("PROFILE");
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ProfileFragment()).commit();
            }
        });

        mNotice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mCallback.setNAvFromFrag("NOTICE");
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new NewsFragment()).commit();

            }
        });

        mPerformance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mCallback.setNAvFromFrag("PERFORMANCE");
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new PerformanceFragment()).commit();
            }
        });

        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mCallback.setNAvFromFrag("DASHBOARD");
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new DashboardFragment()).commit();
                FirebaseAuth.getInstance().signOut();
            }
        });

        return rootView;

    }


    public interface MyInterface {
        void setNAvFromFrag(String navItem);
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof MyInterface) {
            mCallback = (MyInterface) activity;
        } else {
            throw new ClassCastException(activity + " must implement MyInterface");
        }
    }


}