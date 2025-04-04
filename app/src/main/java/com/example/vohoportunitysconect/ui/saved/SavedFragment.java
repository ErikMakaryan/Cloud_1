package com.example.vohoportunitysconect.ui.saved;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vohoportunitysconect.R;
import com.example.vohoportunitysconect.database.DatabaseManager;
import com.example.vohoportunitysconect.models.Opportunity;
import com.example.vohoportunitysconect.ui.opportunities.OpportunityAdapter;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class SavedFragment extends Fragment {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyView;
    private OpportunityAdapter opportunityAdapter;
    private DatabaseManager databaseManager;
    private FirebaseAuth mAuth;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseManager = DatabaseManager.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_saved, container, false);
        
        recyclerView = view.findViewById(R.id.saved_recycler_view);
        progressBar = view.findViewById(R.id.progress_bar);
        emptyView = view.findViewById(R.id.empty_view);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        opportunityAdapter = new OpportunityAdapter(new ArrayList<>(), opportunity -> {
            // Handle opportunity click
        });
        recyclerView.setAdapter(opportunityAdapter);
        
        loadSavedOpportunities();
        
        return view;
    }

    private void loadSavedOpportunities() {
        if (mAuth.getCurrentUser() == null) {
            showEmptyView();
            return;
        }

        showLoading();
        databaseManager.getUserSavedOpportunities(mAuth.getCurrentUser().getUid(), new DatabaseManager.DatabaseCallback<List<Opportunity>>() {
            @Override
            public void onSuccess(List<Opportunity> opportunities) {
                hideLoading();
                if (opportunities.isEmpty()) {
                    showEmptyView();
                } else {
                    hideEmptyView();
                    opportunityAdapter.updateOpportunities(opportunities);
                }
            }

            @Override
            public void onError(Exception e) {
                hideLoading();
                showEmptyView();
            }
        });
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
    }

    private void showEmptyView() {
        emptyView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    private void hideEmptyView() {
        emptyView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }
} 