package com.example.vohoportunitysconect.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vohoportunitysconect.R;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class OrganizationSetupActivity extends AppCompatActivity {
    private TextInputLayout nameInput;
    private TextInputLayout descriptionInput;
    private TextInputLayout websiteInput;
    private TextInputLayout phoneInput;
    private TextInputLayout addressInput;
    private Button completeButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organization_setup);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        nameInput = findViewById(R.id.organization_name_input);
        descriptionInput = findViewById(R.id.organization_description_input);
        websiteInput = findViewById(R.id.organization_website_input);
        phoneInput = findViewById(R.id.organization_phone_input);
        addressInput = findViewById(R.id.organization_address_input);
        completeButton = findViewById(R.id.complete_setup_button);

        completeButton.setOnClickListener(v -> completeSetup());
    }

    private void completeSetup() {
        String name = nameInput.getEditText().getText().toString().trim();
        String description = descriptionInput.getEditText().getText().toString().trim();
        String website = websiteInput.getEditText().getText().toString().trim();
        String phone = phoneInput.getEditText().getText().toString().trim();
        String address = addressInput.getEditText().getText().toString().trim();

        // Validate inputs
        if (name.isEmpty() || description.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();

        // Update user profile with organization details
        db.collection("users").document(userId)
                .update(
                    "organizationName", name,
                    "organizationDescription", description,
                    "organizationWebsite", website,
                    "phoneNumber", phone,
                    "location", address
                )
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(OrganizationSetupActivity.this,
                            "Organization profile created successfully",
                            Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(OrganizationSetupActivity.this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(OrganizationSetupActivity.this,
                            "Error creating organization profile: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
} 