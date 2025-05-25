package com.example.vohoportunitysconect.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vohoportunitysconect.R;
import com.example.vohoportunitysconect.adapters.ApplicationAdapter;
import com.example.vohoportunitysconect.models.User;
import com.example.vohoportunitysconect.models.UserType;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ApplicationsFragment extends Fragment {
    private RecyclerView applicationsRecycler;
    private DatabaseReference databaseRef;
    private FirebaseAuth auth;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseRef = FirebaseDatabase.getInstance("https://vvoohh-e2b0a-default-rtdb.firebaseio.com").getReference();
        auth = FirebaseAuth.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_applications, container, false);
        initializeViews(view);
        checkUserType();
        return view;
    }

    private void initializeViews(View view) {
        applicationsRecycler = view.findViewById(R.id.applications_recycler);
    }

    private void checkUserType() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "Please sign in to view applications", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        databaseRef.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    boolean isOrganizer = user.getUserType() == UserType.ORGANIZATION;
                    loadApplications(isOrganizer);
                } else {
                    loadApplications(false); // Default to volunteer view if user data not found
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error loading user data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                loadApplications(false); // Default to volunteer view on error
            }
        });
    }

    private void loadApplications(boolean isOrganizer) {
        // Implement the logic to load applications for the user and display them in the RecyclerView
        // This is a placeholder and should be replaced with the actual implementation
        Toast.makeText(getContext(), "Loading applications...", Toast.LENGTH_SHORT).show();
    }
} 