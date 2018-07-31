package com.fnspl.hiplaedu_student.receiver;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.fnspl.hiplaedu_student.beaconeManager.MyService;
import com.fnspl.hiplaedu_student.service.ClassStartService;
import com.fnspl.hiplaedu_student.service.MyNavigationService;
import com.fnspl.hiplaedu_student.service.RoutineFetchService;
/**
 * Created by FNSPL on 9/5/2017.
 */

public class StartServiceBroadcastReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equalsIgnoreCase("STOP_WIFI_ZONE_SERVICE")) {
            //With Beacons
            /*if (isMyServiceRunning(context, MyService.class)) {
                Log.d("Testing", "Service is running!! Stopping...");
                context.stopService(new Intent(context, MyService.class));
            } else {
                Log.d("Testing", "Service not running");
            }*/

            //Navigine
            if (isMyServiceRunning(context, MyNavigationService.class)) {
                Log.d("Testing", "Service is running!! Stopping...");
                context.stopService(new Intent(context, MyNavigationService.class));
            } else {
                Log.d("Testing", "Service not running");
            }



        }else if (intent.getAction().equalsIgnoreCase("START_WIFI_ZONE_SERVICE")) {
            if (!isMyServiceRunning(context, ClassStartService.class)) {
                Log.d("Testing", "Service not running!! Starting...");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(new Intent(context, ClassStartService.class));
                } else {
                    context.startService(new Intent(context, ClassStartService.class));
                }
            } else {
                Log.d("Testing", "Service is running");
            }
        }else if (intent.getAction().equalsIgnoreCase("FETCH_DAILY_ROUTINE_SERVICE")) {
            if (!isMyServiceRunning(context, RoutineFetchService.class)) {
                Log.d("Testing", "Service not running!! Starting...");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(new Intent(context, RoutineFetchService.class));
                } else {
                    context.startService(new Intent(context, RoutineFetchService.class));
                }
            } else {
                Log.d("Testing", "Service is running");
            }
        }

    }

    private boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}