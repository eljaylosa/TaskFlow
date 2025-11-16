package com.example.prototype;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TaskUtils {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public static String categorizeTask(String dateString) {
        try {
            Date taskDate = DATE_FORMAT.parse(dateString);
            if (taskDate == null) return "Upcoming";

            Calendar now = Calendar.getInstance();
            Calendar taskCal = Calendar.getInstance();
            taskCal.setTime(taskDate);

            // Set Monday as first day of the week
            now.setFirstDayOfWeek(Calendar.MONDAY);
            taskCal.setFirstDayOfWeek(Calendar.MONDAY);

            if (isThisWeek(taskCal, now)) {
                return "This Week";
            } else if (isNextWeek(taskCal, now)) {
                return "Next Week";
            } else {
                return "Upcoming";
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return "Upcoming"; // fallback
    }

    private static boolean isThisWeek(Calendar taskCal, Calendar now) {
        int currentWeek = now.get(Calendar.WEEK_OF_YEAR);
        int currentYear = now.get(Calendar.YEAR);
        int taskWeek = taskCal.get(Calendar.WEEK_OF_YEAR);
        int taskYear = taskCal.get(Calendar.YEAR);

        return currentYear == taskYear && currentWeek == taskWeek;
    }

    private static boolean isNextWeek(Calendar taskCal, Calendar now) {
        int currentWeek = now.get(Calendar.WEEK_OF_YEAR);
        int currentYear = now.get(Calendar.YEAR);
        int taskWeek = taskCal.get(Calendar.WEEK_OF_YEAR);
        int taskYear = taskCal.get(Calendar.YEAR);

        // Handle year change
        if (taskYear > currentYear) {
            taskWeek += 52;
        }

        return taskWeek == currentWeek + 1;
    }
}
