package project.bizpalm.ui.transactions;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import project.bizpalm.R;
import project.bizpalm.data.database.AppDatabase;
import project.bizpalm.data.entities.Product;
import project.bizpalm.data.entities.Transaction;
import project.bizpalm.data.entities.TransactionItem;

public class TransactionDetailActivity extends AppCompatActivity {

    private TextView tvId, tvDate, tvTotal, tvCash, tvChange, tvPaymentMethod;
    private TextView tvLoanerName;
    private ImageView ivSignature;
    private LinearLayout llLoanInfo;
    private RecyclerView rvItems;
    private MaterialButton btnPrintReceipt, btnDeleteTransaction, btnMarkAsPaid;
    private TransactionItemsAdapter adapter;
    private AppDatabase db;
    private TransactionViewModel viewModel;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
    
    private Transaction currentTransaction;
    private List<DetailedItem> currentDetailedItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_detail);

        int transactionId = getIntent().getIntExtra("TRANSACTION_ID", -1);
        if (transactionId == -1) {
            finish();
            return;
        }

        db = AppDatabase.getDatabase(this);
        viewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
        initViews();
        loadTransactionData(transactionId);
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        tvId = findViewById(R.id.tvDetailId);
        tvDate = findViewById(R.id.tvDetailDate);
        tvTotal = findViewById(R.id.tvDetailTotal);
        tvCash = findViewById(R.id.tvDetailCash);
        tvChange = findViewById(R.id.tvDetailChange);
        tvPaymentMethod = findViewById(R.id.tvDetailPaymentMethod);
        
        llLoanInfo = findViewById(R.id.llLoanInfo);
        tvLoanerName = findViewById(R.id.tvLoanerName);
        ivSignature = findViewById(R.id.ivSignature);
        
        rvItems = findViewById(R.id.rvTransactionItems);
        btnPrintReceipt = findViewById(R.id.btnPrintReceipt);
        btnDeleteTransaction = findViewById(R.id.btnDeleteTransaction);
        btnMarkAsPaid = findViewById(R.id.btnMarkAsPaid);

        rvItems.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TransactionItemsAdapter();
        rvItems.setAdapter(adapter);

        btnPrintReceipt.setOnClickListener(v -> generateReceiptPdf());
        btnDeleteTransaction.setOnClickListener(v -> confirmDelete());
        btnMarkAsPaid.setOnClickListener(v -> confirmMarkAsPaid());
    }

    private void loadTransactionData(int transactionId) {
        executorService.execute(() -> {
            currentTransaction = db.transactionDao().getTransactionById(transactionId);
            List<TransactionItem> items = db.transactionItemDao().getItemsByTransactionId(transactionId);
            
            List<DetailedItem> detailedItems = new ArrayList<>();
            for (TransactionItem item : items) {
                Product product = db.productDao().getProductById(item.productId);
                detailedItems.add(new DetailedItem(
                        product != null ? product.productName : "Unknown Product",
                        item.quantitySold,
                        item.amountDue
                ));
            }
            currentDetailedItems = detailedItems;

            runOnUiThread(() -> {
                if (currentTransaction != null) {
                    tvId.setText("Receipt #" + currentTransaction.id);
                    tvDate.setText("Date: " + sdf.format(new Date(currentTransaction.timestamp)));
                    tvTotal.setText(String.format(Locale.getDefault(), "₱%.2f", currentTransaction.totalAmount));
                    tvCash.setText(String.format(Locale.getDefault(), "₱%.2f", currentTransaction.cashReceived));
                    tvChange.setText(String.format(Locale.getDefault(), "₱%.2f", currentTransaction.changeAmount));
                    
                    if (currentTransaction.isLoaned) {
                        tvPaymentMethod.setText("LOAN (UTANG)");
                        tvPaymentMethod.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FFEBEE")));
                        tvPaymentMethod.setTextColor(android.graphics.Color.parseColor("#D32F2F"));
                        
                        llLoanInfo.setVisibility(View.VISIBLE);
                        tvLoanerName.setText("Loaner: " + currentTransaction.loanerName);
                        btnMarkAsPaid.setVisibility(View.VISIBLE);

                        if (currentTransaction.loanerSignature != null) {
                            try {
                                byte[] decodedString = Base64.decode(currentTransaction.loanerSignature, Base64.DEFAULT);
                                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                ivSignature.setImageBitmap(decodedByte);
                            } catch (Exception ignored) {}
                        }
                    } else if (currentTransaction.isOnlinePayment) {
                        tvPaymentMethod.setText("ONLINE PAYMENT");
                        btnMarkAsPaid.setVisibility(View.GONE);
                    } else {
                        tvPaymentMethod.setText("CASH");
                        btnMarkAsPaid.setVisibility(View.GONE);
                    }

                    adapter.setItems(detailedItems);
                }
            });
        });
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Transaction")
                .setMessage("Are you sure you want to permanently delete this transaction record?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deleteTransaction(currentTransaction);
                    Toast.makeText(this, "Transaction deleted", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_menu_delete)
                .show();
    }

    private void confirmMarkAsPaid() {
        new AlertDialog.Builder(this)
                .setTitle("Mark as Paid")
                .setMessage("Has this customer fully paid the loan amount of ₱" + String.format(Locale.getDefault(), "%.2f", currentTransaction.totalAmount) + "?")
                .setPositiveButton("Yes, Paid", (dialog, which) -> {
                    viewModel.markAsPaid(currentTransaction);
                    Toast.makeText(this, "Transaction marked as PAID", Toast.LENGTH_SHORT).show();
                    loadTransactionData(currentTransaction.id); // Refresh view
                })
                .setNegativeButton("Not Yet", null)
                .show();
    }

    private void generateReceiptPdf() {
        if (currentTransaction == null || currentDetailedItems.isEmpty()) return;

        executorService.execute(() -> {
            String fileName = "Receipt_" + currentTransaction.id + "_" + System.currentTimeMillis() + ".pdf";
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/BizPalm_Receipts");
            }

            Uri uri = getContentResolver().insert(MediaStore.Files.getContentUri("external"), values);
            if (uri == null) return;

            try (OutputStream out = getContentResolver().openOutputStream(uri)) {
                PdfWriter writer = new PdfWriter(out);
                PdfDocument pdf = new PdfDocument(writer);
                Document document = new Document(pdf);

                document.add(new Paragraph("BIZPALM RETAIL POS")
                        .setTextAlignment(TextAlignment.CENTER)
                        .setBold().setFontSize(18f));
                document.add(new Paragraph("OFFICIAL RECEIPT")
                        .setTextAlignment(TextAlignment.CENTER)
                        .setFontSize(12f));
                document.add(new Paragraph("\n"));

                document.add(new Paragraph("Receipt #: " + currentTransaction.id));
                document.add(new Paragraph("Date: " + sdf.format(new Date(currentTransaction.timestamp))));
                
                String paymentStr = "Cash";
                if (currentTransaction.isLoaned) paymentStr = "LOAN (Utang) - " + currentTransaction.loanerName;
                else if (currentTransaction.isOnlinePayment) paymentStr = "Online";
                
                document.add(new Paragraph("Payment: " + paymentStr));
                document.add(new Paragraph("\n"));

                float[] columnWidths = {200f, 50f, 100f};
                Table table = new Table(UnitValue.createPointArray(columnWidths));
                table.addCell(new Cell().add(new Paragraph("Item")).setBold());
                table.addCell(new Cell().add(new Paragraph("Qty")).setBold());
                table.addCell(new Cell().add(new Paragraph("Price")).setBold());

                for (DetailedItem item : currentDetailedItems) {
                    table.addCell(new Cell().add(new Paragraph(item.name)));
                    table.addCell(new Cell().add(new Paragraph(String.valueOf(item.qty))));
                    table.addCell(new Cell().add(new Paragraph(String.format(Locale.getDefault(), "₱%.2f", item.subtotal))));
                }
                document.add(table);

                document.add(new Paragraph("\n"));
                document.add(new Paragraph("Total: ₱" + String.format(Locale.getDefault(), "%.2f", currentTransaction.totalAmount)).setBold());
                
                if (!currentTransaction.isLoaned) {
                    document.add(new Paragraph("Cash: ₱" + String.format(Locale.getDefault(), "₱%.2f", currentTransaction.cashReceived)));
                    document.add(new Paragraph("Change: ₱" + String.format(Locale.getDefault(), "₱%.2f", currentTransaction.changeAmount)));
                } else {
                    document.add(new Paragraph("STATUS: UNPAID LOAN").setBold().setFontColor(com.itextpdf.kernel.colors.ColorConstants.RED));
                }

                document.add(new Paragraph("\nThank you for shopping!").setTextAlignment(TextAlignment.CENTER).setItalic());
                document.close();

                runOnUiThread(() -> {
                    Toast.makeText(this, "Receipt saved to Downloads/BizPalm_Receipts", Toast.LENGTH_LONG).show();
                    shareFile(uri);
                });

            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Error generating PDF", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void shareFile(Uri uri) {
        Intent intent = new Intent(Intent.ACTION_SEND)
                .setType("application/pdf")
                .putExtra(Intent.EXTRA_STREAM, uri)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, "Share Receipt"));
    }

    private static class DetailedItem {
        String name;
        int qty;
        double subtotal;

        DetailedItem(String name, int qty, double subtotal) {
            this.name = name;
            this.qty = qty;
            this.subtotal = subtotal;
        }
    }

    private static class TransactionItemsAdapter extends RecyclerView.Adapter<TransactionItemsAdapter.ViewHolder> {
        private List<DetailedItem> items = new ArrayList<>();

        void setItems(List<DetailedItem> items) {
            this.items = items;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_detail_product, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            DetailedItem item = items.get(position);
            holder.tvName.setText(item.name);
            holder.tvQty.setText("Qty: " + item.qty);
            holder.tvSubtotal.setText(String.format(Locale.getDefault(), "₱%.2f", item.subtotal));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvQty, tvSubtotal;
            ViewHolder(View v) {
                super(v);
                tvName = v.findViewById(R.id.tvDetailProductName);
                tvQty = v.findViewById(R.id.tvDetailProductQty);
                tvSubtotal = v.findViewById(R.id.tvDetailProductSubtotal);
            }
        }
    }
}
