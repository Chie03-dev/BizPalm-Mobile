package project.bizpalm.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import project.bizpalm.R;
import project.bizpalm.data.database.AppDatabase;

public class NotificationsFragment extends Fragment {

    private NotificationAdapter adapter;
    private TextView tvNoNotifications, tvTitle;
    private LinearLayout selectionBar;
    private Button btnSelectAll;
    private ImageButton btnDelete;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        tvTitle = view.findViewById(R.id.tvTitle);
        selectionBar = view.findViewById(R.id.selection_bar);
        btnSelectAll = view.findViewById(R.id.btn_select_all);
        btnDelete = view.findViewById(R.id.btn_delete);

        ImageButton btnClose = view.findViewById(R.id.btnCloseNotif);
        btnClose.setOnClickListener(v -> {
            if (adapter != null && adapter.getSelectedIds().isEmpty()) {
                closeFragment();
            } else if (adapter != null) {
                adapter.exitSelectionMode();
            }
        });

        RecyclerView rv = view.findViewById(R.id.rvNotifications);
        tvNoNotifications = view.findViewById(R.id.tvNoNotifications);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        
        adapter = new NotificationAdapter(getContext());
        rv.setAdapter(adapter);

        adapter.setOnSelectionChangedListener(new NotificationAdapter.OnSelectionChangedListener() {
            @Override
            public void onSelectionModeChanged(boolean enabled) {
                selectionBar.setVisibility(enabled ? View.VISIBLE : View.GONE);
                if (!enabled && tvTitle != null) {
                    tvTitle.setText("Notifications");
                }
            }

            @Override
            public void onSelectionCountChanged(int count) {
                if (tvTitle != null) {
                    tvTitle.setText(count + " selected");
                }
            }
        });

        btnSelectAll.setOnClickListener(v -> { if (adapter != null) adapter.selectAll(); });
        btnDelete.setOnClickListener(v -> deleteSelected());

        setupViewModel();
        
        return view;
    }

    private void deleteSelected() {
        if (adapter == null) return;
        List<Integer> ids = adapter.getSelectedIds();
        if (ids.isEmpty()) return;

        AppDatabase.databaseWriteExecutor.execute(() -> {
            if (getContext() == null) return;
            AppDatabase db = AppDatabase.getDatabase(getContext());
            db.notificationDao().deleteByIds(ids);
            
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (adapter != null) adapter.exitSelectionMode();
                    Toast.makeText(getContext(), "Deleted " + ids.size() + " notifications", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void closeFragment() {
        if (getFragmentManager() != null && getContext() != null) {
            markAllAsRead();
            getFragmentManager().beginTransaction()
                    .setCustomAnimations(0, android.R.anim.fade_out)
                    .remove(this)
                    .commit();
        }
    }

    private void setupViewModel() {
        NotificationsViewModel viewModel = new ViewModelProvider(this).get(NotificationsViewModel.class);
        
        viewModel.getAllNotifications().observe(getViewLifecycleOwner(), notifications -> {
            if (adapter != null) {
                adapter.setNotifications(notifications);
            }
            if (tvNoNotifications != null) {
                tvNoNotifications.setVisibility(notifications.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });
    }
    
    private void markAllAsRead() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            if (getContext() == null) return;
            AppDatabase db = AppDatabase.getDatabase(getContext());
            db.notificationDao().markAllAsRead();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}
