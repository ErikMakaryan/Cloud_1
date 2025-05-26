package com.example.vohoportunitysconect.ui.opportunities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import com.example.vohoportunitysconect.MainActivity;
import com.example.vohoportunitysconect.R;
import com.example.vohoportunitysconect.databinding.FragmentOpportunityDetailsBinding;
import com.example.vohoportunitysconect.models.Opportunity;
import com.example.vohoportunitysconect.utils.EmailUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class OpportunityDetailsFragment extends Fragment {
    private FragmentOpportunityDetailsBinding binding;
    private DatabaseReference databaseRef;
    private FirebaseAuth mAuth;
    private String opportunityId;
    private static final int EMAIL_REQUEST_CODE = 1;
    private String currentUserId;
    private String currentOpportunityId;
    private Opportunity currentOpportunity;

    private final ActivityResultLauncher<Intent> emailLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == android.app.Activity.RESULT_OK) {
                if (currentUserId != null && currentOpportunityId != null) {
                    // Create application data
                    String applicationId = UUID.randomUUID().toString();
                    Map<String, Object> application = new HashMap<>();
                    application.put("id", applicationId);
                    application.put("userId", currentUserId);
                    application.put("opportunityId", currentOpportunityId);
                    application.put("title", currentOpportunity.getTitle());
                    application.put("organization", currentOpportunity.getOrganization());
                    application.put("status", "PENDING");
                    application.put("appliedAt", System.currentTimeMillis());
                    application.put("organizerId", currentOpportunity.getOrganizationId());

                    // Save application
                    databaseRef.child("applications").child(applicationId)
                        .setValue(application)
                        .addOnSuccessListener(aVoid -> {
                            // Update UI
                            if (binding != null) {
                                binding.applyButton.setText(R.string.applied);
                                binding.applyButton.setEnabled(false);
                                binding.applyButton.setBackgroundColor(getResources().getColor(R.color.gray, null));
                                Toast.makeText(getContext(), "Application submitted!", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Failed to save application: " + e.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                        });
                }
            }
        }
    );

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance("https://vvoohh-e2b0a-default-rtdb.firebaseio.com").getReference();
        
        // Get opportunity ID from arguments
        if (getArguments() != null) {
            opportunityId = getArguments().getString("opportunityId");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOpportunityDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup back button
        binding.backButton.setOnClickListener(v -> {
            if (getArguments() != null) {
                String previousFragment = getArguments().getString("previousFragment");
                if (previousFragment != null) {
                    if (previousFragment.equals("saved")) {
                        Navigation.findNavController(requireView())
                            .navigate(R.id.savedOpportunitiesFragment);
                    } else if (previousFragment.equals("opportunities")) {
                        Navigation.findNavController(requireView())
                            .navigate(R.id.opportunitiesFragment);
                    }
                }
            }
        });

        // Set title and back button text based on previous fragment
        if (getArguments() != null) {
            String previousFragment = getArguments().getString("previousFragment");
            if (previousFragment != null) {
                if (previousFragment.equals("saved")) {
                    binding.opportunityTitle.setText(R.string.saved);
                    binding.backButtonText.setText(R.string.saved);
                } else if (previousFragment.equals("opportunities")) {
                    binding.opportunityTitle.setText(R.string.opportunities);
                    binding.backButtonText.setText(R.string.opportunities);
                }
            }
        }

        // Load opportunity details
        if (opportunityId != null) {
            loadOpportunityDetails();
        } else {
            Toast.makeText(getContext(), "Error: Opportunity ID not found", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(requireView()).popBackStack();
        }

        binding.applyButton.setOnClickListener(v -> handleApplyClick());
        checkApplicationStatus();
    }

    private void loadOpportunityDetails() {
        databaseRef.child("opportunities").child(opportunityId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Opportunity opportunity = dataSnapshot.getValue(Opportunity.class);
                        if (opportunity != null) {
                            opportunity.setId(opportunityId);
                            updateUI(opportunity);
                        }
                    } else {
                        Toast.makeText(getContext(), "Opportunity not found", Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(requireView())
                            .navigate(R.id.opportunitiesFragment);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getContext(), "Error loading opportunity: " + databaseError.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView())
                        .navigate(R.id.opportunitiesFragment);
                }
            });
    }

    private void updateUI(Opportunity opportunity) {
        if (binding == null) return;
        
        currentOpportunity = opportunity;
        
        // Batch UI updates
        binding.opportunityTitle.post(() -> {
            binding.opportunityTitle.setText(opportunity.getTitle());
            binding.opportunityOrganization.setText(opportunity.getOrganization());
            
            // Update visibility and text in a single pass
            updateSectionVisibility(binding.locationTitle, binding.opportunityLocation, opportunity.getLocation());
            updateSectionVisibility(binding.descriptionTitle, binding.opportunityDescription, opportunity.getDescription());
            updateSectionVisibility(binding.requirementsTitle, binding.opportunityRequirements, opportunity.getRequirements());
            updateSectionVisibility(binding.benefitsTitle, binding.opportunityBenefits, opportunity.getBenefits());
            
            // Update deadline
            if (opportunity.getDeadline() != null) {
                binding.deadlineTitle.setVisibility(View.VISIBLE);
                binding.opportunityDeadline.setText(new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                    .format(opportunity.getDeadline()));
            } else {
                binding.deadlineTitle.setVisibility(View.GONE);
                binding.opportunityDeadline.setText("");
            }
            
            // Update indicators
            binding.urgentIndicator.setVisibility(opportunity.isUrgent() ? View.VISIBLE : View.GONE);
            binding.remoteChip.setVisibility(opportunity.isRemote() ? View.VISIBLE : View.GONE);
            binding.featuredChip.setVisibility(opportunity.isFeatured() ? View.VISIBLE : View.GONE);
        });

        // Setup apply button and saved state
        setupApplyButton(opportunity);
        setupSavedState(opportunity);
    }

    private void updateSectionVisibility(View titleView, TextView contentView, String content) {
        if (content != null && !content.isEmpty()) {
            titleView.setVisibility(View.VISIBLE);
            contentView.setText(content);
        } else {
            titleView.setVisibility(View.GONE);
            contentView.setText("");
        }
    }

    private void setupApplyButton(Opportunity opportunity) {
        if (mAuth.getCurrentUser() == null) {
            updateApplyButtonState(false);
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        databaseRef.child("applications")
            .orderByChild("userId")
            .equalTo(userId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean isApplied = false;
                    for (DataSnapshot applicationSnapshot : snapshot.getChildren()) {
                        String appOpportunityId = applicationSnapshot.child("opportunityId").getValue(String.class);
                        if (opportunity.getId().equals(appOpportunityId)) {
                            isApplied = true;
                            break;
                        }
                    }
                    updateApplyButtonState(isApplied);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Error checking application status", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void setupSavedState(Opportunity opportunity) {
        if (mAuth.getCurrentUser() == null) {
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        databaseRef.child("users").child(userId).child("saved_opportunities")
            .child(opportunity.getId())
            .get()
            .addOnSuccessListener(snapshot -> {
                boolean isSaved = snapshot.exists();
                if (isSaved) {
                } else {
                }
            });
    }

    private void updateApplyButtonState(boolean isApplied) {
        if (binding != null) {
            binding.applyButton.setEnabled(!isApplied);
            binding.applyButton.setText(isApplied ? "Applied" : "Apply");
            binding.applyButton.setBackgroundTintList(ContextCompat.getColorStateList(
                requireContext(),
                isApplied ? R.color.gray : R.color.green
            ));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clear references to prevent memory leaks
        if (binding != null) {
            binding.applyButton.setOnClickListener(null);
            binding.backButton.setOnClickListener(null);
        }
        binding = null;
    }

    private void checkApplicationStatus() {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            String opportunityId = getArguments().getString("opportunityId");

            databaseRef.child("applications")
                .orderByChild("userId")
                .equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean isApplied = false;
                        for (DataSnapshot applicationSnapshot : snapshot.getChildren()) {
                            String appOpportunityId = applicationSnapshot.child("opportunityId").getValue(String.class);
                            if (opportunityId != null && opportunityId.equals(appOpportunityId)) {
                                isApplied = true;
                                break;
                            }
                        }
                        updateApplyButtonState(isApplied);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Error checking application status", Toast.LENGTH_SHORT).show();
                    }
                });
        }
    }

    private void handleApplyClick() {
        if (mAuth.getCurrentUser() != null && currentOpportunity != null) {
            String userId = mAuth.getCurrentUser().getUid();
            
            // First update the button state
            updateApplyButtonState(true);
            
            // Get organizer's email
            databaseRef.child("users").child(currentOpportunity.getOrganizationId())
                .child("email")
                .get()
                .addOnSuccessListener(emailSnapshot -> {
                    String organizerEmail = emailSnapshot.getValue(String.class);
                    if (organizerEmail != null && !organizerEmail.isEmpty()) {
                        String subject = "Application for: " + currentOpportunity.getTitle();
                        String body = "Hello,\n\nI am interested in applying for the opportunity '" + 
                            currentOpportunity.getTitle() + "' at " + currentOpportunity.getOrganization() + 
                            ".\n\nPlease let me know about the next steps.\n\nThank you!";
                        
                        Intent emailIntent = new Intent(Intent.ACTION_SEND);
                        emailIntent.setType("message/rfc822");
                        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{organizerEmail});
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
                        emailIntent.putExtra(Intent.EXTRA_TEXT, body);
                        
                        try {
                            currentUserId = userId;
                            currentOpportunityId = currentOpportunity.getId();
                            emailLauncher.launch(Intent.createChooser(emailIntent, "Send email..."));
                        } catch (android.content.ActivityNotFoundException ex) {
                            Toast.makeText(getContext(), "No email clients installed.", Toast.LENGTH_SHORT).show();
                            // Revert button state if email client is not available
                            updateApplyButtonState(false);
                        }
                    } else {
                        Toast.makeText(getContext(), "Organizer email not available.", Toast.LENGTH_SHORT).show();
                        // Revert button state if email is not available
                        updateApplyButtonState(false);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error getting organizer email: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                    // Revert button state if there's an error
                    updateApplyButtonState(false);
                });
        } else {
            Toast.makeText(getContext(), "Please sign in to apply", Toast.LENGTH_SHORT).show();
        }
    }
} 