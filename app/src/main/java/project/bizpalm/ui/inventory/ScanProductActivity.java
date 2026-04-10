package project.bizpalm.ui.inventory;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import project.bizpalm.R;
import project.bizpalm.data.database.AppDatabase;
import project.bizpalm.data.entities.Product;
import project.bizpalm.data.models.CartItem;
import project.bizpalm.ui.pos.CartAdapter;

public class ScanProductActivity extends AppCompatActivity {

    public static final String EXTRA_IS_PICKER = "extra_is_picker";
    public static final String RESULT_BARCODE = "barcode_result";

    private static final String TAG = "ScanProductActivity";
    private static final int PERMISSION_CAMERA_REQUEST = 1001;
    private static final long SCAN_DELAY_MS = 2000;
    
    private PreviewView previewView;
    private CardView cardResult;
    private TextView tvName, tvDetails;
    private ImageButton btnClose;
    private MaterialButton btnAddToCart, btnCancelScan;
    private FloatingActionButton fabViewCart;
    
    private ExecutorService cameraExecutor;
    private AppDatabase db;
    private Product lastScannedProduct;
    private volatile String lastBarcodeValue = "";
    private static List<CartItem> cartList = new ArrayList<>(); 
    private boolean isPickerMode = false;
    private boolean isScanPaused = false;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final ActivityResultLauncher<Intent> checkoutLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    cartList.clear();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState) ;
        setContentView(R.layout.activity_scan_product);

        isPickerMode = getIntent().getBooleanExtra(EXTRA_IS_PICKER, false);
        db = AppDatabase.getDatabase(this);
        
        initViews();
        cameraExecutor = Executors.newSingleThreadExecutor();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_CAMERA_REQUEST);
        }
    }

    private void initViews() {
        previewView = findViewById(R.id.previewView);
        cardResult = findViewById(R.id.cardScanResult);
        tvName = findViewById(R.id.tvScanProductName);
        tvDetails = findViewById(R.id.tvScanProductDetails);
        btnClose = findViewById(R.id.btnCloseScanner);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnCancelScan = findViewById(R.id.btnCancelScan);
        fabViewCart = findViewById(R.id.fabViewCart);

        if (isPickerMode) {
            btnAddToCart.setVisibility(View.GONE);
            fabViewCart.setVisibility(View.GONE);
        }

        btnClose.setOnClickListener(v -> finish());
        
        btnCancelScan.setOnClickListener(v -> {
            resetScanState();
        });

        btnAddToCart.setOnClickListener(v -> {
            if (lastScannedProduct != null) {
                addToCart(lastScannedProduct);
                resetScanState();
            }
        });

        fabViewCart.setOnClickListener(v -> showCartBottomSheet());
    }

    private void resetScanState() {
        lastBarcodeValue = "";
        cardResult.setVisibility(View.GONE);
        isScanPaused = false;
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error starting camera: " + e.getMessage());
                Toast.makeText(this, "Camera error", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @androidx.camera.core.ExperimentalGetImage
    private void bindCameraUseCases(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                .build();
        BarcodeScanner scanner = BarcodeScanning.getClient(options);

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(cameraExecutor, image -> {
            if (isScanPaused) {
                image.close();
                return;
            }

            Image mediaImage = image.getImage();
            if (mediaImage != null) {
                InputImage inputImage = InputImage.fromMediaImage(mediaImage, image.getImageInfo().getRotationDegrees());
                scanner.process(inputImage)
                        .addOnSuccessListener(barcodes -> {
                            if (isScanPaused || barcodes.isEmpty()) return;

                            Barcode prioritizedBarcode = null;
                            float minDistance = Float.MAX_VALUE;
                            
                            int imgWidth = inputImage.getWidth();
                            int imgHeight = inputImage.getHeight();
                            float centerX = imgWidth / 2f;
                            float centerY = imgHeight / 2f;

                            for (Barcode barcode : barcodes) {
                                Rect rect = barcode.getBoundingBox();
                                if (rect != null) {
                                    float dx = rect.centerX() - centerX;
                                    float dy = rect.centerY() - centerY;
                                    float distance = (float) Math.sqrt(dx * dx + dy * dy);

                                    if (distance < minDistance) {
                                        minDistance = distance;
                                        prioritizedBarcode = barcode;
                                    }
                                }
                            }

                            if (prioritizedBarcode != null) {
                                String rawValue = prioritizedBarcode.getRawValue();
                                if (rawValue != null && !rawValue.isEmpty() && !rawValue.equals(lastBarcodeValue)) {
                                    isScanPaused = true; 
                                    runOnUiThread(() -> {
                                        vibrate();
                                        if (isPickerMode) {
                                            Intent resultIntent = new Intent();
                                            resultIntent.putExtra(RESULT_BARCODE, rawValue);
                                            setResult(RESULT_OK, resultIntent);
                                            finish();
                                        } else {
                                            lookupProduct(rawValue);
                                            mainHandler.postDelayed(() -> isScanPaused = false, SCAN_DELAY_MS);
                                        }
                                    });
                                }
                            }
                        })
                        .addOnFailureListener(e -> Log.e(TAG, "Barcode analysis failed", e))
                        .addOnCompleteListener(task -> image.close());
            } else {
                image.close();
            }
        });

        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
        try {
            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
        } catch (Exception e) {
            Log.e(TAG, "Use case binding failed", e);
        }
    }

    private void lookupProduct(String barcode) {
        lastBarcodeValue = barcode;
        AppDatabase.databaseWriteExecutor.execute(() -> {
            Product product = db.productDao().getProductByBarcode(barcode);
            runOnUiThread(() -> {
                if (product != null) {
                    lastScannedProduct = product;
                    tvName.setText(product.productName);
                    tvDetails.setText(String.format(Locale.getDefault(), "Price: ₱%.2f | Stock: %d", product.price, product.quantity));
                } else {
                    lastScannedProduct = null;
                    tvName.setText("Product Not Found");
                    tvDetails.setText("ID: " + barcode);
                }
                cardResult.setVisibility(View.VISIBLE);
            });
        });
    }

    private void addToCart(Product product) {
        if (product.quantity <= 0) {
            Toast.makeText(this, product.productName + " is out of stock.", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean found = false;
        for (CartItem item : cartList) {
            if (item.product.id == product.id) {
                if (item.quantity < product.quantity) {
                    item.quantity++;
                } else {
                    Toast.makeText(this, "No more stock available.", Toast.LENGTH_SHORT).show();
                }
                found = true;
                break;
            }
        }
        if (!found) {
            cartList.add(new CartItem(product, 1));
        }
    }

    private void showCartBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.layout_cart_bottom_sheet, null);
        dialog.setContentView(bottomSheetView);

        RecyclerView rv = bottomSheetView.findViewById(R.id.rvCartItems);
        TextView tvTotal = bottomSheetView.findViewById(R.id.tvCartTotal);
        MaterialButton btnCheckout = bottomSheetView.findViewById(R.id.btnCheckout);

        rv.setLayoutManager(new LinearLayoutManager(this));
        CartAdapter adapter = new CartAdapter(cartList, new CartAdapter.OnItemInteractionListener() {
            @Override
            public void onRemove(int position) {
                cartList.remove(position);
                updateCartTotal(tvTotal);
                rv.getAdapter().notifyItemRemoved(position);
            }

            @Override
            public void onQuantityChanged(int position, int newQty) {
                if (newQty > cartList.get(position).product.quantity) {
                    Toast.makeText(ScanProductActivity.this, "Not enough stock.", Toast.LENGTH_SHORT).show();
                    return;
                }
                cartList.get(position).quantity = newQty;
                updateCartTotal(tvTotal);
                rv.getAdapter().notifyItemChanged(position);
            }
        });
        rv.setAdapter(adapter);
        updateCartTotal(tvTotal);

        btnCheckout.setOnClickListener(v -> {
            if (cartList.isEmpty()) {
                Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show();
                return;
            }
            dialog.dismiss();
            double total = 0;
            for (CartItem item : cartList) total += item.getSubtotal();
            
            Intent intent = new Intent(this, CheckoutActivity.class);
            intent.putExtra("TOTAL_AMOUNT", total);
            intent.putExtra("CART_ITEMS", (Serializable) cartList);
            checkoutLauncher.launch(intent);
        });

        dialog.show();
    }

    private void updateCartTotal(TextView tvTotal) {
        double total = 0;
        for (CartItem item : cartList) {
            total += item.getSubtotal();
        }
        tvTotal.setText(String.format(Locale.getDefault(), "₱%.2f", total));
    }

    private void vibrate() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null) {
            v.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CAMERA_REQUEST && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
