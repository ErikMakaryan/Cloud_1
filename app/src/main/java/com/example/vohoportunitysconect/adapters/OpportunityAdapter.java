package com.example.vohoportunitysconect.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vohoportunitysconect.R;
import com.example.vohoportunitysconect.models.Opportunity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OpportunityAdapter extends RecyclerView.Adapter<OpportunityAdapter.OpportunityViewHolder> {
    private final List<Opportunity> opportunities;
    private final OnOpportunityClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private final DatabaseReference savedRef;
    private final String userId;

    public interface OnOpportunityClickListener {
        void onOpportunityClick(Opportunity opportunity);
    }

    public OpportunityAdapter(List<Opportunity> opportunities, OnOpportunityClickListener listener) {
        this.opportunities = opportunities;
        this.listener = listener;
        this.userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.savedRef = FirebaseDatabase.getInstance().getReference()
            .child("users")
            .child(userId)
            .child("saved_opportunities");
    }

    public void updateOpportunities(List<Opportunity> newOpportunities) {
        if (newOpportunities != null) {
            this.opportunities.clear();
            this.opportunities.addAll(newOpportunities);
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public OpportunityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_opportunity, parent, false);
        return new OpportunityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OpportunityViewHolder holder, int position) {
        Opportunity opportunity = opportunities.get(position);
        if (opportunity != null) {
            holder.bind(opportunity);
        }
    }

    @Override
    public int getItemCount() {
        return opportunities.size();
    }

    class OpportunityViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleText;
        private final TextView organizationText;
        private final TextView locationText;
        private final TextView categoryText;
        private final TextView applicationStatusText;
        private final ImageButton saveButton;

        OpportunityViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.opportunity_title);
            organizationText = itemView.findViewById(R.id.opportunity_organization);
            locationText = itemView.findViewById(R.id.opportunity_location);
            categoryText = itemView.findViewById(R.id.opportunity_category);
            applicationStatusText = itemView.findViewById(R.id.application_status);
            saveButton = itemView.findViewById(R.id.save_button);

            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onOpportunityClick(opportunities.get(position));
                }
            });

            saveButton.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Opportunity opportunity = opportunities.get(position);
                    toggleSaveOpportunity(opportunity);
                }
            });
        }

        void bind(Opportunity opportunity) {
            if (opportunity == null) return;

            // Set text with null checks
            titleText.setText(opportunity.getTitle() != null ? opportunity.getTitle() : "");
            organizationText.setText(opportunity.getOrganization() != null ? opportunity.getOrganization() : "");
            locationText.setText(opportunity.getLocation() != null ? opportunity.getLocation() : "");
            categoryText.setText(opportunity.getCategory() != null ? opportunity.getCategory() : "");

            // Set application status
            String status = opportunity.getApplicationStatus();
            if (status != null && !status.isEmpty()) {
                applicationStatusText.setVisibility(View.VISIBLE);
                applicationStatusText.setText(status.toUpperCase());
                // Set color based on status
                int colorRes;
                switch (status.toLowerCase()) {
                    case "pending":
                        colorRes = R.color.orange;
                        break;
                    case "accepted":
                        colorRes = R.color.green;
                        break;
                    case "rejected":
                        colorRes = R.color.red;
                        break;
                    default:
                        colorRes = R.color.primary;
                }
                applicationStatusText.setTextColor(itemView.getContext().getResources().getColor(colorRes, null));
            } else {
                applicationStatusText.setVisibility(View.GONE);
            }

            // Check if opportunity is saved
            savedRef.child(opportunity.getId()).get().addOnSuccessListener(snapshot -> {
                boolean isSaved = snapshot.exists();
                saveButton.setImageResource(isSaved ? R.drawable.ic_bookmark_filled : R.drawable.ic_bookmark_border);
                if (isSaved) {
                    saveButton.setColorFilter(itemView.getContext().getResources().getColor(R.color.orange, null));
                } else {
                    saveButton.clearColorFilter();
                }
            });
        }

        private void toggleSaveOpportunity(Opportunity opportunity) {
            savedRef.child(opportunity.getId()).get().addOnSuccessListener(snapshot -> {
                if (snapshot.exists()) {
                    // Remove from saved
                    savedRef.child(opportunity.getId()).removeValue()
                        .addOnSuccessListener(aVoid -> {
                            saveButton.setImageResource(R.drawable.ic_bookmark_border);
                            saveButton.clearColorFilter();
                            Toast.makeText(itemView.getContext(), "Removed from saved", Toast.LENGTH_SHORT).show();
                        });
                } else {
                    // Add to saved
                    Map<String, Object> savedData = new HashMap<>();
                    savedData.put("id", opportunity.getId());
                    savedData.put("title", opportunity.getTitle());
                    savedData.put("organization", opportunity.getOrganization());
                    savedData.put("location", opportunity.getLocation());
                    savedData.put("category", opportunity.getCategory());
                    savedData.put("savedAt", System.currentTimeMillis());

                    savedRef.child(opportunity.getId()).setValue(savedData)
                        .addOnSuccessListener(aVoid -> {
                            saveButton.setImageResource(R.drawable.ic_bookmark_filled);
                            saveButton.setColorFilter(itemView.getContext().getResources().getColor(R.color.orange, null));
                            Toast.makeText(itemView.getContext(), "Saved opportunity", Toast.LENGTH_SHORT).show();
                        });
                }
            });
        }
    }
} 