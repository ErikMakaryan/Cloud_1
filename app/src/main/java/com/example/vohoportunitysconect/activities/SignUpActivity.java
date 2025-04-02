package com.example.vohoportunitysconect.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vohoportunitysconect.MainActivity;
import com.example.vohoportunitysconect.R;
import com.example.vohoportunitysconect.VOHApplication;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {
    private static final String TAG = "SignUpActivity";

    private EditText nameInput;
    private EditText emailInput;
    private EditText passwordInput;
    private EditText confirmPasswordInput;
    private RadioGroup userTypeGroup;
    private Button signUpButton;
    private TextView loginLink;
    private ProgressBar progressBar;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseAuth.getInstance().getDatabase().getReference();

        // Initialize views
        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        nameInput = findViewById(R.id.name_input);
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        confirmPasswordInput = findViewById(R.id.confirm_password_input);
        userTypeGroup = findViewById(R.id.user_type_group);
        signUpButton = findViewById(R.id.signup_button);
        loginLink = findViewById(R.id.login_link);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupClickListeners() {
        signUpButton.setOnClickListener(v -> handleSignUp());
        loginLink.setOnClickListener(v -> finish());
    }

    private void handleSignUp() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();
        String userType = userTypeGroup.getCheckedRadioButtonId() == R.id.volunteer_radio ? "volunteer" : "organization";

        if (validateInput(name, email, password, confirmPassword)) {
            showLoading(true);
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Update user profile
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();

                            task.getResult().getUser().updateProfile(profileUpdates)
                                    .addOnCompleteListener(profileTask -> {
                                        if (profileTask.isSuccessful()) {
                                            // Save user data to Realtime Database
                                            saveUserToFirestore(task.getResult().getUser().getUid(), name, email, userType);
                                        } else {
                                            showLoading(false);
                                            Log.e(TAG, "Error updating profile", profileTask.getException());
                                            Toast.makeText(SignUpActivity.this,
                                                    "Error updating profile: " + profileTask.getException().getMessage(),
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            showLoading(false);
                            Log.e(TAG, "Sign up failed", task.getException());
                            Toast.makeText(SignUpActivity.this,
                                    "Sign up failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void saveUserToFirestore(String userId, String name, String email, String userType) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("email", email);
        userData.put("userType", userType);
        userData.put("createdAt", System.currentTimeMillis());

        databaseRef.child("users").child(userId).setValue(userData)
                .addOnSuccessListener(aVoid -> {
                    // Save user data to local preferences
                    VOHApplication.getInstance().getDataManager()
                            .saveUserData(userId, email, name);
                    VOHApplication.getInstance().getDataManager()
                            .saveUserType(userType);
                    // Save password for persistent sign-in
                    VOHApplication.getInstance().getDataManager()
                            .saveCurrentUserPassword(passwordInput.getText().toString().trim());
                    navigateToMain();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Log.e(TAG, "Error saving user data", e);
                    Toast.makeText(SignUpActivity.this,
                            "Error saving user data: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private boolean validateInput(String name, String email, String password, String confirmPassword) {
        if (name.isEmpty()) {
            nameInput.setError("Name is required");
            return false;
        }
        if (email.isEmpty()) {
            emailInput.setError("Email is required");
            return false;
        }
        if (password.isEmpty()) {
            passwordInput.setError("Password is required");
            return false;
        }
        if (confirmPassword.isEmpty()) {
            confirmPasswordInput.setError("Confirm password is required");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            confirmPasswordInput.setError("Passwords do not match");
            return false;
        }
        if (password.length() < 6) {
            passwordInput.setError("Password must be at least 6 characters");
            return false;
        }
        return true;
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        signUpButton.setEnabled(!isLoading);
        nameInput.setEnabled(!isLoading);
        emailInput.setEnabled(!isLoading);
        passwordInput.setEnabled(!isLoading);
        confirmPasswordInput.setEnabled(!isLoading);
        userTypeGroup.setEnabled(!isLoading);
        loginLink.setEnabled(!isLoading);
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
} 