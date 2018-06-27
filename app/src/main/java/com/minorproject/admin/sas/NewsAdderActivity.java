package com.minorproject.admin.sas;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class NewsAdderActivity extends AppCompatActivity {

    public static final int DEFAULT_MSG_LENGTH_LIMIT = 200;
    private static final int RC_PHOTO_PICKER =  2;

    private NewsMessageAdapter mNewsAdapter;
    private ImageButton mPhotoPickerButton;

    private ListView mNewsListView;
    private EditText mNewsEditText;
    private EditText mTitleEditText;
    private EditText mImageUrl;
    private Button mSendButton;
    private String mSenderName;
    private String mImageURI;
    private String mDay;
    private String mTitle;
    String formattedDate;
    String formattedTime;

    private boolean isTextReady=false;
    private  boolean isImageReady=false;
    private boolean isTitleReady=false;


    private static FirebaseUser mFbUser;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mNewsDatabaseReference;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mNewsPhotoStorageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.editor_add_news);

        setTitle("Add a Notice");

        mFbUser = MainActivity.UserInstanceForFragment();
        mTitle=null;



        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();

        mNewsDatabaseReference = mFirebaseDatabase.getReference().child("News_Database");
        mNewsPhotoStorageReference = mFirebaseStorage.getReference().child("news_photos");


        mPhotoPickerButton =  findViewById(R.id.photoPickerButton);
        mNewsEditText = findViewById(R.id.newsEditText);
        mSendButton = findViewById(R.id.sendButton);
        mTitleEditText = findViewById(R.id.titleEditText);
        mImageUrl = findViewById(R.id.imageUrlEditText);
        mNewsListView = findViewById(R.id.newsListView2);


        Intent intent = getIntent();
       mSenderName = intent.getStringExtra("UserValue");


        // Initialize message ListView and its adapter
        List<NewsMessageInfoCollector> newsMessages = new ArrayList<>();
        mNewsAdapter = new NewsMessageAdapter(this, R.layout.item_news_message, newsMessages);
        mNewsListView.setAdapter(mNewsAdapter);


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
                if (charSequence.toString().trim().length() > 0 && isTitleReady) {
                    mSendButton.setEnabled(true);
                    isTextReady=true;
                }
                else if(charSequence.toString().trim().length() > 0 && !isTitleReady){
                    mSendButton.setEnabled(false);
                    isTextReady=true;
                }

                else {
                    mSendButton.setEnabled(false);
                    isTextReady=false;
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        mNewsEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});


        // Enable Send button when there's text to send
        mTitleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0 && isTextReady) {
                    mSendButton.setEnabled(true);
                    isTitleReady=true;
                    mTitle = mTitleEditText.getText().toString();

                }
                else if(charSequence.toString().trim().length() > 0 && !isTextReady){
                    mSendButton.setEnabled(false);
                    isTitleReady=true;
                    mTitle = mTitleEditText.getText().toString();
                }

                else {
                    mSendButton.setEnabled(false);
                    isTitleReady=false;
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        // Send button sends a message and clears the EditText
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // create a calendar
                Calendar calendar = Calendar.getInstance();
                Date date = new Date(calendar.getTimeInMillis());

                NewsMessageInfoCollector mNewsMessage=null;

                 formattedDate = formatDate(date);
                 formattedTime = formatTime(date);


                getCurrentDay();

                if(isTextReady) {
                     mNewsMessage = new NewsMessageInfoCollector(mTitle, mNewsEditText.getText().toString(), mSenderName, null, mDay, formattedDate, formattedTime);
                }

                else if(isImageReady)
                     mNewsMessage = new NewsMessageInfoCollector(mTitle,null,mSenderName, mImageURI,mDay,formattedDate,formattedTime);


                addToDatabse(mNewsMessage);

                // Clear input box
                mNewsEditText.setText("");
                mTitleEditText.setText("");
                mImageUrl.setText("");


                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new NewsFragment()).commit();


            }
        });





    }

    private void addToDatabse(NewsMessageInfoCollector newsMessage){
        mNewsDatabaseReference.push().setValue(newsMessage);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
            final StorageReference photoRef = mNewsPhotoStorageReference.child(selectedImageUri.getLastPathSegment());
            photoRef.putFile(selectedImageUri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    // create a calendar
                    Calendar calendar = Calendar.getInstance();
                    Date date = new Date(calendar.getTimeInMillis());

                    formattedDate = formatDate(date);
                    formattedTime = formatTime(date);

                    getCurrentDay();


                   mImageURI = photoRef.getDownloadUrl().toString();

                    mImageUrl.setText(mImageURI);
                    isImageReady=true;

                    if(isTitleReady)
                    mSendButton.setEnabled(true);

                }
            });
        }
        else if(requestCode==RC_PHOTO_PICKER && resultCode == RESULT_CANCELED){
            Toast.makeText(this, "Not an Image", Toast.LENGTH_SHORT).show();
            isImageReady=false;
        }

    }

    private void getCurrentDay(){

        String daysArray[] = {"Sunday","Monday","Tuesday", "Wednesday","Thursday","Friday", "Saturday"};

        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK)-1;
        mDay = daysArray[day];

    }

    /**
     * Return the formatted date string (i.e. "Mar 3, 1984") from a Date object.
     */
    private String formatDate(Date dateObject) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("LLL dd, yyyy");
        return dateFormat.format(dateObject);
    }

    /**
     * Return the formatted date string (i.e. "4:30 PM") from a Date object.
     */
    private String formatTime(Date dateObject) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
        return timeFormat.format(dateObject);
    }


    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Discard and Quit Adding ?");
        builder.setPositiveButton("Discard", discardButtonClickListener);
        builder.setNegativeButton("Keep Adding", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            // Otherwise if there are unsaved changes, setup a dialog to warn the user.
            // Create a click listener to handle the user confirming that
            // changes should be discarded.
            DialogInterface.OnClickListener discardButtonClickListener =
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // User clicked "Discard" button, navigate to parent fragment.
                            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container2,
                                    new NewsFragment()).commit();

                        }
                    };

            // Show a dialog that notifies the user they have unsaved changes
            showUnsavedChangesDialog(discardButtonClickListener);
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container2,
                                new NewsFragment()).commit();

                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }




}
