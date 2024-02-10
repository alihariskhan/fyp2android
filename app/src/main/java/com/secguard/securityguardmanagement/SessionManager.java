package com.secguard.securityguardmanagement;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "session_pref";
    private static final String KEY_GUARD_ID = "guard_id";

    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void setGuardId(String guardId) {
        editor.putString(KEY_GUARD_ID, guardId);
        editor.apply();
    }

    public String getGuardId() {
        return sharedPreferences.getString(KEY_GUARD_ID, null);
    }

    public void clearSession() {
        editor.clear();
        editor.apply();
    }
}
