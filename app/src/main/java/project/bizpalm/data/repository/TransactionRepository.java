package project.bizpalm.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.util.ArrayList;
import java.util.List;
import project.bizpalm.data.entities.Transaction;

public class TransactionRepository {
    public TransactionRepository(Application application) {}
    
    public LiveData<Double> getTodaySales() {
        return new MutableLiveData<>(0.0);
    }
    
    public LiveData<List<Transaction>> getRecentTransactions() {
        return new MutableLiveData<>(new ArrayList<>());
    }
}
