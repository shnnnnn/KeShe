package com.example.keshe;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.keshe.adapter.TaskAdapter;
import com.example.keshe.entity.Task;
import com.example.keshe.notification.ReminderScheduler;
import com.example.keshe.viewmodel.TaskViewModel;
import com.example.keshe.viewmodel.UserViewModel;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TaskViewModel taskViewModel;
    private UserViewModel userViewModel;
    private TaskAdapter taskAdapter;
    private RecyclerView recyclerView;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private SharedPreferences sharedPreferences;
    private MaterialButtonToggleGroup categoryGroup;
    private EditText etSearch;

    private List<Task> allCurrentTasks = new ArrayList<>();
    private String currentCategoryFilter = "全部";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 登录检查
        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        if (sharedPreferences.getInt("user_id", -1) == -1) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupViewModel();
        setupNavigationDrawer();
        setupFloatingButton();
        setupSearch();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recycler_view);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        categoryGroup = findViewById(R.id.category_group);
        etSearch = findViewById(R.id.et_search);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(navigationView));

        categoryGroup.check(R.id.btn_category_all);
        categoryGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;

            if (checkedId == R.id.btn_category_all) currentCategoryFilter = "全部";
            else if (checkedId == R.id.btn_category_work) currentCategoryFilter = "工作";
            else if (checkedId == R.id.btn_category_study) currentCategoryFilter = "学习";
            else if (checkedId == R.id.btn_category_life) currentCategoryFilter = "生活";

            applyFilters();
        });
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new TaskAdapter();
        taskAdapter.setViewModel(taskViewModel);
        recyclerView.setAdapter(taskAdapter);

        taskAdapter.setOnTaskClickListener(new TaskAdapter.OnTaskClickListener() {
            @Override
            public void onTaskClick(Task task) {
                Toast.makeText(MainActivity.this,
                        "任务：" + task.getTitle(),
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onEditClick(Task task) {
                Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
                intent.putExtra("task_id", task.getId());
                startActivity(intent);
            }
        });
    }

    private void setupViewModel() {
        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);
        userViewModel = new UserViewModel(getApplication());

        taskViewModel.getAllTasks().observe(this, tasks -> {
            updateTaskList(tasks);
            handleTaskReminders(tasks);
        });
    }

    private void setupNavigationDrawer() {
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_all_tasks) {
                taskViewModel.getAllTasks().observe(this, this::updateTaskList);
            } else if (id == R.id.nav_pending) {
                taskViewModel.getTasksByStatus(0).observe(this, this::updateTaskList);
            } else if (id == R.id.nav_completed) {
                taskViewModel.getCompletedHistory().observe(this, this::updateTaskList);
            } else if (id == R.id.nav_logout) {
                logout();
            }

            drawerLayout.closeDrawers();
            return true;
        });
    }

    private void setupFloatingButton() {
        FloatingActionButton fab = findViewById(R.id.fab_add_task);
        fab.setOnClickListener(v ->
                startActivity(new Intent(this, AddTaskActivity.class))
        );
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }

            @Override
            public void afterTextChanged(android.text.Editable s) { }
        });
    }

    private void updateTaskList(List<Task> tasks) {
        allCurrentTasks = tasks == null ? new ArrayList<>() : tasks;
        applyFilters();
    }

    private void applyFilters() {
        String keyword = etSearch.getText().toString().trim();
        List<Task> filtered = new ArrayList<>();

        for (Task task : allCurrentTasks) {
            boolean matchCategory = "全部".equals(currentCategoryFilter) || currentCategoryFilter.equals(task.getCategory());
            boolean matchKeyword = keyword.isEmpty() ||
                    (task.getTitle() != null && task.getTitle().contains(keyword)) ||
                    (task.getDescription() != null && task.getDescription().contains(keyword));
            if (matchCategory && matchKeyword) filtered.add(task);
        }

        taskAdapter.setTasks(filtered);
    }

    private void handleTaskReminders(List<Task> tasks) {
        if (tasks == null) return;

        for (Task task : tasks) {
            if (task.getStatus() == 2) {
                ReminderScheduler.cancelReminder(this, task);
            } else {
                ReminderScheduler.scheduleReminder(this, task);
            }
        }
    }

    private void logout() {
        sharedPreferences.edit().clear().apply();
        userViewModel.logout();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
        Toast.makeText(this, "已退出登录", Toast.LENGTH_SHORT).show();
    }
}