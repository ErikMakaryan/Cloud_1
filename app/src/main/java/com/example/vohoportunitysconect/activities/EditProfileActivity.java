package com.example.vohoportunitysconect.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.activity.OnBackPressedCallback;

import com.bumptech.glide.Glide;
import com.example.vohoportunitysconect.R;
import com.example.vohoportunitysconect.database.DatabaseManager;
import com.example.vohoportunitysconect.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class EditProfileActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String TAG = "EditProfileActivity";

    private ImageView profileImage;
    private EditText nameInput;
    private EditText phoneInput;
    private EditText bioInput;
    private Button saveButton;
    private Uri selectedImageUri;
    private User currentUser;
    private DatabaseManager databaseManager;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private boolean hasUnsavedChanges = false;
    private OnBackPressedCallback backCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize Firebase
        databaseManager = DatabaseManager.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        // Initialize views
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        profileImage = findViewById(R.id.profile_image);
        nameInput = findViewById(R.id.name_input);
        phoneInput = findViewById(R.id.phone_input);
        bioInput = findViewById(R.id.bio_input);
        saveButton = findViewById(R.id.save_button);

        // Set up click listeners
        profileImage.setOnClickListener(v -> selectImage());
        saveButton.setOnClickListener(v -> saveProfile());

        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    handleImageSelection(result.getData());
                }
            }
        );

        // Load current user data
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        loadUserData(userId);

        backCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (hasUnsavedChanges) {
                    showUnsavedChangesDialog();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, backCallback);
    }

    private void loadUserData(String userId) {
        databaseManager.getUser(userId, new DatabaseManager.DatabaseCallback<User>() {
            @Override
            public void onSuccess(User user) {
                currentUser = user;
                if (user != null) {
                    nameInput.setText(user.getName());
                    phoneInput.setText(user.getPhoneNumber());
                    bioInput.setText(user.getBio());

                    if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                        Glide.with(EditProfileActivity.this)
                            .load(user.getProfileImageUrl())
                            .placeholder(R.drawable.default_profile_image)
                            .into(profileImage);
                    }
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(EditProfileActivity.this, "Error loading user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        imagePickerLauncher.launch(intent);
    }

    private void handleImageSelection(Intent data) {
        if (data.getData() != null) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                Glide.with(this)
                    .load(selectedImageUri)
                    .placeholder(R.drawable.default_profile_image)
                    .into(profileImage);
            }
        }
    }

    private void saveProfile() {
        String name = nameInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String bio = bioInput.getText().toString().trim();

        if (name.isEmpty()) {
            nameInput.setError("Name is required");
            return;
        }

        // Update user object
        currentUser.setName(name);
        currentUser.setPhoneNumber(phone);
        currentUser.setBio(bio);

        // If there's a new image, upload it first
        if (selectedImageUri != null) {
            uploadImageAndSaveProfile();
        } else {
            saveUserToDatabase();
        }
    }

    private void uploadImageAndSaveProfile() {
        String imageFileName = "profile_images/" + UUID.randomUUID().toString();
        StorageReference imageRef = storageReference.child(imageFileName);

        imageRef.putFile(selectedImageUri)
            .addOnSuccessListener(taskSnapshot -> {
                // Get the download URL
                imageRef.getDownloadUrl()
                    .addOnSuccessListener(uri -> {
                        currentUser.setProfileImageUrl(uri.toString());
                        saveUserToDatabase();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(EditProfileActivity.this,
                            "Error getting image URL: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    });
            })
            .addOnFailureListener(e -> {
                Toast.makeText(EditProfileActivity.this,
                    "Error uploading image: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            });
    }

    private void saveUserToDatabase() {
        databaseManager.saveUser(currentUser, new DatabaseManager.DatabaseCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(EditProfileActivity.this,
                    "Profile updated successfully",
                    Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(EditProfileActivity.this,
                    "Error saving profile: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showUnsavedChangesDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Unsaved Changes")
            .setMessage("You have unsaved changes. Do you want to discard them?")
            .setPositiveButton("Discard", (dialog, which) -> {
                if (backCallback != null) {
                    backCallback.setEnabled(false);
                }
                getOnBackPressedDispatcher().onBackPressed();
            })
            .setNegativeButton("Keep Editing", null)
            .show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 