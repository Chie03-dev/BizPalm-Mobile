package project.bizpalm.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
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

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etPin;
    private TextInputLayout tilPin;
    private Button btnRegister;
    private TextView tvLoginLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        etUsername = findViewById(R.id.etUsername);
        etPin = findViewById(R.id.etPin);
        tilPin = findViewById(R.id.tilPin);
        btnRegister = findViewById(R.id.btnRegister);
        tvLoginLink = findViewById(R.id.tvLoginLink);
    }

    private void setupClickListeners() {
        btnRegister.setOnClickListener(v -> handleRegistration());
        tvLoginLink.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        tilPin.setEndIconOnClickListener(v -> {
            int selection = etPin.getSelectionEnd();
            if (etPin.getTransformationMethod() instanceof PasswordTransformationMethod) {
                etPin.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                etPin.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            etPin.setSelection(selection);
        });
    }

    private void handleRegistration() {
        String username = etUsername.getText().toString().trim();
        String pin = etPin.getText().toString().trim();
        final String role = "Owner"; // Always register as Owner

        if (username.isEmpty() || pin.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (pin.length() < 4 || pin.length() > 6) {
            Toast.makeText(this, "PIN must be 4-6 digits", Toast.LENGTH_SHORT).show();
            return;
        }

        AppDatabase db = AppDatabase.getDatabase(this);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            User existingUser = db.userDao().getUserByUsername(username);
            if (existingUser != null) {
                runOnUiThread(() -> Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show());
                return;
            }

            User newUser = new User(username, pin, role);
            db.userDao().insert(newUser);

            runOnUiThread(() -> {
                Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}
