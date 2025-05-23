package com.example.vohoportunitysconect.fragments;

import android.annotation.SuppressLint;
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
import androidx.recyclerview.widget.RecyclerView;

import com.example.vohoportunitysconect.R;
import com.example.vohoportunitysconect.adapters.ApplicationAdapter;
import com.example.vohoportunitysconect.models.Application;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ApplicationListFragment extends Fragment implements ApplicationAdapter.OnApplicationClickListener {
    private static final String ARG_STATUS = "status";
    private RecyclerView recyclerView;
    private ApplicationAdapter adapter;
    private List<Application> applications;
    private DatabaseReference databaseRef;
    private FirebaseAuth auth;
    private Application.Status status;
    private ValueEventListener valueEventListener;
    private boolean isOrganizer;

    public static ApplicationListFragment newInstance(Application.Status status, boolean isOrganizer) {
        ApplicationListFragment fragment = new ApplicationListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_STATUS, status.name());
        args.putBoolean("isOrganizer", isOrganizer);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String statusValue = getArguments().getString(ARG_STATUS);
            status = Application.Status.fromString(statusValue);
            isOrganizer = getArguments().getBoolean("isOrganizer", false);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_application_list, container, false);
        initializeViews(view);
        setupRecyclerView();
        loadApplications();
        return view;
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.applications_recycler);
        databaseRef = FirebaseDatabase.getInstance("https://vvoohh-e2b0a-default-rtdb.firebaseio.com").getReference();
        auth = FirebaseAuth.getInstance();
        applications = new ArrayList<>();
    }

    private void setupRecyclerView() {
        adapter = new ApplicationAdapter(applications, this, isOrganizer);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void loadApplications() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Please sign in to view applications", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference applicationsRef = databaseRef.child("applications").child(userId);

        // Remove existing listener if any
        if (valueEventListener != null) {
            applicationsRef.removeEventListener(valueEventListener);
        }

        valueEventListener = new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!isAdded() || getContext() == null) return;
                
                applications.clear();
                for (DataSnapshot applicationSnapshot : dataSnapshot.getChildren()) {
                    try {
                        Application application = applicationSnapshot.getValue(Application.class);
                        if (application != null && application.getStatusEnum() == status) {
                            application.setId(applicationSnapshot.getKey());
                            applications.add(application);
                        }
                    } catch (Exception e) {
                        Log.e("ApplicationList", "Error parsing application: " + e.getMessage());
                    }
                }
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (!isAdded() || getContext() == null) return;
                Toast.makeText(getContext(), 
                    "Error loading applications: " + databaseError.getMessage(),
                    Toast.LENGTH_SHORT).show();
                Log.e("ApplicationList", "Database error: " + databaseError.getMessage());
            }
        };

        applicationsRef.addValueEventListener(valueEventListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (databaseRef != null && valueEventListener != null) {
            databaseRef.removeEventListener(valueEventListener);
            valueEventListener = null;
        }
    }

    @Override
    public void onAcceptClick(Application application) {
        updateApplicationStatus(application, "accepted");
    }

    @Override
    public void onRejectClick(Application application) {
        updateApplicationStatus(application, "rejected");
    }

    @Override
    public void onCancelClick(Application application) {
        if (!isOrganizer) {
            databaseRef.child("applications").child(application.getVolunteerId())
                    .child(application.getOpportunityId())
                    .removeValue();
        }
    }

    private void updateApplicationStatus(Application application, String status) {
        if (isOrganizer) {
            // Update in organizer's applications
            databaseRef.child("organizer_applications").child(application.getOrganizerId())
                    .child(application.getOpportunityId())
                    .child("status")
                    .setValue(status);

            // Update in volunteer's applications
            databaseRef.child("applications").child(application.getVolunteerId())
                    .child(application.getOpportunityId())
                    .child("status")
                    .setValue(status);
        }
    }
} 