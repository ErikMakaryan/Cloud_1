package com.example.vohoportunitysconect.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import java.util.List;

public class ProfileFragment extends Fragment {
    private ShapeableImageView profileImage;
    private TextView nameText, emailText, hoursText, volunteerHoursText, opportunitiesText, applicationsCount;
    private RecyclerView activityRecyclerView;
    private MaterialButton editProfileButton, settingsButton, signOutButton, addHoursButton;
    private ActivityAdapter activityAdapter;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseRef;
    private StorageReference storageRef;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance("https://vvoohh-e2b0a-default-rtdb.firebaseio.com").getReference();
        storageRef = FirebaseStorage.getInstance().getReference("profile_images");

        // Handle image picking result
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

        activityRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        activityAdapter = new ActivityAdapter();
        activityRecyclerView.setAdapter(activityAdapter);

        setupClickListeners();
        loadUserData();
        loadRecentActivity();

        return root;
    }

    private void setupClickListeners() {
        editProfileButton.setOnClickListener(v -> startActivity(new Intent(getContext(), EditProfileActivity.class)));

        settingsButton.setOnClickListener(v -> Toast.makeText(getContext(), "Settings coming soon!", Toast.LENGTH_SHORT).show());

        signOutButton.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(getContext(), LoginActivity.class));
            getActivity().finish();
        });

        profileImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        addHoursButton.setOnClickListener(v -> addVolunteerHours(1)); // Add 1 hour per click
    }

    private void loadUserData() {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            DatabaseReference userRef = databaseRef.child("users").child(userId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        nameText.setText(dataSnapshot.child("name").getValue(String.class));
                        emailText.setText(dataSnapshot.child("email").getValue(String.class));
                        hoursText.setText(String.valueOf(dataSnapshot.child("volunteer_hours").getValue(Long.class) != null ? dataSnapshot.child("volunteer_hours").getValue(Long.class) : 0));
                        opportunitiesText.setText(String.valueOf(dataSnapshot.child("completed_projects").getValue(Long.class) != null ? dataSnapshot.child("completed_projects").getValue(Long.class) : 0));
                        applicationsCount.setText(String.valueOf(dataSnapshot.child("applications").getValue(Long.class) != null ? dataSnapshot.child("applications").getValue(Long.class) : 0));

                        String imageUrl = dataSnapshot.child("profile_image").getValue(String.class);
                        if (imageUrl != null) {
                            Glide.with(ProfileFragment.this).load(imageUrl).into(profileImage);
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
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Activity activity = snapshot.getValue(Activity.class);
                        if (activity != null) {
                            activity.setId(snapshot.getKey());
                            activities.add(activity);
                        }
                    }
                    activityAdapter.setActivities(activities);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getContext(), "Error loading activities", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void uploadProfileImage(Uri imageUri) {
        String userId = mAuth.getCurrentUser().getUid();
        StorageReference userImageRef = storageRef.child(userId + ".jpg");

        userImageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> userImageRef.getDownloadUrl().addOnSuccessListener(uri -> updateUserProfileImage(uri.toString())))
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Upload failed!", Toast.LENGTH_SHORT).show());
    }

    private void updateUserProfileImage(String imageUrl) {
        String userId = mAuth.getCurrentUser().getUid();
        databaseRef.child("users").child(userId)
                .child("profile_image")
                .setValue(imageUrl)
                .addOnSuccessListener(aVoid -> {
                    Glide.with(this).load(imageUrl).into(profileImage);
                    Toast.makeText(getContext(), "Profile picture updated!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error updating image", Toast.LENGTH_SHORT).show());
    }

    private void addVolunteerHours(int hours) {
        String userId = mAuth.getCurrentUser().getUid();
        databaseRef.child("users").child(userId)
                .child("volunteer_hours")
                .setValue(ServerValue.increment(hours))
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Hours updated!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update hours!", Toast.LENGTH_SHORT).show());
    }
}
