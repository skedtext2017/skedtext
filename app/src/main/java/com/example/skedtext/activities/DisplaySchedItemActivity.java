package com.example.skedtext.activities;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TimePicker;

import com.example.skedtext.DBHelper.SQLiteDatabaseHelper;
import com.example.skedtext.R;
import com.example.skedtext.Sms.SMSManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DisplaySchedItemActivity extends AppCompatActivity {

    ScrollView activity_display_sched_item;
    Spinner dropdownListGroups;
    EditText edtMessage, edtEventSelectTime, edtEventSelectDate;
    String contact, message, EventDateTime, EventTime;
    String eDate;
    String eTime;

    private ArrayList<String> permissionsGranted = new ArrayList<>();
    final private static int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;
    final private String[] permissionsRequired = new String[] {
            Manifest.permission.SEND_SMS
    };

    Activity activity;

    SQLiteDatabaseHelper myDB;
    SimpleDateFormat formatter;
    Date sDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_sched_item);
        getSupportActionBar().setHomeButtonEnabled(true);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_left_arrow);
        permissionsGranted();

        Intent infoItem = getIntent();
        String msID = infoItem.getStringExtra("id");
        contact = infoItem.getStringExtra("contact");
        message = infoItem.getStringExtra("message");
        String[] DateTime = infoItem.getStringExtra("eventDateTime").split("\\s+");
        eDate = DateTime[0];
        eTime = DateTime[1];

        EventDateTime = "";

        sDate = null;
        formatter = new SimpleDateFormat("yyyy-MM-dd");

        Calendar mcurrentDate=Calendar.getInstance();
        final int mYear=mcurrentDate.get(Calendar.YEAR);
        final int mMonth=mcurrentDate.get(Calendar.MONTH);
        final int mDay=mcurrentDate.get(Calendar.DAY_OF_MONTH);

        Date cDate = null;
        try {
            cDate = formatter.parse(eDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        final Date finalCDate = cDate;

        myDB = new SQLiteDatabaseHelper(this);
        activity = this;
        edtMessage = (EditText) findViewById(R.id.edtMessage);
        edtMessage.setText(message);
        activity_display_sched_item = (ScrollView) findViewById(R.id.activity_display_sched_item);
        dropdownListGroups = (Spinner) findViewById(R.id.dropdownListGroups);

        List<String> groups = new ArrayList<String>();
        Cursor cGroups = myDB.getContactGroups();
        if(cGroups.moveToFirst()){
            do{
                groups.add(cGroups.getString(cGroups.getColumnIndex("name")));
            }while(cGroups.moveToNext());
        }

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, groups);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdownListGroups.setAdapter(dataAdapter);
        Cursor cGroup = myDB.getContactGroup(contact);
        if(cGroup.moveToFirst()){
            contact = cGroup.getString(cGroup.getColumnIndex("name"));
            int spinnerPosition = dataAdapter.getPosition(contact);
            dropdownListGroups.setSelection(spinnerPosition);
        }

        edtEventSelectDate = (EditText) findViewById(R.id.edtEventSelectDate);
        edtEventSelectDate.setText(eDate);
        edtEventSelectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog mDatePicker=new DatePickerDialog(DisplaySchedItemActivity.this, new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                        EventDateTime = String.valueOf(selectedyear) + "-" + String.valueOf(selectedmonth) + "-" + String.valueOf(selectedday);
                        try{
                            sDate = formatter.parse(EventDateTime);
                        }catch (ParseException e){
                            e.printStackTrace();
                        }
                        if(finalCDate.compareTo(sDate)>0){
                            Snackbar snackbar = Snackbar.make(activity_display_sched_item, "Error: Selected day of event has already ended!", Snackbar.LENGTH_LONG);
                            snackbar.show();
                            edtEventSelectDate.setText(eDate);
                            edtEventSelectDate.setHint("Select Date");
                            EventDateTime = "";
                        }else{
                            edtEventSelectDate.setText(selectedyear + "-" + selectedmonth + "-" + selectedday);
                            EventDateTime = selectedyear + "-" + selectedmonth + "-" + selectedday;
                        }
                    }
                },mYear, mMonth, mDay);
                mDatePicker.show();
            }
        });

        edtEventSelectTime = (EditText) findViewById(R.id.edtEventSelectTime);
        edtEventSelectTime.setText(eTime);
        edtEventSelectTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(DisplaySchedItemActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        edtEventSelectTime.setText(String.format("%02d:%02d", selectedHour, selectedMinute));
                        EventTime = String.format("%02d:%02d", selectedHour, selectedMinute) + ":" + "00";
                        EventDateTime += " " + EventTime;
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
                message = edtMessage.getText().toString();
                if(message.trim().equals("") || EventDateTime.trim().equals("")){
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
                                    String selectedGroup = dropdownListGroups.getSelectedItem().toString();

                                    Cursor cGroup = myDB.getContactGroup(selectedGroup);
                                    String gID = "";
                                    if(cGroup.moveToFirst()){
                                        gID = cGroup.getString(cGroup.getColumnIndex("id"));
                                        if(sDate != null){
                                            Calendar alarm = Calendar.getInstance();
                                            alarm.setTime(sDate);
                                            alarm.add(Calendar.DATE, -3);
                                            Date EventAlarm = alarm.getTime();
                                            String strEventAlarm = formatter.format(EventAlarm);
                                            strEventAlarm += " " + EventTime;
                                            Log.d("SkedText", "Event: " + EventDateTime );
                                            Log.d("SkedText", "Event Alarm: " + strEventAlarm);
                                            if(myDB.saveMessage(gID, message, EventDateTime, strEventAlarm)){
                                                ArrayList<String> phoneNumbers = new ArrayList<String>();

                                                Cursor cLastID = myDB.getMessages();
                                                String msgID = "";
                                                if(cLastID.moveToLast()){
                                                    msgID = cLastID.getString(cLastID.getColumnIndex("id"));
                                                    Cursor cNumbers = myDB.getContactPhone(gID);
                                                    if(cNumbers.moveToFirst()){
                                                        do{
                                                            String num = "09" + cNumbers.getString(cNumbers.getColumnIndex("phone_number"));
                                                            phoneNumbers.add(num);
                                                        }while(cNumbers.moveToNext());

                                                    }
                                                }
                                                finish();
                                            }
                                        }
                                    }

                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
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
    }

    @Override
    public boolean onKeyDown(int keycode, KeyEvent event) {
        if (keycode == KeyEvent.KEYCODE_BACK) {
        }
        return super.onKeyDown(keycode, event);
    }

    private boolean permissionsGranted() {
        boolean granted = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArrayList<String> permissionsNotGranted = new ArrayList<>();
            for (String required : this.permissionsRequired) {
                if (checkSelfPermission(required) != PackageManager.PERMISSION_GRANTED) {
                    permissionsNotGranted.add(required);
                } else {
                    this.permissionsGranted.add(required);
                }
            }
            if (permissionsNotGranted.size() > 0) {
                granted = false;
                String[] notGrantedArray = permissionsNotGranted.toArray(new String[permissionsNotGranted.size()]);
                requestPermissions(notGrantedArray, REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            }
        }
        return granted;
    }

    private int getIndex(Spinner spinner, String myString)
    {
        int index = 0;

        for (int i=0;i<spinner.getCount();i++){
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)){
                index = i;
                break;
            }
        }
        return index;
    }

}
