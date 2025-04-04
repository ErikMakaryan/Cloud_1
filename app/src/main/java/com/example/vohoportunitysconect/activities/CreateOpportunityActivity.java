package com.example.vohoportunitysconect.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vohoportunitysconect.R;
import com.example.vohoportunitysconect.models.Opportunity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;

public class CreateOpportunityActivity extends AppCompatActivity {
    private TextInputEditText titleInput;
    private TextInputEditText descriptionInput;
    private TextInputEditText locationInput;
    private TextInputEditText startDateInput;
    private TextInputEditText endDateInput;
    private TextInputEditText maxParticipantsInput;
    private TextInputEditText skillsInput;
    private TextInputEditText requirementsInput;
    private TextInputEditText compensationInput;
    private MaterialButton createButton;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseRef;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_opportunity);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference();
        currentUser = mAuth.getCurrentUser();

        // Initialize views
        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        titleInput = findViewById(R.id.title_input);
        descriptionInput = findViewById(R.id.description_input);
        locationInput = findViewById(R.id.location_input);
        startDateInput = findViewById(R.id.start_date_input);
        endDateInput = findViewById(R.id.end_date_input);
        maxParticipantsInput = findViewById(R.id.max_participants_input);
        skillsInput = findViewById(R.id.skills_input);
        requirementsInput = findViewById(R.id.requirements_input);
        compensationInput = findViewById(R.id.compensation_input);
        createButton = findViewById(R.id.create_button);
        
        // Initialize return button
        MaterialButton returnButton = findViewById(R.id.return_button);
        returnButton.setOnClickListener(v -> finish());
    }

    private void setupClickListeners() {
        createButton.setOnClickListener(v -> {
            if (currentUser != null) {
                createOpportunity();
            } else {
                Toast.makeText(this, "Please sign in to create an opportunity", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createOpportunity() {
        // Get input values
        String title = titleInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        String location = locationInput.getText().toString().trim();
        String startDate = startDateInput.getText().toString().trim();
        String endDate = endDateInput.getText().toString().trim();
        String maxParticipantsStr = maxParticipantsInput.getText().toString().trim();
        String skills = skillsInput.getText().toString().trim();
        String requirements = requirementsInput.getText().toString().trim();
        String compensation = compensationInput.getText().toString().trim();

        // Validate inputs
        if (title.isEmpty() || description.isEmpty() || location.isEmpty() || 
            startDate.isEmpty() || endDate.isEmpty() || maxParticipantsStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int maxParticipants = Integer.parseInt(maxParticipantsStr);
            
            // Create new opportunity
            Opportunity opportunity = new Opportunity();
            opportunity.setTitle(title);
            opportunity.setDescription(description);
            opportunity.setLocation(location);
            opportunity.setStartDate(startDate);
            opportunity.setEndDate(endDate);
            opportunity.setMaxParticipants(maxParticipants);
            opportunity.setSkills(skills);
            opportunity.setRequirements(requirements);
            opportunity.setCompensationType(compensation);
            opportunity.setOrganizationId(currentUser.getUid());
            opportunity.setCreatedAt(new Date().getTime());
            opportunity.setStatus("active");

            // Save to Firebase
            String opportunityId = databaseRef.child("opportunities").push().getKey();
            if (opportunityId != null) {
                databaseRef.child("opportunities").child(opportunityId).setValue(opportunity)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Opportunity created successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Error creating opportunity: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid number for max participants", Toast.LENGTH_SHORT).show();
        }
    }
} 