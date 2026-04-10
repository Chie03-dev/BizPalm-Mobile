package project.bizpalm.ui.inventory;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import project.bizpalm.R;
import project.bizpalm.data.entities.Product;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder> {

    private List<Product> products = Collections.emptyList();
    private OnProductActionListener listener;
    private boolean isSelectionMode = false;
    private boolean isReadOnly = false;
    private final List<Product> selectedProducts = new ArrayList<>();

    public interface OnProductActionListener {
        void onEdit(Product product);
        void onDelete(Product product);
    }

    public void setOnProductActionListener(OnProductActionListener listener) {
        this.listener = listener;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
        notifyDataSetChanged();
    }

    public void setReadOnly(boolean readOnly) {
        this.isReadOnly = readOnly;
        notifyDataSetChanged();
    }

    public void setSelectionMode(boolean isSelectionMode) {
        this.isSelectionMode = isSelectionMode;
        if (!isSelectionMode) {
            selectedProducts.clear();
        }
        notifyDataSetChanged();
    }

    public boolean isSelectionMode() {
        return isSelectionMode;
    }

    public List<Product> getSelectedProducts() {
        return selectedProducts;
    }
    
    public void selectAll() {
        selectedProducts.clear();
        selectedProducts.addAll(products);
        notifyDataSetChanged();
    }

    public void deselectAll() {
        selectedProducts.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public InventoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_inventory_product, parent, false);
        return new InventoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InventoryViewHolder holder, int position) {
        Product product = products.get(position);
        holder.tvName.setText(product.productName);
        holder.tvQuantity.setText(String.valueOf(product.quantity));
        holder.tvPrice.setText(String.format(Locale.getDefault(), "Retail: ₱%.2f", product.price));
        holder.tvCost.setText(String.format(Locale.getDefault(), "Cost: ₱%.2f", product.unitCost));

        // Display Product Image if exists, else show QR
        if (product.imageUri != null) {
            File imgFile = new File(product.imageUri);
            if (imgFile.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                holder.ivQrCode.setImageBitmap(myBitmap);
                holder.ivQrCode.setScaleType(ImageView.ScaleType.CENTER_CROP);
                holder.ivQrCode.setImageTintList(null);
            } else {
                setQRCode(holder.ivQrCode, product.barcode);
            }
        } else {
            setQRCode(holder.ivQrCode, product.barcode);
        }

        if (isSelectionMode) {
            holder.btnEdit.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.GONE);
            holder.checkbox.setVisibility(View.VISIBLE);
            holder.checkbox.setChecked(selectedProducts.contains(product));

            holder.checkbox.setOnClickListener(v -> {
                if (holder.checkbox.isChecked()) {
                    if (!selectedProducts.contains(product)) {
                        selectedProducts.add(product);
                    }
                } else {
                    selectedProducts.remove(product);
                }
            });
            
            holder.itemView.setOnClickListener(v -> holder.checkbox.performClick());

        } else {
            // Hide edit/delete if read-only (Employee role)
            if (isReadOnly) {
                holder.btnEdit.setVisibility(View.GONE);
                holder.btnDelete.setVisibility(View.GONE);
            } else {
                holder.btnEdit.setVisibility(View.VISIBLE);
                holder.btnDelete.setVisibility(View.VISIBLE);
            }

            holder.checkbox.setVisibility(View.GONE);
            holder.checkbox.setChecked(false);
            holder.itemView.setOnClickListener(null);

            holder.btnEdit.setOnClickListener(v -> {
                if (listener != null) listener.onEdit(product);
            });

            holder.btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDelete(product);
            });
        }
    }

    private void setQRCode(ImageView imageView, String barcode) {
        try {
            Bitmap qrBitmap = generateQRCode(barcode, 100);
            imageView.setImageBitmap(qrBitmap);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        } catch (Exception e) {
            imageView.setImageResource(R.drawable.ic_cube);
        }
    }

    private Bitmap generateQRCode(String text, int size) throws Exception {
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

    @Override
    public int getItemCount() {
        return products.size();
    }

    static class InventoryViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName, tvQuantity, tvPrice, tvCost;
        final ImageButton btnEdit, btnDelete;
        final CheckBox checkbox;
        final ImageView ivQrCode;

        InventoryViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvProductName);
            tvQuantity = itemView.findViewById(R.id.tvProductStock);
            tvPrice = itemView.findViewById(R.id.tvProductPrice);
            tvCost = itemView.findViewById(R.id.tvProductCost);
            btnEdit = itemView.findViewById(R.id.btnEditProduct);
            btnDelete = itemView.findViewById(R.id.btnDeleteProduct);
            checkbox = itemView.findViewById(R.id.checkboxSelect);
            ivQrCode = itemView.findViewById(R.id.ivProductQrCode);
        }
    }
}
