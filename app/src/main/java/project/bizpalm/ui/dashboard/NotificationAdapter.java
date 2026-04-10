package project.bizpalm.ui.dashboard;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.color.MaterialColors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import project.bizpalm.R;
import project.bizpalm.data.entities.Notification;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<Notification> notifications = Collections.emptyList();
    private final Context context;
    private final Set<Integer> selectedIds = new HashSet<>();
    private boolean isSelectionMode = false;
    private OnSelectionChangedListener selectionChangedListener;

    public interface OnSelectionChangedListener {
        void onSelectionModeChanged(boolean enabled);
        void onSelectionCountChanged(int count);
    }

    public NotificationAdapter(Context context) {
        this.context = context;
    }

    public void setOnSelectionChangedListener(OnSelectionChangedListener listener) {
        this.selectionChangedListener = listener;
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        
        holder.tvTitle.setText(notification.title);
        holder.tvMessage.setText(notification.message);
        holder.tvTime.setText(formatTimeAgo(notification.timestamp));

        int onSurfaceColor = MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnSurface, Color.BLACK);
        int surfaceVariantColor = MaterialColors.getColor(context, com.google.android.material.R.attr.colorSurfaceVariant, Color.LTGRAY);
        int primaryColor = MaterialColors.getColor(context, com.google.android.material.R.attr.colorPrimaryContainer, Color.BLUE);
        
        if (notification.type.equals("OUT_OF_STOCK")) {
            holder.ivIcon.setImageResource(android.R.drawable.stat_sys_warning);
            holder.ivIcon.setColorFilter(Color.RED);
            holder.tvTitle.setTextColor(Color.RED);
        } else if (notification.type.equals("LOW_STOCK")) {
            holder.ivIcon.setImageResource(android.R.drawable.stat_sys_warning);
            holder.ivIcon.setColorFilter(Color.parseColor("#FF9800"));
            holder.tvTitle.setTextColor(Color.parseColor("#E65100"));
        } else {
            holder.ivIcon.setImageResource(android.R.drawable.ic_menu_info_details);
            holder.ivIcon.setColorFilter(onSurfaceColor);
            holder.tvTitle.setTextColor(onSurfaceColor);
        }

        if (selectedIds.contains(notification.id)) {
            holder.itemView.setBackgroundColor(primaryColor);
        } else if (!notification.isRead) {
            holder.itemView.setBackgroundColor(surfaceVariantColor);
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }

        holder.itemView.setAlpha(notification.isRead ? 0.6f : 1.0f);
        
        holder.tvMessage.setTextColor(onSurfaceColor);
        holder.tvTime.setTextColor(onSurfaceColor);
        holder.tvMessage.setAlpha(0.7f);
        holder.tvTime.setAlpha(0.5f);

        holder.itemView.setOnLongClickListener(v -> {
            if (!isSelectionMode) {
                isSelectionMode = true;
                toggleSelection(notification.id);
                if (selectionChangedListener != null) {
                    selectionChangedListener.onSelectionModeChanged(true);
                }
                return true;
            }
            return false;
        });

        holder.itemView.setOnClickListener(v -> {
            if (isSelectionMode) {
                toggleSelection(notification.id);
            }
        });
    }

    private void toggleSelection(int id) {
        if (selectedIds.contains(id)) {
            selectedIds.remove(id);
        } else {
            selectedIds.add(id);
        }
        notifyDataSetChanged();
        if (selectionChangedListener != null) {
            selectionChangedListener.onSelectionCountChanged(selectedIds.size());
        }
        if (selectedIds.isEmpty() && isSelectionMode) {
            exitSelectionMode();
        }
    }

    public void selectAll() {
        selectedIds.clear();
        for (Notification n : notifications) {
            selectedIds.add(n.id);
        }
        notifyDataSetChanged();
        if (selectionChangedListener != null) {
            selectionChangedListener.onSelectionCountChanged(selectedIds.size());
        }
    }

    public void exitSelectionMode() {
        isSelectionMode = false;
        selectedIds.clear();
        notifyDataSetChanged();
        if (selectionChangedListener != null) {
            selectionChangedListener.onSelectionModeChanged(false);
        }
    }

    public List<Integer> getSelectedIds() {
        return new ArrayList<>(selectedIds);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }
    
    private String formatTimeAgo(long timestamp) {
        long diff = System.currentTimeMillis() - timestamp;
        if (diff < 0) diff = 0;

        if (diff < TimeUnit.MINUTES.toMillis(1)) {
            return "Just now";
        } else if (diff < TimeUnit.HOURS.toMillis(1)) {
            long mins = TimeUnit.MILLISECONDS.toMinutes(diff);
            return mins + (mins == 1 ? " min ago" : " mins ago");
        } else if (diff < TimeUnit.DAYS.toMillis(1)) {
            long hrs = TimeUnit.MILLISECONDS.toHours(diff);
            return hrs + (hrs == 1 ? " hr ago" : " hrs ago");
        } else {
            long days = TimeUnit.MILLISECONDS.toDays(diff);
            return days + (days == 1 ? " day ago" : " days ago");
        }
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivIcon;
        final TextView tvTitle;
        final TextView tvMessage;
        final TextView tvTime;

        NotificationViewHolder(View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivNotifIcon);
            tvTitle = itemView.findViewById(R.id.tvNotifTitle);
            tvMessage = itemView.findViewById(R.id.tvNotifMessage);
            tvTime = itemView.findViewById(R.id.tvNotifTime);
        }
    }
}
