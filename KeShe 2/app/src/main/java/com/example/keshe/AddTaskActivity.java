package com.example.keshe;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.keshe.entity.Task;
import com.example.keshe.notification.ReminderScheduler;
import com.example.keshe.viewmodel.TaskViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

public class AddTaskActivity extends AppCompatActivity {

    private TextInputEditText etTitle, etDescription;
    private RadioGroup rgCategory, rgPriority;
    private Button btnSelectStartDate, btnSelectEndDate, btnSave;
    private TextView tvSelectedStartDate, tvSelectedEndDate;
    private Spinner spinnerRemindOffset, spinnerRecurrence;
    private CheckBox cbRecurring;

    private TaskViewModel taskViewModel;
    private Calendar startCalendar = Calendar.getInstance();
    private Calendar endCalendar = Calendar.getInstance();

    private int editingTaskId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // 初始化视图
        etTitle = findViewById(R.id.et_title);
        etDescription = findViewById(R.id.et_description);
        rgCategory = findViewById(R.id.rg_category);
        rgPriority = findViewById(R.id.rg_priority);
        btnSelectStartDate = findViewById(R.id.btn_select_start_date);
        btnSelectEndDate = findViewById(R.id.btn_select_date);
        tvSelectedStartDate = findViewById(R.id.tv_selected_start_date);
        tvSelectedEndDate = findViewById(R.id.tv_selected_date);
        btnSave = findViewById(R.id.btn_save);
        spinnerRemindOffset = findViewById(R.id.spinner_remind_offset);
        cbRecurring = findViewById(R.id.cb_recurring);
        spinnerRecurrence = findViewById(R.id.spinner_recurrence);

        taskViewModel = new TaskViewModel(getApplication());

        // 设置提前提醒 Spinner 适配器
        String[] remindOptions = {"不提醒", "提前5分钟", "提前10分钟", "提前15分钟", "提前30分钟", "提前1小时"};
        ArrayAdapter<String> remindAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                remindOptions
        );
        remindAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRemindOffset.setAdapter(remindAdapter);

        // 如果是编辑任务
        editingTaskId = getIntent().getIntExtra("task_id", -1);
        if (editingTaskId != -1) {
            loadTask(editingTaskId);
        }

        // 点击选择开始时间
        btnSelectStartDate.setOnClickListener(v -> pickDateTime(startCalendar, tvSelectedStartDate));

        // 点击选择截止时间
        btnSelectEndDate.setOnClickListener(v -> pickDateTime(endCalendar, tvSelectedEndDate));

        // 保存按钮
        btnSave.setOnClickListener(v -> saveTask());
    }

    private void loadTask(int taskId) {
        new Thread(() -> {
            Task task = taskViewModel.getTaskByIdSync(taskId);
            runOnUiThread(() -> {
                if (task == null) return;

                etTitle.setText(task.getTitle());
                etDescription.setText(task.getDescription());

                // 分类
                if ("工作".equals(task.getCategory())) rgCategory.check(R.id.rb_work);
                else if ("学习".equals(task.getCategory())) rgCategory.check(R.id.rb_study);
                else rgCategory.check(R.id.rb_life);

                // 优先级
                if (task.getPriority() == 3) rgPriority.check(R.id.rb_high);
                else if (task.getPriority() == 2) rgPriority.check(R.id.rb_medium);
                else rgPriority.check(R.id.rb_low);

                // 开始时间
                if (task.getStartDate() != null) {
                    startCalendar.setTime(task.getStartDate());
                    tvSelectedStartDate.setText(android.text.format.DateFormat.format("yyyy-MM-dd HH:mm", startCalendar));
                }

                // 截止时间
                if (task.getDueDate() != null) {
                    endCalendar.setTime(task.getDueDate());
                    tvSelectedEndDate.setText(android.text.format.DateFormat.format("yyyy-MM-dd HH:mm", endCalendar));
                }

                // 提前提醒
                int offset = task.getRemindOffsetMinutes();
                if (offset == 0) spinnerRemindOffset.setSelection(0);
                else if (offset == 5) spinnerRemindOffset.setSelection(1);
                else if (offset == 10) spinnerRemindOffset.setSelection(2);
                else if (offset == 15) spinnerRemindOffset.setSelection(3);
                else if (offset == 30) spinnerRemindOffset.setSelection(4);
                else if (offset == 60) spinnerRemindOffset.setSelection(5);
                else spinnerRemindOffset.setSelection(0);
            });
        }).start();
    }

    private void pickDateTime(Calendar calendar, TextView targetTextView) {
        // 先选择日期
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    // 再选择时间
                    TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                            (timeView, hourOfDay, minute) -> {
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                calendar.set(Calendar.MINUTE, minute);
                                targetTextView.setText(android.text.format.DateFormat.format("yyyy-MM-dd HH:mm", calendar));
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true);
                    timePickerDialog.show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void saveTask() {
        String title = etTitle.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(this, "请输入任务标题", Toast.LENGTH_SHORT).show();
            return;
        }

        Task task = new Task();
        task.setTitle(title);
        task.setDescription(etDescription.getText().toString().trim());
        task.setStartDate(startCalendar.getTime());
        task.setDueDate(endCalendar.getTime());
        task.setStatus(0);

        // 分类
        int catId = rgCategory.getCheckedRadioButtonId();
        if (catId == R.id.rb_work) task.setCategory("工作");
        else if (catId == R.id.rb_study) task.setCategory("学习");
        else task.setCategory("生活");

        // 优先级
        int priId = rgPriority.getCheckedRadioButtonId();
        RadioButton checked = findViewById(priId);
        int index = rgPriority.indexOfChild(checked);
        if (index == 2) task.setPriority(3);
        else if (index == 1) task.setPriority(2);
        else task.setPriority(1);

        // 提前提醒
        int pos = spinnerRemindOffset.getSelectedItemPosition();
        switch (pos) {
            case 0: task.setRemindOffsetMinutes(0); break;
            case 1: task.setRemindOffsetMinutes(5); break;
            case 2: task.setRemindOffsetMinutes(10); break;
            case 3: task.setRemindOffsetMinutes(15); break;
            case 4: task.setRemindOffsetMinutes(30); break;
            case 5: task.setRemindOffsetMinutes(60); break;
            default: task.setRemindOffsetMinutes(0);
        }

        if (editingTaskId == -1) {
            taskViewModel.insert(task);
            ReminderScheduler.scheduleReminder(this, task);
            Toast.makeText(this, "任务已添加并设置提醒", Toast.LENGTH_SHORT).show();
        } else {
            task.setId(editingTaskId);
            ReminderScheduler.cancelReminder(this, task);
            taskViewModel.update(task);
            ReminderScheduler.scheduleReminder(this, task);
            Toast.makeText(this, "任务已更新，提醒已刷新", Toast.LENGTH_SHORT).show();
        }

        finish();
    }
}