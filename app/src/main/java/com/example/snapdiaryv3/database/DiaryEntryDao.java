package com.example.snapdiaryv3.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * Data Access Object for the diary entries table
 */
@Dao
public interface DiaryEntryDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(DiaryEntryEntity diaryEntry);
    
    @Update
    void update(DiaryEntryEntity diaryEntry);
    
    @Delete
    void delete(DiaryEntryEntity diaryEntry);
    
    @Query("DELETE FROM diary_entries WHERE entryId = :entryId")
    void deleteById(String entryId);
    
    @Query("SELECT * FROM diary_entries ORDER BY timestamp DESC")
    LiveData<List<DiaryEntryEntity>> getAllDiaryEntries();
    
    @Query("SELECT * FROM diary_entries WHERE entryId = :entryId")
    LiveData<DiaryEntryEntity> getDiaryEntryById(String entryId);
    
    @Query("SELECT * FROM diary_entries WHERE isSynced = 0")
    List<DiaryEntryEntity> getUnsyncedEntries();
    
    @Query("UPDATE diary_entries SET isSynced = 1 WHERE entryId = :entryId")
    void markAsSynced(String entryId);
    
    @Query("SELECT * FROM diary_entries WHERE description LIKE '%' || :query || '%'")
    LiveData<List<DiaryEntryEntity>> searchDiaryEntries(String query);
    
    @Query("SELECT * FROM diary_entries WHERE moodLevel = :moodLevel ORDER BY timestamp DESC")
    LiveData<List<DiaryEntryEntity>> getDiaryEntriesByMood(float moodLevel);
}
