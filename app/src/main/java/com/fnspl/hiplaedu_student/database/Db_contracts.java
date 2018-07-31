package com.fnspl.hiplaedu_student.database;

import android.provider.BaseColumns;

/**
 * Created by FNSPL on 8/25/2017.
 */

public class Db_contracts {


    private Db_contracts(){}

    public static class RoutineDB implements BaseColumns{
        public  static final    String TABLE_ROUTINE = "routine";
        public  static final    String COLUMN_ROUTINE = "routineId";
        public  static final    String COLUMN_SUBJECT_ID = "subjectId";
        public  static final    String COLUMN_DAY = "day";
        public  static final    String COLUMN_TEACHER_ID = "teacherId";
        public  static final    String COLUMN_CLASS_ID = "classId";
        public  static final    String COLUMN_SECTION_ID = "sectionId";
        public  static final    String COLUMN_YEAR = "year";
        public  static final    String COLUMN_STREAM_ID = "streamId";
        public  static final    String COLUMN_CLASS_NAME = "className";
        public  static final    String COLUMN_TEACHER_NAME = "teacherName";
        public  static final    String COLUMN_SUBJECT_NAME = "subjectname";
        public  static final    String COLUMN_SECTION_NAME = "sectionName";
        public  static final    String COLUMN_STREAM_NAME = "streamName";
        public  static final    String COLUMN_START_NAME = "startTime";
        public  static final    String COLUMN_END_NAME = "endTime";
        public  static final    String COLUMN_TIME_STAMP = "timeStamp";
        public  static final    String COLUMN_ROOM_ID = "roomId";
    }

    public static class ZoneInfo implements BaseColumns{

        public  static final String TABLE_ZONE = "zoneInfo";
        public  static final String COLUMN_ZONE_ID = "zoneId";
        public  static final String COLUMN_CENTER = "centerPoint";
        public  static final String COLUMN_POINT_A = "pointA";
        public  static final String COLUMN_POINT_B = "pointB";
        public  static final String COLUMN_POINT_C = "pointC";
        public  static final String COLUMN_POINT_D = "pointD";

    }
}
