package project.bizpalm.ui.transactions;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

import project.bizpalm.R;
import project.bizpalm.data.entities.Transaction;

public class TransactionHistoryActivity extends AppCompatActivity {

    private TransactionAdapter adapter;
    private TransactionViewModel viewModel;
    private List<Transaction> allTransactions = new ArrayList<>();
    private TextView tvEmpty;
    private RecyclerView rv;
    private ChipGroup chipGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_history);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        rv = findViewById(R.id.rvTransactions);
        tvEmpty = findViewById(R.id.tvEmpty);
        chipGroup = findViewById(R.id.chipGroupFilters);

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TransactionAdapter();
        rv.setAdapter(adapter);

        adapter.setOnTransactionClickListener(transaction -> {
            Intent intent = new Intent(this, TransactionDetailActivity.class);
            intent.putExtra("TRANSACTION_ID", transaction.id);
            startActivity(intent);
        });

        viewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
        viewModel.getAllTransactions().observe(this, transactions -> {
            allTransactions = transactions;
            applyFilter(chipGroup.getCheckedChipId());
        });

        setupFilters();
    }

    private void setupFilters() {
        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipCalendar) {
                showDatePicker();
            } else {
                applyFilter(checkedId);
            }
        });
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(year, month, dayOfMonth, 0, 0, 0);
            selected.set(Calendar.MILLISECOND, 0);
            long startOfDay = selected.getTimeInMillis();
            
            selected.set(Calendar.HOUR_OF_DAY, 23);
            selected.set(Calendar.MINUTE, 59);
            selected.set(Calendar.SECOND, 59);
            long endOfDay = selected.getTimeInMillis();

            filterByRange(startOfDay, endOfDay);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void applyFilter(int checkedId) {
        Calendar cal = Calendar.getInstance();
        long now = cal.getTimeInMillis();
        
        if (checkedId == R.id.chipAll || checkedId == View.NO_ID) {
            updateList(allTransactions);
            return;
        }

        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        if (checkedId == R.id.chipToday) {
            filterByRange(cal.getTimeInMillis(), now);
        } else if (checkedId == R.id.chipWeek) {
            cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
            filterByRange(cal.getTimeInMillis(), now);
        } else if (checkedId == R.id.chipMonth) {
            cal.set(Calendar.DAY_OF_MONTH, 1);
            filterByRange(cal.getTimeInMillis(), now);
        } else if (checkedId == R.id.chipYear) {
            cal.set(Calendar.DAY_OF_YEAR, 1);
            filterByRange(cal.getTimeInMillis(), now);
        }
    }

    private void filterByRange(long start, long end) {
        List<Transaction> filtered = allTransactions.stream()
                .filter(t -> t.timestamp >= start && t.timestamp <= end)
                .collect(Collectors.toList());
        updateList(filtered);
    }

    private void updateList(List<Transaction> list) {
        if (list == null || list.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rv.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rv.setVisibility(View.VISIBLE);
            adapter.setTransactions(list);
        }
    }
}
