package com.example.prototype;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prototype.model.Mood;

import java.util.ArrayList;
import java.util.List;

public class MoodActivity extends AppCompatActivity implements MoodAdapter.OnMoodListener {

    private RecyclerView moodRecyclerView;
    private MoodAdapter moodAdapter;
    private List<Mood> moodList;
    private Button btnSaveMood;
    private LinearLayout layoutCustomMood;
    private EditText etCustomMood;
    private EditText etCustomSticker;
    private MyDatabaseHelper dbHelper;
    private int currentUserId;

    public static final String EXTRA_MOOD_NAME = "com.example.prototype.EXTRA_MOOD_NAME";
    public static final String EXTRA_MOOD_STICKER = "com.example.prototype.EXTRA_MOOD_STICKER";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mood);

        currentUserId = getIntent().getIntExtra("USER_ID", -1);
        if (currentUserId == -1) {
            Toast.makeText(this, "Error: User not identified.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        dbHelper = MyDatabaseHelper.getInstance(this);
        moodRecyclerView = findViewById(R.id.recyclerViewMoods);
        btnSaveMood = findViewById(R.id.btnSaveMood);
        layoutCustomMood = findViewById(R.id.layoutCustomMood);
        etCustomMood = findViewById(R.id.etCustomMood);
        etCustomSticker = findViewById(R.id.etCustomSticker);

        setupMoodList();
        setupRecyclerView();

        btnSaveMood.setOnClickListener(v -> saveSelectedMood());
    }

    private void setupMoodList() {
        moodList = new ArrayList<>();
        // Using actual Unicode emoji characters.
        moodList.add(new Mood("Happy", "😊"));
        moodList.add(new Mood("Sad", "😢"));
        moodList.add(new Mood("Angry", "😠"));
        moodList.add(new Mood("Calm", "😌"));
        moodList.add(new Mood("Excited", "😃"));
        moodList.add(new Mood("Tired", "😴"));
        moodList.add(new Mood("Others", "➕")); // Special item for custom moods
    }

    private void setupRecyclerView() {
        moodAdapter = new MoodAdapter(moodList, this);
        moodRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        moodRecyclerView.setAdapter(moodAdapter);
    }

    @Override
    public void onMoodClick(int position) {
        Mood selectedMood = moodList.get(position);
        if (selectedMood.getName().equals("Others")) {
            layoutCustomMood.setVisibility(View.VISIBLE);
        } else {
            layoutCustomMood.setVisibility(View.GONE);
        }
    }

    private void saveSelectedMood() {
        Mood selectedMood = moodAdapter.getSelectedMood();
        String moodName = null;
        String moodSticker = null;

        if (selectedMood == null) {
            Toast.makeText(this, "Please select a mood", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedMood.getName().equals("Others")) {
            moodName = etCustomMood.getText().toString().trim();
            moodSticker = etCustomSticker.getText().toString().trim();
            if (moodName.isEmpty()) {
                Toast.makeText(this, "Please enter a custom mood name", Toast.LENGTH_SHORT).show();
                return;
            }
            if (moodSticker.isEmpty()) {
                Toast.makeText(this, "Please enter a sticker", Toast.LENGTH_SHORT).show();
                return;
            }
            // Restrictive validation removed to allow all emojis from the keyboard.
        } else {
            moodName = selectedMood.getName();
            moodSticker = selectedMood.getSticker();
        }

        // Save to DB
        dbHelper.addMood(currentUserId, moodName, moodSticker);

        // Return result to DashboardActivity
        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_MOOD_NAME, moodName);
        resultIntent.putExtra(EXTRA_MOOD_STICKER, moodSticker);
        setResult(RESULT_OK, resultIntent);

        Toast.makeText(this, "Mood saved!", Toast.LENGTH_SHORT).show();
        finish();
    }
}