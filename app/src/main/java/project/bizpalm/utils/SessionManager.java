package project.bizpalm.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "BizPalmSession";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_ROLE = "role";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_LAST_ACTIVITY = "last_activity";

    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void createLoginSession(int userId, String username, String role) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_ROLE, role);
        editor.putLong(KEY_LAST_ACTIVITY, System.currentTimeMillis());
        editor.apply();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void logoutUser() {
        editor.clear();
        editor.apply();
    }

    public int getUserId() {
        return pref.getInt(KEY_USER_ID, -1);
    }

    public String getUsername() {
        return pref.getString(KEY_USERNAME, "");
    }

    public String getRole() {
        return pref.getString(KEY_ROLE, "Staff");
    }

    public void updateLastActivity() {
        editor.putLong(KEY_LAST_ACTIVITY, System.currentTimeMillis());
        editor.apply();
    }

    public boolean isSessionExpired() {
        long lastActivity = pref.getLong(KEY_LAST_ACTIVITY, 0);
        long currentTime = System.currentTimeMillis();
        // 30 minutes in milliseconds
        return (currentTime - lastActivity) > (30 * 60 * 1000);
    }
}
