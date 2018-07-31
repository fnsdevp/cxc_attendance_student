package com.fnspl.hiplaedu_student.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.fnspl.hiplaedu_student.database.Db_helper;
import com.fnspl.hiplaedu_student.model.RoutinePeriod;
import com.fnspl.hiplaedu_student.service.RoutineFetchService;
import com.fnspl.hiplaedu_student.utils.CONST;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.paperdb.Paper;

/**
 * Created by FNSPL on 10/6/2017.
 */

public class BootCompleteBroadcastReceiver extends BroadcastReceiver {

    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
    
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Boot Complete", Toast.LENGTH_LONG).show();

        if(Paper.book().read(CONST.LOGIN_FOR_FIRST_TIME)==null){
            //setStartZoneDetectionService(getApplicationContext());
            context.startService(new Intent(context, RoutineFetchService.class));
        }else{
            setUpNextClassTimer(context);
        }
    }

    private void setUpNextClassTimer(Context context) {
        try {
            Date currentDateTime = new Date();

            Db_helper db_helper = new Db_helper(context);

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

                    setClassStartService(context, classDateTime);

                    break;
                }

            }


        }catch (Exception ex){

        }

    }

    public void setClassStartService(Context mContext, Date periodDate) {
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, CONST.CLASS_START_ID,
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

        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),pendingIntent);
    }


}
