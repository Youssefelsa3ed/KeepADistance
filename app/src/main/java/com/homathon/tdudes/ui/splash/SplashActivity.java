package com.homathon.tdudes.ui.splash;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.CountDownTimer;

import androidx.appcompat.app.AppCompatActivity;

import com.homathon.tdudes.R;
import com.homathon.tdudes.ui.login.LoginActivity;
import com.homathon.tdudes.ui.main.MainActivity;
import com.homathon.tdudes.utills.SharedPrefManager;

import java.util.Locale;

public class SplashActivity extends AppCompatActivity {

    private String language;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        readSharedPreference();
    }

    private void readSharedPreference() {
        SharedPreferences prefs = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        language = prefs.getString("language", "ar");
        setLanguages();
        loading();
    }

    public void loading() {
        new CountDownTimer(3000, 1000) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                SharedPrefManager sharedPrefManager = new SharedPrefManager(SplashActivity.this);
                if (sharedPrefManager.getLoginStatus())
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                else
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                finish();
            }
        }.start();
    }

    private void setLanguages() {
        Configuration config = new Configuration();
        config.locale = new Locale(language);
        getResources().updateConfiguration(config,getResources().getDisplayMetrics());
    }
}