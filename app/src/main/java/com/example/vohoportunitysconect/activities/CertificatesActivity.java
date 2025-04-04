package com.example.vohoportunitysconect.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vohoportunitysconect.R;
import com.example.vohoportunitysconect.adapters.CertificateAdapter;
import com.example.vohoportunitysconect.models.Certificate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CertificatesActivity extends AppCompatActivity {
    private RecyclerView certificatesRecyclerView;
    private CertificateAdapter certificateAdapter;
    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_certificates);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();

        // Initialize views
        certificatesRecyclerView = findViewById(R.id.certificates_recycler_view);

        // Setup RecyclerView
        certificatesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        certificateAdapter = new CertificateAdapter(this::onCertificateClick);
        certificatesRecyclerView.setAdapter(certificateAdapter);

        // Load certificates
        loadCertificates();
    }

    private void loadCertificates() {
        String userId = mAuth.getCurrentUser().getUid();
        Query query = dbRef.child("certificates")
            .orderByChild("userId")
            .equalTo(userId);
            
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Certificate> certificates = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Certificate certificate = snapshot.getValue(Certificate.class);
                    if (certificate != null) {
                        certificate.setId(snapshot.getKey());
                        certificates.add(certificate);
                    }
                }
                certificateAdapter.setCertificates(certificates);

                // Show empty state if no certificates
                findViewById(R.id.empty_state).setVisibility(
                    certificates.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(CertificatesActivity.this, 
                    "Error loading certificates: " + databaseError.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onCertificateClick(Certificate certificate) {
        // Show certificate details dialog
        showCertificateDetailsDialog(certificate);
    }

    private void showCertificateDetailsDialog(Certificate certificate) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(certificate.getTitle())
            .setMessage(
                "Description: " + certificate.getDescription() + "\n\n" +
                "Skills: " + certificate.getSkills() + "\n" +
                "Hours Completed: " + certificate.getHoursCompleted() + "\n" +
                "Issue Date: " + certificate.getIssueDate()
            )
            .setPositiveButton("Share", (dialog, which) -> shareCertificate(certificate))
            .setNegativeButton("Close", null)
            .show();
    }

    private void shareCertificate(Certificate certificate) {
        android.content.Intent shareIntent = new android.content.Intent();
        shareIntent.setAction(android.content.Intent.ACTION_SEND);
        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT,
            "I received a certificate for volunteering!\n\n" +
            "Title: " + certificate.getTitle() + "\n" +
            "Skills: " + certificate.getSkills() + "\n" +
            "Hours: " + certificate.getHoursCompleted()
        );
        shareIntent.setType("text/plain");
        startActivity(android.content.Intent.createChooser(shareIntent, "Share Certificate"));
    }
} 