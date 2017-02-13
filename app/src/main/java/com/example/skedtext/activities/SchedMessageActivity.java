package com.example.skedtext.activities;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.skedtext.DBHelper.SQLiteDatabaseHelper;
import com.example.skedtext.R;

import java.util.Calendar;

public class SchedMessageActivity extends AppCompatActivity {

    EditText edtContacts, edtMessage, edtEventSelectTime, edtEventSelectDate,
            edtAlarmSelectTime, edtAlarmSelectDate;

    String contact, message, EventDate, EventTime, AlarmDate, AlarmTime;

    Activity activity;

    SQLiteDatabaseHelper myDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sched_message);
        getSupportActionBar().setHomeButtonEnabled(true);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_left_arrow);

        myDB = new SQLiteDatabaseHelper(this);
        activity = this;
        edtContacts = (EditText) findViewById(R.id.edtContacts);
        edtMessage = (EditText) findViewById(R.id.edtMessage);

        edtEventSelectDate = (EditText) findViewById(R.id.edtEventSelectDate);
        edtEventSelectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar mcurrentDate=Calendar.getInstance();
                final int mYear=mcurrentDate.get(Calendar.YEAR);
                final int mMonth=mcurrentDate.get(Calendar.MONTH);
                final int mDay=mcurrentDate.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog mDatePicker=new DatePickerDialog(SchedMessageActivity.this, new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                        if(selectedyear < mYear || selectedmonth < mMonth || selectedday < mDay){
                            Toast.makeText(getApplicationContext(), "Error: Selected day of message is less than" +
                                    " the current date.", Toast.LENGTH_LONG).show();
                            edtEventSelectDate.setText("");
                            edtEventSelectDate.setHint("Select Date");
                        }else{
                            edtEventSelectDate.setText(selectedyear + "/" + selectedmonth + "/" + selectedday);
                            EventDate = selectedyear + "-" + selectedmonth + "-" + selectedday;
                        }
                    }
                },mYear, mMonth, mDay);
                mDatePicker.show();
            }
        });

        edtEventSelectTime = (EditText) findViewById(R.id.edtEventSelectTime);
        edtEventSelectTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(SchedMessageActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        edtEventSelectTime.setText( selectedHour + ":" + selectedMinute);
                        EventTime = selectedHour + ":" + selectedMinute;
                    }
                }, hour, minute, true);
                mTimePicker.show();
            }
        });

        edtAlarmSelectDate = (EditText) findViewById(R.id.edtAlarmSelectDate);
        edtAlarmSelectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar mcurrentDate=Calendar.getInstance();
                final int mYear=mcurrentDate.get(Calendar.YEAR);
                final int mMonth=mcurrentDate.get(Calendar.MONTH);
                final int mDay=mcurrentDate.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog mDatePicker=new DatePickerDialog(SchedMessageActivity.this, new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                        if(selectedyear < mYear || selectedmonth < mMonth || selectedday < mDay){
                            Toast.makeText(getApplicationContext(), "Error: Selected day of message is less than" +
                                    " the current date.", Toast.LENGTH_LONG).show();
                            edtAlarmSelectDate.setText("");
                            edtAlarmSelectDate.setHint("Select Date");
                        }else{
                            edtAlarmSelectDate.setText(selectedyear + "/" + selectedmonth + "/" + selectedday);
                            AlarmDate = selectedyear + "-" + selectedmonth + "-" + selectedday;
                        }
                    }
                },mYear, mMonth, mDay);
                mDatePicker.show();
            }
        });

        edtAlarmSelectTime = (EditText) findViewById(R.id.edtAlarmSelectTime);
        edtAlarmSelectTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(SchedMessageActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        edtAlarmSelectTime.setText( selectedHour + ":" + selectedMinute);
                        AlarmTime = selectedHour + ":" + selectedMinute;
                    }
                }, hour, minute, true);
                mTimePicker.show();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_message, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.confirm:
                contact = edtContacts.getText().toString();
                message = edtMessage.getText().toString();
                if(contact.trim().equals("") || message.trim().equals("") ||
                        EventDate.trim().equals("") || EventTime.trim().equals("") ||
                        AlarmDate.trim().equals("") || AlarmTime.trim().equals("")){
                    final android.app.AlertDialog.Builder loginAlert = new android.app.AlertDialog.Builder(activity);
                    loginAlert.setTitle("Incomplete Information");
                    loginAlert.setMessage("Please input the required information. For more information" +
                            ", press the icon next to submit.");
                    loginAlert.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
                    loginAlert.show();
                }else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("Are you sure?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    String event = EventDate + " " + EventTime;
                                    String alarm = AlarmDate + " " + AlarmTime;
                                    if(myDB.saveMessage(contact, message, event, alarm)){
                                        finish();
                                    }
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // User cancelled the dialog
                                }
                            });
                    AlertDialog d = builder.create();
                    d.setTitle("Save Alarm");
                    d.show();
                }

                break;

            case R.id.info:
                break;

            case android.R.id.home:
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        edtEventSelectTime.setText("");
//        edtEventSelectTime.setHint("Select Time");
//        edtEventSelectDate.setText("");
//        edtEventSelectDate.setHint("Select Date");
    }

    @Override
    public boolean onKeyDown(int keycode, KeyEvent event) {
        if (keycode == KeyEvent.KEYCODE_BACK) {
        }
        return super.onKeyDown(keycode, event);
    }

}
