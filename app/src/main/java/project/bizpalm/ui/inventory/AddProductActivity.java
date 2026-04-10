package project.bizpalm.ui.inventory;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import project.bizpalm.R;
import project.bizpalm.data.database.AppDatabase;
import project.bizpalm.data.entities.Notification;
import project.bizpalm.data.entities.Product;

public class AddProductActivity extends AppCompatActivity {

    private TextInputEditText etName, etQuantity, etPrice, etUnitCost, etExpiryDate, etBarcodeDisplay;
    private ImageView ivQRCode, ivProductImage;
    private TextView tvHiddenId;
    private ImageButton btnScanExisting;
    private FloatingActionButton fabEditImage;
    private MaterialButton btnGenerateQR, btnDone, btnDownloadQR;
    private AppDatabase db;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private int editProductId = -1;
    private String customBarcode = null;
    private Bitmap currentQRBitmap = null;
    private Long selectedExpiryDate = null;
    private String currentImagePath = null;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    private final ActivityResultLauncher<Intent> scanLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String barcode = result.getData().getStringExtra(ScanProductActivity.RESULT_BARCODE);
                    if (barcode != null) {
                        customBarcode = barcode;
                        etBarcodeDisplay.setText(barcode);
                        checkDuplicateBarcode(barcode);
                    }
                }
            }
    );

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImage = result.getData().getData();
                    try {
                        saveImageToInternalStorage(selectedImage);
                    } catch (IOException e) {
                        Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private final ActivityResultLauncher<Uri> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            success -> {
                if (success) {
                    displayProductImage(currentImagePath);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        db = AppDatabase.getDatabase(this);

        etName = findViewById(R.id.etProductName);
        etQuantity = findViewById(R.id.etQuantity);
        etPrice = findViewById(R.id.etPrice);
        etUnitCost = findViewById(R.id.etUnitCost);
        etExpiryDate = findViewById(R.id.etExpiryDate);
        etBarcodeDisplay = findViewById(R.id.etBarcodeDisplay);
        btnScanExisting = findViewById(R.id.btnScanExisting);
        ivQRCode = findViewById(R.id.ivQRCode);
        ivProductImage = findViewById(R.id.ivProductImage);
        fabEditImage = findViewById(R.id.fabEditImage);
        tvHiddenId = findViewById(R.id.tvHiddenId);
        btnGenerateQR = findViewById(R.id.btnGenerateQR);
        btnDone = findViewById(R.id.btnDone);
        btnDownloadQR = findViewById(R.id.btnDownloadQR);

        etExpiryDate.setOnClickListener(v -> showDatePicker());
        
        btnScanExisting.setOnClickListener(v -> {
            Intent intent = new Intent(this, ScanProductActivity.class);
            intent.putExtra(ScanProductActivity.EXTRA_IS_PICKER, true);
            scanLauncher.launch(intent);
        });

        fabEditImage.setOnClickListener(v -> showImageSourceDialog());

        if (getIntent().hasExtra("PRODUCT_ID")) {
            editProductId = getIntent().getIntExtra("PRODUCT_ID", -1);
            loadProductForEdit();
            btnGenerateQR.setText("Update Product");
        }

        btnGenerateQR.setOnClickListener(v -> saveOrUpdateProduct());
        btnDone.setOnClickListener(v -> finish());
        btnDownloadQR.setOnClickListener(v -> saveQRToGallery());
    }

    private void checkDuplicateBarcode(String barcode) {
        executorService.execute(() -> {
            Product existing = db.productDao().getProductByBarcode(barcode);
            if (existing != null && existing.id != editProductId) {
                runOnUiThread(() -> {
                    new AlertDialog.Builder(this)
                            .setTitle("Duplicate Barcode Detected")
                            .setMessage("The barcode '" + barcode + "' is already assigned to '" + existing.productName + "'. Do you want to edit that product instead?")
                            .setPositiveButton("Edit Existing", (dialog, which) -> {
                                editProductId = existing.id;
                                loadProductForEdit();
                            })
                            .setNegativeButton("Change Barcode", (dialog, which) -> {
                                etBarcodeDisplay.setText("");
                                customBarcode = null;
                            })
                            .show();
                });
            }
        });
    }

    private void showImageSourceDialog() {
        String[] options = {"Take Photo", "Choose from Gallery"};
        new AlertDialog.Builder(this)
                .setTitle("Select Product Image")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        launchCamera();
                    } else {
                        launchGallery();
                    }
                }).show();
    }

    private void launchCamera() {
        try {
            File photoFile = createImageFile();
            Uri photoURI = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
            currentImagePath = photoFile.getAbsolutePath();
            cameraLauncher.launch(photoURI);
        } catch (IOException ex) {
            Toast.makeText(this, "Error creating file", Toast.LENGTH_SHORT).show();
        }
    }

    private void launchGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void saveImageToInternalStorage(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        File photoFile = createImageFile();
        currentImagePath = photoFile.getAbsolutePath();
        FileOutputStream outputStream = new FileOutputStream(photoFile);
        
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        
        inputStream.close();
        outputStream.close();
        displayProductImage(currentImagePath);
    }

    private void displayProductImage(String path) {
        if (path != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            ivProductImage.setImageBitmap(bitmap);
            ivProductImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            ivProductImage.setImageTintList(null);
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        if (selectedExpiryDate != null) {
            calendar.setTimeInMillis(selectedExpiryDate);
        }
        
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            selectedExpiryDate = calendar.getTimeInMillis();
            etExpiryDate.setText(dateFormat.format(new Date(selectedExpiryDate)));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void loadProductForEdit() {
        executorService.execute(() -> {
            Product product = db.productDao().getProductById(editProductId);
            if (product != null) {
                customBarcode = product.barcode;
                runOnUiThread(() -> {
                    etName.setText(product.productName);
                    etQuantity.setText(String.valueOf(product.quantity));
                    etPrice.setText(String.valueOf(product.price));
                    etUnitCost.setText(String.valueOf(product.unitCost));
                    etBarcodeDisplay.setText(product.barcode);
                    if (product.expiryDate != null) {
                        selectedExpiryDate = product.expiryDate;
                        etExpiryDate.setText(dateFormat.format(new Date(selectedExpiryDate)));
                    }
                    if (product.imageUri != null) {
                        currentImagePath = product.imageUri;
                        displayProductImage(currentImagePath);
                    }
                    
                    try {
                        currentQRBitmap = generateQRCode(product.barcode);
                        ivQRCode.setImageBitmap(currentQRBitmap);
                        ivQRCode.setVisibility(View.VISIBLE);
                        btnDownloadQR.setVisibility(View.VISIBLE);
                        tvHiddenId.setText("ID: " + product.barcode);
                        tvHiddenId.setVisibility(View.VISIBLE);
                    } catch (WriterException ignored) {}
                });
            }
        });
    }

    private void saveOrUpdateProduct() {
        String name = etName.getText().toString().trim();
        String qtyStr = etQuantity.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String costStr = etUnitCost.getText().toString().trim();

        if (name.isEmpty() || qtyStr.isEmpty() || priceStr.isEmpty() || costStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int qty = Integer.parseInt(qtyStr);
        double price = Double.parseDouble(priceStr);
        double cost = Double.parseDouble(costStr);
        
        String barcode = customBarcode;
        if (barcode == null) {
            barcode = "BIZ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }

        final String finalBarcode = barcode;
        executorService.execute(() -> {
            // Check for duplicate barcode again before saving
            Product existingByBarcode = db.productDao().getProductByBarcode(finalBarcode);
            if (existingByBarcode != null && existingByBarcode.id != editProductId) {
                runOnUiThread(() -> Toast.makeText(this, "Error: Barcode already exists for another product", Toast.LENGTH_LONG).show());
                return;
            }

            // Check for duplicate name (optional validation)
            List<Product> allProducts = db.productDao().getAllProductsSync();
            for (Product p : allProducts) {
                if (p.productName.equalsIgnoreCase(name) && p.id != editProductId) {
                    runOnUiThread(() -> Toast.makeText(this, "Warning: A product with this name already exists", Toast.LENGTH_SHORT).show());
                    // We don't block the save for names, just warn.
                }
            }

            if (editProductId == -1) {
                Product newProduct = new Product(finalBarcode, name, price, cost, qty, false, selectedExpiryDate);
                newProduct.imageUri = currentImagePath;
                long id = db.productDao().insert(newProduct);
                checkExpiryAndNotify(name, selectedExpiryDate, (int)id);
            } else {
                Product product = db.productDao().getProductById(editProductId);
                if (product != null) {
                    product.productName = name;
                    product.quantity = qty;
                    product.price = price;
                    product.unitCost = cost;
                    product.barcode = finalBarcode;
                    product.expiryDate = selectedExpiryDate;
                    product.imageUri = currentImagePath;
                    db.productDao().update(product);
                    checkExpiryAndNotify(name, selectedExpiryDate, editProductId);
                }
            }

            try {
                currentQRBitmap = generateQRCode(finalBarcode);
                runOnUiThread(() -> {
                    ivQRCode.setImageBitmap(currentQRBitmap);
                    ivQRCode.setVisibility(View.VISIBLE);
                    btnDownloadQR.setVisibility(View.VISIBLE);
                    tvHiddenId.setText("ID: " + finalBarcode);
                    tvHiddenId.setVisibility(View.VISIBLE);
                    btnDone.setVisibility(View.VISIBLE);
                    btnGenerateQR.setVisibility(View.GONE);
                    Toast.makeText(this, "Product Saved Successfully", Toast.LENGTH_SHORT).show();
                });
            } catch (WriterException ignored) {}
        });
    }

    private void checkExpiryAndNotify(String productName, Long expiryDate, int productId) {
        if (expiryDate == null) return;
        long currentTime = System.currentTimeMillis();
        long diff = expiryDate - currentTime;
        long daysRemaining = diff / (24 * 60 * 60 * 1000);

        if (daysRemaining <= 7 && daysRemaining > 0) {
            String title = "Expiration Warning: " + productName;
            String message = String.format(Locale.getDefault(), "%s is expiring in %d days.", productName, daysRemaining);
            db.notificationDao().insert(new Notification("EXPIRY_NEAR", title, message, currentTime, false, productId));
        }
    }

    private void saveQRToGallery() {
        if (currentQRBitmap == null) return;
        String productName = etName.getText().toString().trim();
        if (productName.isEmpty()) productName = "Product";

        Bitmap combinedBitmap = Bitmap.createBitmap(currentQRBitmap.getWidth(), currentQRBitmap.getHeight() + 80, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(combinedBitmap);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(currentQRBitmap, 0, 0, null);

        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(36f);
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(productName, combinedBitmap.getWidth() / 2f, currentQRBitmap.getHeight() + 50, paint);

        String filename = "QR_" + productName.replaceAll("\\s+", "_") + "_" + System.currentTimeMillis() + ".png";
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/BizPalm_QR");
        }

        Uri uri = getContentResolver().insert(MediaStore.Files.getContentUri("external"), values);
        if (uri != null) {
            try (OutputStream out = getContentResolver().openOutputStream(uri)) {
                combinedBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                Toast.makeText(this, "QR Code saved to Downloads/BizPalm_QR", Toast.LENGTH_LONG).show();
            } catch (Exception ignored) {}
        }
    }

    private Bitmap generateQRCode(String text) throws WriterException {
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 512, 512);
        Bitmap bmp = Bitmap.createBitmap(512, 512, Bitmap.Config.RGB_565);
        for (int x = 0; x < 512; x++) {
            for (int y = 0; y < 512; y++) {
                bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }
        return bmp;
    }
}
