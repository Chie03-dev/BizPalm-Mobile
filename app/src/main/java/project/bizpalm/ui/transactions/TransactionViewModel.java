package project.bizpalm.ui.transactions;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import project.bizpalm.data.database.AppDatabase;
import project.bizpalm.data.entities.Transaction;

public class TransactionViewModel extends AndroidViewModel {

    private final AppDatabase db;
    private final LiveData<List<Transaction>> transactions;

    public TransactionViewModel(@NonNull Application application) {
        super(application);
        db = AppDatabase.getDatabase(application);
        transactions = db.transactionDao().getAllTransactions();
    }

    public LiveData<List<Transaction>> getAllTransactions() {
        return transactions;
    }

    public void deleteTransaction(Transaction transaction) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            db.transactionDao().delete(transaction);
        });
    }

    public void markAsPaid(Transaction transaction) {
        transaction.isLoaned = false;
        AppDatabase.databaseWriteExecutor.execute(() -> {
            db.transactionDao().update(transaction);
        });
    }
}
