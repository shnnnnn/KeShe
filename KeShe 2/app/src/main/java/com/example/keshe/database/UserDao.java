package com.example.keshe.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.keshe.entity.User;

import java.util.List;

@Dao
public interface UserDao {
    @Insert
    long insert(User user);

    @Update
    void update(User user);

    @Query("SELECT * FROM users WHERE email = :email")
    User getUserByEmail(String email);
    
    @Query("SELECT * FROM users WHERE username = :username")
    User getUserByUsername(String username);

    @Query("SELECT * FROM users WHERE id = :id")
    User getUserById(int id);

    @Query("SELECT * FROM users WHERE email = :email AND password = :password")
    User login(String email, String password);

    @Query("SELECT * FROM users")
    LiveData<List<User>> getAllUsers();

    @Query("SELECT * FROM users WHERE email LIKE :search OR username LIKE :search")
    LiveData<List<User>> searchUsers(String search);
}