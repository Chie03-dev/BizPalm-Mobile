package project.bizpalm.data.database.dao;

import androidx.room.Dao;
import androidx.room.Embedded;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import project.bizpalm.data.entities.TransactionItem;

@Dao
public interface TransactionItemDao {
    @Insert
    void insertAll(List<TransactionItem> items);

    @Query("SELECT * FROM transaction_items WHERE transaction_id = :transactionId")
    List<TransactionItem> getItemsByTransactionId(int transactionId);

    @Query("SELECT ti.*, p.product_name as productName " +
           "FROM transaction_items ti " +
           "JOIN products p ON ti.product_id = p.id " +
           "WHERE ti.transaction_id = :transactionId")
    List<TransactionItemWithProduct> getItemsWithProductByTransactionIdSync(int transactionId);

    @Query("SELECT transaction_id, product_id FROM transaction_items ORDER BY transaction_id")
    List<TransactionProductId> getAllTransactionProductIds();

    class TransactionProductId {
        public int transaction_id;
        public int product_id;
    }

    class TransactionItemWithProduct {
        @Embedded
        public TransactionItem item;
        public String productName;
    }
}
