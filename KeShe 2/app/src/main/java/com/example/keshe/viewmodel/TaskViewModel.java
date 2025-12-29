package com.example.keshe.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.keshe.entity.Task;
import com.example.keshe.repository.TaskRepository;

import java.util.List;

public class TaskViewModel extends AndroidViewModel {
    private TaskRepository repository;
    private LiveData<List<Task>> allTasks;

    public TaskViewModel(Application application) {
        super(application);
        repository = new TaskRepository(application);
        allTasks = repository.getAllTasks();
    }

    public LiveData<List<Task>> getAllTasks() {
        return allTasks;
    }

    public LiveData<List<Task>> getTasksByStatus(int status) {
        return repository.getTasksByStatus(status);
    }

    public LiveData<List<Task>> getTasksByCategory(String category) {
        return repository.getTasksByCategory(category);
    }

    public LiveData<List<Task>> getCompletedHistory() {
        return repository.getCompletedHistory();
    }

    public void insert(Task task) {
        repository.insert(task);
    }

    public void update(Task task) {
        repository.update(task);
    }

    public void delete(Task task) {
        repository.delete(task);
    }

    public void updateTaskStatus(int id, int status) {
        repository.updateTaskStatus(id, status);
    }

    public Task getTaskByIdSync(int id) {
        return repository.getTaskByIdSync(id);
    }
}