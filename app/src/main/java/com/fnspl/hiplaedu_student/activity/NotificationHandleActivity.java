package com.fnspl.hiplaedu_student.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.fnspl.hiplaedu_student.R;
import com.fnspl.hiplaedu_student.application.MainApplication;
import com.fnspl.hiplaedu_student.databinding.ActivityWelcomeClassBinding;
import com.fnspl.hiplaedu_student.fragment.AttendanceStartFragment;
import com.fnspl.hiplaedu_student.fragment.AttendanceSuccessFragment;
import com.fnspl.hiplaedu_student.fragment.ClassStartFragment;
import com.fnspl.hiplaedu_student.fragment.FacialRecognitionFragment;
import com.fnspl.hiplaedu_student.fragment.ManualAttendanceFragment;
import com.fnspl.hiplaedu_student.beaconeManager.Constants;
import com.fnspl.hiplaedu_student.utils.CONST;
import com.navigine.naviginesdk.NavigineSDK;

import io.paperdb.Paper;

public class NotificationHandleActivity extends BaseActivity {

    private ActivityWelcomeClassBinding binding_welcome_class;
    private PowerManager.WakeLock wakeLock;
    public static final String CLASS_START = "ClassStart";
    public static final String FACIAL_ATTENDANCE = "facialAttendance";
    public static final String ATTENDANCE_SUCCESS = "attendanceSuccess";
    public static final String ATTENDANCE_START = "attendanceStart";
    public static final String MANUAL_ATTENDANCE = "manualAttendance";
    public static final String ATTENDANCE_STOP = "attendanceStop";
    private ClassFinishBroadcastReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Constants.activity = NotificationHandleActivity.this;

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        binding_welcome_class = DataBindingUtil.setContentView(NotificationHandleActivity.this, R.layout.activity_welcome_class);
        binding_welcome_class.setWelcomeClass(NotificationHandleActivity.this);

        binding_welcome_class.imgDrawerToggel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NotificationHandleActivity.this, DashboardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        if(Paper.book().read(CONST.PROFILE_INFO)!=null) {
            if (getIntent() != null && getIntent().getStringExtra(CONST.NOTIFICATION_TYPE) != null) {
                switch (getIntent().getStringExtra(CONST.NOTIFICATION_TYPE)) {

                    case CLASS_START:

                        setFragment(new ClassStartFragment(), CLASS_START);
                        break;

                    case FACIAL_ATTENDANCE:

                        setFragment(new FacialRecognitionFragment(), FACIAL_ATTENDANCE);
                        break;

                    case MANUAL_ATTENDANCE:

                        setFragment(new ManualAttendanceFragment(), MANUAL_ATTENDANCE);
                        break;

                    case ATTENDANCE_SUCCESS:

                        setFragment(new AttendanceSuccessFragment(), ATTENDANCE_SUCCESS);
                        break;

                    case ATTENDANCE_START:

                        setFragment(new AttendanceStartFragment(), ATTENDANCE_START);

                        break;
                }
            }
        }else{
            switch (getIntent().getStringExtra(CONST.NOTIFICATION_TYPE)){

                case CLASS_START:
                    Paper.book().write(CONST.CLASS_STARTED, true);
                    Paper.book().delete(CONST.ATTENDANCE_STARTED);
                    Paper.book().delete(CONST.MANUAL_ATTENDANCE_STARTED);
                    Paper.book().delete(CONST.FACIAL_ATTENDANCE_STARTED);
                    Paper.book().delete(CONST.ATTENDANCE_CONFIRMED);
                    break;

                case FACIAL_ATTENDANCE:
                    Paper.book().write(CONST.FACIAL_ATTENDANCE_STARTED,true);
                    Paper.book().delete(CONST.CLASS_STARTED);
                    Paper.book().delete(CONST.ATTENDANCE_STARTED);
                    Paper.book().delete(CONST.MANUAL_ATTENDANCE_STARTED);
                    Paper.book().delete(CONST.ATTENDANCE_CONFIRMED);

                    break;

                case MANUAL_ATTENDANCE:
                    Paper.book().write(CONST.MANUAL_ATTENDANCE_STARTED,true);
                    Paper.book().delete(CONST.CLASS_STARTED);
                    Paper.book().delete(CONST.ATTENDANCE_STARTED);
                    Paper.book().delete(CONST.FACIAL_ATTENDANCE_STARTED);
                    Paper.book().delete(CONST.ATTENDANCE_CONFIRMED);

                    break;

                case ATTENDANCE_SUCCESS:
                    Paper.book().write(CONST.ATTENDANCE_CONFIRMED,true);
                    Paper.book().delete(CONST.CLASS_STARTED);
                    Paper.book().delete(CONST.ATTENDANCE_STARTED);
                    Paper.book().delete(CONST.MANUAL_ATTENDANCE_STARTED);
                    Paper.book().delete(CONST.FACIAL_ATTENDANCE_STARTED);

                    break;

                case ATTENDANCE_START:
                    Paper.book().write(CONST.ATTENDANCE_STARTED,true);
                    Paper.book().delete(CONST.CLASS_STARTED);
                    Paper.book().delete(CONST.MANUAL_ATTENDANCE_STARTED);
                    Paper.book().delete(CONST.FACIAL_ATTENDANCE_STARTED);
                    Paper.book().delete(CONST.ATTENDANCE_CONFIRMED);

                    break;
            }

            Intent intent = new Intent(NotificationHandleActivity.this, SplashActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP |
                    PowerManager.ON_AFTER_RELEASE, "wakeLock");
            wakeLock.acquire();
            wakeLock.release();

            MainApplication.isNotificationScreen=true;

            IntentFilter intentFilter = new IntentFilter(
                    "android.intent.action.FINSIHACTIVITY");

            mReceiver = new ClassFinishBroadcastReceiver();
            registerReceiver(mReceiver, intentFilter);

            if(!MainApplication.isNavigineInitialized) {
                new InitTask(this).execute();
            }

        } catch (Throwable th) {
            // ignoring this exception, probably wakeLock was already released
        }

    }

    public class ClassFinishBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                switch (intent.getStringExtra(CONST.NOTIFICATION_TYPE)) {

                    case ATTENDANCE_START:
                        Paper.book().delete(CONST.CLASS_STARTED);
                        Paper.book().write(CONST.ATTENDANCE_STARTED, true);
                        Paper.book().delete(CONST.MANUAL_ATTENDANCE_STARTED);
                        Paper.book().delete(CONST.FACIAL_ATTENDANCE_STARTED);

                        setFragment(new AttendanceStartFragment(), NotificationHandleActivity.ATTENDANCE_START);
                        break;

                    case ATTENDANCE_SUCCESS:
                        Paper.book().write(CONST.ATTENDANCE_CONFIRMED, true);
                        Paper.book().delete(CONST.CLASS_STARTED);
                        Paper.book().delete(CONST.ATTENDANCE_STARTED);
                        Paper.book().delete(CONST.MANUAL_ATTENDANCE_STARTED);
                        Paper.book().delete(CONST.FACIAL_ATTENDANCE_STARTED);

                        setFragment(new AttendanceSuccessFragment(), NotificationHandleActivity.ATTENDANCE_SUCCESS);
                        break;

                    case ATTENDANCE_STOP:
                        Paper.book().delete(CONST.MANUAL_ATTENDANCE_STARTED);
                        Paper.book().delete(CONST.CLASS_STARTED);
                        Paper.book().delete(CONST.ATTENDANCE_STARTED);
                        Paper.book().delete(CONST.FACIAL_ATTENDANCE_STARTED);
                        Paper.book().delete(CONST.ATTENDANCE_CONFIRMED);

                        Intent nextPage = new Intent(NotificationHandleActivity.this, DashboardActivity.class);
                        nextPage.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(nextPage);
                        finish();
                        break;
                }
            }catch (Exception e){

            }
        }
    }

    public void setFragment(Fragment fragment, String fragmentName) {

        try{
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP |
                    PowerManager.ON_AFTER_RELEASE, "wakeLock");
            wakeLock.acquire();
            wakeLock.release();
        }catch (Exception e){

        }

        switch (fragmentName){

            case CLASS_START:
                Paper.book().write(CONST.CLASS_STARTED, true);
                Paper.book().delete(CONST.ATTENDANCE_STARTED);
                Paper.book().delete(CONST.MANUAL_ATTENDANCE_STARTED);
                Paper.book().delete(CONST.FACIAL_ATTENDANCE_STARTED);
                break;

            case FACIAL_ATTENDANCE:
                Paper.book().write(CONST.FACIAL_ATTENDANCE_STARTED,true);
                Paper.book().delete(CONST.CLASS_STARTED);
                Paper.book().delete(CONST.ATTENDANCE_STARTED);
                Paper.book().delete(CONST.MANUAL_ATTENDANCE_STARTED);
                Paper.book().delete(CONST.ATTENDANCE_CONFIRMED);

                break;

            case MANUAL_ATTENDANCE:
                Paper.book().write(CONST.MANUAL_ATTENDANCE_STARTED,true);
                Paper.book().delete(CONST.CLASS_STARTED);
                Paper.book().delete(CONST.ATTENDANCE_STARTED);
                Paper.book().delete(CONST.FACIAL_ATTENDANCE_STARTED);
                Paper.book().delete(CONST.ATTENDANCE_CONFIRMED);

                break;

            case ATTENDANCE_SUCCESS:
                Paper.book().write(CONST.ATTENDANCE_CONFIRMED,true);
                Paper.book().delete(CONST.CLASS_STARTED);
                Paper.book().delete(CONST.ATTENDANCE_STARTED);
                Paper.book().delete(CONST.MANUAL_ATTENDANCE_STARTED);
                Paper.book().delete(CONST.FACIAL_ATTENDANCE_STARTED);

                break;

            case ATTENDANCE_START:
                Paper.book().write(CONST.ATTENDANCE_STARTED,true);
                Paper.book().delete(CONST.CLASS_STARTED);
                Paper.book().delete(CONST.MANUAL_ATTENDANCE_STARTED);
                Paper.book().delete(CONST.FACIAL_ATTENDANCE_STARTED);
                Paper.book().delete(CONST.ATTENDANCE_CONFIRMED);

                break;
        }

        android.support.v4.app.FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        /*Fragment oldfragment = getSupportFragmentManager().findFragmentByTag(fragmentName);
        if(oldfragment==null) {
            t.replace(R.id.fragment_container, fragment, fragmentName);
            t.addToBackStack(null);
        }else{*/
            t.replace(R.id.fragment_container, fragment, fragmentName);
        //}
        t.commit();
    }

    @Override
    public void onBackPressed() {

        Intent nextPage = new Intent(NotificationHandleActivity.this, DashboardActivity.class);
        nextPage.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(nextPage);
        finish();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    protected void onPause() {
        super.onPause();

        if(mReceiver!=null){
            unregisterReceiver(mReceiver);
        }

        MainApplication.isNotificationScreen=false;

    }

    class InitTask extends AsyncTask<Void, Void, Boolean> {
        private Context mContext = null;
        private String mErrorMsg = null;

        public InitTask(Context context) {
            mContext = context.getApplicationContext();
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            if (!MainApplication.initialize(getApplicationContext())) {
                mErrorMsg = "Error downloading location information! Please, try again later or contact technical support";
                return Boolean.FALSE;
            }
            Log.d("AAAAAA", "Initialized!");
            if (!NavigineSDK.loadLocation(MainApplication.LOCATION_ID, 30)) {
                mErrorMsg = "Error downloading location information! Please, try again later or contact technical support";
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }

        @Override
        protected void onPostExecute(Boolean result) {

            if (result.booleanValue()) {
                // Starting main activity

            } else {
                Toast.makeText(mContext, mErrorMsg, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }
    }


}
