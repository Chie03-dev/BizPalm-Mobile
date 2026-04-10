package project.bizpalm.ui.inventory;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import project.bizpalm.R;
import project.bizpalm.data.database.AppDatabase;
import project.bizpalm.data.entities.InventoryLog;
import project.bizpalm.data.entities.Notification;
import project.bizpalm.data.entities.Product;
import project.bizpalm.data.entities.Transaction;
import project.bizpalm.data.entities.TransactionItem;
import project.bizpalm.data.models.CartItem;
import project.bizpalm.utils.NotificationHelper;

public class CheckoutActivity extends AppCompatActivity {

    private double totalAmount = 0.0;
    private List<CartItem> cartItems = new ArrayList<>();
    private TextView tvTotal, tvChange, tvReceiptDetails;
    private TextInputEditText etCash, etLoanerName;
    private SwitchMaterial switchOnlinePayment, switchLoan;
    private MaterialButton btnComplete, btnBack, btnCloseReceipt;
    private LinearLayout llLoanDetails;
    private FrameLayout signatureContainer;
    private SignatureView signatureView;
    private Button btnClearSignature;
    private CardView cardReceipt;
    private AppDatabase db;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static final int LOW_STOCK_THRESHOLD = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        db = AppDatabase.getDatabase(this);
        totalAmount = getIntent().getDoubleExtra("TOTAL_AMOUNT", 0.0);
        
        loadCartItems();

        tvTotal = findViewById(R.id.tvTotalAmount);
        tvChange = findViewById(R.id.tvChangeAmount);
        etCash = findViewById(R.id.etCashReceived);
        switchOnlinePayment = findViewById(R.id.switchOnlinePayment);
        switchLoan = findViewById(R.id.switchLoan);
        llLoanDetails = findViewById(R.id.llLoanDetails);
        etLoanerName = findViewById(R.id.etLoanerName);
        signatureContainer = findViewById(R.id.signatureContainer);
        btnClearSignature = findViewById(R.id.btnClearSignature);
        btnComplete = findViewById(R.id.btnCompleteCheckout);
        btnBack = findViewById(R.id.btnBackToMenu);
        cardReceipt = findViewById(R.id.cardReceipt);
        tvReceiptDetails = findViewById(R.id.tvReceiptDetails);
        btnCloseReceipt = findViewById(R.id.btnCloseReceipt);

        // Setup Signature Pad
        signatureView = new SignatureView(this, null);
        signatureContainer.addView(signatureView);

        tvTotal.setText(String.format(Locale.getDefault(), "₱%.2f", totalAmount));

        switchLoan.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                llLoanDetails.setVisibility(View.VISIBLE);
                switchOnlinePayment.setChecked(false);
                switchOnlinePayment.setEnabled(false);
                etCash.setText("0.00");
                etCash.setEnabled(false);
                btnComplete.setEnabled(true);
            } else {
                llLoanDetails.setVisibility(View.GONE);
                switchOnlinePayment.setEnabled(true);
                etCash.setEnabled(true);
                etCash.setText("");
                calculateChange();
            }
        });

        switchOnlinePayment.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                etCash.setText(String.valueOf(totalAmount));
                etCash.setEnabled(false);
            } else {
                etCash.setText("");
                etCash.setEnabled(true);
            }
            calculateChange();
        });

        etCash.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateChange();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnClearSignature.setOnClickListener(v -> signatureView.clear());
        btnComplete.setOnClickListener(v -> saveAndShowReceipt());
        btnBack.setOnClickListener(v -> finish()); 

        btnCloseReceipt.setOnClickListener(v -> {
            setResult(RESULT_OK);
            finish();
        });
    }

    private void calculateChange() {
        if (switchLoan.isChecked()) return;

        String cashStr = etCash.getText().toString().trim();
        if (cashStr.isEmpty()) {
            tvChange.setText("₱0.00");
            btnComplete.setEnabled(false);
            return;
        }

        try {
            double cash = Double.parseDouble(cashStr);
            double change = cash - totalAmount;
            if (cash <= 0) {
                tvChange.setText("Invalid Amount");
                btnComplete.setEnabled(false);
            } else if (change >= 0) {
                tvChange.setText(String.format(Locale.getDefault(), "₱%.2f", change));
                btnComplete.setEnabled(true);
            } else {
                tvChange.setText("Insufficient Cash");
                btnComplete.setEnabled(false);
            }
        } catch (NumberFormatException e) {
            tvChange.setText("Invalid Amount");
            btnComplete.setEnabled(false);
        }
    }

    private void saveAndShowReceipt() {
        boolean isLoan = switchLoan.isChecked();
        String loanerName = etLoanerName.getText().toString().trim();
        String signatureBase64 = signatureView.getSignatureBase64();

        if (isLoan) {
            if (loanerName.isEmpty()) {
                Toast.makeText(this, "Please enter Loaner's Name", Toast.LENGTH_SHORT).show();
                return;
            }
            if (signatureView.isSignatureEmpty()) {
                Toast.makeText(this, "Customer signature is required for loan", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        String cashStr = etCash.getText().toString().trim();
        double cash = 0;
        if (!isLoan) {
            if (cashStr.isEmpty()) {
                Toast.makeText(this, "Please enter cash amount", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                cash = Double.parseDouble(cashStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid cash amount", Toast.LENGTH_SHORT).show();
                return;
            }
            if (cash < totalAmount) {
                Toast.makeText(this, "Insufficient cash", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        final double finalCash = cash;
        double change = isLoan ? 0 : cash - totalAmount;
        boolean isOnline = switchOnlinePayment.isChecked();

        executorService.execute(() -> {
            // Stock Validation
            StringBuilder stockErrors = new StringBuilder();
            for (CartItem cartItem : cartItems) {
                Product product = db.productDao().getProductById(cartItem.product.id);
                if (product != null && product.quantity < cartItem.quantity) {
                    stockErrors.append(product.productName).append(": Available: ").append(product.quantity).append("\n");
                }
            }

            if (stockErrors.length() > 0) {
                runOnUiThread(() -> Toast.makeText(this, "Not enough stock for:\n" + stockErrors.toString(), Toast.LENGTH_LONG).show());
                return;
            }

            db.runInTransaction(() -> {
                Transaction transaction = new Transaction(totalAmount, finalCash, Math.max(0, change), System.currentTimeMillis(), isOnline);
                transaction.isLoaned = isLoan;
                transaction.loanerName = isLoan ? loanerName : null;
                transaction.loanerSignature = isLoan ? signatureBase64 : null;
                
                long transactionId = db.transactionDao().insert(transaction);

                List<TransactionItem> transactionItems = new ArrayList<>();
                for (CartItem cartItem : cartItems) {
                    Product product = db.productDao().getProductById(cartItem.product.id);
                    if (product != null) {
                        transactionItems.add(new TransactionItem((int) transactionId, cartItem.getSubtotal(), product.unitCost, cartItem.quantity, cartItem.product.id));
                        int oldQuantity = product.quantity;
                        product.quantity -= cartItem.quantity;
                        db.productDao().update(product);
                        db.inventoryLogDao().insert(new InventoryLog(product.id, "SALE", -cartItem.quantity, oldQuantity, product.quantity, System.currentTimeMillis()));
                    }
                }
                db.transactionItemDao().insertAll(transactionItems);
            });

            runOnUiThread(() -> {
                String details = (isLoan ? "LOANED TO: " + loanerName : "Payment Type: " + (isOnline ? "Online" : "Cash")) + "\n" +
                                 "Total: ₱" + String.format(Locale.getDefault(), "%.2f", totalAmount) + "\n" +
                                 "Status: " + (isLoan ? "UNPAID (LOAN)" : "PAID");
                
                tvReceiptDetails.setText(details);
                cardReceipt.setVisibility(View.VISIBLE);
                btnComplete.setVisibility(View.GONE);
                btnBack.setVisibility(View.GONE);
            });
        });
    }

    @SuppressWarnings("unchecked")
    private void loadCartItems() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Serializable serializable = getIntent().getSerializableExtra("CART_ITEMS", ArrayList.class);
            if (serializable instanceof List) {
                cartItems = (List<CartItem>) serializable;
            }
        } else {
            Serializable serializable = getIntent().getSerializableExtra("CART_ITEMS");
            if (serializable instanceof List) {
                cartItems = (List<CartItem>) serializable;
            }
        }
    }
}
