package com.example.vohoportunitysconect.ui.opportunities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.vohoportunitysconect.R;
import com.example.vohoportunitysconect.models.Opportunity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class OpportunityDetailsFragment extends Fragment {
    private static final String ARG_OPPORTUNITY_ID = "opportunity_id";
    private String opportunityId;
    private TextView titleTextView;
    private TextView organizationTextView;
    private TextView locationTextView;
    private TextView descriptionTextView;
    private TextView requirementsTextView;
    private TextView benefitsTextView;
    private TextView deadlineTextView;
    private ImageView urgentIndicator;
    private Chip remoteChip;
    private Chip featuredChip;
    private FloatingActionButton saveFab;
    private MaterialButton applyButton;
    private DatabaseReference opportunityRef;
    private DatabaseReference savedRef;
    private DatabaseReference applicationsRef;
    private boolean isSaved = false;
    private boolean hasApplied = false;

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
        if (getArguments() != null) {
            opportunityId = getArguments().getString(ARG_OPPORTUNITY_ID);
        }

        // Handle back press
        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_opportunity_details, container, false);

        // Initialize views
        titleTextView = view.findViewById(R.id.opportunity_title);
        organizationTextView = view.findViewById(R.id.opportunity_organization);
        locationTextView = view.findViewById(R.id.opportunity_location);
        descriptionTextView = view.findViewById(R.id.opportunity_description);
        requirementsTextView = view.findViewById(R.id.opportunity_requirements);
        benefitsTextView = view.findViewById(R.id.opportunity_benefits);
        deadlineTextView = view.findViewById(R.id.opportunity_deadline);
        urgentIndicator = view.findViewById(R.id.urgent_indicator);
        remoteChip = view.findViewById(R.id.remote_chip);
        featuredChip = view.findViewById(R.id.featured_chip);
        saveFab = view.findViewById(R.id.save_fab);
        applyButton = view.findViewById(R.id.apply_button);

        // Setup toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        // Initialize Firebase
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        opportunityRef = FirebaseDatabase.getInstance().getReference("opportunities").child(opportunityId);
        savedRef = FirebaseDatabase.getInstance().getReference("saved_opportunities").child(userId).child(opportunityId);
        applicationsRef = FirebaseDatabase.getInstance().getReference("applications").child(userId).child(opportunityId);

        // Load opportunity data
        loadOpportunityData();
        checkSavedStatus();
        checkApplicationStatus();

        // Setup save button
        saveFab.setOnClickListener(v -> toggleSave());

        // Setup apply button
        applyButton.setOnClickListener(v -> toggleApply());

        return view;
    }

    private void loadOpportunityData() {
        opportunityRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Opportunity opportunity = snapshot.getValue(Opportunity.class);
                    if (opportunity != null) {
                        updateUI(opportunity);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error loading opportunity: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkSavedStatus() {
        savedRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                isSaved = snapshot.exists();
                updateSaveButton();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error checking saved status: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkApplicationStatus() {
        applicationsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                hasApplied = snapshot.exists();
                updateApplyButton();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error checking application status: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleSave() {
        if (isSaved) {
            // Unsave
            savedRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    isSaved = false;
                    updateSaveButton();
                    Toast.makeText(getContext(), "Opportunity unsaved", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> 
                    Toast.makeText(getContext(), "Error unsaving opportunity: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            // Save
            savedRef.setValue(true)
                .addOnSuccessListener(aVoid -> {
                    isSaved = true;
                    updateSaveButton();
                    Toast.makeText(getContext(), "Opportunity saved", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> 
                    Toast.makeText(getContext(), "Error saving opportunity: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void toggleApply() {
        if (hasApplied) {
            // Withdraw application
            applicationsRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    hasApplied = false;
                    updateApplyButton();
                    Toast.makeText(getContext(), "Application withdrawn", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> 
                    Toast.makeText(getContext(), "Error withdrawing application: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            // Apply
            applicationsRef.setValue(true)
                .addOnSuccessListener(aVoid -> {
                    hasApplied = true;
                    updateApplyButton();
                    Toast.makeText(getContext(), "Application submitted successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> 
                    Toast.makeText(getContext(), "Error submitting application: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void updateSaveButton() {
        if (isSaved) {
            saveFab.setImageResource(R.drawable.ic_bookmark_filled);
        } else {
            saveFab.setImageResource(R.drawable.ic_bookmark);
        }
    }

    private void updateApplyButton() {
        if (hasApplied) {
            applyButton.setText("Withdraw Application");
            applyButton.setEnabled(true);
        } else {
            applyButton.setText("Apply Now");
            applyButton.setEnabled(true);
        }
    }

    private void updateUI(Opportunity opportunity) {
        titleTextView.setText(opportunity.getTitle());
        organizationTextView.setText(opportunity.getOrganization());
        locationTextView.setText(opportunity.getLocation());
        descriptionTextView.setText(opportunity.getDescription());
        requirementsTextView.setText(opportunity.getRequirements());
        benefitsTextView.setText(opportunity.getBenefits());
        deadlineTextView.setText("Deadline: " + opportunity.getDeadline());

        // Update indicators
        urgentIndicator.setVisibility(opportunity.isUrgent() ? View.VISIBLE : View.GONE);
        remoteChip.setVisibility(opportunity.isRemote() ? View.VISIBLE : View.GONE);
        featuredChip.setVisibility(opportunity.isFeatured() ? View.VISIBLE : View.GONE);
    }
} 