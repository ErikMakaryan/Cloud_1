package com.example.vohoportunitysconect.fragments;

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
import com.example.vohoportunitysconect.adapters.ActivityAdapter;
import com.example.vohoportunitysconect.adapters.OpportunityAdapter;
import com.example.vohoportunitysconect.models.Activity;
import com.example.vohoportunitysconect.models.Opportunity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements OpportunityAdapter.OnOpportunityClickListener {
    private TextView welcomeText;
    private TextView hoursText;
    private TextView projectsText;
    private RecyclerView featuredOpportunitiesRecycler;
    private RecyclerView recentActivityRecycler;
    private MaterialButton viewAllOpportunitiesButton;
    private MaterialButton viewAllActivityButton;
    private OpportunityAdapter opportunityAdapter;
    private ActivityAdapter activityAdapter;
    
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        
        // Initialize views
        welcomeText = root.findViewById(R.id.welcome_text);
        hoursText = root.findViewById(R.id.hours_text);
        projectsText = root.findViewById(R.id.projects_text);
        featuredOpportunitiesRecycler = root.findViewById(R.id.featured_opportunities_recycler);
        recentActivityRecycler = root.findViewById(R.id.recent_activity_recycler);
        viewAllOpportunitiesButton = root.findViewById(R.id.view_all_opportunities_button);
        viewAllActivityButton = root.findViewById(R.id.view_all_activity_button);

        // Setup RecyclerViews
        setupRecyclerViews();
        
        // Setup button click listeners
        setupButtonListeners();

        // Load user data
        loadUserData();
        
        // Load opportunities and activities
        loadFeaturedOpportunities();
        loadRecentActivity();

        return root;
    }

    private void setupButtonListeners() {
        viewAllOpportunitiesButton.setOnClickListener(v -> {
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_homeFragment_to_opportunitiesFragment);
        });

        viewAllActivityButton.setOnClickListener(v -> {
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_homeFragment_to_applicationsFragment);
        });
    }

    private void setupRecyclerViews() {
        // Featured opportunities
        featuredOpportunitiesRecycler.setLayoutManager(
            new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        opportunityAdapter = new OpportunityAdapter(new ArrayList<>(), this);
        featuredOpportunitiesRecycler.setAdapter(opportunityAdapter);

        // Recent activity
        recentActivityRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        activityAdapter = new ActivityAdapter();
        recentActivityRecycler.setAdapter(activityAdapter);
    }

    private void loadUserData() {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        welcomeText.setText("Welcome back, " + name + "!");
                        
                        // Update stats
                        Long hours = documentSnapshot.getLong("volunteer_hours");
                        Long projects = documentSnapshot.getLong("completed_projects");
                        
                        hoursText.setText(String.valueOf(hours != null ? hours : 0));
                        projectsText.setText(String.valueOf(projects != null ? projects : 0));
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error loading user data: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
        }
    }

    private void loadFeaturedOpportunities() {
        db.collection("opportunities")
            .whereEqualTo("featured", true)
            .limit(5)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Opportunity> opportunities = queryDocumentSnapshots.toObjects(Opportunity.class);
                opportunityAdapter.setOpportunities(opportunities);
                
                // Show/hide view all button based on opportunities count
                viewAllOpportunitiesButton.setVisibility(
                    opportunities.isEmpty() ? View.GONE : View.VISIBLE);
            })
            .addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Error loading featured opportunities: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            });
    }

    private void loadRecentActivity() {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            db.collection("activities")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Activity> activities = queryDocumentSnapshots.toObjects(Activity.class);
                    activityAdapter.setActivities(activities);
                    
                    // Show/hide view all button based on activities count
                    viewAllActivityButton.setVisibility(
                        activities.isEmpty() ? View.GONE : View.VISIBLE);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error loading recent activity: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
        }
    }

    @Override
    public void onOpportunityClick(Opportunity opportunity) {
        Bundle args = new Bundle();
        args.putString("opportunityId", opportunity.getId());
        Navigation.findNavController(requireView())
                .navigate(R.id.action_homeFragment_to_opportunityDetailsFragment, args);
    }
} 