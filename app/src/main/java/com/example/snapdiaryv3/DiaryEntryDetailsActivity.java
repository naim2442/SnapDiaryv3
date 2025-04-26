package com.example.snapdiaryv3;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.snapdiaryv3.database.DiaryEntryEntity;
import com.example.snapdiaryv3.viewmodel.DiaryViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DiaryEntryDetailsActivity extends AppCompatActivity {

    private static final String TAG = "DiaryEntryDetailsActivity";
    
    private FirebaseAuth mAuth;
    private DiaryViewModel diaryViewModel;
    private String entryId;

    private TextView textViewDescription;
    private TextView textViewLocation;
    private RatingBar ratingBarMood;
    private ImageView imageViewPhoto;
    private TextView textViewTimestamp;
    private MaterialButton buttonPlaybackAudio;
    private MediaPlayer mediaPlayer;
    private String audioFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_diary_entry_details);

        mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        if (userId == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Initialize ViewModel
        diaryViewModel = new ViewModelProvider(this).get(DiaryViewModel.class);

        // Initialize UI components
        ImageButton buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(this::onBackButtonClick);

        textViewDescription = findViewById(R.id.textViewDescription);
        ratingBarMood = findViewById(R.id.ratingBarMood);
        imageViewPhoto = findViewById(R.id.imageViewPhoto);
        textViewTimestamp = findViewById(R.id.textViewTimestamp);
        buttonPlaybackAudio = findViewById(R.id.buttonPlaybackAudio);
        textViewLocation = findViewById(R.id.textViewLocation);

        entryId = getIntent().getStringExtra("entryId");

        if (entryId != null) {
            fetchEntryDetails(entryId);
        } else {
            Snackbar.make(findViewById(android.R.id.content), "Entry ID not found", Snackbar.LENGTH_SHORT).show();
            finish();
        }

        buttonPlaybackAudio.setOnClickListener(v -> playRecording());
    }

    private void playRecording() {
        if (audioFilePath == null || audioFilePath.isEmpty()) {
            Snackbar.make(findViewById(android.R.id.content), "No audio file available", Snackbar.LENGTH_SHORT).show();
            return;
        }

        // Check if file exists
        File audioFile = new File(audioFilePath);
        if (!audioFile.exists()) {
            Snackbar.make(findViewById(android.R.id.content), "Audio file not found", Snackbar.LENGTH_SHORT).show();
            return;
        }

        // Release previous MediaPlayer if it exists
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(audioFilePath);
            mediaPlayer.prepare();
            mediaPlayer.setOnCompletionListener(mp -> {
                buttonPlaybackAudio.setIcon(getDrawable(R.drawable.ic_play_audio));
                buttonPlaybackAudio.setText(R.string.play_audio);
            });
            mediaPlayer.start();
            
            // Update button to show playing state
            buttonPlaybackAudio.setIcon(getDrawable(R.drawable.ic_stop));
            buttonPlaybackAudio.setText("Stop");
            
            Snackbar.make(findViewById(android.R.id.content), "Playing audio", Snackbar.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Snackbar.make(findViewById(android.R.id.content), "Failed to play audio: " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
        }
    }

    private void fetchEntryDetails(String entryId) {
        diaryViewModel.getDiaryEntryById(entryId).observe(this, entity -> {
            if (entity != null) {
                updateUI(entity);
            } else {
                // Try to fetch from Firebase if not in local database
                Snackbar.make(findViewById(android.R.id.content), "Entry not found in local database", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(DiaryEntryEntity entity) {
        textViewDescription.setText(entity.getDescription());
        ratingBarMood.setRating(entity.getMoodLevel());
        
        if (entity.getImageUri() != null && !entity.getImageUri().isEmpty()) {
            imageViewPhoto.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(entity.getImageUri())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .into(imageViewPhoto);
        } else {
            imageViewPhoto.setVisibility(View.GONE);
        }

        audioFilePath = entity.getAudioFilePath();
        if (audioFilePath != null && !audioFilePath.isEmpty()) {
            buttonPlaybackAudio.setVisibility(View.VISIBLE);
        } else {
            buttonPlaybackAudio.setVisibility(View.GONE);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String dateString = sdf.format(new Date(entity.getTimestamp()));
        textViewTimestamp.setText(dateString);

        // Display location if available
        if (entity.getLatitude() != null && entity.getLongitude() != null) {
            // Call GeocodingHelper to fetch location name
            GeocodingHelper.displayLocationName(this, entity.getLatitude(), entity.getLongitude(), textViewLocation);
        } else {
            textViewLocation.setText("Location not available");
        }
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
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            buttonPlaybackAudio.setIcon(getDrawable(R.drawable.ic_play_audio));
            buttonPlaybackAudio.setText(R.string.play_audio);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
