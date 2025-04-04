package com.example.vohoportunitysconect.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.vohoportunitysconect.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SettingsActivity extends AppCompatActivity {
    private MaterialButton changePasswordButton;
    private MaterialButton deleteAccountButton;
    private MaterialButton addOpportunityButton;
    private SwitchMaterial notificationsSwitch;
    private SwitchMaterial emailNotificationsSwitch;
    private SwitchMaterial darkModeSwitch;
    private SwitchMaterial locationServicesSwitch;
    private MaterialButton returnButton;
    
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
        addOpportunityButton = findViewById(R.id.add_opportunity_button);
        notificationsSwitch = findViewById(R.id.notifications_switch);
        emailNotificationsSwitch = findViewById(R.id.email_notifications_switch);
        darkModeSwitch = findViewById(R.id.dark_mode_switch);
        locationServicesSwitch = findViewById(R.id.location_services_switch);
        returnButton = findViewById(R.id.return_button);
    }

    private void setupClickListeners() {
        changePasswordButton.setOnClickListener(v -> {
            if (currentUser != null) {
                showChangePasswordDialog();
            } else {
                showSnackbar("Please sign in to change password");
            }
        });

        deleteAccountButton.setOnClickListener(v -> {
            if (currentUser != null) {
                showDeleteAccountConfirmation();
            } else {
                showSnackbar("Please sign in to delete account");
            }
        });

        addOpportunityButton.setOnClickListener(v -> {
            if (currentUser != null) {
                startActivity(new Intent(this, CreateOpportunityActivity.class));
            } else {
                showSnackbar("Please sign in to add opportunities");
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

        returnButton.setOnClickListener(v -> finish());
    }

    private void showChangePasswordDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        TextInputEditText currentPasswordInput = view.findViewById(R.id.current_password_input);
        TextInputEditText newPasswordInput = view.findViewById(R.id.new_password_input);
        TextInputEditText confirmPasswordInput = view.findViewById(R.id.confirm_password_input);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Change Password")
                .setView(view)
                .setPositiveButton("Change", (dialog, which) -> {
                    String currentPassword = currentPasswordInput.getText().toString().trim();
                    String newPassword = newPasswordInput.getText().toString().trim();
                    String confirmPassword = confirmPasswordInput.getText().toString().trim();

                    if (validatePasswordInputs(currentPassword, newPassword, confirmPassword)) {
                        changePassword(currentPassword, newPassword);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private boolean validatePasswordInputs(String currentPassword, String newPassword, String confirmPassword) {
        if (TextUtils.isEmpty(currentPassword)) {
            showSnackbar("Please enter your current password");
            return false;
        }
        if (TextUtils.isEmpty(newPassword)) {
            showSnackbar("Please enter a new password");
            return false;
        }
        if (newPassword.length() < 6) {
            showSnackbar("New password must be at least 6 characters");
            return false;
        }
        if (!newPassword.equals(confirmPassword)) {
            showSnackbar("New passwords do not match");
            return false;
        }
        return true;
    }

    private void changePassword(String currentPassword, String newPassword) {
        AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), currentPassword);
        
        currentUser.reauthenticate(credential)
                .addOnSuccessListener(aVoid -> {
                    currentUser.updatePassword(newPassword)
                            .addOnSuccessListener(aVoid1 -> {
                                showSnackbar("Password changed successfully");
                            })
                            .addOnFailureListener(e -> {
                                showSnackbar("Failed to change password: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    showSnackbar("Authentication failed: " + e.getMessage());
                });
    }

    private void loadUserSettings() {
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference settingsRef = databaseRef.child("users").child(userId).child("settings");
            
            settingsRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                @Override
                public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                    try {
                        if (dataSnapshot.exists()) {
                            Boolean notificationsEnabled = dataSnapshot.child("notificationsEnabled").getValue(Boolean.class);
                            Boolean emailNotificationsEnabled = dataSnapshot.child("emailNotificationsEnabled").getValue(Boolean.class);
                            
                            if (notificationsEnabled != null) {
                                notificationsSwitch.setChecked(notificationsEnabled);
                            }
                            if (emailNotificationsEnabled != null) {
                                emailNotificationsSwitch.setChecked(emailNotificationsEnabled);
                            }
                        } else {
                            // Initialize default settings if they don't exist
                            saveNotificationSettings(true, true);
                        }
                    } catch (Exception e) {
                        showSnackbar("Error loading settings: " + e.getMessage());
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    showSnackbar("Error loading settings: " + databaseError.getMessage());
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
            DatabaseReference settingsRef = databaseRef.child("users").child(userId).child("settings");
            
            settingsRef.child("notificationsEnabled").setValue(notificationsEnabled)
                    .addOnSuccessListener(aVoid -> {
                        settingsRef.child("emailNotificationsEnabled").setValue(emailNotificationsEnabled)
                                .addOnSuccessListener(aVoid1 -> {
                                    showSnackbar("Settings saved successfully");
                                })
                                .addOnFailureListener(e -> {
                                    showSnackbar("Error saving email notification settings: " + e.getMessage());
                                });
                    })
                    .addOnFailureListener(e -> {
                        showSnackbar("Error saving notification settings: " + e.getMessage());
                    });
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
        showSnackbar("Location services coming soon!");
    }

    private void showDeleteAccountConfirmation() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteAccount())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteAccount() {
        if (currentUser != null) {
            String userId = currentUser.getUid();
            
            // First, delete all user data
            databaseRef.child("users").child(userId).removeValue()
                    .addOnSuccessListener(aVoid -> {
                        // Then delete the user's applications
                        databaseRef.child("applications").orderByChild("userId").equalTo(userId)
                                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                                    @Override
                                    public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                                        for (com.google.firebase.database.DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                            snapshot.getRef().removeValue();
                                        }
                                        // Finally, delete the Firebase Auth account
                                        deleteFirebaseAccount();
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        showSnackbar("Error deleting applications: " + databaseError.getMessage());
                                    }
                                });
                    })
                    .addOnFailureListener(e -> {
                        showSnackbar("Error deleting user data: " + e.getMessage());
                    });
        }
    }

    private void deleteFirebaseAccount() {
        currentUser.delete()
                .addOnSuccessListener(aVoid -> {
                    showSnackbar("Account deleted successfully");
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    showSnackbar("Error deleting account: " + e.getMessage());
                });
    }

    private void showSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
    }
} 