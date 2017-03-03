package com.example.skedtext.Sms;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;

/**
 * Created by solomon on 2/25/17.
 */

public class SmsReceiver extends BroadcastReceiver{

    public static final String SMS_ID = "sms_id";
    public static final String SMS_NUMBER = "sms_number";
    public static final String SMS_MESSAGE = "sms_message";
    public static String SENT = "SMS_SENT";
    public static String DELIVERED = "SMS_DELIVERED";

    @Override
    public void onReceive(Context context, Intent intent) {
        String id = intent.getStringExtra(SMS_ID);
        String phoneNumber = intent.getStringExtra(SMS_NUMBER);
        String message = intent.getStringExtra(SMS_MESSAGE);

        Intent intentSent = new Intent(context, SmsSent.class);
        intentSent.putExtra(SMS_ID, id);
        Intent intentDelivered = new Intent(context, SmsSent.class);
        intentDelivered.putExtra(SMS_ID, id);

        PendingIntent sentPI = PendingIntent.getBroadcast(context, 0, intentSent, PendingIntent.FLAG_CANCEL_CURRENT);

        PendingIntent deliveredPI = PendingIntent.getBroadcast(context, 0, intentDelivered, PendingIntent.FLAG_CANCEL_CURRENT);

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
    }
}
