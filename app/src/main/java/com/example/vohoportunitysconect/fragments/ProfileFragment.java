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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
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
    private FirebaseFirestore db;
    private StorageReference storageRef;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
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
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            nameText.setText(documentSnapshot.getString("name"));
                            emailText.setText(documentSnapshot.getString("email"));
                            hoursText.setText(String.valueOf(documentSnapshot.getLong("volunteer_hours") != null ? documentSnapshot.getLong("volunteer_hours") : 0));
                            opportunitiesText.setText(String.valueOf(documentSnapshot.getLong("completed_projects") != null ? documentSnapshot.getLong("completed_projects") : 0));
                            applicationsCount.setText(String.valueOf(documentSnapshot.getLong("applications") != null ? documentSnapshot.getLong("applications") : 0));

                            String imageUrl = documentSnapshot.getString("profile_image");
                            if (imageUrl != null) {
                                Glide.with(this).load(imageUrl).into(profileImage);
                            }
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Error loading user data", Toast.LENGTH_SHORT).show());
        }
    }

    private void loadRecentActivity() {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            db.collection("activities")
                    .whereEqualTo("userId", userId)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(5)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        List<Activity> activities = new ArrayList<>();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            activities.add(document.toObject(Activity.class));
                        }
                        activityAdapter.setActivities(activities);
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Error loading activities", Toast.LENGTH_SHORT).show());
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
        db.collection("users").document(userId)
                .update("profile_image", imageUrl)
                .addOnSuccessListener(aVoid -> {
                    Glide.with(this).load(imageUrl).into(profileImage);
                    Toast.makeText(getContext(), "Profile picture updated!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error updating image", Toast.LENGTH_SHORT).show());
    }

    private void addVolunteerHours(int hours) {
        String userId = mAuth.getCurrentUser().getUid();
        DocumentReference userRef = db.collection("users").document(userId);

        userRef.update("volunteer_hours", com.google.firebase.firestore.FieldValue.increment(hours))
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Hours updated!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update hours!", Toast.LENGTH_SHORT).show());
    }
}
