package com.minorproject.admin.sas;

import android.content.Context;
import android.content.Intent;
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


import java.util.Arrays;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {



    private static final int RC_SIGN_IN = 1;
    private static boolean noInternet =true;
    private static String mUsername;
    private static String mUserEmail;
    private static Uri mUserPhotoUri;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;




    private TextView mEmptyStateTextView;
    private ImageView mUserImageView;
    private TextView mUserNameView;
    private TextView mUserEmailView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


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
        bottomNav.getMenu().getItem(1).setChecked(true);

        bottomNav.setOnNavigationItemSelectedListener(navListener);



        //TODO:DONE

        // Get a reference to the ConnectivityManager to check state of network connectivity

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        // If there is a network connection, fetch data
        if (!(networkInfo != null && networkInfo.isConnected())) {

            noInternet=true;
            setContentView(R.layout.activity_main);
            mEmptyStateTextView = findViewById(R.id.empty_view);
            mEmptyStateTextView.setText("CONNECTION NOT AVAILABLE");
        }
        else {

            TextView trialView = findViewById(R.id.trial_view);
            trialView.setText("Welcome!!!");
            noInternet = false;
            mFirebaseAuth = FirebaseAuth.getInstance();

            mAuthStateListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user != null) {

                        // Name, email address, and profile photo Url
                        mUsername = user.getDisplayName();
                        mUserEmail = user.getEmail();
                        mUserPhotoUri = user.getPhotoUrl();

                        onSignedInInitializeNavSide();

                    } else {
                        //Signed out

                        //    onSignedOutCleanup();

                        startActivityForResult(
                                AuthUI.getInstance()
                                        .createSignInIntentBuilder()
                                        .setIsSmartLockEnabled(false)
                                        .setAvailableProviders(Arrays.asList(
                                                new AuthUI.IdpConfig.EmailBuilder().build(),
                                                new AuthUI.IdpConfig.GoogleBuilder().build()
                                        ))
                                        .build(),
                                RC_SIGN_IN);
                    }

                }
            };
        }
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
        if (id == R.id.sign_out_menu) {
            //TODO
            AuthUI.getInstance().signOut(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }









    //TODO



    private void onSignedInInitializeNavSide(){


        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);


        mUserImageView = headerView.findViewById(R.id.userImageView);
        mUserNameView = headerView.findViewById(R.id.userNameView);
        mUserEmailView = headerView.findViewById(R.id.userEmailView);

        mUserNameView.setText(mUsername);
        mUserEmailView.setText(mUserEmail);

        Glide
                .with(this)     //TODO:FOR SINGLE ACTIVITY
                .load(mUserPhotoUri) // the uri you got from Firebase
                .centerCrop()
                .into(mUserImageView); //Your imageView variable


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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode ==RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Signing In", Toast.LENGTH_SHORT).show();


                NavigationView navigationView = findViewById(R.id.nav_view);
                navigationView.getMenu().getItem(0).setChecked(true);

                BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
                bottomNavigationView.getMenu().getItem(1).setChecked(true);



            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "App Exit", Toast.LENGTH_SHORT).show();
                finish();
            }
        }


    }



    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        //TODO
        // Handle bottom_navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_dashboard) {

        } else if (id == R.id.nav_news) {

        } else if (id == R.id.nav_performance) {

        } else if (id == R.id.nav_result) {

        } else if (id == R.id.nav_attendance) {

        } else if (id == R.id.nav_users) {

        } else if (id == R.id.nav_profile) {

        } else if (id == R.id.nav_logout) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }



    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;
                    NavigationView navigationView =findViewById(R.id.nav_view);

                    switch (item.getItemId()) {
                        case R.id.nav_bottom_performance:
                            selectedFragment = new HomeFragment();
                            navigationView.getMenu().getItem(2).setChecked(true);

                            break;
                        case R.id.nav_bottom_dashboard:
                            selectedFragment = new HomeFragment();
                           navigationView.getMenu().getItem(0).setChecked(true);

                            break;
                        case R.id.nav_bottom_news:
                            selectedFragment = new HomeFragment();
                            navigationView.getMenu().getItem(1).setChecked(true);

                            break;
                    }


                    

                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            selectedFragment).commit();

                    return true;
                }
            };


}
