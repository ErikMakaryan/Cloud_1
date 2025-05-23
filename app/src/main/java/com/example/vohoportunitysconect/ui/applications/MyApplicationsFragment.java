package com.example.vohoportunitysconect.ui.applications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
    private TextView emptyStateText;
    private View progressBar;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseRef;
    private OpportunityAdapter opportunityAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance("https://vvoohh-e2b0a-default-rtdb.firebaseio.com").getReference();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_my_applications, container, false);
        
        // Initialize views
        applicationsRecycler = root.findViewById(R.id.applications_recycler);
        emptyStateText = root.findViewById(R.id.empty_state_text);
        progressBar = root.findViewById(R.id.progress_bar);

        // Setup RecyclerView
        setupRecyclerView();

        // Load applications
        loadApplications();

        return root;
    }

    private void setupRecyclerView() {
        applicationsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        opportunityAdapter = new OpportunityAdapter(new ArrayList<>(), this);
        applicationsRecycler.setAdapter(opportunityAdapter);
    }

    private void loadApplications() {
        if (mAuth.getCurrentUser() == null) {
            showEmptyState();
            return;
        }

        showLoading();
        String userId = mAuth.getCurrentUser().getUid();
        
        databaseRef.child("users").child(userId).child("applications")
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    List<Opportunity> applications = new ArrayList<>();
                    
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String opportunityId = snapshot.getKey();
                        if (opportunityId != null) {
                            databaseRef.child("opportunities").child(opportunityId)
                                .get().addOnSuccessListener(opportunitySnapshot -> {
                                    Opportunity opportunity = opportunitySnapshot.getValue(Opportunity.class);
                                    if (opportunity != null) {
                                        opportunity.setId(opportunityId);
                                        applications.add(opportunity);
                                        opportunityAdapter.updateOpportunities(applications);
                                        updateUI(applications);
                                    }
                                });
                        }
                    }
                    
                    if (applications.isEmpty()) {
                        showEmptyState();
                    } else {
                        hideLoading();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    hideLoading();
                    Toast.makeText(getContext(), "Error loading applications: " + databaseError.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void updateUI(List<Opportunity> opportunities) {
        if (opportunities.isEmpty()) {
            showEmptyState();
        } else {
            hideEmptyState();
            opportunityAdapter.updateOpportunities(opportunities);
        }
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        applicationsRecycler.setVisibility(View.GONE);
        emptyStateText.setVisibility(View.GONE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
        applicationsRecycler.setVisibility(View.VISIBLE);
    }

    private void showEmptyState() {
        progressBar.setVisibility(View.GONE);
        applicationsRecycler.setVisibility(View.GONE);
        emptyStateText.setVisibility(View.VISIBLE);
    }

    private void hideEmptyState() {
        emptyStateText.setVisibility(View.GONE);
        applicationsRecycler.setVisibility(View.VISIBLE);
    }

    @Override
    public void onOpportunityClick(Opportunity opportunity) {
        Bundle args = new Bundle();
        args.putString("opportunityId", opportunity.getId());
        Navigation.findNavController(requireView())
            .navigate(R.id.action_opportunitiesFragment_to_opportunityDetailsFragment, args);
    }
} 