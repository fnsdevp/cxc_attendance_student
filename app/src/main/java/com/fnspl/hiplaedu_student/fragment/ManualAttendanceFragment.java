package com.fnspl.hiplaedu_student.fragment;


import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.fnspl.hiplaedu_student.Networking.NetworkUtility;
import com.fnspl.hiplaedu_student.R;
import com.fnspl.hiplaedu_student.activity.DashboardActivity;
import com.fnspl.hiplaedu_student.activity.NotificationHandleActivity;
import com.fnspl.hiplaedu_student.application.MainApplication;
import com.fnspl.hiplaedu_student.database.Db_helper;
import com.fnspl.hiplaedu_student.databinding.FragmentManualAttendanceBinding;
import com.fnspl.hiplaedu_student.model.ProfileInfo;
import com.fnspl.hiplaedu_student.model.RoutinePeriod;
import com.fnspl.hiplaedu_student.model.ZoneInfo;
import com.fnspl.hiplaedu_student.utils.CONST;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.navigine.naviginesdk.DeviceInfo;
import com.navigine.naviginesdk.NavigationThread;
import com.navigine.naviginesdk.NavigineSDK;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import io.paperdb.Paper;

/**
 * A simple {@link Fragment} subclass.
 */
public class ManualAttendanceFragment extends Fragment {

    private FragmentManualAttendanceBinding binding_manual_attendance;
    private View mView;
    private ProgressDialog pDialog;
    private ZoneDetectionBroadcastReceiver mReceiver;
    private static boolean isInClass = false;
    private long mErrorMessageTime = 0;
    private static final int ERROR_MESSAGE_TIMEOUT = 5000; // milliseconds
    private ArrayList<ZoneInfo> zoneInfos = new ArrayList<>();
    private ArrayList<PointF[]> zoneInfoPoint = new ArrayList<>();
    private RoutinePeriod routinePeriod;

    public ManualAttendanceFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        IntentFilter intentFilter = new IntentFilter(
                "android.intent.action.SUCCESSLOCATION");

        mReceiver = new ZoneDetectionBroadcastReceiver();
        getActivity().registerReceiver(mReceiver, intentFilter);

        // Inflate the layout for this fragment
        binding_manual_attendance = DataBindingUtil.inflate(inflater, R.layout.fragment_manual_attendance, container, false);
        mView = binding_manual_attendance.getRoot();
        init(mView);
        return mView;
    }

    private void init(View mView) {
        Db_helper db_helper = new Db_helper(getActivity());
        if (db_helper != null) {
            //zoneInfos = db_helper.getAllZoneInfo();
            routinePeriod = db_helper.getRoutine(Paper.book().read(CONST.CURRENT_PERIOD, 0));
            if (routinePeriod != null) {
                ZoneInfo zoneInfo1 = db_helper.getZoneInfo("" + routinePeriod.getRoom_id());
                zoneInfos.add(zoneInfo1);
            }else{
                zoneInfos = db_helper.getAllZoneInfo();
            }

            for (ZoneInfo zoneInfo :
                    zoneInfos) {
                zoneInfoPoint.add(convertToPoints(zoneInfo));
            }
        }

        binding_manual_attendance.btnManualAttendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainApplication.Navigation != null) {

                    final long timeNow = NavigineSDK.currentTimeMillis();

                    if (mErrorMessageTime > 0 && timeNow > mErrorMessageTime + ERROR_MESSAGE_TIMEOUT) {
                        mErrorMessageTime = 0;
                    }

                    if (MainApplication.Navigation.getMode() == NavigationThread.MODE_IDLE)
                        MainApplication.Navigation.setMode(NavigationThread.MODE_NORMAL);

                    DeviceInfo mDeviceInfo = MainApplication.Navigation.getDeviceInfo();

                    if (mDeviceInfo != null && routinePeriod!=null && calculateZone(mDeviceInfo)) {
                        isInClass = true;
                        doMarkPresent();
                    } else {
                        if (getActivity() != null)
                            Toast.makeText(getActivity(), getResources().getString(R.string.be_in_class), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }

    private void doMarkPresent() {

        if (Paper.book().read(CONST.PROFILE_INFO) != null) {
            ProfileInfo profileInfo = Paper.book().read(CONST.PROFILE_INFO);
            int currentPeriod = Paper.book().read(CONST.CURRENT_PERIOD, 0);

            Db_helper db_helper = new Db_helper(getActivity());
            RoutinePeriod routinePeriod = db_helper.getRoutine(currentPeriod);

            if (routinePeriod != null) {
                String urlParameters = "routine_history_id=" + currentPeriod +
                        "&teacher_id=" + routinePeriod.getTeacher_details().getId() +
                        "&student_id=" + profileInfo.getId() +
                        "&in_time=" + new SimpleDateFormat("hh:mm a").format(new Date()) +
                        "&present=N" +
                        "&attendance_type=manual";

                new APIRequest().execute(urlParameters);
            }
        }
    }

    private class APIRequest extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String result = "";
            HttpURLConnection urlConnection = null;
            try {
                Log.d("Tester", "Before request");
                URL url = new URL(NetworkUtility.BASEURL + NetworkUtility.ATTENDENCE_REQUEST);
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

            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage(getString(R.string.dialog_msg));
            pDialog.setCancelable(false);
            pDialog.show();
            //macAddress = "d0:37:42:d4:b6:f1";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }

            try {
                JSONObject cmxResponse = new JSONObject(s);

                if (cmxResponse.getString("status").equalsIgnoreCase("success")) {

                    if (getActivity() != null) {
                        //Toast.makeText(getActivity(), cmxResponse.optString("message"), Toast.LENGTH_SHORT).show();
                    }

                    //((NotificationHandleActivity)getActivity()).setFragment(new AttendanceSuccessFragment(), NotificationHandleActivity.ATTENDANCE_SUCCESS);
                    startActivity(new Intent(getActivity(), DashboardActivity.class));
                    Paper.book().delete(CONST.MANUAL_ATTENDANCE_STARTED);

                } else {
                    if (getActivity() != null)
                        Toast.makeText(getActivity(), cmxResponse.optString("message"), Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                //Toast.makeText(getActivity(), "JSON Exception", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!Paper.book().read(CONST.ATTENDANCE_CONFIRMED, false)) {

            if (!Paper.book().read(CONST.MANUAL_ATTENDANCE_STARTED, false)) {

                Paper.book().delete(CONST.MANUAL_ATTENDANCE_STARTED);
                Paper.book().delete(CONST.TIME_LEFT);

                if (Paper.book().read(CONST.CLASS_STARTED, false)) {
                    Paper.book().delete(CONST.ATTENDANCE_STARTED);
                    Paper.book().delete(CONST.TIME_LEFT);

                    ((NotificationHandleActivity) getActivity()).setFragment(new ClassStartFragment(), NotificationHandleActivity.CLASS_START);
                } else {
                    if (getActivity() != null) {
                        getActivity().startActivity(new Intent(getActivity(), DashboardActivity.class));
                        getActivity().finish();
                    }
                }
            }
        } else {
            ((NotificationHandleActivity) getActivity()).setFragment(new AttendanceSuccessFragment(), NotificationHandleActivity.ATTENDANCE_SUCCESS);
        }
    }

    public class ZoneDetectionBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //isInClass = intent.getBooleanExtra(CONST.IS_IN_CLASS, false);

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //Paper.book().delete(CONST.TIME_LEFT);
        if (mReceiver != null) {
            getActivity().unregisterReceiver(mReceiver);
        }

    }

    private boolean calculateZone(DeviceInfo mDeviceInfo) {

        for (int index = 0; index < zoneInfoPoint.size(); index++) {
            boolean inZone = contains(zoneInfoPoint.get(index), new PointF(mDeviceInfo.x, mDeviceInfo.y));
            if (inZone && zoneInfos.get(index).getId()==routinePeriod.getRoom_id()) {
                //currentZone = zoneInfos.get(index).getId();

                return true;
            }else if(inZone && zoneInfos.get(index).getId()!=routinePeriod.getRoom_id() ){
                //currentZone = zoneInfos.get(index).getId();

                return false;
            }else{
                //currentZone = zoneInfos.get(index).getId();

            }
        }

        return false;
    }

    private PointF[] convertToPoints(ZoneInfo zoneInfo) {
        PointF[] pointFs = new PointF[4];

        String[] pointsA = zoneInfo.getPointA().split(",");
        PointF pointA = new PointF(Float.parseFloat(pointsA[0]), Float.parseFloat(pointsA[1]));
        pointFs[0] = pointA;

        String[] pointsB = zoneInfo.getPointB().split(",");
        PointF pointB = new PointF(Float.parseFloat(pointsB[0]), Float.parseFloat(pointsB[1]));
        pointFs[1] = pointB;

        String[] pointsC = zoneInfo.getPointC().split(",");
        PointF pointC = new PointF(Float.parseFloat(pointsC[0]), Float.parseFloat(pointsC[1]));
        pointFs[2] = pointC;

        String[] pointsD = zoneInfo.getPointD().split(",");
        PointF pointD = new PointF(Float.parseFloat(pointsD[0]), Float.parseFloat(pointsD[1]));
        pointFs[3] = pointD;

        return pointFs;
    }

    public boolean contains(PointF[] points, PointF test) {
        int i;
        int j;
        boolean result = false;
        /*for (i = 0, j = points.length - 1; i < points.length; j = i++) {
            if ((points[i].y > test.y) != (points[j].y > test.y) &&
                    (test.x < (points[j].x - points[i].x) * (test.y - points[i].y) / (points[j].y - points[i].y) + points[i].x)) {
                result = !result;
            }
        }*/
        if (((points[0].x < test.x) && (points[0].y > test.y))) {
            if (((points[3].x < test.x) && (points[3].y < test.y))) {
                if (((points[1].x > test.x) && (points[1].y > test.y))) {
                    if (((points[2].x > test.x) && (points[2].y < test.y))) {
                        result = true;
                    }
                }
            }
        }
        return result;
    }


}
