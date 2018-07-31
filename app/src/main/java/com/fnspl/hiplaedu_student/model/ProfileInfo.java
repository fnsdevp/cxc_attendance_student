package com.fnspl.hiplaedu_student.model;

/**
 * Created by FNSPL on 9/17/2017.
 */

public class ProfileInfo {
    private int id;

    private int class_id;

    private String phone;

    private String username;

    private String address;

    private String email;

    private String name;

    private String className;

    private int section_id;

    private String rollNumber;

    private String photo_id;

    private String persistedFaceId;

    private String photo;

    private String section;

    private String semester;

    public String getPersistedFaceId() {
        return persistedFaceId;
    }

    public void setPersistedFaceId(String persistedFaceId) {
        this.persistedFaceId = persistedFaceId;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getClass_id() {
        return class_id;
    }

    public void setClass_id(int class_id) {
        this.class_id = class_id;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public int getSection_id() {
        return section_id;
    }

    public void setSection_id(int section_id) {
        this.section_id = section_id;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getPhoto_id() {
        return photo_id;
    }

    public void setPhoto_id(String photo_id) {
        this.photo_id = photo_id;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [id = "+id+", class_id = "+class_id+", phone = "+phone+", username = "+username+", address = "+address+", email = "+email+", name = "+name+", class = "+className+", section_id = "+section_id+", rollNumber = "+rollNumber+", photo_id = "+photo_id+", photo = "+photo+", section = "+section+"]";
    }
}
