package com.fnspl.hiplaedu_student.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.fnspl.hiplaedu_student.Networking.NetworkUtility;
import com.fnspl.hiplaedu_student.database.Db_helper;
import com.fnspl.hiplaedu_student.model.ProfileInfo;
import com.fnspl.hiplaedu_student.model.RoutinePeriod;
import com.fnspl.hiplaedu_student.model.Teacher_details;
import com.fnspl.hiplaedu_student.utils.CONST;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.paperdb.Paper;

public class RoutineFetchService extends Service {

    private TimerTask mTimerTask = null;
    private Timer mTimer = new Timer();
    private Handler mHandler = new Handler();
    private static final int UPDATE_TIMEOUT = 1 * 60 * 1000;
    private String macAddress = "";
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
    private int i = 0;

    public RoutineFetchService() {

    }

    private Runnable mRunnable =
            new Runnable() {
                public void run() {
                    new RoutineFetch().execute();
                }
            };

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
            startForeground(1003, new Notification());

        /*launchWelcomeClass(NotificationHandleActivity.CLASS_START);
        setClassStartService(getApplicationContext());*/

        if (Paper.book().read(CONST.PROFILE_INFO) != null) {
            if (isOnline()) {
                fetchRoutine();
            } else {
                setRoutineFetchService(getApplicationContext(), 180000);
            }
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //
        Log.d("Tester", "Service destroyed");
    }

    private void fetchRoutine() {
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        ProfileInfo profileInfo = Paper.book().read(CONST.PROFILE_INFO);

        String urlParameters = "userid=" + profileInfo.getId() +
                "&usertype=student" +
                "&date=" + date +
                "&device_type=Android";

        new RoutineFetch().execute(urlParameters);
    }

    private class RoutineFetch extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String result = "";
            HttpURLConnection urlConnection = null;
            try {
                Log.d("Tester", "Before request");
                URL url = new URL(NetworkUtility.BASEURL + NetworkUtility.ROUTINE_FETCH);
                String urlParameters = params[0];

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");
                connection.setRequestProperty("USER-AGENT", "Mozilla/5.0");
                connection.setRequestProperty("ACCEPT-LANGUAGE", "en-US,en;0.5");
                connection.setDoOutput(true);

                DataOutputStream dStream = new DataOutputStream(connection.getOutputStream());
                dStream.writeBytes(urlParameters);
                dStream.flush();
                dStream.close();
                int responseCode = connection.getResponseCode();

                final StringBuilder output = new StringBuilder("Request URL " + url);
                output.append(System.getProperty("line.separator") + "Request Parameters " + urlParameters);
                output.append(System.getProperty("line.separator") + "Response Code " + responseCode);
                output.append(System.getProperty("line.separator") + "Type " + "POST");
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = "";
                StringBuilder responseOutput = new StringBuilder();

                while ((line = br.readLine()) != null) {
                    responseOutput.append(line);
                }
                br.close();

                result = responseOutput.toString();

            } catch (Exception e1) {
                e1.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                return result;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //macAddress = getMacAddr();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            List<RoutinePeriod> routinePeriodsList = new ArrayList<>();

            setRoutineFetchService(getApplicationContext());

            try {
                JSONObject response = new JSONObject(s);

                if (response.getString("status").equalsIgnoreCase("success")) {
                    GsonBuilder builder = new GsonBuilder();
                    builder.setPrettyPrinting();
                    Gson gson = builder.create();

                    JSONObject user_routine = response.getJSONArray("user_routine").getJSONObject(0);
                    JSONArray routineArray = user_routine.getJSONArray("routine");

                    for (int i = 0; i < routineArray.length(); i++) {
                        RoutinePeriod routinePeriod = gson.fromJson(routineArray.getJSONObject(i).toString(),
                                RoutinePeriod.class);
                        Teacher_details teacher_details = gson.fromJson(routineArray.getJSONObject(i).getJSONObject("teacher_details")
                                .toString(), Teacher_details.class);

                        routinePeriod.setTeacher_details(teacher_details);

                        routinePeriodsList.add(routinePeriod);
                    }

                    Db_helper db_helper = new Db_helper(getApplicationContext());
                    db_helper.deleteRoutine();

                    db_helper.insertAllRoutines(routinePeriodsList);
                    db_helper.getWritableDatabase().close();

                    if (routinePeriodsList.size() > 0)
                        Paper.book().write(CONST.LOGIN_FOR_FIRST_TIME, false);
                    else
                        Paper.book().delete(CONST.LOGIN_FOR_FIRST_TIME);

                    if (!Paper.book().read(CONST.CLASS_STARTED, false) && !Paper.book().read(CONST.ATTENDANCE_STARTED, false) &&
                            !Paper.book().read(CONST.MANUAL_ATTENDANCE_STARTED, false) && !Paper.book().read(CONST.FACIAL_ATTENDANCE_STARTED, false)
                            && !Paper.book().read(CONST.ATTENDANCE_CONFIRMED, false)) {

                        setUpNextClassTimer();
                    }

                    stopSelf();
                } else {
                    if (i < 4) {
                        i++;
                        fetchRoutine();
                    } else {
                        stopSelf();
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
                //Toast.makeText(getApplicationContext(), "JSON Exception", Toast.LENGTH_SHORT).show();
                if (i < 4) {
                    i++;
                    fetchRoutine();
                } else {
                    stopSelf();
                }
            }
        }

    }

    private void setUpNextClassTimer() {
        try {
            Date currentDateTime = new Date();

            Db_helper db_helper = new Db_helper(getApplicationContext());

            List<RoutinePeriod> routinePeriodList = db_helper.getRoutine(new SimpleDateFormat("EEEE").
                    format(currentDateTime).toLowerCase());

            for (int i = 0; i < routinePeriodList.size(); i++) {

                Date classDateTime = dateFormat.parse(new SimpleDateFormat("yyyy-MM-dd").format(currentDateTime) + " " + routinePeriodList.get(i).getStartTime());
                Calendar cal = Calendar.getInstance();
                cal.setTime(classDateTime);
                cal.set(Calendar.MINUTE, -2);

                if (currentDateTime.compareTo(classDateTime) > 0) {
                    if (i == (routinePeriodList.size() - 1)) {
                        Paper.book().delete(CONST.LOGIN_FOR_FIRST_TIME);
                    }
                } else {
                    if (Paper.book().read(CONST.CURRENT_PERIOD, 0) != routinePeriodList.get(i).getRoutine_history_id()) {
                        Paper.book().write(CONST.CURRENT_PERIOD, routinePeriodList.get(i).getRoutine_history_id());
                        Paper.book().delete(CONST.CLASS_STARTED);
                        Paper.book().delete(CONST.ATTENDANCE_STARTED);
                        Paper.book().delete(CONST.MANUAL_ATTENDANCE_STARTED);
                        Paper.book().delete(CONST.FACIAL_ATTENDANCE_STARTED);
                        Paper.book().delete(CONST.ATTENDANCE_CONFIRMED);

                        setClassStartService(getApplicationContext(), classDateTime);

                        break;
                    } else {
                        if (i == (routinePeriodList.size() - 1)) {
                            Paper.book().delete(CONST.LOGIN_FOR_FIRST_TIME);
                        }
                    }
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

        int ALARM_TYPE = AlarmManager.RTC_WAKEUP;

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            alarmManager.setExactAndAllowWhileIdle(ALARM_TYPE, calendar.getTimeInMillis()-120000, pendingIntent);
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            alarmManager.setExact(ALARM_TYPE, calendar.getTimeInMillis()-120000, pendingIntent);
        else*/
        alarmManager.set(ALARM_TYPE, calendar.getTimeInMillis() - 119500, pendingIntent);
    }

    public void setRoutineFetchService(Context mContext) {
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), CONST.ROUTINE_FETCH_ID,
                new Intent().setAction("FETCH_DAILY_ROUTINE_SERVICE"), PendingIntent.FLAG_UPDATE_CURRENT);

        // reset previous pending intent
        alarmManager.cancel(pendingIntent);

        // Set the alarm to start at approximately 08:00 morning.
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + 1);
        calendar.set(Calendar.HOUR_OF_DAY, 9);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    public void setRoutineFetchService(Context mContext, long time) {
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), CONST.ROUTINE_FETCH_ID,
                new Intent().setAction("FETCH_DAILY_ROUTINE_SERVICE"), PendingIntent.FLAG_UPDATE_CURRENT);

        // reset previous pending intent
        alarmManager.cancel(pendingIntent);

        alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
    }

    protected boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }
    }

}
