package com.example.prototype;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class MyDatabaseHelper extends SQLiteOpenHelper {

    private Context context;
    public static final String DATABASE_NAME = "signup.DB";
    public static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME = "users";
    private static final String USER_ID = "user_id";
    private static final String FULL_NAME = "fullname";
    private static final String PHONE_NO = "phone_no";
    private static final String EMAIL = "email";
    private static final String PASSWORD = "password";



    public  MyDatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String query =
                "CREATE TABLE " + TABLE_NAME +
                        " (" + USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        FULL_NAME + " TEXT, " +
                        PHONE_NO + " INTEGER, " +
                        EMAIL + " TEXT, " +
                        PASSWORD + " TEXT)";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
    void insertUser(String name, String phone, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(FULL_NAME, name);
        cv.put(PHONE_NO, phone);
        cv.put(EMAIL, email);
        cv.put(PASSWORD, password);
        long result = db.insert(TABLE_NAME, null, cv);
        if (result == -1) {
            Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show();
        }
    }

}
