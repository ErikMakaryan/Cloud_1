package com.example.vohoportunitysconect.ui.opportunities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.vohoportunitysconect.R;
import com.example.vohoportunitysconect.adapters.OpportunityAdapter;
import com.example.vohoportunitysconect.models.Opportunity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class OpportunitiesFragment extends Fragment implements OpportunityAdapter.OnOpportunityClickListener {
    private RecyclerView opportunitiesRecycler;
    private OpportunityAdapter opportunityAdapter;
    private List<Opportunity> opportunities = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_opportunities, container, false);
        
        opportunitiesRecycler = root.findViewById(R.id.opportunities_recycler);
        opportunitiesRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        opportunityAdapter = new OpportunityAdapter(opportunities, this);
        opportunitiesRecycler.setAdapter(opportunityAdapter);

        loadOpportunities();
        
        return root;
    }

    private void loadOpportunities() {
        FirebaseFirestore.getInstance()
            .collection("opportunities")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                opportunities.clear();
                for (var doc : queryDocumentSnapshots) {
                    Opportunity opportunity = doc.toObject(Opportunity.class);
                    opportunity.setId(doc.getId());
                    opportunities.add(opportunity);
                }
                opportunityAdapter.notifyDataSetChanged();
            })
            .addOnFailureListener(e -> {
                // Handle error
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