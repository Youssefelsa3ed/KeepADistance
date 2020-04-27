package com.homathon.tdudes.ui.home;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.homathon.tdudes.R;
import com.homathon.tdudes.utills.LocationObject;

import java.util.List;
import java.util.Objects;

public class HomeFragment extends Fragment implements OnMapReadyCallback, LocationListener, ValueEventListener {
    private GoogleMap mMap;
    private PolygonOptions polygonOptions;
    private Polygon polygon;
    LocationManager mLocationManager;

    private static final String TAG = "HomeFragment";
    private DatabaseReference locations;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        polygonOptions = new PolygonOptions();
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null)
            mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        getCurrentLocation();

        locations = FirebaseDatabase.getInstance().getReference("locations");
        locations.addValueEventListener(this);
        // Add a marker in Sydney and move the camera
        /*LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/
    }

    private void countPolygonPoints() {
        if(polygonOptions.getPoints().size() > 3){
            polygonOptions.strokeColor(Color.RED);
            polygonOptions.strokeWidth((float) 0.30);
            polygonOptions.fillColor(Color.BLUE);
            polygon = mMap.addPolygon(polygonOptions);
        }
    }

    private BitmapDescriptor getMarkerIconFromDrawable() {
        Canvas canvas = new Canvas();
        Drawable drawable = getResources().getDrawable(R.drawable.map_infection);
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void getCurrentLocation() {
        mLocationManager = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);
        if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location = getLastKnownLocation();

        if (location != null) {
            LatLng myLoc = new LatLng(location.getLatitude(), location.getLongitude());
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(myLoc)
                    .bearing(90)
                    .tilt(40)
                    .build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLoc, 13));
        } else {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 0, this);
        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setMessage(getString(R.string.pleaseOpenGPS))
                .setCancelable(false)
                .setPositiveButton(R.string.open, (dialog, id) -> startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                .setNegativeButton(R.string.close, (DialogInterface.OnClickListener) (dialog, which) -> dialog.dismiss());
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private Location getLastKnownLocation() {
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return bestLocation;
            }
            Location l = mLocationManager.getLastKnownLocation(provider);

            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {

                bestLocation = l;
            }
        }
        return bestLocation;
    }


    @Override
    public void onLocationChanged(Location location) {
        LatLng myLoc = new LatLng(location.getLatitude(), location.getLongitude());
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(myLoc)
                .bearing(90)
                .tilt(40)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLoc, 16));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        for (DataSnapshot item : dataSnapshot.getChildren()){

            for(DataSnapshot subItem : item.getChildren()){
                LocationObject locationObject = subItem.getValue(LocationObject.class);
                if(locationObject == null || !locationObject.infected)
                    continue;
                polygonOptions.add(new LatLng(locationObject.latitude, locationObject.longitude));
                mMap.addMarker(new MarkerOptions().position(new LatLng(locationObject.latitude, locationObject.longitude)).icon(getMarkerIconFromDrawable()));
            }
        }
        countPolygonPoints();
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }

    // TODO: 4/23/2020 handle QRCode
    /*@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode== Activity.RESULT_OK && requestCode == 100) {
            if (data != null){
                Log.d(TAG, "onActivityResult: " + data.getStringExtra("userId"));
            }
        }
    }

    private void scanQRCode() {
        Intent intent = new Intent(requireContext() , LiveBarcodeScanningActivity.class);
        startActivityForResult(intent , 100);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // If request is cancelled, the result arrays are empty.
        if (requestCode == 2 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            scanQRCode();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!Utils.allPermissionsGranted(requireContext()))
            Utils.requestRuntimePermissions(requireActivity());
        else
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 2);
    }*/

    @Override
    public void onPause() {
        super.onPause();
        locations.removeEventListener(this);
    }
}