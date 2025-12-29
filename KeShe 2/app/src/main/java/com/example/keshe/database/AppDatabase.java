package com.example.keshe.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.keshe.entity.Task;
import com.example.keshe.entity.User; // 添加User导入

@Database(entities = {Task.class, User.class}, version = 6, exportSchema = false) // 版本号改为6，添加startDate字段
@TypeConverters({DateConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    public abstract TaskDao taskDao();
    public abstract UserDao userDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "task_database")
                    .fallbackToDestructiveMigration() // 这会清除旧数据，开发阶段可用
                    .build();
        }
        return instance;
    }
}