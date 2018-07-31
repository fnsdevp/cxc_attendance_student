package com.fnspl.hiplaedu_student.activity;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.fnspl.hiplaedu_student.R;
import com.fnspl.hiplaedu_student.application.MainApplication;
import com.fnspl.hiplaedu_student.beaconeManager.MyService;
import com.fnspl.hiplaedu_student.database.Db_helper;
import com.fnspl.hiplaedu_student.beaconeManager.Constants;
import com.fnspl.hiplaedu_student.model.ZoneInfo;
import com.fnspl.hiplaedu_student.service.MyNavigationService;
import com.fnspl.hiplaedu_student.utils.CONST;
import com.fnspl.hiplaedu_student.utils.MarshmallowPermissionHelper;
import com.navigine.naviginesdk.NavigineSDK;

import io.paperdb.Paper;

public class SplashActivity extends BaseActivity {

    private static final int REQUEST_ALL_PERMISSION = 100;
    private String macAddress = "";
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Constants.activity = SplashActivity.this;

        setContentView(R.layout.activity_splash);

        setUpZoneData();

        /*macAddress = "d0:37:42:d4:b6:f1";
        Toast.makeText(this, "MacAddress: " + macAddress, Toast.LENGTH_SHORT).show();*/
        Log.d("Tester","Macaddress: "+getMacAddr());

        //connectToWiFi("FNSPL3F", "Plz@ccess", this);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                nextPage();
            }
        },3000);

    }

    private void nextPage() {
        if(Paper.book().read(CONST.PROFILE_INFO)==null) {
            startActivity(new Intent(this, LoginActivity.class));
            overridePendingTransition(R.anim.slideinfromright, R.anim.slideouttoleft);
            supportFinishAfterTransition();
        }else{
            if(haveNetworkConnection()){
                checkNavinginePermissions();
            }else{
                showNetConnectionDialog();
            }
        }
    }

    private void doCalculation() {
        if(Paper.book().read(CONST.CLASS_STARTED,false)){
            startActivity(new Intent(this, NotificationHandleActivity.class).putExtra(CONST.NOTIFICATION_TYPE, NotificationHandleActivity.CLASS_START));
            overridePendingTransition(R.anim.slideinfromright, R.anim.slideouttoleft);
            supportFinishAfterTransition();

        }else if(Paper.book().read(CONST.ATTENDANCE_STARTED,false)){
            startActivity(new Intent(this, NotificationHandleActivity.class).putExtra(CONST.NOTIFICATION_TYPE, NotificationHandleActivity.ATTENDANCE_START));
            overridePendingTransition(R.anim.slideinfromright, R.anim.slideouttoleft);
            supportFinishAfterTransition();

        }else if(Paper.book().read(CONST.FACIAL_ATTENDANCE_STARTED,false)){
            startActivity(new Intent(this, NotificationHandleActivity.class).putExtra(CONST.NOTIFICATION_TYPE, NotificationHandleActivity.FACIAL_ATTENDANCE));
            overridePendingTransition(R.anim.slideinfromright, R.anim.slideouttoleft);
            supportFinishAfterTransition();

        }else if(Paper.book().read(CONST.MANUAL_ATTENDANCE_STARTED,false)){
            startActivity(new Intent(this, NotificationHandleActivity.class).putExtra(CONST.NOTIFICATION_TYPE, NotificationHandleActivity.MANUAL_ATTENDANCE));
            overridePendingTransition(R.anim.slideinfromright, R.anim.slideouttoleft);
            supportFinishAfterTransition();

        }else if(Paper.book().read(CONST.ATTENDANCE_CONFIRMED,false)){
            startActivity(new Intent(this, NotificationHandleActivity.class).putExtra(CONST.NOTIFICATION_TYPE, NotificationHandleActivity.ATTENDANCE_SUCCESS));
            overridePendingTransition(R.anim.slideinfromright, R.anim.slideouttoleft);
            supportFinishAfterTransition();

        }else {
            startActivity(new Intent(this, DashboardActivity.class));
            overridePendingTransition(R.anim.slideinfromright, R.anim.slideouttoleft);
            supportFinishAfterTransition();

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

    private void setUpZoneData(){
        Db_helper db_helper = new Db_helper(getApplicationContext());

        //For CXC Zone
 /*       db_helper.insert_zone(new ZoneInfo(1, "580,1317", "11,13", "20,13", "20,5", "11,5"));
        db_helper.insert_zone(new ZoneInfo(2,"165,881","4,25","9,25","8,19","5,19"));
        db_helper.insert_zone(new ZoneInfo(3,"739,259","19,46","25,46","25,39","19,39"));
        db_helper.insert_zone(new ZoneInfo(4,"739,259","9.5,24","12,24","12,20.5","9.5,20.5"));
        db_helper.insert_zone(new ZoneInfo(5,"739,259","26,35","29,35","29,32","26,32"));
*/

        db_helper.insert_zone(new ZoneInfo(1, "657,1087", "508,180", "520,180", "520,114", "508,114"));
        db_helper.insert_zone(new ZoneInfo(2,"700,920","467,199","473,199","473,196","467,196"));
        db_helper.insert_zone(new ZoneInfo(3,"597,1011","528,250","532,250","532,238","528,238"));

    }

    private void showNetConnectionDialog() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(SplashActivity.this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(SplashActivity.this);
        }
        builder.setTitle("No Internet Connection")
                .setMessage("Please enable your network connection and then proceed.")
                .setCancelable(false)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                        if(haveNetworkConnection()){
                            checkNavinginePermissions();
                        }else{
                            showNetConnectionDialog();
                        }
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void checkNavinginePermissions() {
        if (Build.VERSION.SDK_INT > 22) {
            if (MarshmallowPermissionHelper.getAllNaviginePermission(null
                    , this, REQUEST_ALL_PERMISSION)) {
                if(!MainApplication.isNavigineInitialized) {
                    (new InitTask(this)).execute();
                }else{
                    if(Paper.book().read(CONST.PROFILE_INFO,null)!=null){
                        doCalculation();
                    }
                }
            }
        } else {
            if(!MainApplication.isNavigineInitialized) {
                (new InitTask(this)).execute();
            }else{
                if(Paper.book().read(CONST.PROFILE_INFO,null) != null){
                    doCalculation();
                }
            }
        }
    }

    // Callback with the request from calling requestPermissions(...)
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        // Make sure it's our original READ_CONTACTS request
        switch (requestCode) {
            case REQUEST_ALL_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[3] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[4] == PackageManager.PERMISSION_GRANTED) {
                    if(!MainApplication.isNavigineInitialized) {
                        (new InitTask(this)).execute();
                    }
                }
                return;
            }

            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
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

                doCalculation();
            } else {
                MainApplication.isNavigineInitialized = false;
                //Toast.makeText(mContext, mErrorMsg, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }
    }

}
