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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.skedtext.DBHelper.SQLiteDatabaseHelper;
import com.example.skedtext.R;
import com.example.skedtext.Sms.SMSManager;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class SchedMessageActivity extends AppCompatActivity{

    ScrollView activity_sched_message;
    Spinner dropdownListGroups;
    EditText edtMessage, edtEventSelectTime, edtEventSelectDate;
    String contact, message, EventDateTime, EventTime, AlarmTime;

    private ArrayList<String> permissionsGranted = new ArrayList<>();
    final private static int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;
    final private String[] permissionsRequired = new String[] {
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_CONTACTS
    };

    Activity activity;

    SQLiteDatabaseHelper myDB;
    SimpleDateFormat formatter;
    Date sDate;
    Intent intentResultAddSMS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sched_message);
        getSupportActionBar().setHomeButtonEnabled(true);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_left_arrow);

        permissionsGranted();

        contact = "";
        message = "";
        EventDateTime = "";
        EventTime = "";
        AlarmTime = "";

        sDate = null;
        intentResultAddSMS = new Intent();
        formatter = new SimpleDateFormat("yyyy-MM-dd");

        Calendar mcurrentDate=Calendar.getInstance();
        final int mYear=mcurrentDate.get(Calendar.YEAR);
        final int mMonth=mcurrentDate.get(Calendar.MONTH);
        final int mDay=mcurrentDate.get(Calendar.DAY_OF_MONTH);

        final String currentDate = String.valueOf(mYear) + "-" + String.valueOf(mMonth) + "-" + String.valueOf(mDay);
        Date cDate = null;
        try {
            cDate = formatter.parse(currentDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        final Date finalCDate = cDate;

        myDB = new SQLiteDatabaseHelper(this);
        activity = this;
        edtMessage = (EditText) findViewById(R.id.edtMessage);
        activity_sched_message = (ScrollView) findViewById(R.id.activity_sched_message);
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

        edtEventSelectDate = (EditText) findViewById(R.id.edtEventSelectDate);
        edtEventSelectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog mDatePicker=new DatePickerDialog(SchedMessageActivity.this, new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                        EventDateTime = selectedyear + "-" + selectedmonth + "-" + selectedday;
                        try{
                            sDate = formatter.parse(EventDateTime);
                        }catch (ParseException e){
                            e.printStackTrace();
                        }
                        if(finalCDate.compareTo(sDate)>0){
                            Snackbar snackbar = Snackbar.make(activity_sched_message, "Error: Selected day of event has already ended!", Snackbar.LENGTH_LONG);
                            snackbar.show();
                            edtEventSelectDate.setText("");
                            edtEventSelectDate.setHint("Select Date");
                            EventDateTime = "";
                        }else{
                            selectedmonth++;
                            edtEventSelectDate.setText(selectedyear + "-" + selectedmonth + "-" + selectedday);
                            EventDateTime = selectedyear + "-" + selectedmonth + "-" + selectedday;
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
                        edtEventSelectTime.setText(String.format("%02d:%02d", selectedHour, selectedMinute));
                        EventTime = String.format("%02d:%02d", selectedHour, selectedMinute) + ":" + "00";
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
        inflater.inflate(R.menu.menu_message, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.confirm:
                Log.d("SkedTest", "Event Time: " + EventTime);
                message = edtMessage.getText().toString();
                if(message.trim().equals("") || EventDateTime.trim().equals("") || EventTime.trim().equals("")){
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
                                    EventDateTime += " " + EventTime;
                                    SimpleDateFormat newFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                    try {
                                        sDate = newFormatter.parse(EventDateTime);
                                    } catch (ParseException e) {
                                        sDate = null;
                                        e.printStackTrace();
                                    }
                                    String selectedGroup = dropdownListGroups.getSelectedItem().toString();

                                    Cursor cGroup = myDB.getContactGroup(selectedGroup);
                                    String gID = "";
                                    if(cGroup.moveToFirst()){
                                        gID = cGroup.getString(cGroup.getColumnIndex("id"));
                                        if(sDate != null){
                                            String strEventAlarm = formatter.format(sDate);
                                            strEventAlarm += " " + AlarmTime;
                                            Log.d("SkedText", "Event: " + EventDateTime );
                                            Log.d("SkedText", "Event Alarm: " + strEventAlarm);
                                            Date curDate = new Date();
                                            SimpleDateFormat testFormatter = new SimpleDateFormat("yyyyMMddHHmmSS");
                                            String uniqueID = testFormatter.format(curDate);
                                            Log.d("SkedTest", String.valueOf(uniqueID));
                                            if(myDB.saveMessage(gID, message, EventDateTime, strEventAlarm, uniqueID)){
                                                ArrayList<String> phoneNumbers = new ArrayList<String>();

                                                Cursor cLastID = myDB.getMessages();
                                                String msgID = "";
                                                if(cLastID.moveToFirst()){
                                                    msgID = cLastID.getString(cLastID.getColumnIndex("id"));
                                                    Log.d("Skedtest", "ID before sa save: " + String.valueOf(msgID));
                                                    uniqueID += msgID;
                                                    Cursor cNumbers = myDB.getContactPhone(gID);
                                                    if(cNumbers.moveToFirst()){
                                                        do{
                                                            String num = "09" + cNumbers.getString(cNumbers.getColumnIndex("phone_number"));
                                                            phoneNumbers.add(num);
                                                        }while(cNumbers.moveToNext());
                                                        if(isSimExists()){
                                                            long finalUniqueID = Long.parseLong(uniqueID);
                                                            for(int i = 0; i<phoneNumbers.size(); i++){
                                                                finalUniqueID++;
                                                                SMSManager smsManager = new SMSManager(getApplicationContext());
                                                                smsManager.setSmsSchedule(finalUniqueID, msgID, phoneNumbers.get(i), message, EventDateTime);
                                                                finalUniqueID++;
                                                                smsManager.setSmsSchedule(finalUniqueID, msgID, phoneNumbers.get(i), message,strEventAlarm);
                                                            }
                                                        }else{
                                                            setResult(RESULT_CANCELED, intentResultAddSMS);
                                                            finish();
                                                        }
                                                    }
                                                }
                                                setResult(RESULT_OK, intentResultAddSMS);
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

    /**
     * @return true if SIM card exists
     * false if SIM card is locked or doesn't exists <br/><br/>
     * <b>Note:</b> This method requires permissions <b> "android.permission.READ_PHONE_STATE" </b>
     */
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
                    Snackbar noSimSnack = Snackbar.make(activity_sched_message, "No Sim Found!", Snackbar.LENGTH_LONG);
                    noSimSnack.show();
                    break;
                case TelephonyManager.SIM_STATE_NETWORK_LOCKED: //SimState = "Network Locked!";
                    Snackbar noNetworkSnack = Snackbar.make(activity_sched_message, "Network Locked!", Snackbar.LENGTH_LONG);
                    noNetworkSnack.show();
                    break;
                case TelephonyManager.SIM_STATE_PIN_REQUIRED: //SimState = "PIN Required to access SIM!";
                    Snackbar pinReqSnack = Snackbar.make(activity_sched_message, "PIN Required to access SIM!", Snackbar.LENGTH_LONG);
                    pinReqSnack.show();
                    break;
                case TelephonyManager.SIM_STATE_PUK_REQUIRED: //SimState = "PUK Required to access SIM!"; // Personal Unblocking Code
                    Snackbar pukReqSnack = Snackbar.make(activity_sched_message, "PUK Required to access SIM!", Snackbar.LENGTH_LONG);
                    pukReqSnack.show();
                    break;
                case TelephonyManager.SIM_STATE_UNKNOWN: //SimState = "Unknown SIM State!";
                    Snackbar unknownSimSnack = Snackbar.make(activity_sched_message, "Unknown SIM State!", Snackbar.LENGTH_LONG);
                    unknownSimSnack.show();
                    break;
            }
            return false;
        }
    }

}
