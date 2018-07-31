package com.fnspl.hiplaedu_student.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.fnspl.hiplaedu_student.application.MainApplication;

import static android.R.attr.format;

/**
 * Created by FNSPL on 9/8/2017.
 */

public class SmsReceiver extends BroadcastReceiver {

    private static SmsListener mListener;
    private String OTP = "";

    @Override
    public void onReceive(Context context, Intent intent) {
        if(MainApplication.isOTPScreen) {
            Bundle data = intent.getExtras();

            Object[] pdus = (Object[]) data.get("pdus");

            for (int i = 0; i < pdus.length; i++) {
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdus[i]);

                String sender = smsMessage.getDisplayOriginatingAddress();
                if (sender.contains("EEGRAB")) {
                    //You must check here if the sender is your provider and not another one with same text.
                    String messageBody = smsMessage.getMessageBody();
                    OTP = messageBody.replaceAll("Your one time password is", "");
                    OTP = OTP.replace(".","");
                    Log.d("Tester", OTP);
                    //Pass on the text to our listener.
                    if (mListener != null)
                        mListener.messageReceived(OTP.trim());
                }

            }
        }
    }

    public static void bindListener(SmsListener listener) {
        mListener = listener;
    }

    public interface SmsListener {
        void messageReceived(String messageText);
    }
}
