package com.example.snapdiaryv3;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DiaryAdapter extends RecyclerView.Adapter<DiaryAdapter.DiaryViewHolder> {

    private Context context;
    private static List<DiaryEntry> diaryEntries;
    private static OnEntryClickListener onEntryClickListener;

    public interface OnEntryClickListener {
        void onEntryDeleteClicked(int position);
        void onEntryClicked(DiaryEntry entry);
        void onEntryDetailsClicked(DiaryEntry entry);
    }

    public DiaryAdapter(Context context, List<DiaryEntry> diaryEntries) {
        this.context = context;
        this.diaryEntries = diaryEntries;
    }

    public void setOnEntryClickListener(OnEntryClickListener listener) {
        this.onEntryClickListener = listener;
    }

    public List<DiaryEntry> getDiaryEntries() {
        return diaryEntries;
    }

    public void setDiaryEntries(List<DiaryEntry> diaryEntries) {
        this.diaryEntries = diaryEntries;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DiaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_diary_entry, parent, false);
        return new DiaryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiaryViewHolder holder, int position) {
        DiaryEntry entry = diaryEntries.get(position);
        holder.textViewDescription.setText(entry.getDescription());
        holder.ratingBarMood.setRating(entry.getMoodLevel());
        holder.ratingBarMood.setIsIndicator(true); // Disable RatingBar

        if (entry.getImageUri() != null) {
            holder.imageViewPhoto.setVisibility(View.VISIBLE);
            Glide.with(context).load(entry.getImageUri()).into(holder.imageViewPhoto);
        } else {
            holder.imageViewPhoto.setVisibility(View.GONE);
        }

        if (entry.getAudioUri() != null) {
            holder.buttonPlayAudio.setVisibility(View.VISIBLE);
            holder.buttonPlayAudio.setOnClickListener(v -> {
                // Handle audio playback if needed
            });
        } else {
            holder.buttonPlayAudio.setVisibility(View.GONE);
        }

        holder.buttonDelete.setOnClickListener(v -> {
            if (onEntryClickListener != null) {
                onEntryClickListener.onEntryDeleteClicked(position);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (onEntryClickListener != null) {
                onEntryClickListener.onEntryClicked(entry);
            }
        });

        holder.buttonDetails.setOnClickListener(v -> {
            if (onEntryClickListener != null) {
                onEntryClickListener.onEntryDetailsClicked(entry);
            }
        });

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String dateString = sdf.format(new Date(entry.getTimestamp()));
        holder.textViewTimestamp.setText(dateString);
    }

    @Override
    public int getItemCount() {
        return diaryEntries.size();
    }

    public static class DiaryViewHolder extends RecyclerView.ViewHolder {

        TextView textViewDescription;
        RatingBar ratingBarMood;
        ImageView imageViewPhoto;
        Button buttonPlayAudio;
        Button buttonDelete;
        TextView textViewTimestamp;
        Button buttonDetails;

        public DiaryViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
            ratingBarMood = itemView.findViewById(R.id.ratingBarMood);
            imageViewPhoto = itemView.findViewById(R.id.imageViewPhoto);
            buttonPlayAudio = itemView.findViewById(R.id.buttonPlayAudio);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
            textViewTimestamp = itemView.findViewById(R.id.textViewTimestamp);
            buttonDetails = itemView.findViewById(R.id.buttonDetails);

            itemView.setOnClickListener(v -> {
                if (onEntryClickListener != null) {
                    onEntryClickListener.onEntryClicked(diaryEntries.get(getAdapterPosition()));
                }
            });

            buttonDetails.setOnClickListener(v -> {
                if (onEntryClickListener != null) {
                    onEntryClickListener.onEntryDetailsClicked(diaryEntries.get(getAdapterPosition()));
                }
            });

            buttonDelete.setOnClickListener(v -> {
                if (onEntryClickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onEntryClickListener.onEntryDeleteClicked(position);
                    }
                }
            });
        }
    }
}
