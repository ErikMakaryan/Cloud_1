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

import com.example.vohoportunitysconect.R;
import com.example.vohoportunitysconect.databinding.FragmentOpportunityDetailsBinding;
import com.example.vohoportunitysconect.models.Opportunity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
            Navigation.findNavController(requireView())
                .navigate(R.id.opportunitiesFragment);
        });

        // Load opportunity details
        if (opportunityId != null) {
            loadOpportunityDetails();
        } else {
            Toast.makeText(getContext(), "Error: Opportunity ID not found", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(requireView())
                .navigate(R.id.opportunitiesFragment);
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
        binding.opportunityLocation.setText(opportunity.getLocation());
        binding.opportunityDescription.setText(opportunity.getDescription());
        binding.opportunityRequirements.setText(opportunity.getRequirements());
        binding.opportunityBenefits.setText(opportunity.getBenefits());
        binding.opportunityDeadline.setText("Deadline: " + opportunity.getDeadline());
        
        // Update indicators
        binding.urgentIndicator.setVisibility(opportunity.isUrgent() ? View.VISIBLE : View.GONE);
        binding.remoteChip.setVisibility(opportunity.isRemote() ? View.VISIBLE : View.GONE);
        binding.featuredChip.setVisibility(opportunity.isFeatured() ? View.VISIBLE : View.GONE);
        
        // Set up apply button
        binding.applyButton.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() != null) {
                String userId = mAuth.getCurrentUser().getUid();
                databaseRef.child("users").child(userId).child("applications")
                    .child(opportunityId).setValue(true)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Application submitted successfully", 
                            Toast.LENGTH_SHORT).show();
                        binding.applyButton.setEnabled(false);
                        binding.applyButton.setText(R.string.applied);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Error submitting application: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    });
            } else {
                Toast.makeText(getContext(), "Please sign in to apply", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up save button
        binding.saveFab.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() != null) {
                String userId = mAuth.getCurrentUser().getUid();
                databaseRef.child("users").child(userId).child("saved_opportunities")
                    .child(opportunityId).setValue(true)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Opportunity saved successfully", 
                            Toast.LENGTH_SHORT).show();
                        binding.saveFab.setEnabled(false);
                        binding.saveFab.setImageResource(R.drawable.ic_bookmark_filled);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Error saving opportunity: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
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