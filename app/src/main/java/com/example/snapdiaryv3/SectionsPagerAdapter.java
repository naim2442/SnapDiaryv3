package com.example.snapdiaryv3;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class SectionsPagerAdapter extends FragmentStateAdapter {
    public SectionsPagerAdapter(FragmentActivity fa) {
        super(fa);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new HomeFragment();
            case 1:
                return new CreateDiaryFragment();
            case 2:
                return new SavedDiariesFragment();
            case 3:
                return new MoodTrackerFragment();
            case 4:
                return new UserProfileFragment();
            case 5:
                return new ReminderFragment();  // Added the new fragment here
            default:
                return new HomeFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 6; // Updated the count to 6
    }
}
