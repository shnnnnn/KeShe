package com.example.keshe.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.keshe.entity.User;
import com.example.keshe.repository.UserRepository;

import java.util.List;

public class UserViewModel extends AndroidViewModel {
    private UserRepository repository;
    private LiveData<List<User>> allUsers;
    private MutableLiveData<User> currentUser = new MutableLiveData<>();

    public UserViewModel(Application application) {
        super(application);
        repository = new UserRepository(application);
        allUsers = repository.getAllUsers();
    }

    public LiveData<List<User>> getAllUsers() {
        return allUsers;
    }

    public interface InsertCallback {
        void onResult(User user);
    }

    public void insert(User user, InsertCallback callback) {
        repository.insert(user, new UserRepository.InsertCallback() {
            @Override
            public void onResult(User user) {
                callback.onResult(user);
            }
        });
    }

    public void update(User user) {
        repository.update(user);
    }

    public interface LoginCallback {
        void onResult(User user);
    }

    public void login(String email, String password, LoginCallback callback) {
        repository.login(email, password, new UserRepository.LoginCallback() {
            @Override
            public void onResult(User user) {
                callback.onResult(user);
            }
        });
    }

    public interface GetUserCallback {
        void onResult(User user);
    }

    public void getUserByEmail(String email, GetUserCallback callback) {
        repository.getUserByEmail(email, new UserRepository.GetUserCallback() {
            @Override
            public void onResult(User user) {
                callback.onResult(user);
            }
        });
    }
    
    public void getUserByUsername(String username, GetUserCallback callback) {
        repository.getUserByUsername(username, new UserRepository.GetUserCallback() {
            @Override
            public void onResult(User user) {
                callback.onResult(user);
            }
        });
    }

    public User getUserByIdSync(int id) {
        return repository.getUserByIdSync(id);
    }

    public LiveData<List<User>> searchUsers(String search) {
        return repository.searchUsers(search);
    }

    public MutableLiveData<User> getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        currentUser.setValue(user);
    }

    public void logout() {
        currentUser.setValue(null);
    }
}