package com.secguard.securityguardmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 123;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        if (sessionManager.getGuardId() == null) {
            Intent intent = new Intent(MainActivity.this, login_activity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        Button allowButton = findViewById(R.id.allowButton);
        Button stopButton = findViewById(R.id.stopButton);
        Button logoutButton = findViewById(R.id.logoutButton);

        allowButton.setOnClickListener(view -> requestLocationPermissionAndStartService());
        stopButton.setOnClickListener(view -> stopLocationService());
        logoutButton.setOnClickListener(view -> logout());
    }

    private void requestLocationPermissionAndStartService() {
        // Check for permission
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            // Request permission
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
        } else {
            // Permission already granted, start service
            startLocationService();
        }
    }

    private void startLocationService() {
        String currentDateTime = getCurrentDateTime();
        new SendDateTimeTask().execute(currentDateTime, "start");
        startService(new Intent(this, LocationUpdateService.class));
    }

    private void stopLocationService() {
        String currentDateTime = getCurrentDateTime();
        new SendDateTimeTask().execute(currentDateTime, "stop");
        stopService(new Intent(this, LocationUpdateService.class));
    }

    private String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    private class SendDateTimeTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            String dateTime = params[0];
            String action = params[1];
            sendDateTimeToServer(dateTime, action);
            return null;
        }

        private void sendDateTimeToServer(String dateTime, String action) {
            try {
                URL url = new URL("https://192.168.76.199/SecurityGuardManagement/details.php");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setDoOutput(true);

                String guardId = sessionManager.getGuardId();

                // Create a JSON object to hold the data
                JSONObject jsonParams = new JSONObject();
                jsonParams.put("guardId", guardId);
                jsonParams.put("datetime", dateTime);
                jsonParams.put("action", action);

                // Convert the JSON object to a string
                String postData = jsonParams.toString();

                // Send data to the server
                OutputStream os = urlConnection.getOutputStream();
                os.write(postData.getBytes());
                os.flush();
                os.close();

                int responseCode = urlConnection.getResponseCode();
                Log.d("HTTP Response", "Response Code: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Successfully sent data to the server
                    Log.d("HTTP Response", "Data sent successfully");
                } else {
                    // Handle the error
                    Log.e("HTTP Error", "HTTP error code: " + responseCode);
                }

                urlConnection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void logout() {
        // Clear session information using SessionManager
        stopService(new Intent(this, LocationUpdateService.class));
        sessionManager.clearSession();

        // Navigate to the login screen
        Intent intent = new Intent(MainActivity.this, login_activity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        // Override the back button behavior if you don't want to go back to MainActivity after login
        moveTaskToBack(true);
    }

    // Handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start service
                startLocationService();
            } else {
                // Permission denied, handle accordingly
                // You might want to show a message to the user
                // or disable the "Allow" button
            }
        }
    }
}
