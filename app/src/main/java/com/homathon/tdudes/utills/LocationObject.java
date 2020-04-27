package com.homathon.tdudes.utills;

import android.content.Context;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class LocationObject {
    public String title;
    public double latitude, longitude;
    public boolean infected;

    public LocationObject() {
        // Default constructor required for calls to DataSnapshot.getValue(LocationObject.class)
    }

    public LocationObject(double lat, double lng, Context context, boolean infected) {
        this.title = ParentClass.getAddress(context, lat, lng);
        this.latitude = lat;
        this.longitude = lng;
        this.infected = infected;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("latitude", latitude);
        result.put("longitude", longitude);
        result.put("title", title);
        result.put("infected", infected);
        return result;
    }
}
