package com.example.vohoportunitysconect.ui.applications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.vohoportunitysconect.R;
import com.example.vohoportunitysconect.adapters.OpportunityAdapter;
import com.example.vohoportunitysconect.models.Opportunity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MyApplicationsFragment extends Fragment implements OpportunityAdapter.OnOpportunityClickListener {
    private RecyclerView applicationsRecycler;
    private OpportunityAdapter opportunityAdapter;
    private List<Opportunity> appliedOpportunities = new ArrayList<>();
    private DatabaseReference applicationsRef;
    private DatabaseReference opportunitiesRef;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView emptyStateText;
    private ProgressBar loadingProgress;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_my_applications, container, false);

        // Initialize views
        applicationsRecycler = root.findViewById(R.id.applications_recycler);
        swipeRefreshLayout = root.findViewById(R.id.swipe_refresh);
        emptyStateText = root.findViewById(R.id.empty_state_text);
        loadingProgress = root.findViewById(R.id.loading_progress);

        // Setup RecyclerView
        applicationsRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        opportunityAdapter = new OpportunityAdapter(appliedOpportunities, this);
        applicationsRecycler.setAdapter(opportunityAdapter);

        // Initialize Firebase
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        applicationsRef = FirebaseDatabase.getInstance().getReference("applications").child(userId);
        opportunitiesRef = FirebaseDatabase.getInstance().getReference("opportunities");

        // Setup swipe refresh
        swipeRefreshLayout.setOnRefreshListener(this::loadApplications);

        // Load applications
        loadApplications();

        return root;
    }

    private void loadApplications() {
        if (getActivity() == null) return;

        getActivity().runOnUiThread(() -> {
            swipeRefreshLayout.setRefreshing(true);
            loadingProgress.setVisibility(View.VISIBLE);
        });

        applicationsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    try {
                        appliedOpportunities.clear();
                        for (DataSnapshot applicationSnapshot : snapshot.getChildren()) {
                            String opportunityId = applicationSnapshot.getKey();
                            if (opportunityId != null) {
                                // Get the full opportunity details
                                opportunitiesRef.child(opportunityId).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot opportunitySnapshot) {
                                        if (opportunitySnapshot.exists()) {
                                            Opportunity opportunity = opportunitySnapshot.getValue(Opportunity.class);
                                            if (opportunity != null) {
                                                opportunity.setId(opportunityId);
                                                appliedOpportunities.add(opportunity);
                                                opportunityAdapter.notifyDataSetChanged();
                                                updateEmptyState();
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(getContext(), "Error loading opportunity: " + error.getMessage(), 
                                            Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Error loading applications: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    } finally {
                        swipeRefreshLayout.setRefreshing(false);
                        loadingProgress.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Error loading applications: " + error.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                    loadingProgress.setVisibility(View.GONE);
                });
            }
        });
    }

    private void updateEmptyState() {
        if (getActivity() == null) return;

        getActivity().runOnUiThread(() -> {
            if (appliedOpportunities.isEmpty()) {
                emptyStateText.setVisibility(View.VISIBLE);
                applicationsRecycler.setVisibility(View.GONE);
                emptyStateText.setText("No applications yet");
            } else {
                emptyStateText.setVisibility(View.GONE);
                applicationsRecycler.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onOpportunityClick(Opportunity opportunity) {
        if (opportunity == null || !isAdded() || getView() == null) {
            return;
        }
        
        try {
            Bundle args = new Bundle();
            args.putString("opportunity_id", opportunity.getId());
            Navigation.findNavController(getView())
                .navigate(R.id.action_myApplicationsFragment_to_opportunityDetailsFragment, args);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error opening opportunity details", Toast.LENGTH_SHORT).show();
        }
    }
} 