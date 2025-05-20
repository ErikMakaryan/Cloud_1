package com.example.vohoportunitysconect.ui.saved;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.vohoportunitysconect.R;
import com.example.vohoportunitysconect.databinding.FragmentSavedOpportunitiesBinding;
import com.example.vohoportunitysconect.models.Opportunity;
import com.example.vohoportunitysconect.ui.opportunities.OpportunityAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SavedOpportunitiesFragment extends Fragment {
    private FragmentSavedOpportunitiesBinding binding;
    private OpportunityAdapter adapter;
    private DatabaseReference databaseRef;
    private FirebaseAuth mAuth;
    private ValueEventListener savedOpportunitiesListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance("https://vvoohh-e2b0a-default-rtdb.firebaseio.com").getReference();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSavedOpportunitiesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (binding == null) return;

        // Setup RecyclerView
        adapter = new OpportunityAdapter(new ArrayList<>(), opportunity -> {
            if (getView() != null) {
                Bundle args = new Bundle();
                args.putString("opportunityId", opportunity.getId());
                Navigation.findNavController(getView())
                    .navigate(R.id.action_savedOpportunitiesFragment_to_opportunityDetailsFragment, args);
            }
        });

        binding.savedOpportunitiesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.savedOpportunitiesRecyclerView.setAdapter(adapter);

        // Setup SwipeRefreshLayout
        binding.swipeRefresh.setOnRefreshListener(this::loadSavedOpportunities);

        // Load saved opportunities
        loadSavedOpportunities();
    }

    private void loadSavedOpportunities() {
        if (binding == null || !isAdded()) return;

        if (mAuth.getCurrentUser() == null) {
            binding.emptyView.setVisibility(View.VISIBLE);
            binding.savedOpportunitiesRecyclerView.setVisibility(View.GONE);
            binding.swipeRefresh.setRefreshing(false);
            return;
        }

        binding.progressIndicator.setVisibility(View.VISIBLE);
        String userId = mAuth.getCurrentUser().getUid();

        // Remove previous listener if exists
        if (savedOpportunitiesListener != null) {
            databaseRef.child("users").child(userId).child("saved_opportunities")
                .removeEventListener(savedOpportunitiesListener);
        }

        savedOpportunitiesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (binding == null || !isAdded()) return;

                List<Opportunity> savedOpportunities = new ArrayList<>();
                
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Opportunity opportunity = snapshot.getValue(Opportunity.class);
                    if (opportunity != null) {
                        opportunity.setId(snapshot.getKey());
                        savedOpportunities.add(opportunity);
                    }
                }

                binding.progressIndicator.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
                
                if (savedOpportunities.isEmpty()) {
                    binding.emptyView.setVisibility(View.VISIBLE);
                    binding.savedOpportunitiesRecyclerView.setVisibility(View.GONE);
                } else {
                    binding.emptyView.setVisibility(View.GONE);
                    binding.savedOpportunitiesRecyclerView.setVisibility(View.VISIBLE);
                    adapter.updateOpportunities(savedOpportunities);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (binding == null || !isAdded()) return;

                binding.progressIndicator.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
                Toast.makeText(getContext(), "Error loading saved opportunities: " + databaseError.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        };

        databaseRef.child("users").child(userId).child("saved_opportunities")
            .addValueEventListener(savedOpportunitiesListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (savedOpportunitiesListener != null && mAuth.getCurrentUser() != null) {
            databaseRef.child("users").child(mAuth.getCurrentUser().getUid()).child("saved_opportunities")
                .removeEventListener(savedOpportunitiesListener);
        }
        binding = null;
    }
} 