package com.example.snapdiaryv3.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.snapdiaryv3.DiaryEntry;
import com.example.snapdiaryv3.database.DiaryEntryEntity;
import com.example.snapdiaryv3.repository.DiaryRepository;

import java.util.List;

/**
 * ViewModel to handle business logic and provide data to UI components
 */
public class DiaryViewModel extends AndroidViewModel {
    
    private final DiaryRepository repository;
    private final MutableLiveData<Boolean> isSaving = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isDeleting = new MutableLiveData<>(false);
    
    public DiaryViewModel(@NonNull Application application) {
        super(application);
        repository = new DiaryRepository(application);
    }
    
    /**
     * Saves a diary entry
     */
    public void saveDiaryEntry(DiaryEntry diaryEntry, String entryId) {
        isSaving.setValue(true);
        repository.saveDiaryEntry(diaryEntry, entryId);
        isSaving.setValue(false);
    }
    
    /**
     * Deletes a diary entry
     */
    public void deleteDiaryEntry(String entryId) {
        isDeleting.setValue(true);
        repository.deleteDiaryEntry(entryId);
        isDeleting.setValue(false);
    }
    
    /**
     * Gets all diary entries
     */
    public LiveData<List<DiaryEntryEntity>> getAllDiaryEntries() {
        return repository.getAllDiaryEntries();
    }
    
    /**
     * Gets a specific diary entry by ID
     */
    public LiveData<DiaryEntryEntity> getDiaryEntryById(String entryId) {
        return repository.getDiaryEntryById(entryId);
    }
    
    /**
     * Search diary entries by query string
     */
    public LiveData<List<DiaryEntryEntity>> searchDiaryEntries(String query) {
        return repository.searchDiaryEntries(query);
    }
    
    /**
     * Gets diary entries filtered by mood level
     */
    public LiveData<List<DiaryEntryEntity>> getDiaryEntriesByMood(float moodLevel) {
        return repository.getDiaryEntriesByMood(moodLevel);
    }
    
    /**
     * Syncs all unsynced entries to Firebase
     */
    public void syncUnsyncedEntries() {
        repository.syncUnsyncedEntries();
    }
    
    /**
     * Returns if a save operation is in progress
     */
    public LiveData<Boolean> isSaving() {
        return isSaving;
    }
    
    /**
     * Returns if a delete operation is in progress
     */
    public LiveData<Boolean> isDeleting() {
        return isDeleting;
    }
}
