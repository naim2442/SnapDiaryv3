package com.example.snapdiaryv3;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MoodTrackerFragment extends Fragment {

    private RecyclerView recyclerView;
    private DiaryAdapter diaryAdapter;
    private TextView notificationTextView;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public MoodTrackerFragment() {
        // Required empty public constructor
    }

    public static MoodTrackerFragment newInstance(String param1, String param2) {
        MoodTrackerFragment fragment = new MoodTrackerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize your adapter here if necessary
        diaryAdapter = new DiaryAdapter(requireContext(), new ArrayList<>()); // Ensure diaryEntries are populated
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mood_tracker, container, false);
        notificationTextView = view.findViewById(R.id.notificationTextView);
        recyclerView = view.findViewById(R.id.recyclerView);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Assuming you have already set up your RecyclerView and DiaryAdapter
        recyclerView.setAdapter(diaryAdapter);

        // Calculate and display average mood
        float averageMood = calculateAverageMood();
        displayNotification(averageMood);
    }

    private float calculateAverageMood() {
        List<DiaryEntry> entries = diaryAdapter.getDiaryEntries();
        if (entries.isEmpty()) {
            return 0; // Return 0 if no entries are found
        }

        int count = Math.min(entries.size(), 3); // Calculate average for up to 3 entries
        float totalMood = 0;

        // Calculate sum of mood levels for the last 3 entries
        for (int i = entries.size() - 1; i >= Math.max(0, entries.size() - 3); i--) {
            totalMood += entries.get(i).getMoodLevel();
        }

        // Calculate average mood
        return totalMood / count;
    }

    private void displayNotification(float averageMood) {
        String notificationMessage = generateNotificationMessage(averageMood);
        String timestamp = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());

        String notificationEntry = timestamp + " - " + notificationMessage;
        notificationTextView.setText(notificationEntry);

        // Save notifications persistently if needed (e.g., using SharedPreferences, database, etc.)
    }

    private String generateNotificationMessage(float averageMood) {
        Random random = new Random();
        String[] encouragementMessages = {
                "Keep going! You got this!",
                "Every day is a fresh start.",
                "You're stronger than you think."
        };
        String[] congratulatoryMessages = {
                "You're doing great!",
                "Keep up the awesome work!",
                "You're on fire!"
        };

        if (averageMood < 2.0) {
            return encouragementMessages[random.nextInt(encouragementMessages.length)];
        } else if (averageMood > 4.0) {
            return congratulatoryMessages[random.nextInt(congratulatoryMessages.length)];
        } else {
            return "Keep tracking your mood!";
        }
    }
}
