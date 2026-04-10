package project.bizpalm.ui.products;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import project.bizpalm.R;
import project.bizpalm.data.entities.Product;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> products = new ArrayList<>();
    private OnItemClickListener listener;

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product currentProduct = products.get(position);
        holder.tvProductName.setText(currentProduct.productName);
        holder.tvProductCategory.setText("-"); 
        holder.tvProductPrice.setText(String.format("₱%.2f", currentProduct.price));
        holder.tvStockCount.setText("Stock: " + currentProduct.quantity);

        if (currentProduct.statusLabel != null && !currentProduct.statusLabel.isEmpty()) {
            holder.tvAIStatus.setText(currentProduct.statusLabel.toUpperCase());
            holder.tvAIStatus.setVisibility(View.VISIBLE);
            
            // Customize colors based on status
            switch (currentProduct.statusLabel) {
                case "Not Selling":
                case "Out of Stock":
                case "Slow Mover":
                    holder.tvAIStatus.getBackground().setTint(Color.parseColor("#E53935")); // Red
                    break;
                case "Low Stock":
                case "Review Margin":
                    holder.tvAIStatus.getBackground().setTint(Color.parseColor("#FB8C00")); // Orange
                    break;
                default:
                    holder.tvAIStatus.getBackground().setTint(Color.parseColor("#757575")); // Gray
                    break;
            }
        } else {
            holder.tvAIStatus.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public void setProducts(List<Product> products) {
        this.products = products;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(Product product);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvProductName;
        private final TextView tvProductCategory;
        private final TextView tvProductPrice;
        private final TextView tvStockCount;
        private final TextView tvAIStatus;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductCategory = itemView.findViewById(R.id.tvProductCategory);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvStockCount = itemView.findViewById(R.id.tvStockCount);
            tvAIStatus = itemView.findViewById(R.id.tvAIStatus);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(products.get(position));
                }
            });
        }
    }
}
