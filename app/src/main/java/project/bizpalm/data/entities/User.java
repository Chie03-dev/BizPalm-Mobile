package project.bizpalm.data.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "username")
    public String username;

    @ColumnInfo(name = "pin")
    public String pin;

    @ColumnInfo(name = "role")
    public String role; // "OWNER" or "EMPLOYEE"

    public User(@NonNull String username, String pin, String role) {
        this.username = username;
        this.pin = pin;
        this.role = role;
    }
}
