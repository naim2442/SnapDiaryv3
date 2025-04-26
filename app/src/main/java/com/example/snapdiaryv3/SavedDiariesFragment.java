package com.example.snapdiaryv3;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.snapdiaryv3.database.DiaryEntryEntity;
import com.example.snapdiaryv3.viewmodel.DiaryViewModel;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class SavedDiariesFragment extends Fragment implements DiaryAdapter.OnEntryClickListener {

    private RecyclerView recyclerView;
    private DiaryAdapter diaryAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private DiaryViewModel diaryViewModel;
    private FirebaseAuth mAuth;

    public SavedDiariesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_saved_diaries, container, false);

        mAuth = FirebaseAuth.getInstance();
        
        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        diaryAdapter = new DiaryAdapter(requireContext(), new ArrayList<>());
        recyclerView.setAdapter(diaryAdapter);

        // Set up SwipeRefreshLayout
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.tertiary);
        swipeRefreshLayout.setOnRefreshListener(this::refreshDiaryEntries);

        // Set click listener
        diaryAdapter.setOnEntryClickListener(this);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize ViewModel
        diaryViewModel = new ViewModelProvider(this).get(DiaryViewModel.class);
        
        // Observe diary entries
        observeDiaryEntries();
        
        // Try to sync any unsynced entries when fragment is created
        diaryViewModel.syncUnsyncedEntries();
    }
    
    private void observeDiaryEntries() {
        diaryViewModel.getAllDiaryEntries().observe(getViewLifecycleOwner(), this::updateDiaryEntries);
    }
    
    private void updateDiaryEntries(List<DiaryEntryEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            showEmptyState();
            return;
        }
        
        hideEmptyState();
        
        // Convert DiaryEntryEntity to DiaryEntry for the adapter
        List<DiaryEntry> entries = new ArrayList<>();
        for (DiaryEntryEntity entity : entities) {
            DiaryEntry entry = new DiaryEntry(
                    entity.getDescription(),
                    entity.getMoodLevel(),
                    entity.getImageUri(),
                    entity.getAudioUri(),
                    entity.getTimestamp(),
                    entity.getAudioFilePath(),
                    entity.getLatitude(),
                    entity.getLongitude()
            );
            entry.setEntryId(entity.getEntryId());
            entries.add(entry);
        }
        
        diaryAdapter.setDiaryEntries(entries);
        diaryAdapter.notifyDataSetChanged();
        
        // Stop refresh animation if it's running
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }
    
    private void showEmptyState() {
        // Show empty state view if implemented
        View emptyStateView = getView().findViewById(R.id.emptyStateView);
        if (emptyStateView != null) {
            emptyStateView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }
    
    private void hideEmptyState() {
        // Hide empty state view if implemented
        View emptyStateView = getView().findViewById(R.id.emptyStateView);
        if (emptyStateView != null) {
            emptyStateView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
    
    private void refreshDiaryEntries() {
        swipeRefreshLayout.setRefreshing(true);
        diaryViewModel.syncUnsyncedEntries();
        // The LiveData observer will update the UI when data changes
    }

    @Override
    public void onEntryClicked(DiaryEntry entry) {
        // Handle item click here (optional)
    }

    @Override
    public void onEntryDetailsClicked(DiaryEntry entry) {
        if (mAuth.getCurrentUser() != null) {
            Intent intent = new Intent(requireContext(), DiaryEntryDetailsActivity.class);
            intent.putExtra("entryId", entry.getEntryId());
            requireContext().startActivity(intent);
        } else {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            // Handle authentication flow or redirect to login screen
        }
    }

    @Override
    public void onEntryDeleteClicked(int position) {
        if (mAuth.getCurrentUser() != null) {
            List<DiaryEntry> entries = diaryAdapter.getDiaryEntries();
            if (entries != null && position >= 0 && position < entries.size()) {
                DiaryEntry entryToDelete = entries.get(position);
                String entryId = entryToDelete.getEntryId();

                // Delete using ViewModel
                diaryViewModel.deleteDiaryEntry(entryId);
                
                // Show snackbar with undo option
                Snackbar.make(requireView(), "Diary entry deleted", Snackbar.LENGTH_LONG)
                        .setAction("Undo", v -> {
                            // Recreate the entry if user wants to undo
                            diaryViewModel.saveDiaryEntry(entryToDelete, entryId);
                        })
                        .show();
            } else {
                Toast.makeText(requireContext(), "Invalid entry position", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }
}
