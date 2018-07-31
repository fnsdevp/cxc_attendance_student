package com.fnspl.hiplaedu_student.model;

/**
 * Created by FNSPL on 9/14/2017.
 */

public class RoutinePeriod {

    private int lectureNo=0;

    private String startTime;

    private String subject_name;

    private int class_id;

    private Teacher_details teacher_details;

    private int routine_history_id;

    private int subject_id;

    private String classname;

    private int section_id;

    private String year;

    private String day;

    private String endTime;

    private String section_name;

    private String semester;

    private int room_id=0;

    public int getLectureNo() {
        return lectureNo;
    }

    public void setLectureNo(int lectureNo) {
        this.lectureNo = lectureNo;
    }

    public int getRoom_id() {
        return room_id;
    }

    public void setRoom_id(int room_id) {
        this.room_id = room_id;
    }

    public String getStartTime ()
    {
        return startTime;
    }

    public void setStartTime (String startTime)
    {
        this.startTime = startTime;
    }

    public String getSubject_name ()
    {
        return subject_name;
    }

    public void setSubject_name (String subject_name)
    {
        this.subject_name = subject_name;
    }

    public int getClass_id ()
    {
        return class_id;
    }

    public void setClass_id (int class_id)
    {
        this.class_id = class_id;
    }

    public Teacher_details getTeacher_details ()
    {
        return teacher_details;
    }

    public void setTeacher_details (Teacher_details teacher_details)
    {
        this.teacher_details = teacher_details;
    }

    public int getRoutine_history_id ()
    {
        return routine_history_id;
    }

    public void setRoutine_history_id (int routine_history_id)
    {
        this.routine_history_id = routine_history_id;
    }

    public int getSubject_id ()
    {
        return subject_id;
    }

    public void setSubject_id (int subject_id)
    {
        this.subject_id = subject_id;
    }

    public String getClassname ()
    {
        return classname;
    }

    public void setClassname (String classname)
    {
        this.classname = classname;
    }

    public int getSection_id ()
    {
        return section_id;
    }

    public void setSection_id (int section_id)
    {
        this.section_id = section_id;
    }

    public String getYear ()
    {
        return year;
    }

    public void setYear (String year)
    {
        this.year = year;
    }

    public String getDay ()
    {
        return day;
    }

    public void setDay (String day)
    {
        this.day = day;
    }

    public String getEndTime ()
    {
        return endTime;
    }

    public void setEndTime (String endTime)
    {
        this.endTime = endTime;
    }

    public String getSection_name ()
    {
        return section_name;
    }

    public void setSection_name (String section_name)
    {
        this.section_name = section_name;
    }

    public String getSemester ()
    {
        return semester;
    }

    public void setSemester (String semester)
    {
        this.semester = semester;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [startTime = "+startTime+", subject_name = "+subject_name+", class_id = "+class_id+", teacher_details = "+teacher_details+", routine_history_id = "+routine_history_id+", subject_id = "+subject_id+", classname = "+classname+", section_id = "+section_id+", year = "+year+", day = "+day+", endTime = "+endTime+", section_name = "+section_name+", semester = "+semester+"]";
    }
}
