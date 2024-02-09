package com.secguard.securityguardmanagement;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class MyApplication extends Application {

    private String guardId;

    @Override
    public void onCreate() {
        super.onCreate();

        // Notification channel creation
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("channel_id", "Location Service", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    // Setter for guard_id
    public void setGuardId(String guardId) {
        this.guardId = guardId;
    }

    // Getter for guard_id
    public String getGuardId() {
        return guardId;
    }
}
