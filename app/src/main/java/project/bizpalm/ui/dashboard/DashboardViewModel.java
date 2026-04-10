package project.bizpalm.ui.dashboard;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import project.bizpalm.data.entities.Transaction;
import project.bizpalm.data.repository.ProductRepository;
import project.bizpalm.data.repository.TransactionRepository;

public class DashboardViewModel extends AndroidViewModel {
    private final TransactionRepository transactionRepository;
    private final ProductRepository productRepository;

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        transactionRepository = new TransactionRepository(application);
        productRepository = new ProductRepository(application);
    }

    public LiveData<Double> getTodaySales() {
        return transactionRepository.getTodaySales();
    }

    public LiveData<Integer> getLowStockCount() {
        return productRepository.getLowStockCount();
    }

    public LiveData<List<Transaction>> getRecentTransactions() {
        return transactionRepository.getRecentTransactions();
    }
}
