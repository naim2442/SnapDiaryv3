package com.example.snapdiaryv3;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CreateDiaryFragment extends Fragment {

    private static final String TAG = "CreateDiaryFragment";
    private RatingBar ratingBarMood;

    private static final int REQUEST_LOCATION_PERMISSION = 300;

    private EditText editTextDescription;
    private Button buttonCamera, buttonSelectImage, buttonRecordAudio, buttonStopAudio, buttonPlaybackAudio, buttonSave, buttonReset;

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    private Uri imageUri;
    private DatabaseReference databaseReference;
    private DatabaseReference diaryRef;
    private FirebaseAuth mAuth;

    private ActivityResultLauncher<Intent> imageCaptureLauncher;
    private ActivityResultLauncher<Intent> imagePickLauncher;

    // For audio recording and playback
    private MediaRecorder mediaRecorder;
    private String audioFilePath = null;
    private MediaPlayer mediaPlayer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_diary, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference();
        String userId = mAuth.getCurrentUser().getUid();
        diaryRef = databaseReference.child("diaries").child(userId);

        editTextDescription = view.findViewById(R.id.editTextDescription);
        buttonCamera = view.findViewById(R.id.buttonCamera);
        buttonSelectImage = view.findViewById(R.id.buttonSelectImage);
        buttonRecordAudio = view.findViewById(R.id.buttonRecordAudio);
        buttonStopAudio = view.findViewById(R.id.buttonStopAudio);
        buttonPlaybackAudio = view.findViewById(R.id.buttonPlaybackAudio);
        buttonSave = view.findViewById(R.id.buttonSave);
        buttonReset = view.findViewById(R.id.buttonReset);
        ratingBarMood = view.findViewById(R.id.ratingBarMood);


        buttonCamera.setOnClickListener(v -> takePicture());
        buttonSelectImage.setOnClickListener(v -> showImageSourceDialog());
        buttonRecordAudio.setOnClickListener(v -> checkAudioPermissionsAndStartRecording());
        buttonStopAudio.setOnClickListener(v -> stopRecording());
        buttonPlaybackAudio.setOnClickListener(v -> playRecording());
        buttonSave.setOnClickListener(v -> checkLocationPermissionsAndSaveDiary());
        buttonReset.setOnClickListener(v->resetInputs());

        imageCaptureLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        if (imageUri != null) {
                            Toast.makeText(requireContext(), "Image captured!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), "Failed to capture image", Toast.LENGTH_SHORT).show();
                        }
                    }
                });



        imagePickLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK) {
                        if (result.getData() != null && result.getData().getData() != null) {
                            imageUri = result.getData().getData();
                            Toast.makeText(requireContext(), "Image selected!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), "Failed to select image", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private void checkLocationPermissionsAndSaveDiary() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        } else {
            saveDiaryWithLocation();
        }
    }

    private void saveDiaryWithLocation() {
        FusedLocationProviderClient locationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    saveDiary(latitude, longitude);
                } else {
                    Toast.makeText(requireContext(), "Unable to retrieve location", Toast.LENGTH_SHORT).show();
                    // Save diary without location if location is not available
                    saveDiary(null, null);
                }
            });
        }
    }




    private void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(requireContext(), "No camera app found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveDiaryWithLocation();
            } else {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
                // Save diary without location if permission is denied
                saveDiary(null, null);
            }
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e(TAG, "Error creating image file", ex);
                Toast.makeText(requireContext(), "Error creating image file", Toast.LENGTH_SHORT).show();
            }
            if (photoFile != null) {
                imageUri = FileProvider.getUriForFile(requireContext(), "com.example.snapdiaryv3.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                imageCaptureLauncher.launch(takePictureIntent);
            }
        } else {
            Toast.makeText(requireContext(), "No camera app found", Toast.LENGTH_SHORT).show();
        }
    }

    private void showImageSourceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Select Image")
                .setItems(new String[]{"Take Picture", "Choose from Gallery"}, (dialog, which) -> {
                    switch (which) {
                        case 0: // Take Picture
                            takePicture();
                            break;
                        case 1: // Choose from Gallery
                            dispatchPickPictureIntent();
                            break;
                    }
                })
                .show();
    }




    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == getActivity().RESULT_OK) {
            if (data != null && data.getExtras() != null) {
                Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                if (imageBitmap != null) {
                    // Save the image to storage or display it in your UI
                    saveImageToStorage(imageBitmap); // Example method to save image
                } else {
                    Toast.makeText(requireContext(), "Failed to capture image", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(requireContext(), "Failed to capture image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveImageToStorage(Bitmap bitmap) {
        // Example: Save bitmap to a file or database, or display it in an ImageView
        // Here, you can save the bitmap to a specific directory or Firebase Storage


        // For saving to a directory in external storage:
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + ".jpg";

        File storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = new File(storageDir, imageFileName);

        try {
            FileOutputStream fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();

            // Optionally, store the image URI for further use
            imageUri = Uri.fromFile(imageFile);

            // Notify user or perform any other necessary action
            Toast.makeText(requireContext(), "Image saved successfully", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Failed to save image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
    }


    private void dispatchPickPictureIntent() {
        Intent pickPictureIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickLauncher.launch(pickPictureIntent);
    }

    private void checkAudioPermissionsAndStartRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_RECORD_AUDIO_PERMISSION);
            } else {
                startRecording();
            }
        } else {
            startRecording();
        }
    }

    private void startRecording() {
        if (mediaRecorder == null) {
            String fileName = "audio_" + System.currentTimeMillis() + ".3gp";
            File storageDir = requireActivity().getExternalFilesDir("audio");
            if (storageDir != null) {
                audioFilePath = storageDir.getAbsolutePath() + "/" + fileName;
                mediaRecorder = new MediaRecorder();
                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                mediaRecorder.setOutputFile(audioFilePath);
                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

                try {
                    mediaRecorder.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mediaRecorder.start();
                Toast.makeText(requireContext(), "Recording started", Toast.LENGTH_SHORT).show();

                // Update UI
                buttonRecordAudio.setVisibility(View.GONE);
                buttonStopAudio.setVisibility(View.VISIBLE);
                buttonPlaybackAudio.setVisibility(View.GONE);
            }
        }
    }

    private void stopRecording() {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            Toast.makeText(requireContext(), "Recording stopped", Toast.LENGTH_SHORT).show();

            // Update UI
            buttonRecordAudio.setVisibility(View.VISIBLE);
            buttonStopAudio.setVisibility(View.GONE);
            buttonPlaybackAudio.setVisibility(View.VISIBLE);
        }
    }

    private void playRecording() {
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(audioFilePath);
            mediaPlayer.prepare();
            mediaPlayer.start();
            Toast.makeText(requireContext(), "Playing audio", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Failed to play audio", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveDiary(Double latitude, Double longitude) {
        String description = editTextDescription.getText().toString().trim();
        float moodLevel = ratingBarMood.getRating();

        if (description.isEmpty()) {
            Toast.makeText(requireContext(), "Description cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        String entryId = diaryRef.push().getKey();
        if (entryId == null) {
            Toast.makeText(requireContext(), "Failed to generate diary entry key", Toast.LENGTH_SHORT).show();
            return;
        }

        long timestamp = System.currentTimeMillis();
        String audioUri = "some_uri";
        DiaryEntry diaryEntry = new DiaryEntry(description, moodLevel,
                imageUri != null ? imageUri.toString() : null, audioFilePath, timestamp, audioFilePath, latitude, longitude); // Make sure audioFilePath is correctly set here

        diaryRef.child(entryId).setValue(diaryEntry)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), "Diary entry saved successfully", Toast.LENGTH_SHORT).show();
                    resetInputs(); // Clear inputs after saving
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to save diary entry: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void resetInputs() {
        // Clear text input
        editTextDescription.setText("");
        ratingBarMood.setRating(0);

        // Reset image URI
        imageUri = null;

        // Reset audio recording
        stopRecording(); // Stop if recording is in progress
        audioFilePath = null;
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        // Reset UI visibility for audio buttons
        buttonRecordAudio.setVisibility(View.VISIBLE);
        buttonStopAudio.setVisibility(View.GONE);
        buttonPlaybackAudio.setVisibility(View.GONE);

        Toast.makeText(requireContext(), "Inputs reset", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaRecorder != null) {
            mediaRecorder.release();
            mediaRecorder = null;
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}

