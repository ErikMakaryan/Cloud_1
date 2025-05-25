package com.example.vohoportunitysconect.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.example.vohoportunitysconect.models.UserActivity;
import com.example.vohoportunitysconect.models.Opportunity;
import com.example.vohoportunitysconect.models.Organization;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class OpportunityDetailsActivity extends AppCompatActivity {
    private ImageView opportunityImage;
    private TextView titleText;
    private TextView organizationText;
    private TextView locationText;
    private TextView descriptionText;
    private TextView hoursText;
    private TextView dateText;
    private Chip remoteChip;
    private Button applyButton;

    private FirebaseAuth mAuth;
    private DatabaseManager databaseManager;
    private Opportunity opportunity;
    private SimpleDateFormat dateFormat;
    private boolean hasUnsavedChanges = false;
    private OnBackPressedCallback backCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opportunity_details);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        databaseManager = DatabaseManager.getInstance();
        dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

        // Check authentication state
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please sign in to view opportunity details", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        opportunityImage = findViewById(R.id.opportunity_image);
        titleText = findViewById(R.id.opportunity_title);
        organizationText = findViewById(R.id.opportunity_organization);
        locationText = findViewById(R.id.opportunity_location);
        descriptionText = findViewById(R.id.opportunity_description);
        hoursText = findViewById(R.id.opportunity_hours);
        dateText = findViewById(R.id.opportunity_date);
        remoteChip = findViewById(R.id.remote_chip);
        applyButton = findViewById(R.id.apply_button);

        // Get opportunity ID from intent
        String opportunityId = getIntent().getStringExtra("opportunity_id");
        if (opportunityId == null || opportunityId.isEmpty()) {
            Toast.makeText(this, "Invalid opportunity ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load opportunity details
        loadOpportunity(opportunityId);

        // Set up apply button
        applyButton.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() == null) {
                Toast.makeText(this, "Please sign in to apply", Toast.LENGTH_SHORT).show();
                return;
            }
            applyForOpportunity();
        });

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

    private void loadOpportunity(String opportunityId) {
        databaseManager.getOpportunity(opportunityId, new DatabaseManager.DatabaseCallback<Opportunity>() {
            @Override
            public void onSuccess(Opportunity result) {
                if (result == null) {
                    Toast.makeText(OpportunityDetailsActivity.this,
                        "Opportunity not found",
                        Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                opportunity = result;
                updateUI();
            }

            @Override
            public void onError(Exception e) {
                String errorMessage = "Error loading opportunity";
                if (e.getMessage() != null) {
                    errorMessage += ": " + e.getMessage();
                }
                Toast.makeText(OpportunityDetailsActivity.this,
                    errorMessage,
                    Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void updateUI() {
        if (opportunity == null) return;

        titleText.setText(opportunity.getTitle());
        organizationText.setText(opportunity.getOrganization());
        locationText.setText(opportunity.getLocation());
        descriptionText.setText(opportunity.getDescription());
        hoursText.setText(String.format(Locale.getDefault(), "%d hours", opportunity.getRequiredHours()));
        dateText.setText("Deadline: " + dateFormat.format(opportunity.getDeadline()));
        remoteChip.setChecked(opportunity.isRemote());

        if (opportunity.getImageUrl() != null && !opportunity.getImageUrl().isEmpty()) {
            Glide.with(this)
                .load(opportunity.getImageUrl())
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .into(opportunityImage);
        }
    }

    private void applyForOpportunity() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please sign in to apply", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the organization's email from the database
        databaseManager.getOrganization(opportunity.getOrganizationId(), new DatabaseManager.DatabaseCallback<Organization>() {
            @Override
            public void onSuccess(Organization organization) {
                if (organization == null || organization.getEmail() == null || organization.getEmail().isEmpty()) {
                    Toast.makeText(OpportunityDetailsActivity.this,
                        "Organization contact information not available",
                        Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create email intent
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:" + organization.getEmail()));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Application for " + opportunity.getTitle());
                
                // Create email body
                String emailBody = "Dear " + opportunity.getOrganizationName() + ",\n\n" +
                    "I am writing to express my interest in the volunteer opportunity: " + opportunity.getTitle() + "\n\n" +
                    "I would like to apply for this position and would appreciate the opportunity to discuss how my skills and experience align with your requirements.\n\n" +
                    "Best regards,\n" +
                    mAuth.getCurrentUser().getDisplayName();
                
                emailIntent.putExtra(Intent.EXTRA_TEXT, emailBody);

                // Save application in database
                String userId = mAuth.getCurrentUser().getUid();
                String opportunityId = opportunity.getId();
                UserActivity activity = new UserActivity(
                    userId,
                    opportunityId,
                    "Applied to " + opportunity.getTitle(),
                    "You applied to volunteer at " + opportunity.getOrganization(),
                    UserActivity.Type.APPLIED
                );

                databaseManager.saveActivity(activity, new DatabaseManager.DatabaseCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        // Start email client
                        try {
                            startActivity(emailIntent);
                        } catch (android.content.ActivityNotFoundException e) {
                            Toast.makeText(OpportunityDetailsActivity.this,
                                "No email client found",
                                Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        String errorMessage = "Error saving application";
                        if (e.getMessage() != null) {
                            errorMessage += ": " + e.getMessage();
                        }
                        Toast.makeText(OpportunityDetailsActivity.this,
                            errorMessage,
                            Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                String errorMessage = "Error getting organization details";
                if (e.getMessage() != null) {
                    errorMessage += ": " + e.getMessage();
                }
                Toast.makeText(OpportunityDetailsActivity.this,
                    errorMessage,
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (hasUnsavedChanges) {
                showUnsavedChangesDialog();
            } else {
                if (backCallback != null) {
                    backCallback.setEnabled(false);
                }
                getOnBackPressedDispatcher().onBackPressed();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
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
} 