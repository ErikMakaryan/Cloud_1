package com.example.vohoportunitysconect.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vohoportunitysconect.databinding.ActivityRegisterBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference();

        binding.registerButton.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String email = binding.emailInput.getText().toString().trim();
        String password = binding.passwordInput.getText().toString().trim();
        String name = binding.nameInput.getText().toString().trim();
        boolean isOrganization = binding.organizationCheckbox.isChecked();

        // Validate inputs
        if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading dialog
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.registerButton.setEnabled(false);

        // Create user with email and password
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        // Send email verification
                        user.sendEmailVerification()
                            .addOnCompleteListener(verificationTask -> {
                                if (verificationTask.isSuccessful()) {
                                    // Save user data to database
                                    saveUserData(user.getUid(), name, email, isOrganization);
                                    
                                    // Show verification dialog
                                    showVerificationDialog();
                                } else {
                                    binding.progressBar.setVisibility(View.GONE);
                                    binding.registerButton.setEnabled(true);
                                    Toast.makeText(RegisterActivity.this,
                                        "Failed to send verification email: " + verificationTask.getException().getMessage(),
                                        Toast.LENGTH_SHORT).show();
                                }
                            });
                    }
                } else {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.registerButton.setEnabled(true);
                    Toast.makeText(RegisterActivity.this,
                        "Registration failed: " + task.getException().getMessage(),
                        Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void saveUserData(String userId, String name, String email, boolean isOrganization) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("email", email);
        userData.put("isOrganization", isOrganization);
        userData.put("volunteerHours", 0);
        userData.put("createdAt", System.currentTimeMillis());

        databaseRef.child("users").child(userId).setValue(userData)
            .addOnSuccessListener(aVoid -> {
                binding.progressBar.setVisibility(View.GONE);
                binding.registerButton.setEnabled(true);
            })
            .addOnFailureListener(e -> {
                binding.progressBar.setVisibility(View.GONE);
                binding.registerButton.setEnabled(true);
                Toast.makeText(RegisterActivity.this,
                    "Failed to save user data: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            });
    }

    private void showVerificationDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Email Verification Required")
            .setMessage("A verification email has been sent to your email address. " +
                "Please verify your email before logging in.")
            .setPositiveButton("OK", (dialog, which) -> {
                // Sign out the user
                mAuth.signOut();
                // Go to login activity
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            })
            .setCancelable(false)
            .show();
    }
} 