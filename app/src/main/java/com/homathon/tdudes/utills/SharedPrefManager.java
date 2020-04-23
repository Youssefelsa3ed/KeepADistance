package com.homathon.tdudes.utills;

import android.content.Context;
import android.content.SharedPreferences;


public class SharedPrefManager {
    private final String SHARED_PREF_NAME = "the_task";
    private final String LOGIN_STATUS = "login_status";

    private Context mContext;

    public SharedPrefManager(Context mContext) {
        this.mContext = mContext;
    }

    public Boolean getLoginStatus() {
        final SharedPreferences sharedPreferences = mContext.getSharedPreferences(
                SHARED_PREF_NAME, 0);
        return sharedPreferences.getBoolean(LOGIN_STATUS, false);
    }

    private void setLoginStatus(Boolean status) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(LOGIN_STATUS, status);
        editor.apply();
    }

    public void logout() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        setLoginStatus(false);
    }
}
