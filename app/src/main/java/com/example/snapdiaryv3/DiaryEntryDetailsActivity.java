package com.example.snapdiaryv3;

import static androidx.core.content.ContentProviderCompat.requireContext;


import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DiaryEntryDetailsActivity extends AppCompatActivity implements DiaryAdapter.OnEntryClickListener  {


    private DiaryAdapter diaryAdapter;
    private FirebaseAuth mAuth;
    private DatabaseReference diaryRef;
    private String userId;

    private TextView textViewDescription;

    private TextView textViewLocation;
    private RatingBar ratingBarMood;
    private ImageView imageViewPhoto;
    private TextView textViewTimestamp;
    private Button buttonPlaybackAudio; // Changed to Button for playback
    private MediaPlayer mediaPlayer;
    private String audioFilePath;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_diary_entry_details);

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        if (userId == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }




        diaryRef = FirebaseDatabase.getInstance().getReference("diaries").child(userId);

        ImageButton buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(this::onBackButtonClick);

        textViewDescription = findViewById(R.id.textViewDescription);
        ratingBarMood = findViewById(R.id.ratingBarMood);
        imageViewPhoto = findViewById(R.id.imageViewPhoto);
        textViewTimestamp = findViewById(R.id.textViewTimestamp);
        buttonPlaybackAudio = findViewById(R.id.buttonPlaybackAudio);
        textViewLocation = findViewById(R.id.textViewLocation);

        String entryId = getIntent().getStringExtra("entryId");

        if (entryId != null) {
            fetchEntryDetails(entryId);
        } else {
            Toast.makeText(this, "Entry ID not found", Toast.LENGTH_SHORT).show();
            finish();
        }

        buttonPlaybackAudio.setOnClickListener(v -> playRecording());
    }

    private void playRecording() {
        if (audioFilePath == null) {
            Toast.makeText(this, "Audio file path is null", Toast.LENGTH_SHORT).show();
            return;
        }

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(audioFilePath);
            mediaPlayer.prepare();
            mediaPlayer.start();
            Toast.makeText(this, "Playing audio", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to play audio", Toast.LENGTH_SHORT).show();
        }
    }


    private void fetchEntryDetails(String entryId) {
        diaryRef.child(entryId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DiaryEntry entry = snapshot.getValue(DiaryEntry.class);
                if (entry != null) {
                    textViewDescription.setText(entry.getDescription());
                    ratingBarMood.setRating(entry.getMoodLevel());
                    if (entry.getImageUri() != null) {
                        Glide.with(DiaryEntryDetailsActivity.this)
                                .load(entry.getImageUri())
                                .into(imageViewPhoto);
                    } else {
                        imageViewPhoto.setVisibility(View.GONE);
                    }

                    audioFilePath = entry.getAudioFilePath();

                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                    String dateString = sdf.format(new Date(entry.getTimestamp()));
                    textViewTimestamp.setText(dateString);

                    // Display location if available
                    if (entry.getLatitude() != null && entry.getLongitude() != null) {
                        // Call GeocodingHelper to fetch location name
                        GeocodingHelper.displayLocationName(DiaryEntryDetailsActivity.this, entry.getLatitude(), entry.getLongitude(), textViewLocation);
                    } else {
                        textViewLocation.setText("Location not available");
                    }
                } else {
                    Toast.makeText(DiaryEntryDetailsActivity.this, "Entry not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DiaryEntryDetailsActivity.this, "Failed to load entry details: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    @Override
    public void onEntryDetailsClicked(DiaryEntry entry) {

    }


    public void onEntryClicked(DiaryEntry entry) {

    }

    public void onBackButtonClick(View view) {
        onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onEntryDeleteClicked(int position) {
        if (mAuth.getCurrentUser() != null) {
            List<DiaryEntry> entries = diaryAdapter.getDiaryEntries();
            if (entries != null && position >= 0 && position < entries.size()) {
                DiaryEntry entryToDelete = entries.get(position);
                String entryId = entryToDelete.getEntryId();

                diaryRef.child(entryId).removeValue()
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(DiaryEntryDetailsActivity.this, "Diary entry deleted", Toast.LENGTH_SHORT).show();
                            // Optionally, refresh your diary entries here if needed
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(DiaryEntryDetailsActivity.this, "Failed to delete diary entry: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(DiaryEntryDetailsActivity.this, "Invalid entry position", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(DiaryEntryDetailsActivity.this, "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }





}

