package com.example.vohoportunitysconect.ui.home;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements OpportunityAdapter.OnOpportunityClickListener {
    private TextView welcomeText;
    private TextView hoursText;
    private TextView projectsText;
    private RecyclerView featuredOpportunitiesRecycler;
    private RecyclerView recentActivityRecycler;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseRef;
    private OpportunityAdapter opportunityAdapter;
    private ActivityAdapter activityAdapter;
    private TextView opportunitiesButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance("https://vvoohh-e2b0a-default-rtdb.firebaseio.com").getReference();
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
        opportunitiesButton = root.findViewById(R.id.view_all_opportunities_button);

        // Set up adapters
        setupRecyclerViews();

        // Load data
        loadUserData();
        loadFeaturedOpportunities();
        loadRecentActivity();

        opportunitiesButton.setOnClickListener(v -> 
            Navigation.findNavController(requireView())
                .navigate(R.id.action_homeFragment_to_opportunitiesFragment)
        );

        return root;
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
            databaseRef.child("users").child(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String name = dataSnapshot.child("name").getValue(String.class);
                            welcomeText.setText("Welcome back, " + name + "!");
                            
                            // Update stats
                            Long hours = dataSnapshot.child("volunteer_hours").getValue(Long.class);
                            Long projects = dataSnapshot.child("completed_projects").getValue(Long.class);
                            
                            if (hours != null) {
                                hoursText.setText(hours + " hours");
                            }
                            if (projects != null) {
                                projectsText.setText(projects + " projects");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(getContext(), "Error loading user data: " + databaseError.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    }
                });
        }
    }

    private void loadFeaturedOpportunities() {
        Query featuredQuery = databaseRef.child("opportunities")
            .orderByChild("featured")
            .equalTo(true)
            .limitToLast(5);

        featuredQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Opportunity> opportunities = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Opportunity opportunity = snapshot.getValue(Opportunity.class);
                    if (opportunity != null) {
                        opportunity.setId(snapshot.getKey());
                        opportunities.add(opportunity);
                    }
                }
                opportunityAdapter.updateOpportunities(opportunities);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Error loading opportunities: " + databaseError.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadRecentActivity() {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            Query activityQuery = databaseRef.child("activities")
                .orderByChild("userId")
                .equalTo(userId)
                .limitToLast(5);

            activityQuery.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    List<Activity> activities = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Activity activity = snapshot.getValue(Activity.class);
                        if (activity != null) {
                            activity.setId(snapshot.getKey());
                            activities.add(activity);
                        }
                    }
                    activityAdapter.updateActivities(activities);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getContext(), "Error loading activities: " + databaseError.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onOpportunityClick(Opportunity opportunity) {
        // Navigate to opportunity details
        Bundle args = new Bundle();
        args.putString("opportunityId", opportunity.getId());
        Navigation.findNavController(requireView())
            .navigate(R.id.action_homeFragment_to_opportunityDetailsFragment, args);
    }
} 