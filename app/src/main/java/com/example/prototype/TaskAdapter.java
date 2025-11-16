package com.example.prototype;

import android.app.AlertDialog;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prototype.model.Task;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private OnTaskActionListener listener;
    private String currentCategory;

    public interface OnTaskActionListener {
        void onTaskChecked(Task task, boolean isCompleted);
        void onDeleteClicked(Task task);
    }

    public TaskAdapter(List<Task> taskList, OnTaskActionListener listener, String currentCategory) {
        this.taskList = taskList;
        this.listener = listener;
        this.currentCategory = currentCategory;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.tvTitle.setText(task.getTitle());
        holder.tvDate.setText(formatDueDate(task));

        // Prevent multiple triggers
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(task.isCompleted());

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.setCompleted(isChecked);
            if (listener != null) listener.onTaskChecked(task, isChecked);
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClicked(task);
        });

        // Click item to show description
        holder.itemView.setOnClickListener(v -> {
            new AlertDialog.Builder(holder.itemView.getContext())
                    .setTitle(task.getTitle())
                    .setMessage(task.getDescription())
                    .setPositiveButton("OK", null)
                    .show();
        });

        if (task.isCompleted()) {
            holder.itemView.setBackgroundColor(Color.parseColor("#C8E6C9")); // A light green color
        } else if (isOverdue(task)) {
            holder.itemView.setBackgroundColor(Color.parseColor("#FFCDD2")); // A light red color
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT); // Default color
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

    private String formatDueDate(Task task) {
        String dateStr = task.getDate();
        String timeStr = task.getTime();

        if (dateStr == null || dateStr.isEmpty()) {
            return ""; // No date, no text
        }

        try {
            Date date;
            SimpleDateFormat outputFormat;

            if (timeStr != null && !timeStr.isEmpty()) {
                // Both date and time are present
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                date = inputFormat.parse(dateStr + " " + timeStr);
                outputFormat = new SimpleDateFormat("MMMM dd, yyyy, 'at' hh:mm a", Locale.getDefault());
            } else {
                // Only date is present
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                date = inputFormat.parse(dateStr);
                outputFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
            }
            return "Due date: " + outputFormat.format(date);

        } catch (ParseException e) {
            e.printStackTrace();
            // Fallback for debugging
            return "Due date: " + dateStr + (timeStr != null && !timeStr.isEmpty() ? " " + timeStr : "");
        }
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public void updateTasks(List<Task> newTasks, String category) {
        this.taskList = new ArrayList<>(newTasks);
        this.currentCategory = category;
        notifyDataSetChanged();
    }

    public void setCurrentCategory(String category) {
        this.currentCategory = category;
    }

    public void removeTask(Task task) {
        int position = -1;
        for (int i = 0; i < taskList.size(); i++) {
            if (taskList.get(i).getId() == task.getId()) {
                position = i;
                break;
            }
        }
        if (position > -1) {
            taskList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, taskList.size());
        }
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView tvTitle, tvDate;
        ImageView deleteButton;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkBox);
            tvTitle = itemView.findViewById(R.id.TaskTitle);
            tvDate = itemView.findViewById(R.id.TaskDate);
            deleteButton = itemView.findViewById(R.id.btnDelete);
        }
    }
}
