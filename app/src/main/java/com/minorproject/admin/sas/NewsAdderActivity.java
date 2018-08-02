package com.minorproject.admin.sas;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
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

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
    private String formattedDate;
    private String formattedTime;

    private boolean isTextReady=false;
    private  boolean isImageReady=false;
    private final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private String DBLinkValue;


    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mNewsDatabaseReference;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mNewsPhotoStorageReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.editor_add_news);

        setTitle("Add a Notice");

        mTitle=null;


        Intent intent = getIntent();
        mSenderName = intent.getStringExtra("USERNAME");
        DBLinkValue = intent.getStringExtra("DBKEY");


        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();

        mNewsDatabaseReference = mFirebaseDatabase.getReference()
                .child("app")
                .child("app_data")
                .child(DBLinkValue)
                .child("news_database");

        mNewsPhotoStorageReference = mFirebaseStorage.getReference()
                .child("news_photos");


        mPhotoPickerButton =  findViewById(R.id.photoPickerButton);
        mNewsEditText = findViewById(R.id.newsEditText);
        mSendButton = findViewById(R.id.sendButton);
        mTitleEditText = findViewById(R.id.titleEditText);
        mImageUrl = findViewById(R.id.imageUrlEditText);
        mNewsListView = findViewById(R.id.newsListView2);




        // Initialize message ListView and its adapter
        List<NewsMessageInfoCollector> newsMessages = new ArrayList<>();
        mNewsAdapter = new NewsMessageAdapter(this, R.layout.item_news_message, newsMessages);
        mNewsListView.setAdapter(mNewsAdapter);

        editTextListeners();

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

                mTitle = mTitleEditText.getText().toString();


                if(isTextReady) {
                     mNewsMessage = new NewsMessageInfoCollector(mTitle, mNewsEditText.getText().toString(), mSenderName, null, mDay, formattedDate, formattedTime,"txt");
                }

                else if(isImageReady)
                     mNewsMessage = new NewsMessageInfoCollector(mTitle,mNewsEditText.getText().toString(),mSenderName, mImageURI,mDay,formattedDate,formattedTime,"url");


                addToDatabase(mNewsMessage);

                // Clear input box
                mNewsEditText.setText("");
                mTitleEditText.setText("");
                mImageUrl.setText("");


                Intent intent =  new Intent(NewsAdderActivity.this,MainActivity.class);
                intent.putExtra("FRAGMENT","NOTICE");
                startActivity(intent);


            }
        });





    }


    private void editTextListeners() {

        mTitleEditText.addTextChangedListener(watcher);
        mNewsEditText.addTextChangedListener(watcher);
        mImageUrl.addTextChangedListener(watcher);

    }

    private final TextWatcher watcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after)
        { }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count)
        {}
        @Override
        public void afterTextChanged(Editable s) {

            if(
                    mTitleEditText.getText().toString().length() >0 &&
                            mNewsEditText.getText().toString().length() > 0 &&
                            (mImageUrl.getText().toString().length() ==0 )
                    )            {

                mSendButton.setEnabled(true);
                isTextReady =true;
                isImageReady=false;

            }

            else if(
                    mTitleEditText.getText().toString().length() >0 &&
                            (mNewsEditText.getText().toString().length()> 0) &&
                            mImageUrl.getText().toString().length() > 0
                    )            {

                mSendButton.setEnabled(true);
                isImageReady=true;
                isTextReady=false;

            }

            else {
                mSendButton.setEnabled(false);
                isTextReady =false;
                isImageReady=false;
            }

        }
    };


    private void addToDatabase(NewsMessageInfoCollector newsMessage){
        mNewsDatabaseReference.push().setValue(newsMessage);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            final StorageReference photoRef = mNewsPhotoStorageReference.child(randomAlphaNumeric(8));
            UploadTask uploadTask = photoRef.putFile(data.getData());

            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return photoRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {

                    // create a calendar
                    Calendar calendar = Calendar.getInstance();
                    Date date = new Date(calendar.getTimeInMillis());

                    formattedDate = formatDate(date);
                    formattedTime = formatTime(date);

                    getCurrentDay();


                    mImageURI = task.getResult().toString();

                    mImageUrl.setText(mImageURI);   }
                }
            });



        }
        else if(requestCode==RC_PHOTO_PICKER && resultCode == RESULT_CANCELED){
            Toast.makeText(this, "Not an Image", Toast.LENGTH_SHORT).show();
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
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
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



    public String randomAlphaNumeric(int count) {
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int)(Math.random()*ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }

}
