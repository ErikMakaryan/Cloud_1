package com.example.vohoportunitysconect.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.RatingBar;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vohoportunitysconect.R;
import com.example.vohoportunitysconect.models.Application;
import com.example.vohoportunitysconect.models.Opportunity;
import com.example.vohoportunitysconect.models.Rating;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class OpportunityDetailActivity extends AppCompatActivity {
    private MaterialTextView titleText;
    private MaterialTextView organizationText;
    private MaterialTextView descriptionText;
    private Chip locationChip;
    private Chip difficultyChip;
    private Chip deadlineChip;
    private Chip categoryChip;
    private Chip skillsChip;
    private MaterialButton applyButton;
    private MaterialButton rateButton;
    private TextView ratingText;
    
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String opportunityId;
    private Opportunity opportunity;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private float averageRating = 0;
    private int ratingCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opportunity_detail);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Get opportunity ID from intent
        opportunityId = getIntent().getStringExtra("opportunity_id");
        if (opportunityId == null) {
            Toast.makeText(this, "Error: Opportunity not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        titleText = findViewById(R.id.opportunity_title);
        organizationText = findViewById(R.id.organization_name);
        descriptionText = findViewById(R.id.opportunity_description);
        locationChip = findViewById(R.id.location_chip);
        difficultyChip = findViewById(R.id.difficulty_chip);
        deadlineChip = findViewById(R.id.deadline_chip);
        categoryChip = findViewById(R.id.category_chip);
        skillsChip = findViewById(R.id.skills_chip);
        applyButton = findViewById(R.id.apply_button);
        ratingText = findViewById(R.id.rating_text);
        rateButton = findViewById(R.id.rate_button);

        // Load opportunity details
        loadOpportunityDetails();

        // Setup apply button
        applyButton.setOnClickListener(v -> showApplicationDialog());

        // Load ratings
        loadRatings();
    }

    private void loadOpportunityDetails() {
        db.collection("opportunities").document(opportunityId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                opportunity = documentSnapshot.toObject(Opportunity.class);
                if (opportunity != null) {
                    titleText.setText(opportunity.getTitle());
                    organizationText.setText(opportunity.getOrganization());
                    descriptionText.setText(opportunity.getDescription());
                    locationChip.setText(opportunity.getLocation());
                    difficultyChip.setText(opportunity.getDifficulty().toString());
                    deadlineChip.setText(dateFormat.format(opportunity.getDeadline()));
                    categoryChip.setText(opportunity.getCategory());
                    skillsChip.setText(opportunity.getSkills());

                    // Check if user has already applied
                    checkApplicationStatus();
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error loading opportunity details", Toast.LENGTH_SHORT).show();
                finish();
            });
    }

    private void checkApplicationStatus() {
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("applications")
            .whereEqualTo("userId", userId)
            .whereEqualTo("opportunityId", opportunityId)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (!queryDocumentSnapshots.isEmpty()) {
                    applyButton.setText("Applied");
                    applyButton.setEnabled(false);
                }
            });
    }

    private void showApplicationDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_apply, null);
        TextInputLayout messageLayout = dialogView.findViewById(R.id.message_input_layout);
        TextInputEditText messageInput = dialogView.findViewById(R.id.message_input);

        new MaterialAlertDialogBuilder(this)
            .setTitle("Apply for Opportunity")
            .setView(dialogView)
            .setPositiveButton("Apply", (dialog, which) -> {
                String message = messageInput.getText() != null ? messageInput.getText().toString() : "";
                submitApplication(message);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void submitApplication(String message) {
        String userId = mAuth.getCurrentUser().getUid();
        String applicationId = UUID.randomUUID().toString();

        Application application = new Application(
            UUID.randomUUID().toString(),
            mAuth.getCurrentUser().getUid(),
            opportunityId,
            opportunity.getTitle(),
            opportunity.getOrganizationId(),
            opportunity.getOrganization(),
            null, // organizationImageUrl
            new Date(),
            Application.Status.PENDING,
            "" // coverLetter
        );

        db.collection("applications").document(applicationId)
            .set(application)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Application submitted successfully", Toast.LENGTH_SHORT).show();
                applyButton.setText("Applied");
                applyButton.setEnabled(false);

                // Update user's application count
                DocumentReference userRef = db.collection("users").document(userId);
                userRef.update("applications", com.google.firebase.firestore.FieldValue.increment(1));
            })
            .addOnFailureListener(e -> 
                Toast.makeText(this, "Error submitting application", Toast.LENGTH_SHORT).show());
    }

    private void loadRatings() {
        db.collection("ratings")
            .whereEqualTo("targetId", opportunityId)
            .whereEqualTo("targetType", "opportunity")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                float totalRating = 0;
                ratingCount = queryDocumentSnapshots.size();
                
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Rating rating = document.toObject(Rating.class);
                    totalRating += rating.getRating();
                }
                
                if (ratingCount > 0) {
                    averageRating = totalRating / ratingCount;
                    updateRatingDisplay();
                }

                // Check if user has already rated
                checkUserRating();
            })
            .addOnFailureListener(e -> 
                Toast.makeText(this, "Error loading ratings", Toast.LENGTH_SHORT).show());
    }

    private void updateRatingDisplay() {
        if (ratingCount > 0) {
            String ratingText = String.format(Locale.getDefault(), "%.1f ★ (%d reviews)", 
                averageRating, ratingCount);
            this.ratingText.setText(ratingText);
        } else {
            this.ratingText.setText(R.string.no_ratings);
        }
    }

    private void checkUserRating() {
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("ratings")
            .whereEqualTo("targetId", opportunityId)
            .whereEqualTo("targetType", "opportunity")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (!queryDocumentSnapshots.isEmpty()) {
                    rateButton.setText("Update Rating");
                }
            });
    }

    private void showRatingDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_rating, null);
        RatingBar ratingBar = dialogView.findViewById(R.id.rating_bar);
        TextInputEditText commentInput = dialogView.findViewById(R.id.comment_input);

        new MaterialAlertDialogBuilder(this)
            .setTitle("Rate this Opportunity")
            .setView(dialogView)
            .setPositiveButton("Submit", (dialog, which) -> {
                float rating = ratingBar.getRating();
                String comment = commentInput.getText() != null ? 
                    commentInput.getText().toString() : "";
                submitRating(rating, comment);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void submitRating(float rating, String comment) {
        String userId = mAuth.getCurrentUser().getUid();
        String ratingId = UUID.randomUUID().toString();

        Rating ratingObj = new Rating(
            ratingId,
            userId,
            opportunityId,
            "opportunity",
            rating,
            comment,
            new Date()
        );

        db.collection("ratings").document(ratingId)
            .set(ratingObj)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Rating submitted successfully", Toast.LENGTH_SHORT).show();
                loadRatings(); // Reload ratings to update display
            })
            .addOnFailureListener(e -> 
                Toast.makeText(this, "Error submitting rating", Toast.LENGTH_SHORT).show());
    }
} 