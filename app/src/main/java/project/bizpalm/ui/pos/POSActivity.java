package project.bizpalm.ui.pos;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import project.bizpalm.R;

public class POSActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pos);
        Toast.makeText(this, "POS UI Mode", Toast.LENGTH_SHORT).show();
    }
}
