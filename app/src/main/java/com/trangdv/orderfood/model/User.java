package com.trangdv.orderfood.model;


import androidx.annotation.NonNull;

public class User {
    private String Name;
    private String Password;
    private String Phone;

    public User() {
    }

    public User(String name, String password) {
        Name = name;
        Password = password;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getPhone() {

        return Phone;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    @NonNull
    @Override
    public String toString() {
        return getPhone();
    }
}
