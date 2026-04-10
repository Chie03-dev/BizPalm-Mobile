package project.bizpalm.data.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import project.bizpalm.data.entities.User;

@Dao
public interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insert(User user);

    @Update
    void update(User user);

    @Query("SELECT * FROM users WHERE username = :username AND pin = :pin")
    User login(String username, String pin);

    @Query("SELECT COUNT(*) FROM users")
    int getUserCount();

    @Query("SELECT * FROM users WHERE username = :username")
    User getUserByUsername(String username);
}
