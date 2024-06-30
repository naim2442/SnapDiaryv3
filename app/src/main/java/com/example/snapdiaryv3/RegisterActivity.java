package com.example.snapdiaryv3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private ImageButton backButton;
    private ImageView logo;
    private TextView title;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private Button signUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        backButton = findViewById(R.id.backButton);
        logo = findViewById(R.id.logo);
        title = findViewById(R.id.title);
        emailEditText = findViewById(R.id.editText);
        passwordEditText = findViewById(R.id.editText2);
        confirmPasswordEditText = findViewById(R.id.editTextConfirmPassword);
        signUpButton = findViewById(R.id.button3);

        signUpButton.setOnClickListener(v -> createUser());
        backButton.setOnClickListener(v -> finish());
    }

    private void createUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        if (validateInput(email, password, confirmPassword)) {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "Registration successful. Please login to continue.", Toast.LENGTH_LONG).show();
                            FirebaseUser user = mAuth.getCurrentUser();
                            navigateToLogin(true);
                        } else {
                            Toast.makeText(RegisterActivity.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            Toast.makeText(this, "Please check your email and passwords", Toast.LENGTH_LONG).show();
        }
    }

    private boolean validateInput(String email, String password, String confirmPassword) {
        return email.contains("@") && password.length() > 6 && password.equals(confirmPassword);
    }

    private void navigateToLogin(boolean fromRegister) {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("fromRegister", fromRegister);
        startActivity(intent);
        finish();
    }
}
