package com.example.keshe.notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.example.keshe.entity.Task;

import java.util.Date;

/**
 * 负责为任务安排/取消到期提醒
 */
public class ReminderScheduler {

    /**
     * 为任务安排提醒（简单版：到期时间点提醒一次）
     *scheduleReminder() 方法*/

    public static void scheduleReminder(Context context, Task task) {
        Date dueDate = task.getDueDate();
        if (dueDate == null) return;

        int offsetMinutes = task.getRemindOffsetMinutes();
        long triggerAtMillis = dueDate.getTime() - offsetMinutes * 60_000L;
        long now = System.currentTimeMillis();
        // 如果时间已过，就不再设置提醒
        if (triggerAtMillis <= now) return;

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, TaskReminderReceiver.class);
        intent.putExtra(TaskReminderReceiver.EXTRA_TASK_TITLE, task.getTitle());
        intent.putExtra(TaskReminderReceiver.EXTRA_TASK_DESC, task.getDescription());
        intent.putExtra(TaskReminderReceiver.EXTRA_NOTIFICATION_ID, task.getNotificationId());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                task.getNotificationId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Android 12+ 需要 SCHEDULE_EXACT_ALARM 权限才能使用精确闹钟
        // 如果没有权限，降级使用 setAndAllowWhileIdle（可能不够精确，但不会崩溃）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            } else {
                // 没有精确闹钟权限，使用非精确版本（系统可能会延迟几分钟）
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            }
        } else {
            // Android 11 及以下，直接使用精确闹钟
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
        }
    }

    /**
     * 取消任务对应的提醒
     */
    public static void cancelReminder(Context context, Task task) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, TaskReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                task.getNotificationId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(pendingIntent);
    }
}


