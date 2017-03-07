package com.example.skedtext.Sms;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.example.skedtext.DBHelper.SQLiteDatabaseHelper;

import static com.example.skedtext.Sms.SmsReceiver.SMS_ID;

/**
 * Created by solomon on 2/25/17.
 */

public class SmsSent extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SQLiteDatabaseHelper myDB = new SQLiteDatabaseHelper(context);
        String id = intent.getStringExtra(SMS_ID);
        switch (getResultCode())
        {
            case Activity.RESULT_OK:
                Toast.makeText(context.getApplicationContext(), "SMS sent",
                        Toast.LENGTH_SHORT).show();
                Log.d("SkedSMS", "RESULT_OK Sent");
                myDB.messageChangeStatus(SQLiteDatabaseHelper.MESSAGE_SENT, id);
                break;
            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                Toast.makeText(context.getApplicationContext(), "SMS not delivered, don't have enough load.",
                        Toast.LENGTH_SHORT).show();
                Log.d("SkedSMS", "RESULT_ERROR_GENERIC_FAILURE Sent");
                myDB.messageChangeStatus(SQLiteDatabaseHelper.MESSAGE_NO_LOAD, id);
                break;
            case SmsManager.RESULT_ERROR_NO_SERVICE:
                Toast.makeText(context.getApplicationContext(), "No service",
                        Toast.LENGTH_SHORT).show();
                Log.d("SkedSMS", "RESULT_ERROR_NO_SERVICE Sent");
                break;
            case SmsManager.RESULT_ERROR_NULL_PDU:
                Toast.makeText(context.getApplicationContext(), "Null PDU",
                        Toast.LENGTH_SHORT).show();
                Log.d("SkedSMS", "RESULT_ERROR_NULL_PDU Sent");
                break;
            case SmsManager.RESULT_ERROR_RADIO_OFF:
                Toast.makeText(context.getApplicationContext(), "Radio off",
                        Toast.LENGTH_SHORT).show();
                Log.d("SkedSMS", "RESULT_ERROR_RADIO_OFF Sent");
                break;
        }
    }


}
