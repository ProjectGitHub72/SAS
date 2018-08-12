package com.minorproject.admin.sas;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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


    private EditText mSubjectCodeEditText;
    private EditText mRollCall1;
    private EditText mRollCall2;
    private Button mBeginButton;

    private String roll_no1;
    private String roll_no2;
    private String subjectCode;




    public AttendanceFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_attendance, container, false);


        mSubjectCodeEditText = rootView.findViewById(R.id.subject_attend_editText);
        mRollCall1 = rootView.findViewById(R.id.roll_attend_editText);
        mRollCall2 = rootView.findViewById(R.id.roll2_attend_editText);
        mBeginButton = rootView.findViewById(R.id.begin_attend_button);

        getActivity().setTitle("Class Information");


        setViewLimiters();




        return rootView;
    }




    private void setViewLimiters() {


        mBeginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                subjectCode = mSubjectCodeEditText.getText().toString().toUpperCase().trim();
                roll_no1 = mRollCall1.getText().toString().trim();
                roll_no2 = mRollCall2.getText().toString().trim();

                // Clear input box

                mSubjectCodeEditText.setText("");
                mRollCall1.setText("");
                mRollCall2.setText("");


                mBeginButton.setEnabled(false);
                Toast.makeText(getContext(), "Preparing To Load...", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(),AttendanceActivity.class);


                intent.putExtra("SUBJECT",subjectCode);
                intent.putExtra("ROLL1",roll_no1);
                intent.putExtra("ROLL2",roll_no2);


                startActivity(intent);

            }
        });



        editTextListeners();


    }


    private void editTextListeners() {


        mSubjectCodeEditText.addTextChangedListener(watcher);
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

                            mSubjectCodeEditText.getText().toString().length() >0 &&
                            mRollCall1.getText().toString().length() > 0 &&
                            mRollCall2.getText().toString().length() >0
                    )            {

                if(!MainActivity.noInternet)
                        mBeginButton.setEnabled(true);

            }

            else
                mBeginButton.setEnabled(false);

        }
    };



}
