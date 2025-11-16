package com.example.prototype;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prototype.model.Mood;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MoodHistoryAdapter extends RecyclerView.Adapter<MoodHistoryAdapter.MoodViewHolder> {

    private final List<Mood> moodList;

    public MoodHistoryAdapter(List<Mood> moodList) {
        this.moodList = moodList;
    }

    @NonNull
    @Override
    public MoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mood_history, parent, false);
        return new MoodViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MoodViewHolder holder, int position) {
        Mood currentMood = moodList.get(position);

        // Set the emoji, name, and date from the Mood object
        holder.moodEmoji.setText(currentMood.getSticker());
        holder.moodName.setText(currentMood.getName());

        // Format and set the date
        try {
            String timestamp = currentMood.getTimestamp();
            if (timestamp != null && !timestamp.isEmpty()) {
                SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                Date date = dbFormat.parse(timestamp);
                SimpleDateFormat displayFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
                holder.moodDate.setText(displayFormat.format(date));
            } else {
                holder.moodDate.setText("No date");
            }
        } catch (ParseException e) {
            holder.moodDate.setText("Invalid date");
        }
    }

    @Override
    public int getItemCount() {
        return moodList.size();
    }

    public static class MoodViewHolder extends RecyclerView.ViewHolder {
        public TextView moodEmoji;
        public TextView moodName;
        public TextView moodDate;

        public MoodViewHolder(View view) {
            super(view);
            moodEmoji = view.findViewById(R.id.mood_icon);
            moodName = view.findViewById(R.id.mood_name);
            moodDate = view.findViewById(R.id.mood_timestamp);
        }
    }
}
