package project.bizpalm.ui.dashboard;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import project.bizpalm.R;
import project.bizpalm.data.database.AppDatabase;
import project.bizpalm.data.entities.Product;
import project.bizpalm.data.entities.Transaction;
import project.bizpalm.data.entities.TransactionItem;
import project.bizpalm.data.entities.User;
import project.bizpalm.ui.auth.LoginActivity;
import project.bizpalm.utils.SessionManager;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private SessionManager sessionManager;
    private static final String PREFS_NAME = "BizPalmSettings";
    private static final String KEY_PIN_LOGIN_ENABLED = "pin_login_enabled";
    private static final String KEY_LOW_STOCK_ALERT = "low_stock_alert";

    private final ActivityResultLauncher<String> createDocumentLauncher =
            registerForActivityResult(new ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"), this::exportDataToExcel);

    private final ActivityResultLauncher<String[]> openDocumentLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), this::importDataFromExcel);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        if ("Employee".equals(sessionManager.getRole())) {
            Toast.makeText(this, "Access Denied: Employees cannot access Settings", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_settings);

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        Toolbar toolbar = findViewById(R.id.toolbarSettings);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        MaterialButton btnChangePin = findViewById(R.id.btnChangePin);
        btnChangePin.setOnClickListener(v -> showChangePinDialog());

        SwitchMaterial switchPinLogin = findViewById(R.id.switchPinLogin);
        switchPinLogin.setChecked(sharedPreferences.getBoolean(KEY_PIN_LOGIN_ENABLED, true));
        switchPinLogin.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean(KEY_PIN_LOGIN_ENABLED, isChecked).apply();
            Toast.makeText(this, "PIN Login " + (isChecked ? "Enabled" : "Disabled"), Toast.LENGTH_SHORT).show();
        });

        SwitchMaterial switchLowStockAlert = findViewById(R.id.switchLowStockAlert);
        switchLowStockAlert.setChecked(sharedPreferences.getBoolean(KEY_LOW_STOCK_ALERT, true));
        switchLowStockAlert.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean(KEY_LOW_STOCK_ALERT, isChecked).apply();
            Toast.makeText(this, "Low-stock Alert " + (isChecked ? "ON" : "OFF"), Toast.LENGTH_SHORT).show();
        });

        MaterialButton btnUserManual = findViewById(R.id.btnUserManual);
        btnUserManual.setOnClickListener(v -> {
            Intent intent = new Intent(this, UserManualActivity.class);
            startActivity(intent);
        });

        MaterialButton btnExportData = findViewById(R.id.btnExportData);
        btnExportData.setOnClickListener(v -> createDocumentLauncher.launch("BizPalm_Data_Export_" + new SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(new Date()) + ".xlsx"));

        MaterialButton btnImportData = findViewById(R.id.btnImportData);
        btnImportData.setOnClickListener(v -> showImportWarningDialog());

        MaterialButton btnDeleteData = findViewById(R.id.btnDeleteData);
        btnDeleteData.setOnClickListener(v -> showDeleteDataConfirmation());
    }

    private void showChangePinDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change PIN");
        
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_change_pin, null);
        TextInputEditText etOldPin = view.findViewById(R.id.etOldPin);
        TextInputEditText etNewPin = view.findViewById(R.id.etNewPin);
        TextInputEditText etConfirmPin = view.findViewById(R.id.etConfirmPin);
        
        builder.setView(view);
        builder.setPositiveButton("Change", (dialog, which) -> {
            String oldPin = etOldPin.getText().toString().trim();
            String newPin = etNewPin.getText().toString().trim();
            String confirmPin = etConfirmPin.getText().toString().trim();
            
            if (newPin.length() < 4 || newPin.length() > 6) {
                Toast.makeText(this, "PIN must be 4-6 digits", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (!newPin.equals(confirmPin)) {
                Toast.makeText(this, "New PINs do not match", Toast.LENGTH_SHORT).show();
                return;
            }
            
            String username = sessionManager.getUsername();
            
            AppDatabase db = AppDatabase.getDatabase(this);
            AppDatabase.databaseWriteExecutor.execute(() -> {
                User user = db.userDao().login(username, oldPin);
                if (user != null) {
                    user.pin = newPin;
                    db.userDao().update(user);
                    runOnUiThread(() -> Toast.makeText(this, "PIN updated successfully", Toast.LENGTH_SHORT).show());
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Incorrect old PIN", Toast.LENGTH_SHORT).show());
                }
            });
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void exportDataToExcel(Uri uri) {
        if (uri == null) return;

        AppDatabase.databaseWriteExecutor.execute(() -> {
            try (Workbook workbook = new XSSFWorkbook();
                 OutputStream outputStream = getContentResolver().openOutputStream(uri)) {

                AppDatabase db = AppDatabase.getDatabase(this);
                SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                SimpleDateFormat dateOnlyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                // Header Style
                CellStyle headerStyle = workbook.createCellStyle();
                headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                headerStyle.setAlignment(HorizontalAlignment.CENTER);
                Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerStyle.setFont(headerFont);

                // Export Products
                Sheet productSheet = workbook.createSheet("Inventory Products");
                productSheet.setColumnWidth(1, 20 * 256);
                productSheet.setColumnWidth(2, 15 * 256);
                productSheet.setColumnWidth(8, 15 * 256);
                
                List<Product> products = db.productDao().getAllProductsSync();
                Row productHeader = productSheet.createRow(0);
                String[] productHeaders = {"ID", "Product Name", "Barcode", "Retail Price (₱)", "Cost Price (₱)", "Quantity", "Status", "Popular", "Expiry Date"};
                for (int i = 0; i < productHeaders.length; i++) {
                    productHeader.createCell(i).setCellValue(productHeaders[i]);
                    productHeader.getCell(i).setCellStyle(headerStyle);
                }

                for (int i = 0; i < products.size(); i++) {
                    Product p = products.get(i);
                    Row row = productSheet.createRow(i + 1);
                    row.createCell(0).setCellValue(p.id);
                    row.createCell(1).setCellValue(p.productName);
                    row.createCell(2).setCellValue(p.barcode);
                    row.createCell(3).setCellValue(p.price);
                    row.createCell(4).setCellValue(p.unitCost);
                    row.createCell(5).setCellValue(p.quantity);
                    row.createCell(6).setCellValue(p.quantity <= 10 ? "Low Stock" : "In Stock");
                    row.createCell(7).setCellValue(p.isPopular ? "Yes" : "No");
                    row.createCell(8).setCellValue(p.expiryDate != null ? dateOnlyFormat.format(new Date(p.expiryDate)) : "N/A");
                }

                // Export Transactions
                Sheet transactionSheet = workbook.createSheet("Sales Transactions");
                transactionSheet.setColumnWidth(1, 15 * 256);
                transactionSheet.setColumnWidth(4, 25 * 256);
                transactionSheet.setColumnWidth(5, 15 * 256);
                
                List<Transaction> transactions = db.transactionDao().getAllTransactionsSync();
                Row transHeader = transactionSheet.createRow(0);
                String[] transHeaders = {"Transaction ID", "Total Amount (₱)", "Cash Received", "Change", "Date & Time", "Payment Type"};
                for (int i = 0; i < transHeaders.length; i++) {
                    transHeader.createCell(i).setCellValue(transHeaders[i]);
                    transHeader.getCell(i).setCellStyle(headerStyle);
                }

                for (int i = 0; i < transactions.size(); i++) {
                    Transaction t = transactions.get(i);
                    Row row = transactionSheet.createRow(i + 1);
                    row.createCell(0).setCellValue(t.id);
                    row.createCell(1).setCellValue(t.totalAmount);
                    row.createCell(2).setCellValue(t.cashReceived);
                    row.createCell(3).setCellValue(t.changeAmount);
                    row.createCell(4).setCellValue(dateTimeFormat.format(new Date(t.timestamp)));
                    row.createCell(5).setCellValue(t.isOnlinePayment ? "Online" : "Cash");
                }

                // Export Transaction Items
                Sheet itemSheet = workbook.createSheet("Transaction Items");
                Row itemHeader = itemSheet.createRow(0);
                String[] itemHeaders = {"Transaction ID", "Product Barcode", "Quantity Sold", "Amount Due (₱)", "Unit Cost (₱)"};
                for (int i = 0; i < itemHeaders.length; i++) {
                    itemHeader.createCell(i).setCellValue(itemHeaders[i]);
                    itemHeader.getCell(i).setCellStyle(headerStyle);
                }

                int itemRowIdx = 1;
                for (Transaction t : transactions) {
                    List<TransactionItem> items = db.transactionItemDao().getItemsByTransactionId(t.id);
                    for (TransactionItem item : items) {
                        Product p = db.productDao().getProductById(item.productId);
                        String barcode = (p != null) ? p.barcode : "UNKNOWN";
                        
                        Row row = itemSheet.createRow(itemRowIdx++);
                        row.createCell(0).setCellValue(item.transactionId);
                        row.createCell(1).setCellValue(barcode);
                        row.createCell(2).setCellValue(item.quantitySold);
                        row.createCell(3).setCellValue(item.amountDue);
                        row.createCell(4).setCellValue(item.unitCost);
                    }
                }

                workbook.write(outputStream);
                runOnUiThread(() -> Toast.makeText(this, "Excel report generated successfully", Toast.LENGTH_SHORT).show());

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private void showImportWarningDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Warning: Data Overwrite")
                .setMessage("Importing data from Excel will overwrite existing products with the same barcode. This action cannot be undone. Do you want to proceed?")
                .setPositiveButton("Proceed", (dialog, which) -> openDocumentLauncher.launch(new String[]{"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"}))
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void importDataFromExcel(Uri uri) {
        if (uri == null) return;

        AppDatabase.databaseWriteExecutor.execute(() -> {
            try (InputStream inputStream = getContentResolver().openInputStream(uri);
                 Workbook workbook = new XSSFWorkbook(inputStream)) {

                AppDatabase db = AppDatabase.getDatabase(this);
                SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                SimpleDateFormat dateOnlyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                DataFormatter dataFormatter = new DataFormatter();

                db.runInTransaction(() -> {
                    // 1. Import Products
                    Sheet productSheet = workbook.getSheet("Inventory Products");
                    if (productSheet != null) {
                        for (int i = 1; i <= productSheet.getLastRowNum(); i++) {
                            Row row = productSheet.getRow(i);
                            if (row == null) continue;

                            String name = dataFormatter.formatCellValue(row.getCell(1)).trim();
                            String barcode = dataFormatter.formatCellValue(row.getCell(2)).trim();
                            double price = getCellValueAsDouble(row.getCell(3));
                            double unitCost = getCellValueAsDouble(row.getCell(4));
                            int quantity = (int) getCellValueAsDouble(row.getCell(5));
                            boolean isPopular = "Yes".equalsIgnoreCase(dataFormatter.formatCellValue(row.getCell(7)));
                            
                            String expiryStr = dataFormatter.formatCellValue(row.getCell(8)).trim();
                            Long expiryDate = null;
                            if (!expiryStr.isEmpty() && !"N/A".equalsIgnoreCase(expiryStr)) {
                                try { expiryDate = dateOnlyFormat.parse(expiryStr).getTime(); }
                                catch (Exception ignored) {}
                            }

                            if (!name.isEmpty() && !barcode.isEmpty()) {
                                Product existing = db.productDao().getProductByBarcode(barcode);
                                if (existing == null) {
                                    db.productDao().insert(new Product(barcode, name, price, unitCost, quantity, isPopular, expiryDate));
                                } else {
                                    existing.productName = name;
                                    existing.price = price;
                                    existing.unitCost = unitCost;
                                    existing.quantity = quantity;
                                    existing.isPopular = isPopular;
                                    existing.expiryDate = expiryDate;
                                    db.productDao().update(existing);
                                }
                            }
                        }
                    }

                    // 2. Import Transactions
                    Map<Integer, Long> transactionIdMap = new HashMap<>();
                    Sheet transactionSheet = workbook.getSheet("Sales Transactions");
                    if (transactionSheet != null) {
                        for (int i = 1; i <= transactionSheet.getLastRowNum(); i++) {
                            Row row = transactionSheet.getRow(i);
                            if (row == null) continue;

                            int excelTransId = (int) getCellValueAsDouble(row.getCell(0));
                            double totalAmount = getCellValueAsDouble(row.getCell(1));
                            double cashReceived = getCellValueAsDouble(row.getCell(2));
                            double changeAmount = getCellValueAsDouble(row.getCell(3));
                            String dateStr = dataFormatter.formatCellValue(row.getCell(4)).trim();
                            boolean isOnline = "Online".equalsIgnoreCase(dataFormatter.formatCellValue(row.getCell(5)));

                            if (!dateStr.isEmpty()) {
                                try {
                                    long timestamp = dateTimeFormat.parse(dateStr).getTime();
                                    long newDbId = db.transactionDao().insert(new Transaction(totalAmount, cashReceived, changeAmount, timestamp, isOnline));
                                    transactionIdMap.put(excelTransId, newDbId);
                                } catch (Exception ignored) {}
                            }
                        }
                    }

                    // 3. Import Transaction Items
                    Sheet itemSheet = workbook.getSheet("Transaction Items");
                    if (itemSheet != null) {
                        List<TransactionItem> itemsToInsert = new ArrayList<>();
                        for (int i = 1; i <= itemSheet.getLastRowNum(); i++) {
                            Row row = itemSheet.getRow(i);
                            if (row == null) continue;

                            int excelTransId = (int) getCellValueAsDouble(row.getCell(0));
                            String barcode = dataFormatter.formatCellValue(row.getCell(1)).trim();
                            int qty = (int) getCellValueAsDouble(row.getCell(2));
                            double amount = getCellValueAsDouble(row.getCell(3));
                            double cost = getCellValueAsDouble(row.getCell(4));

                            Long dbTransId = transactionIdMap.get(excelTransId);
                            Product product = db.productDao().getProductByBarcode(barcode);

                            if (dbTransId != null && product != null) {
                                itemsToInsert.add(new TransactionItem(dbTransId.intValue(), amount, cost, qty, product.id));
                            }
                        }
                        if (!itemsToInsert.isEmpty()) {
                            db.transactionItemDao().insertAll(itemsToInsert);
                        }
                    }
                });

                runOnUiThread(() -> Toast.makeText(this, "Data imported successfully", Toast.LENGTH_SHORT).show());

            } catch (Exception e) {
                Log.e("SettingsActivity", "Import Error", e);
                runOnUiThread(() -> Toast.makeText(this, "Import failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private double getCellValueAsDouble(Cell cell) {
        if (cell == null) return 0.0;
        try {
            switch (cell.getCellType()) {
                case NUMERIC: return cell.getNumericCellValue();
                case STRING:
                    String val = cell.getStringCellValue().replaceAll("[^\\d.]", "");
                    return val.isEmpty() ? 0.0 : Double.parseDouble(val);
                case FORMULA:
                    return cell.getNumericCellValue();
                default: return 0.0;
            }
        } catch (Exception e) {
            return 0.0;
        }
    }

    private void showDeleteDataConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Delete All Data")
                .setMessage("Are you sure you want to delete all data? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        AppDatabase.getDatabase(getApplicationContext()).clearAllTables();
                    });
                    new SessionManager(this).logoutUser();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
