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
import com.example.vohoportunitysconect.databinding.FragmentMyApplicationsBinding;
import com.example.vohoportunitysconect.models.Opportunity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MyApplicationsFragment extends Fragment implements OpportunityAdapter.OnOpportunityClickListener {
    private FragmentMyApplicationsBinding binding;
    private List<Opportunity> applications = new ArrayList<>();
    private OpportunityAdapter adapter;
    private DatabaseReference databaseRef;
    private ValueEventListener applicationsListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMyApplicationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupRecyclerView();
        databaseRef = FirebaseDatabase.getInstance("https://vvoohh-e2b0a-default-rtdb.firebaseio.com").getReference();
        loadApplications();
    }

    private void setupRecyclerView() {
        adapter = new OpportunityAdapter(applications, this);
        binding.applicationsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.applicationsRecycler.setAdapter(adapter);
    }

    private void loadApplications() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser().getUid();

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.emptyStateText.setVisibility(View.GONE);

        // Remove existing listener if any
        if (applicationsListener != null) {
            databaseRef.child("applications").removeEventListener(applicationsListener);
        }

        // Query applications for current user
        Query query = databaseRef.child("applications")
            .orderByChild("userId")
            .equalTo(userId);

        applicationsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                applications.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Opportunity opportunity = snapshot.getValue(Opportunity.class);
                    if (opportunity != null) {
                        applications.add(opportunity);
                    }
                }

                binding.progressBar.setVisibility(View.GONE);
                if (applications.isEmpty()) {
                    binding.emptyStateText.setVisibility(View.VISIBLE);
                } else {
                    binding.emptyStateText.setVisibility(View.GONE);
                }
                adapter.updateOpportunities(applications);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error loading applications: " + databaseError.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        };

        query.addValueEventListener(applicationsListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (applicationsListener != null) {
            databaseRef.child("applications").removeEventListener(applicationsListener);
        }
        binding = null;
    }

    @Override
    public void onOpportunityClick(Opportunity opportunity) {
        Bundle args = new Bundle();
        args.putString("opportunityId", opportunity.getId());
        args.putString("previousFragment", "applications");
        Navigation.findNavController(requireView())
            .navigate(R.id.action_opportunitiesFragment_to_opportunityDetailsFragment, args);
    }
} 