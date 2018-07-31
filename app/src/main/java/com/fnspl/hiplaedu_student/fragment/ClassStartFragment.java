package com.fnspl.hiplaedu_student.fragment;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.fnspl.hiplaedu_student.R;
import com.fnspl.hiplaedu_student.activity.DashboardActivity;
import com.fnspl.hiplaedu_student.activity.NotificationHandleActivity;
import com.fnspl.hiplaedu_student.application.MainApplication;
import com.fnspl.hiplaedu_student.database.Db_helper;
import com.fnspl.hiplaedu_student.databinding.FragmentClassStartBinding;
import com.fnspl.hiplaedu_student.model.RoutinePeriod;
import com.fnspl.hiplaedu_student.model.ZoneInfo;
import com.fnspl.hiplaedu_student.utils.CONST;
import com.fnspl.hiplaedu_student.widget.Dialogs;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.paperdb.Paper;

/**
 * A simple {@link Fragment} subclass.
 */
public class ClassStartFragment extends Fragment {

    private FragmentClassStartBinding binding_classStart;
    private View mView;
    private MyCountDownTimer myCountDownTimer;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
    private RoutinePeriod routinePeriod;

    public ClassStartFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        binding_classStart = DataBindingUtil.inflate(inflater, R.layout.fragment_class_start, container, false);
        mView = binding_classStart.getRoot();

        init(mView);
        return mView;
    }

    private void init(View mView) {

        if (Paper.book().read(CONST.TIME_LEFT) == null) {
            Paper.book().write(CONST.TIME_LEFT, System.currentTimeMillis());
        }

        binding_classStart.ivNavigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Dialogs.dialogFetchImage(getActivity(), new Dialogs.OnOptionSelect() {
                    @Override
                    public void openNavigine() {

                        openMap();

                    }

                    @Override
                    public void openNormalApp() {
                        if(getActivity()!=null){
                            //getActivity().startActivity(new Intent(getActivity(), NavigationActivity.class));
                        }
                    }
                });

            }
        });

        setUpNextClassTimer();

    }

    private void openMap() {
        Db_helper db_helper = new Db_helper(getActivity());
        if(routinePeriod!=null) {
            ZoneInfo zoneInfo = db_helper.getZoneInfo(String.format("%s", routinePeriod.getRoom_id()));
            if (zoneInfo != null) {
                String[] location = zoneInfo.getCenterPoint().split(",");

                NavigineMapDialogNew mapDialog = new NavigineMapDialogNew();
                Bundle bundle = new Bundle();
                bundle.putString(CONST.POINTX, location[0]);
                bundle.putString(CONST.POINTY, location[1]);
                mapDialog.setArguments(bundle);
                if (mapDialog != null && mapDialog.getDialog() != null
                        && mapDialog.getDialog().isShowing()) {
                    //dialog is showing so do something
                } else {
                    //dialog is not showing
                    mapDialog.show(getChildFragmentManager(), "mapDialog");
                }
            } else {
                Toast.makeText(getActivity(), getResources().getString(R.string.no_info_available), Toast.LENGTH_SHORT).show();
            }
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
                Paper.book().delete(CONST.CLASS_STARTED);
                Paper.book().delete(CONST.TIME_LEFT);

                if (myCountDownTimer != null) {
                    myCountDownTimer.cancel();
                }

                if (getActivity() != null) {
                    getActivity().startActivity(new Intent(getActivity(), DashboardActivity.class));
                    getActivity().finish();
                }

            }

            int min = timeInSeconds / 60;
            int sec = timeInSeconds % 60;

            if (min > 9 && sec > 9) {
                binding_classStart.tvCountdownTimer.setText(String.format("%sm : %ss", min, sec));
            } else if (min > 9 && sec < 10) {
                binding_classStart.tvCountdownTimer.setText(String.format("%sm : 0%ss", min, sec));
            } else if (min < 10 && sec < 10) {
                binding_classStart.tvCountdownTimer.setText(String.format("0%sm : 0%ss", min, sec));
            } else if (min < 10 && sec > 9) {
                binding_classStart.tvCountdownTimer.setText(String.format("0%sm : %ss", min, sec));
            } else {
                binding_classStart.tvCountdownTimer.setText(String.format("00m : 00s", min, sec));
            }

        }

        @Override
        public void onFinish() {
            Paper.book().delete(CONST.CLASS_STARTED);

            if(getActivity()!=null) {
                startActivity(new Intent(getActivity(), DashboardActivity.class));
                getActivity().finish();
            }

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (myCountDownTimer != null)
            myCountDownTimer.cancel();

    }

    @Override
    public void onResume() {
        super.onResume();

        if (Paper.book().read(CONST.ATTENDANCE_STARTED, false) && MainApplication.isNotificationScreen) {
            if(getActivity()!=null){
                ((NotificationHandleActivity) getActivity()).setFragment(new AttendanceStartFragment(), NotificationHandleActivity.ATTENDANCE_START);
            }
        } else {
            Db_helper db_helper = new Db_helper(getActivity());
            routinePeriod = db_helper.getRoutine(Paper.book().read(CONST.CURRENT_PERIOD, 0));

            if (routinePeriod != null) {
                Long milliSecLeft = (Long) Paper.book().read(CONST.TIME_LEFT, System.currentTimeMillis());

                Long timeLeft = System.currentTimeMillis() - milliSecLeft;

                if (120000 - timeLeft > 0) {
                    if(myCountDownTimer==null) {
                        myCountDownTimer = new MyCountDownTimer(120000 - timeLeft, 1000);
                        myCountDownTimer.start();
                    }
                } else {
                    Paper.book().delete(CONST.CLASS_STARTED);
                    Paper.book().delete(CONST.TIME_LEFT);

                    if (myCountDownTimer != null) {
                        myCountDownTimer.cancel();
                    }

                    if(getActivity()!=null) {
                        getActivity().startActivity(new Intent(getActivity(), DashboardActivity.class));
                        getActivity().finish();
                    }

                }

                binding_classStart.tvClassName.setText("" + routinePeriod.getClassname() + "-" + routinePeriod.getSection_name());
                binding_classStart.tvTeacherName.setText("" + routinePeriod.getTeacher_details().getName());
                binding_classStart.tvSubject.setText("" + routinePeriod.getSubject_name());
            }
        }

    }

    private void setUpNextClassTimer() {
        try {
            Date currentDateTime = new Date();

            Db_helper db_helper = new Db_helper(getActivity());

            List<RoutinePeriod> routinePeriodList = db_helper.getRoutine(new SimpleDateFormat("EEEE").
                    format(currentDateTime).toLowerCase());

            for (int i=0; i<routinePeriodList.size(); i++) {

                Date classDateTime = dateFormat.parse(new SimpleDateFormat("yyyy-MM-dd").format(currentDateTime) + " " + routinePeriodList.get(i).getStartTime());
                if (Paper.book().read(CONST.CURRENT_PERIOD, 0) >= routinePeriodList.get(i).getRoutine_history_id()) {
                    if(i==(routinePeriodList.size()-1)){
                        Paper.book().delete(CONST.LOGIN_FOR_FIRST_TIME);
                    }
                } else {
                    Paper.book().write(CONST.NEXT_PERIOD, routinePeriodList.get(i).getRoutine_history_id());
                    Paper.book().delete(CONST.ATTENDANCE_STARTED);
                    //Paper.book().delete(CONST.CLASS_STARTED);
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

        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() - 119500, pendingIntent);
    }

}
