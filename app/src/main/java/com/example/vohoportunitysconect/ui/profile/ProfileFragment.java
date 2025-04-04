package com.example.vohoportunitysconect.ui.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.vohoportunitysconect.R;
import com.example.vohoportunitysconect.activities.EditProfileActivity;
import com.example.vohoportunitysconect.activities.LoginActivity;
import com.example.vohoportunitysconect.activities.SettingsActivity;
import com.example.vohoportunitysconect.activities.CreateOpportunityActivity;
import com.example.vohoportunitysconect.adapters.ActivityAdapter;
import com.example.vohoportunitysconect.models.Activity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProfileFragment extends Fragment {
    private ShapeableImageView profileImage;
    private TextView nameText, emailText, hoursText, volunteerHoursText, opportunitiesText, applicationsCount;
    private RecyclerView activityRecyclerView;
    private MaterialButton editProfileButton, settingsButton, signOutButton, addHoursButton, createOpportunityButton;
    private ActivityAdapter activityAdapter;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseRef;
    private StorageReference storageRef;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference();
        storageRef = FirebaseStorage.getInstance().getReference("profile_images");

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        uploadProfileImage(imageUri);
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        profileImage = root.findViewById(R.id.profile_image);
        nameText = root.findViewById(R.id.name_text);
        emailText = root.findViewById(R.id.email_text);
        hoursText = root.findViewById(R.id.hours_text);
        volunteerHoursText = root.findViewById(R.id.volunteer_hours_text);
        opportunitiesText = root.findViewById(R.id.opportunities_text);
        applicationsCount = root.findViewById(R.id.applications_count);
        activityRecyclerView = root.findViewById(R.id.activity_recycler_view);
        editProfileButton = root.findViewById(R.id.edit_profile_button);
        settingsButton = root.findViewById(R.id.settings_button);
        signOutButton = root.findViewById(R.id.sign_out_button);
        addHoursButton = root.findViewById(R.id.add_hours_button);
        createOpportunityButton = root.findViewById(R.id.create_opportunity_button);

        activityRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        activityAdapter = new ActivityAdapter();
        activityRecyclerView.setAdapter(activityAdapter);

        setupClickListeners();
        checkAuthAndLoadData();

        return root;
    }

    private void checkAuthAndLoadData() {
        if (mAuth.getCurrentUser() != null) {
            loadUserData();
            loadRecentActivity();
        } else {
            // User is not authenticated, redirect to login
            startActivity(new Intent(getContext(), LoginActivity.class));
            if (getActivity() != null) {
                getActivity().finish();
            }
        }
    }

    private void setupClickListeners() {
        editProfileButton.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() != null) {
                startActivity(new Intent(getContext(), EditProfileActivity.class));
            } else {
                Toast.makeText(getContext(), "Please sign in to edit profile", Toast.LENGTH_SHORT).show();
            }
        });

        settingsButton.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() != null) {
                startActivity(new Intent(getContext(), SettingsActivity.class));
            } else {
                Toast.makeText(getContext(), "Please sign in to access settings", Toast.LENGTH_SHORT).show();
            }
        });

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

        profileImage.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() != null) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                imagePickerLauncher.launch(intent);
            } else {
                Toast.makeText(getContext(), "Please sign in to change profile picture", Toast.LENGTH_SHORT).show();
            }
        });

        createOpportunityButton.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() != null) {
                startActivity(new Intent(getContext(), CreateOpportunityActivity.class));
            } else {
                Toast.makeText(getContext(), "Please sign in to create an opportunity", Toast.LENGTH_SHORT).show();
            }
        });

        // Remove the add hours button click listener since only organizations can add hours
        addHoursButton.setVisibility(View.GONE);
    }

    private void loadUserData() {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            databaseRef.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Load basic user info
                        nameText.setText(dataSnapshot.child("name").getValue(String.class));
                        emailText.setText(dataSnapshot.child("email").getValue(String.class));
                        
                        // Load statistics
                        Long hours = dataSnapshot.child("volunteerHours").getValue(Long.class);
                        hoursText.setText(hours != null ? String.valueOf(hours) : "0");
                        
                        Long opportunities = dataSnapshot.child("completedOpportunitiesCount").getValue(Long.class);
                        opportunitiesText.setText(opportunities != null ? String.valueOf(opportunities) : "0");
                        
                        // Check if user is an organization
                        Boolean isOrganization = dataSnapshot.child("isOrganization").getValue(Boolean.class);
                        if (isOrganization != null && isOrganization) {
                            createOpportunityButton.setVisibility(View.VISIBLE);
                            addHoursButton.setVisibility(View.GONE);
                        } else {
                            createOpportunityButton.setVisibility(View.GONE);
                            addHoursButton.setVisibility(View.VISIBLE);
                        }
                        
                        // Load profile image
                        String imageUrl = dataSnapshot.child("profileImageUrl").getValue(String.class);
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(ProfileFragment.this)
                                .load(imageUrl)
                                .placeholder(R.drawable.placeholder_profile)
                                .into(profileImage);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getContext(), "Error loading user data", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadRecentActivity() {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            Query activitiesQuery = databaseRef.child("activities")
                    .orderByChild("userId")
                    .equalTo(userId)
                    .limitToLast(5);

            activitiesQuery.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    List<Activity> activities = new ArrayList<>();
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            try {
                                Activity activity = snapshot.getValue(Activity.class);
                                if (activity != null) {
                                    activity.setId(snapshot.getKey());
                                    // Ensure timestamp is set
                                    if (activity.getTimestamp() == null) {
                                        activity.setTimestamp(new Date());
                                    }
                                    activities.add(activity);
                                }
                            } catch (Exception e) {
                                Log.e("ProfileFragment", "Error parsing activity: " + e.getMessage());
                            }
                        }
                    }
                    activityAdapter.setActivities(activities);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("ProfileFragment", "Error loading activities: " + databaseError.getMessage());
                    Toast.makeText(getContext(), "Error loading activities: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getContext(), "Please sign in to view activities", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadProfileImage(Uri imageUri) {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            StorageReference userImageRef = storageRef.child(userId + ".jpg");

            userImageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> userImageRef.getDownloadUrl().addOnSuccessListener(uri -> updateUserProfileImage(uri.toString())))
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void updateUserProfileImage(String imageUrl) {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            databaseRef.child("users").child(userId)
                    .child("profileImageUrl")
                    .setValue(imageUrl)
                    .addOnSuccessListener(aVoid -> {
                        Glide.with(this).load(imageUrl).into(profileImage);
                        Toast.makeText(getContext(), "Profile picture updated!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Error updating image: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
} 