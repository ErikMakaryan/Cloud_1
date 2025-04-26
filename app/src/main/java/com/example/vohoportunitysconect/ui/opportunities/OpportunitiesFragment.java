package com.example.vohoportunitysconect.ui.opportunities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.vohoportunitysconect.R;
import com.example.vohoportunitysconect.adapters.OpportunityAdapter;
import com.example.vohoportunitysconect.models.Opportunity;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OpportunitiesFragment extends Fragment implements OpportunityAdapter.OnOpportunityClickListener {
    private static final String TAG = "OpportunitiesFragment";
    private RecyclerView opportunitiesRecycler;
    private OpportunityAdapter opportunityAdapter;
    private List<Opportunity> opportunities = new ArrayList<>();
    private List<Opportunity> filteredOpportunities = new ArrayList<>();
    private DatabaseReference databaseRef;
    private SwipeRefreshLayout swipeRefreshLayout;
    private SearchView searchView;
    private TextView emptyStateText;
    private ProgressBar loadingProgress;
    private ChipGroup filterChipGroup;
    private Chip remoteChip;
    private Chip featuredChip;
    private Chip urgentChip;
    private String currentCategory = "";

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_opportunities, container, false);
        
        // Initialize views first
        swipeRefreshLayout = root.findViewById(R.id.swipe_refresh);
        searchView = root.findViewById(R.id.search_view);
        emptyStateText = root.findViewById(R.id.empty_state_text);
        loadingProgress = root.findViewById(R.id.loading_progress);
        filterChipGroup = root.findViewById(R.id.filter_chip_group);
        remoteChip = root.findViewById(R.id.chip_remote);
        featuredChip = root.findViewById(R.id.chip_featured);
        urgentChip = root.findViewById(R.id.chip_urgent);
        
        // Initialize RecyclerView
        opportunitiesRecycler = root.findViewById(R.id.opportunities_recycler);
        opportunitiesRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        opportunityAdapter = new OpportunityAdapter(filteredOpportunities, this);
        opportunitiesRecycler.setAdapter(opportunityAdapter);
        
        // Setup UI components
        setupSearchView();
        setupFilterChips();
        swipeRefreshLayout.setOnRefreshListener(this::loadOpportunities);
        
        // Initialize Firebase in a separate method
        initializeFirebase();
        
        return root;
    }

    @SuppressLint("SetTextI18n")
    private void initializeFirebase() {
        if (getActivity() == null) return;
        
        try {
            // Initialize Firebase Database with error handling
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            
            // Get reference and enable offline persistence
            databaseRef = database.getReference();
            databaseRef.keepSynced(true);
            
            // Test connection with retry mechanism
            testDatabaseConnection(databaseRef, 3); // Try 3 times
            
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase: " + e.getMessage(), e);
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Error connecting to database: " + e.getMessage(), 
                        Toast.LENGTH_LONG).show();
                    emptyStateText.setText("Error connecting to database. Please check your internet connection and try again.");
                    emptyStateText.setVisibility(View.VISIBLE);
                    loadingProgress.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                });
            }
        }
    }

    private void testDatabaseConnection(DatabaseReference ref, int retryCount) {
        if (retryCount <= 0) {
            Log.e(TAG, "Failed to connect to database after multiple attempts");
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Failed to connect to database. Please try again later.", 
                        Toast.LENGTH_LONG).show();
                    emptyStateText.setText("Error connecting to database. Please check your internet connection and try again.");
                    emptyStateText.setVisibility(View.VISIBLE);
                    loadingProgress.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                });
            }
            return;
        }

        ref.child(".info/connected").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    Log.d(TAG, "Firebase Database connected successfully");
                    // Load opportunities only when connected
                    loadOpportunities();
                } else {
                    Log.w(TAG, "Firebase Database disconnected, retrying...");
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "No internet connection. Using cached data.", 
                                Toast.LENGTH_SHORT).show();
                            loadOpportunities(); // Try to load cached data
                        });
                    }
                    // Retry connection
                    testDatabaseConnection(ref, retryCount - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Firebase Database connection listener cancelled: " + error.getMessage());
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Error connecting to database: " + error.getMessage(), 
                            Toast.LENGTH_LONG).show();
                        loadOpportunities(); // Try to load cached data
                    });
                }
                // Retry connection
                testDatabaseConnection(ref, retryCount - 1);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clear references to avoid memory leaks
        if (databaseRef != null) {
            databaseRef.removeEventListener(opportunitiesListener);
        }
    }

    private final ValueEventListener opportunitiesListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if (getActivity() == null) return;
            
            getActivity().runOnUiThread(() -> {
                try {
                    opportunities.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Opportunity opportunity = snapshot.getValue(Opportunity.class);
                        if (opportunity != null) {
                            opportunity.setId(snapshot.getKey());
                            opportunities.add(opportunity);
                        }
                    }
                    
                    filteredOpportunities.clear();
                    filteredOpportunities.addAll(opportunities);
                    opportunityAdapter.notifyDataSetChanged();
                    updateEmptyState();
                    
                    Log.d(TAG, "Successfully loaded " + opportunities.size() + " opportunities");
                } catch (Exception e) {
                    Log.e(TAG, "Error processing data: " + e.getMessage(), e);
                    Toast.makeText(getContext(), "Error processing data: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                } finally {
                    swipeRefreshLayout.setRefreshing(false);
                    loadingProgress.setVisibility(View.GONE);
                }
            });
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            if (getActivity() == null) return;
            
            getActivity().runOnUiThread(() -> {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
                Toast.makeText(getContext(), "Error loading opportunities: " + databaseError.getMessage(), 
                    Toast.LENGTH_LONG).show();
                swipeRefreshLayout.setRefreshing(false);
                loadingProgress.setVisibility(View.GONE);
            });
        }
    };

    private void loadOpportunities() {
        if (getActivity() == null || databaseRef == null) return;
        
        getActivity().runOnUiThread(() -> {
            swipeRefreshLayout.setRefreshing(true);
            loadingProgress.setVisibility(View.VISIBLE);
        });
        
        Query opportunitiesQuery = databaseRef.child("opportunities")
            .orderByChild("createdAt")
            .limitToLast(50);

        opportunitiesQuery.addValueEventListener(opportunitiesListener);
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                applyFilters();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Check if the search text contains a category prefix
                if (newText.toLowerCase().startsWith("category:")) {
                    currentCategory = newText.substring(9).trim().toLowerCase();
                } else {
                    currentCategory = "";
                }
                applyFilters();
                return true;
            }
        });

        // Add search suggestions
        searchView.setQueryHint("Search by name or use 'category:name' to filter by category");
    }

    @SuppressWarnings("deprecation")
    private void setupFilterChips() {
        filterChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            applyFilters();
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void applyFilters() {
        if (getActivity() == null || opportunityAdapter == null || searchView == null || 
            remoteChip == null || featuredChip == null || urgentChip == null) {
            return;
        }
        
        CharSequence query = searchView.getQuery();
        String searchQuery = query != null ? query.toString().toLowerCase(Locale.getDefault()) : "";
        boolean isRemoteSelected = remoteChip.isChecked();
        boolean isFeaturedSelected = featuredChip.isChecked();
        boolean isUrgentSelected = urgentChip.isChecked();

        List<Opportunity> newFilteredList = new ArrayList<>();
        for (Opportunity opportunity : opportunities) {
            if (opportunity == null) continue;
            
            boolean matchesSearch = true;
            
            // Handle category search
            if (!currentCategory.isEmpty()) {
                String category = opportunity.getCategory();
                matchesSearch = category != null && 
                    category.toLowerCase(Locale.getDefault()).contains(currentCategory);
            } else if (!searchQuery.isEmpty()) {
                // Regular search
                String title = opportunity.getTitle();
                String organization = opportunity.getOrganization();
                String description = opportunity.getDescription();
                String location = opportunity.getLocation();
                String category = opportunity.getCategory();
                
                matchesSearch = (title != null && title.toLowerCase(Locale.getDefault()).contains(searchQuery)) ||
                    (organization != null && organization.toLowerCase(Locale.getDefault()).contains(searchQuery)) ||
                    (description != null && description.toLowerCase(Locale.getDefault()).contains(searchQuery)) ||
                    (location != null && location.toLowerCase(Locale.getDefault()).contains(searchQuery)) ||
                    (category != null && category.toLowerCase(Locale.getDefault()).contains(searchQuery));
            }

            boolean matchesFilters = true;
            if (isRemoteSelected && !opportunity.isRemote()) {
                matchesFilters = false;
            }
            if (isFeaturedSelected && !opportunity.isFeatured()) {
                matchesFilters = false;
            }
            if (isUrgentSelected && !opportunity.isUrgent()) {
                matchesFilters = false;
            }

            if (matchesSearch && matchesFilters) {
                newFilteredList.add(opportunity);
            }
        }

        Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(() -> {
                filteredOpportunities.clear();
                filteredOpportunities.addAll(newFilteredList);
                opportunityAdapter.notifyDataSetChanged();
                updateEmptyState();
            });
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateEmptyState() {
        if (getActivity() == null) return;
        
        getActivity().runOnUiThread(() -> {
            if (filteredOpportunities.isEmpty()) {
                emptyStateText.setVisibility(View.VISIBLE);
                opportunitiesRecycler.setVisibility(View.GONE);
                if (!currentCategory.isEmpty()) {
                    emptyStateText.setText("No opportunities found in category: " + currentCategory);
                } else if (!searchView.getQuery().toString().isEmpty()) {
                    emptyStateText.setText("No opportunities found matching your search");
                } else {
                    emptyStateText.setText("No opportunities available");
                }
            } else {
                emptyStateText.setVisibility(View.GONE);
                opportunitiesRecycler.setVisibility(View.VISIBLE);
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
            NavController navController = Navigation.findNavController(getView());
            if (navController != null) {
                navController.navigate(R.id.action_opportunitiesFragment_to_opportunityDetailsFragment, args);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to details: " + e.getMessage(), e);
            Context context = getContext();
            if (context != null) {
                Toast.makeText(context, "Error opening opportunity details", Toast.LENGTH_SHORT).show();
            }
        }
    }
} 