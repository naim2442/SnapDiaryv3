package com.example.snapdiaryv3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.FirebaseApp;
import com.google.android.gms.maps.model.LatLng;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private LatLng diaryLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        auth = FirebaseAuth.getInstance();

        // Check if user is logged in
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            // User is logged in, navigate to ProfileActivity
            navigateToProfile();
        }
    }

    public void openRegister(View v) {
        Intent i = new Intent(this, RegisterActivity.class);
        startActivity(i);
    }

    public void openLogin(View v) {
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
    }

    private void navigateToProfile() {
        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
        startActivity(intent);
        finish();
    }

    public void setDiaryLocation(LatLng location) {
        this.diaryLocation = location;
        Intent intent = new Intent(MainActivity.this, CreateDiaryFragment.class);
        intent.putExtra("location", location);
        startActivity(intent);
    }

    public LatLng getDiaryLocation() {
        return diaryLocation;
    }
}
