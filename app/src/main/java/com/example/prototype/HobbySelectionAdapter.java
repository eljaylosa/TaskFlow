package com.example.prototype;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.prototype.model.Hobby;
import java.util.List;

public class HobbySelectionAdapter extends RecyclerView.Adapter<HobbySelectionAdapter.HobbyViewHolder> {

    private final List<Hobby> allHobbies;
    private final OnHobbyDeleteListener deleteListener;

    // The listener interface for delete actions
    public interface OnHobbyDeleteListener {
        void onHobbyDelete(Hobby hobby);
    }

    // This is the corrected constructor that fixes the build error.
    // It accepts a list of hobbies and the delete listener.
    public HobbySelectionAdapter(List<Hobby> allHobbies, OnHobbyDeleteListener deleteListener) {
        this.allHobbies = allHobbies;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public HobbyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hobby_selection, parent, false);
        return new HobbyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HobbyViewHolder holder, int position) {
        Hobby hobby = allHobbies.get(position);
        holder.hobbyCheckBox.setText(hobby.getName());

        // This screen is only for managing hobbies (add/delete), not selecting them.
        // We disable the checkbox to avoid user confusion.
        holder.hobbyCheckBox.setClickable(false);
        holder.hobbyCheckBox.setChecked(false); // Always show as unchecked

        // Set up the delete button listener
        holder.deleteButton.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onHobbyDelete(hobby);
            }
        });
    }

    @Override
    public int getItemCount() {
        return allHobbies.size();
    }

    static class HobbyViewHolder extends RecyclerView.ViewHolder {
        CheckBox hobbyCheckBox;
        ImageButton deleteButton;

        public HobbyViewHolder(@NonNull View itemView) {
            super(itemView);
            hobbyCheckBox = itemView.findViewById(R.id.hobbyCheckBox);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
