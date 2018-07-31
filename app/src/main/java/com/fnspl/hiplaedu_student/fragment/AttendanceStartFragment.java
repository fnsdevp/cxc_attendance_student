package com.fnspl.hiplaedu_student.fragment;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.fnspl.hiplaedu_student.R;
import com.fnspl.hiplaedu_student.activity.DashboardActivity;
import com.fnspl.hiplaedu_student.activity.NotificationHandleActivity;
import com.fnspl.hiplaedu_student.application.MainApplication;
import com.fnspl.hiplaedu_student.database.Db_helper;
import com.fnspl.hiplaedu_student.databinding.FragmentAttendenceStartBinding;
import com.fnspl.hiplaedu_student.model.RoutinePeriod;
import com.fnspl.hiplaedu_student.model.ZoneInfo;
import com.fnspl.hiplaedu_student.utils.CONST;
import com.navigine.naviginesdk.DeviceInfo;
import com.navigine.naviginesdk.NavigationThread;
import com.navigine.naviginesdk.NavigineSDK;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.paperdb.Paper;

public class AttendanceStartFragment extends Fragment {

    private FragmentAttendenceStartBinding binding_attendance_start;
    private View mView;
    private MyCountDownTimer myCountDownTimer;
    private ZoneDetectionBroadcastReceiver mReceiver;
    private static boolean isInClass = false;
    private boolean isTimeFinished = false;
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
    private TimerTask mTimerTask = null;
    private long mErrorMessageTime = 0;
    private static final int ERROR_MESSAGE_TIMEOUT = 5000; // milliseconds
    private ArrayList<ZoneInfo> zoneInfos = new ArrayList<>();
    private ArrayList<PointF[]> zoneInfoPoint = new ArrayList<>();
    private RoutinePeriod routinePeriod;
    private int timerInSec= 180000;

    private Timer mTimer = new Timer();
    private Handler mHandler = new Handler();
    private Runnable mRunnable =
            new Runnable() {
                public void run() {
                    try {
                        if (binding_attendance_start != null && binding_attendance_start.btnOk != null) {
                            if (MainApplication.Navigation != null) {

                                final long timeNow = NavigineSDK.currentTimeMillis();

                                if (mErrorMessageTime > 0 && timeNow > mErrorMessageTime + ERROR_MESSAGE_TIMEOUT) {
                                    mErrorMessageTime = 0;
                                }

                                if (MainApplication.Navigation.getMode() == NavigationThread.MODE_IDLE)
                                    MainApplication.Navigation.setMode(NavigationThread.MODE_NORMAL);

                                DeviceInfo mDeviceInfo = MainApplication.Navigation.getDeviceInfo();

                                if (mDeviceInfo != null && routinePeriod != null && calculateZone(mDeviceInfo)) {
                                    isInClass = true;
                                    binding_attendance_start.btnOk.setBackground(getResources().getDrawable(R.drawable.green_button));
                                } else {
                                    isInClass = false;
                                    binding_attendance_start.btnOk.setBackground(getResources().getDrawable(R.drawable.gray_button));
                                }

                            }
                        }
                    } catch (Exception ex) {

                    }
                }
            };

    public AttendanceStartFragment() {
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
        binding_attendance_start = DataBindingUtil.inflate(inflater, R.layout.fragment_attendence_start, container, false);
        binding_attendance_start.setAttendence(AttendanceStartFragment.this);
        mView = binding_attendance_start.getRoot();
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

        binding_attendance_start.btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (!isTimeFinished) {
                        if (isInClass) {
                            ((NotificationHandleActivity) getActivity()).setFragment(new FacialRecognitionFragment(), NotificationHandleActivity.FACIAL_ATTENDANCE);
                        } else {
                            if (getActivity() != null)
                                Toast.makeText(getActivity(), getResources().getString(R.string.be_in_class), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        if (getActivity() != null)
                            Toast.makeText(getActivity(), getResources().getString(R.string.attendance_is_finished), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        binding_attendance_start.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Paper.book().delete(CONST.CLASS_STARTED);
                Paper.book().delete(CONST.ATTENDANCE_STARTED);
                Paper.book().delete(CONST.MANUAL_ATTENDANCE_STARTED);
                Paper.book().delete(CONST.FACIAL_ATTENDANCE_STARTED);
                Paper.book().delete(CONST.ATTENDANCE_CONFIRMED);
                Paper.book().delete(CONST.TIME_LEFT);

                setUpNextClassTimer();

                Intent intent = new Intent(getActivity(), DashboardActivity.class);
                startActivity(intent);
                ((NotificationHandleActivity) getActivity()).finish();
            }
        });

        if (Paper.book().read(CONST.TIME_LEFT) == null)
            Paper.book().write(CONST.TIME_LEFT, System.currentTimeMillis());

        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                mHandler.post(mRunnable);
            }
        };
        mTimer.schedule(mTimerTask, 2500, 1000);

    }

    public class MyCountDownTimer extends CountDownTimer {

        public MyCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            int timeInSeconds = (int) (millisUntilFinished / 1000);

            if (timeInSeconds < 2) {
                isTimeFinished = true;
                Paper.book().delete(CONST.ATTENDANCE_STARTED);
                Paper.book().delete(CONST.TIME_LEFT);

                if (myCountDownTimer != null) {
                    myCountDownTimer.cancel();
                }

                getActivity().startActivity(new Intent(getActivity(), DashboardActivity.class));
                getActivity().finish();
            }

            int min = timeInSeconds / 60;
            int sec = timeInSeconds % 60;

            if (min > 9 && sec > 9) {
                binding_attendance_start.tvCountdownTimer.setText(String.format("%sm : %ss", min, sec));
            } else if (min > 9 && sec < 10) {
                binding_attendance_start.tvCountdownTimer.setText(String.format("%sm : 0%ss", min, sec));
            } else if (min < 10 && sec < 10) {
                binding_attendance_start.tvCountdownTimer.setText(String.format("0%sm : 0%ss", min, sec));
            } else if (min < 10 && sec > 9) {
                binding_attendance_start.tvCountdownTimer.setText(String.format("0%sm : %ss", min, sec));
            } else {
                binding_attendance_start.tvCountdownTimer.setText(String.format("00m : 00s", min, sec));
            }
        }

        @Override
        public void onFinish() {
            Paper.book().delete(CONST.ATTENDANCE_STARTED);
            if (getActivity() != null) {
                startActivity(new Intent(getActivity(), DashboardActivity.class));
                getActivity().finish();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        Db_helper db_helper = new Db_helper(getActivity());
        routinePeriod = db_helper.getRoutine(Paper.book().read(CONST.CURRENT_PERIOD, 0));

        if (!Paper.book().read(CONST.ATTENDANCE_STARTED, false)) {

            Paper.book().delete(CONST.ATTENDANCE_STARTED);
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

        } else if (routinePeriod != null) {
            List<RoutinePeriod> routinePeriodList = db_helper.getRoutine(new SimpleDateFormat("EEEE").
                    format(new Date()).toLowerCase());

            for (int i = 0; i < routinePeriodList.size(); i++) {
                if (routinePeriodList.get(i).getRoutine_history_id() == routinePeriod.getRoutine_history_id()) {
                    binding_attendance_start.tvPeriod.setText("" + (i + 1));
                }
            }

            binding_attendance_start.tvClassName.setText("" + routinePeriod.getClassname() + "-" + routinePeriod.getSection_name());
            binding_attendance_start.tvTeacherName.setText("" + routinePeriod.getTeacher_details().getName());
            binding_attendance_start.tvSubject.setText("" + routinePeriod.getSubject_name());

            Long milliSecLeft = (Long) Paper.book().read(CONST.TIME_LEFT, System.currentTimeMillis());

            Long timeLeft = System.currentTimeMillis() - milliSecLeft;

            if (timerInSec - timeLeft > 0) {
                if (myCountDownTimer == null) {
                    myCountDownTimer = new MyCountDownTimer(timerInSec - timeLeft, 1000);
                    myCountDownTimer.start();
                }
            } else {
                isTimeFinished = true;
                Paper.book().delete(CONST.ATTENDANCE_STARTED);
                Paper.book().delete(CONST.TIME_LEFT);

                if (myCountDownTimer != null) {
                    myCountDownTimer.cancel();
                }

                if (getActivity() != null) {
                    getActivity().startActivity(new Intent(getActivity(), DashboardActivity.class));
                    getActivity().finish();
                }
            }
        }


        if (Paper.book().read(CONST.CLASS_STARTED, false)) {
            Paper.book().delete(CONST.ATTENDANCE_STARTED);
            Paper.book().delete(CONST.TIME_LEFT);

            ((NotificationHandleActivity) getActivity()).setFragment(new ClassStartFragment(), NotificationHandleActivity.CLASS_START);
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
            mReceiver = null;
        }

        if (myCountDownTimer != null) {
            myCountDownTimer.cancel();
        }

        if (mTimerTask != null && mTimer != null) {
            mTimer.cancel();
            mTimerTask.cancel();
        }
    }

    private void setUpNextClassTimer() {
        try {
            Date currentDateTime = new Date();

            Db_helper db_helper = new Db_helper(getActivity());

            List<RoutinePeriod> routinePeriodList = db_helper.getRoutine(new SimpleDateFormat("EEEE").
                    format(currentDateTime).toLowerCase());

            for (RoutinePeriod routinePeriod :
                    routinePeriodList) {

                Date classDateTime = dateFormat.parse(new SimpleDateFormat("yyyy-MM-dd").format(currentDateTime) + " " + routinePeriod.getStartTime());
                if (currentDateTime.compareTo(classDateTime) > 0) {

                } else {
                    Paper.book().write(CONST.CURRENT_PERIOD, routinePeriod.getRoutine_history_id());
                    Paper.book().write(CONST.ATTENDANCE_STARTED, true);
                    Paper.book().delete(CONST.CLASS_STARTED);
                    Paper.book().delete(CONST.MANUAL_ATTENDANCE_STARTED);
                    Paper.book().delete(CONST.FACIAL_ATTENDANCE_STARTED);
                    Paper.book().delete(CONST.ATTENDANCE_CONFIRMED);

                    setClassStartService(getActivity(), classDateTime);

                    break;
                }

            }

        } catch (Exception ex) {

        }

    }

    public void setClassStartService(Context mContext, Date periodDate) {
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), CONST.CLASS_START_ID,
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

        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() - 120000,
                24 * 60 * 60 * 1000, pendingIntent);
    }

    private boolean calculateZone(DeviceInfo mDeviceInfo) {

        for (int index = 0; index < zoneInfoPoint.size(); index++) {
            boolean inZone = contains(zoneInfoPoint.get(index), new PointF(mDeviceInfo.x, mDeviceInfo.y));
            if (inZone && zoneInfos.get(index).getId() == routinePeriod.getRoom_id()) {
                //currentZone = zoneInfos.get(index).getId();

                return true;
            } else if (inZone && zoneInfos.get(index).getId() != routinePeriod.getRoom_id()) {
                //currentZone = zoneInfos.get(index).getId();

                return false;
            } else {
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
