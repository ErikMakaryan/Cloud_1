package com.example.vohoportunitysconect.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;
import android.widget.ImageView;
import android.app.Dialog;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vohoportunitysconect.R;
import com.example.vohoportunitysconect.adapters.CertificateAdapter;
import com.example.vohoportunitysconect.models.Certificate;
import com.example.vohoportunitysconect.database.CertificateDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CertificatesActivity extends AppCompatActivity {
    private RecyclerView certificatesRecyclerView;
    private CertificateAdapter certificateAdapter;
    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;
    private FirebaseStorage storage;
    private Dialog loadingDialog;
    private CertificateDatabase certificateDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_certificates);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        storage = FirebaseStorage.getInstance();

        // Initialize local database
        certificateDatabase = new CertificateDatabase(this);

        // Initialize loading dialog
        loadingDialog = new Dialog(this);
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loadingDialog.setContentView(R.layout.dialog_loading);
        loadingDialog.setCancelable(false);
        Window window = loadingDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Initialize views
        certificatesRecyclerView = findViewById(R.id.certificates_recycler_view);

        // Setup RecyclerView
        certificatesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        certificateAdapter = new CertificateAdapter(new ArrayList<>(), new CertificateAdapter.OnCertificateClickListener() {
            @Override
            public void onCertificateClick(Certificate certificate) {
                showCertificatePreview(certificate);
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

    private void showLoadingDialog(String message) {
        TextView messageText = loadingDialog.findViewById(R.id.loading_message);
        if (messageText != null) {
            messageText.setText(message);
        }
        loadingDialog.show();
    }

    private void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
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
                        // Save to local database
                        certificateDatabase.saveCertificate(certificate);
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

    private void showCertificatePreview(Certificate certificate) {
        showLoadingDialog("Loading certificate...");

        try {
            // Get the file reference from Firebase Storage
            StorageReference storageRef = storage.getReferenceFromUrl(certificate.getFileUrl());
            
            // Create a temporary file
            File localFile = File.createTempFile("certificate", ".pdf", getCacheDir());
            
            // Download the file
            storageRef.getFile(localFile)
                .addOnSuccessListener(taskSnapshot -> {
                    hideLoadingDialog();
                    showPreviewDialog(localFile);
                })
                .addOnFailureListener(e -> {
                    hideLoadingDialog();
                    Toast.makeText(this, "Error downloading certificate: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        } catch (IOException e) {
            hideLoadingDialog();
            Toast.makeText(this, "Error preparing certificate: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showPreviewDialog(File pdfFile) {
        Dialog previewDialog = new Dialog(this);
        previewDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        previewDialog.setContentView(R.layout.dialog_certificate_preview);
        
        ImageView previewImage = previewDialog.findViewById(R.id.preview_image);
        
        try {
            ParcelFileDescriptor fileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY);
            PdfRenderer pdfRenderer = new PdfRenderer(fileDescriptor);
            
            // Render first page
            PdfRenderer.Page page = pdfRenderer.openPage(0);
            Bitmap bitmap = Bitmap.createBitmap(page.getWidth() * 2, page.getHeight() * 2, Bitmap.Config.ARGB_8888);
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            
            previewImage.setImageBitmap(bitmap);
            
            page.close();
            pdfRenderer.close();
            fileDescriptor.close();
            
            // Set dialog size
            Window window = previewDialog.getWindow();
            if (window != null) {
                WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                layoutParams.copyFrom(window.getAttributes());
                layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
                layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
                window.setAttributes(layoutParams);
            }
            
            previewDialog.show();
            
        } catch (IOException e) {
            Toast.makeText(this, "Error showing preview: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
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
                // Also delete from local database
                certificateDatabase.deleteCertificate(certificate.getId());
                Toast.makeText(this, "Certificate deleted successfully", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to delete certificate: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (certificateDatabase != null) {
            certificateDatabase.close();
        }
        hideLoadingDialog();
    }
} 