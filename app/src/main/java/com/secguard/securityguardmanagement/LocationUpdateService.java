package com.secguard.securityguardmanagement;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class LocationUpdateService extends Service {

    private static final String TAG = "LocationUpdateService";
    private static final int NOTIFICATION_ID = 123;
    private static final String LOCATION_UPDATE_ACTION = "com.shah.securityguard.LOCATION_UPDATE_ACTION";
    private FusedLocationProviderClient fusedLocationClient;
    private BroadcastReceiver locationUpdateReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        registerLocationUpdateReceiver();
    }

    @SuppressLint("ForegroundServiceType")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIFICATION_ID, createNotification());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        startLocationUpdates();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterLocationUpdateReceiver();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@Nullable LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }

                Location location = locationResult.getLastLocation();

                if (location != null) {
                    sendLocationBroadcast(location.getLatitude(), location.getLongitude());
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
        );
    }

    private void sendLocationBroadcast(double latitude, double longitude) {
        Intent intent = new Intent(LOCATION_UPDATE_ACTION);
        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);
        sendBroadcast(intent);
    }

    private void registerLocationUpdateReceiver() {
        locationUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction() != null && intent.getAction().equals(LOCATION_UPDATE_ACTION)) {
                    double latitude = intent.getDoubleExtra("latitude", 0);
                    double longitude = intent.getDoubleExtra("longitude", 0);
                    handleLocationData(latitude, longitude);
                }
            }
        };

        IntentFilter filter = new IntentFilter(LOCATION_UPDATE_ACTION);
        registerReceiver(locationUpdateReceiver, filter);
    }

    private void unregisterLocationUpdateReceiver() {
        if (locationUpdateReceiver != null) {
            unregisterReceiver(locationUpdateReceiver);
        }
    }

    private void handleLocationData(double latitude, double longitude) {
        String guardId = "8001";
        JSONObject locationData = new JSONObject();

        try {
            locationData.put("guardId", guardId);
            locationData.put("latitude", latitude);
            locationData.put("longitude", longitude);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "Error creating JSON object: " + e.getMessage());
        }

        sendLocationToServer(locationData);
    }

    private static class SendLocationTask extends AsyncTask<Void, Void, Void> {
        private final JSONObject locationData;

        SendLocationTask(JSONObject locationData) {
            this.locationData = locationData;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                String serverUrl = "http://192.168.0.221/SecurityGuardManagement/gps_tracking.php";

                URL url = new URL(serverUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setDoOutput(true);

                OutputStream outputStream = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
                writer.write(locationData.toString());
                writer.flush();
                writer.close();
                outputStream.close();

                int responseCode = urlConnection.getResponseCode();
                Log.d(TAG, "Server response code: " + responseCode);
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.d(TAG, "Location data sent successfully");
                } else {
                    Log.e(TAG, "Failed to send location data. Response Code: " + responseCode);
                }

                urlConnection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "Error sending location data: " + e.getMessage());
            }
            return null;
        }
    }

    private static void sendLocationToServer(JSONObject locationData) {
        new SendLocationTask(locationData).execute();
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MyApplication.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, "channel_id")
                .setContentTitle("Location Update Service")
                .setContentText("Service is running in the background")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build();
    }
}
