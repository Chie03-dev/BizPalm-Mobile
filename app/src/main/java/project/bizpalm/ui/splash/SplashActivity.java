package project.bizpalm.ui.splash;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import project.bizpalm.R;
import project.bizpalm.ui.auth.LoginActivity;
import project.bizpalm.ui.dashboard.DashboardActivity;
import project.bizpalm.utils.SessionManager;

public class SplashActivity extends AppCompatActivity {

    private boolean isTransitioned = false;
    private static final String PREFS_NAME = "BizPalmSettings";
    private static final String KEY_PIN_LOGIN_ENABLED = "pin_login_enabled";

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

        VideoView videoView = findViewById(R.id.videoView);
        View container = findViewById(R.id.splashContainer);

        // Path to the video file
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.logo);
        videoView.setVideoURI(videoUri);

        // Transition when video finishes
        videoView.setOnCompletionListener(mp -> transitionToNext());

        // Transition on error
        videoView.setOnErrorListener((mp, what, extra) -> {
            transitionToNext();
            return true;
        });

        // Make it skippable: transition when clicking anywhere
        View.OnClickListener skipListener = v -> transitionToNext();
        container.setOnClickListener(skipListener);
        videoView.setOnClickListener(skipListener);

        videoView.start();
    }

    private void transitionToNext() {
        if (!isTransitioned) {
            isTransitioned = true;
            
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            boolean pinLoginEnabled = prefs.getBoolean(KEY_PIN_LOGIN_ENABLED, true);
            SessionManager sessionManager = new SessionManager(this);

            Intent intent;
            if (!pinLoginEnabled && sessionManager.isLoggedIn()) {
                // If PIN Login is disabled and user was previously logged in, go straight to Dashboard
                intent = new Intent(SplashActivity.this, DashboardActivity.class);
            } else {
                // Otherwise, always go to LoginActivity (it will handle session auto-login if appropriate)
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }

            startActivity(intent);
            // Apply fade transition
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }
    }
}
