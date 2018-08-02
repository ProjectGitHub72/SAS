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
        implements NavigationView.OnNavigationItemSelectedListener,DashboardFragment.MyInterface {


    private static FirebaseUser mUser;
    private static String mUsername;
    private static String mUserEmail;
    private static Uri mUserPhotoUri;


    FirebaseAuth mFirebaseAuth;
    FirebaseAuth.AuthStateListener mAuthStateListener;

    private static final int RC_SIGN_IN = 1;
    public static boolean noInternet = true;
    public static boolean isPersistenceOn = false;
    private boolean newLogin = false;

    private ProgressBar mProgressBar;


    private ImageView mUserImageView;
    private TextView mUserNameView;
    private TextView mUserEmailView;



    private int mPriority=2;
    private static boolean newDataAvailable = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        if(!isPersistenceOn)
        {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            isPersistenceOn=true;
        }

        setContentView(R.layout.activity_main);

        loadViewsAndData();

        if(!isIntentFromFragment())
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

        if(!newDataAvailable) {
            mUsername = mUser.getDisplayName();
            mUserPhotoUri = mUser.getPhotoUrl();
        }

        mUserEmail = mUser.getEmail();



        NavigationView navigationView = findViewById(R.id.nav_view);
        Menu menu = navigationView.getMenu();


        if(newDataAvailable) {
            if (mPriority == 2)
                menu.findItem(R.id.nav_attendance).setVisible(false);
            else
                menu.findItem(R.id.nav_attendance).setVisible(true);
        }
        else{
            menu.findItem(R.id.nav_attendance).setVisible(false);
        }

                loadNavigationData();

    }

    private void loadNavigationData() {

        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);


        mUserImageView = headerView.findViewById(R.id.userImageView);
        mUserNameView = headerView.findViewById(R.id.userNameView);
        mUserEmailView = headerView.findViewById(R.id.userEmailView);

        mUserNameView.setText(mUsername);
        mUserEmailView.setText(mUserEmail);

        if(mUserPhotoUri!=null) {
            Glide
                    .with(MainActivity.this)
                    .load(mUserPhotoUri) // the uri you got from Firebase
                    .centerCrop()
                    .placeholder(R.drawable.ic_launcher_foreground11)
                    .into(mUserImageView); //Your imageView variable
        }
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
                        .setTheme(R.style.LoginTheme)
                        .build(),RC_SIGN_IN);
    }

    private void logout() {

        AttachFragmentToItem(new DashboardFragment());
        AuthUI.getInstance().signOut(this);
    }

    private boolean sideBarNavigation(MenuItem item) {
        int id = item.getItemId();
        Fragment selectedFragment = null;
        NavigationView navigationView =findViewById(R.id.nav_view);


        if(id!=R.id.nav_profile)
            navigationView.getMenu().findItem(R.id.nav_profile).setChecked(false);



        if (id == R.id.nav_dashboard) {

            navigationView.getMenu().findItem(R.id.nav_dashboard).setChecked(true);
            selectedFragment = new DashboardFragment();

        } else if (id == R.id.nav_news) {

            selectedFragment = new NewsFragment();


        } else if (id == R.id.nav_performance) {

            selectedFragment = new PerformanceFragment();


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



    }

    private void newLoginLoad() {

        mProgressBar = findViewById(R.id.progressBar_login);

        mProgressBar.setVisibility(View.VISIBLE);

        databaseLoad();


    }

    private void databaseLoad(){

         FirebaseDatabase mFirebaseDatabase;
         DatabaseReference mNewUsersRef;

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mNewUsersRef = mFirebaseDatabase.getReference().child("app")
                .child("users");


        mNewUsersRef.addValueEventListener(new ValueEventListener() {
            @Override


            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.getValue()!=null) {
                    int count = 0;
                    for (DataSnapshot users : dataSnapshot.getChildren()) {

                        if (mUser != null)
                            if (mUser.getUid().contentEquals(users.getKey())) {
                                count++;
                            }

                    }

                    if (count == 0) {
                        newLogin = true;
                    } else
                        newLogin = false;


                    mProgressBar.setVisibility(View.INVISIBLE);
                    if (newLogin) {
                        newLogin = false;
                        Intent intent = new Intent(MainActivity.this, loginActivity.class);
                        startActivity(intent);
                    } else {

                        if (dataSnapshot.child(mUser.getUid()).child("info").child("firstName").getValue() != null
                                && dataSnapshot.child(mUser.getUid()).child("info").child("lastName").getValue() != null
                                && dataSnapshot.child(mUser.getUid()).child("accountLinks").hasChildren()) {
                            mUsername = dataSnapshot.child(mUser.getUid()).child("info").child("firstName").getValue().toString()
                                    + dataSnapshot.child(mUser.getUid()).child("info").child("lastName").getValue().toString();

                            if(dataSnapshot.child(mUser.getUid()).child("info").child("photoUrl").exists())
                            mUserPhotoUri = Uri.parse(dataSnapshot.child(mUser.getUid()).child("info").child("photoUrl").getValue().toString());


                            String priority_node = ((dataSnapshot.child(mUser.getUid()).child("accountLinks").getValue().toString()).split("="))[1];
                            if (priority_node.contains("0"))
                                mPriority = 0;
                            else if (priority_node.contains("1"))
                                mPriority = 1;
                            else if (priority_node.contains("2"))
                                mPriority = 2;


                            NavigationView navigationView = findViewById(R.id.nav_view);
                            Menu menu = navigationView.getMenu();
                            if (mPriority == 2)
                                menu.findItem(R.id.nav_attendance).setVisible(false);
                            else
                                menu.findItem(R.id.nav_attendance).setVisible(true);


                            newDataAvailable = true;
                            loadNavigationData();
                        } else
                            databaseLoad();
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(MainActivity.this, "DB Error checking Available Users", Toast.LENGTH_SHORT).show();
            }
        });


    }

    private boolean isIntentFromFragment(){

        if(getIntent()!=null && getIntent().getStringExtra("FRAGMENT")!=null) {
            Intent intent = getIntent();
            String fromIntent = intent.getStringExtra("FRAGMENT");

            switch (fromIntent){

                case "NOTICE" :     AttachFragmentToItem(new NewsFragment());
                                    break;

                case "PROFILE" :    AttachFragmentToItem(new ProfileFragment());
                                    break;

                default :            AttachFragmentToItem(new DashboardFragment());
                                     break;
            }

            return true;

        }

        return false;

    }

    @Override
    public void setNAvFromFrag(String navItem) {
        NavigationView navigationView = findViewById(R.id.nav_view);

        navigationView.getMenu().findItem(R.id.nav_profile).setChecked(false);
        navigationView.getMenu().findItem(R.id.nav_logout).setChecked(false);


        switch (navItem){



            case "DASHBOARD" :
                navigationView.getMenu().findItem(R.id.nav_dashboard).setChecked(true);
                navigationView.getMenu().findItem(R.id.nav_profile).setChecked(false);
                navigationView.getMenu().findItem(R.id.nav_logout).setChecked(false);

                break;

            case "NOTICE" :
                navigationView.getMenu().findItem(R.id.nav_news).setChecked(true);
                break;

            case "PROFILE" :
                navigationView.getMenu().findItem(R.id.nav_profile).setChecked(true);
                navigationView.getMenu().findItem(R.id.nav_dashboard).setChecked(false);

                break;

            case "PERFORMANCE" :
                navigationView.getMenu().findItem(R.id.nav_performance).setChecked(true);
                break;


        }


    }






}





