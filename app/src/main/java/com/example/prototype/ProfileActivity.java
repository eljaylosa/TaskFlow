package com.example.prototype;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.prototype.model.Hobby;
import com.example.prototype.model.Mood;
import com.example.prototype.model.User;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class ProfileActivity extends AppCompatActivity {
    private ImageView profileImage;
    private TextView tvName, tvHobbies, tvMeterLabel, tvHobbySummary;
    private EditText etName;
    private Button btnEditSave, btnEditHobbies, btnDidIt, btnClearMoods, btnLogout, btnAboutUs; // Added btnAboutUs
    private boolean isEditMode = false;
    private MyDatabaseHelper dbHelper;
    private int currentUserId;
    private User currentUser;
    private Uri selectedImageUri;
    private RecyclerView moodHistoryRecyclerView;
    private Spinner hobbySpinner;
    private ProgressBar hobbyMeter;
    private List<Hobby> userHobbiesList;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        Glide.with(this).load(uri).into(profileImage);
                        selectedImageUri = uri;
                        try {
                            getContentResolver().takePersistableUriPermission(selectedImageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } catch (SecurityException e) {
                            Toast.makeText(this, "Failed to take permission", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

    private final ActivityResultLauncher<Intent> editHobbiesLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                // onResume will handle reloading the hobbies.
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        dbHelper = MyDatabaseHelper.getInstance(this);

        currentUserId = getIntent().getIntExtra("USER_ID", -1);
        if (currentUserId == -1) {
            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
            currentUserId = prefs.getInt("USER_ID", -1);
        }

        if (currentUserId == -1) {
            showFatalError();
            return;
        }

        profileImage = findViewById(R.id.profile_image);
        tvName = findViewById(R.id.profile_name);
        etName = findViewById(R.id.profile_name_edit);
        btnEditSave = findViewById(R.id.btn_edit_save);
        tvHobbies = findViewById(R.id.profile_hobbies);
        btnEditHobbies = findViewById(R.id.btn_edit_hobbies);
        moodHistoryRecyclerView = findViewById(R.id.mood_history_recycler_view);
        hobbySpinner = findViewById(R.id.hobby_selector);
        btnDidIt = findViewById(R.id.btn_did_it);
        hobbyMeter = findViewById(R.id.hobby_meter);
        tvMeterLabel = findViewById(R.id.meter_label);
        tvHobbySummary = findViewById(R.id.hobby_summary_text);
        btnClearMoods = findViewById(R.id.btn_clear_moods);
        btnLogout = findViewById(R.id.btn_logout);
        btnAboutUs = findViewById(R.id.btn_about_us); // Find the button

        moodHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        setupListeners();
    }

    private void setupListeners() {
        btnEditSave.setOnClickListener(v -> toggleEditMode());
        profileImage.setOnClickListener(v -> {
            if (isEditMode) {
                openGallery();
            }
        });

        btnEditHobbies.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, HobbyActivity.class);
            intent.putExtra("USER_ID", currentUserId);
            editHobbiesLauncher.launch(intent);
        });

        btnDidIt.setOnClickListener(v -> {
            if (userHobbiesList == null || userHobbiesList.isEmpty()) {
                Toast.makeText(this, "No hobbies to track. Please add a hobby first.", Toast.LENGTH_SHORT).show();
                return;
            }
            int selectedPosition = hobbySpinner.getSelectedItemPosition();
            if (selectedPosition < 0 || selectedPosition >= userHobbiesList.size()) {
                Toast.makeText(this, "Please select a valid hobby.", Toast.LENGTH_SHORT).show();
                return;
            }

            Hobby selectedHobby = userHobbiesList.get(selectedPosition);

            executor.execute(() -> {
                // Use the new method to add a check-in
                dbHelper.addHobbyCheckIn(selectedHobby.getId(), currentUserId);
                handler.post(() -> {
                    Toast.makeText(ProfileActivity.this, "Great job on completing your hobby!", Toast.LENGTH_SHORT).show();
                    updateWeeklyHobbyProgress(selectedHobby);
                });
            });
        });

        hobbySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (userHobbiesList != null && !userHobbiesList.isEmpty() && position < userHobbiesList.size()) {
                    Hobby selectedHobby = userHobbiesList.get(position);
                    updateWeeklyHobbyProgress(selectedHobby);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                updateWeeklyHobbyProgress(null);
            }
        });

        btnClearMoods.setOnClickListener(v -> showClearMoodsConfirmationDialog());

        btnLogout.setOnClickListener(v -> {
            // Clear the shared preferences
            SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove("USER_ID");
            editor.remove("LAST_ACTIVE_TIME");
            editor.apply();

            // Navigate to LoginActivity and clear task history
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
        
        // Add listener for the About Us button
        btnAboutUs.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://aboutus-mysticforcedevs.netlify.app/"));
            startActivity(browserIntent);
        });
    }

    private void showClearMoodsConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Clear Mood History")
                .setMessage("Are you sure you want to delete all your mood entries? This action cannot be undone.")
                .setPositiveButton("Clear", (dialog, which) -> {
                    executor.execute(() -> {
                        dbHelper.clearMoodHistory(currentUserId);
                        handler.post(() -> {
                            Toast.makeText(ProfileActivity.this, "Mood history cleared.", Toast.LENGTH_SHORT).show();
                            loadMoodHistory(); // Refresh the list
                        });
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    private void showFatalError() {
        Toast.makeText(this, "User ID not provided. Cannot load profile.", Toast.LENGTH_LONG).show();
        finish();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfile();
        loadHobbies();
        loadMoodHistory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }

    private void toggleEditMode() {
        isEditMode = !isEditMode;
        if (isEditMode) {
            tvName.setVisibility(View.GONE);
            etName.setVisibility(View.VISIBLE);
            etName.setText(tvName.getText());
            etName.requestFocus();
            btnEditSave.setText("Save");
        } else {
            tvName.setVisibility(View.VISIBLE);
            etName.setVisibility(View.GONE);
            btnEditSave.setText("Edit");
            saveUserProfile();
        }
    }

    private void saveUserProfile() {
        final String newName = etName.getText().toString().trim();
        if (currentUser == null || TextUtils.isEmpty(newName)) {
            Toast.makeText(this, "Name cannot be empty.", Toast.LENGTH_SHORT).show();
            tvName.setText(currentUser != null ? currentUser.getName() : "");
            return;
        }

        final String imageUriString = (selectedImageUri != null) ? selectedImageUri.toString() : currentUser.getProfilePic();

        executor.execute(() -> {
            dbHelper.updateUser(currentUserId, newName, imageUriString);
            handler.post(this::loadUserProfile);
        });
        Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
    }

    private void loadUserProfile() {
        executor.execute(() -> {
            final User user = dbHelper.getUser(currentUserId);
            handler.post(() -> {
                currentUser = user;
                if (currentUser != null) {
                    tvName.setText(currentUser.getName());
                    etName.setText(currentUser.getName());
                    if (selectedImageUri == null && currentUser.getProfilePic() != null && !currentUser.getProfilePic().isEmpty()) {
                        Glide.with(ProfileActivity.this).load(Uri.parse(currentUser.getProfilePic())).into(profileImage);
                    }
                } else {
                    Toast.makeText(this, "Could not load user profile.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void loadHobbies() {
        executor.execute(() -> {
            List<Hobby> originalHobbies = dbHelper.getHobbiesForUser(currentUserId);
            if (originalHobbies == null) {
                originalHobbies = new ArrayList<>();
            }

            final List<Hobby> validHobbies = originalHobbies.stream()
                    .filter(h -> h != null && h.getName() != null && !h.getName().trim().isEmpty())
                    .collect(Collectors.toList());

            final List<String> hobbyNames = validHobbies.stream()
                    .map(Hobby::getName)
                    .collect(Collectors.toList());

            handler.post(() -> {
                userHobbiesList = validHobbies;

                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, hobbyNames);
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                hobbySpinner.setAdapter(spinnerAdapter);

                if (!userHobbiesList.isEmpty()) {
                    String hobbiesText = userHobbiesList.stream()
                            .map(Hobby::getName)
                            .collect(Collectors.joining(", "));
                    tvHobbies.setText(hobbiesText);

                    if (hobbySpinner.getSelectedItemPosition() != AdapterView.INVALID_POSITION) {
                        updateWeeklyHobbyProgress(userHobbiesList.get(hobbySpinner.getSelectedItemPosition()));
                    } else {
                        updateWeeklyHobbyProgress(null);
                    }
                } else {
                    tvHobbies.setText("No hobbies yet. Add some!");
                    updateWeeklyHobbyProgress(null);
                }
            });
        });
    }

    private void loadMoodHistory() {
        executor.execute(() -> {
            final List<Mood> moodHistory = dbHelper.getMoodHistory(currentUserId);
            handler.post(() -> {
                MoodHistoryAdapter moodAdapter = new MoodHistoryAdapter(moodHistory != null ? moodHistory : new ArrayList<>());
                moodHistoryRecyclerView.setAdapter(moodAdapter);
            });
        });
    }

    private void updateWeeklyHobbyProgress(Hobby hobby) {
        if (hobby == null) {
            hobbyMeter.setVisibility(View.GONE);
            tvMeterLabel.setVisibility(View.GONE);
            btnDidIt.setVisibility(View.GONE);
            tvHobbySummary.setText("Select a hobby to see progress.");
            return;
        }

        executor.execute(() -> {
            final int weeklyCount = dbHelper.getWeeklyCheckInCount(hobby.getId(), currentUserId);
            final boolean completedToday = dbHelper.hasCheckedInToday(hobby.getId(), currentUserId);

            handler.post(() -> {
                hobbyMeter.setVisibility(View.VISIBLE);
                tvMeterLabel.setVisibility(View.VISIBLE);
                btnDidIt.setVisibility(View.VISIBLE);

                hobbyMeter.setMax(7);
                hobbyMeter.setProgress(weeklyCount);

                String summary = String.format(Locale.getDefault(),
                        "This week's progress: %d/7.", weeklyCount);
                tvHobbySummary.setText(summary);

                if (completedToday) {
                    btnDidIt.setEnabled(false);
                    btnDidIt.setText("Completed Today");
                } else {
                    btnDidIt.setEnabled(true);
                    btnDidIt.setText("I Did It!");
                }
            });
        });
    }

    public static class MoodHistoryAdapter extends RecyclerView.Adapter<MoodHistoryAdapter.MoodViewHolder> {
        private final List<Mood> moods;
        private final SimpleDateFormat displayFormat = new SimpleDateFormat("MMMM dd, yyyy, hh:mm a", Locale.getDefault());
        private final SimpleDateFormat parseFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        public MoodHistoryAdapter(List<Mood> moods) {
            this.moods = moods;
        }

        @NonNull
        @Override
        public MoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mood_history, parent, false);
            return new MoodViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MoodViewHolder holder, int position) {
            Mood mood = moods.get(position);

            if (mood == null) {
                holder.moodIcon.setText("?");
                holder.moodName.setText("Invalid Mood Data");
                holder.moodDate.setText("");
                return;
            }

            holder.moodIcon.setText(mood.getSticker() != null ? mood.getSticker() : "");
            holder.moodName.setText(mood.getName() != null ? mood.getName() : "Unnamed Mood");

            try {
                String timestamp = mood.getTimestamp();
                if (timestamp != null && !timestamp.isEmpty()) {
                    Date date = parseFormat.parse(timestamp);
                    holder.moodDate.setText(date != null ? displayFormat.format(date) : "");
                } else {
                    holder.moodDate.setText("No Date");
                }
            } catch (ParseException e) {
                holder.moodDate.setText("Invalid Date");
            }
        }

        @Override
        public int getItemCount() {
            return moods.size();
        }

        static class MoodViewHolder extends RecyclerView.ViewHolder {
            TextView moodIcon, moodName, moodDate;

            public MoodViewHolder(@NonNull View itemView) {
                super(itemView);
                moodIcon = itemView.findViewById(R.id.mood_icon);
                moodName = itemView.findViewById(R.id.mood_name);
                moodDate = itemView.findViewById(R.id.mood_timestamp);
            }
        }
    }
}
