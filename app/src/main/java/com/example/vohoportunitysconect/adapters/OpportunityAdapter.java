package com.example.vohoportunitysconect.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vohoportunitysconect.R;
import com.example.vohoportunitysconect.models.Opportunity;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OpportunityAdapter extends RecyclerView.Adapter<OpportunityAdapter.OpportunityViewHolder> {
    private List<Opportunity> opportunities = new ArrayList<>();
    private OnOpportunityClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public interface OnOpportunityClickListener {
        void onOpportunityClick(Opportunity opportunity);
    }

    public OpportunityAdapter(List<Opportunity> opportunities, OnOpportunityClickListener listener) {
        this.opportunities = opportunities;
        this.listener = listener;
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
        holder.bind(opportunity);
    }

    @Override
    public int getItemCount() {
        return opportunities.size();
    }

    public void setOpportunities(List<Opportunity> opportunities) {
        this.opportunities.clear();
        this.opportunities.addAll(opportunities);
        notifyDataSetChanged();
    }

    class OpportunityViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleText;
        private final TextView organizationText;
        private final TextView descriptionText;
        private final Chip locationChip;
        private final Chip difficultyChip;
        private final Chip deadlineChip;
        private final Chip categoryChip;
        private final Chip skillsChip;

        OpportunityViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.opportunity_title);
            organizationText = itemView.findViewById(R.id.organization_name);
            descriptionText = itemView.findViewById(R.id.opportunity_description);
            locationChip = itemView.findViewById(R.id.location_chip);
            difficultyChip = itemView.findViewById(R.id.difficulty_chip);
            deadlineChip = itemView.findViewById(R.id.deadline_chip);
            categoryChip = itemView.findViewById(R.id.category_chip);
            skillsChip = itemView.findViewById(R.id.skills_chip);

            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onOpportunityClick(opportunities.get(position));
                }
            });
        }

        void bind(Opportunity opportunity) {
            titleText.setText(opportunity.getTitle());
            organizationText.setText(opportunity.getOrganizationName());
            descriptionText.setText(opportunity.getDescription());
            locationChip.setText(opportunity.getLocation());
            difficultyChip.setText(opportunity.getDifficulty().toString());
            deadlineChip.setText(dateFormat.format(opportunity.getDeadline()));
            categoryChip.setText(opportunity.getCategory());
            skillsChip.setText(opportunity.getSkills());
        }
    }
} 