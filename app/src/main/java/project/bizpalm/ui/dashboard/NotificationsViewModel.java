package project.bizpalm.ui.dashboard;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import project.bizpalm.data.database.AppDatabase;
import project.bizpalm.data.database.dao.NotificationDao;
import project.bizpalm.data.entities.Notification;

public class NotificationsViewModel extends AndroidViewModel {

    private final NotificationDao notificationDao;
    private final LiveData<List<Notification>> allNotifications;
    private final LiveData<Integer> unreadCount;

    public NotificationsViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        notificationDao = db.notificationDao();
        allNotifications = notificationDao.getAllNotifications();
        unreadCount = notificationDao.getUnreadNotificationCount();
    }

    public LiveData<List<Notification>> getAllNotifications() {
        return allNotifications;
    }

    public LiveData<Integer> getUnreadCount() {
        return unreadCount;
    }

    public void insert(Notification notification) {
        AppDatabase.databaseWriteExecutor.execute(() -> notificationDao.insert(notification));
    }
}
