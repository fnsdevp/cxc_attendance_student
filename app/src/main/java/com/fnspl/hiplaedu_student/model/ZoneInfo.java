package com.fnspl.hiplaedu_student.model;

/**
 * Created by FNSPL on 11/2/2017.
 */

public class ZoneInfo {

    private int id;
    private String centerPoint;
    private String pointA;
    private String pointB;
    private String pointC;
    private String pointD;

    public ZoneInfo(){

    }

    public ZoneInfo(int id, String centerPoint, String pointA, String pointB, String pointC, String pointD) {
        this.id = id;
        this.centerPoint = centerPoint;
        this.pointA = pointA;
        this.pointB = pointB;
        this.pointC = pointC;
        this.pointD = pointD;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCenterPoint() {
        return centerPoint;
    }

    public void setCenterPoint(String centerPoint) {
        this.centerPoint = centerPoint;
    }

    public String getPointA() {
        return pointA;
    }

    public void setPointA(String pointA) {
        this.pointA = pointA;
    }

    public String getPointB() {
        return pointB;
    }

    public void setPointB(String pointB) {
        this.pointB = pointB;
    }

    public String getPointC() {
        return pointC;
    }

    public void setPointC(String pointC) {
        this.pointC = pointC;
    }

    public String getPointD() {
        return pointD;
    }

    public void setPointD(String pointD) {
        this.pointD = pointD;
    }
}
