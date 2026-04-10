package project.bizpalm.ui.products;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import project.bizpalm.R;

public class AddEditProductActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_product);
        Toast.makeText(this, "Legacy Add/Edit UI Mode", Toast.LENGTH_SHORT).show();
    }
}
