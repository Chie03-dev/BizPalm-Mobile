package project.bizpalm.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import project.bizpalm.R;
import project.bizpalm.data.database.AppDatabase;
import project.bizpalm.data.entities.User;
import project.bizpalm.ui.dashboard.DashboardActivity;
import project.bizpalm.utils.SessionManager;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etPin;
    private TextInputLayout tilPin;
    private Button btnLogin;
    private TextView tvRegister;
    
    // Eye Animation Views
    private ImageView ivLeftPupil, ivRightPupil;
    private ImageView ivLeftEyeWhite, ivRightEyeWhite;
    private ImageView ivLeftEyeClosed, ivRightEyeClosed;
    private View llEyesContainer;

    private final Handler animationHandler = new Handler(Looper.getMainLooper());
    private boolean isBlinking = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        SessionManager sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
            return;
        }
        
        setContentView(R.layout.activity_login);

        initViews();
        setupEyeAnimation();
        setupClickListeners();
        setupDynamicLoginButton();
        startPeriodicAnimations();
    }

    private void initViews() {
        etUsername = findViewById(R.id.etUsername);
        etPin = findViewById(R.id.etPin);
        tilPin = findViewById(R.id.tilPin);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);

        ivLeftPupil = findViewById(R.id.ivLeftEyePupil);
        ivRightPupil = findViewById(R.id.ivRightEyePupil);
        ivLeftEyeWhite = findViewById(R.id.ivLeftEyeWhite);
        ivRightEyeWhite = findViewById(R.id.ivRightEyeWhite);
        ivLeftEyeClosed = findViewById(R.id.ivLeftEyeClosed);
        ivRightEyeClosed = findViewById(R.id.ivRightEyeClosed);
        llEyesContainer = findViewById(R.id.llEyesContainer);
        
        // Initial state
        btnLogin.setText("EMPLOYEE LOGIN");
    }

    private void setupDynamicLoginButton() {
        TextWatcher loginWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        etUsername.addTextChangedListener(loginWatcher);
        etPin.addTextChangedListener(loginWatcher);
    }

    private void updateButtonState() {
        String user = etUsername.getText().toString().trim();
        String pin = etPin.getText().toString().trim();

        String currentText = btnLogin.getText().toString();
        String nextText = (user.isEmpty() && pin.isEmpty()) ? "EMPLOYEE LOGIN" : "LOGIN AS OWNER";

        if (!currentText.equals(nextText)) {
            btnLogin.setText(nextText);
            // Surprise reaction when switching to Owner Login
            if (nextText.equals("LOGIN AS OWNER")) {
                reactToOwnerLogin();
            }
        }
    }

    private void reactToOwnerLogin() {
        // Eyes widen and look down at the button
        ivLeftEyeWhite.animate().scaleX(1.1f).scaleY(1.1f).setDuration(200).withEndAction(() -> 
                ivLeftEyeWhite.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200)).start();
        ivRightEyeWhite.animate().scaleX(1.1f).scaleY(1.1f).setDuration(200).withEndAction(() -> 
                ivRightEyeWhite.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200)).start();
        
        ivLeftPupil.animate().translationY(10f).setDuration(300).start();
        ivRightPupil.animate().translationY(10f).setDuration(300).start();
    }

    private void setupEyeAnimation() {
        // Follow username typing with smooth movement
        etUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                float moveX = Math.min(s.length() * 1.5f, 20f) - 10f;
                ivLeftPupil.animate()
                        .translationX(moveX)
                        .translationY(5f)
                        .setDuration(100)
                        .setInterpolator(new AccelerateDecelerateInterpolator())
                        .start();
                ivRightPupil.animate()
                        .translationX(moveX)
                        .translationY(5f)
                        .setDuration(100)
                        .setInterpolator(new AccelerateDecelerateInterpolator())
                        .start();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Close/Peek eyes when entering PIN
        etPin.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                // Look down towards PIN field before closing
                ivLeftPupil.animate().translationY(15f).setDuration(200).withEndAction(this::updateEyeState).start();
                ivRightPupil.animate().translationY(15f).setDuration(200).start();
            } else {
                updateEyeState();
            }
        });

        // Handle password toggle click
        tilPin.setEndIconOnClickListener(v -> {
            int selection = etPin.getSelectionEnd();
            if (etPin.getTransformationMethod() instanceof PasswordTransformationMethod) {
                etPin.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                etPin.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            etPin.setSelection(selection);
            updateEyeState();
        });
    }

    private void updateEyeState() {
        if (etPin.hasFocus()) {
            boolean isRevealed = !(etPin.getTransformationMethod() instanceof PasswordTransformationMethod);
            if (isRevealed) {
                // Peeking: Left eye closed, Right eye open (looking curious)
                setEyeIndividualState(true, false);
                ivRightPupil.setTranslationY(8f);
                ivRightPupil.setTranslationX(5f);
            } else {
                // Both closed
                setEyeIndividualState(true, true);
            }
        } else {
            // Both open
            setEyeIndividualState(false, false);
            if (!etUsername.hasFocus()) {
                // Return to center if not typing username
                ivLeftPupil.animate().translationX(0).translationY(0).setDuration(300).start();
                ivRightPupil.animate().translationX(0).translationY(0).setDuration(300).start();
            }
        }
    }

    private void setEyeIndividualState(boolean leftClosed, boolean rightClosed) {
        ivLeftEyeWhite.setVisibility(leftClosed ? View.GONE : View.VISIBLE);
        ivLeftPupil.setVisibility(leftClosed ? View.GONE : View.VISIBLE);
        ivLeftEyeClosed.setVisibility(leftClosed ? View.VISIBLE : View.GONE);

        ivRightEyeWhite.setVisibility(rightClosed ? View.GONE : View.VISIBLE);
        ivRightPupil.setVisibility(rightClosed ? View.GONE : View.VISIBLE);
        ivRightEyeClosed.setVisibility(rightClosed ? View.VISIBLE : View.GONE);
    }

    private void startPeriodicAnimations() {
        // Periodic Blinking
        animationHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!etPin.hasFocus() && !isBlinking) {
                    performBlink();
                }
                animationHandler.postDelayed(this, 3000 + (long) (Math.random() * 4000));
            }
        }, 3000);

        // Idle Eye Movements (Look around occasionally)
        animationHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!etUsername.hasFocus() && !etPin.hasFocus() && !isBlinking) {
                    float randomX = (float) (Math.random() * 16 - 8);
                    float randomY = (float) (Math.random() * 10 - 5);
                    ivLeftPupil.animate().translationX(randomX).translationY(randomY).setDuration(600).start();
                    ivRightPupil.animate().translationX(randomX).translationY(randomY).setDuration(600).start();
                }
                animationHandler.postDelayed(this, 5000 + (long) (Math.random() * 5000));
            }
        }, 5000);
    }

    private void performBlink() {
        isBlinking = true;
        setEyeIndividualState(true, true);
        animationHandler.postDelayed(() -> {
            updateEyeState();
            isBlinking = false;
        }, 120);
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> handleLogin());
        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    private void handleLogin() {
        String username = etUsername.getText().toString().trim();
        String pin = etPin.getText().toString().trim();

        if (username.isEmpty() && pin.isEmpty()) {
            // Employee Login: Bypass account check
            new SessionManager(this).createLoginSession(-1, "Employee", "Employee");
            Intent intent = new Intent(this, DashboardActivity.class);
            intent.putExtra("is_employee_login", true);
            startActivity(intent);
            finish();
            return;
        }

        // Owner Login: Must have both fields filled
        if (username.isEmpty() || pin.isEmpty()) {
            shakeEyes();
            Toast.makeText(this, "Please enter both username and PIN for Owner login", Toast.LENGTH_SHORT).show();
            return;
        }

        AppDatabase db = AppDatabase.getDatabase(this);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            User user = db.userDao().login(username, pin);
            if (user != null) {
                runOnUiThread(() -> {
                    new SessionManager(this).createLoginSession(0, user.username, user.role);
                    Toast.makeText(this, "Welcome, " + user.username + " (" + user.role + ")", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, DashboardActivity.class));
                    finish();
                });
            } else {
                runOnUiThread(() -> {
                    shakeEyes();
                    Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void shakeEyes() {
        llEyesContainer.animate()
                .translationX(20f)
                .setDuration(50)
                .withEndAction(() -> llEyesContainer.animate()
                        .translationX(-20f)
                        .setDuration(50)
                        .withEndAction(() -> llEyesContainer.animate()
                                .translationX(0)
                                .setDuration(50)
                                .start())
                        .start())
                .start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        animationHandler.removeCallbacksAndMessages(null);
    }
}
