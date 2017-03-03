package com.example.skedtext.Sms;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;

import com.example.skedtext.DBHelper.SQLiteDatabaseHelper;

import static com.example.skedtext.Sms.SmsReceiver.SMS_ID;

/**
 * Created by solomon on 2/25/17.
 */

public class SmsDelivered extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("SkedTextSMSDelivered", String.valueOf(getResultCode()));
        SQLiteDatabaseHelper myDB = new SQLiteDatabaseHelper(context);
        String id = intent.getStringExtra(SMS_ID);
        Log.d("SkedTextSMSDelivered", "Intent ID: " + intent.getStringExtra(SMS_ID));
        switch (getResultCode())
        {
            case Activity.RESULT_OK:
                Toast.makeText(context.getApplicationContext(), "SMS delivered",
                        Toast.LENGTH_SHORT).show();
                myDB.messageChangeStatus(SQLiteDatabaseHelper.MESSAGE_SENT, id);
                break;
            case Activity.RESULT_CANCELED:
                Toast.makeText(context.getApplicationContext(), "SMS not delivered",
                        Toast.LENGTH_SHORT).show();
                break;
        }
        new IntentFilter(SmsReceiver.DELIVERED);
    }
}
