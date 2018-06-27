package com.minorproject.admin.sas;


import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class DashboardFragment extends Fragment {


    NavigationView navigationView;
    BottomNavigationView bottomNav;
    private ImageButton mProfileButon;
    private ImageButton mNoticeButon;
    private ImageButton mPerformanceButon;



    public DashboardFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_dashboard, container, false);

        navigationView = rootView.findViewById(R.id.nav_view);
        bottomNav = rootView.findViewById(R.id.bottom_navigation);


        mProfileButon = rootView.findViewById(R.id.profileButton);
        mNoticeButon = rootView.findViewById(R.id.noticeButton);
        mPerformanceButon = rootView.findViewById(R.id.performanceButton);


        mProfileButon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ProfileFragment()).commit();
            }
        });

        mNoticeButon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                  getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new NewsFragment()).commit();

            }
        });

        mPerformanceButon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new PerformanceFragment()).commit();
            }
        });

        return rootView;

    }




}
