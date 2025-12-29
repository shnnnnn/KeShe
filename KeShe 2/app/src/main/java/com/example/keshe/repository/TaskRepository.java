package com.example.keshe.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.keshe.database.AppDatabase;
import com.example.keshe.database.TaskDao;
import com.example.keshe.entity.Task;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskRepository {
    private TaskDao taskDao;
    private LiveData<List<Task>> allTasks;
    private ExecutorService executor;

    public TaskRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        taskDao = database.taskDao();
        allTasks = taskDao.getAllTasks();
        executor = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Task>> getAllTasks() {
        return allTasks;
    }

    public void insert(Task task) {
        executor.execute(() -> {
            taskDao.insert(task);
        });
    }

    public void update(Task task) {
        executor.execute(() -> {
            taskDao.update(task);
        });
    }

    public void delete(Task task) {
        executor.execute(() -> {
            taskDao.delete(task);
        });
    }

    public LiveData<List<Task>> getTasksByStatus(int status) {
        return taskDao.getTasksByStatus(status);
    }

    public LiveData<List<Task>> getTasksByCategory(String category) {
        return taskDao.getTasksByCategory(category);
    }

    public LiveData<List<Task>> getCompletedHistory() {
        return taskDao.getCompletedHistory();
    }

    public void updateTaskStatus(int id, int status) {
        executor.execute(() -> {
            taskDao.updateTaskStatus(id, status);
        });
    }

    public Task getTaskByIdSync(int id) {
        return taskDao.getTaskById(id);
    }
}