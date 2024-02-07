package com.secguard.securityguardmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class splash_activity extends AppCompatActivity {

    private static final int SPLASH_TIME_OUT = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Use a Handler to delay the transition to the main activity
        new Handler().postDelayed(() -> {
            // Start your app's main activity
            Intent intent = new Intent(splash_activity.this, MainActivity.class);
            startActivity(intent);

            // Close this activity
            finish();
        }, SPLASH_TIME_OUT);
    }
}
