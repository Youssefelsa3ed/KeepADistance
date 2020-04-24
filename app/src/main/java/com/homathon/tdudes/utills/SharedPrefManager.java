package com.homathon.tdudes.utills;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.homathon.tdudes.data.User;


public class SharedPrefManager {
    private final String SHARED_PREF_NAME = "the_task";
    private final String LOGIN_STATUS = "login_status";
    private final String USER_DATA = "user_data";

    private Context mContext;

    public SharedPrefManager(Context mContext) {
        this.mContext = mContext;
    }

    public Boolean getLoginStatus() {
        final SharedPreferences sharedPreferences = mContext.getSharedPreferences(
                SHARED_PREF_NAME, 0);
        return sharedPreferences.getBoolean(LOGIN_STATUS, false);
    }

    public void setLoginStatus(Boolean status) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(LOGIN_STATUS, status);
        editor.apply();
    }

    public void setUserData(User data){
        SharedPreferences.Editor editor = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE).edit();
        Gson gson = new Gson();
        String json = gson.toJson(data);
        editor.putString(USER_DATA, json);
        setLoginStatus(true);
        editor.apply();
    }


    public User getUserData(){
        SharedPreferences prefs = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString(USER_DATA, "");
        return gson.fromJson(json, User.class) ;
    }

    public void logout() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        setLoginStatus(false);
    }
}
