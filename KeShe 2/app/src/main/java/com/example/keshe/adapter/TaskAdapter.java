package com.example.keshe.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.keshe.R;
import com.example.keshe.entity.Task;
import com.example.keshe.notification.ReminderScheduler;
import com.example.keshe.util.RecurrenceUtil;
import com.example.keshe.viewmodel.TaskViewModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private List<Task> tasks;
    private TaskViewModel taskViewModel;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    private OnTaskClickListener listener;

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
        void onEditClick(Task task);
    }

    public void setOnTaskClickListener(OnTaskClickListener listener) {
        this.listener = listener;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
        notifyDataSetChanged();
    }

    public void setViewModel(TaskViewModel viewModel) {
        this.taskViewModel = viewModel;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_item, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);

        holder.tvTitle.setText(task.getTitle());
        holder.tvDescription.setText(task.getDescription());
        holder.tvCategory.setText(task.getCategory());

        // 显示截止时间或完成时间
        if (task.getStatus() == 2 && task.getCompletedAt() != null) {
            holder.tvDueDate.setText("完成于 " + dateFormat.format(task.getCompletedAt()));
        } else if (task.getDueDate() != null) {
            holder.tvDueDate.setText(dateFormat.format(task.getDueDate()));
        } else {
            holder.tvDueDate.setText("无截止时间");
        }

        // 设置状态
        updateStatusUI(holder, task.getStatus());

        // 设置优先级颜色
        int priorityColor = R.color.gray;
        switch (task.getPriority()) {
            case 1:
                priorityColor = R.color.green;
                break;
            case 2:
                priorityColor = R.color.yellow;
                break;
            case 3:
                priorityColor = R.color.red;
                break;
        }
        holder.priorityIndicator.setBackgroundResource(priorityColor);

        // 设置分类颜色
        int categoryColor = R.color.gray;
        switch (task.getCategory()) {
            case "工作":
                categoryColor = R.color.blue;
                break;
            case "学习":
                categoryColor = R.color.green;
                break;
            case "生活":
                categoryColor = R.color.orange;
                break;
        }
        holder.tvCategory.setBackgroundResource(categoryColor);

        // 状态切换按钮点击事件
        holder.btnToggleStatus.setOnClickListener(v -> {
            if (taskViewModel != null) {
                int oldStatus = task.getStatus();
                int newStatus = (oldStatus + 1) % 3; // 0→1→2→0循环
                task.setStatus(newStatus);

                // 记录 / 清除完成时间
                if (newStatus == 2) {
                    task.setCompletedAt(new Date());
                    // 若为循环任务，且当前时间已超过截止时间，才生成下一次任务
                    if (task.isRecurring() && task.getDueDate() != null) {
                        Date now = new Date();
                        // 只有当前时间超过截止时间，才生成下一个任务
                        if (now.after(task.getDueDate())) {
                            Date nextDue = RecurrenceUtil.getNextDueDate(task.getDueDate(), task.getRecurrencePattern());
                            if (nextDue != null) {
                                Task next = new Task();
                                next.setTitle(task.getTitle());
                                next.setDescription(task.getDescription());
                                next.setCategory(task.getCategory());
                                next.setPriority(task.getPriority());
                                // 新任务的开始时间 = 当前任务的截止时间
                                next.setStartDate(task.getDueDate());
                                next.setDueDate(nextDue);
                                next.setStatus(0);
                                next.setRecurring(true);
                                next.setRecurrencePattern(task.getRecurrencePattern());
                                next.setRemindOffsetMinutes(task.getRemindOffsetMinutes());
                                // 重新调度提醒
                                ReminderScheduler.scheduleReminder(v.getContext(), next);
                                taskViewModel.insert(next);
                            }
                        }
                    }
                } else if (oldStatus == 2 && newStatus != 2) {
                    task.setCompletedAt(null);
                }

                taskViewModel.update(task);
                updateStatusUI(holder, newStatus);
                // 同步更新完成时间显示
                if (newStatus == 2 && task.getCompletedAt() != null) {
                    holder.tvDueDate.setText("完成于 " + dateFormat.format(task.getCompletedAt()));
                } else if (task.getDueDate() != null) {
                    holder.tvDueDate.setText(dateFormat.format(task.getDueDate()));
                } else {
                    holder.tvDueDate.setText("无截止时间");
                }
            }
        });

        // 编辑按钮点击事件
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(task);
            }
        });

        // 分享按钮点击事件
        holder.btnShare.setOnClickListener(v -> {
            String shareText = buildShareText(task);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "任务分享");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            v.getContext().startActivity(Intent.createChooser(shareIntent, "分享任务"));
        });

        // 删除按钮点击事件
        holder.btnDelete.setOnClickListener(v -> {
            if (taskViewModel != null) {
                // 先取消提醒
                Context context = v.getContext();
                ReminderScheduler.cancelReminder(context, task);
                taskViewModel.delete(task);
            }
        });

        // 点击任务项
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTaskClick(task);
            }
        });

        // 长按编辑
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(task);
                return true;
            }
            return false;
        });
    }

    private void updateStatusUI(TaskViewHolder holder, int status) {
        String statusText = "";
        int statusColor = 0;

        switch (status) {
            case 0:
                statusText = "未开始";
                statusColor = android.R.color.darker_gray;
                break;
            case 1:
                statusText = "进行中";
                statusColor = R.color.blue;
                break;
            case 2:
                statusText = "已完成";
                statusColor = R.color.green;
                break;
        }

        holder.tvStatus.setText(statusText);
        holder.tvStatus.setBackgroundResource(statusColor);

        // 根据状态设置按钮图标
        int iconResource;
        switch (status) {
            case 0:
                iconResource = android.R.drawable.ic_media_play;
                break;
            case 1:
                iconResource = android.R.drawable.ic_media_pause;
                break;
            case 2:
                iconResource = android.R.drawable.checkbox_on_background;
                break;
            default:
                iconResource = android.R.drawable.ic_media_play;
        }
        holder.btnToggleStatus.setImageResource(iconResource);
    }

    @Override
    public int getItemCount() {
        return tasks == null ? 0 : tasks.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvCategory, tvDueDate, tvStatus;
        View priorityIndicator;
        ImageButton btnToggleStatus, btnEdit, btnDelete, btnShare;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvDueDate = itemView.findViewById(R.id.tv_due_date);
            tvStatus = itemView.findViewById(R.id.tv_status);
            priorityIndicator = itemView.findViewById(R.id.priority_indicator);
            btnToggleStatus = itemView.findViewById(R.id.btn_toggle_status);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            btnShare = itemView.findViewById(R.id.btn_share);
        }
    }

    private String buildShareText(Task task) {
        StringBuilder sb = new StringBuilder();
        sb.append("任务: ").append(task.getTitle()).append("\n");
        if (task.getDescription() != null) sb.append("描述: ").append(task.getDescription()).append("\n");
        if (task.getCategory() != null) sb.append("分类: ").append(task.getCategory()).append("\n");
        sb.append("优先级: ").append(task.getPriority()).append("\n");
        if (task.getDueDate() != null) sb.append("截止: ").append(dateFormat.format(task.getDueDate())).append("\n");
        sb.append("状态: ").append(task.getStatus()).append("\n");
        return sb.toString();
    }
}