package com.fnspl.hiplaedu_student.model;

/**
 * Created by FNSPL on 11/15/2017.
 */

public class SubjectReport {

    private String TotalClass;
    private String getPresentValue;
    private String getAbsentValue;
    private String getSneakValue;

    public String getTotalClass() {
        if (TotalClass != null)
            return TotalClass;
        else
            return "0";
    }

    public void setTotalClass(String totalClass) {
        TotalClass = totalClass;
    }

    public String getGetPresentValue() {
        if (getPresentValue != null)
            return getPresentValue;
        else
            return "0";
    }

    public void setGetPresentValue(String getPresentValue) {
        this.getPresentValue = getPresentValue;
    }

    public String getGetAbsentValue() {
        if (getAbsentValue != null)
            return getAbsentValue;
        else
            return "0";
    }

    public void setGetAbsentValue(String getAbsentValue) {
        this.getAbsentValue = getAbsentValue;
    }

    public String getGetSneakValue() {
        if (getSneakValue != null)
            return getSneakValue;
        else
            return "0";
    }

    public void setGetSneakValue(String getSneakValue) {
        this.getSneakValue = getSneakValue;
    }
}
