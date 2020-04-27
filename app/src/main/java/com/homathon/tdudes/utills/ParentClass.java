package com.homathon.tdudes.utills;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.widget.Toast;

import com.homathon.tdudes.R;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

public class ParentClass {

    public static void handleException(Context context, Throwable t) {
        if (t instanceof UnknownHostException || t instanceof ConnectException || t instanceof SocketTimeoutException)
            makeToast(context, context.getResources().getString(R.string.connection_error));
        else
            makeToast(context, t.getLocalizedMessage());

    }

    public static void makeToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static String getLocalization(Context context) {
        return context.getSharedPreferences(context.getPackageName(), MODE_PRIVATE).getString("language", "ar");
    }

    public static String getAddress(Context context, double lat , double lng)   {

        String address;
        Geocoder geocoder;
        List<Address> addresses;
        Locale locale = new Locale(getLocalization(context)) ;
        geocoder = new Geocoder(context ,  locale);
        try {
            addresses = geocoder.getFromLocation(lat, lng, 1);
            address = addresses.get(0).getAddressLine(0);
        }
        catch (IOException | IndexOutOfBoundsException e){
            address = context.getString(R.string.unknown_location);
        }

        return address ;
    }

}
