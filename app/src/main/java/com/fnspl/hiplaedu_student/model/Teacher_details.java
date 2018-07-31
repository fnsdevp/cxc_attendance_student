package com.fnspl.hiplaedu_student.model;

/**
 * Created by FNSPL on 9/18/2017.
 */

public class Teacher_details {

    private int id;

    private String phone;

    private String username;

    private String address;

    private String email;

    private String department;

    private String name;

    private String designation;

    private String photo_id;

    private String photo;

    public int getId ()
    {
        return id;
    }

    public void setId (int id)
    {
        this.id = id;
    }

    public String getPhone ()
    {
        return phone;
    }

    public void setPhone (String phone)
    {
        this.phone = phone;
    }

    public String getUsername ()
    {
        return username;
    }

    public void setUsername (String username)
    {
        this.username = username;
    }

    public String getAddress ()
    {
        return address;
    }

    public void setAddress (String address)
    {
        this.address = address;
    }

    public String getEmail ()
    {
        return email;
    }

    public void setEmail (String email)
    {
        this.email = email;
    }

    public String getDepartment ()
    {
        return department;
    }

    public void setDepartment (String department)
    {
        this.department = department;
    }

    public String getName ()
    {
        return name;
    }

    public void setName (String name)
    {
        this.name = name;
    }

    public String getDesignation ()
    {
        return designation;
    }

    public void setDesignation (String designation)
    {
        this.designation = designation;
    }

    public String getPhoto_id ()
    {
        return photo_id;
    }

    public void setPhoto_id (String photo_id)
    {
        this.photo_id = photo_id;
    }

    public String getPhoto ()
    {
        return photo;
    }

    public void setPhoto (String photo)
    {
        this.photo = photo;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [id = "+id+", phone = "+phone+", username = "+username+", address = "+address+", email = "+email+", department = "+department+", name = "+name+", designation = "+designation+", photo_id = "+photo_id+", photo = "+photo+"]";
    }

}
