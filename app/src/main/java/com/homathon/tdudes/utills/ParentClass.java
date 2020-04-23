package com.homathon.tdudes.utills;

import android.content.Context;
import android.widget.Toast;

import com.homathon.tdudes.R;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

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
}
