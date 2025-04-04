package com.example.vohoportunitysconect.ui.opportunities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.vohoportunitysconect.R;
import com.example.vohoportunitysconect.database.DatabaseManager;
import com.example.vohoportunitysconect.models.Opportunity;
import com.google.firebase.auth.FirebaseAuth;

public class OpportunityDetailsFragment extends Fragment {
    private static final String TAG = "OpportunityDetailsFragment";
    private static final String ARG_OPPORTUNITY_ID = "opportunityId";

    private TextView titleTextView;
    private TextView descriptionTextView;
    private TextView organizationTextView;
    private TextView locationTextView;
    private TextView dateTextView;
    private TextView skillsTextView;
    private ImageView imageView;
    private Button applyButton;
    private Button saveButton;
    private ProgressBar progressBar;
    private DatabaseManager databaseManager;
    private FirebaseAuth mAuth;
    private String opportunityId;
    private Opportunity opportunity;

    public static OpportunityDetailsFragment newInstance(String opportunityId) {
        OpportunityDetailsFragment fragment = new OpportunityDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_OPPORTUNITY_ID, opportunityId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseManager = DatabaseManager.getInstance();
        mAuth = FirebaseAuth.getInstance();
        
        if (getArguments() != null) {
            opportunityId = getArguments().getString(ARG_OPPORTUNITY_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_opportunity_details, container, false);

        // Initialize views
        titleTextView = view.findViewById(R.id.title_text_view);
        descriptionTextView = view.findViewById(R.id.description_text_view);
        organizationTextView = view.findViewById(R.id.organization_text_view);
        locationTextView = view.findViewById(R.id.location_text_view);
        dateTextView = view.findViewById(R.id.date_text_view);
        skillsTextView = view.findViewById(R.id.requirements_text_view);
        imageView = view.findViewById(R.id.image_view);
        applyButton = view.findViewById(R.id.apply_button);
        saveButton = view.findViewById(R.id.save_button);
        progressBar = view.findViewById(R.id.progress_bar);

        // Setup click listeners
        applyButton.setOnClickListener(v -> handleApply());
        saveButton.setOnClickListener(v -> handleSave());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadOpportunityDetails();
    }

    private void loadOpportunityDetails() {
        if (opportunityId == null) {
            showError("Invalid opportunity ID");
            return;
        }

        showLoading(true);
        
        databaseManager.getOpportunity(opportunityId, new DatabaseManager.DatabaseCallback<Opportunity>() {
            @Override
            public void onSuccess(Opportunity opportunity) {
                showLoading(false);
                updateUI(opportunity);
            }

            @Override
            public void onError(Exception e) {
                showLoading(false);
                showError("Failed to load opportunity details");
            }
        });
    }

    private void updateUI(Opportunity opportunity) {
        this.opportunity = opportunity;
        
        titleTextView.setText(opportunity.getTitle());
        descriptionTextView.setText(opportunity.getDescription());
        organizationTextView.setText(opportunity.getOrganization());
        locationTextView.setText(opportunity.getLocation());
        dateTextView.setText(opportunity.getDate());
        skillsTextView.setText(opportunity.getSkills());

        if (opportunity.getImageUrl() != null && !opportunity.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(opportunity.getImageUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(imageView);
        }

        // Update button states
        if (mAuth.getCurrentUser() != null) {
            databaseManager.isOpportunitySaved(mAuth.getCurrentUser().getUid(), opportunityId, 
                new DatabaseManager.DatabaseCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean isSaved) {
                        saveButton.setText(isSaved ? "Unsave" : "Save");
                    }

                    @Override
                    public void onError(Exception e) {
                        showError("Failed to check saved status");
                    }
                });
        }
    }

    private void handleApply() {
        if (mAuth.getCurrentUser() == null) {
            showError("Please sign in to apply");
            return;
        }

        if (opportunity == null) {
            showError("Opportunity details not loaded");
            return;
        }

        showLoading(true);
        
        databaseManager.applyForOpportunity(mAuth.getCurrentUser().getUid(), opportunityId, 
            new DatabaseManager.DatabaseCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    showLoading(false);
                    showSuccess("Application submitted successfully");
                    applyButton.setEnabled(false);
                    applyButton.setText("Applied");
                }

                @Override
                public void onError(Exception e) {
                    showLoading(false);
                    showError("Failed to submit application");
                }
            });
    }

    private void handleSave() {
        if (mAuth.getCurrentUser() == null) {
            showError("Please sign in to save opportunities");
            return;
        }

        if (opportunity == null) {
            showError("Opportunity details not loaded");
            return;
        }

        boolean isSaving = saveButton.getText().toString().equals("Save");
        
        databaseManager.toggleSaveOpportunity(mAuth.getCurrentUser().getUid(), opportunityId, isSaving,
            new DatabaseManager.DatabaseCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    saveButton.setText(isSaving ? "Unsave" : "Save");
                    showSuccess(isSaving ? "Opportunity saved" : "Opportunity unsaved");
                }

                @Override
                public void onError(Exception e) {
                    showError("Failed to update saved status");
                }
            });
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void showSuccess(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
} 