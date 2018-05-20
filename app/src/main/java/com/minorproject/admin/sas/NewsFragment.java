package com.minorproject.admin.sas;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class NewsFragment extends Fragment {


    public static final int DEFAULT_MSG_LENGTH_LIMIT = 200;
    private static final String USER = "userName";
    private static final String ADMIN = "userEmail";
    private static final String ADMIN_EMAIL = "bhattadavid@gmail.com";

    private static final int RC_PHOTO_PICKER =  2;

    private ListView mNewsListView;
    private NewsMessageAdapter mNewsAdapter;
    private ProgressBar mProgressBar;
    private ImageButton mPhotoPickerButton;
    private EditText mNewsEditText;
    private Button mSendButton;
    private String mSenderName;
    private String mDay;
    private Long mDate;
    private boolean isAdmin = false;


    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mNewsDatabaseReference;
    private ChildEventListener mChildEventListener;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mNewsPhotoStorageReference;




    public NewsFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_news, container, false);

        if(getArguments().containsKey(USER)){
            mSenderName = getArguments().getString(USER);
        }

        if (getArguments().containsKey(ADMIN)) {
            if(getArguments().getString(ADMIN).contentEquals(ADMIN_EMAIL))
                isAdmin = true;
            else
                isAdmin = false;

        }


        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();

        mNewsDatabaseReference = mFirebaseDatabase.getReference().child("News_Database");
        mNewsPhotoStorageReference = mFirebaseStorage.getReference().child("news_photos");

        // Initialize references to views
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        mNewsListView = (ListView) rootView.findViewById(R.id.newsListView);
        mPhotoPickerButton = (ImageButton) rootView.findViewById(R.id.photoPickerButton);
        mNewsEditText = (EditText) rootView.findViewById(R.id.newsEditText);
        mSendButton = (Button) rootView.findViewById(R.id.sendButton);

        // Initialize message ListView and its adapter
        List<NewsMessageInfoCollector> newsMessages = new ArrayList<>();
        mNewsAdapter = new NewsMessageAdapter(getActivity(), R.layout.item_news_message, newsMessages);
        mNewsListView.setAdapter(mNewsAdapter);


        if(isAdmin) {

            rootView.findViewById(R.id.news_add_linear_layout).setVisibility(View.VISIBLE);
            // ImagePickerButton shows an image picker to upload a image for a message
            mPhotoPickerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                    startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
                }
            });

            // Enable Send button when there's text to send
            mNewsEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if (charSequence.toString().trim().length() > 0) {
                        mSendButton.setEnabled(true);
                    } else {
                        mSendButton.setEnabled(false);
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {
                }
            });

            mNewsEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});

            // Send button sends a message and clears the EditText
            mSendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // TODO: Send messages on click

                    // create a calendar
                    Calendar calendar = Calendar.getInstance();
                    mDate = calendar.getTimeInMillis();

                    getCurrentDay();

                    NewsMessageInfoCollector mNewsMessage = new NewsMessageInfoCollector(mNewsEditText.getText().toString(), mSenderName, null,mDay,mDate);
                    mNewsDatabaseReference.push().setValue(mNewsMessage);

                    // Clear input box
                    mNewsEditText.setText("");
                }
            });
        }
        else
        {
            rootView.findViewById(R.id.news_add_linear_layout).setVisibility(View.INVISIBLE);
        }

        attachDatabaseReadListener();

        return rootView;

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
            StorageReference photoRef = mNewsPhotoStorageReference.child(selectedImageUri.getLastPathSegment());
            photoRef.putFile(selectedImageUri).addOnSuccessListener(getActivity(), new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    // create a calendar
                    Calendar calendar = Calendar.getInstance();
                    mDate = calendar.getTimeInMillis();

                    getCurrentDay();


                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    NewsMessageInfoCollector newsMessage = new NewsMessageInfoCollector(null,mSenderName, downloadUrl.toString(),mDay,mDate);
                    mNewsDatabaseReference.push().setValue(newsMessage);
                }
            });
        }
        else if(requestCode==RC_PHOTO_PICKER && resultCode == RESULT_CANCELED){
            Toast.makeText(getActivity(), "Not an Image", Toast.LENGTH_SHORT).show();
        }
    }


    private void attachDatabaseReadListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                    NewsMessageInfoCollector newsMessage = dataSnapshot.getValue(NewsMessageInfoCollector.class);
                    mNewsAdapter.add(newsMessage);
                    if(mNewsAdapter!=null)
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



    public static NewsFragment newInstance(FirebaseUser mUser) {
        NewsFragment fragment = new NewsFragment();
        Bundle arguments = new Bundle();
        arguments.putString(USER, mUser.getDisplayName());
        arguments.putString(ADMIN,mUser.getEmail());
        fragment.setArguments(arguments);
        return fragment;
    }

    private void getCurrentDay(){

        String daysArray[] = {"Sunday","Monday","Tuesday", "Wednesday","Thursday","Friday", "Saturday"};

        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK)-1;
        mDay = daysArray[day];

    }


}
