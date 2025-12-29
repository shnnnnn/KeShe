package com.example.keshe.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.keshe.database.AppDatabase;
import com.example.keshe.database.UserDao;
import com.example.keshe.entity.User;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserRepository {
    private UserDao userDao;
    private LiveData<List<User>> allUsers;
    private ExecutorService executor;

    public UserRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        userDao = database.userDao();
        allUsers = userDao.getAllUsers();
        executor = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<User>> getAllUsers() {
        return allUsers;
    }

    public interface InsertCallback {
        void onResult(User user);
    }

    public void insert(User user, InsertCallback callback) {
        executor.execute(() -> {
            long id = userDao.insert(user);
            user.setId((int) id);
            // 使用Handler切换到主线程执行回调
            android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
            mainHandler.post(() -> callback.onResult(user));
        });
    }

    public void update(User user) {
        executor.execute(() -> {
            userDao.update(user);
        });
    }

    public interface LoginCallback {
        void onResult(User user);
    }

    public void login(String email, String password, LoginCallback callback) {
        executor.execute(() -> {
            User user = userDao.login(email, password);
            // 使用Handler切换到主线程执行回调
            android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
            mainHandler.post(() -> callback.onResult(user));
        });
    }

    public interface GetUserCallback {
        void onResult(User user);
    }

    public void getUserByEmail(String email, GetUserCallback callback) {
        executor.execute(() -> {
            User user = userDao.getUserByEmail(email);
            // 使用Handler切换到主线程执行回调
            android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
            mainHandler.post(() -> callback.onResult(user));
        });
    }
    
    public void getUserByUsername(String username, GetUserCallback callback) {
        executor.execute(() -> {
            User user = userDao.getUserByUsername(username);
            // 使用Handler切换到主线程执行回调
            android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
            mainHandler.post(() -> callback.onResult(user));
        });
    }

    public User getUserByIdSync(int id) {
        return userDao.getUserById(id);
    }

    public LiveData<List<User>> searchUsers(String search) {
        return userDao.searchUsers("%" + search + "%");
    }
}