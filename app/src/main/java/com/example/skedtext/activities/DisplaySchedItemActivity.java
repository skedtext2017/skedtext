package com.example.skedtext.activities;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
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
import java.util.GregorianCalendar;
import java.util.List;

public class DisplaySchedItemActivity extends AppCompatActivity {

    ScrollView activity_display_sched_item;
    Spinner dropdownListGroups;
    EditText edtMessage, edtEventSelectTime, edtEventSelectDate;
    String contact, message, createdDateTime ,EventDateTime, EventTime, AlarmTime;
    String eDate;
    String eTime;
    String msgID;

    private ArrayList<String> permissionsGranted = new ArrayList<>();
    final private static int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;
    final private String[] permissionsRequired = new String[] {
            Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS
    };

    SMSManager smsManager;

    Activity activity;

    SQLiteDatabaseHelper myDB;
    SimpleDateFormat formatter;
    Date sDate, nDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_sched_item);
        getSupportActionBar().setHomeButtonEnabled(true);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_left_arrow);
        permissionsGranted();

        Intent infoItem = getIntent();
        msgID = infoItem.getStringExtra("id");
        Log.d("SkedTest", "Saved Message ID: " + msgID);
        contact = infoItem.getStringExtra("contact");
        message = infoItem.getStringExtra("message");
        String[] DateTime = infoItem.getStringExtra("eventDateTime").split("\\s+");
        createdDateTime = infoItem.getStringExtra("createdDateTime");
        Log.d("SkedTest", "CreatedDateTime: " + createdDateTime);
        eDate = DateTime[0];
        eTime = DateTime[1];
        String testDate = eDate + " " + eTime;
        EventDateTime = testDate;
        AlarmTime = eTime;

        sDate = null;
        formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        smsManager = new SMSManager(this);

        try {
            nDate = formatter.parse(testDate);
            sDate = formatter.parse(EventDateTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar mcurrentDate=Calendar.getInstance();
        final int mYear=mcurrentDate.get(Calendar.YEAR);
        final int mMonth=mcurrentDate.get(Calendar.MONTH);
        final int mDay=mcurrentDate.get(Calendar.DAY_OF_MONTH);

        final String currentDate = String.valueOf(mYear) + "-" + String.valueOf(mMonth) + "-" + String.valueOf(mDay) + " " + eTime;
        Date cDate = null;
        try {
            cDate = formatter.parse(currentDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        final Date finalCDate = cDate;
        Log.d("SkedText", "Current Date: " + finalCDate);

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
                String gcID = cGroups.getString(cGroups.getColumnIndex("id"));
                Cursor cNumbers = myDB.getContactPhone(gcID);
                if(cNumbers.moveToFirst()){
                    if(cNumbers.moveToFirst()){
                        groups.add(cGroups.getString(cGroups.getColumnIndex("name")));
                    }
                }
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
                        EventDateTime = String.valueOf(selectedyear) + "-" + String.valueOf(selectedmonth) + "-" + String.valueOf(selectedday) + " " + eTime;
                        try{
                            sDate = formatter.parse(EventDateTime);
                            Log.d("SkedText", "Selected Date: " + sDate);
                            if(finalCDate.compareTo(sDate)>0){
                                Snackbar snackbar = Snackbar.make(activity_display_sched_item, "Error: Selected day of event has already ended!", Snackbar.LENGTH_LONG);
                                snackbar.show();
                                edtEventSelectDate.setText(eDate);
                                EventDateTime = "";
                            }else{
                                selectedmonth++;
                                edtEventSelectDate.setText(selectedyear + "-" + selectedmonth + "-" + selectedday);
                                EventDateTime = selectedyear + "-" + selectedmonth + "-" + selectedday + " " + eTime;
                            }
                        }catch (ParseException e){
                            e.printStackTrace();
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
                        EventDateTime = eDate + " " + EventTime;
                        AlarmTime = String.format("%02d:%02d", selectedHour, (selectedMinute-10) ) + ":" + "00";
                    }
                }, hour, minute, true);
                mTimePicker.show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_item_message, menu);
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
                                    if(cGroup.moveToFirst()) {
                                        gID = cGroup.getString(cGroup.getColumnIndex("id"));
                                        String[] strEvent = EventDateTime.split(" ");
                                        String strEventAlarm = strEvent[0] + " " + AlarmTime;
                                        try {
                                            Date d = formatter.parse(strEventAlarm);
                                            Calendar gc = new GregorianCalendar();
                                            gc.setTime(d);
                                            gc.add(Calendar.MINUTE, -10);
                                            Date d2 = gc.getTime();
                                            strEventAlarm = formatter.format(d2);
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }

                                        Log.d("SkedText", "AlarmTimeDate: " + strEventAlarm);
                                        Log.d("SkedText", "EventTimeDate: " + EventDateTime);

                                        ArrayList<String> phoneNumbers = new ArrayList<String>();
                                        createdDateTime += msgID;
                                        Cursor cNumbers = myDB.getContactPhone(gID);
                                        if(cNumbers.moveToFirst()){
                                            do{
                                                String num = "09" + cNumbers.getString(cNumbers.getColumnIndex("phone_number"));
                                                phoneNumbers.add(num);
                                            }while(cNumbers.moveToNext());
                                            if(isSimExists()){
                                                if(myDB.messageUpdateInfo(msgID, gID, message, EventDateTime,strEventAlarm, createdDateTime)){
                                                    long finalUniqueID = Long.parseLong(createdDateTime);
                                                    Log.d("SkedText", "First Unique ID: " + finalUniqueID);
                                                    for(int i = 0; i<phoneNumbers.size(); i++){
                                                        finalUniqueID++;
                                                        Log.d("SkedText", "Incremented ID: " + finalUniqueID);
                                                        smsManager.setSmsSchedule(finalUniqueID, msgID, phoneNumbers.get(i), message, EventDateTime);
                                                        finalUniqueID++;
                                                        Log.d("SkedText", "Incremented ID: " + finalUniqueID);
                                                        smsManager.setSmsSchedule(finalUniqueID, msgID, phoneNumbers.get(i), message,strEventAlarm);
                                                    }
                                                }
                                                setResult(MainActivity.RESULT_SMS_EDIT, new Intent(getApplicationContext(), MainActivity.class));
                                                finish();
                                            }else{
                                                setResult(RESULT_CANCELED, new Intent(getApplicationContext(), MainActivity.class));
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

            case R.id.cancel:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Are you sure?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String selectedGroup = dropdownListGroups.getSelectedItem().toString();

                                Cursor cGroup = myDB.getContactGroup(selectedGroup);
                                String gID = "";
                                if(cGroup.moveToFirst()){
                                    gID = cGroup.getString(cGroup.getColumnIndex("id"));
                                    ArrayList<String> phoneNumbers = new ArrayList<String>();

                                    createdDateTime += msgID;
                                    Log.d("SkedTest", "Selected Unique ID: " + createdDateTime);
                                    Cursor cNumbers = myDB.getContactPhone(gID);
                                    if(cNumbers.moveToFirst()){
                                        do{
                                            phoneNumbers.add(cNumbers.getString(cNumbers.getColumnIndex("phone_number")));
                                        }while (cNumbers.moveToNext());

                                        long finalUniqueID = Long.parseLong(createdDateTime);
                                        for(int i = 0; i<phoneNumbers.size(); i++){
                                            finalUniqueID++;
                                            Log.d("SkedHello", "Current Unique ID: " + String.valueOf(finalUniqueID));
                                            smsManager.cancelSmsSchedule(finalUniqueID);
                                            finalUniqueID++;
                                            Log.d("SkedHello", "Current Unique ID: " + String.valueOf(finalUniqueID));
                                            smsManager.cancelSmsSchedule(finalUniqueID);
                                        }
                                        myDB.messageChangeStatus(myDB.MESSAGE_CANCELLED, msgID);
                                        Intent toMain = new Intent(getApplicationContext(), MainActivity.class);
                                        setResult(MainActivity.RESULT_SMS_CANCELLED, toMain);
                                        finish();
                                    }
                                }

                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                AlertDialog d = builder.create();
                d.setTitle("Cancel Alarm");
                d.show();
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
            Intent toMain = new Intent(getApplicationContext(), MainActivity.class);
            setResult(1, toMain);
            finish();
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

    public boolean isSimExists()
    {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        int SIM_STATE = telephonyManager.getSimState();

        if(SIM_STATE == TelephonyManager.SIM_STATE_READY)
            return true;
        else
        {
            switch(SIM_STATE)
            {
                case TelephonyManager.SIM_STATE_ABSENT: //SimState = "No Sim Found!";
                    Snackbar noSimSnack = Snackbar.make(activity_display_sched_item, "No Sim Found!", Snackbar.LENGTH_LONG);
                    noSimSnack.show();
                    break;
                case TelephonyManager.SIM_STATE_NETWORK_LOCKED: //SimState = "Network Locked!";
                    Snackbar noNetworkSnack = Snackbar.make(activity_display_sched_item, "Network Locked!", Snackbar.LENGTH_LONG);
                    noNetworkSnack.show();
                    break;
                case TelephonyManager.SIM_STATE_PIN_REQUIRED: //SimState = "PIN Required to access SIM!";
                    Snackbar pinReqSnack = Snackbar.make(activity_display_sched_item, "PIN Required to access SIM!", Snackbar.LENGTH_LONG);
                    pinReqSnack.show();
                    break;
                case TelephonyManager.SIM_STATE_PUK_REQUIRED: //SimState = "PUK Required to access SIM!"; // Personal Unblocking Code
                    Snackbar pukReqSnack = Snackbar.make(activity_display_sched_item, "PUK Required to access SIM!", Snackbar.LENGTH_LONG);
                    pukReqSnack.show();
                    break;
                case TelephonyManager.SIM_STATE_UNKNOWN: //SimState = "Unknown SIM State!";
                    Snackbar unknownSimSnack = Snackbar.make(activity_display_sched_item, "Unknown SIM State!", Snackbar.LENGTH_LONG);
                    unknownSimSnack.show();
                    break;
            }
            return false;
        }
    }

}
