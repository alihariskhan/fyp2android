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
        startService(new Intent(this, LocationUpdateService.class));
    }

    private void stopLocationService() {
        stopService(new Intent(this, LocationUpdateService.class));
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
