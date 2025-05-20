package com.example.vohoportunitysconect.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vohoportunitysconect.R;
import com.example.vohoportunitysconect.models.Opportunity;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class OpportunityDetailActivity extends AppCompatActivity {
    private MaterialTextView titleText;
    private MaterialTextView organizationText;
    private MaterialTextView descriptionText;
    
    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;
    private String opportunityId;
    private Opportunity opportunity;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opportunity_detail);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();

        // Get opportunity ID from intent
        opportunityId = getIntent().getStringExtra("opportunity_id");
        if (opportunityId == null) {
            Toast.makeText(this, "Error: Opportunity not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        titleText = findViewById(R.id.opportunity_title);
        organizationText = findViewById(R.id.organization_name);
        descriptionText = findViewById(R.id.opportunity_description);

        // Load opportunity details
        loadOpportunityDetails();
    }

    private void loadOpportunityDetails() {
        dbRef.child("opportunities").child(opportunityId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    opportunity = dataSnapshot.getValue(Opportunity.class);
                    if (opportunity != null) {
                        opportunity.setId(dataSnapshot.getKey());
                        titleText.setText(opportunity.getTitle());
                        organizationText.setText(opportunity.getOrganization());
                        descriptionText.setText(opportunity.getDescription());
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(OpportunityDetailActivity.this, 
                        "Error loading opportunity details: " + databaseError.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
    }
} 