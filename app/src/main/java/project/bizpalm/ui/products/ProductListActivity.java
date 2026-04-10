package project.bizpalm.ui.products;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import project.bizpalm.R;
import project.bizpalm.ui.inventory.AddProductActivity;

public class ProductListActivity extends AppCompatActivity {

    private ProductListViewModel viewModel;
    private ProductAdapter adapter;
    private boolean pickMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        pickMode = getIntent().getBooleanExtra("PICK_MODE", false);

        RecyclerView recyclerView = findViewById(R.id.rvProducts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProductAdapter();
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(ProductListViewModel.class);
        viewModel.getAllProducts().observe(this, products -> {
            adapter.setProducts(products);
        });

        TextInputEditText etSearch = findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.searchProducts(s.toString()).observe(ProductListActivity.this, products -> {
                    adapter.setProducts(products);
                });
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        FloatingActionButton fab = findViewById(R.id.fabAddProduct);
        if (pickMode) {
            fab.hide();
        }
        
        fab.setOnClickListener(v -> {
            startActivity(new Intent(ProductListActivity.this, AddProductActivity.class));
        });

        adapter.setOnItemClickListener(product -> {
            if (pickMode) {
                Intent data = new Intent();
                data.putExtra("PRODUCT_ID", product.id);
                setResult(RESULT_OK, data);
                finish();
            } else {
                // For now, we redirect to AddProductActivity or just show a message
                // In a full app, this would be EditProduct
            }
        });
    }
}
