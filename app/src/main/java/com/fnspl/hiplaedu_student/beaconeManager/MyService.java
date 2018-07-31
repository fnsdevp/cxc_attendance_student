package com.fnspl.hiplaedu_student.beaconeManager;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.anthonycr.grant.PermissionsManager;
import com.anthonycr.grant.PermissionsResultAction;
import com.fnspl.hiplaedu_student.Networking.NetworkUtility;
import com.fnspl.hiplaedu_student.R;
import com.fnspl.hiplaedu_student.activity.LoginActivity;
import com.fnspl.hiplaedu_student.activity.NotificationHandleActivity;
import com.fnspl.hiplaedu_student.database.Db_helper;
import com.fnspl.hiplaedu_student.model.ProfileInfo;
import com.fnspl.hiplaedu_student.model.RoutinePeriod;
import com.fnspl.hiplaedu_student.utils.CONST;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import io.paperdb.Paper;
import uk.co.alt236.bluetoothlelib.device.BluetoothLeDevice;
import uk.co.alt236.bluetoothlelib.device.beacon.BeaconType;
import uk.co.alt236.bluetoothlelib.device.beacon.BeaconUtils;
import uk.co.alt236.bluetoothlelib.device.beacon.ibeacon.IBeaconDevice;


/**
 * Created by FNSPL on 9/21/2017.
 */

public class MyService extends Service {

    private BluetoothLeDeviceStore mDeviceStore;
    private IBeaconDevice iBeaconDevice;
    private boolean pass1 = false, pass2 = false, pass3 = false;
    private String msg = "";
    private BluetoothLeScanner mScanner;
    private BluetoothUtils mBluetoothUtils;
    private boolean flag = true;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
    private TimerTask mTimerTask = null;
    private Timer mTimer = new Timer();
    private TimerTask mTimerTask1 = null;
    private Timer mTimer1 = new Timer();
    private Handler mHandler = new Handler();
    private Handler mHandler1 = new Handler();
    private static int UPDATE_TIMEOUT = 12 * 1000;
    private boolean isNotified = true;
    private boolean isMarkedAbsent = false;
    private String accuracy = "";
    private String macAddress="";

    private Runnable mRunnable =
            new Runnable() {
                public void run() {
                    if(isOnline()) {
                        new ZoneDetection().execute();
                    }else{
                        setBluetoothEnable(true, MyService.this);
                        startScan();
                    }
                }
            };

    private Runnable mRunnable1 =
            new Runnable() {
                public void run() {

                    if (flag) {
                        isNotified = true;
                    } else {
                        isNotified = false;

                        if (Paper.book().read(CONST.ATTENDANCE_CONFIRMED, false) && !isNotified && !isMarkedAbsent) {
                            isMarkedAbsent=true;
                            goingOutOfClass();
                        }
                    }

                }
            };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (getApplicationContext() != null) {
            mDeviceStore = new BluetoothLeDeviceStore();
            mBluetoothUtils = new BluetoothUtils(getApplicationContext());
            mScanner = new BluetoothLeScanner(mLeScanCallback, mBluetoothUtils);
            isMarkedAbsent = false;

            if(mTimerTask==null) {
                mTimerTask = new TimerTask() {
                    @Override
                    public void run() {
                        //mHandler.post(mRunnable);
                    }
                };
                mTimer.schedule(mTimerTask, UPDATE_TIMEOUT, 10);
            }

            mHandler.post(mRunnable);
        } else {
            stopSelf();
        }

    }

    private final BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {

            final BluetoothLeDevice deviceLe = new BluetoothLeDevice(device, rssi, scanRecord, System.currentTimeMillis());
            if (BeaconUtils.getBeaconType(deviceLe) == BeaconType.IBEACON) {
                iBeaconDevice = new IBeaconDevice(deviceLe);
                int minor = iBeaconDevice.getMinor();
                if (minor == 33826) {  //52227 yellow

                    accuracy = "" + iBeaconDevice.getAccuracy();

                    if (iBeaconDevice.getAccuracy() < 2.7) {//3.55

                        msg = "entered";
                        flag = true;
                        Log.d("joseph", "entered: ");

                    } else {

                        msg = "exited";
                        flag = false;
                        Log.d("joseph", "exite  d: ");

                        if (Paper.book().read(CONST.ATTENDANCE_CONFIRMED, false)) {
                            mHandler1.postDelayed(mRunnable1, UPDATE_TIMEOUT + 4000);
                        }

                    }

                    Intent zoneInfo = new Intent("android.intent.action.SUCCESSLOCATION");
                    zoneInfo.putExtra(CONST.IS_IN_CLASS, flag);
                    zoneInfo.putExtra(CONST.ACCURACY, accuracy);
                    sendBroadcast(zoneInfo);

                    //sendNotification("You are in class : "+flag);
                }

            }


        }
    };


    private void goingOutOfClass() {
        String time = new SimpleDateFormat("hh:mm a").format(new Date());

        ProfileInfo profileInfo = Paper.book().read(CONST.PROFILE_INFO);
        int routine_id = Paper.book().read(CONST.CURRENT_PERIOD, 0);

        Db_helper db_helper = new Db_helper(getApplicationContext());
        RoutinePeriod routinePeriod = db_helper.getRoutine(routine_id);

        String urlParameters = "student_id=" + profileInfo.getId() +
                "&teacher_id=" + routinePeriod.getTeacher_details().getId() +
                "&out_time=" + time +
                "&remark=Marked Absent" +
                "&routine_history_id=" + routine_id;

        new SneakRegister().execute(urlParameters);
    }


    private class SneakRegister extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String result = "";
            HttpURLConnection urlConnection = null;
            try {
                Log.d("Tester", "Before request");
                URL url = new URL(NetworkUtility.BASEURL + NetworkUtility.SNEAK_OUT);
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

            try {
                JSONObject response = new JSONObject(s);

                if (response.getString("status").equalsIgnoreCase("success")) {

                }

                if (MyService.this != null)
                    Toast.makeText(MyService.this, "Please return to the class", Toast.LENGTH_SHORT).show();

                isMarkedAbsent=true;

            } catch (JSONException e) {
                e.printStackTrace();
                //Toast.makeText(getApplicationContext(), "JSON Exception", Toast.LENGTH_SHORT).show();
                isMarkedAbsent=false;
            }
        }
    }


    private void startScanPrepare() {
        //
        // The COARSE_LOCATION permission is only needed after API 23 to do a BTLE scan
        //
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PermissionsManager.getInstance().requestPermissionsIfNecessaryForResult(Constants.activity,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, new PermissionsResultAction() {

                        @Override
                        public void onGranted() {
                            startScan();
                        }

                        @Override
                        public void onDenied(String permission) {
                            Toast.makeText(Constants.activity,
                                    R.string.permission_not_granted_coarse_location,
                                    Toast.LENGTH_SHORT)
                                    .show();
                        }
                    });
        } else {
            startScan();
        }
    }

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
        Intent resultIntent = new Intent(this, NotificationHandleActivity.class);
        resultIntent.putExtra(CONST.NOTIFICATION_TYPE, NotificationHandleActivity.MANUAL_ATTENDANCE);

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

    private void startScan() {
        final boolean isBluetoothOn = mBluetoothUtils.isBluetoothOn();
        final boolean isBluetoothLePresent = mBluetoothUtils.isBluetoothLeSupported();
        mDeviceStore.clear();

        mBluetoothUtils.askUserToEnableBluetoothIfNeeded();
        if (isBluetoothOn && isBluetoothLePresent) {
            mScanner.scanLeDevice(UPDATE_TIMEOUT, true);
        }
        //sendNotification("Scaning started");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        isMarkedAbsent = true;

        if (mScanner != null) {
            mScanner.scanLeDevice(-1, false);
        }

        mTimer.cancel();
        if(mTimerTask!=null)
        mTimerTask.cancel();

    }

    private int getNotificationIcon() {
        boolean useWhiteIcon = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.mipmap.ic_launcher : R.mipmap.ic_launcher;
    }

    private boolean setBluetoothEnable(boolean enable, Context mContext) {
        BluetoothAdapter bluetoothAdapter = mBluetoothUtils.getBluetoothAdapter();
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
        enableLocation(mContext);
        return true;
    }

    private void enableLocation(Context mContext) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
            boolean isGpsProviderEnabled, isNetworkProviderEnabled;
            isGpsProviderEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkProviderEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGpsProviderEnabled && !isNetworkProviderEnabled) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }

        }
    }

    protected boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }
    }

    private class ZoneDetection extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String result = "";
            HttpsURLConnection urlConnection=null;
            try {
                TrustManager[] trustAllCerts = new TrustManager[] {
                        new X509TrustManager() {
                            public X509Certificate[] getAcceptedIssuers() {
                                return null;
                            }
                            public void checkClientTrusted(X509Certificate[] certs, String authType) {}

                            public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                        }
                };

                SSLContext sc = null;

                try {
                    sc = SSLContext.getInstance("SSL");
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }

                try {
                    sc.init(null, trustAllCerts, new java.security.SecureRandom());
                } catch (KeyManagementException e) {
                    e.printStackTrace();
                }

                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

                // Create all-trusting host name verifier
                HostnameVerifier allHostsValid = new HostnameVerifier() {
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                };
                // Install the all-trusting host verifier
                HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

                String authString ="admin" + ":" + "C1sco12345";
                byte[] authEncBytes = Base64.encode(authString.getBytes(), Base64.DEFAULT);
                String authStringEnc = new String(authEncBytes);

                Log.d("Tester","Before request");
                String restURL = "https://bgl-cmx.ebc.cisco.com/api/location/v1/clients/count/byzone/detail?zoneId=102";
                URL url = new URL(restURL);
                Log.d("Tester","Make URL");
                urlConnection = (HttpsURLConnection) url.openConnection();
                Log.d("Tester","Open Connection");
                urlConnection.setRequestProperty("Authorization", "Basic " + authStringEnc);
                urlConnection.setConnectTimeout(10000);
                Log.d("Tester","Set Property");
                int responseCode = urlConnection.getResponseCode();
                Log.d("Tester","Responce Code: "+responseCode);

                if(responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream is = urlConnection.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is);

                    int numCharsRead;
                    char[] charArray = new char[1024];
                    StringBuffer sb = new StringBuffer();
                    while ((numCharsRead = isr.read(charArray)) > 0) {
                        sb.append(charArray, 0, numCharsRead);
                    }
                    result = sb.toString();
                }

            }catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                return result;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            macAddress = getMacAddr();
            //macAddress = "d0:37:42:d4:b6:f1";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try {
                JSONObject cmxResponse = new JSONObject(s);
                flag = false;

                if(cmxResponse.getJSONArray("MacAddress")!=null){

                    if (mScanner != null) {
                        mScanner.scanLeDevice(-1, false);
                    }

                    JSONArray macAddressArray = cmxResponse.getJSONArray("MacAddress");

                    for (int i = 0; i < macAddressArray.length(); i++) {
                        if(macAddress.equalsIgnoreCase(macAddressArray.get(i).toString())){
                            isMarkedAbsent = false;
                            flag = true;
                            break;
                        }
                    }

                    if (!flag) {
                        if (Paper.book().read(CONST.ATTENDANCE_CONFIRMED, false)) {
                            mHandler1.postDelayed(mRunnable1, UPDATE_TIMEOUT + 4000);
                        }

                        //Toast.makeText(MyService.this, "Not in zone", Toast.LENGTH_SHORT).show();
                    }else{
                        //Toast.makeText(MyService.this, "In zone", Toast.LENGTH_SHORT).show();
                    }

                }else{
                    setBluetoothEnable(true, MyService.this);
                    startScan();
                }

                Intent zoneInfo = new Intent("android.intent.action.SUCCESSLOCATION");
                zoneInfo.putExtra(CONST.IS_IN_CLASS, flag);
                zoneInfo.putExtra(CONST.ACCURACY, accuracy);
                sendBroadcast(zoneInfo);

                mHandler.post(mRunnable);

            } catch (JSONException e) {
                e.printStackTrace();

                Intent zoneInfo = new Intent("android.intent.action.SUCCESSLOCATION");
                zoneInfo.putExtra(CONST.IS_IN_CLASS, false);
                zoneInfo.putExtra(CONST.ACCURACY, accuracy);
                sendBroadcast(zoneInfo);

                setBluetoothEnable(true, MyService.this);
                startScan();

                mHandler.post(mRunnable);
            }
        }
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
