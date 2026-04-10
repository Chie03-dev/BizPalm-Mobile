package project.bizpalm.ui.pos;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import project.bizpalm.R;
import project.bizpalm.data.models.CartItem;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private final List<CartItem> cartItems;
    private final OnItemInteractionListener listener;

    public interface OnItemInteractionListener {
        void onRemove(int position);
        void onQuantityChanged(int position, int newQty);
    }

    public CartAdapter(List<CartItem> cartItems, OnItemInteractionListener listener) {
        this.cartItems = cartItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.tvItemName.setText(item.product.productName);
        holder.tvItemDetails.setText(String.format("₱%.2f each", item.product.price));
        holder.tvItemSubtotal.setText(String.format("₱%.2f", item.getSubtotal()));
        holder.tvItemQty.setText(String.valueOf(item.quantity));

        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemove(holder.getAdapterPosition());
            }
        });

        holder.btnIncrease.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (listener != null && pos != RecyclerView.NO_POSITION) {
                listener.onQuantityChanged(pos, item.quantity + 1);
            }
        });

        holder.btnDecrease.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (listener != null && pos != RecyclerView.NO_POSITION && item.quantity > 1) {
                listener.onQuantityChanged(pos, item.quantity - 1);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemName, tvItemDetails, tvItemSubtotal, tvItemQty;
        ImageButton btnRemove, btnIncrease, btnDecrease;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvItemDetails = itemView.findViewById(R.id.tvItemDetails);
            tvItemSubtotal = itemView.findViewById(R.id.tvItemSubtotal);
            tvItemQty = itemView.findViewById(R.id.tvItemQty);
            btnRemove = itemView.findViewById(R.id.btnRemove);
            btnIncrease = itemView.findViewById(R.id.btnIncreaseQty);
            btnDecrease = itemView.findViewById(R.id.btnDecreaseQty);
        }
    }
}
