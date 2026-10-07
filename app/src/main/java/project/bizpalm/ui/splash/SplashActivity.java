package project.bizpalm.ui.splash;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import project.bizpalm.R;
import project.bizpalm.ui.auth.LoginActivity;
import project.bizpalm.ui.dashboard.DashboardActivity;
import project.bizpalm.utils.SessionManager;

public class SplashActivity extends AppCompatActivity {

    private boolean isTransitioned = false;
    private static final String PREFS_NAME = "BizPalmSettings";
    private static final String KEY_PIN_LOGIN_ENABLED = "pin_login_enabled";
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable transitionRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Hide status bar and navigation bar for a true fullscreen splash
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        
        setContentView(R.layout.activity_splash);

        View container = findViewById(R.id.splashContainer);

        transitionRunnable = this::transitionToNext;

        // Transition after 1.5 seconds or on click
        handler.postDelayed(transitionRunnable, 1500);
        container.setOnClickListener(v -> {
            handler.removeCallbacks(transitionRunnable);
            transitionToNext();
        });
    }

    private void transitionToNext() {
        if (!isTransitioned) {
            isTransitioned = true;
            
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            boolean pinLoginEnabled = prefs.getBoolean(KEY_PIN_LOGIN_ENABLED, true);
            SessionManager sessionManager = new SessionManager(this);

            Intent intent;
            if (!pinLoginEnabled && sessionManager.isLoggedIn()) {
                intent = new Intent(SplashActivity.this, DashboardActivity.class);
            } else {
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }

            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (transitionRunnable != null) {
            handler.removeCallbacks(transitionRunnable);
        }
    }
}
