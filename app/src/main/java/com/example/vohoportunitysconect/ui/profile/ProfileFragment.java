package com.example.vohoportunitysconect.ui.profile;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.vohoportunitysconect.R;
import com.example.vohoportunitysconect.activities.EditProfileActivity;
import com.example.vohoportunitysconect.activities.LoginActivity;
import com.example.vohoportunitysconect.activities.SettingsActivity;
import com.example.vohoportunitysconect.activities.CreateOpportunityActivity;
import com.example.vohoportunitysconect.databinding.FragmentProfileBinding;
import com.example.vohoportunitysconect.models.Certificate;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;
    private ShapeableImageView profileImage;
    private TextView nameText, emailText, hoursText;
    private MaterialButton editProfileButton, settingsButton, signOutButton, addHoursButton, deleteHoursButton, addCertificateButton;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseRef;
    private StorageReference storageRef;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Intent> certificatePickerLauncher;
    private Uri selectedFileUri;
    private String pendingCertificateName;
    private RecyclerView certificatesRecyclerView;
    private CertificateAdapter certificateAdapter;
    private Uri selectedImageUri;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance("https://vvoohh-e2b0a-default-rtdb.firebaseio.com").getReference();
        storageRef = FirebaseStorage.getInstance().getReference();

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        uploadImage();
                    }
                }
        );

        certificatePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri certificateUri = result.getData().getData();
                    if (certificateUri != null && pendingCertificateName != null) {
                        uploadCertificate(certificateUri, pendingCertificateName);
                        pendingCertificateName = null;
                    }
                }
            }
        );
    }

    @SuppressLint({"CutPasteId", "MissingInflatedId"})
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);

        profileImage = binding.profileImage;
        nameText = binding.nameText;
        emailText = binding.emailText;
        hoursText = binding.hoursText;
        editProfileButton = binding.addHoursButton;
        settingsButton = binding.settingsButton;
        signOutButton = binding.signOutButton;
        addHoursButton = binding.addHoursButton;
        deleteHoursButton = binding.deleteHoursButton;
        addCertificateButton = binding.addCertificateButton;
        certificatesRecyclerView = binding.certificatesRecyclerView;

        // Setup RecyclerView for certificates
        certificatesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        certificateAdapter = new CertificateAdapter(new ArrayList<>(), new CertificateAdapter.OnCertificateClickListener() {
            @Override
            public void onCertificateClick(Certificate certificate) {
                // Open certificate details
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(certificate.getFileUrl()));
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(Certificate certificate) {
                showDeleteCertificateConfirmation(certificate);
            }
        });
        certificatesRecyclerView.setAdapter(certificateAdapter);

        setupClickListeners();
        checkAuthAndLoadData();
        loadCertificates(); // Load certificates when fragment is created

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        databaseRef = FirebaseDatabase.getInstance("https://vvoohh-e2b0a-default-rtdb.firebaseio.com").getReference();
        storageRef = FirebaseStorage.getInstance().getReference();
        
        setupProfilePictureClick();
        loadUserProfile();
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
                // Sign out from Firebase
                mAuth.signOut();
                
                // Clear any stored credentials or preferences
                if (getActivity() != null) {
                    getActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                        .edit()
                        .clear()
                        .apply();
                }
                
                Toast.makeText(getContext(), "Signed out successfully", Toast.LENGTH_SHORT).show();
                
                // Create a new task and clear all previous activities
                Intent intent = new Intent(getContext(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                
                // Finish the current activity
                if (getActivity() != null) {
                    getActivity().finish();
                }
            } else {
                Toast.makeText(getContext(), "You are not signed in", Toast.LENGTH_SHORT).show();
            }
        });

        profileImage.setOnClickListener(v -> showProfilePictureDialog());

        addHoursButton.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() != null) {
                showAddHoursDialog();
            } else {
                Toast.makeText(getContext(), "Please sign in to add hours", Toast.LENGTH_SHORT).show();
            }
        });

        deleteHoursButton.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() != null) {
                showDeleteHoursDialog();
            } else {
                Toast.makeText(getContext(), "Please sign in to delete hours", Toast.LENGTH_SHORT).show();
            }
        });

        addCertificateButton.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() != null) {
                showAddCertificateDialog();
            } else {
                Toast.makeText(getContext(), "Please sign in to add certificates", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupProfilePictureClick() {
        binding.profileImage.setOnClickListener(v -> showProfilePictureDialog());
    }

    private void showProfilePictureDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_profile_picture, null);
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create();

        dialogView.findViewById(R.id.change_photo).setOnClickListener(v -> {
            dialog.dismiss();
            openGallery();
        });

        dialogView.findViewById(R.id.remove_photo).setOnClickListener(v -> {
            dialog.dismiss();
            removeProfilePicture();
        });

        dialog.show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void uploadImage() {
        if (selectedImageUri == null) return;

        String userId = mAuth.getCurrentUser().getUid();
        binding.progressBar.setVisibility(View.VISIBLE);

        try {
            // Create profile_images directory if it doesn't exist
            File profileImagesDir = new File(requireContext().getFilesDir(), "profile_images");
            if (!profileImagesDir.exists()) {
                profileImagesDir.mkdirs();
            }

            // Create a unique filename for the profile image
            String fileName = "profile_" + userId + ".jpg";
            File profileImageFile = new File(profileImagesDir, fileName);

            // Copy the image file
            InputStream inputStream = requireContext().getContentResolver().openInputStream(selectedImageUri);
            if (inputStream != null) {
                FileOutputStream outputStream = new FileOutputStream(profileImageFile);
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                inputStream.close();
                outputStream.close();

                // Update profile image path in database
                String imagePath = profileImageFile.getAbsolutePath();
                databaseRef.child("users").child(userId).child("profileImagePath")
                    .setValue(imagePath)
                    .addOnSuccessListener(aVoid -> {
                        binding.progressBar.setVisibility(View.GONE);
                        loadProfileImage(imagePath);
                        Toast.makeText(getContext(), "Profile picture updated successfully", 
                            Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Failed to update profile picture: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    });
            } else {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error reading image file", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            binding.progressBar.setVisibility(View.GONE);
            Toast.makeText(getContext(), "Error saving image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void removeProfilePicture() {
        String userId = mAuth.getCurrentUser().getUid();
        binding.progressBar.setVisibility(View.VISIBLE);

        // Get the current profile image path
        databaseRef.child("users").child(userId).child("profileImagePath")
            .get()
            .addOnSuccessListener(snapshot -> {
                String imagePath = snapshot.getValue(String.class);
                if (imagePath != null) {
                    // Delete the local file
                    File imageFile = new File(imagePath);
                    if (imageFile.exists()) {
                        imageFile.delete();
                    }
                }

                // Remove from database
                databaseRef.child("users").child(userId).child("profileImagePath")
                    .removeValue()
                    .addOnSuccessListener(aVoid -> {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.profileImage.setImageResource(R.drawable.placeholder_profile);
                        Toast.makeText(getContext(), "Profile picture removed successfully", 
                            Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Failed to remove profile picture: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    });
            })
            .addOnFailureListener(e -> {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error removing profile picture: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }

    private void loadProfileImage(String imagePath) {
        if (imagePath != null && !imagePath.isEmpty()) {
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                Glide.with(this)
                    .load(imageFile)
                    .placeholder(R.drawable.placeholder_profile)
                    .error(R.drawable.placeholder_profile)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .circleCrop()
                    .into(binding.profileImage);
            } else {
                binding.profileImage.setImageResource(R.drawable.placeholder_profile);
            }
        } else {
            binding.profileImage.setImageResource(R.drawable.placeholder_profile);
        }
    }

    private void loadUserProfile() {
        String userId = mAuth.getCurrentUser().getUid();
        binding.progressBar.setVisibility(View.VISIBLE);

        databaseRef.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                binding.progressBar.setVisibility(View.GONE);
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String profileImagePath = snapshot.child("profileImagePath").getValue(String.class);

                    binding.nameText.setText(name);
                    binding.emailText.setText(email);
                    loadProfileImage(profileImagePath);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error loading profile: " + error.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
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
                        
                        // Check if user is an organization
                        Boolean isOrganization = dataSnapshot.child("isOrganization").getValue(Boolean.class);
                        if (isOrganization != null && isOrganization) {
                            addHoursButton.setVisibility(View.GONE);
                            deleteHoursButton.setVisibility(View.GONE);
                        } else {
                            addHoursButton.setVisibility(View.VISIBLE);
                            deleteHoursButton.setVisibility(View.VISIBLE);
                        }
                        
                        // Load profile image
                        String profileImagePath = dataSnapshot.child("profileImagePath").getValue(String.class);
                        loadProfileImage(profileImagePath);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Error loading profile data", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @SuppressLint("SetTextI18n")
    private void uploadCertificate(Uri certificateUri, String name) {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            
            // Show loading dialog
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_loading, null);
            TextView messageText = dialogView.findViewById(R.id.message_text);
            messageText.setText("Uploading certificate...");
            builder.setView(dialogView);
            builder.setCancelable(false);
            androidx.appcompat.app.AlertDialog loadingDialog = builder.create();
            loadingDialog.show();

            try {
                // Create certificates directory if it doesn't exist
                File certificatesDir = new File(requireContext().getFilesDir(), "certificates");
                if (!certificatesDir.exists()) {
                    certificatesDir.mkdirs();
                }

                // Create a unique filename for the certificate
                String fileName = "cert_" + userId + "_" + System.currentTimeMillis() + ".pdf";
                File certificateFile = new File(certificatesDir, fileName);

                // Copy the certificate file
                InputStream inputStream = requireContext().getContentResolver().openInputStream(certificateUri);
                if (inputStream != null) {
                    FileOutputStream outputStream = new FileOutputStream(certificateFile);
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    inputStream.close();
                    outputStream.close();

                    // Save certificate metadata
                    Certificate certificate = new Certificate();
                    certificate.setName(name);
                    certificate.setFilePath(certificateFile.getAbsolutePath());
                    certificate.setFileUrl(certificateUri.toString());
                    certificate.setUploadDate(System.currentTimeMillis());

                    // Add to database
                    String certificateId = databaseRef.child("users").child(userId).child("certificates").push().getKey();
                    if (certificateId != null) {
                        databaseRef.child("users").child(userId).child("certificates").child(certificateId)
                            .setValue(certificate)
                            .addOnSuccessListener(aVoid -> {
                                loadingDialog.dismiss();
                                Toast.makeText(getContext(), "Certificate uploaded successfully", Toast.LENGTH_SHORT).show();
                                loadCertificates(); // Refresh certificates list
                            })
                            .addOnFailureListener(e -> {
                                loadingDialog.dismiss();
                                Toast.makeText(getContext(), "Error uploading certificate: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                    }
                } else {
                    loadingDialog.dismiss();
                    Toast.makeText(getContext(), "Error reading certificate file", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                loadingDialog.dismiss();
                Toast.makeText(getContext(), "Error saving certificate: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadCertificates() {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            databaseRef.child("users").child(userId).child("certificates")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Certificate> certificates = new ArrayList<>();
                        for (DataSnapshot certSnapshot : snapshot.getChildren()) {
                            Certificate certificate = certSnapshot.getValue(Certificate.class);
                            if (certificate != null) {
                                certificate.setId(certSnapshot.getKey());
                                certificates.add(certificate);
                            }
                        }
                        updateCertificatesList(certificates);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Error loading certificates: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        }
    }

    private void updateCertificatesList(List<Certificate> certificates) {
        if (certificateAdapter != null) {
            certificateAdapter.updateCertificates(certificates);
            certificatesRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showAddCertificateDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_certificate_name, null);
        TextInputEditText nameInput = dialogView.findViewById(R.id.certificate_name_input);

        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add Certificate")
            .setView(dialogView)
            .setPositiveButton("Upload", (dialog, which) -> {
                String name = nameInput.getText().toString().trim();
                if (!name.isEmpty()) {
                    pendingCertificateName = name;
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("application/pdf");
                    certificatePickerLauncher.launch(intent);
                } else {
                    Toast.makeText(getContext(), "Please enter a certificate name", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showAddHoursDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_hours, null);
        EditText hoursInput = dialogView.findViewById(R.id.hours_input);

        builder.setView(dialogView)
            .setTitle("Add Hours")
            .setPositiveButton("Add", (dialog, which) -> {
                String hoursStr = hoursInput.getText().toString();
                if (!hoursStr.isEmpty()) {
                    try {
                        double hours = Double.parseDouble(hoursStr);
                        if (hours > 0) {
                            addHours(hours);
                        } else {
                            Toast.makeText(getContext(), "Please enter a positive number", Toast.LENGTH_SHORT).show();
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(), "Please enter a valid number", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Please enter hours", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null);

        builder.create().show();
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
                                
                                // Check if trying to delete more hours than available
                                if (hours > currentHours) {
                                    Toast.makeText(getContext(), 
                                        "Cannot delete more hours than you have (" + currentHours + " hours available)", 
                                        Toast.LENGTH_LONG).show();
                                    return;
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

    private void addHours(double hours) {
        if (mAuth.getCurrentUser() != null) {
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
        }
    }

    private void showDeleteCertificateConfirmation(Certificate certificate) {
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Certificate")
            .setMessage("Are you sure you want to delete this certificate?")
            .setPositiveButton("Delete", (dialog, which) -> deleteCertificate(certificate))
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void deleteCertificate(Certificate certificate) {
        String userId = mAuth.getCurrentUser().getUid();
        databaseRef.child("users").child(userId).child("certificates").child(certificate.getId())
            .removeValue()
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(getContext(), "Certificate deleted successfully", Toast.LENGTH_SHORT).show();
                loadCertificates(); // Refresh the certificates list
            })
            .addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Failed to delete certificate: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 