package project.bizpalm.ui.inventory;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import project.bizpalm.R;
import project.bizpalm.data.database.AppDatabase;
import project.bizpalm.data.entities.InventoryLog;

public class InventoryLogAdapter extends RecyclerView.Adapter<InventoryLogAdapter.LogViewHolder> {

    private List<InventoryLog> logs = new ArrayList<>();
    private final AppDatabase db;

    public InventoryLogAdapter(AppDatabase db) {
        this.db = db;
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_inventory_log, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        InventoryLog log = logs.get(position);
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        holder.tvDate.setText(sdf.format(new Date(log.logDate)));
        holder.tvType.setText(log.actionType);
        holder.tvChange.setText("Change: " + (log.quantityChange > 0 ? "+" : "") + log.quantityChange);
        holder.tvStock.setText("Stock: " + log.previousQuantity + " -> " + log.newQuantity);

        // We should ideally have the product name in the log or fetch it.
        // For simplicity, we can fetch it if needed, but in a real app, it's better to join in the DAO.
        // I'll leave it as a placeholder or update the log entity to include product name for performance if this were production.
        holder.tvProduct.setText("Product ID: " + log.productId);
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    public void setLogs(List<InventoryLog> logs) {
        this.logs = logs;
        notifyDataSetChanged();
    }

    static class LogViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvType, tvProduct, tvChange, tvStock;

        public LogViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvLogDate);
            tvType = itemView.findViewById(R.id.tvLogType);
            tvProduct = itemView.findViewById(R.id.tvLogProduct);
            tvChange = itemView.findViewById(R.id.tvLogChange);
            tvStock = itemView.findViewById(R.id.tvLogStock);
        }
    }
}
