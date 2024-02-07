package com.secguard.securityguardmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button allowButton = findViewById(R.id.allowButton);
        Button stopButton = findViewById(R.id.stopButton);

        allowButton.setOnClickListener(view -> requestLocationPermissionAndStartService());
        stopButton.setOnClickListener(view -> stopLocationService());
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
        startService(new Intent(this, LocationUpdateService.class));
    }

    private void stopLocationService() {
        stopService(new Intent(this, LocationUpdateService.class));
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
