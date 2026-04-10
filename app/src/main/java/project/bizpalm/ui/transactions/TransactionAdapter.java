package project.bizpalm.ui.transactions;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import project.bizpalm.R;
import project.bizpalm.data.entities.Transaction;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transaction> transactions = new ArrayList<>();
    private final SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
    private OnTransactionClickListener listener;

    public interface OnTransactionClickListener {
        void onTransactionClick(Transaction transaction);
    }

    public void setOnTransactionClickListener(OnTransactionClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);
        holder.tvId.setText("Receipt #" + transaction.id);
        holder.tvDate.setText(sdf.format(new Date(transaction.timestamp)));
        holder.tvAmount.setText(String.format(Locale.getDefault(), "₱%.2f", transaction.totalAmount));
        
        // Dynamic Icon Logic
        if (transaction.isLoaned) {
            holder.ivReceiptIcon.setImageResource(R.drawable.ic_loan);
            holder.ivReceiptIcon.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FFEBEE")));
            holder.ivReceiptIcon.setColorFilter(android.graphics.Color.parseColor("#D32F2F"));
            holder.tvLoanFlag.setVisibility(View.VISIBLE);
            holder.tvLoanFlag.setText("UNPAID LOAN");
        } else {
            // It's paid. Check if it was a loan before or direct payment.
            if (transaction.loanerName != null && !transaction.loanerName.isEmpty()) {
                // Was a loan, now paid
                holder.ivReceiptIcon.setImageResource(R.drawable.ic_save); // Using save as a "check" or "completed" icon
                holder.ivReceiptIcon.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#E8F5E9")));
                holder.ivReceiptIcon.setColorFilter(android.graphics.Color.parseColor("#388E3C"));
                holder.tvLoanFlag.setVisibility(View.VISIBLE);
                holder.tvLoanFlag.setText("LOAN PAID");
                holder.tvLoanFlag.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#E8F5E9")));
                holder.tvLoanFlag.setTextColor(android.graphics.Color.parseColor("#388E3C"));
            } else {
                // Standard direct payment
                holder.ivReceiptIcon.setImageResource(transaction.isOnlinePayment ? R.drawable.ic_gcash : R.drawable.ic_cash);
                holder.ivReceiptIcon.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#F0F2F5")));
                holder.ivReceiptIcon.setColorFilter(android.graphics.Color.parseColor("#1A1A1A"));
                holder.tvLoanFlag.setVisibility(View.GONE);
            }
        }

        // Small indicator icon for payment type (GCash/Cash)
        if (transaction.isOnlinePayment) {
            holder.ivPaymentType.setVisibility(View.VISIBLE);
            holder.ivPaymentType.setImageResource(R.drawable.ic_gcash); 
        } else {
            holder.ivPaymentType.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTransactionClick(transaction);
            }
        });
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
        notifyDataSetChanged();
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView tvId, tvDate, tvAmount, tvLoanFlag;
        ImageView ivPaymentType, ivReceiptIcon;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tvTransactionId);
            tvDate = itemView.findViewById(R.id.tvTransactionDate);
            tvAmount = itemView.findViewById(R.id.tvTransactionAmount);
            ivPaymentType = itemView.findViewById(R.id.ivPaymentType);
            ivReceiptIcon = itemView.findViewById(R.id.ivReceiptIcon);
            tvLoanFlag = itemView.findViewById(R.id.tvLoanFlag);
        }
    }
}
