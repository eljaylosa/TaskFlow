package com.example.prototype;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prototype.model.Hobby;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class HobbyActivity extends AppCompatActivity implements HobbyAdapter.OnHobbyInteractionListener {

    private MyDatabaseHelper myDB;
    private final List<Hobby> allHobbies = new ArrayList<>();
    private HobbyAdapter adapter;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hobby);

        userId = getIntent().getIntExtra("USER_ID", -1);
        if (userId == -1) {
            userId = getSharedPreferences(DashboardActivity.USER_PREFS_NAME, MODE_PRIVATE).getInt(DashboardActivity.USER_ID_KEY, -1);
        }

        if (userId == -1) {
            Toast.makeText(this, "Error: User not logged in.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        myDB = MyDatabaseHelper.getInstance(this);

        RecyclerView recyclerView = findViewById(R.id.hobbiesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // The adapter handles clicks and deletes.
        adapter = new HobbyAdapter(allHobbies, this);
        recyclerView.setAdapter(adapter);

        FloatingActionButton fabAddHobby = findViewById(R.id.fabAddHobby);
        fabAddHobby.setOnClickListener(view -> showAddHobbyDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshHobbies();
    }

    /**
     * This is the corrected logic. It uses the same check-in system as the ProfileActivity,
     * ensuring that progress is saved to the right place and the profile screen updates correctly.
     */
    @Override
    public void onHobbyItemClicked(Hobby hobby) {
        // We use the same method as ProfileActivity to check if the hobby was already done today.
        boolean alreadyCheckedIn = myDB.hasCheckedInToday(hobby.getId(), userId);

        if (alreadyCheckedIn) {
            Toast.makeText(this, "You've already completed this hobby today.", Toast.LENGTH_SHORT).show();
        } else {
            // This is the correct method. It adds a record to the check-in history.
            // ProfileActivity reads from this history, so it will now see the update.
            myDB.addHobbyCheckIn(hobby.getId(), userId);
            Toast.makeText(this, "Great job! Your progress has been updated.", Toast.LENGTH_SHORT).show();

            // TODO: The weekly summary pop-up was tied to the old, broken system.
            // We can now re-implement it based on the reliable check-in history as a next step.

            // Refresh the UI in this activity to show that the item is now "locked" for today.
            refreshHobbies();
        }
    }

    @Override
    public void onDeleteClicked(Hobby hobby) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Hobby")
                .setMessage("Are you sure you want to delete '" + hobby.getName() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    myDB.deleteHobby(hobby.getId(), userId);
                    Toast.makeText(HobbyActivity.this, "Hobby deleted.", Toast.LENGTH_SHORT).show();
                    refreshHobbies();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * This summary dialog is part of the weekly summary feature.
     * It is not called right now, but is kept here so we can re-enable it
     * based on the new, correct check-in system.
     */
    private void showWeeklySummaryDialog(Hobby hobby, int streak) {
        String title;
        String message;
        String emoji;

        if (streak >= 4) {
            title = "Nice Work!";
            message = "You did your hobby '" + hobby.getName() + "' " + streak + "/7 days.";
            emoji = " \uD83D\uDE0A"; // Smiling Face
        } else {
            title = "Keep Going!";
            message = "You\'ve only done your hobby \'" + hobby.getName() + "\' " + streak + " time(s) this week. You can do better!";
            emoji = " \uD83D\uDE1E"; // Sad Face
        }
        if (streak == 7) {
            title = "Congrats!";
            message = "You did your hobby \'" + hobby.getName() + "\' a perfect 7/7 days!";
            emoji = " \uD83C\uDF89"; // Party Popper
        } else if (streak <= 3) {
            title = "Don\'t Give Up!";
            message = "You haven\'t done your hobby \'" + hobby.getName() + "\' much lately. Just " + streak + " day(s).";
            emoji = " \uD83D\uDE22"; // Crying face
        }


        new AlertDialog.Builder(this)
                .setTitle(title + emoji)
                .setMessage(message)
                                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showAddHobbyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add a New Hobby");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String hobbyName = input.getText().toString().trim();
            if (!hobbyName.isEmpty()) {
                myDB.addHobby(userId, hobbyName);
                Toast.makeText(HobbyActivity.this, "Hobby added!", Toast.LENGTH_SHORT).show();
                refreshHobbies();
            } else {
                Toast.makeText(HobbyActivity.this, "Hobby name cannot be empty.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void refreshHobbies() {
        List<Hobby> updatedHobbies = myDB.getHobbiesForUser(userId);
        allHobbies.clear();
        if (updatedHobbies != null) {
            allHobbies.addAll(updatedHobbies);
        }
        adapter.notifyDataSetChanged();
    }
}
