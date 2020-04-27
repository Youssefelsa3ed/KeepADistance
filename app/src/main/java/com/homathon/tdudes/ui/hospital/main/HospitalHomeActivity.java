package com.homathon.tdudes.ui.hospital.main;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.navigation.NavigationView;
import com.homathon.tdudes.R;
import com.homathon.tdudes.databinding.ActivityHospitalHomeBinding;
import com.homathon.tdudes.ui.splash.SplashActivity;
import com.homathon.tdudes.utills.ParentClass;
import com.homathon.tdudes.utills.SharedPrefManager;
import com.homathon.tdudes.utills.Utils;

import java.util.Objects;

public class HospitalHomeActivity extends AppCompatActivity implements MenuItem.OnMenuItemClickListener {

    private ActivityHospitalHomeBinding binding;
    private BottomSheetBehavior<RelativeLayout> bottomSheetBehavior;
    private RelativeLayout relativeLayout;
    //private AppBarConfiguration mAppBarConfiguration;
    private NavController navController;
    private boolean checked;
    CurrentFragment fragment;

    enum CurrentFragment{
        Home,
        Menu,
        Profile
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_hospital_home);
        relativeLayout = binding.bottom.bottomLayout;
        bottomSheetBehavior = BottomSheetBehavior.from(relativeLayout);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fragment = CurrentFragment.Home;

        /*DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);*/
        navController = Navigation.findNavController(this, R.id.nav_hospital_home_fragment);
        /*mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_home).setDrawerLayout(drawer).build();
        NavigationUI.setupWithNavController(navigationView, navController);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);

        MenuItem logout = navigationView.getMenu().findItem(R.id.nav_logout);
        logout.setOnMenuItemClickListener(this);*/
        binding.bottom.home.setOnClickListener(v -> {
            if(fragment != CurrentFragment.Home){
                fragment = CurrentFragment.Home;
                binding.bottom.home.setCardBackgroundColor(getColorStateList(R.color.colorPrimary));
                binding.bottom.menu.setImageTintList(getColorStateList(R.color.colorGray));
                binding.bottom.profile.setImageTintList(getColorStateList(R.color.colorGray));
                navController.navigate(R.id.action_to_nav_home);
            }
        });
        binding.bottom.menu.setOnClickListener(v -> {
            if(fragment != CurrentFragment.Menu){
                fragment = CurrentFragment.Menu;
                binding.bottom.menu.setImageTintList(getColorStateList(R.color.colorPrimary));
                binding.bottom.home.setCardBackgroundColor(getColorStateList(R.color.colorGray));
                binding.bottom.profile.setImageTintList(getColorStateList(R.color.colorGray));
                navController.navigate(R.id.action_to_hospital_menu);
            }
        });
        binding.bottom.profile.setOnClickListener(v -> {
            /*if(fragment != CurrentFragment.Profile){
                fragment = CurrentFragment.Profile;
                binding.bottom.profile.setBackgroundColor(getColor(R.color.colorPrimary));
                binding.bottom.home.setCardBackgroundColor(getColorStateList(R.color.colorGray));
                binding.bottom.menu.setBackgroundColor(getColor(R.color.colorGray));
                navController.navigate(R.id.action_to_nav_scan);
            }*/
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setOnMenuItemClickListener(this);
        }
        return true;
    }

    /*@Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }*/

    public void logoutUser() {
        SharedPrefManager sharedPrefManager = new SharedPrefManager(this);
        sharedPrefManager.logout();
        startActivity(new Intent(this, SplashActivity.class));
        finish();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if(item.getItemId() == R.id.nav_logout)
            logoutUser();
        else if(item.getItemId() == R.id.change_language)
            OpenChangeLanguageDialog();
        return false;
    }

    private void OpenChangeLanguageDialog(){
        Dialog changeLanguageDialog = new Dialog(this);
        changeLanguageDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        changeLanguageDialog.setContentView(R.layout.dialog_change_language);
        Objects.requireNonNull(changeLanguageDialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        Objects.requireNonNull(changeLanguageDialog.getWindow()).setLayout(WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT);
        Button btnChange = changeLanguageDialog.findViewById(R.id.btnChange);
        RelativeLayout linearEnglish = changeLanguageDialog.findViewById(R.id.linearEnglish);
        RelativeLayout linearArabic = changeLanguageDialog.findViewById(R.id.linearArabic);
        ImageView linearEnglishSelected = changeLanguageDialog.findViewById(R.id.linearEnglishSelected);
        ImageView linearArabicSelected = changeLanguageDialog.findViewById(R.id.linearArabicSelected);
        if (ParentClass.getLocalization(this).equals("ar")) {
            checked = true;
            linearEnglishSelected.setVisibility(View.GONE);
            linearArabicSelected.setVisibility(View.VISIBLE);
        } else {
            checked = false;
            linearEnglishSelected.setVisibility(View.VISIBLE);
            linearArabicSelected.setVisibility(View.GONE);
        }

        linearArabic.setOnClickListener(view -> {
            checked = true;
            linearEnglishSelected.setVisibility(View.GONE);
            linearArabicSelected.setVisibility(View.VISIBLE);
        });

        linearEnglish.setOnClickListener(view -> {
            checked = false;
            linearEnglishSelected.setVisibility(View.VISIBLE);
            linearArabicSelected.setVisibility(View.GONE);
        });

        btnChange.setOnClickListener(v-> {
            if (checked && ParentClass.getLocalization(this).equals("en")) {
                changeLanguage("ar");
            }
            else
                changeLanguageDialog.dismiss();
            if (!checked && ParentClass.getLocalization(this).equals("ar"))
                changeLanguage("en");
            else
                changeLanguageDialog.dismiss();
        });
        changeLanguageDialog.show();
    }

    private void changeLanguage(String language) {
        SharedPreferences prefs = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        prefs.edit().putString("language", language).apply();
        startActivity(new Intent(this, SplashActivity.class));
        finishAffinity();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION}, 2);
    }
}
