package com.example.vohoportunitysconect.fragments;

import android.os.Bundle;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ApplicationListFragment extends Fragment {
    private static final String ARG_STATUS = "status";
    private RecyclerView recyclerView;
    private ApplicationAdapter adapter;
    private List<Application> applications;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private Application.Status status;

    public static ApplicationListFragment newInstance(Application.Status status) {
        ApplicationListFragment fragment = new ApplicationListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_STATUS, status.name());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String statusValue = getArguments().getString(ARG_STATUS);
            status = Application.Status.fromString(statusValue);
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
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        applications = new ArrayList<>();
    }

    private void setupRecyclerView() {
        adapter = new ApplicationAdapter(applications, application -> {
            // Handle application click
            Toast.makeText(getContext(), "Clicked: " + application.getOpportunityTitle(), Toast.LENGTH_SHORT).show();
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void loadApplications() {
        String userId = auth.getCurrentUser().getUid();
        db.collection("applications")
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", status.name())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    applications.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // Get the opportunity details from the opportunity document
                        long opportunityId = document.getLong("opportunityId");
                        db.collection("opportunities")
                                .document(String.valueOf(opportunityId))
                                .get()
                                .addOnSuccessListener(opportunityDoc -> {
                                    String opportunityTitle = opportunityDoc.getString("title");
                                    String organizationImageUrl = opportunityDoc.getString("organizationImageUrl");
                                    String organizationName = opportunityDoc.getString("organizationName");
                                    Application application = new Application(
                                            document.getId(),
                                            document.getString("userId"),
                                            document.getString("opportunityId"),
                                            document.getString("opportunityTitle"),
                                            document.getString("organizationId"),
                                            document.getString("organizationName"),
                                            document.getString("organizationImageUrl"),
                                            document.getDate("appliedDate"),
                                            Application.Status.fromString(document.getString("status")),
                                            document.getString("coverLetter")
                                    );
                                    applications.add(application);
                                    adapter.notifyDataSetChanged();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "Error loading opportunity details: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error loading applications: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
} 