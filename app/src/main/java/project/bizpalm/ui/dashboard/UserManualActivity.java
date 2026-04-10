package project.bizpalm.ui.dashboard;

import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import project.bizpalm.R;

public class UserManualActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_manual);

        Toolbar toolbar = findViewById(R.id.toolbarUserManual);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        setupSection(R.id.sectionDashboard, R.string.manual_section_dashboard, R.string.manual_body_dashboard);
        setupSection(R.id.sectionInventory, R.string.manual_section_inventory, R.string.manual_body_inventory);
        setupSection(R.id.sectionSalesAssistant, R.string.manual_section_sales_assistant, R.string.manual_body_sales_assistant);
        setupSection(R.id.sectionStrategicInsights, R.string.manual_section_strategic_insights, R.string.manual_body_strategic_insights);
        setupSection(R.id.sectionPOS, R.string.manual_section_pos, R.string.manual_body_pos);
        setupSection(R.id.sectionSettings, R.string.manual_section_settings, R.string.manual_body_settings);
    }

    private void setupSection(int layoutId, int titleResId, int bodyResId) {
        View section = findViewById(layoutId);
        TextView tvTitle = section.findViewById(R.id.tvSectionTitle);
        TextView tvBody = section.findViewById(R.id.tvSectionBody);

        tvTitle.setText(getString(titleResId));
        tvBody.setText(Html.fromHtml(getString(bodyResId), Html.FROM_HTML_MODE_COMPACT));
    }
}
