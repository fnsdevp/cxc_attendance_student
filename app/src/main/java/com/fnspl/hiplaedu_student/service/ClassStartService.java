package com.fnspl.hiplaedu_student.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.fnspl.hiplaedu_student.activity.NotificationHandleActivity;
import com.fnspl.hiplaedu_student.application.MainApplication;
import com.fnspl.hiplaedu_student.utils.CONST;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.paperdb.Paper;

public class ClassStartService extends Service {

    private TimerTask mTimerTask = null;
    private Timer mTimer = new Timer();
    private Handler mHandler = new Handler();
    private static final int UPDATE_TIMEOUT = 1*45*1000;
    private String macAddress="";

    public ClassStartService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //
        Log.d("Tester", "Service started");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startForeground(1001, new Notification());

        if(Paper.book().read(CONST.NEXT_PERIOD)!=null){
            Paper.book().write(CONST.CURRENT_PERIOD, Paper.book().read(CONST.NEXT_PERIOD));

            Paper.book().delete(CONST.NEXT_PERIOD);
        }

        Paper.book().delete(CONST.TIME_LEFT);
        Paper.book().write(CONST.CLASS_STARTED, true);
        Paper.book().delete(CONST.ATTENDANCE_STARTED);
        Paper.book().delete(CONST.MANUAL_ATTENDANCE_STARTED);
        Paper.book().delete(CONST.FACIAL_ATTENDANCE_STARTED);
        Paper.book().delete(CONST.ATTENDANCE_CONFIRMED);

        if(!MainApplication.isNotificationScreen) {
            launchWelcomeClass(NotificationHandleActivity.CLASS_START);
        }else{
            finishActivity(NotificationHandleActivity.CLASS_START);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //
        Log.d("Tester", "Service destroyed");
        mTimer.cancel();
        mTimerTask = null;
    }

    private void launchWelcomeClass(String notificationtype) {
        //sendBroadcast(new Intent(getResources().getString(R.string.class_start)));
        Intent notificationHandle = new Intent(ClassStartService.this, NotificationHandleActivity.class);
        notificationHandle.putExtra(CONST.NOTIFICATION_TYPE, notificationtype);
        notificationHandle.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(notificationHandle);

        stopSelf();
    }

    private void finishActivity(String notificationtype) {
        Intent zoneInfo = new Intent("android.intent.action.FINSIHACTIVITY");
        zoneInfo.putExtra(CONST.NOTIFICATION_TYPE, notificationtype);
        sendBroadcast(zoneInfo);

        stopSelf();
    }

    public String getMacAddr() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(Integer.toHexString(b & 0xFF) + ":");
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
            //handle exception
        }
        return "";
    }


}
