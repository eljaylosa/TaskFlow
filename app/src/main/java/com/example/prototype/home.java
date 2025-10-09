package com.example.prototype;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class home extends AppCompatActivity {


    ImageButton Home, Hobby, Task, Mood, Profile, addBTN;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);


        addBTN = findViewById(R.id.addBtn);
        Home = findViewById(R.id.home);
        Hobby = findViewById(R.id.hobby);
        Task = findViewById(R.id.task);
        Mood = findViewById(R.id.mood);
        Profile = findViewById(R.id.profile);

    }
}
