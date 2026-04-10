package project.bizpalm.ui.dashboard;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import project.bizpalm.R;
import project.bizpalm.ui.analytics.SalesAssistantActivity;
import project.bizpalm.ui.auth.LoginActivity;
import project.bizpalm.ui.inventory.InventoryActivity;
import project.bizpalm.ui.inventory.ScanProductActivity;
import project.bizpalm.ui.transactions.TransactionHistoryActivity;
import project.bizpalm.utils.SessionManager;

public class DashboardActivity extends AppCompatActivity {

    private static final int NOTIFICATION_PERMISSION_CODE = 1002;
    private CardView cardSales, cardScan, cardTransactions, cardNearby, cardInventory, cardSettings, cardNotifications;
    private Button btnSignOut;
    private TextView tvUnreadCount, tvNoNotif, tvUsername, tvUserRole;
    private NotificationsViewModel notificationsViewModel;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        sessionManager = new SessionManager(this);

        cardSales = findViewById(R.id.cardSalesAssistant);
        cardScan = findViewById(R.id.cardScanProduct);
        cardTransactions = findViewById(R.id.cardTransactions);
        cardNearby = findViewById(R.id.cardNearbyStores);
        cardInventory = findViewById(R.id.cardInventory);
        cardSettings = findViewById(R.id.cardSettings);
        cardNotifications = findViewById(R.id.cardNotifications);
        btnSignOut = findViewById(R.id.btnSignOut);
        
        tvUnreadCount = findViewById(R.id.tvUnreadCount);
        tvNoNotif = findViewById(R.id.tvNoNotif);
        tvUsername = findViewById(R.id.tvUsername);
        tvUserRole = findViewById(R.id.tvUserRole);

        String role = sessionManager.getRole();
        if (tvUsername != null) {
            tvUsername.setText("Hello, " + sessionManager.getUsername() + "!");
        }
        if (tvUserRole != null) {
            tvUserRole.setText(role + " Account");
        }

        // Apply Role Restrictions
        if ("Employee".equals(role)) {
            cardSettings.setVisibility(View.GONE);
            cardSales.setVisibility(View.GONE);
        }

        notificationsViewModel = new ViewModelProvider(this).get(NotificationsViewModel.class);
        observeNotifications();
        requestNotificationPermission();

        cardSales.setOnClickListener(v -> startActivity(new Intent(this, SalesAssistantActivity.class)));
        
        cardScan.setOnClickListener(v -> startActivity(new Intent(this, ScanProductActivity.class)));

        cardTransactions.setOnClickListener(v -> startActivity(new Intent(this, TransactionHistoryActivity.class)));
        
        cardNearby.setOnClickListener(v -> startActivity(new Intent(this, NearbyStoresActivity.class)));
        
        cardInventory.setOnClickListener(v -> startActivity(new Intent(this, InventoryActivity.class)));

        cardSettings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));

        cardNotifications.setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, 0)
                    .replace(R.id.fragmentContainer, new NotificationsFragment())
                    .addToBackStack(null)
                    .commit();
        });

        btnSignOut.setOnClickListener(v -> {
            sessionManager.logoutUser();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_CODE);
            }
        }
    }
    
    private void observeNotifications() {
        notificationsViewModel.getUnreadCount().observe(this, count -> {
            if (count != null && count > 0) {
                tvUnreadCount.setText(String.valueOf(count));
                tvUnreadCount.setVisibility(View.VISIBLE);
                tvNoNotif.setText(String.format("You have %d new alerts.", count));
            } else {
                tvUnreadCount.setVisibility(View.GONE);
                tvNoNotif.setText("No new notifications.");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        sessionManager.updateLastActivity();
    }
}
