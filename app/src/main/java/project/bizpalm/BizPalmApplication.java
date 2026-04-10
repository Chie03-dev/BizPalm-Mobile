package project.bizpalm;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;

public class BizPalmApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Force the app to always use the light theme, regardless of system settings
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }
}
