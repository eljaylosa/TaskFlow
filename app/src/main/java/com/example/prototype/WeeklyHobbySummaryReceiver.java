package com.example.prototype;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.example.prototype.model.Hobby;
import com.example.prototype.model.User;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class WeeklyHobbySummaryReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "hobby_summary_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        MyDatabaseHelper dbHelper = MyDatabaseHelper.getInstance(context);
        SharedPreferences sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        int userId = sharedPreferences.getInt("USER_ID", -1);

        if (userId != -1) {
            User user = dbHelper.getUser(userId);
            String userName = (user != null) ? user.getName().split(" ")[0] : "";

            List<Hobby> hobbies = dbHelper.getAllHobbies(userId);
            if (hobbies.isEmpty()) {
                return; // No hobbies, no summary needed
            }

            StringBuilder summaryText = new StringBuilder();
            int totalCheckIns = 0;
            int totalPossibleCheckIns = 0;

            // Get check-ins from the last 7 days
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, -7);
            Date oneWeekAgo = cal.getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            for (Hobby hobby : hobbies) {
                List<String> checkInDatesStr = dbHelper.getHobbyCheckIns(hobby.getId());
                int weeklyCheckIns = 0;
                for (String dateStr : checkInDatesStr) {
                    try {
                        Date checkInDate = sdf.parse(dateStr);
                        if (checkInDate != null && checkInDate.after(oneWeekAgo)) {
                            weeklyCheckIns++;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                summaryText.append(hobby.getName()).append(": ").append(weeklyCheckIns).append("/7\n");
                totalCheckIns += weeklyCheckIns;
                totalPossibleCheckIns += 7;
            }

            if (totalPossibleCheckIns > 0) {
                 createAndShowNotification(context, userName, summaryText.toString(), totalCheckIns, totalPossibleCheckIns);
            }
        }
    }

    private void createAndShowNotification(Context context, String userName, String summary, int totalCheckIns, int totalPossible) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Hobby Summary Channel";
            String description = "Channel for weekly hobby summary";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        Intent notificationIntent = new Intent(context, SplashActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        String contentTitle = "Hi " + userName + ", here's your weekly hobby summary!";
        String bigText = "Overall: " + totalCheckIns + "/" + totalPossible + " check-ins.\n\n" + summary;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_hobby)
                .setContentTitle(contentTitle)
                .setContentText("Overall: " + totalCheckIns + "/" + totalPossible + " check-ins.")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(bigText))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(2, builder.build()); // Using a fixed ID 2 for the summary notification
    }
}