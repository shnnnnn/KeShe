package com.example.keshe.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.keshe.R;

/**
 * 任务到期提醒的广播接收器
 */
public class TaskReminderReceiver extends BroadcastReceiver {

    public static final String EXTRA_TASK_TITLE = "extra_task_title";
    public static final String EXTRA_TASK_DESC = "extra_task_desc";
    public static final String EXTRA_NOTIFICATION_ID = "extra_notification_id";
    private static final String CHANNEL_ID = "task_reminder_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra(EXTRA_TASK_TITLE);
        String desc = intent.getStringExtra(EXTRA_TASK_DESC);
        int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, (int) System.currentTimeMillis());

        // Android 13+ 通知权限检查，未授权则直接返回，避免崩溃
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        createNotificationChannel(context);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title != null ? title : "任务提醒")
                .setContentText(desc != null ? desc : "您有一个任务到期了")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        manager.notify(notificationId, builder.build());
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "任务提醒";
            String description = "任务到期时提醒通知";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}


