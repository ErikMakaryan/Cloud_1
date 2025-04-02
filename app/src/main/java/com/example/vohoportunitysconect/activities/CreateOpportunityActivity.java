package com.example.vohoportunitysconect.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vohoportunitysconect.R;
import com.example.vohoportunitysconect.models.Opportunity;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class CreateOpportunityActivity extends AppCompatActivity {
    private TextInputLayout titleInput;
    private TextInputLayout descriptionInput;
    private TextInputLayout locationInput;
    private TextInputLayout categoryInput;
    private TextInputLayout skillsInput;
    private TextInputLayout maxVolunteersInput;
    private TextInputLayout compensationInput;
    private Button deadlineButton;
    private Button difficultyButton;
    private Button createButton;
    private Date selectedDeadline;
    private Opportunity.Difficulty selectedDifficulty;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_opportunity);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        titleInput = findViewById(R.id.opportunity_title_input);
        descriptionInput = findViewById(R.id.opportunity_description_input);
        locationInput = findViewById(R.id.opportunity_location_input);
        categoryInput = findViewById(R.id.opportunity_category_input);
        skillsInput = findViewById(R.id.opportunity_skills_input);
        maxVolunteersInput = findViewById(R.id.opportunity_max_volunteers_input);
        compensationInput = findViewById(R.id.opportunity_compensation_input);
        deadlineButton = findViewById(R.id.deadline_button);
        difficultyButton = findViewById(R.id.difficulty_button);
        createButton = findViewById(R.id.create_opportunity_button);

        // Set up listeners
        deadlineButton.setOnClickListener(v -> showDatePicker());
        difficultyButton.setOnClickListener(v -> showDifficultyDialog());
        createButton.setOnClickListener(v -> createOpportunity());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                calendar.set(year, month, dayOfMonth);
                selectedDeadline = calendar.getTime();
                deadlineButton.setText(String.format("Deadline: %tF", selectedDeadline));
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void showDifficultyDialog() {
        String[] difficulties = {"Easy", "Medium", "Hard"};
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Select Difficulty")
            .setItems(difficulties, (dialog, which) -> {
                selectedDifficulty = Opportunity.Difficulty.values()[which];
                difficultyButton.setText("Difficulty: " + difficulties[which]);
            })
            .show();
    }

    private void createOpportunity() {
        // Validate inputs
        if (!validateInputs()) {
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        String opportunityId = UUID.randomUUID().toString();

        // Create opportunity object
        Opportunity opportunity = new Opportunity(
            opportunityId,
            titleInput.getEditText().getText().toString().trim(),
            descriptionInput.getEditText().getText().toString().trim(),
            userId,
            getOrganizationName(), // This should be fetched from the user's profile
            locationInput.getEditText().getText().toString().trim(),
            categoryInput.getEditText().getText().toString().trim(),
            skillsInput.getEditText().getText().toString().trim()
        );

        // Set additional fields
        opportunity.setDeadline(selectedDeadline);
        opportunity.setDifficulty(selectedDifficulty);
        opportunity.setMaxVolunteers(Integer.parseInt(maxVolunteersInput.getEditText().getText().toString()));
        opportunity.setCompensationType(compensationInput.getEditText().getText().toString().trim());

        // Save to Firestore
        db.collection("opportunities").document(opportunityId)
            .set(opportunity)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(CreateOpportunityActivity.this,
                        "Opportunity created successfully",
                        Toast.LENGTH_SHORT).show();
                finish();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(CreateOpportunityActivity.this,
                        "Error creating opportunity: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            });
    }

    private boolean validateInputs() {
        if (titleInput.getEditText().getText().toString().trim().isEmpty()) {
            titleInput.setError("Title is required");
            return false;
        }
        if (descriptionInput.getEditText().getText().toString().trim().isEmpty()) {
            descriptionInput.setError("Description is required");
            return false;
        }
        if (locationInput.getEditText().getText().toString().trim().isEmpty()) {
            locationInput.setError("Location is required");
            return false;
        }
        if (categoryInput.getEditText().getText().toString().trim().isEmpty()) {
            categoryInput.setError("Category is required");
            return false;
        }
        if (skillsInput.getEditText().getText().toString().trim().isEmpty()) {
            skillsInput.setError("Skills are required");
            return false;
        }
        if (maxVolunteersInput.getEditText().getText().toString().trim().isEmpty()) {
            maxVolunteersInput.setError("Maximum volunteers is required");
            return false;
        }
        if (selectedDeadline == null) {
            Toast.makeText(this, "Please select a deadline", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (selectedDifficulty == null) {
            Toast.makeText(this, "Please select difficulty level", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private String getOrganizationName() {
        // This should be fetched from the user's profile
        // For now, returning a placeholder
        return "Organization Name";
    }
} 