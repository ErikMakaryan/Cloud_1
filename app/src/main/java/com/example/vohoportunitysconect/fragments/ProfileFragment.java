package com.example.vohoportunitysconect.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.vohoportunitysconect.R;
import com.example.vohoportunitysconect.activities.LoginActivity;
import com.example.vohoportunitysconect.activities.SettingsActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {
    private View rootView;
    private TextView nameText, emailText, hoursText;
    private MaterialButton settingsButton, signOutButton, addHoursButton, deleteHoursButton;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseRef;
    private ValueEventListener connectionStateListener;
    private boolean isViewInitialized = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance("https://vvoohh-e2b0a-default-rtdb.firebaseio.com").getReference();

        // Setup database connection state listener
        setupDatabaseConnectionListener();
    }

    private void setupDatabaseConnectionListener() {
        DatabaseReference connectedRef = FirebaseDatabase.getInstance("https://vvoohh-e2b0a-default-rtdb.firebaseio.com").getReference(".info/connected");
        connectionStateListener = connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    Log.d("ProfileFragment", "Database connected");
                    // Reload data when connection is restored
                    if (isAdded()) {
                        loadUserData();
                    }
                } else {
                    Log.d("ProfileFragment", "Database disconnected");
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Database connection lost. Attempting to reconnect...", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ProfileFragment", "Database connection listener cancelled: " + error.getMessage());
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Remove the connection state listener
        if (connectionStateListener != null) {
            DatabaseReference connectedRef = FirebaseDatabase.getInstance("https://vvoohh-e2b0a-default-rtdb.firebaseio.com").getReference(".info/connected");
            connectedRef.removeEventListener(connectionStateListener);
        }
    }

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        isViewInitialized = false;

        try {
            initializeViews();
            setupClickListeners();
            isViewInitialized = true;
            checkAuthAndLoadData();
        } catch (Exception e) {
            Log.e("ProfileFragment", "Error initializing views: " + e.getMessage());
            Toast.makeText(requireContext(), "Error loading profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        return rootView;
    }

    private void initializeViews() {
        if (rootView == null) {
            throw new IllegalStateException("Root view is null");
        }

        try {
            nameText = rootView.findViewById(R.id.name_text);
            emailText = rootView.findViewById(R.id.email_text);
            hoursText = rootView.findViewById(R.id.hours_text);
            settingsButton = rootView.findViewById(R.id.settings_button);
            signOutButton = rootView.findViewById(R.id.sign_out_button);
            addHoursButton = rootView.findViewById(R.id.add_hours_button);
            deleteHoursButton = rootView.findViewById(R.id.delete_hours_button);
            ImageView profileImage = rootView.findViewById(R.id.profile_image);

            // Verify all required views are initialized
            if (nameText == null || emailText == null || 
                hoursText == null || settingsButton == null || 
                signOutButton == null || addHoursButton == null || deleteHoursButton == null || profileImage == null) {
                throw new IllegalStateException("Failed to initialize all required views");
            }

            // Set initial values
            nameText.setText("");
            emailText.setText("");
            hoursText.setText("0");
            profileImage.setImageResource(R.drawable.placeholder_profile);
        } catch (Exception e) {
            Log.e("ProfileFragment", "Error in initializeViews: " + e.getMessage());
            throw e;
        }
    }

    private void setupClickListeners() {
        if (rootView == null) {
            Log.e("ProfileFragment", "Root view is null in setupClickListeners");
            return;
        }

        MaterialButton settingsButton = rootView.findViewById(R.id.settings_button);
        if (settingsButton != null) {
            settingsButton.setOnClickListener(v -> {
                if (mAuth.getCurrentUser() != null) {
                    startActivity(new Intent(getContext(), SettingsActivity.class));
                } else {
                    Toast.makeText(getContext(), "Please sign in to access settings", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.e("ProfileFragment", "Settings button not found");
        }

        MaterialButton signOutButton = rootView.findViewById(R.id.sign_out_button);
        if (signOutButton != null) {
            signOutButton.setOnClickListener(v -> {
                if (mAuth.getCurrentUser() != null) {
                    mAuth.signOut();
                    Toast.makeText(getContext(), "Signed out successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getContext(), LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    if (getActivity() != null) {
                        getActivity().finish();
                    }
                } else {
                    Toast.makeText(getContext(), "You are not signed in", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.e("ProfileFragment", "Sign out button not found");
        }

        MaterialButton addHoursButton = rootView.findViewById(R.id.add_hours_button);
        if (addHoursButton != null) {
            addHoursButton.setOnClickListener(v -> {
                if (mAuth.getCurrentUser() != null) {
                    showAddHoursDialog();
                } else {
                    Toast.makeText(getContext(), "Please sign in to add hours", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.e("ProfileFragment", "Add hours button not found");
        }

        MaterialButton deleteHoursButton = rootView.findViewById(R.id.delete_hours_button);
        if (deleteHoursButton != null) {
            deleteHoursButton.setOnClickListener(v -> {
                if (mAuth.getCurrentUser() != null) {
                    showDeleteHoursDialog();
                } else {
                    Toast.makeText(getContext(), "Please sign in to delete hours", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.e("ProfileFragment", "Delete hours button not found");
        }
    }

    private void checkAuthAndLoadData() {
        if (mAuth.getCurrentUser() != null) {
            loadUserData();
        } else {
            // User is not authenticated, redirect to login
            startActivity(new Intent(getContext(), LoginActivity.class));
            if (getActivity() != null) {
                getActivity().finish();
            }
        }
    }

    private void loadUserData() {
        if (mAuth.getCurrentUser() == null || !isAdded() || !isViewInitialized || rootView == null) {
            Log.d("ProfileFragment", "Skipping user data load - fragment not ready");
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        
        // Enable offline persistence
        FirebaseDatabase.getInstance("https://vvoohh-e2b0a-default-rtdb.firebaseio.com").setPersistenceEnabled(true);
        
        databaseRef.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!isAdded() || !isViewInitialized || rootView == null) {
                    Log.d("ProfileFragment", "Skipping user data update - fragment not ready");
                    return;
                }
                
                try {
                    if (dataSnapshot.exists()) {
                        // Update name
                        if (nameText != null) {
                            String name = dataSnapshot.child("name").getValue(String.class);
                            nameText.setText(name != null ? name : "");
                        }

                        // Update email
                        if (emailText != null) {
                            String email = dataSnapshot.child("email").getValue(String.class);
                            emailText.setText(email != null ? email : "");
                        }
                        
                        // Update hours
                        if (hoursText != null) {
                            Long hours = dataSnapshot.child("volunteerHours").getValue(Long.class);
                            hoursText.setText(hours != null ? String.valueOf(hours) : "0");
                        }

                        // Update profile image
                        ImageView profileImage = rootView.findViewById(R.id.profile_image);
                        String profileImageUrl = dataSnapshot.child("profileImageUrl").getValue(String.class);
                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            Glide.with(requireContext())
                                .load(profileImageUrl)
                                .placeholder(R.drawable.placeholder_profile)
                                .error(R.drawable.placeholder_profile)
                                .into(profileImage);
                        } else {
                            profileImage.setImageResource(R.drawable.placeholder_profile);
                        }
                    }
                } catch (Exception e) {
                    Log.e("ProfileFragment", "Error loading user data: " + e.getMessage());
                    if (isAdded()) {
                        Toast.makeText(requireContext(), "Error loading profile data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    reconnectToDatabase();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ProfileFragment", "Error loading user data: " + error.getMessage());
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Error loading profile data: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
                reconnectToDatabase();
            }
        });
    }

    private void showAddHoursDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_hours, null);
        EditText hoursInput = dialogView.findViewById(R.id.hours_input);

        builder.setView(dialogView)
            .setTitle("Add Volunteer Hours")
            .setPositiveButton("Add", (dialog, which) -> {
                String hoursStr = hoursInput.getText().toString();
                
                if (!hoursStr.isEmpty()) {
                    try {
                        double hours = Double.parseDouble(hoursStr);
                        String userId = mAuth.getCurrentUser().getUid();
                        
                        // Update volunteer hours in Firebase
                        databaseRef.child("users").child(userId).child("volunteerHours")
                            .get().addOnSuccessListener(snapshot -> {
                                double currentHours = 0;
                                if (snapshot.getValue() != null) {
                                    currentHours = ((Number) snapshot.getValue()).doubleValue();
                                }
                                
                                Map<String, Object> updates = new HashMap<>();
                                updates.put("volunteerHours", currentHours + hours);
                                
                                databaseRef.child("users").child(userId)
                                    .updateChildren(updates)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(getContext(), "Hours added successfully", Toast.LENGTH_SHORT).show();
                                        loadUserData(); // Refresh the profile data
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(getContext(), "Error adding hours", Toast.LENGTH_SHORT).show();
                                    });
                            });
                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(), "Please enter a valid number", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Please enter hours", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showDeleteHoursDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_hours, null);
        EditText hoursInput = dialogView.findViewById(R.id.hours_input);

        builder.setView(dialogView)
            .setTitle("Delete Volunteer Hours")
            .setPositiveButton("Delete", (dialog, which) -> {
                String hoursStr = hoursInput.getText().toString();
                
                if (!hoursStr.isEmpty()) {
                    try {
                        double hours = Double.parseDouble(hoursStr);
                        String userId = mAuth.getCurrentUser().getUid();
                        
                        // Update volunteer hours in Firebase
                        databaseRef.child("users").child(userId).child("volunteerHours")
                            .get().addOnSuccessListener(snapshot -> {
                                double currentHours = 0;
                                if (snapshot.getValue() != null) {
                                    currentHours = ((Number) snapshot.getValue()).doubleValue();
                                }
                                
                                // Ensure we don't go below 0 hours
                                double newHours = Math.max(0, currentHours - hours);
                                
                                Map<String, Object> updates = new HashMap<>();
                                updates.put("volunteerHours", newHours);
                                
                                databaseRef.child("users").child(userId)
                                    .updateChildren(updates)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(getContext(), "Hours deleted successfully", Toast.LENGTH_SHORT).show();
                                        loadUserData(); // Refresh the profile data
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(getContext(), "Error deleting hours", Toast.LENGTH_SHORT).show();
                                    });
                            });
                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(), "Please enter a valid number", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Please enter hours", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void reconnectToDatabase() {
        if (isAdded()) {
            Toast.makeText(requireContext(), "Attempting to reconnect to database...", Toast.LENGTH_SHORT).show();
            // Reinitialize database reference
            databaseRef = FirebaseDatabase.getInstance("https://vvoohh-e2b0a-default-rtdb.firebaseio.com").getReference();
            // Retry loading data after a short delay using the main looper
            new Handler(Looper.getMainLooper()).postDelayed(this::loadUserData, 2000);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isViewInitialized = false;
        rootView = null;
        // Clear references to views to prevent memory leaks
        nameText = null;
        emailText = null;
        hoursText = null;
        settingsButton = null;
        signOutButton = null;
        addHoursButton = null;
        deleteHoursButton = null;
    }
}
