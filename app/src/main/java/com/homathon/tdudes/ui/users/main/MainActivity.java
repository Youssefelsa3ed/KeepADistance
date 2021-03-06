package com.homathon.tdudes.ui.users.main;

import android.Manifest;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.homathon.tdudes.R;
import com.homathon.tdudes.data.User;
import com.homathon.tdudes.databinding.ActivityMainBinding;
import com.homathon.tdudes.ui.splash.SplashActivity;
import com.homathon.tdudes.utills.GeofenceBroadcastReceiver;
import com.homathon.tdudes.utills.LocationObject;
import com.homathon.tdudes.utills.LocationUpdatesBroadcastReceiver;
import com.homathon.tdudes.utills.LocationUpdatesIntentService;
import com.homathon.tdudes.utills.ParentClass;
import com.homathon.tdudes.utills.PushNotification;
import com.homathon.tdudes.utills.SharedPrefManager;
import com.homathon.tdudes.utills.Utils;
import com.homathon.tdudes.viewmodels.LocationUpdateViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements MenuItem.OnMenuItemClickListener {

    //private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private NavController navController;
    private boolean checked;
    private static final String TAG = "MainActivity";
    private LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationClient;
    private GeofencingClient geofencingClient;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private List<Geofence> geofenceList;
    private PendingIntent geofencePendingIntent;
    private LocationUpdateViewModel locationUpdateViewModel;
    private BottomSheetBehavior<RelativeLayout> bottomSheetBehavior;
    private RelativeLayout relativeLayout;
    private User userData;
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
        locationUpdateViewModel = new ViewModelProvider(this).get(LocationUpdateViewModel.class);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        relativeLayout = binding.bottom.bottomLayout;
        bottomSheetBehavior = BottomSheetBehavior.from(relativeLayout);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        geofenceList = new ArrayList<>();
        //DrawerLayout drawer = findViewById(R.id.drawer_layout);
        //NavigationView navigationView = findViewById(R.id.nav_view);
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        //mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_home).setDrawerLayout(drawer).build();
        //NavigationUI.setupWithNavController(navigationView, navController);
        /*NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);*/

        /*MenuItem logout = navigationView.getMenu().findItem(R.id.nav_logout);
        logout.setOnMenuItemClickListener(this);
        TextView userName = navigationView.getHeaderView(0).findViewById(R.id.txtUserName);
        TextView userEmail = navigationView.getHeaderView(0).findViewById(R.id.txtUserEmail);*/
        SharedPrefManager sharedPrefManager = new SharedPrefManager(this);
        userData = sharedPrefManager.getUserData();
        /*userName.setText(userData.getName());
        userEmail.setText(userData.getEmail());*/
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
                navController.navigate(R.id.action_to_user_menu);
            }
        });
        binding.bottom.profile.setOnClickListener(v -> {
            /*Toast.makeText(this, "openProfile", Toast.LENGTH_SHORT).show();
            if(fragment != CurrentFragment.Profile){
                fragment = CurrentFragment.Profile;
                binding.bottom.profile.setBackgroundColor(getColor(R.color.colorPrimary));
                binding.bottom.home.setCardBackgroundColor(getColorStateList(R.color.colorGray));
                binding.bottom.menu.setBackgroundColor(getColor(R.color.colorGray));
            }*/
        });

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference users = database.getReference("users");
        DatabaseReference locations = database.getReference("locations");
        locations.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot item : dataSnapshot.getChildren()){
                    if(Objects.equals(item.getKey(), userData.getId()))
                        return;
                    for(DataSnapshot subItem : item.getChildren()){
                        LocationObject locationObject = subItem.getValue(LocationObject.class);
                        if(locationObject == null || !locationObject.infected)
                            continue;
                        geofenceList.add(new Geofence.Builder()
                                .setRequestId(locationObject.title)
                                .setCircularRegion(locationObject.latitude, locationObject.longitude, 40)
                                .setExpirationDuration(30000)
                                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                                .build());
                    }
                }
                if(geofenceList.size() > 0)
                    geofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        users.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(!Objects.equals(dataSnapshot.getKey(), userData.getId())){
                    PushNotification.pushOrderNotification(MainActivity.this, "New infection", dataSnapshot.getValue(String.class) + " was reported as infected");
                }
                else {
                    SharedPrefManager sharedPrefManager = new SharedPrefManager(MainActivity.this);
                    userData.setInfected(true);
                    sharedPrefManager.setUserData(userData);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                if(Objects.equals(dataSnapshot.getKey(), userData.getId())){
                    SharedPrefManager sharedPrefManager = new SharedPrefManager(MainActivity.this);
                    userData.setInfected(false);
                    sharedPrefManager.setUserData(userData);
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        geofencingClient = LocationServices.getGeofencingClient(this);
        createLocationRequest();
        locationUpdateViewModel.getLocationListLiveData().observe(this, updatedLocations -> {
            Log.d(TAG, String.format("Got %s locations", updatedLocations.size()));
            if(updatedLocations.size() > 0){
                String key = userData.getName() + String.valueOf(updatedLocations.get(0).getLatitude()).replace(".","")
                        + String.valueOf(updatedLocations.get(0).getLongitude()).replace(".","");
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put(key, new LocationObject(updatedLocations.get(0).getLatitude(), updatedLocations.get(0).getLongitude(), this, userData.isInfected()).toMap());
                locations.child(userData.getId()).updateChildren(childUpdates);
            }
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
        FirebaseAuth.getInstance().signOut();
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

    private PendingIntent getPendingIntent() {
        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.O){
            Intent intent = new Intent(this, LocationUpdatesIntentService.class);
            intent.setAction(LocationUpdatesIntentService.ACTION_PROCESS_UPDATES);
            return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        Intent intent = new Intent(this, LocationUpdatesBroadcastReceiver.class);
        intent.setAction(LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES);
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission was granted.
            requestLocationUpdates();
            locationUpdateViewModel.startLocationUpdates();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
        else {
            locationUpdateViewModel.startLocationUpdates();
            requestLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        removeLocationUpdates();
        if ((locationUpdateViewModel.getReceivingLocationUpdates().getValue() == null || locationUpdateViewModel.getReceivingLocationUpdates().getValue()) &&
                (!Utils.hasPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION))) {
            locationUpdateViewModel.stopLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        removeLocationUpdates();
    }

    protected void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    public void requestLocationUpdates() {
        try {
            Log.i(TAG, "Starting location updates");
            Utils.setRequestingLocationUpdates(this, true);
            fusedLocationClient.requestLocationUpdates(locationRequest, getPendingIntent());
        } catch (SecurityException e) {
            Utils.setRequestingLocationUpdates(this, false);
            e.printStackTrace();
        }
    }

    public void removeLocationUpdates() {
        Log.i(TAG, "Removing location updates");
        Utils.setRequestingLocationUpdates(this, false);
        fusedLocationClient.removeLocationUpdates(getPendingIntent());
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geofenceList);
        return builder.build();
    }


    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        geofencePendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return geofencePendingIntent;
    }

}
