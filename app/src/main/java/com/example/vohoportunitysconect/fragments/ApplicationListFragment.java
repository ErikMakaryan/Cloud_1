package com.example.vohoportunitysconect.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.vohoportunitysconect.R;
import com.example.vohoportunitysconect.adapters.ApplicationAdapter;
import com.example.vohoportunitysconect.databinding.FragmentApplicationsBinding;
import com.example.vohoportunitysconect.models.Application;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ApplicationListFragment extends Fragment implements ApplicationAdapter.OnItemClickListener {
    private static final String TAG = "ApplicationListFragment";
    private static final String ARG_STATUS = "status";
    private static final String ARG_IS_ORGANIZER = "isOrganizer";

    private FragmentApplicationsBinding binding;
    private DatabaseReference databaseRef;
    private FirebaseAuth mAuth;
    private ApplicationAdapter adapter;
    private List<Application> applications;
    private Application.Status status;
    private boolean isOrganizer;
    private ValueEventListener applicationsListener;

    public static ApplicationListFragment newInstance(Application.Status status, boolean isOrganizer) {
        ApplicationListFragment fragment = new ApplicationListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_STATUS, status.name());
        args.putBoolean(ARG_IS_ORGANIZER, isOrganizer);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance("https://vvoohh-e2b0a-default-rtdb.firebaseio.com").getReference();
        applications = new ArrayList<>();

        if (getArguments() != null) {
            String statusValue = getArguments().getString(ARG_STATUS);
            status = Application.Status.fromString(statusValue);
            isOrganizer = getArguments().getBoolean(ARG_IS_ORGANIZER, false);
        }
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
        checkAndLoadApplications();
    }

    private void setupRecyclerView() {
        adapter = new ApplicationAdapter(applications, this);
        binding.applicationsRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.applicationsRecycler.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener(this::checkAndLoadApplications);
    }

    private void checkAndLoadApplications() {
        if (mAuth.getCurrentUser() == null) {
            showEmptyState();
            return;
        }

        showLoading();
        String userId = mAuth.getCurrentUser().getUid();

        // First check if user has any applications
        databaseRef.child("applications")
            .orderByChild("userId")
            .equalTo(userId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists()) {
                        // No applications found
                        hideLoading();
                        showEmptyState();
                        return;
                    }

                    // Applications exist, now load them
                    loadApplications(userId);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    hideLoading();
                    Log.e(TAG, "Error checking applications: " + databaseError.getMessage());
                    Toast.makeText(requireContext(), "Error checking applications: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void loadApplications(String userId) {
        // Remove existing listener if any
        if (applicationsListener != null) {
            databaseRef.child("applications").removeEventListener(applicationsListener);
        }

        Query query = databaseRef.child("applications")
            .orderByChild("userId")
            .equalTo(userId);

        applicationsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                applications.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Application application = snapshot.getValue(Application.class);
                    if (application != null) {
                        // If status filter is set, only add matching applications
                        if (status == null || application.getStatusEnum() == status) {
                            applications.add(application);
                        }
                    }
                }
                updateUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                hideLoading();
                Log.e(TAG, "Error loading applications: " + databaseError.getMessage());
                Toast.makeText(requireContext(), "Error loading applications: " + databaseError.getMessage(),
                    Toast.LENGTH_SHORT).show();
            }
        };

        query.addValueEventListener(applicationsListener);
    }

    @Override
    public void onItemClick(Application application) {
        // Navigate to opportunity details
        Bundle args = new Bundle();
        args.putString("opportunityId", application.getOpportunityId());
        args.putString("previousFragment", "applications");
        // Navigation.findNavController(requireView()).navigate(R.id.opportunityDetailsFragment, args);
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
        if (applicationsListener != null) {
            databaseRef.child("applications").removeEventListener(applicationsListener);
        }
        binding = null;
    }
} 