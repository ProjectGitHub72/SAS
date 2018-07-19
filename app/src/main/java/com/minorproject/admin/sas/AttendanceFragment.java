package com.minorproject.admin.sas;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 */
public class AttendanceFragment extends Fragment {

    private EditText mYearEditText;
    private EditText mFacultyEditText;
    private EditText mSubjectCodeEditText;
    private EditText mRollCall1;
    private EditText mRollCall2;
    private Button mBeginButton;

    private String roll_no1;
    private String roll_no2;
    private String subjectCode;
    private String faculty_symbol;
    private String year;
    private String teacherName;

    private SharedPreferences mSharedPref;



    public AttendanceFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_attendance, container, false);


        mYearEditText = rootView.findViewById(R.id.year_attendance_editText);
        mFacultyEditText = rootView.findViewById(R.id.faculty_attend_editText);
        mSubjectCodeEditText = rootView.findViewById(R.id.subject_attend_editText);
        mRollCall1 = rootView.findViewById(R.id.roll_attend_editText);
        mRollCall2 = rootView.findViewById(R.id.roll2_attend_editText);
        mBeginButton = rootView.findViewById(R.id.begin_attend_button);


        mSharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        obtainPreference();

        setViewLimiters();







        return rootView;
    }





    private void obtainPreference() {

        mFacultyEditText.setText(mSharedPref.getString(getString(R.string.FACULTY), ""));
        mYearEditText.setText(mSharedPref.getString(getString(R.string.YEAR), ""));
        teacherName = mSharedPref.getString(getString(R.string.NAME)," ");

    }

    private void setViewLimiters() {


        mBeginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                year = mYearEditText.getText().toString().trim();
                faculty_symbol = mFacultyEditText.getText().toString().toUpperCase().trim();
                subjectCode = mSubjectCodeEditText.getText().toString().toUpperCase().trim();
                roll_no1 = mRollCall1.getText().toString().trim();
                roll_no2 = mRollCall2.getText().toString().trim();

                // Clear input box
                mYearEditText.setText("");
                mFacultyEditText.setText("");
                mSubjectCodeEditText.setText("");
                mRollCall1.setText("");
                mRollCall2.setText("");


                mBeginButton.setEnabled(false);
                Toast.makeText(getContext(), "Preparing To Load...", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(),AttendanceActivity.class);

                intent.putExtra("FACULTY",faculty_symbol);
                intent.putExtra("TEACHER",teacherName);
                intent.putExtra("SUBJECT",subjectCode);
                intent.putExtra("YEAR",year);
                intent.putExtra("ROLL1",roll_no1);
                intent.putExtra("ROLL2",roll_no2);


                startActivity(intent);

            }
        });



        editTextListeners();


    }


    private void editTextListeners() {

        mYearEditText.addTextChangedListener(watcher);
        mSubjectCodeEditText.addTextChangedListener(watcher);
        mFacultyEditText.addTextChangedListener(watcher);
        mRollCall1.addTextChangedListener(watcher);
        mRollCall2.addTextChangedListener(watcher);
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
                    mYearEditText.getText().toString().length() >0 &&
                            mFacultyEditText.getText().toString().length() > 0 &&
                            mSubjectCodeEditText.getText().toString().length() >0 &&
                            mRollCall1.getText().toString().length() > 0 &&
                            mRollCall2.getText().toString().length() >0
                    )            {

                        mBeginButton.setEnabled(true);

            }

            else
                mBeginButton.setEnabled(false);

        }
    };



}
