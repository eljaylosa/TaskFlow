package com.example.prototype;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prototype.model.Hobby;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class HobbyAdapter extends RecyclerView.Adapter<HobbyAdapter.HobbyViewHolder> {

    private final List<Hobby> hobbyList;
    private final OnHobbyInteractionListener listener;
    private final boolean isInteraction;

    public interface OnHobbyInteractionListener {
        void onHobbyItemClicked(Hobby hobby);
        void onDeleteClicked(Hobby hobby);
    }

    // Constructor for display-only mode (e.g., ProfileActivity)
    public HobbyAdapter(List<Hobby> hobbyList) {
        this.hobbyList = hobbyList;
        this.listener = null;
        this.isInteraction = false;
    }

    // Constructor for interaction mode (e.g., HobbyActivity)
    public HobbyAdapter(List<Hobby> hobbyList, OnHobbyInteractionListener listener) {
        this.hobbyList = hobbyList;
        this.listener = listener;
        this.isInteraction = true;
    }

    @NonNull
    @Override
    public HobbyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hobby, parent, false);
        return new HobbyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HobbyViewHolder holder, int position) {
        Hobby hobby = hobbyList.get(position);
        holder.hobbyName.setText(hobby.getName());

        if (isInteraction && listener != null) {
            holder.deleteButton.setVisibility(View.VISIBLE);

            boolean isLocked = isHobbyLocked(hobby.getLastCheckedIn());

            if (isLocked) {
                holder.itemView.setEnabled(false);
                holder.itemView.setAlpha(0.5f);
            } else {
                holder.itemView.setEnabled(true);
                holder.itemView.setAlpha(1.0f);
                holder.itemView.setOnClickListener(v -> listener.onHobbyItemClicked(hobby));
            }

            holder.deleteButton.setOnClickListener(v -> listener.onDeleteClicked(hobby));

        } else {
            // Display-only mode (e.g., ProfileActivity)
            holder.deleteButton.setVisibility(View.GONE);
            holder.itemView.setEnabled(false); // Make it non-clickable
        }
    }

    private boolean isHobbyLocked(String lastCheckedInDate) {
        if (lastCheckedInDate == null || lastCheckedInDate.isEmpty() || lastCheckedInDate.equals("0")) {
            return false;
        }
        try {
            // The date string from the database is in UTC.
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date lastDate = sdf.parse(lastCheckedInDate);

            // DateUtils.isToday() correctly handles all timezone conversions to determine
            // if the given time falls on the same day as the current device day.
            return DateUtils.isToday(lastDate.getTime());
        } catch (ParseException e) {
            // Log the error for debugging
            e.printStackTrace();
            return false; // Fail safe: assume not locked if parsing fails
        }
    }


    @Override
    public int getItemCount() {
        return hobbyList.size();
    }

    static class HobbyViewHolder extends RecyclerView.ViewHolder {
        TextView hobbyName;
        ImageButton deleteButton;

        HobbyViewHolder(@NonNull View itemView) {
            super(itemView);
            hobbyName = itemView.findViewById(R.id.hobbyName);
            deleteButton = itemView.findViewById(R.id.deleteHobbyButton);
        }
    }
}
