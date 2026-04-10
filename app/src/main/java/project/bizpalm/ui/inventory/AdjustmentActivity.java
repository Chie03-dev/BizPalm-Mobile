package project.bizpalm.ui.inventory;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import project.bizpalm.R;

public class AdjustmentActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adjustment);
        Toast.makeText(this, "Adjustment UI Mode", Toast.LENGTH_SHORT).show();
    }
}
