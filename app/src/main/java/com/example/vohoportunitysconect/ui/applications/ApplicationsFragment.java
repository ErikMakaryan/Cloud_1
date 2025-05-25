package com.example.vohoportunitysconect.ui.applications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.vohoportunitysconect.R;
import com.example.vohoportunitysconect.adapters.ApplicationAdapter;
import com.example.vohoportunitysconect.databinding.FragmentApplicationsBinding;
import com.example.vohoportunitysconect.models.Application;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ApplicationsFragment extends Fragment {
    private FragmentApplicationsBinding binding;
    private DatabaseReference databaseRef;
    private FirebaseAuth mAuth;
    private ApplicationAdapter adapter;
    private List<Application> applications;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance("https://vvoohh-e2b0a-default-rtdb.firebaseio.com").getReference();
        applications = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentApplicationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        setupSwipeRefresh();
        loadApplications();
    }

    private void setupRecyclerView() {
        adapter = new ApplicationAdapter(applications, application -> {
            // Handle application click
            // Navigate to opportunity details
            Bundle args = new Bundle();
            args.putString("opportunityId", application.getOpportunityId());
            args.putString("previousFragment", "applications");
            // Navigation.findNavController(requireView()).navigate(R.id.opportunityDetailsFragment, args);
        });

        binding.applicationsRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.applicationsRecycler.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener(this::loadApplications);
    }

    private void loadApplications() {
        if (mAuth.getCurrentUser() == null) {
            showEmptyState();
            return;
        }

        showLoading();
        String userId = mAuth.getCurrentUser().getUid();

        databaseRef.child("applications")
            .orderByChild("userId")
            .equalTo(userId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    applications.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Application application = snapshot.getValue(Application.class);
                        if (application != null) {
                            applications.add(application);
                        }
                    }
                    updateUI();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    hideLoading();
                    Toast.makeText(requireContext(), "Error loading applications: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void updateUI() {
        hideLoading();
        if (applications.isEmpty()) {
            showEmptyState();
        } else {
            hideEmptyState();
            adapter.updateApplications(applications);
        }
    }

    private void showLoading() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.applicationsRecycler.setVisibility(View.GONE);
        binding.emptyStateText.setVisibility(View.GONE);
    }

    private void hideLoading() {
        binding.progressBar.setVisibility(View.GONE);
        binding.swipeRefresh.setRefreshing(false);
    }

    private void showEmptyState() {
        binding.emptyStateText.setVisibility(View.VISIBLE);
        binding.applicationsRecycler.setVisibility(View.GONE);
    }

    private void hideEmptyState() {
        binding.emptyStateText.setVisibility(View.GONE);
        binding.applicationsRecycler.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 