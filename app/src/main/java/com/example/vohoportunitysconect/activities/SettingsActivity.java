package com.example.vohoportunitysconect.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.vohoportunitysconect.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SettingsActivity extends AppCompatActivity {
    private MaterialButton changePasswordButton;
    private MaterialButton deleteAccountButton;
    private SwitchMaterial notificationsSwitch;
    private SwitchMaterial emailNotificationsSwitch;
    private SwitchMaterial darkModeSwitch;
    private SwitchMaterial locationServicesSwitch;
    
    private FirebaseAuth mAuth;
    private DatabaseReference databaseRef;
    private FirebaseUser currentUser;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference();
        currentUser = mAuth.getCurrentUser();
        sharedPreferences = getSharedPreferences("app_settings", MODE_PRIVATE);

        // Initialize views
        initializeViews();
        setupClickListeners();
        loadUserSettings();
        loadAppSettings();
    }

    private void initializeViews() {
        changePasswordButton = findViewById(R.id.change_password_button);
        deleteAccountButton = findViewById(R.id.delete_account_button);
        notificationsSwitch = findViewById(R.id.notifications_switch);
        emailNotificationsSwitch = findViewById(R.id.email_notifications_switch);
        darkModeSwitch = findViewById(R.id.dark_mode_switch);
        locationServicesSwitch = findViewById(R.id.location_services_switch);
    }

    private void setupClickListeners() {
        changePasswordButton.setOnClickListener(v -> {
            if (currentUser != null) {
                showChangePasswordDialog();
            } else {
                Toast.makeText(this, "Please sign in to change password", Toast.LENGTH_SHORT).show();
            }
        });

        deleteAccountButton.setOnClickListener(v -> {
            if (currentUser != null) {
                showDeleteAccountConfirmation();
            } else {
                Toast.makeText(this, "Please sign in to delete account", Toast.LENGTH_SHORT).show();
            }
        });

        notificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (currentUser != null) {
                saveNotificationSettings(isChecked, emailNotificationsSwitch.isChecked());
            }
        });

        emailNotificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (currentUser != null) {
                saveNotificationSettings(notificationsSwitch.isChecked(), isChecked);
            }
        });

        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveDarkModeSetting(isChecked);
            applyDarkMode(isChecked);
        });

        locationServicesSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveLocationServicesSetting(isChecked);
            if (isChecked) {
                requestLocationPermissions();
            }
        });
    }

    private void showChangePasswordDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Change Password");
        
        View view = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        builder.setView(view);
        
        builder.setPositiveButton("Change", (dialog, which) -> {
            // TODO: Implement password change logic
            Toast.makeText(this, "Password change coming soon!", Toast.LENGTH_SHORT).show();
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void loadUserSettings() {
        if (currentUser != null) {
            String userId = currentUser.getUid();
            databaseRef.child("users").child(userId).child("settings")
                    .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                        @Override
                        public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                Boolean notificationsEnabled = dataSnapshot.child("notificationsEnabled").getValue(Boolean.class);
                                Boolean emailNotificationsEnabled = dataSnapshot.child("emailNotificationsEnabled").getValue(Boolean.class);
                                
                                if (notificationsEnabled != null) {
                                    notificationsSwitch.setChecked(notificationsEnabled);
                                }
                                if (emailNotificationsEnabled != null) {
                                    emailNotificationsSwitch.setChecked(emailNotificationsEnabled);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(com.google.firebase.database.DatabaseError databaseError) {
                            Toast.makeText(SettingsActivity.this, "Error loading settings", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void loadAppSettings() {
        boolean isDarkMode = sharedPreferences.getBoolean("dark_mode", false);
        boolean isLocationEnabled = sharedPreferences.getBoolean("location_services", false);
        
        darkModeSwitch.setChecked(isDarkMode);
        locationServicesSwitch.setChecked(isLocationEnabled);
    }

    private void saveNotificationSettings(boolean notificationsEnabled, boolean emailNotificationsEnabled) {
        if (currentUser != null) {
            String userId = currentUser.getUid();
            databaseRef.child("users").child(userId).child("settings")
                    .child("notificationsEnabled").setValue(notificationsEnabled);
            databaseRef.child("users").child(userId).child("settings")
                    .child("emailNotificationsEnabled").setValue(emailNotificationsEnabled);
        }
    }

    private void saveDarkModeSetting(boolean isEnabled) {
        sharedPreferences.edit().putBoolean("dark_mode", isEnabled).apply();
    }

    private void saveLocationServicesSetting(boolean isEnabled) {
        sharedPreferences.edit().putBoolean("location_services", isEnabled).apply();
    }

    private void applyDarkMode(boolean isEnabled) {
        if (isEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void requestLocationPermissions() {
        // TODO: Implement location permissions request
        Toast.makeText(this, "Location services coming soon!", Toast.LENGTH_SHORT).show();
    }

    private void showDeleteAccountConfirmation() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteAccount())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteAccount() {
        if (currentUser != null) {
            // Delete user data from Realtime Database
            String userId = currentUser.getUid();
            databaseRef.child("users").child(userId).removeValue()
                    .addOnSuccessListener(aVoid -> {
                        // Delete user from Firebase Auth
                        currentUser.delete()
                                .addOnSuccessListener(aVoid1 -> {
                                    Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(this, LoginActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, "Error deleting account: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error deleting user data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
} 