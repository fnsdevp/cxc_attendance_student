package com.fnspl.hiplaedu_student.fragment;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fnspl.hiplaedu_student.R;
import com.fnspl.hiplaedu_student.activity.DashboardActivity;
import com.fnspl.hiplaedu_student.database.Db_helper;
import com.fnspl.hiplaedu_student.databinding.FragmentAttendanceSuccessBinding;
import com.fnspl.hiplaedu_student.model.RoutinePeriod;
import com.fnspl.hiplaedu_student.service.MyNavigationService;
import com.fnspl.hiplaedu_student.utils.CONST;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.paperdb.Paper;

/**
 * A simple {@link Fragment} subclass.
 */
public class AttendanceSuccessFragment extends Fragment {

    private FragmentAttendanceSuccessBinding binding_attendance_success;
    private View mView;
    private MyCountDownTimer myCountDownTimer;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
    private boolean isTimeFinished = false;

    public AttendanceSuccessFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Paper.book().write(CONST.LOGIN_FOR_FIRST_TIME, false);

        binding_attendance_success = DataBindingUtil.inflate(inflater, R.layout.fragment_attendance_success, container, false);
        mView = binding_attendance_success.getRoot();
        init(mView);
        return mView;
    }

    private void init(View mView) {
        String udata = "Class Name";
        SpannableString content = new SpannableString(udata);
        content.setSpan(new UnderlineSpan(), 0, udata.length(), 0);
        binding_attendance_success.tvClassName.setText(content);

        if (Paper.book().read(CONST.TIME_LEFT) == null)
            Paper.book().write(CONST.TIME_LEFT, System.currentTimeMillis());

        setClassStartService(getActivity());

        if (getActivity() != null) {
            getActivity().startService(new Intent(getActivity(), MyNavigationService.class));
        }
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
                Paper.book().delete(CONST.ATTENDANCE_CONFIRMED);
                Paper.book().delete(CONST.TIME_LEFT);

                if (myCountDownTimer != null) {
                    myCountDownTimer.cancel();
                }

                if (getActivity() != null) {
                    getActivity().stopService(new Intent(getActivity(), MyNavigationService.class));

                    getActivity().startActivity(new Intent(getActivity(), DashboardActivity.class));
                    getActivity().finish();
                }
            }

            int min = timeInSeconds / 60;
            int sec = timeInSeconds % 60;

            if (min > 9 && sec > 9) {
                binding_attendance_success.tvCountdownTimer.setText(String.format("%s : %s", min, sec));
            } else if (min > 9 && sec < 10) {
                binding_attendance_success.tvCountdownTimer.setText(String.format("%s : 0%s", min, sec));
            } else if (min < 10 && sec < 10) {
                binding_attendance_success.tvCountdownTimer.setText(String.format("0%s : 0%s", min, sec));
            } else if (min < 10 && sec > 9) {
                binding_attendance_success.tvCountdownTimer.setText(String.format("0%s : %s", min, sec));
            } else {
                binding_attendance_success.tvCountdownTimer.setText(String.format("00 : 00", min, sec));
            }
        }

        @Override
        public void onFinish() {
            Paper.book().delete(CONST.ATTENDANCE_CONFIRMED);
            Paper.book().delete(CONST.TIME_LEFT);

            if (getActivity() != null) {
                startActivity(new Intent(getActivity(), DashboardActivity.class));
                getActivity().finish();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        try {

            if (!Paper.book().read(CONST.ATTENDANCE_CONFIRMED, false)) {
                Paper.book().delete(CONST.ATTENDANCE_CONFIRMED);
                if (getActivity() != null) {
                    startActivity(new Intent(getActivity(), DashboardActivity.class));
                    getActivity().finish();
                }
            } else {
                if (Paper.book().read(CONST.TIME_LEFT) != null) {
                    Db_helper db_helper = new Db_helper(getActivity());
                    RoutinePeriod routinePeriod = db_helper.getRoutine(Paper.book().read(CONST.CURRENT_PERIOD, 0));
                    if (routinePeriod != null) {
                        Date currentDateTime = new Date();
                        Date classStartDateTime = dateFormat.parse(new SimpleDateFormat("yyyy-MM-dd").format(currentDateTime) + " " + routinePeriod.getStartTime());
                        Date classEndDateTime = dateFormat.parse(new SimpleDateFormat("yyyy-MM-dd").format(currentDateTime) + " " + routinePeriod.getEndTime());

                        long timePassed = System.currentTimeMillis() - classStartDateTime.getTime();

                        long diff = classEndDateTime.getTime() - classStartDateTime.getTime() - timePassed;

                        binding_attendance_success.tvClassName.setText("" + routinePeriod.getSubject_name());

                        Long milliSecLeft = (Long) Paper.book().read(CONST.TIME_LEFT, System.currentTimeMillis());

                        Long timeLeft = System.currentTimeMillis() - milliSecLeft;

                        if (diff > 0) {
                            if (myCountDownTimer == null) {
                                myCountDownTimer = new MyCountDownTimer(diff, 1000);
                                myCountDownTimer.start();
                            }
                        } else {
                            isTimeFinished = true;
                            Paper.book().delete(CONST.ATTENDANCE_CONFIRMED);
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
                    db_helper.getReadableDatabase().close();
                } else {
                    Paper.book().delete(CONST.ATTENDANCE_CONFIRMED);
                    if (getActivity() != null) {
                        startActivity(new Intent(getActivity(), DashboardActivity.class));
                        getActivity().finish();
                    }
                }
            }
        } catch (Exception e) {

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (myCountDownTimer != null)
            myCountDownTimer.cancel();
    }

    public void setClassStartService(Context mContext) {
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), CONST.CLASS_START_ID,
                new Intent().setAction("START_WIFI_ZONE_SERVICE"), 0);

        // reset previous pending intent
        alarmManager.cancel(pendingIntent);

        Paper.book().delete(CONST.NEXT_PERIOD);
    }

}
