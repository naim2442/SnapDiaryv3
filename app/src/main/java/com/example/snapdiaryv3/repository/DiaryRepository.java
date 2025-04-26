package com.example.snapdiaryv3.repository;

import android.app.Application;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.snapdiaryv3.DiaryEntry;
import com.example.snapdiaryv3.database.DiaryDatabase;
import com.example.snapdiaryv3.database.DiaryEntryDao;
import com.example.snapdiaryv3.database.DiaryEntryEntity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository class that handles data operations and manages the synchronization
 * between Room database and Firebase
 */
public class DiaryRepository {
    private static final String TAG = "DiaryRepository";
    
    private final DiaryEntryDao diaryEntryDao;
    private final ExecutorService executorService;
    private final DatabaseReference diaryRef;
    private final FirebaseAuth auth;
    private final Application application;
    
    public DiaryRepository(Application application) {
        this.application = application;
        DiaryDatabase database = DiaryDatabase.getInstance(application);
        diaryEntryDao = database.diaryEntryDao();
        executorService = Executors.newFixedThreadPool(4);
        
        auth = FirebaseAuth.getInstance();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";
        diaryRef = firebaseDatabase.getReference("diaries").child(userId);
    }
    
    /**
     * Saves a diary entry to local database and Firebase if network is available
     */
    public void saveDiaryEntry(DiaryEntry diaryEntry, String entryId) {
        // Convert DiaryEntry to DiaryEntryEntity
        DiaryEntryEntity entity = convertToEntity(diaryEntry, entryId);
        
        // Save to local database
        executorService.execute(() -> {
            diaryEntryDao.insert(entity);
            
            // If online, sync with Firebase
            if (isNetworkAvailable()) {
                syncEntryToFirebase(diaryEntry, entryId);
            }
        });
    }
    
    /**
     * Deletes a diary entry from local database and Firebase if network is available
     */
    public void deleteDiaryEntry(String entryId) {
        executorService.execute(() -> {
            diaryEntryDao.deleteById(entryId);
            
            // If online, delete from Firebase
            if (isNetworkAvailable() && auth.getCurrentUser() != null) {
                diaryRef.child(entryId).removeValue()
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Entry deleted from Firebase"))
                        .addOnFailureListener(e -> Log.e(TAG, "Failed to delete entry from Firebase", e));
            }
        });
    }
    
    /**
     * Gets all diary entries from local database
     */
    public LiveData<List<DiaryEntryEntity>> getAllDiaryEntries() {
        // Fetch from Firebase if online and update local database
        if (isNetworkAvailable() && auth.getCurrentUser() != null) {
            fetchEntriesFromFirebase();
        }
        
        return diaryEntryDao.getAllDiaryEntries();
    }
    
    /**
     * Gets a specific diary entry by ID
     */
    public LiveData<DiaryEntryEntity> getDiaryEntryById(String entryId) {
        return diaryEntryDao.getDiaryEntryById(entryId);
    }
    
    /**
     * Search diary entries by query string
     */
    public LiveData<List<DiaryEntryEntity>> searchDiaryEntries(String query) {
        return diaryEntryDao.searchDiaryEntries(query);
    }
    
    /**
     * Gets diary entries filtered by mood level
     */
    public LiveData<List<DiaryEntryEntity>> getDiaryEntriesByMood(float moodLevel) {
        return diaryEntryDao.getDiaryEntriesByMood(moodLevel);
    }
    
    /**
     * Syncs all unsynced entries to Firebase
     */
    public void syncUnsyncedEntries() {
        if (!isNetworkAvailable() || auth.getCurrentUser() == null) {
            return;
        }
        
        executorService.execute(() -> {
            List<DiaryEntryEntity> unsyncedEntries = diaryEntryDao.getUnsyncedEntries();
            for (DiaryEntryEntity entity : unsyncedEntries) {
                DiaryEntry diaryEntry = convertToModel(entity);
                syncEntryToFirebase(diaryEntry, entity.getEntryId());
            }
        });
    }
    
    /**
     * Syncs a single entry to Firebase
     */
    private void syncEntryToFirebase(DiaryEntry diaryEntry, String entryId) {
        if (auth.getCurrentUser() == null) {
            return;
        }
        
        diaryRef.child(entryId).setValue(diaryEntry)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Entry synced to Firebase");
                    executorService.execute(() -> diaryEntryDao.markAsSynced(entryId));
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to sync entry to Firebase", e));
    }
    
    /**
     * Fetches entries from Firebase and updates local database
     */
    private void fetchEntriesFromFirebase() {
        diaryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                executorService.execute(() -> {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        DiaryEntry entry = dataSnapshot.getValue(DiaryEntry.class);
                        if (entry != null) {
                            String entryId = dataSnapshot.getKey();
                            DiaryEntryEntity entity = convertToEntity(entry, entryId);
                            entity.setSynced(true);
                            diaryEntryDao.insert(entity);
                        }
                    }
                });
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to fetch entries from Firebase", error.toException());
            }
        });
    }
    
    /**
     * Converts DiaryEntry model to DiaryEntryEntity
     */
    private DiaryEntryEntity convertToEntity(DiaryEntry diaryEntry, String entryId) {
        return new DiaryEntryEntity(
                entryId,
                diaryEntry.getDescription(),
                diaryEntry.getMoodLevel(),
                diaryEntry.getImageUri(),
                diaryEntry.getAudioUri(),
                diaryEntry.getAudioFilePath(),
                diaryEntry.getTimestamp(),
                diaryEntry.getLatitude(),
                diaryEntry.getLongitude(),
                null // Location will be resolved later
        );
    }
    
    /**
     * Converts DiaryEntryEntity to DiaryEntry model
     */
    private DiaryEntry convertToModel(DiaryEntryEntity entity) {
        DiaryEntry diaryEntry = new DiaryEntry(
                entity.getDescription(),
                entity.getMoodLevel(),
                entity.getImageUri(),
                entity.getAudioUri(),
                entity.getTimestamp(),
                entity.getAudioFilePath(),
                entity.getLatitude(),
                entity.getLongitude()
        );
        diaryEntry.setEntryId(entity.getEntryId());
        return diaryEntry;
    }
    
    /**
     * Checks if network is available
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) 
                application.getSystemService(Application.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }
}
