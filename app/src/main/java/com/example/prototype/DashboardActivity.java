package com.example.prototype;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.prototype.model.Mood;
import com.example.prototype.model.Task;
import com.example.prototype.model.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DashboardActivity extends AppCompatActivity {

    // --- Constants ---
    public static final String USER_PREFS_NAME = "user_prefs";
    public static final String USER_ID_KEY = "USER_ID";
    public static final String LAST_ACTIVE_TIME_KEY = "LAST_ACTIVE_TIME";
    private static final long THIRTY_DAYS_IN_MILLIS = 30L * 24 * 60 * 60 * 1000;
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    // Filter Categories
    private static final String FILTER_ALL = "All";
    private static final String FILTER_THIS_WEEK = "This Week";
    private static final String FILTER_NEXT_WEEK = "Next Week";
    private static final String FILTER_UPCOMING = "Upcoming";
    private static final String FILTER_NOT_COMPLETED = "Not Completed";
    private static final String FILTER_COMPLETED = "Completed";

    // --- Views & Adapters ---
    private TextView tvWelcome, tvTaskCount;
    private RecyclerView recyclerViewTasks;
    private TaskAdapter taskAdapter;
    private BottomNavigationView bottomNav;

    // --- State & Helpers ---
    private MyDatabaseHelper dbHelper;
    private int currentUserId = -1;
    private List<Task> allTasks = new ArrayList<>();
    private String currentFilter = FILTER_ALL;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    private final ActivityResultLauncher<Intent> moodLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    loadDashboardData();
                }
            });

    private final ActivityResultLauncher<Intent> addTaskLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // onResume will handle reloading tasks
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!checkSession()) {
            return; // Stop loading if session is invalid
        }

        setContentView(R.layout.activity_dashboard);

        dbHelper = MyDatabaseHelper.getInstance(this);

        SharedPreferences prefs = getSharedPreferences(USER_PREFS_NAME, MODE_PRIVATE);
        currentUserId = prefs.getInt(USER_ID_KEY, -1);

        if (currentUserId == -1) {
            handleSessionError();
            return;
        }

        initViews();
        setupBottomNavigation();
        setupRecyclerView();
        setupFilterButtons();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentUserId == -1) {
            return;
        }

        // Update last active time
        SharedPreferences prefs = getSharedPreferences(USER_PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putLong(LAST_ACTIVE_TIME_KEY, System.currentTimeMillis()).apply();

        loadDashboardData();
        if (bottomNav != null) {
            bottomNav.getMenu().findItem(R.id.nav_home).setChecked(true);
        }
    }

    private boolean checkSession() {
        SharedPreferences prefs = getSharedPreferences(USER_PREFS_NAME, MODE_PRIVATE);
        long lastActiveTime = prefs.getLong(LAST_ACTIVE_TIME_KEY, 0);

        if (lastActiveTime == 0) {
            if (prefs.contains(USER_ID_KEY)) {
                prefs.edit().putLong(LAST_ACTIVE_TIME_KEY, System.currentTimeMillis()).apply();
                return true;
            }
            handleSessionError();
            return false;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastActiveTime > THIRTY_DAYS_IN_MILLIS) {
            Toast.makeText(this, "Session expired due to inactivity. Please log in again.", Toast.LENGTH_LONG).show();
            prefs.edit().clear().apply();
            Intent intent = new Intent(this, LoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return false;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }

    private void handleSessionError() {
        Toast.makeText(this, "User session error. Please log in again.", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, LoginActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void initViews() {
        tvWelcome = findViewById(R.id.welcome_text);
        tvTaskCount = findViewById(R.id.task_count_text);
        recyclerViewTasks = findViewById(R.id.recyclerViewTasks);
        bottomNav = findViewById(R.id.bottom_navigation);

        findViewById(R.id.btnAddTask).setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, AddTaskActivity.class);
            intent.putExtra(USER_ID_KEY, currentUserId);
            addTaskLauncher.launch(intent);
        });
    }

    private void setupBottomNavigation() {
        if (bottomNav == null) return;

        bottomNav.setItemIconTintList(null);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                return true;
            }

            if (itemId == R.id.nav_mood) {
                Intent moodIntent = new Intent(this, MoodActivity.class);
                moodIntent.putExtra(USER_ID_KEY, currentUserId);
                moodLauncher.launch(moodIntent);
                return false;
            }

            Intent intent = null;
            if (itemId == R.id.nav_hobby) {
                intent = new Intent(this, HobbyActivity.class);
            } else if (itemId == R.id.nav_profile) {
                intent = new Intent(this, ProfileActivity.class);
            }

            if (intent != null) {
                intent.putExtra(USER_ID_KEY, currentUserId);
                startActivity(intent);
            }
            return true;
        });
    }


    private void setupRecyclerView() {
        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new TaskAdapter(new ArrayList<>(), new TaskAdapter.OnTaskActionListener() {
            @Override
            public void onTaskChecked(Task task, boolean isCompleted) {
                executor.execute(() -> {
                    task.setCompleted(isCompleted);
                    dbHelper.updateTask(task);
                    handler.post(() -> loadDashboardData());
                });
            }

            @Override
            public void onDeleteClicked(Task task) {
                executor.execute(() -> {
                    dbHelper.deleteTask(task.getId(), currentUserId);
                    handler.post(() -> loadDashboardData());
                });
            }
        }, currentFilter);
        recyclerViewTasks.setAdapter(taskAdapter);
    }

    private void setupFilterButtons() {
        View.OnClickListener filterClickListener = v -> {
            Button clickedButton = (Button) v;
            currentFilter = clickedButton.getText().toString();
            applyFilter();
        };

        int[] filterButtonIds = {
                R.id.btn_all, R.id.btn_this_week, R.id.btn_next_week,
                R.id.btn_upcoming, R.id.btn_not_completed, R.id.btn_completed
        };

        for (int id : filterButtonIds) {
            findViewById(id).setOnClickListener(filterClickListener);
        }
    }


    private void loadDashboardData() {
        executor.execute(() -> {
            final User user = dbHelper.getUser(currentUserId);
            final Mood latestMood = dbHelper.getLatestMood(currentUserId);
            allTasks = dbHelper.getAllTasks(currentUserId);

            handler.post(() -> {
                if (isFinishing() || isDestroyed()) return;

                if (user != null && user.getName() != null && !user.getName().trim().isEmpty()) {
                    String firstName = user.getName().split(" ")[0];
                    tvWelcome.setText("Hi " + firstName);
                } else {
                    tvWelcome.setText("Hi User");
                }

                updateMoodInNavBar(latestMood);
                applyFilter();
            });
        });
    }

    private void applyFilter() {
        if (allTasks == null) {
            taskAdapter.updateTasks(Collections.emptyList(), currentFilter);
            tvTaskCount.setText("You have 0 tasks");
            return;
        }

        List<Task> filteredTasks = new ArrayList<>();
        int activeTaskCount = 0;
        for (Task task : allTasks) {
            if (matchesCategory(task, currentFilter)) {
                filteredTasks.add(task);
            }
            if (!task.isCompleted() && !isOverdue(task)) {
                activeTaskCount++;
            }
        }

        tvTaskCount.setText("You have " + activeTaskCount + " tasks");
        taskAdapter.updateTasks(filteredTasks, currentFilter);
    }

    private boolean matchesCategory(Task task, String category) {
        switch (category) {
            case FILTER_ALL:
                return true;
            case FILTER_COMPLETED:
                return task.isCompleted();
            case FILTER_NOT_COMPLETED:
                return !task.isCompleted() && isOverdue(task);
        }

        if (task.isCompleted() || isOverdue(task)) {
            return false;
        }

        Date taskDate = parseTaskDate(task);
        if (taskDate == null) {
            return false;
        }

        switch (category) {
            case FILTER_THIS_WEEK:
                return isThisWeek(taskDate);
            case FILTER_NEXT_WEEK:
                return isNextWeek(taskDate);
            case FILTER_UPCOMING:
                return isUpcoming(taskDate);
            default:
                return false;
        }
    }

    private boolean isOverdue(Task task) {
        if (task.isCompleted() || task.getDate() == null || task.getDate().isEmpty()) {
            return false;
        }
        try {
            Calendar now = Calendar.getInstance();
            Calendar taskCal = Calendar.getInstance();
            String dateStr = task.getDate();
            String timeStr = task.getTime();
            Date parsedDate;

            if (timeStr != null && !timeStr.isEmpty()) {
                parsedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).parse(dateStr + " " + timeStr);
            } else {
                parsedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dateStr);
                taskCal.setTime(parsedDate);
                taskCal.set(Calendar.HOUR_OF_DAY, 23);
                taskCal.set(Calendar.MINUTE, 59);
                taskCal.set(Calendar.SECOND, 59);
                parsedDate = taskCal.getTime();
            }
            taskCal.setTime(parsedDate);
            return now.after(taskCal);
        } catch (ParseException e) {
            return false;
        }
    }

    private boolean isThisWeek(Date taskDate) {
        Calendar taskCal = Calendar.getInstance();
        taskCal.setTime(taskDate);
        taskCal.setFirstDayOfWeek(Calendar.MONDAY);

        Calendar today = getCalendarForNow();
        today.setFirstDayOfWeek(Calendar.MONDAY);

        Calendar startOfWeek = (Calendar) today.clone();
        startOfWeek.set(Calendar.DAY_OF_WEEK, startOfWeek.getFirstDayOfWeek());

        Calendar endOfWeek = (Calendar) startOfWeek.clone();
        endOfWeek.add(Calendar.WEEK_OF_YEAR, 1);

        return !taskCal.before(startOfWeek) && taskCal.before(endOfWeek);
    }

    private boolean isNextWeek(Date taskDate) {
        Calendar today = getCalendarForNow();
        today.setFirstDayOfWeek(Calendar.MONDAY);
        Calendar taskCal = Calendar.getInstance();
        taskCal.setTime(taskDate);
        taskCal.setFirstDayOfWeek(Calendar.MONDAY);

        Calendar startOfNextWeek = (Calendar) today.clone();
        startOfNextWeek.add(Calendar.WEEK_OF_YEAR, 1);
        startOfNextWeek.set(Calendar.DAY_OF_WEEK, startOfNextWeek.getFirstDayOfWeek());

        Calendar endOfNextWeek = (Calendar) startOfNextWeek.clone();
        endOfNextWeek.add(Calendar.WEEK_OF_YEAR, 1);

        return !taskCal.before(startOfNextWeek) && taskCal.before(endOfNextWeek);
    }

    private boolean isUpcoming(Date taskDate) {
        Calendar taskCal = Calendar.getInstance();
        taskCal.setTime(taskDate);

        if (taskCal.before(getCalendarForNow())) {
            return false;
        }

        return !isThisWeek(taskDate) && !isNextWeek(taskDate);
    }


    private Calendar getCalendarForNow() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    private Date parseTaskDate(Task task) {
        if (task == null || task.getDate() == null || task.getDate().isEmpty()) return null;
        try {
            return new SimpleDateFormat(DATE_FORMAT, Locale.US).parse(task.getDate());
        } catch (ParseException e) {
            return null;
        }
    }

    private void updateMoodInNavBar(Mood mood) {
        if (bottomNav == null) return;
        final MenuItem moodMenuItem = bottomNav.getMenu().findItem(R.id.nav_mood);
        if (moodMenuItem == null) return;

        if (mood != null && mood.getSticker() != null && !mood.getSticker().trim().isEmpty()) {
            moodMenuItem.setTitle(mood.getName());
            String sticker = mood.getSticker();

            if (sticker.startsWith("content://")) {
                int iconSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());

                Glide.with(DashboardActivity.this)
                        .asBitmap()
                        .load(Uri.parse(sticker))
                        .circleCrop()
                        .error(R.drawable.ic_mood)
                        .into(new CustomTarget<Bitmap>(iconSize, iconSize) {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                moodMenuItem.setIcon(new BitmapDrawable(getResources(), resource));
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {
                                moodMenuItem.setIcon(ContextCompat.getDrawable(DashboardActivity.this, R.drawable.ic_mood));
                            }

                            @Override
                            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                super.onLoadFailed(errorDrawable);
                                moodMenuItem.setIcon(ContextCompat.getDrawable(DashboardActivity.this, R.drawable.ic_mood));
                            }
                        });
            } else {
                Drawable emojiDrawable = createEmojiDrawable(this, sticker);
                if (emojiDrawable != null) {
                    moodMenuItem.setIcon(emojiDrawable);
                } else {
                    moodMenuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_mood));
                }
            }
        } else {
            moodMenuItem.setTitle("Mood");
            moodMenuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_mood));
        }
    }

    private Drawable createEmojiDrawable(Context context, String emoji) {
        if (emoji == null || emoji.trim().isEmpty()) {
            return null;
        }

        int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, context.getResources().getDisplayMetrics());

        if (size <= 0) {
            size = 72;
        }

        TextView tv = new TextView(context);
        tv.setText(emoji);
        tv.setTextColor(Color.BLACK);
        tv.setGravity(android.view.Gravity.CENTER);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, size * 0.9f);

        tv.getPaint().setShadowLayer(0.001f, 0, 0, 0);

        int widthSpec = View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.EXACTLY);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.EXACTLY);
        tv.measure(widthSpec, heightSpec);
        tv.layout(0, 0, size, size);

        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        tv.draw(canvas);

        return new BitmapDrawable(context.getResources(), bitmap);
    }
}
