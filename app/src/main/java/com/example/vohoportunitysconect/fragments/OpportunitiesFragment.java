package com.example.vohoportunitysconect.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vohoportunitysconect.R;
import com.example.vohoportunitysconect.activities.OpportunityDetailsActivity;
import com.example.vohoportunitysconect.adapters.OpportunityAdapter;
import com.example.vohoportunitysconect.models.Opportunity;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class OpportunitiesFragment extends Fragment implements OpportunityAdapter.OnOpportunityClickListener {
    private RecyclerView recyclerView;
    private OpportunityAdapter adapter;
    private SearchView searchView;
    private ChipGroup filterChipGroup;
    private List<Opportunity> opportunities;
    private FirebaseFirestore db;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        opportunities = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_opportunities, container, false);

        recyclerView = view.findViewById(R.id.opportunities_recycler);
        searchView = view.findViewById(R.id.search_view);
        filterChipGroup = view.findViewById(R.id.filter_chip_group);

        setupRecyclerView();
        setupSearch();
        setupFilters();
        loadOpportunities();

        return view;
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new OpportunityAdapter(opportunities, this);
        recyclerView.setAdapter(adapter);
    }

    private void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterOpportunities();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterOpportunities();
                return true;
            }
        });
    }

    private void setupFilters() {
        filterChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> filterOpportunities());
    }

    private void loadOpportunities() {
        db.collection("opportunities")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                opportunities.clear();
                opportunities.addAll(queryDocumentSnapshots.toObjects(Opportunity.class));
                adapter.setOpportunities(opportunities);
            })
            .addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Error loading opportunities: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            });
    }

    private void filterOpportunities() {
        String query = searchView.getQuery().toString().toLowerCase();
        List<Opportunity> filteredList = new ArrayList<>();

        for (Opportunity opportunity : opportunities) {
            boolean matchesSearch = opportunity.getTitle().toLowerCase().contains(query) ||
                                  opportunity.getDescription().toLowerCase().contains(query) ||
                                  opportunity.getOrganization().toLowerCase().contains(query);

            boolean matchesFilters = true;
            int checkedChipId = filterChipGroup.getCheckedChipId();
            
            if (checkedChipId != View.NO_ID) {
                Chip checkedChip = filterChipGroup.findViewById(checkedChipId);
                if (checkedChip != null) {
                    String chipText = checkedChip.getText().toString();
                    if (chipText.equals("Remote")) {
                        matchesFilters = opportunity.isRemote();
                    } else if (chipText.equals("Featured")) {
                        matchesFilters = opportunity.isFeatured();
                    }
                }
            }

            if (matchesSearch && matchesFilters) {
                filteredList.add(opportunity);
            }
        }

        adapter.setOpportunities(filteredList);
    }

    @Override
    public void onOpportunityClick(Opportunity opportunity) {
        Intent intent = new Intent(getContext(), OpportunityDetailsActivity.class);
        intent.putExtra("opportunity_id", opportunity.getId());
        startActivity(intent);
    }
} 