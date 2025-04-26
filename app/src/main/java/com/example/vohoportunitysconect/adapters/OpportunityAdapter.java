package com.example.vohoportunitysconect.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
    private final List<Opportunity> opportunities;
    private final OnOpportunityClickListener listener;
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
        private final TextView locationText;
        private final TextView categoryText;
        private final ImageView urgentIndicator;
        private final ImageView remoteIndicator;
        private final ImageView featuredIndicator;
        private final Chip difficultyChip;
        private final Chip deadlineChip;
        private final Chip skillsChip;

        OpportunityViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.opportunity_title);
            organizationText = itemView.findViewById(R.id.opportunity_organization);
            locationText = itemView.findViewById(R.id.opportunity_location);
            categoryText = itemView.findViewById(R.id.opportunity_category);
            urgentIndicator = itemView.findViewById(R.id.urgent_indicator);
            remoteIndicator = itemView.findViewById(R.id.remote_indicator);
            featuredIndicator = itemView.findViewById(R.id.featured_indicator);
            difficultyChip = itemView.findViewById(R.id.difficulty_chip);
            deadlineChip = itemView.findViewById(R.id.deadline_chip);
            skillsChip = itemView.findViewById(R.id.skills_chip);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onOpportunityClick(opportunities.get(position));
                }
            });
        }

        void bind(Opportunity opportunity) {
            titleText.setText(opportunity.getTitle());
            organizationText.setText(opportunity.getOrganization());
            locationText.setText(opportunity.getLocation());
            categoryText.setText(opportunity.getCategory());

            // Set indicators visibility
            urgentIndicator.setVisibility(opportunity.isUrgent() ? View.VISIBLE : View.GONE);
            remoteIndicator.setVisibility(opportunity.isRemote() ? View.VISIBLE : View.GONE);
            featuredIndicator.setVisibility(opportunity.isFeatured() ? View.VISIBLE : View.GONE);

            difficultyChip.setText(opportunity.getDifficulty().toString());
            deadlineChip.setText(dateFormat.format(opportunity.getDeadline()));
            skillsChip.setText(opportunity.getSkills());
        }
    }
} 