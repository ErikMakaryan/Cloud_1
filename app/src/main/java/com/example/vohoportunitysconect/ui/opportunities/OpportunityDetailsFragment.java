package com.example.vohoportunitysconect.ui.opportunities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

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

public class OpportunityDetailsFragment extends Fragment {
    private FragmentOpportunityDetailsBinding binding;
    private DatabaseReference databaseRef;
    private FirebaseAuth mAuth;
    private String opportunityId;

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
        binding.opportunityTitle.setText(opportunity.getTitle());
        binding.opportunityOrganization.setText(opportunity.getOrganization());
        
        // Location
        binding.locationTitle.setVisibility(opportunity.getLocation() != null && !opportunity.getLocation().isEmpty() ? View.VISIBLE : View.GONE);
        binding.opportunityLocation.setText(opportunity.getLocation());
        
        // Description
        binding.descriptionTitle.setVisibility(opportunity.getDescription() != null && !opportunity.getDescription().isEmpty() ? View.VISIBLE : View.GONE);
        binding.opportunityDescription.setText(opportunity.getDescription());
        
        // Requirements
        binding.requirementsTitle.setVisibility(opportunity.getRequirements() != null && !opportunity.getRequirements().isEmpty() ? View.VISIBLE : View.GONE);
        binding.opportunityRequirements.setText(opportunity.getRequirements());
        
        // Benefits
        binding.benefitsTitle.setVisibility(opportunity.getBenefits() != null && !opportunity.getBenefits().isEmpty() ? View.VISIBLE : View.GONE);
        binding.opportunityBenefits.setText(opportunity.getBenefits());
        
        // Deadline
        binding.deadlineTitle.setVisibility(opportunity.getDeadline() != null ? View.VISIBLE : View.GONE);
        binding.opportunityDeadline.setText(opportunity.getDeadline() != null ? 
            new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                .format(opportunity.getDeadline()) : "");
        
        // Update indicators
        binding.urgentIndicator.setVisibility(opportunity.isUrgent() ? View.VISIBLE : View.GONE);
        binding.remoteChip.setVisibility(opportunity.isRemote() ? View.VISIBLE : View.GONE);
        binding.featuredChip.setVisibility(opportunity.isFeatured() ? View.VISIBLE : View.GONE);
        
        // Set up apply button
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            // Check if already applied
            databaseRef.child("applications").child(userId).child(opportunityId)
                .get().addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        binding.applyButton.setText(R.string.applied);
                        binding.applyButton.setEnabled(false);
                    } else {
                        binding.applyButton.setText(R.string.apply_now);
                        binding.applyButton.setEnabled(true);
                    }
                });
        }

        binding.applyButton.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() != null) {
                String userId = mAuth.getCurrentUser().getUid();
                
                // Create application data
                Map<String, Object> applicationData = new HashMap<>();
                applicationData.put("opportunityId", opportunity.getId());
                applicationData.put("opportunityTitle", opportunity.getTitle());
                applicationData.put("organization", opportunity.getOrganization());
                applicationData.put("status", "pending");
                applicationData.put("appliedAt", System.currentTimeMillis());
                applicationData.put("userId", userId);
                
                // Add to applications node
                databaseRef.child("applications")
                    .child(userId)
                    .child(opportunityId)
                    .setValue(applicationData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Application submitted successfully", 
                            Toast.LENGTH_SHORT).show();
                        binding.applyButton.setText(R.string.applied);
                        binding.applyButton.setEnabled(false);

                        // Get organizer's email and send notification
                        databaseRef.child("users").child(opportunity.getOrganizationId())
                            .child("email")
                            .get()
                            .addOnSuccessListener(snapshot -> {
                                String organizerEmail = snapshot.getValue(String.class);
                                if (organizerEmail != null && !organizerEmail.isEmpty()) {
                                    String subject = "New Application: " + opportunity.getTitle();
                                    String body = String.format(
                                        "Hello,\n\n" +
                                        "A new application has been submitted for your opportunity:\n\n" +
                                        "Opportunity: %s\n" +
                                        "Applicant: %s\n\n" +
                                        "Please review the application in your dashboard.\n\n" +
                                        "Best regards,\n" +
                                        "VOH Opportunities Connect",
                                        opportunity.getTitle(),
                                        mAuth.getCurrentUser().getEmail()
                                    );
                                    
                                    EmailUtils.sendEmail(requireContext(), organizerEmail, subject, body);
                                }
                            });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Error submitting application: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    });
            } else {
                Toast.makeText(getContext(), "Please sign in to apply", Toast.LENGTH_SHORT).show();
            }
        });

        // Check if opportunity is saved
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            databaseRef.child("users").child(userId).child("saved_opportunities")
                .child(opportunityId).get().addOnSuccessListener(snapshot -> {
                    boolean isSaved = snapshot.exists();
                    binding.saveFab.setImageResource(isSaved ? R.drawable.ic_bookmark_filled : R.drawable.ic_bookmark_border);
                    if (isSaved) {
                        binding.saveFab.setColorFilter(getResources().getColor(R.color.orange, null));
                    } else {
                        binding.saveFab.clearColorFilter();
                    }
                });
        }

        // Set up save button
        binding.saveFab.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() != null) {
                String userId = mAuth.getCurrentUser().getUid();
                DatabaseReference savedRef = databaseRef.child("users").child(userId).child("saved_opportunities");
                
                savedRef.child(opportunityId).get().addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        // Remove from saved
                        savedRef.child(opportunityId).removeValue()
                            .addOnSuccessListener(aVoid -> {
                                binding.saveFab.setImageResource(R.drawable.ic_bookmark_border);
                                binding.saveFab.clearColorFilter();
                                Toast.makeText(getContext(), "Removed from saved", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Error removing from saved: " + e.getMessage(), 
                                    Toast.LENGTH_SHORT).show();
                            });
                    } else {
                        // Add to saved
                        Map<String, Object> savedData = new HashMap<>();
                        savedData.put("id", opportunity.getId());
                        savedData.put("title", opportunity.getTitle());
                        savedData.put("organization", opportunity.getOrganization());
                        savedData.put("location", opportunity.getLocation());
                        savedData.put("category", opportunity.getCategory());
                        savedData.put("description", opportunity.getDescription());
                        savedData.put("requirements", opportunity.getRequirements());
                        savedData.put("benefits", opportunity.getBenefits());
                        savedData.put("deadline", opportunity.getDeadline());
                        savedData.put("savedAt", System.currentTimeMillis());

                        savedRef.child(opportunityId).setValue(savedData)
                            .addOnSuccessListener(aVoid -> {
                                binding.saveFab.setImageResource(R.drawable.ic_bookmark_filled);
                                binding.saveFab.setColorFilter(getResources().getColor(R.color.orange, null));
                                Toast.makeText(getContext(), "Saved opportunity", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Error saving opportunity: " + e.getMessage(), 
                                    Toast.LENGTH_SHORT).show();
                            });
                    }
                });
            } else {
                Toast.makeText(getContext(), "Please sign in to save opportunities", 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 