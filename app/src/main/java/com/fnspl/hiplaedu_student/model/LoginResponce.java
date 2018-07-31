package com.fnspl.hiplaedu_student.model;

/**
 * Created by FNSPL on 9/16/2017.
 */

public class LoginResponce {

    private String status;
    private String message;
    private String user_id;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
}
