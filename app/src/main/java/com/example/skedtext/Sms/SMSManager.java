package com.example.skedtext.Sms;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.example.skedtext.DBHelper.SQLiteDatabaseHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by solomon on 2/24/17.
 */

public class SMSManager {
    private Context context;
    private SQLiteDatabaseHelper myDB;
    private SimpleDateFormat formatter;
    private PendingIntent pendingIntent;
    private Intent smsIntent;

    public SMSManager(Context context){
        this.context = context;
        formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        myDB = new SQLiteDatabaseHelper(context);
        smsIntent = new Intent(context, SmsReceiver.class);
    }

    public void setSmsSchedule(long uniqueId, String msgID, String phoneNumber, String message, String eventDateTime) {

        Date dSched = new Date();
        Calendar sms_alarm = Calendar.getInstance();
        try {
            smsIntent.putExtra(SmsReceiver.SMS_ID, msgID);
            smsIntent.putExtra(SmsReceiver.SMS_NUMBER, phoneNumber);
            smsIntent.putExtra(SmsReceiver.SMS_MESSAGE, message);
            pendingIntent = PendingIntent.getBroadcast(context, (int) uniqueId, smsIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            dSched = formatter.parse(eventDateTime);
            sms_alarm.setTime(dSched);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, sms_alarm.getTimeInMillis(), pendingIntent);
            }else{
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, sms_alarm.getTimeInMillis(), 0, pendingIntent);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void cancelSmsSchedule(long uniqueIDs){
        pendingIntent = PendingIntent.getBroadcast(context, (int) uniqueIDs, smsIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

}
