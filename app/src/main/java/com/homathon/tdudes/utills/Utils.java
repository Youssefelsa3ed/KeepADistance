package com.homathon.tdudes.utills;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.hardware.Camera;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.preference.PreferenceManager;


import com.homathon.tdudes.R;
import com.homathon.tdudes.ui.infected.main.MainActivity;
import com.homathon.tdudes.utills.camera.CameraSizePair;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static androidx.core.content.ContextCompat.checkSelfPermission;


/** Utility class to provide helper methods. */
public class Utils {

    /**
     * If the absolute difference between aspect ratios is less than this tolerance, they are
     * considered to be the same aspect ratio.
     */
    public static final float ASPECT_RATIO_TOLERANCE = 0.01f;

    static final int REQUEST_CODE_PHOTO_LIBRARY = 1;

    private static final String TAG = "Utils";
    public final static String KEY_LOCATION_UPDATES_REQUESTED = "location-updates-requested";
    public final static String KEY_LOCATION_UPDATES_RESULT = "location-update-result";
    public final static String CHANNEL_ID = "channel_01";

    public static void requestRuntimePermissions(Activity activity) {
        List<String> allNeededPermissions = new ArrayList<>();
        for (String permission : getRequiredPermissions(activity)) {
            if (checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                allNeededPermissions.add(permission);
            }
        }

        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    activity, allNeededPermissions.toArray(new String[0]), /* requestCode= */ 0);
        }
    }

    public static boolean allPermissionsGranted(Context context) {
        for (String permission : getRequiredPermissions(context)) {
            if (checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private static String[] getRequiredPermissions(Context context) {
        try {
            PackageInfo info =
                    context
                            .getPackageManager()
                            .getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] ps = info.requestedPermissions;
            return (ps != null && ps.length > 0) ? ps : new String[0];
        } catch (Exception e) {
            return new String[0];
        }
    }

    public static boolean isPortraitMode(Context context) {
        return context.getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_PORTRAIT;
    }

    /**
     * Generates a list of acceptable preview sizes. Preview sizes are not acceptable if there is not
     * a corresponding picture size of the same aspect ratio. If there is a corresponding picture size
     * of the same aspect ratio, the picture size is paired up with the preview size.
     *
     * <p>This is necessary because even if we don't use still pictures, the still picture size must
     * be set to a size that is the same aspect ratio as the preview size we choose. Otherwise, the
     * preview images may be distorted on some devices.
     */
    public static List<CameraSizePair> generateValidPreviewSizeList(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
        List<Camera.Size> supportedPictureSizes = parameters.getSupportedPictureSizes();
        List<CameraSizePair> validPreviewSizes = new ArrayList<>();
        for (Camera.Size previewSize : supportedPreviewSizes) {
            float previewAspectRatio = (float) previewSize.width / (float) previewSize.height;

            // By looping through the picture sizes in order, we favor the higher resolutions.
            // We choose the highest resolution in order to support taking the full resolution
            // picture later.
            for (Camera.Size pictureSize : supportedPictureSizes) {
                float pictureAspectRatio = (float) pictureSize.width / (float) pictureSize.height;
                if (Math.abs(previewAspectRatio - pictureAspectRatio) < ASPECT_RATIO_TOLERANCE) {
                    validPreviewSizes.add(new CameraSizePair(previewSize, pictureSize));
                    break;
                }
            }
        }

        // If there are no picture sizes with the same aspect ratio as any preview sizes, allow all of
        // the preview sizes and hope that the camera can handle it.  Probably unlikely, but we still
        // account for it.
        if (validPreviewSizes.size() == 0) {
            Log.w(TAG, "No preview sizes have a corresponding same-aspect-ratio picture size.");
            for (Camera.Size previewSize : supportedPreviewSizes) {
                // The null picture size will let us know that we shouldn't set a picture size.
                validPreviewSizes.add(new CameraSizePair(previewSize, null));
            }
        }

        return validPreviewSizes;
    }

    public static Bitmap getCornerRoundedBitmap(Bitmap srcBitmap, int cornerRadius) {
        Bitmap dstBitmap =
                Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(dstBitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        RectF rectF = new RectF(0, 0, srcBitmap.getWidth(), srcBitmap.getHeight());
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(srcBitmap, 0, 0, paint);
        return dstBitmap;
    }

    static void openImagePicker(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        activity.startActivityForResult(intent, REQUEST_CODE_PHOTO_LIBRARY);
    }

    static Bitmap loadImage(Context context, Uri imageUri, int maxImageDimension) throws IOException {
        InputStream inputStreamForSize = null;
        InputStream inputStreamForImage = null;
        try {
            inputStreamForSize = context.getContentResolver().openInputStream(imageUri);
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStreamForSize, /* outPadding= */ null, opts);
            int inSampleSize =
                    Math.max(opts.outWidth / maxImageDimension, opts.outHeight / maxImageDimension);

            opts = new BitmapFactory.Options();
            opts.inSampleSize = inSampleSize;
            inputStreamForImage = context.getContentResolver().openInputStream(imageUri);
            Bitmap decodedBitmap =
                    BitmapFactory.decodeStream(inputStreamForImage, /* outPadding= */ null, opts);
            return maybeTransformBitmap(context.getContentResolver(), imageUri, decodedBitmap);

        } finally {
            if (inputStreamForSize != null) {
                inputStreamForSize.close();
            }
            if (inputStreamForImage != null) {
                inputStreamForImage.close();
            }
        }
    }

    private static Bitmap maybeTransformBitmap(ContentResolver resolver, Uri uri, Bitmap bitmap) {
        int orientation = getExifOrientationTag(resolver, uri);
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_UNDEFINED:
            case ExifInterface.ORIENTATION_NORMAL:
                // Set the matrix to be null to skip the image transform.
                matrix = null;
                break;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix = new Matrix();
                matrix.postScale(-1.0f, 1.0f);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.postRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.postRotate(90.0f);
                matrix.postScale(-1.0f, 1.0f);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.postRotate(180.0f);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.postScale(1.0f, -1.0f);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.postRotate(-90.0f);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.postRotate(-90.0f);
                matrix.postScale(-1.0f, 1.0f);
                break;
            default:
                // Set the matrix to be null to skip the image transform.
                matrix = null;
                break;
        }

        if (matrix != null) {
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } else {
            return bitmap;
        }
    }

    private static int getExifOrientationTag(ContentResolver resolver, Uri imageUri) {
        if (!ContentResolver.SCHEME_CONTENT.equals(imageUri.getScheme())
                && !ContentResolver.SCHEME_FILE.equals(imageUri.getScheme())) {
            return 0;
        }

        ExifInterface exif = null;
        try (InputStream inputStream = resolver.openInputStream(imageUri)) {
            if (inputStream != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    exif = new ExifInterface(inputStream);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to open file to read rotation meta data: " + imageUri, e);
        }

        return exif !=  null
                ? exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                : ExifInterface.ORIENTATION_UNDEFINED;
    }

    public static void setRequestingLocationUpdates(Context context, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_LOCATION_UPDATES_REQUESTED, value)
                .apply();
    }

    public static boolean getRequestingLocationUpdates(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_LOCATION_UPDATES_REQUESTED, false);
    }

    /**
     * Posts a notification in the notification bar when a transition is detected.
     * If the user clicks the notification, control goes to the MainActivity.
     */
    static void sendNotification(Context context, String notificationDetails) {
        // Create an explicit content Intent that starts the main Activity.
        Intent notificationIntent = new Intent(context, MainActivity.class);

        notificationIntent.putExtra("from_notification", true);

        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(MainActivity.class);

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        // Define the notification settings.
        builder.setSmallIcon(R.mipmap.ic_launcher)
                // In a real app, you may want to use a library like Volley
                // to decode the Bitmap.
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                        R.mipmap.ic_launcher))
                .setColor(Color.RED)
                .setContentTitle("Location update")
                .setContentText(notificationDetails)
                .setContentIntent(notificationPendingIntent);

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true);

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if(mNotificationManager == null)
            return;

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.app_name);
            // Create the channel for the notification
            NotificationChannel mChannel =
                    new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);

            // Set the Notification Channel for the Notification Manager.
            mNotificationManager.createNotificationChannel(mChannel);

            // Channel ID
            builder.setChannelId(CHANNEL_ID);
        }

        // Issue the notification
        mNotificationManager.notify(0, builder.build());
    }


    /**
     * Returns the title for reporting about a list of {@link Location} objects.
     *
     * @param context The {@link Context}.
     */
    static String getLocationResultTitle(Context context, List<Location> locations) {
        String numLocationsReported = context.getResources().getQuantityString(locations.size(), locations.size());
        return numLocationsReported + ": " + DateFormat.getDateTimeInstance().format(new Date());
    }

    /**
     * Returns te text for reporting about a list of  {@link Location} objects.
     *
     * @param locations List of {@link Location}s.
     */
    private static String getLocationResultText(Context context, List<Location> locations) {
        if (locations.isEmpty()) {
            return context.getString(R.string.unknown_location);
        }
        StringBuilder sb = new StringBuilder();
        for (Location location : locations) {
            sb.append("(");
            sb.append(location.getLatitude());
            sb.append(", ");
            sb.append(location.getLongitude());
            sb.append(")");
            sb.append("\n");
        }
        return sb.toString();
    }

    static void setLocationUpdatesResult(Context context, List<Location> locations) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(KEY_LOCATION_UPDATES_RESULT, getLocationResultTitle(context, locations)
                        + "\n" + getLocationResultText(context, locations))
                .apply();
    }

    public static String getLocationUpdatesResult(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_LOCATION_UPDATES_RESULT, "");
    }

    public static boolean hasPermission(Context context, String permission){
        if (permission.equals(Manifest.permission.ACCESS_BACKGROUND_LOCATION) &&
                android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) {
            return true;
        }

        return ActivityCompat.checkSelfPermission(context, permission) ==
                PackageManager.PERMISSION_GRANTED;
    }
}