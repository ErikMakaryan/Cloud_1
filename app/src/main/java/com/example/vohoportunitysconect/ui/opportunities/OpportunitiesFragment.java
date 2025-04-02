package com.example.vohoportunitysconect.ui.opportunities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.vohoportunitysconect.R;
import com.example.vohoportunitysconect.adapters.OpportunityAdapter;
import com.example.vohoportunitysconect.models.Opportunity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class OpportunitiesFragment extends Fragment implements OpportunityAdapter.OnOpportunityClickListener {
    private RecyclerView opportunitiesRecycler;
    private OpportunityAdapter opportunityAdapter;
    private List<Opportunity> opportunities = new ArrayList<>();
    private DatabaseReference databaseRef;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_opportunities, container, false);
        
        // Initialize Firebase Database
        databaseRef = FirebaseDatabase.getInstance("https://vvoohh-e2b0a-default-rtdb.firebaseio.com").getReference();
        
        opportunitiesRecycler = root.findViewById(R.id.opportunities_recycler);
        opportunitiesRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        opportunityAdapter = new OpportunityAdapter(opportunities, this);
        opportunitiesRecycler.setAdapter(opportunityAdapter);

        loadOpportunities();
        
        return root;
    }

    private void loadOpportunities() {
        Query opportunitiesQuery = databaseRef.child("opportunities")
            .orderByChild("createdAt")
            .limitToLast(50);

        opportunitiesQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                opportunities.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Opportunity opportunity = snapshot.getValue(Opportunity.class);
                    if (opportunity != null) {
                        opportunity.setId(snapshot.getKey());
                        opportunities.add(opportunity);
                    }
                }
                opportunityAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Error loading opportunities: " + databaseError.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onOpportunityClick(Opportunity opportunity) {
        // Navigate to opportunity details
        Bundle args = new Bundle();
        args.putString("opportunityId", opportunity.getId());
        Navigation.findNavController(requireView())
            .navigate(R.id.action_opportunitiesFragment_to_opportunityDetailsFragment, args);
    }
} 