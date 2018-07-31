package com.fnspl.hiplaedu_student.firebase;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.fnspl.hiplaedu_student.R;
import com.fnspl.hiplaedu_student.activity.DashboardActivity;
import com.fnspl.hiplaedu_student.activity.LoginActivity;
import com.fnspl.hiplaedu_student.activity.NotificationHandleActivity;
import com.fnspl.hiplaedu_student.application.MainApplication;
import com.fnspl.hiplaedu_student.beaconeManager.MyService;
import com.fnspl.hiplaedu_student.database.Db_helper;
import com.fnspl.hiplaedu_student.model.RoutinePeriod;
import com.fnspl.hiplaedu_student.service.MyNavigationService;
import com.fnspl.hiplaedu_student.service.RoutineFetchService;
import com.fnspl.hiplaedu_student.utils.CONST;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import io.paperdb.Paper;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private String TAG = "Firebase";

    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a");

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.d(TAG, "Notification Rewceived");

        if (remoteMessage != null && remoteMessage.getData() != null) {
            Log.d(TAG, "From: " + remoteMessage.getFrom());
            //Log.d(TAG, "Notification TripMessageData Body: " + remoteMessage.getNotification().getBody());
            Log.d(TAG, "Notification TripMessageData Data: " + remoteMessage.getData().toString());

            Map<String, String> data = remoteMessage.getData();

            ActivityManager am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
            // Get info from the currently active task
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            String activityName = taskInfo.get(0).topActivity.getClassName();

            //String body = remoteMessage.getNotification().getBody();

            try {

                if (data.containsKey("pushType") && data.get("pushType").equalsIgnoreCase("start_attendance")) {

                    try {

                        setBluetoothEnable(true);

                        Paper.book().write(CONST.CURRENT_PERIOD, Integer.parseInt(data.get("studentroutine_historyid")));
                        Paper.book().delete(CONST.CLASS_STARTED);
                        Paper.book().write(CONST.ATTENDANCE_STARTED, true);
                        Paper.book().delete(CONST.MANUAL_ATTENDANCE_STARTED);
                        Paper.book().delete(CONST.FACIAL_ATTENDANCE_STARTED);
                        Paper.book().delete(CONST.ATTENDANCE_CONFIRMED);
                        Paper.book().delete(CONST.TIME_LEFT);

                        if (!MainApplication.isNotificationScreen)
                            launchWelcomeClass(NotificationHandleActivity.ATTENDANCE_START);
                        else
                            finishActivity(NotificationHandleActivity.ATTENDANCE_START);

                        //With Becones
                    /*if (!isMyServiceRunning(getApplicationContext(), MyService.class))
                        startService(new Intent(getApplicationContext(), MyService.class));*/

                        //With Navigine
                    /*if (!isMyServiceRunning(getApplicationContext(), MyNavigationService.class))
                        startService(new Intent(getApplicationContext(), MyNavigationService.class));*/

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else if (data.containsKey("pushType") && data.get("pushType").equalsIgnoreCase("stop_attendance")) {

                    try {

                        Paper.book().delete(CONST.CLASS_STARTED);
                        Paper.book().delete(CONST.ATTENDANCE_STARTED);
                        Paper.book().delete(CONST.MANUAL_ATTENDANCE_STARTED);
                        Paper.book().delete(CONST.FACIAL_ATTENDANCE_STARTED);
                        Paper.book().delete(CONST.ATTENDANCE_CONFIRMED);
                        Paper.book().delete(CONST.TIME_LEFT);

                        if (MainApplication.isNotificationScreen)
                            finishActivity(NotificationHandleActivity.ATTENDANCE_STOP);

                        setUpNextClassTimer();

                        //Paper.book().delete(CONST.CURRENT_PERIOD);
                        //launchWelcomeClass(NotificationHandleActivity.ATTENDANCE_START);

                        //With Becones
                    /*if (isMyServiceRunning(getApplicationContext(), MyService.class))
                        stopService(new Intent(getApplicationContext(), MyService.class));*/

                        //With Navigine
                        if (isMyServiceRunning(getApplicationContext(), MyNavigationService.class))
                            stopService(new Intent(getApplicationContext(), MyNavigationService.class));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else if (data.containsKey("pushType") && data.get("pushType").equalsIgnoreCase("manual_attendance")) {

                    try {

                        Paper.book().delete(CONST.CLASS_STARTED);
                        Paper.book().delete(CONST.ATTENDANCE_STARTED);
                        Paper.book().delete(CONST.MANUAL_ATTENDANCE_STARTED);
                        Paper.book().delete(CONST.FACIAL_ATTENDANCE_STARTED);
                        Paper.book().write(CONST.ATTENDANCE_CONFIRMED, true);
                        Paper.book().delete(CONST.TIME_LEFT);

                        if (!MainApplication.isNotificationScreen)
                            launchWelcomeClass(NotificationHandleActivity.ATTENDANCE_SUCCESS);
                        else
                            finishActivity(NotificationHandleActivity.ATTENDANCE_SUCCESS);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else if (data.containsKey("pushType") && data.get("pushType").equalsIgnoreCase("manual_absent_attendance")) {

                    Toast.makeText(getApplicationContext(), "You have mark absent for today.", Toast.LENGTH_SHORT).show();
                }  else if (data.containsKey("pushType") && data.get("pushType").equalsIgnoreCase("mark_attendance")) {

                    Toast.makeText(getApplicationContext(), "You have mark yourself present for today.", Toast.LENGTH_SHORT).show();
                } else if (data.containsKey("action") && data.get("action").equalsIgnoreCase("routine_updated")) {

                    //startService(new Intent(getApplicationContext(), RoutineFetchService.class));
                    setRoutineFetchService(getApplicationContext());

                } else if (data.containsKey("action") && data.get("action").equalsIgnoreCase("user_modification")) {
                    Paper.book().write(CONST.UPDATE_PROFILE, true);
                }else if (data.containsKey("action") && data.get("action").equalsIgnoreCase("student_sneakout")) {

                    Paper.book().delete(CONST.CLASS_STARTED);
                    Paper.book().delete(CONST.ATTENDANCE_STARTED);
                    Paper.book().delete(CONST.MANUAL_ATTENDANCE_STARTED);
                    Paper.book().delete(CONST.FACIAL_ATTENDANCE_STARTED);
                    Paper.book().delete(CONST.ATTENDANCE_CONFIRMED);
                    Paper.book().delete(CONST.TIME_LEFT);

                    if (MainApplication.isNotificationScreen)
                        finishActivity(NotificationHandleActivity.ATTENDANCE_STOP);

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            //sendNotification(data.get("pushType")+data.get("body"));
        }

    }

    private void finishActivity(String notificationtype) {
        Intent zoneInfo = new Intent("android.intent.action.FINSIHACTIVITY");
        zoneInfo.putExtra(CONST.NOTIFICATION_TYPE, notificationtype);
        sendBroadcast(zoneInfo);
    }

    private void launchWelcomeClass(String notificationtype) {
        //sendBroadcast(new Intent(getResources().getString(R.string.class_start)));
        Intent notificationHandle = new Intent(MyFirebaseMessagingService.this, NotificationHandleActivity.class);
        notificationHandle.putExtra(CONST.NOTIFICATION_TYPE, notificationtype);
        notificationHandle.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(notificationHandle);
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();

    }

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String message) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(getNotificationIcon())
                        .setColor(ContextCompat.getColor(getBaseContext(), R.color.colorPrimary))
                        .setVibrate(new long[]{1000, 1000, 1000})
                        .setLights(Color.RED, 3000, 3000)
                        .setContentTitle(getResources().getString(R.string.app_name))
                        .setContentText("" + message)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(message));
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, LoginActivity.class);
        //resultIntent.putExtra("NotificationMessage", "This is from notification");

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(LoginActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setAutoCancel(true);
        mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(0, mBuilder.build());
    }


    private int getNotificationIcon() {
        boolean useWhiteIcon = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.mipmap.ic_launcher_round : R.mipmap.ic_launcher;
    }

    private void setUpNextClassTimer() {
        try {
            Date currentDateTime = new Date();

            Db_helper db_helper = new Db_helper(getApplicationContext());

            List<RoutinePeriod> routinePeriodList = db_helper.getRoutine(new SimpleDateFormat("EEEE").
                    format(currentDateTime).toLowerCase());

            for (int i = 0; i < routinePeriodList.size(); i++) {

                Date classDateTime = dateFormat.parse(new SimpleDateFormat("yyyy-MM-dd").format(currentDateTime) + " " +
                        routinePeriodList.get(i).getStartTime());
                if (Paper.book().read(CONST.CURRENT_PERIOD, 0) >= routinePeriodList.get(i).getRoutine_history_id()) {
                    if (i == (routinePeriodList.size() - 1)) {
                        Paper.book().delete(CONST.LOGIN_FOR_FIRST_TIME);
                        //Paper.book().delete(CONST.CURRENT_PERIOD);

                        if (!isMyServiceRunning(getApplicationContext(), RoutineFetchService.class))
                            startService(new Intent(getApplicationContext(), RoutineFetchService.class));
                    }
                } else {
                    Paper.book().write(CONST.CURRENT_PERIOD, routinePeriodList.get(i).getRoutine_history_id());
                    Paper.book().delete(CONST.ATTENDANCE_STARTED);
                    Paper.book().delete(CONST.CLASS_STARTED);
                    Paper.book().delete(CONST.MANUAL_ATTENDANCE_STARTED);
                    Paper.book().delete(CONST.FACIAL_ATTENDANCE_STARTED);
                    Paper.book().delete(CONST.ATTENDANCE_CONFIRMED);
                    Paper.book().delete(CONST.NEXT_PERIOD);

                    setClassStartService(getApplicationContext(), classDateTime);

                    break;
                }

            }

        } catch (Exception ex) {

        }

    }

    public void setClassStartService(Context mContext, Date periodDate) {
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), CONST.CLASS_START_ID,
                new Intent().setAction("START_WIFI_ZONE_SERVICE"), PendingIntent.FLAG_UPDATE_CURRENT);

        // reset previous pending intent
        alarmManager.cancel(pendingIntent);

        // Set the alarm to start at approximately 08:00 morning.
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(periodDate);

        // if the scheduler date is passed, move scheduler time to tomorrow
        if (System.currentTimeMillis() > calendar.getTimeInMillis()) {
            //calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() - 120000, pendingIntent);
    }

    private boolean setBluetoothEnable(boolean enable) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        boolean isEnabled = false;

        if (bluetoothAdapter != null) {
            isEnabled = bluetoothAdapter.isEnabled();
        } else {
            return false;
        }

        if (enable && !isEnabled) {
            return bluetoothAdapter.enable();
        } else if (!enable && isEnabled) {
            return bluetoothAdapter.disable();
        }
        // No need to change bluetooth state
        return true;
    }

    public void setRoutineFetchService(Context mContext) {
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), CONST.ROUTINE_FETCH_ID,
                new Intent().setAction("FETCH_DAILY_ROUTINE_SERVICE"), PendingIntent.FLAG_UPDATE_CURRENT);

        // reset previous pending intent
        alarmManager.cancel(pendingIntent);


        alarmManager.set(AlarmManager.RTC_WAKEUP, 12000, pendingIntent);
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
