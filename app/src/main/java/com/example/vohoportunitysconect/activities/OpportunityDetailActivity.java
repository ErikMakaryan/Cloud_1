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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
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
    private DatabaseReference dbRef;
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
        dbRef = FirebaseDatabase.getInstance().getReference();

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
        dbRef.child("opportunities").child(opportunityId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    opportunity = dataSnapshot.getValue(Opportunity.class);
                    if (opportunity != null) {
                        opportunity.setId(dataSnapshot.getKey());
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
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(OpportunityDetailActivity.this, 
                        "Error loading opportunity details: " + databaseError.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
    }

    private void checkApplicationStatus() {
        String userId = mAuth.getCurrentUser().getUid();
        Query query = dbRef.child("applications")
            .orderByChild("userId")
            .equalTo(userId);
            
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Application application = snapshot.getValue(Application.class);
                    if (application != null && application.getOpportunityId().equals(opportunityId)) {
                        applyButton.setText("Applied");
                        applyButton.setEnabled(false);
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(OpportunityDetailActivity.this, 
                    "Error checking application status: " + databaseError.getMessage(), 
                    Toast.LENGTH_SHORT).show();
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

        Map<String, Object> application = new HashMap<>();
        application.put("id", applicationId);
        application.put("userId", userId);
        application.put("opportunityId", opportunityId);
        application.put("title", opportunity.getTitle());
        application.put("organizationId", opportunity.getOrganizationId());
        application.put("organization", opportunity.getOrganization());
        application.put("organizationImageUrl", "");
        application.put("appliedAt", System.currentTimeMillis());
        application.put("status", "PENDING");
        application.put("coverLetter", message);

        dbRef.child("applications").child(applicationId).setValue(application)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Application submitted successfully", Toast.LENGTH_SHORT).show();
                applyButton.setText("Applied");
                applyButton.setEnabled(false);

                // Update user's application count
                dbRef.child("users").child(userId).child("applications")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            int currentCount = dataSnapshot.exists() ? 
                                dataSnapshot.getValue(Integer.class) : 0;
                            dbRef.child("users").child(userId)
                                .child("applications")
                                .setValue(currentCount + 1);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(OpportunityDetailActivity.this, 
                                "Error updating application count: " + databaseError.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                        }
                    });
            })
            .addOnFailureListener(e -> 
                Toast.makeText(this, "Error submitting application: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show());
    }

    private void loadRatings() {
        Query query = dbRef.child("ratings")
            .orderByChild("targetId")
            .equalTo(opportunityId);
            
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                float totalRating = 0;
                ratingCount = 0;
                
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Rating rating = snapshot.getValue(Rating.class);
                    if (rating != null && rating.getTargetType().equals("opportunity")) {
                        totalRating += rating.getRating();
                        ratingCount++;
                    }
                }
                
                if (ratingCount > 0) {
                    averageRating = totalRating / ratingCount;
                    updateRatingDisplay();
                }

                // Check if user has already rated
                checkUserRating();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(OpportunityDetailActivity.this, 
                    "Error loading ratings: " + databaseError.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateRatingDisplay() {
        ratingText.setText(String.format(Locale.getDefault(), 
            "%.1f (%d ratings)", averageRating, ratingCount));
    }

    private void checkUserRating() {
        String userId = mAuth.getCurrentUser().getUid();
        Query query = dbRef.child("ratings")
            .orderByChild("userId")
            .equalTo(userId);
            
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean hasRated = false;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Rating rating = snapshot.getValue(Rating.class);
                    if (rating != null && 
                        rating.getTargetId().equals(opportunityId) && 
                        rating.getTargetType().equals("opportunity")) {
                        hasRated = true;
                        break;
                    }
                }
                rateButton.setEnabled(!hasRated);
                rateButton.setText(hasRated ? "Already Rated" : "Rate");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(OpportunityDetailActivity.this, 
                    "Error checking user rating: " + databaseError.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showRatingDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_rating, null);
        RatingBar ratingBar = dialogView.findViewById(R.id.rating_bar);
        TextInputEditText commentInput = dialogView.findViewById(R.id.comment_input);

        new MaterialAlertDialogBuilder(this)
            .setTitle("Rate Opportunity")
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

        Map<String, Object> ratingData = new HashMap<>();
        ratingData.put("id", ratingId);
        ratingData.put("userId", userId);
        ratingData.put("targetId", opportunityId);
        ratingData.put("targetType", "opportunity");
        ratingData.put("rating", rating);
        ratingData.put("comment", comment);
        ratingData.put("createdAt", System.currentTimeMillis());

        dbRef.child("ratings").child(ratingId).setValue(ratingData)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Rating submitted successfully", Toast.LENGTH_SHORT).show();
                loadRatings();
            })
            .addOnFailureListener(e -> 
                Toast.makeText(this, "Error submitting rating: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show());
    }
} 