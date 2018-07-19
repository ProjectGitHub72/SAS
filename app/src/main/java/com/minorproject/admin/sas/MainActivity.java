package com.minorproject.admin.sas;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
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

import java.util.Arrays;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private static FirebaseUser mUser;
    private static String mUsername;
    private static String mUserEmail;
    private static Uri mUserPhotoUri;


    FirebaseAuth mFirebaseAuth;
    FirebaseAuth.AuthStateListener mAuthStateListener;

    private static final int RC_SIGN_IN = 1;
    private static boolean noInternet = true;
    public static boolean isPersistenceOn = false;
    private boolean newLogin = false;

    private ProgressBar mProgressBar;

    private FirebaseDatabase mFirebaseDatabse;
    private DatabaseReference mUSerListDbRef;

    private SharedPreferences mSharedPref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        if(!isPersistenceOn)
        {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            isPersistenceOn=true;
        }

        setContentView(R.layout.activity_main);

        //TODO:For now
//        signOutScreen();


        loadViewsAndData();
        setDefaultViews();
        checkNetworkConnection();


    }



    private void checkNetworkConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {

            NetworkAvailable();

        } else {
            NetworkNotAvailable();
        }
    }


    private void loadViewsAndData() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);




    }

    private void setDefaultViews() {

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.getMenu().findItem(R.id.nav_dashboard).setChecked(true);

        AttachFragmentToItem(new DashboardFragment());

    }

    private void NetworkNotAvailable() {

        noInternet = true;
        Toast.makeText(this, "OOPS,check your connection.", Toast.LENGTH_SHORT).show();
    }

    public void NetworkAvailable() {

        noInternet = false;
        authenticationStateCheck();

    }

    private void AttachFragmentToItem(Fragment selectedFragment) {

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                selectedFragment).addToBackStack(null).commit();

    }



    private void authenticationStateCheck() {
        mFirebaseAuth = FirebaseAuth.getInstance();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {


                mUser = firebaseAuth.getCurrentUser();
                if (mUser != null) {

                    onSignedInInitializeNavSide(mUser);



                } else {
                    //Signed out

                    signOutScreen();
                }

            }
        };
    }


    private void onSignedInInitializeNavSide(FirebaseUser mUser) {

        newLoginLoad();

        mSharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        mUsername = mSharedPref.getString(getString(R.string.NAME),mUser.getDisplayName());
        mUserEmail = mUser.getEmail();
        mUserPhotoUri = Uri.parse(mSharedPref.getString(getString(R.string.PHOTO_URL),""));



        ImageView mUserImageView;
        TextView mUserNameView;
        TextView mUserEmailView;

        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        Menu menu = navigationView.getMenu();


        if(mSharedPref.getInt(getString(R.string.PRIORITY),0)==0)
            menu.findItem(R.id.nav_attendance).setVisible(false);
        else
            menu.findItem(R.id.nav_attendance).setVisible(true);


        mUserImageView = headerView.findViewById(R.id.userImageView);
        mUserNameView = headerView.findViewById(R.id.userNameView);
        mUserEmailView = headerView.findViewById(R.id.userEmailView);

        mUserNameView.setText(mUsername);
        mUserEmailView.setText(mUserEmail);

        Glide
                .with(MainActivity.this)
                .load(mUserPhotoUri) // the uri you got from Firebase
                .centerCrop()
                .into(mUserImageView); //Your imageView variable


    }

    private void signOutScreen() {

        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setIsSmartLockEnabled(false)
                        .setAvailableProviders(Arrays.asList(
                                new AuthUI.IdpConfig.EmailBuilder().build(),
                                new AuthUI.IdpConfig.GoogleBuilder().build()
                        ))
                        .build(),RC_SIGN_IN);
    }

    private void logout() {

        AttachFragmentToItem(new DashboardFragment());
        AuthUI.getInstance().signOut(this);
    }

    private boolean sideBarNavigation(MenuItem item) {
        int id = item.getItemId();
        Fragment selectedFragment = null;

        if (id == R.id.nav_dashboard) {

            selectedFragment = new DashboardFragment();

        } else if (id == R.id.nav_news) {

            selectedFragment = new NewsFragment();


        } else if (id == R.id.nav_performance) {

            selectedFragment = new PerformanceFragment();


        } else if (id == R.id.nav_result) {

            selectedFragment = new ResultFragment();

        } else if (id == R.id.nav_attendance) {

            selectedFragment = new AttendanceFragment();

        } else if (id == R.id.nav_profile) {

            selectedFragment = new ProfileFragment();

        } else if (id == R.id.nav_logout) {

            logout();
            return true;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        AttachFragmentToItem(selectedFragment);
        return true;
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


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        return (sideBarNavigation(item));


    }

    public static FirebaseUser UserInstanceForFragment() {

        return mUser;

    }


    @Override
    protected void onResume() {
        super.onResume();

        detectingConnection();
        if (!noInternet)
            mFirebaseAuth.addAuthStateListener(mAuthStateListener);



        if(mUser!=null)
            onSignedInInitializeNavSide(mUser);


    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }

    }



    private void detectingConnection(){
        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    if(mUser!=null)
                        onSignedInInitializeNavSide(mUser);

                    NetworkAvailable();
                } else {
                    NetworkNotAvailable();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("Listener was cancelled");
            }
        });
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode ==RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Signing In", Toast.LENGTH_SHORT).show();

                mUser = FirebaseAuth.getInstance().getCurrentUser();
                if(mUser!=null) {
                    newLogin=true;
                    newLoginLoad();

                }

            } else if (resultCode == RESULT_CANCELED ) {
                Toast.makeText(this, "Login Error", Toast.LENGTH_SHORT).show();
                finish();
            }
        }

//        if(requestCode == RC_NEW_LOGIN){
//            if(resultCode == RESULT_OK){
//                Toast.makeText(this, "Profile Complete", Toast.LENGTH_SHORT).show();
////                loginInfo_Collector result_login_object = (loginInfo_Collector) data.getExtras().getSerializable("LOGIN_OBJECT");
//            }
//        }


    }

    private void newLoginLoad() {

        mProgressBar = findViewById(R.id.progressBar_login);

        mProgressBar.setVisibility(View.VISIBLE);

        databaseLoad();

//        mProgressBar.setVisibility(View.INVISIBLE);
//        if(newLogin){
//            newLogin = false;
//            Intent intent = new Intent(this,loginActivity.class);
//            startActivity(intent);
//        }

    }

    private void databaseLoad(){
        mFirebaseDatabse = FirebaseDatabase.getInstance();
        mUSerListDbRef = mFirebaseDatabse.getReference().child("UserList");


        mUSerListDbRef.addValueEventListener(new ValueEventListener() {
            @Override


            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                int count=0;
                for(DataSnapshot users : dataSnapshot.getChildren()){

                    if(mUser!=null)
                        if(mUser.getUid().contentEquals(users.getKey())){
                            count++;
                        }

                }

                if(count==0){
                    newLogin = true;
                }
                else
                    newLogin = false;


                mProgressBar.setVisibility(View.INVISIBLE);
                if(newLogin){
                    newLogin = false;
                    Intent intent = new Intent(MainActivity.this,loginActivity.class);
                    startActivity(intent);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }



}





