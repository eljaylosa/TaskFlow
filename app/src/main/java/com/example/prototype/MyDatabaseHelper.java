package com.example.prototype;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.prototype.model.Hobby;
import com.example.prototype.model.Mood;
import com.example.prototype.model.Task;
import com.example.prototype.model.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyDatabaseHelper extends SQLiteOpenHelper {
    private static MyDatabaseHelper instance;

    private static final String DATABASE_NAME = "prototype.db";
    private static final int DATABASE_VERSION = 3; // Incremented version

    // Table Names
    private static final String TABLE_USERS = "users";
    private static final String TABLE_TASKS = "tasks";
    private static final String TABLE_HOBBIES = "hobbies";
    private static final String TABLE_HOBBY_CHECKINS = "hobby_checkins";
    private static final String TABLE_MOODS = "moods";

    // User Table Columns
    private static final String COLUMN_USER_ID = "id";
    private static final String COLUMN_USER_FULL_NAME = "full_name";
    private static final String COLUMN_USER_PHONE = "phone";
    private static final String COLUMN_USER_EMAIL = "email";
    private static final String COLUMN_USER_PASSWORD = "password";
    private static final String COLUMN_USER_PROFILE_PIC = "profile_pic";

    // Task Table Columns
    private static final String COLUMN_TASK_ID = "id";
    private static final String COLUMN_TASK_USER_ID = "user_id";
    private static final String COLUMN_TASK_TITLE = "title";
    private static final String COLUMN_TASK_DESCRIPTION = "description";
    private static final String COLUMN_TASK_DATE = "date";
    private static final String COLUMN_TASK_TIME = "time";
    private static final String COLUMN_TASK_CATEGORY = "category";
    private static final String COLUMN_TASK_IS_COMPLETED = "is_completed";

    // Hobby Table Columns
    private static final String COLUMN_HOBBY_ID = "id";
    private static final String COLUMN_HOBBY_USER_ID = "user_id";
    private static final String COLUMN_HOBBY_NAME = "name";
    private static final String COLUMN_HOBBY_STREAK = "streak";
    private static final String COLUMN_HOBBY_LAST_CHECKED_IN = "last_checked_in";
    private static final String COLUMN_HOBBY_WEEK_START_DATE = "week_start_date"; // New Column


    // Hobby Check-in Table Columns
    private static final String COLUMN_CHECKIN_ID = "id";
    private static final String COLUMN_CHECKIN_HOBBY_ID = "hobby_id";
    private static final String COLUMN_CHECKIN_USER_ID = "user_id";
    private static final String COLUMN_CHECKIN_TIMESTAMP = "timestamp";

    // Mood Table Columns
    private static final String COLUMN_MOOD_ID = "id";
    private static final String COLUMN_MOOD_USER_ID = "user_id";
    private static final String COLUMN_MOOD_NAME = "name";
    private static final String COLUMN_MOOD_STICKER = "sticker";
    private static final String COLUMN_MOOD_TIMESTAMP = "timestamp";


    public static synchronized MyDatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new MyDatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private MyDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Tables
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USER_FULL_NAME + " TEXT,"
                + COLUMN_USER_PHONE + " TEXT,"
                + COLUMN_USER_EMAIL + " TEXT,"
                + COLUMN_USER_PASSWORD + " TEXT,"
                + COLUMN_USER_PROFILE_PIC + " TEXT" + ")";
        db.execSQL(CREATE_USERS_TABLE);

        String CREATE_TASKS_TABLE = "CREATE TABLE " + TABLE_TASKS + "("
                + COLUMN_TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TASK_USER_ID + " INTEGER,"
                + COLUMN_TASK_TITLE + " TEXT,"
                + COLUMN_TASK_DESCRIPTION + " TEXT,"
                + COLUMN_TASK_DATE + " TEXT,"
                + COLUMN_TASK_TIME + " TEXT,"
                + COLUMN_TASK_CATEGORY + " TEXT,"
                + COLUMN_TASK_IS_COMPLETED + " INTEGER DEFAULT 0,"
                + "FOREIGN KEY(" + COLUMN_TASK_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ")"
                + ")";
        db.execSQL(CREATE_TASKS_TABLE);

        String CREATE_HOBBIES_TABLE = "CREATE TABLE " + TABLE_HOBBIES + "("
                + COLUMN_HOBBY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_HOBBY_USER_ID + " INTEGER,"
                + COLUMN_HOBBY_NAME + " TEXT,"
                + COLUMN_HOBBY_STREAK + " INTEGER DEFAULT 0,"
                + COLUMN_HOBBY_LAST_CHECKED_IN + " TEXT,"
                + COLUMN_HOBBY_WEEK_START_DATE + " TEXT,"
                + "FOREIGN KEY(" + COLUMN_HOBBY_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ")"
                + ")";
        db.execSQL(CREATE_HOBBIES_TABLE);

        String CREATE_HOBBY_CHECKINS_TABLE = "CREATE TABLE " + TABLE_HOBBY_CHECKINS + "("
                + COLUMN_CHECKIN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_CHECKIN_HOBBY_ID + " INTEGER,"
                + COLUMN_CHECKIN_USER_ID + " INTEGER,"
                + COLUMN_CHECKIN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY(" + COLUMN_CHECKIN_HOBBY_ID + ") REFERENCES " + TABLE_HOBBIES + "(" + COLUMN_HOBBY_ID + "),"
                + "FOREIGN KEY(" + COLUMN_CHECKIN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ")"
                + ")";
        db.execSQL(CREATE_HOBBY_CHECKINS_TABLE);

        String CREATE_MOODS_TABLE = "CREATE TABLE " + TABLE_MOODS + "("
                + COLUMN_MOOD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_MOOD_USER_ID + " INTEGER,"
                + COLUMN_MOOD_NAME + " TEXT,"
                + COLUMN_MOOD_STICKER + " TEXT,"
                + COLUMN_MOOD_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY(" + COLUMN_MOOD_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ")"
                + ")";
        db.execSQL(CREATE_MOODS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_HOBBY_CHECKINS);
            String CREATE_HOBBY_CHECKINS_TABLE = "CREATE TABLE " + TABLE_HOBBY_CHECKINS + "("
                    + COLUMN_CHECKIN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_CHECKIN_HOBBY_ID + " INTEGER,"
                    + COLUMN_CHECKIN_USER_ID + " INTEGER,"
                    + COLUMN_CHECKIN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                    + "FOREIGN KEY(" + COLUMN_CHECKIN_HOBBY_ID + ") REFERENCES " + TABLE_HOBBIES + "(" + COLUMN_HOBBY_ID + "),"
                    + "FOREIGN KEY(" + COLUMN_CHECKIN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ")"
                    + ")";
            db.execSQL(CREATE_HOBBY_CHECKINS_TABLE);
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TABLE_HOBBIES + " ADD COLUMN " + COLUMN_HOBBY_WEEK_START_DATE + " TEXT");
        }
    }


    // --- User Methods ---
    public User getUser(int userId) {
        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.query(TABLE_USERS, null, COLUMN_USER_ID + "=?", new String[]{String.valueOf(userId)}, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return new User(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_FULL_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_EMAIL)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_PASSWORD)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_PROFILE_PIC))
                );
            }
        }
        return null;
    }

    public int updateUser(int userId, String name, String profilePicUri) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_FULL_NAME, name);
        values.put(COLUMN_USER_PROFILE_PIC, profilePicUri);
        return db.update(TABLE_USERS, values, COLUMN_USER_ID + " = ?", new String[]{String.valueOf(userId)});
    }

    public boolean addUser(String fullName, String phone, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_FULL_NAME, fullName);
        values.put(COLUMN_USER_PHONE, phone);
        values.put(COLUMN_USER_EMAIL, email);
        values.put(COLUMN_USER_PASSWORD, password);
        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public int checkUser(String email, String password) {
        int userId = -1;
        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_USER_ID}, COLUMN_USER_EMAIL + " = ? AND " + COLUMN_USER_PASSWORD + " = ?", new String[]{email, password}, null, null, null)) {
            if (cursor.moveToFirst()) {
                userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID));
            }
        }
        return userId;
    }

    // --- Task Methods ---
    public long addTask(Task task, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TASK_TITLE, task.getTitle());
        values.put(COLUMN_TASK_DESCRIPTION, task.getDescription());
        values.put(COLUMN_TASK_DATE, task.getDate());
        values.put(COLUMN_TASK_TIME, task.getTime());
        values.put(COLUMN_TASK_CATEGORY, task.getCategory());
        values.put(COLUMN_TASK_IS_COMPLETED, task.isCompleted() ? 1 : 0);
        values.put(COLUMN_TASK_USER_ID, userId);
        return db.insert(TABLE_TASKS, null, values);
    }

    public List<Task> getAllTasks(int userId) {
        List<Task> taskList = new ArrayList<>();
        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TASKS + " WHERE " + COLUMN_TASK_USER_ID + " = ?", new String[]{String.valueOf(userId)})) {
            if (cursor.moveToFirst()) {
                do {
                    Task task = new Task(
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_TITLE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_DESCRIPTION)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_DATE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_TIME)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_CATEGORY)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TASK_IS_COMPLETED)) == 1
                    );
                    task.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TASK_ID)));
                    taskList.add(task);
                } while (cursor.moveToNext());
            }
        }
        return taskList;
    }

    public int updateTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TASK_TITLE, task.getTitle());
        values.put(COLUMN_TASK_DESCRIPTION, task.getDescription());
        values.put(COLUMN_TASK_DATE, task.getDate());
        values.put(COLUMN_TASK_TIME, task.getTime());
        values.put(COLUMN_TASK_CATEGORY, task.getCategory());
        values.put(COLUMN_TASK_IS_COMPLETED, task.isCompleted() ? 1 : 0);
        return db.update(TABLE_TASKS, values, COLUMN_TASK_ID + " = ?", new String[]{String.valueOf(task.getId())});
    }

    public void deleteTask(int taskId, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TASKS, COLUMN_TASK_ID + " = ? AND " + COLUMN_TASK_USER_ID + " = ?", new String[]{String.valueOf(taskId), String.valueOf(userId)});
    }

    // --- Hobby Methods ---
    public void addHobby(int userId, String hobbyName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_HOBBY_USER_ID, userId);
        values.put(COLUMN_HOBBY_NAME, hobbyName);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        values.put(COLUMN_HOBBY_WEEK_START_DATE, sdf.format(new Date()));
        db.insert(TABLE_HOBBIES, null, values);
    }

    public void deleteHobby(int hobbyId, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        // First delete any check-ins associated with this hobby to maintain data integrity
        db.delete(TABLE_HOBBY_CHECKINS, COLUMN_CHECKIN_HOBBY_ID + " = ? AND " + COLUMN_CHECKIN_USER_ID + " = ?", new String[]{String.valueOf(hobbyId), String.valueOf(userId)});
        // Then delete the hobby itself
        db.delete(TABLE_HOBBIES, COLUMN_HOBBY_ID + " = ? AND " + COLUMN_HOBBY_USER_ID + " = ?", new String[]{String.valueOf(hobbyId), String.valueOf(userId)});
    }

    public List<Hobby> getHobbiesForUser(int userId) {
        List<Hobby> hobbyList = new ArrayList<>();
        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_HOBBIES + " WHERE " + COLUMN_HOBBY_USER_ID + " = ?", new String[]{String.valueOf(userId)})) {
            if (cursor.moveToFirst()) {
                do {
                    Hobby hobby = new Hobby(
                            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_HOBBY_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HOBBY_NAME)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_HOBBY_STREAK)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HOBBY_LAST_CHECKED_IN)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HOBBY_WEEK_START_DATE))
                    );
                    hobbyList.add(hobby);
                } while (cursor.moveToNext());
            }
        }
        return hobbyList;
    }

    public List<Hobby> getAllHobbies(int userId) {
        List<Hobby> hobbyList = new ArrayList<>();
        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_HOBBIES + " WHERE " + COLUMN_HOBBY_USER_ID + " = ?", new String[]{String.valueOf(userId)})) {
            if (cursor.moveToFirst()) {
                do {
                    Hobby hobby = new Hobby(
                            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_HOBBY_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HOBBY_NAME)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_HOBBY_STREAK)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HOBBY_LAST_CHECKED_IN)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HOBBY_WEEK_START_DATE))
                    );
                    hobbyList.add(hobby);
                } while (cursor.moveToNext());
            }
        }
        return hobbyList;
    }

    public List<String> getHobbyCheckIns(int hobbyId) {
        List<String> checkInDates = new ArrayList<>();
        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.query(TABLE_HOBBY_CHECKINS, new String[]{COLUMN_CHECKIN_TIMESTAMP}, COLUMN_CHECKIN_HOBBY_ID + " = ?", new String[]{String.valueOf(hobbyId)}, null, null, COLUMN_CHECKIN_TIMESTAMP + " DESC")) {
            if (cursor.moveToFirst()) {
                do {
                    checkInDates.add(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CHECKIN_TIMESTAMP)));
                } while (cursor.moveToNext());
            }
        }
        return checkInDates;
    }

    public void updateHobby(Hobby hobby) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_HOBBY_STREAK, hobby.getStreak());
        values.put(COLUMN_HOBBY_LAST_CHECKED_IN, hobby.getLastCheckedIn());
        values.put(COLUMN_HOBBY_WEEK_START_DATE, hobby.getWeekStartDate());
        db.update(TABLE_HOBBIES, values, COLUMN_HOBBY_ID + " = ?", new String[]{String.valueOf(hobby.getId())});
    }

    public void addHobbyCheckIn(int hobbyId, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CHECKIN_HOBBY_ID, hobbyId);
        values.put(COLUMN_CHECKIN_USER_ID, userId);
        // Explicitly add the current timestamp to ensure the check works correctly.
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        values.put(COLUMN_CHECKIN_TIMESTAMP, sdf.format(new Date()));
        db.insert(TABLE_HOBBY_CHECKINS, null, values);
    }

    /**
     * Checks if a user has already checked in for a specific hobby TODAY.
     * @param hobbyId The ID of the hobby.
     * @param userId The ID of the user.
     * @return true if a check-in exists for today, false otherwise.
     */
    public boolean hasCheckedInToday(int hobbyId, int userId) {
        try (SQLiteDatabase db = this.getReadableDatabase()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String todayDate = sdf.format(new Date());

            try (Cursor cursor = db.rawQuery(
                    "SELECT 1 FROM " + TABLE_HOBBY_CHECKINS +
                            " WHERE " + COLUMN_CHECKIN_HOBBY_ID + " = ? AND " +
                            COLUMN_CHECKIN_USER_ID + " = ? AND " +
                            "DATE(" + COLUMN_CHECKIN_TIMESTAMP + ") = ?",
                    new String[]{String.valueOf(hobbyId), String.valueOf(userId), todayDate})) {
                return cursor.moveToFirst();
            }
        }
    }

    public boolean hasCheckedInThisWeek(int hobbyId, int userId) {
        try (SQLiteDatabase db = this.getReadableDatabase()) {
            Calendar calendar = Calendar.getInstance();
            calendar.setFirstDayOfWeek(Calendar.MONDAY);
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            Date weekStartDate = calendar.getTime();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String weekStartString = sdf.format(weekStartDate);

            try (Cursor cursor = db.rawQuery("SELECT 1 FROM " + TABLE_HOBBY_CHECKINS + " WHERE " + COLUMN_CHECKIN_HOBBY_ID + " = ? AND " + COLUMN_CHECKIN_USER_ID + " = ? AND " + COLUMN_CHECKIN_TIMESTAMP + " >= ?", new String[]{String.valueOf(hobbyId), String.valueOf(userId), weekStartString})) {
                return cursor.moveToFirst();
            }
        }
    }


    public int getWeeklyCheckInCount(int hobbyId, int userId) {
        try (SQLiteDatabase db = this.getReadableDatabase()) {
            Calendar calendar = Calendar.getInstance();
            calendar.setFirstDayOfWeek(Calendar.MONDAY);
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            Date weekStartDate = calendar.getTime();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String weekStartString = sdf.format(weekStartDate);

            try (Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_HOBBY_CHECKINS + " WHERE " + COLUMN_CHECKIN_HOBBY_ID + " = ? AND " + COLUMN_CHECKIN_USER_ID + " = ? AND " + COLUMN_CHECKIN_TIMESTAMP + " >= ?", new String[]{String.valueOf(hobbyId), String.valueOf(userId), weekStartString})) {
                if (cursor.moveToFirst()) {
                    return cursor.getInt(0);
                }
            }
        }
        return 0;
    }


    // --- Mood Methods ---
    public void addMood(int userId, String moodName, String moodSticker) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_MOOD_USER_ID, userId);
        values.put(COLUMN_MOOD_NAME, moodName);
        values.put(COLUMN_MOOD_STICKER, moodSticker);
        db.insert(TABLE_MOODS, null, values);
    }

    public Mood getLatestMood(int userId) {
        Mood latestMood = null;
        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_MOODS + " WHERE " + COLUMN_MOOD_USER_ID + " = ? ORDER BY " + COLUMN_MOOD_TIMESTAMP + " DESC LIMIT 1", new String[]{String.valueOf(userId)})) {
            if (cursor.moveToFirst()) {
                latestMood = new Mood(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MOOD_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MOOD_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MOOD_STICKER)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MOOD_TIMESTAMP))
                );
            }
        }
        return latestMood;
    }

    public List<Mood> getMoodHistory(int userId) {
        List<Mood> moodList = new ArrayList<>();
        // Corrected query to order by timestamp in descending order
        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_MOODS + " WHERE " + COLUMN_MOOD_USER_ID + " = ? ORDER BY " + COLUMN_MOOD_TIMESTAMP + " DESC", new String[]{String.valueOf(userId)})) {
            if (cursor.moveToFirst()) {
                do {
                    Mood mood = new Mood(
                            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MOOD_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MOOD_NAME)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MOOD_STICKER)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MOOD_TIMESTAMP))
                    );
                    moodList.add(mood);
                } while (cursor.moveToNext());
            }
        }
        return moodList;
    }

    public void clearMoodHistory(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MOODS, COLUMN_MOOD_USER_ID + " = ?", new String[]{String.valueOf(userId)});
    }
}
