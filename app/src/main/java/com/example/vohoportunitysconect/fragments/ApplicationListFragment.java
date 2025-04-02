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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ApplicationListFragment extends Fragment {
    private static final String ARG_STATUS = "status";
    private RecyclerView recyclerView;
    private ApplicationAdapter adapter;
    private List<Application> applications;
    private DatabaseReference databaseRef;
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
        databaseRef = FirebaseDatabase.getInstance("https://vvoohh-e2b0a-default-rtdb.firebaseio.com").getReference();
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
        Query applicationsQuery = databaseRef.child("applications")
                .orderByChild("userId")
                .equalTo(userId);

        applicationsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                applications.clear();
                for (DataSnapshot applicationSnapshot : dataSnapshot.getChildren()) {
                    Application application = applicationSnapshot.getValue(Application.class);
                    if (application != null && application.getStatus() == status) {
                        application.setId(applicationSnapshot.getKey());
                        applications.add(application);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Error loading applications: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
} 