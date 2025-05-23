package com.example.vohoportunitysconect.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.vohoportunitysconect.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ApplicationFragment extends Fragment {
    private FirebaseAuth mAuth;
    private DatabaseReference databaseRef;
    private Button submitButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_application, container, false);
        submitButton = view.findViewById(R.id.submit_button);
        submitButton.setOnClickListener(v -> submitApplication());
        return view;
    }

    private void submitApplication() {
        String userId = mAuth.getCurrentUser().getUid();
        String opportunityId = getArguments().getString("opportunityId");
        
        // Get current date
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());

        Map<String, Object> application = new HashMap<>();
        application.put("userId", userId);
        application.put("opportunityId", opportunityId);
        application.put("status", "pending");
        application.put("applicationDate", currentDate);
        application.put("timestamp", ServerValue.TIMESTAMP);

        databaseRef.child("applications").push().setValue(application)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(getContext(), "Application submitted successfully", Toast.LENGTH_SHORT).show();
                if (getActivity() != null) {
                    getActivity().finish();
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Error submitting application: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
} 