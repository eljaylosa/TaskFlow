package com.example.prototype;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.prototype.model.Task;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddTaskActivity extends AppCompatActivity {

    EditText etTitle, etDescription;
    Button btnDate, btnTime, btnSave;
    Calendar calendar;
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    String selectedDate = "";
    String selectedTime = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        etTitle = findViewById(R.id.etTaskTitle);
        etDescription = findViewById(R.id.etTaskDescription);
        btnDate = findViewById(R.id.btnSelectDate);
        btnTime = findViewById(R.id.btnSelectTime);
        btnSave = findViewById(R.id.btnSaveTask);

        calendar = Calendar.getInstance();

        btnDate.setOnClickListener(v -> showDatePicker());
        btnTime.setOnClickListener(v -> showTimePicker());

        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String description = etDescription.getText().toString().trim();

            if (title.isEmpty()) {
                Toast.makeText(AddTaskActivity.this, "Title cannot be empty.", Toast.LENGTH_SHORT).show();
                return;
            }

            MyDatabaseHelper dbHelper = MyDatabaseHelper.getInstance(AddTaskActivity.this);
            SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
            int userId = sharedPreferences.getInt("USER_ID", -1);

            if (userId != -1) {
                String category = "No Date";
                Task newTask = new Task(title, description, selectedDate, selectedTime, category, false);
                long result = dbHelper.addTask(newTask, userId);

                if (result != -1) {
                    if (!selectedDate.isEmpty() && !selectedTime.isEmpty()) {
                        try {
                            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                            Calendar alarmCalendar = Calendar.getInstance();
                            alarmCalendar.setTime(dateTimeFormat.parse(selectedDate + " " + selectedTime));
                            scheduleAlarm(alarmCalendar.getTimeInMillis(), (int) result, title, description);
                        } catch (ParseException e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Error parsing date/time for alarm.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    Toast.makeText(AddTaskActivity.this, "Task saved.", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(AddTaskActivity.this, "Failed to save task.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(AddTaskActivity.this, "Error: User not logged in.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDatePicker() {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePicker = new DatePickerDialog(
                this,
                (DatePicker view, int selectedYear, int selectedMonth, int selectedDay) -> {
                    calendar.set(Calendar.YEAR, selectedYear);
                    calendar.set(Calendar.MONTH, selectedMonth);
                    calendar.set(Calendar.DAY_OF_MONTH, selectedDay);

                    selectedDate = dateFormat.format(calendar.getTime());
                    btnDate.setText(selectedDate);
                },
                year, month, day
        );
        datePicker.show();
    }

    private void showTimePicker() {
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePicker = new TimePickerDialog(
                this,
                (TimePicker view, int selectedHour, int selectedMinute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                    calendar.set(Calendar.MINUTE, selectedMinute);

                    selectedTime = timeFormat.format(calendar.getTime());
                    btnTime.setText(selectedTime);
                },
                hour, minute, true // 24-hour format
        );
        timePicker.show();
    }

    private void scheduleAlarm(long timeInMillis, int taskId, String taskTitle, String taskDescription) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("task_title", taskTitle);
        intent.putExtra("task_description", taskDescription);
        intent.putExtra("is_reminder", false);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, taskId, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Intent reminderIntent = new Intent(this, AlarmReceiver.class);
        reminderIntent.putExtra("task_title", taskTitle);
        reminderIntent.putExtra("task_description", taskDescription);
        reminderIntent.putExtra("is_reminder", true);
        PendingIntent reminderPendingIntent = PendingIntent.getBroadcast(this, taskId + 1000000, reminderIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        long reminderTimeInMillis = timeInMillis - (24 * 60 * 60 * 1000);

        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
                    if (reminderTimeInMillis > System.currentTimeMillis()) {
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTimeInMillis, reminderPendingIntent);
                    }
                } else {
                    Intent settingsIntent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    startActivity(settingsIntent);
                    Toast.makeText(this, "Please grant permission to schedule exact alarms.", Toast.LENGTH_LONG).show();
                }
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
                if (reminderTimeInMillis > System.currentTimeMillis()) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTimeInMillis, reminderPendingIntent);
                }
            }
        }
    }
}
