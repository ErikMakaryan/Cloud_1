package com.example.vohoportunitysconect.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vohoportunitysconect.R;
import com.example.vohoportunitysconect.adapters.OpportunityAdapter;
import com.example.vohoportunitysconect.models.Opportunity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class BrowseOpportunitiesActivity extends AppCompatActivity {
    private RecyclerView opportunitiesRecyclerView;
    private OpportunityAdapter opportunityAdapter;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FloatingActionButton createOpportunityFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_opportunities);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        opportunitiesRecyclerView = findViewById(R.id.opportunities_recycler_view);
        createOpportunityFab = findViewById(R.id.create_opportunity_fab);

        // Setup RecyclerView
        opportunitiesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        opportunityAdapter = new OpportunityAdapter(new ArrayList<>(), this::onOpportunityClick);
        opportunitiesRecyclerView.setAdapter(opportunityAdapter);

        // Setup FAB
        createOpportunityFab.setOnClickListener(v -> 
            startActivity(new android.content.Intent(this, CreateOpportunityActivity.class)));

        // Load opportunities
        loadOpportunities();
    }

    private void loadOpportunities() {
        db.collection("opportunities")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Opportunity> opportunities = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Opportunity opportunity = document.toObject(Opportunity.class);
                    opportunities.add(opportunity);
                }
                opportunityAdapter.setOpportunities(opportunities);
            })
            .addOnFailureListener(e -> 
                Toast.makeText(this, "Error loading opportunities", Toast.LENGTH_SHORT).show());
    }

    private void onOpportunityClick(Opportunity opportunity) {
        // Start OpportunityDetailActivity
        android.content.Intent intent = new android.content.Intent(this, OpportunityDetailActivity.class);
        intent.putExtra("opportunity_id", opportunity.getId());
        startActivity(intent);
    }
} 