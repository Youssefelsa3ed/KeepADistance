package com.homathon.tdudes.data;

import androidx.annotation.NonNull;

public class User {
    private String phone;
    private String name;
    private String email;
    private String id;
    private boolean infected;

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isInfected() {
        return infected;
    }

    public void setInfected(boolean infected) {
        this.infected = infected;
    }

    @NonNull
    @Override
    public String toString() {
        return id + " " + name;
    }
}
