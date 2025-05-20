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
        certificateAdapter = new CertificateAdapter(new ArrayList<>(), new CertificateAdapter.OnCertificateClickListener() {
            @Override
            public void onCertificateClick(Certificate certificate) {
                showCertificateDetailsDialog(certificate);
            }

            @Override
            public void onDeleteClick(Certificate certificate) {
                showDeleteConfirmationDialog(certificate);
            }
        });
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
                certificateAdapter.updateCertificates(certificates);

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

    private void showCertificateDetailsDialog(Certificate certificate) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(certificate.getName())
            .setMessage(
                "Certificate Name: " + certificate.getName() + "\n\n" +
                "Upload Date: " + new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                    .format(new java.util.Date(certificate.getUploadDate()))
            )
            .setPositiveButton("View", (dialog, which) -> {
                // Open the certificate file
                android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
                intent.setData(android.net.Uri.parse(certificate.getFileUrl()));
                startActivity(intent);
            })
            .setNegativeButton("Close", null)
            .show();
    }

    private void shareCertificate(Certificate certificate) {
        android.content.Intent shareIntent = new android.content.Intent();
        shareIntent.setAction(android.content.Intent.ACTION_SEND);
        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT,
            "I received a certificate!\n\n" +
            "Certificate Name: " + certificate.getName() + "\n" +
            "Upload Date: " + new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                .format(new java.util.Date(certificate.getUploadDate()))
        );
        shareIntent.setType("text/plain");
        startActivity(android.content.Intent.createChooser(shareIntent, "Share Certificate"));
    }

    private void showDeleteConfirmationDialog(Certificate certificate) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Delete Certificate")
            .setMessage("Are you sure you want to delete this certificate?")
            .setPositiveButton("Delete", (dialog, which) -> deleteCertificate(certificate))
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void deleteCertificate(Certificate certificate) {
        String userId = mAuth.getCurrentUser().getUid();
        dbRef.child("certificates")
            .child(certificate.getId())
            .removeValue()
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Certificate deleted successfully", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to delete certificate: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
} 