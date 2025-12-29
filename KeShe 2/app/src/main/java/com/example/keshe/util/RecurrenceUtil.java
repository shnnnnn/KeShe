package com.example.keshe.util;

import java.util.Calendar;
import java.util.Date;

/**
 * 简单的循环任务下一次时间计算
 */
public class RecurrenceUtil {

    /**
     * 根据模式返回下一次截止时间（每天/每周/每月/每年）
     */
    public static Date getNextDueDate(Date current, String recurrencePattern) {
        if (current == null || recurrencePattern == null) return null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(current);
        switch (recurrencePattern) {
            case "每天":
                cal.add(Calendar.DAY_OF_MONTH, 1);
                break;
            case "每周":
                cal.add(Calendar.WEEK_OF_YEAR, 1);
                break;
            case "每月":
                cal.add(Calendar.MONTH, 1);
                break;
            case "每年":
                cal.add(Calendar.YEAR, 1);
                break;
            default:
                return null;
        }
        return cal.getTime();
    }
}


