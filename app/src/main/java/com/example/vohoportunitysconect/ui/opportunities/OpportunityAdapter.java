package com.example.vohoportunitysconect.ui.opportunities;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vohoportunitysconect.R;
import com.example.vohoportunitysconect.models.Opportunity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class OpportunityAdapter extends RecyclerView.Adapter<OpportunityAdapter.OpportunityViewHolder> {
    private List<Opportunity> opportunities;
    private final OnOpportunityClickListener listener;
    private final DatabaseReference databaseRef;
    private final String userId;

    public interface OnOpportunityClickListener {
        void onOpportunityClick(Opportunity opportunity);
    }

    public OpportunityAdapter(List<Opportunity> opportunities, OnOpportunityClickListener listener) {
        this.opportunities = opportunities;
        this.listener = listener;
        this.databaseRef = FirebaseDatabase.getInstance("https://vvoohh-e2b0a-default-rtdb.firebaseio.com").getReference();
        this.userId = FirebaseAuth.getInstance().getCurrentUser() != null ? 
            FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
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
        holder.bind(opportunity, listener);

        // Check if opportunity is saved
        if (userId != null) {
            databaseRef.child("users").child(userId).child("saved_opportunities")
                .child(opportunity.getId()).get().addOnSuccessListener(snapshot -> {
                    boolean isSaved = snapshot.exists();
                    holder.saveButton.setImageResource(isSaved ? 
                        R.drawable.ic_bookmark : R.drawable.ic_bookmark_border);
                });
        }

        // Set up save button click listener
        holder.saveButton.setOnClickListener(v -> {
            if (userId != null) {
                DatabaseReference savedRef = databaseRef.child("users").child(userId)
                    .child("saved_opportunities").child(opportunity.getId());
                
                savedRef.get().addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        // Remove from saved
                        savedRef.removeValue().addOnSuccessListener(aVoid -> {
                            holder.saveButton.setImageResource(R.drawable.ic_bookmark_border);
                        });
                    } else {
                        // Add to saved
                        savedRef.setValue(opportunity).addOnSuccessListener(aVoid -> {
                            holder.saveButton.setImageResource(R.drawable.ic_bookmark);
                        });
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return opportunities.size();
    }

    public void updateOpportunities(List<Opportunity> newOpportunities) {
        this.opportunities = newOpportunities;
        notifyDataSetChanged();
    }

    static class OpportunityViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleText;
        private final TextView organizationText;
        private final TextView locationText;
        private final TextView categoryText;
        private final ImageButton saveButton;

        public OpportunityViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.opportunity_title);
            organizationText = itemView.findViewById(R.id.opportunity_organization);
            locationText = itemView.findViewById(R.id.opportunity_location);
            categoryText = itemView.findViewById(R.id.opportunity_category);
            saveButton = itemView.findViewById(R.id.save_button);
        }

        public void bind(Opportunity opportunity, OnOpportunityClickListener listener) {
            titleText.setText(opportunity.getTitle());
            organizationText.setText(opportunity.getOrganization());
            locationText.setText(opportunity.getLocation());
            categoryText.setText(opportunity.getCategory());

            itemView.setOnClickListener(v -> listener.onOpportunityClick(opportunity));
        }
    }
} 