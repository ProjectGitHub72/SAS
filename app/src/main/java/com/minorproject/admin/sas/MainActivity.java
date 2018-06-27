package com.minorproject.admin.sas;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {



    private static final int RC_SIGN_IN = 1;
    private static boolean noInternet =true;

    private static FirebaseUser mUser;
    private static String mUsername;
    private static String mUserEmail;
    private static Uri mUserPhotoUri;
    private static Fragment selectedFragment;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseDatabase mFirebaseDatabaseAu;
    private DatabaseReference mAuthorizeDatabaseReferenceAu;
    private DatabaseReference mAdminDbReference;

    private List<String> mAdminList = new ArrayList<>();


    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ImageView mUserImageView;
    private TextView mUserNameView;
    private TextView mUserEmailView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        loadViewsAndData();


        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (!(networkInfo != null && networkInfo.isConnected())) {
            NetworkNotAvailable();
        }
        else {
            NetworkAvailable();
        }

    }


    private void loadViewsAndData(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        mFirebaseDatabaseAu = FirebaseDatabase.getInstance();

        mAuthorizeDatabaseReferenceAu = mFirebaseDatabaseAu.getReference().child("Authorize").child("userIDs");

        mAdminDbReference = mFirebaseDatabaseAu.getReference().child("Permissions").child("Admin");


        mSwipeRefreshLayout = findViewById(R.id.refreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Toast.makeText(MainActivity.this, "Refreshing", Toast.LENGTH_SHORT).show();
                updateUI();
            }
        });


        selectedFragment =new DashboardFragment();
        setDefaultViews();
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.refresh_menu) {

            mSwipeRefreshLayout.setRefreshing(true);
            Toast.makeText(this, "Refreshing", Toast.LENGTH_SHORT).show();
            updateUI();

            return true;
        }


        return super.onOptionsItemSelected(item);
    }



    @Override
    protected void onResume() {
        super.onResume();
        if(!noInternet)
            mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }

    }

//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if(requestCode ==RC_SIGN_IN) {
//            if (resultCode == RESULT_OK && data != null) {
//
//                Toast.makeText(this, "Signing In", Toast.LENGTH_SHORT).show();
//                setDefaultViews();
//
//
//            } else if (resultCode == RESULT_CANCELED ) {
//                Toast.makeText(this, "App Exit", Toast.LENGTH_SHORT).show();
//                finish();
//            }
//        }
//
//
//    }



    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        //TODO
        // Handle bottom_navigation view item clicks here.
        int id = item.getItemId();
        int bottomSelectedItemIndex;
         selectedFragment=null;
        BottomNavigationView bottomNavigationView =findViewById(R.id.bottom_navigation);



        for(bottomSelectedItemIndex=0;bottomSelectedItemIndex<=2;bottomSelectedItemIndex++)
            bottomNavigationView.getMenu().getItem(bottomSelectedItemIndex).setCheckable(true);


        if (id == R.id.nav_dashboard) {
            bottomNavigationView.getMenu().getItem(1).setChecked(true);
            selectedFragment = new DashboardFragment();

        } else if (id == R.id.nav_news) {
            bottomNavigationView.getMenu().getItem(2).setChecked(true);
            selectedFragment = new NewsFragment();


        } else if (id == R.id.nav_performance) {
            bottomNavigationView.getMenu().getItem(0).setChecked(true);
            selectedFragment = new PerformanceFragment();


        } else {
            for (bottomSelectedItemIndex = 0; bottomSelectedItemIndex <= 2; bottomSelectedItemIndex++)
                bottomNavigationView.getMenu().getItem(bottomSelectedItemIndex).setCheckable(false);


            if (id == R.id.nav_result) {

                selectedFragment = new ResultFragment();

            } else if (id == R.id.nav_attendance) {

                selectedFragment = new AttendanceFragment();

            } else if (id == R.id.nav_authorize) {

                selectedFragment = new AuthorizeFragment();

            } else if (id == R.id.nav_profile) {

                selectedFragment = new ProfileFragment();

            } else if (id == R.id.nav_logout) {

                AttachFragmentToItem(new DashboardFragment());
                AuthUI.getInstance().signOut(this);
                return true;
            }
        }
        DrawerLayout drawer =  findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        AttachFragmentToItem(selectedFragment);
        return true;
    }



    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                     selectedFragment = null;
                    int bottomSelectedItemIndex;
                    NavigationView navigationView =findViewById(R.id.nav_view);
                    BottomNavigationView bottomNavigationView =findViewById(R.id.bottom_navigation);


                    for(bottomSelectedItemIndex=0;bottomSelectedItemIndex<=2;bottomSelectedItemIndex++)
                        bottomNavigationView.getMenu().getItem(bottomSelectedItemIndex).setCheckable(true);


                    switch (item.getItemId()) {
                        case R.id.nav_bottom_performance:
                            selectedFragment = new PerformanceFragment();
                            navigationView.getMenu().getItem(2).setChecked(true);

                            break;
                        case R.id.nav_bottom_dashboard:
                            selectedFragment = new DashboardFragment();
                            navigationView.getMenu().getItem(0).setChecked(true);

                            break;
                        case R.id.nav_bottom_news:
                            selectedFragment = new NewsFragment();
                            navigationView.getMenu().getItem(1).setChecked(true);

                            break;
                    }

                    AttachFragmentToItem(selectedFragment);

                    return true;
                }
            };

    private void onSignedInInitializeNavSide(){


        TextView mWelcome = findViewById(R.id.dashWelcomeView2);
       if(mWelcome!=null)
        mWelcome.setText(mUsername);


        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);


        mUserImageView = headerView.findViewById(R.id.userImageView);
        mUserNameView = headerView.findViewById(R.id.userNameView);
        mUserEmailView = headerView.findViewById(R.id.userEmailView);

        mUserNameView.setText(mUsername);
        mUserEmailView.setText(mUserEmail);

        Glide
                .with(this)
                .load(mUserPhotoUri) // the uri you got from Firebase
                .centerCrop()
                .into(mUserImageView); //Your imageView variable


    }

    private void setDefaultViews(){
        int bottomSelectedItemIndex;

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.getMenu().getItem(0).setChecked(true);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        for(bottomSelectedItemIndex=0;bottomSelectedItemIndex<=2;bottomSelectedItemIndex++)
            bottomNavigationView.getMenu().getItem(bottomSelectedItemIndex).setCheckable(true);

        bottomNavigationView.getMenu().getItem(1).setChecked(true);

        AttachFragmentToItem(selectedFragment);

    }

    private void AttachFragmentToItem(Fragment selectedFragment){


        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                selectedFragment).commit();

    }

    private void DisableBothNavigation(){
        int SelectedItemIndex;

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        NavigationView navigationView = findViewById(R.id.nav_view);

        for (SelectedItemIndex = 0; SelectedItemIndex <= 4; SelectedItemIndex++)
            navigationView.getMenu().getItem(SelectedItemIndex).setEnabled(false);


        for (SelectedItemIndex = 0; SelectedItemIndex <= 2; SelectedItemIndex++)
            bottomNavigationView.getMenu().getItem(SelectedItemIndex).setEnabled(false);

    }

    private void EnableBothNavigation(){
        int SelectedItemIndex;

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        NavigationView navigationView = findViewById(R.id.nav_view);

        for (SelectedItemIndex = 0; SelectedItemIndex <= 4; SelectedItemIndex++)
            navigationView.getMenu().getItem(SelectedItemIndex).setEnabled(true);


        for (SelectedItemIndex = 0; SelectedItemIndex <= 2; SelectedItemIndex++)
            bottomNavigationView.getMenu().getItem(SelectedItemIndex).setEnabled(true);

    }


    private void NetworkNotAvailable(){

        noInternet=true;
     Toast.makeText(this, "CONNECTION NOT AVAILABLE", Toast.LENGTH_SHORT).show();
        DisableBothNavigation();

    }


    public void NetworkAvailable(){

        EnableBothNavigation();
        noInternet = false;
        mFirebaseAuth = FirebaseAuth.getInstance();

        databaseWork();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                mUser = firebaseAuth.getCurrentUser();
                if (mUser != null) {

                    // Name, email address, and profile photo Url
                    mUsername = mUser.getDisplayName();
                    mUserEmail = mUser.getEmail();
                    mUserPhotoUri = mUser.getPhotoUrl();

                    onSignedInInitializeNavSide();
                    databaseWork();

                    AuthorizeInfoCollector authorizeInfo = new AuthorizeInfoCollector(mUsername,mUserEmail,mUser.getUid());
                    mAuthorizeDatabaseReferenceAu.child("BCT_2072").child(mUser.getUid()).setValue(authorizeInfo);


                } else {
                    //Signed out


                    startActivity(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.EmailBuilder().build(),
                                            new AuthUI.IdpConfig.GoogleBuilder().build()
                                    ))
                                    .build());
                }

            }
        };


    }



private void updateUI(){

    ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

    mSwipeRefreshLayout.setRefreshing(false);

    if (!(networkInfo != null && networkInfo.isConnected())) {

        NetworkNotAvailable();
    }
    else {


        NetworkAvailable();

    }
}


    public static FirebaseUser UserInstanceForFragment(){

    return mUser;

    }


    private void databaseWork(){



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

                Toast.makeText(MainActivity.this,"Admin"+ databaseError.toString(), Toast.LENGTH_SHORT).show();

            }
        });

        NavigationView navigationView = findViewById(R.id.nav_view);

if(mAdminList!=null) {
    for (int index = 0; index < mAdminList.size(); index++) {
        if (mAdminList.get(index).contentEquals(mUserEmail)) {

            navigationView.getMenu().findItem(R.id.nav_authorize).setVisible(true);
            break;

        } else
            navigationView.getMenu().findItem(R.id.nav_authorize).setVisible(false);

    }
}

    }



}
