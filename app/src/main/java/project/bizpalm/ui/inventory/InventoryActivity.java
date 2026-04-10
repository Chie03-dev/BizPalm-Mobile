package project.bizpalm.ui.inventory;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import project.bizpalm.R;
import project.bizpalm.data.database.AppDatabase;
import project.bizpalm.data.entities.Product;
import project.bizpalm.utils.SessionManager;

public class InventoryActivity extends AppCompatActivity {

    private InventoryAdapter adapter;
    private AppDatabase db;
    private LiveData<List<Product>> currentProducts;
    private EditText etSearch;
    private TextView tvTotalProductCount, tvTotalStockCount;
    private LinearLayout layoutSelectionActions;
    private Button btnSelectAll;
    private ImageButton btnActionMenu;
    private SessionManager sessionManager;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        db = AppDatabase.getDatabase(this);
        sessionManager = new SessionManager(this);

        initViews();
        setupRecyclerView();
        setupSearch();
        setupActionMenu();
        setupSelectionActions();

        observeProducts(db.productDao().getAllProducts());
        
        applyRoleRestrictions();
    }

    private void applyRoleRestrictions() {
        if ("Employee".equals(sessionManager.getRole())) {
            findViewById(R.id.fabAddProduct).setVisibility(View.GONE);
            if (adapter != null) {
                adapter.setReadOnly(true);
            }
        }
    }

    private void initViews() {
        etSearch = findViewById(R.id.etSearchInventory);
        tvTotalProductCount = findViewById(R.id.tvTotalProductCount);
        tvTotalStockCount = findViewById(R.id.tvTotalStockCount);
        layoutSelectionActions = findViewById(R.id.layoutSelectionActions);
        btnSelectAll = findViewById(R.id.btnSelectAll);
        btnActionMenu = findViewById(R.id.btnActionMenu);
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.rvInventoryItems);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InventoryAdapter();
        recyclerView.setAdapter(adapter);

        // Only allow Edit/Delete if NOT an Employee
        if (!"Employee".equals(sessionManager.getRole())) {
            adapter.setOnProductActionListener(new InventoryAdapter.OnProductActionListener() {
                @Override
                public void onEdit(Product product) {
                    Intent intent = new Intent(InventoryActivity.this, AddProductActivity.class);
                    intent.putExtra("PRODUCT_ID", product.id);
                    startActivity(intent);
                }

                @Override
                public void onDelete(Product product) {
                    showDeleteConfirmation(product);
                }
            });
        }
    }

    private void setupSearch() {
        etSearch.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_search, 0, 0, 0);
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    observeProducts(db.productDao().getAllProducts());
                } else {
                    observeProducts(db.productDao().searchProducts(query));
                }
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void setupActionMenu() {
        btnActionMenu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, v);
            popup.getMenu().add(0, 1, 0, "Filter: All");
            popup.getMenu().add(0, 2, 1, "Filter: Low Stock");
            popup.getMenu().add(0, 3, 2, "Filter: Out of Stock");
            
            // Only show Print option for Owners
            if (!"Employee".equals(sessionManager.getRole())) {
                popup.getMenu().add(0, 4, 3, "Select Items to Print");
            }
            
            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case 1: observeProducts(db.productDao().getAllProducts()); break;
                    case 2: observeProducts(db.productDao().getLowStockProducts()); break;
                    case 3: observeProducts(db.productDao().getOutOfStockProducts()); break;
                    case 4: enableSelectionMode(true); break;
                }
                return true;
            });
            popup.show();
        });

        findViewById(R.id.fabAddProduct).setOnClickListener(v -> 
            startActivity(new Intent(this, AddProductActivity.class)));
    }

    private void setupSelectionActions() {
        btnSelectAll.setOnClickListener(v -> {
            if (btnSelectAll.getText().toString().equals("Select All")) {
                adapter.selectAll();
                btnSelectAll.setText("Deselect All");
            } else {
                adapter.deselectAll();
                btnSelectAll.setText("Select All");
            }
        });

        findViewById(R.id.btnPrintSelected).setOnClickListener(v -> {
            List<Product> selected = adapter.getSelectedProducts();
            if (selected.isEmpty()) {
                Toast.makeText(this, "Please select at least one item", Toast.LENGTH_SHORT).show();
            } else {
                generatePdf(selected);
            }
        });

        findViewById(R.id.btnCancelSelection).setOnClickListener(v -> enableSelectionMode(false));
    }

    private void enableSelectionMode(boolean enable) {
        adapter.setSelectionMode(enable);
        layoutSelectionActions.setVisibility(enable ? View.VISIBLE : View.GONE);
        btnSelectAll.setText("Select All");
        
        // Ensure FAB stays hidden if employee, or toggles if owner
        if (!"Employee".equals(sessionManager.getRole())) {
            findViewById(R.id.fabAddProduct).setVisibility(enable ? View.GONE : View.VISIBLE);
        } else {
            findViewById(R.id.fabAddProduct).setVisibility(View.GONE);
        }
    }

    private void showDeleteConfirmation(Product product) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Product")
                .setMessage("Are you sure you want to delete " + product.productName + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    executorService.execute(() -> db.productDao().delete(product));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void observeProducts(LiveData<List<Product>> liveData) {
        if (currentProducts != null) {
            currentProducts.removeObservers(this);
        }
        currentProducts = liveData;
        currentProducts.observe(this, products -> {
            adapter.setProducts(products);
            int uniqueCount = products != null ? products.size() : 0;
            int totalStock = 0;
            if (products != null) {
                for (Product p : products) {
                    totalStock += p.quantity;
                }
            }
            updateTotalDisplay(uniqueCount, totalStock);
        });
    }

    private void updateTotalDisplay(int uniqueCount, int totalStock) {
        if (tvTotalProductCount != null) {
            tvTotalProductCount.setText(String.valueOf(uniqueCount));
        }
        if (tvTotalStockCount != null) {
            tvTotalStockCount.setText(String.valueOf(totalStock));
        }
    }

    private void generatePdf(List<Product> products) {
        executorService.execute(() -> {
            PdfDocument document = new PdfDocument();
            int pageWidth = 595; 
            int pageHeight = 842; 
            int margin = 40;
            int qrSize = 120;
            int padding = 20;
            int cols = 3;
            
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);
            Canvas canvas = page.getCanvas();
            Paint paint = new Paint();
            paint.setTextSize(10f);
            paint.setTextAlign(Paint.Align.CENTER);

            int x = margin;
            int y = margin;
            int count = 0;

            for (Product product : products) {
                if (y + qrSize + 30 > pageHeight - margin) {
                    document.finishPage(page);
                    pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, document.getPages().size() + 1).create();
                    page = document.startPage(pageInfo);
                    canvas = page.getCanvas();
                    x = margin;
                    y = margin;
                }

                try {
                    Bitmap qr = generateQRCode(product.barcode, qrSize);
                    canvas.drawBitmap(qr, x, y, null);
                    canvas.drawText(product.productName, x + (qrSize / 2f), y + qrSize + 15, paint);
                } catch (Exception ignored) {}

                count++;
                if (count % cols == 0) {
                    x = margin;
                    y += qrSize + 50;
                } else {
                    x += qrSize + padding + 20;
                }
            }

            document.finishPage(page);

            String filename = "QR_Codes_" + System.currentTimeMillis() + ".pdf";
            savePdfToStorage(document, filename);
        });
    }

    private void savePdfToStorage(PdfDocument document, String filename) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/BizPalm_Reports");
        }

        Uri uri = getContentResolver().insert(MediaStore.Files.getContentUri("external"), values);

        try {
            if (uri != null) {
                OutputStream out = getContentResolver().openOutputStream(uri);
                document.writeTo(out);
                document.close();
                out.close();
                runOnUiThread(() -> {
                    Toast.makeText(this, "PDF saved to Downloads/BizPalm_Reports", Toast.LENGTH_LONG).show();
                    enableSelectionMode(false);
                });
            }
        } catch (Exception e) {
            runOnUiThread(() -> Toast.makeText(this, "Error saving PDF", Toast.LENGTH_SHORT).show());
        }
    }

    private Bitmap generateQRCode(String text, int size) throws WriterException {
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, size, size);
        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }
        return bmp;
    }
}
