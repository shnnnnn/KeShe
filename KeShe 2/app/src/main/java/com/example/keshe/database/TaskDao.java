package com.example.keshe.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.keshe.entity.Task;

import java.util.Date;
import java.util.List;

@Dao
public interface TaskDao {
    @Insert
    void insert(Task task);

    @Update
    void update(Task task);

    @Delete
    void delete(Task task);

    @Query("DELETE FROM tasks")
    void deleteAllTasks();

    @Query("SELECT * FROM tasks ORDER BY due_date ASC")
    LiveData<List<Task>> getAllTasks();

    @Query("SELECT * FROM tasks WHERE status = :status ORDER BY due_date ASC")
    LiveData<List<Task>> getTasksByStatus(int status);

    @Query("SELECT * FROM tasks WHERE category = :category ORDER BY due_date ASC")
    LiveData<List<Task>> getTasksByCategory(String category);

    @Query("SELECT * FROM tasks WHERE status = 2 ORDER BY completed_at DESC")
    LiveData<List<Task>> getCompletedHistory();

    @Query("SELECT * FROM tasks WHERE id = :id")
    Task getTaskById(int id);

    @Query("UPDATE tasks SET status = :status WHERE id = :id")
    void updateTaskStatus(int id, int status);

    @Query("SELECT * FROM tasks WHERE due_date BETWEEN :start AND :end")
    LiveData<List<Task>> getTasksBetweenDates(Date start, Date end);
}