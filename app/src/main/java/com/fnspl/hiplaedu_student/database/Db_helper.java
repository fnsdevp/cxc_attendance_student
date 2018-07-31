package com.fnspl.hiplaedu_student.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.fnspl.hiplaedu_student.model.RoutinePeriod;
import com.fnspl.hiplaedu_student.model.Teacher_details;
import com.fnspl.hiplaedu_student.model.ZoneInfo;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by FNSPL on 8/25/2017.
 */

public class Db_helper extends SQLiteOpenHelper {



    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "hiplastudent.db";
    public static final String TAG = "hiplastudent";
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
//+ Db_contracts.Entries._ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "
    public static final String  SQL_CREATE_TABLE_ROUTINE = "CREATE TABLE "+ Db_contracts.RoutineDB.TABLE_ROUTINE+" ( "+
                                                         Db_contracts.RoutineDB.COLUMN_ROUTINE+" INTEGER PRIMARY KEY , "+
                                                         Db_contracts.RoutineDB.COLUMN_SUBJECT_ID+" INTEGER , "+
                                                         Db_contracts.RoutineDB.COLUMN_DAY+" TEXT , "+
                                                         Db_contracts.RoutineDB.COLUMN_TEACHER_ID+" INTEGER , "+
                                                         Db_contracts.RoutineDB.COLUMN_TIME_STAMP+" INTEGER , "+
                                                         Db_contracts.RoutineDB.COLUMN_CLASS_ID+" INTEGER , "+
                                                         Db_contracts.RoutineDB.COLUMN_SECTION_ID+" INTEGER , "+
                                                         Db_contracts.RoutineDB.COLUMN_YEAR+" TEXT , "+
                                                         Db_contracts.RoutineDB.COLUMN_STREAM_ID+" INTEGER , "+
                                                         Db_contracts.RoutineDB.COLUMN_ROOM_ID+" INTEGER , "+
                                                         Db_contracts.RoutineDB.COLUMN_CLASS_NAME+" TEXT , "+
                                                         Db_contracts.RoutineDB.COLUMN_TEACHER_NAME+" TEXT , "+
                                                         Db_contracts.RoutineDB.COLUMN_SUBJECT_NAME+" TEXT , "+
                                                         Db_contracts.RoutineDB.COLUMN_SECTION_NAME+" TEXT , "+
                                                         Db_contracts.RoutineDB.COLUMN_STREAM_NAME+" TEXT , "+
                                                         Db_contracts.RoutineDB.COLUMN_START_NAME+" TEXT , "+
                                                         Db_contracts.RoutineDB.COLUMN_END_NAME+" TEXT )";

    public static final String SQL_CREATE_TABLE_ZONEINFO = "CREATE TABLE " + Db_contracts.ZoneInfo.TABLE_ZONE + " ( " +
            Db_contracts.ZoneInfo.COLUMN_ZONE_ID + " INTEGER PRIMARY KEY , " +
            Db_contracts.ZoneInfo.COLUMN_CENTER + " TEXT , " +
            Db_contracts.ZoneInfo.COLUMN_POINT_A + " TEXT , " +
            Db_contracts.ZoneInfo.COLUMN_POINT_B + " TEXT , " +
            Db_contracts.ZoneInfo.COLUMN_POINT_C + " TEXT , " +
            Db_contracts.ZoneInfo.COLUMN_POINT_D + " TEXT )";


   public Db_helper(Context context){
       super(context , DATABASE_NAME ,null ,DATABASE_VERSION);


   }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(SQL_CREATE_TABLE_ROUTINE);
        db.execSQL(SQL_CREATE_TABLE_ZONEINFO);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void deleteRoutine(){
        try{

            getWritableDatabase().delete(Db_contracts.RoutineDB.TABLE_ROUTINE, null, null);

        }catch (SQLException e){

        }
    }

    public void insertAllRoutines(List<RoutinePeriod> routinePeriodList){
        for (RoutinePeriod routinePeriod:
             routinePeriodList) {

            insert_routine(routinePeriod);
        }
    }

    public void insert_routine(RoutinePeriod routine){
        try {
            ContentValues values = new ContentValues();
            values.put(Db_contracts.RoutineDB.COLUMN_ROUTINE, routine.getRoutine_history_id());
            values.put(Db_contracts.RoutineDB.COLUMN_SUBJECT_ID, routine.getSubject_id());
            values.put(Db_contracts.RoutineDB.COLUMN_DAY, routine.getDay());
            values.put(Db_contracts.RoutineDB.COLUMN_TEACHER_ID, routine.getTeacher_details().getId());
            values.put(Db_contracts.RoutineDB.COLUMN_CLASS_ID, routine.getClass_id());
            values.put(Db_contracts.RoutineDB.COLUMN_SECTION_ID, routine.getSection_id());
            values.put(Db_contracts.RoutineDB.COLUMN_YEAR, routine.getYear());
            values.put(Db_contracts.RoutineDB.COLUMN_STREAM_ID, "1");
            values.put(Db_contracts.RoutineDB.COLUMN_ROOM_ID, routine.getRoom_id());
            values.put(Db_contracts.RoutineDB.COLUMN_CLASS_NAME, routine.getClassname());
            values.put(Db_contracts.RoutineDB.COLUMN_TEACHER_NAME, routine.getTeacher_details().getName());
            values.put(Db_contracts.RoutineDB.COLUMN_SUBJECT_NAME, routine.getSubject_name());
            values.put(Db_contracts.RoutineDB.COLUMN_SECTION_NAME, routine.getSection_name());
            values.put(Db_contracts.RoutineDB.COLUMN_STREAM_NAME, "Science");
            values.put(Db_contracts.RoutineDB.COLUMN_START_NAME, routine.getStartTime());
            values.put(Db_contracts.RoutineDB.COLUMN_END_NAME, routine.getEndTime());
            values.put(Db_contracts.RoutineDB.COLUMN_TIME_STAMP, dateFormat.parse(new SimpleDateFormat("yyyy-MM-dd").format(new Date())
                    + " " + routine.getStartTime()).getTime());


            if (!getUid(routine.getRoutine_history_id())) {
                getWritableDatabase().insert(Db_contracts.RoutineDB.TABLE_ROUTINE, null, values);
            } else {
                getWritableDatabase().update(Db_contracts.RoutineDB.TABLE_ROUTINE, values,
                        Db_contracts.RoutineDB.COLUMN_ROUTINE + "=" + routine.getRoutine_history_id(), null);
            }
        }catch (Exception ex){

        }
    }

    public boolean getUid(int uid){
        String read_query = " SELECT "+ Db_contracts.RoutineDB.COLUMN_ROUTINE  +" FROM "+ Db_contracts.RoutineDB.TABLE_ROUTINE ;
        Cursor cursor = getReadableDatabase().rawQuery(read_query, null);

        if(cursor == null)
            return false;

        if(cursor.moveToFirst()) {
            Log.d(TAG, "getUid: " + cursor.getInt(cursor.getColumnIndex(Db_contracts.RoutineDB.COLUMN_ROUTINE)));

            if (uid == cursor.getInt(cursor.getColumnIndex(Db_contracts.RoutineDB.COLUMN_ROUTINE))) {
                return true;
            }
        }
        return false;
    }

    public List<RoutinePeriod> getRoutine(String day){
        SQLiteDatabase db = getReadableDatabase();
        List<RoutinePeriod> routineList = new ArrayList<>();

        String[] params = new String[]{""+day};

        Cursor cursor = db.query(Db_contracts.RoutineDB.TABLE_ROUTINE, null,
               Db_contracts.RoutineDB.COLUMN_DAY+" = ?", params,
                null, null, Db_contracts.RoutineDB.COLUMN_TIME_STAMP +" ASC");

        while(cursor!=null && cursor.moveToNext()){
            Log.d("dev", "database read: "+cursor.getInt(cursor.getColumnIndex(Db_contracts.RoutineDB.COLUMN_ROUTINE)));
            RoutinePeriod routine = new RoutinePeriod() ;
            Teacher_details teacher_details = new Teacher_details();
            teacher_details.setId(cursor.getInt(cursor.getColumnIndex(Db_contracts.RoutineDB.COLUMN_TEACHER_ID)));
            teacher_details.setName(cursor.getString(cursor.getColumnIndex(Db_contracts.RoutineDB.COLUMN_TEACHER_NAME)));

            routine.setRoutine_history_id(cursor.getInt(cursor.getColumnIndex(Db_contracts.RoutineDB.COLUMN_ROUTINE)));
            routine.setSubject_id(cursor.getInt(cursor.getColumnIndex(Db_contracts.RoutineDB.COLUMN_SUBJECT_ID)));
            routine.setDay(cursor.getString(cursor.getColumnIndex(Db_contracts.RoutineDB.COLUMN_DAY)));
            routine.setClass_id(cursor.getInt(cursor.getColumnIndex(Db_contracts.RoutineDB.COLUMN_CLASS_ID)));
            routine.setSection_id(cursor.getInt(cursor.getColumnIndex(Db_contracts.RoutineDB.COLUMN_SECTION_ID)));
            routine.setRoom_id(cursor.getInt(cursor.getColumnIndex(Db_contracts.RoutineDB.COLUMN_ROOM_ID)));
            routine.setYear(cursor.getString(cursor.getColumnIndex(Db_contracts.RoutineDB.COLUMN_YEAR)));
            //routine.setStreamId(cursor.getInt(cursor.getColumnIndex(Db_contracts.RoutineDB.COLUMN_STREAM_ID)));
            routine.setClassname(cursor.getString(cursor.getColumnIndex(Db_contracts.RoutineDB.COLUMN_CLASS_NAME)));
            routine.setSubject_name(cursor.getString(cursor.getColumnIndex(Db_contracts.RoutineDB.COLUMN_SUBJECT_NAME)));
            routine.setSection_name(cursor.getString(cursor.getColumnIndex(Db_contracts.RoutineDB.COLUMN_SECTION_NAME)));
            //routine.setStreamName(cursor.getString(cursor.getColumnIndex(Db_contracts.RoutineDB.COLUMN_STREAM_NAME)));
            routine.setStartTime(cursor.getString(cursor.getColumnIndex(Db_contracts.RoutineDB.COLUMN_START_NAME)));
            routine.setEndTime(cursor.getString(cursor.getColumnIndex(Db_contracts.RoutineDB.COLUMN_END_NAME)));
            routine.setTeacher_details(teacher_details);
            routineList.add(routine);
        }

        db.close();

        return routineList;
    }

    public RoutinePeriod getRoutine(int routine_id){
        SQLiteDatabase db = getReadableDatabase();
        RoutinePeriod routine=null;

        String[] params = new String[]{""+routine_id};

        Cursor cursor = db.query(Db_contracts.RoutineDB.TABLE_ROUTINE, null,
               Db_contracts.RoutineDB.COLUMN_ROUTINE+" = ?", params,
                null, null, null);

        if(cursor!=null && cursor.moveToNext()){
            Log.d("dev", "database read: "+cursor.getInt(cursor.getColumnIndex(Db_contracts.RoutineDB.COLUMN_ROUTINE)));
            routine = new RoutinePeriod() ;
            Teacher_details teacher_details = new Teacher_details();
            teacher_details.setId(cursor.getInt(cursor.getColumnIndex(Db_contracts.RoutineDB.COLUMN_TEACHER_ID)));
            teacher_details.setName(cursor.getString(cursor.getColumnIndex(Db_contracts.RoutineDB.COLUMN_TEACHER_NAME)));

            routine.setRoutine_history_id(cursor.getInt(cursor.getColumnIndex(Db_contracts.RoutineDB.COLUMN_ROUTINE)));
            routine.setSubject_id(cursor.getInt(cursor.getColumnIndex(Db_contracts.RoutineDB.COLUMN_SUBJECT_ID)));
            routine.setDay(cursor.getString(cursor.getColumnIndex(Db_contracts.RoutineDB.COLUMN_DAY)));
            routine.setClass_id(cursor.getInt(cursor.getColumnIndex(Db_contracts.RoutineDB.COLUMN_CLASS_ID)));
            routine.setSection_id(cursor.getInt(cursor.getColumnIndex(Db_contracts.RoutineDB.COLUMN_SECTION_ID)));
            routine.setYear(cursor.getString(cursor.getColumnIndex(Db_contracts.RoutineDB.COLUMN_YEAR)));
            //routine.setStreamId(cursor.getInt(cursor.getColumnIndex(Db_contracts.RoutineDB.COLUMN_STREAM_ID)));
            routine.setClassname(cursor.getString(cursor.getColumnIndex(Db_contracts.RoutineDB.COLUMN_CLASS_NAME)));
            routine.setSubject_name(cursor.getString(cursor.getColumnIndex(Db_contracts.RoutineDB.COLUMN_SUBJECT_NAME)));
            routine.setSection_name(cursor.getString(cursor.getColumnIndex(Db_contracts.RoutineDB.COLUMN_SECTION_NAME)));
            //routine.setStreamName(cursor.getString(cursor.getColumnIndex(Db_contracts.RoutineDB.COLUMN_STREAM_NAME)));
            routine.setStartTime(cursor.getString(cursor.getColumnIndex(Db_contracts.RoutineDB.COLUMN_START_NAME)));
            routine.setEndTime(cursor.getString(cursor.getColumnIndex(Db_contracts.RoutineDB.COLUMN_END_NAME)));
            routine.setRoom_id(cursor.getInt(cursor.getColumnIndex(Db_contracts.RoutineDB.COLUMN_ROOM_ID)));
            routine.setTeacher_details(teacher_details);
        }

        db.close();

        return routine;
    }

    public boolean insert_zone(ZoneInfo zoneInfo) {
        try {
            ContentValues values = new ContentValues();
            values.put(Db_contracts.ZoneInfo.COLUMN_ZONE_ID, zoneInfo.getId());
            values.put(Db_contracts.ZoneInfo.COLUMN_CENTER, zoneInfo.getCenterPoint());
            values.put(Db_contracts.ZoneInfo.COLUMN_POINT_A, zoneInfo.getPointA());
            values.put(Db_contracts.ZoneInfo.COLUMN_POINT_B, zoneInfo.getPointB());
            values.put(Db_contracts.ZoneInfo.COLUMN_POINT_C, zoneInfo.getPointC());
            values.put(Db_contracts.ZoneInfo.COLUMN_POINT_D, zoneInfo.getPointD());

            if (!getZoneId(zoneInfo.getId())) {

                getWritableDatabase().insert(Db_contracts.ZoneInfo.TABLE_ZONE, null, values);

                Log.d(TAG, "Zone Added");
                return true;
            } else {

                getWritableDatabase().update(Db_contracts.ZoneInfo.TABLE_ZONE, values, Db_contracts.ZoneInfo.COLUMN_ZONE_ID + "=" + zoneInfo.getId(), null);

                Log.d(TAG, "Zone updated");
                return false;
            }
        } finally {
            try {
                getWritableDatabase().close();
            } catch (Exception ignore) {
            }
        }

    }

    public boolean getZoneId(int uid) {
        String read_query = " SELECT " + Db_contracts.ZoneInfo.COLUMN_ZONE_ID + " FROM " + Db_contracts.ZoneInfo.TABLE_ZONE;
        Cursor cursor = getReadableDatabase().rawQuery(read_query, null);

        try {
            if (cursor == null)
                return false;

            if (cursor.moveToFirst()) {
                Log.d(TAG, "getUid: " + cursor.getInt(cursor.getColumnIndex(Db_contracts.ZoneInfo.COLUMN_ZONE_ID)));
                do {
                    if (uid == cursor.getInt(cursor.getColumnIndex(Db_contracts.ZoneInfo.COLUMN_ZONE_ID))) {
                        return true;
                    }
                }while (cursor.moveToNext());
            }
        } catch (SQLiteException e) {

        } finally {
            try {
                cursor.close();
            } catch (Exception ignore) {
            }
        }
        return false;
    }

    public ZoneInfo getZoneInfo(String uid) {
        SQLiteDatabase db = getReadableDatabase();
        ZoneInfo zoneInfo = null;
        try {
            String[] params = new String[]{uid};

            Cursor cursor = db.query(Db_contracts.ZoneInfo.TABLE_ZONE, null,
                    Db_contracts.ZoneInfo.COLUMN_ZONE_ID + " = ?", params,
                    null, null, null);

            try {
                if (cursor != null && cursor.moveToNext()) {
                    Log.d("dev", "database read: " + cursor.getString(cursor.getColumnIndex(Db_contracts.ZoneInfo.COLUMN_CENTER)));
                    zoneInfo = new ZoneInfo();
                    zoneInfo.setId(cursor.getInt(cursor.getColumnIndex(Db_contracts.ZoneInfo.COLUMN_ZONE_ID)));
                    zoneInfo.setCenterPoint(cursor.getString(cursor.getColumnIndex(Db_contracts.ZoneInfo.COLUMN_CENTER)));
                    zoneInfo.setPointA(cursor.getString(cursor.getColumnIndex(Db_contracts.ZoneInfo.COLUMN_POINT_A)));
                    zoneInfo.setPointB(cursor.getString(cursor.getColumnIndex(Db_contracts.ZoneInfo.COLUMN_POINT_B)));
                    zoneInfo.setPointC(cursor.getString(cursor.getColumnIndex(Db_contracts.ZoneInfo.COLUMN_POINT_C)));
                    zoneInfo.setPointD(cursor.getString(cursor.getColumnIndex(Db_contracts.ZoneInfo.COLUMN_POINT_D)));
                }
            } catch (SQLiteException e) {

            } finally {
                try {
                    cursor.close();
                } catch (Exception ignore) {
                }
            }
        } finally {
            try {
                db.close();
            } catch (Exception ignore) {
            }
        }
        return zoneInfo;
    }

    public ArrayList<ZoneInfo> getAllZoneInfo() {

        ArrayList<ZoneInfo> list = new ArrayList<ZoneInfo>();

        String selectQuery = "SELECT  * FROM " + Db_contracts.ZoneInfo.TABLE_ZONE;

        SQLiteDatabase db = this.getReadableDatabase();
        try {

            Cursor cursor = db.rawQuery(selectQuery, null);
            try {

                if (cursor.moveToFirst()) {
                    do {
                        ZoneInfo zoneInfo = new ZoneInfo();
                        zoneInfo.setId(cursor.getInt(cursor.getColumnIndex(Db_contracts.ZoneInfo.COLUMN_ZONE_ID)));
                        zoneInfo.setCenterPoint(cursor.getString(cursor.getColumnIndex(Db_contracts.ZoneInfo.COLUMN_CENTER)));
                        zoneInfo.setPointA(cursor.getString(cursor.getColumnIndex(Db_contracts.ZoneInfo.COLUMN_POINT_A)));
                        zoneInfo.setPointB(cursor.getString(cursor.getColumnIndex(Db_contracts.ZoneInfo.COLUMN_POINT_B)));
                        zoneInfo.setPointC(cursor.getString(cursor.getColumnIndex(Db_contracts.ZoneInfo.COLUMN_POINT_C)));
                        zoneInfo.setPointD(cursor.getString(cursor.getColumnIndex(Db_contracts.ZoneInfo.COLUMN_POINT_D)));

                        list.add(zoneInfo);
                    } while (cursor.moveToNext());
                }

            } finally {
                try {
                    cursor.close();
                } catch (Exception ignore) {
                }
            }

        } finally {
            try {
                db.close();
            } catch (Exception ignore) {
            }
        }

        return list;
    }

}
