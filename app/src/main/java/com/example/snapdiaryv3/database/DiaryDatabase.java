package com.example.snapdiaryv3.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * Room database for storing diary entries locally
 */
@Database(entities = {DiaryEntryEntity.class}, version = 1, exportSchema = false)
public abstract class DiaryDatabase extends RoomDatabase {
    
    private static final String DATABASE_NAME = "diary_database";
    private static DiaryDatabase instance;
    
    public abstract DiaryEntryDao diaryEntryDao();
    
    // Singleton pattern to ensure only one instance of the database is created
    public static synchronized DiaryDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    DiaryDatabase.class,
                    DATABASE_NAME)
                    .fallbackToDestructiveMigration() // Recreate database if no Migration object specified
                    .build();
        }
        return instance;
    }
}
