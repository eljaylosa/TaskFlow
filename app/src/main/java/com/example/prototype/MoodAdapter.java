package com.example.prototype;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.prototype.model.Mood;
import java.util.List;

public class MoodAdapter extends RecyclerView.Adapter<MoodAdapter.MoodViewHolder> {

    private final List<Mood> moodList;
    private final OnMoodListener onMoodListener;
    private int selectedPosition = -1;

    public MoodAdapter(List<Mood> moodList, OnMoodListener onMoodListener) {
        this.moodList = moodList;
        this.onMoodListener = onMoodListener;
    }

    @NonNull
    @Override
    public MoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mood, parent, false);
        return new MoodViewHolder(view, onMoodListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MoodViewHolder holder, int position) {
        Mood mood = moodList.get(position);
        holder.moodSticker.setText(mood.getSticker());
        holder.moodName.setText(mood.getName());
        holder.itemView.setSelected(selectedPosition == position);
    }

    @Override
    public int getItemCount() {
        return moodList.size();
    }

    public Mood getSelectedMood() {
        if (selectedPosition != -1) {
            return moodList.get(selectedPosition);
        }
        return null;
    }

    class MoodViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView moodSticker;
        TextView moodName;
        OnMoodListener onMoodListener;

        public MoodViewHolder(@NonNull View itemView, OnMoodListener onMoodListener) {
            super(itemView);
            moodSticker = itemView.findViewById(R.id.moodSticker);
            moodName = itemView.findViewById(R.id.moodName);
            this.onMoodListener = onMoodListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onMoodListener.onMoodClick(getAdapterPosition());
            notifyItemChanged(selectedPosition);
            selectedPosition = getAdapterPosition();
            notifyItemChanged(selectedPosition);
        }
    }

    public interface OnMoodListener {
        void onMoodClick(int position);
    }
}
