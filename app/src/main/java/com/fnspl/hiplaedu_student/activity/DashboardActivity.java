package com.fnspl.hiplaedu_student.activity;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.util.Log;
import android.view.View;

import com.fnspl.hiplaedu_student.Networking.NetworkUtility;
import com.fnspl.hiplaedu_student.R;
import com.fnspl.hiplaedu_student.application.MainApplication;
import com.fnspl.hiplaedu_student.beaconeManager.MyService;
import com.fnspl.hiplaedu_student.database.Db_helper;
import com.fnspl.hiplaedu_student.databinding.ActivityDashboardBinding;
import com.fnspl.hiplaedu_student.fragment.AttendenceReportFragment;
import com.fnspl.hiplaedu_student.fragment.NavigineMapDialogNew;
import com.fnspl.hiplaedu_student.fragment.PathFinderMapDialog;
import com.fnspl.hiplaedu_student.fragment.ProfileInfoFragment;
import com.fnspl.hiplaedu_student.fragment.RoutineFragment;
import com.fnspl.hiplaedu_student.beaconeManager.Constants;
import com.fnspl.hiplaedu_student.model.ProfileInfo;
import com.fnspl.hiplaedu_student.model.RoutinePeriod;
import com.fnspl.hiplaedu_student.service.MyNavigationService;
import com.fnspl.hiplaedu_student.service.RoutineFetchService;
import com.fnspl.hiplaedu_student.utils.CONST;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.paperdb.Paper;

public class DashboardActivity extends BaseActivity {

    private ActivityDashboardBinding binding_dashboard;
    private final String PROFILE = "profileFragment";
    private final String ROUTINE = "routineFragment";
    private final String REPORT = "reportFragment";
    private ProgressDialog pDialog;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
    private AlertBroadcastReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Constants.activity = DashboardActivity.this;

        setBluetoothEnable(true);

        binding_dashboard = DataBindingUtil.setContentView(DashboardActivity.this, R.layout.activity_dashboard);
        binding_dashboard.setDashboard(DashboardActivity.this);

        if (Paper.book().read(CONST.PROFILE_INFO) != null) {
            ProfileInfo profileInfo = Paper.book().read(CONST.PROFILE_INFO);
            ImageLoader.getInstance().displayImage(NetworkUtility.IMAGE_BASEURL + "" + profileInfo.getPhoto(),
                    binding_dashboard.ivProfilePic, CONST.ErrorWithLoaderRoundedCorner);
            binding_dashboard.tvName.setText(String.format("%s", profileInfo.getName()));
        }

        setFragment(new ProfileInfoFragment(), PROFILE);

        binding_dashboard.llRoutine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding_dashboard.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding_dashboard.drawerLayout.closeDrawer(GravityCompat.START);
                }

                setFragment(new RoutineFragment(), ROUTINE);

                if (Paper.book().read(CONST.LOGIN_FOR_FIRST_TIME) == null) {
                    if (!isMyServiceRunning(DashboardActivity.this, RoutineFetchService.class))
                        startService(new Intent(getApplicationContext(), RoutineFetchService.class));
                }
            }
        });

        binding_dashboard.llReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding_dashboard.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding_dashboard.drawerLayout.closeDrawer(GravityCompat.START);
                }

                setFragment(new AttendenceReportFragment(), REPORT);

                if (Paper.book().read(CONST.LOGIN_FOR_FIRST_TIME) == null) {
                    if (!isMyServiceRunning(DashboardActivity.this, RoutineFetchService.class))
                        startService(new Intent(getApplicationContext(), RoutineFetchService.class));
                }
            }
        });

        binding_dashboard.ivProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding_dashboard.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding_dashboard.drawerLayout.closeDrawer(GravityCompat.START);
                }

                setFragment(new ProfileInfoFragment(), PROFILE);
            }
        });

        binding_dashboard.llPathFinder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (binding_dashboard.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding_dashboard.drawerLayout.closeDrawer(GravityCompat.START);
                }

                PathFinderMapDialog mapDialog = new PathFinderMapDialog();
                Bundle bundle = new Bundle();
                mapDialog.setArguments(bundle);
                if (mapDialog != null && mapDialog.getDialog() != null
                        && mapDialog.getDialog().isShowing()) {
                    //dialog is showing so do something
                } else {
                    //dialog is not showing
                    mapDialog.show(getSupportFragmentManager(), "mapDialog");
                }

                if (Paper.book().read(CONST.LOGIN_FOR_FIRST_TIME) == null) {
                    if (!isMyServiceRunning(DashboardActivity.this, RoutineFetchService.class))
                        startService(new Intent(getApplicationContext(), RoutineFetchService.class));
                }

            }
        });
    }

    @Override
    public void onBackPressed() {
        if (binding_dashboard.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding_dashboard.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            Fragment oldfragment = getSupportFragmentManager().findFragmentByTag(PROFILE);
            if (oldfragment != null && oldfragment.isVisible()) {
                finish();
            } else {
                setFragment(new ProfileInfoFragment(), PROFILE);
            }
        }
    }

    public void setFragment(Fragment fragment, String fragmentName) {

        android.support.v4.app.FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        Fragment oldfragment = getSupportFragmentManager().findFragmentByTag(fragmentName);
        if (oldfragment == null) {
            //t.addToBackStack(null);
            t.replace(R.id.fragment_container, fragment, fragmentName);
        } else if (!oldfragment.isAdded()) {
            t.replace(R.id.fragment_container, oldfragment, fragmentName);
        }

        t.commit();
    }

    public void openDrawerOnClick() {
        if (!binding_dashboard.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding_dashboard.drawerLayout.openDrawer(GravityCompat.START);
        } else {
            binding_dashboard.drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    public void logout() {
        if (binding_dashboard.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding_dashboard.drawerLayout.closeDrawer(GravityCompat.START);
        }

        startActivity(new Intent(DashboardActivity.this, LoginActivity.class));
        supportFinishAfterTransition();
        overridePendingTransition(R.anim.slideinfromleft, R.anim.slideouttoright);
    }

    @Override
    protected void onResume() {
        super.onResume();

        MainApplication.isNotificationScreen = false;

        if (Paper.book().read(CONST.LOGIN_FOR_FIRST_TIME) == null) {
            if (!isMyServiceRunning(DashboardActivity.this, RoutineFetchService.class))
                startService(new Intent(getApplicationContext(), RoutineFetchService.class));
        } else {
            if (Paper.book().read(CONST.CURRENT_PERIOD) == null) {
                setUpNextClassTimer();
            } else if(Paper.book().read(CONST.ATTENDANCE_CONFIRMED, false)){
                //With Becones
                /*if (!isMyServiceRunning(DashboardActivity.this, MyService.class))
                    startService(new Intent(getApplicationContext(), MyService.class));*/

                //With Navigine
                if (!isMyServiceRunning(DashboardActivity.this, MyNavigationService.class))
                    startService(new Intent(getApplicationContext(), MyNavigationService.class));
            }else{
                //checkForClass();
            }
        }

        updateDeviceInfo();

        IntentFilter intentFilter = new IntentFilter(
                "android.intent.action.SHOWALEART");

        mReceiver = new AlertBroadcastReceiver();
        registerReceiver(mReceiver, intentFilter);

    }

    @Override
    protected void onPause() {
        super.onPause();

        if(mReceiver!=null){
            unregisterReceiver(mReceiver);
        }
    }

    public class AlertBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {

                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(DashboardActivity.this, android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(DashboardActivity.this);
                }

                builder.setTitle(getString(R.string.wraning))
                        .setMessage(getString(R.string.zone_notification))
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // continue with delete

                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void updateDeviceInfo() {
        ProfileInfo profileInfo = Paper.book().read(CONST.PROFILE_INFO);
        if (profileInfo != null) {
            String urlParameters = "userid=" + profileInfo.getId() +
                    "&usertype=student" +
                    "&device_type=Android" +
                    "&device_token=" + Paper.book().read(CONST.TOKEN, "");

            new APIRequest().execute(urlParameters);
        }
    }

    private class APIRequest extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String result = "";
            HttpURLConnection urlConnection = null;

            try {
                Log.d("Tester", "Before request");
                URL url = new URL(NetworkUtility.BASEURL + NetworkUtility.DEVICE_INFO_UPDATE);
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

            /*pDialog = new ProgressDialog(DashboardActivity.this);
            pDialog.setMessage(getString(R.string.dialog_msg));
            pDialog.setCancelable(false);
            pDialog.show();*/

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            /*if (pDialog.isShowing()) {
               pDialog.dismiss();
            }*/

        }
    }

    private void setUpNextClassTimer() {
        try {
            Date currentDateTime = new Date();

            Db_helper db_helper = new Db_helper(getApplicationContext());

            List<RoutinePeriod> routinePeriodList = db_helper.getRoutine(new SimpleDateFormat("EEEE").
                    format(currentDateTime).toLowerCase());

            for (int i=0; i<routinePeriodList.size(); i++) {

                Date classDateTime = dateFormat.parse(new SimpleDateFormat("yyyy-MM-dd").format(currentDateTime)+" "+routinePeriodList.get(i).getStartTime());
                Calendar cal = Calendar.getInstance();
                cal.setTime(classDateTime);
                cal.set(Calendar.MINUTE, -2);

                if(currentDateTime.compareTo(classDateTime)>0){
                    if(i==(routinePeriodList.size()-1)){
                        Paper.book().delete(CONST.LOGIN_FOR_FIRST_TIME);
                    }
                }else{
                    Paper.book().write(CONST.CURRENT_PERIOD, routinePeriodList.get(i).getRoutine_history_id());
                    Paper.book().delete(CONST.CLASS_STARTED);
                    Paper.book().delete(CONST.ATTENDANCE_STARTED);
                    Paper.book().delete(CONST.MANUAL_ATTENDANCE_STARTED);
                    Paper.book().delete(CONST.FACIAL_ATTENDANCE_STARTED);
                    Paper.book().delete(CONST.ATTENDANCE_CONFIRMED);

                    setClassStartService(getApplicationContext(), classDateTime);

                    break;
                }

            }

        } catch (Exception ex) {

        }

    }

    private void checkForClass() {
        try {
            Date currentDateTime = new Date();

            Db_helper db_helper = new Db_helper(getApplicationContext());

            List<RoutinePeriod> routinePeriodList = db_helper.getRoutine(new SimpleDateFormat("EEEE").
                    format(currentDateTime).toLowerCase());

            for (int i = 0; i < routinePeriodList.size(); i++) {

                Date classDateTime = dateFormat.parse(new SimpleDateFormat("yyyy-MM-dd").format(currentDateTime) + " " + routinePeriodList.get(i).getEndTime());
                Calendar cal = Calendar.getInstance();
                cal.setTime(classDateTime);
                cal.set(Calendar.MINUTE, -2);

                if (currentDateTime.compareTo(classDateTime) > 0) {
                    if (i == (routinePeriodList.size() - 1)) {
                        Paper.book().delete(CONST.LOGIN_FOR_FIRST_TIME);
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
        alarmManager.setInexactRepeating(ALARM_TYPE, calendar.getTimeInMillis() - 120000, 1 * 24 * 60 * 60 * 1000, pendingIntent);
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
