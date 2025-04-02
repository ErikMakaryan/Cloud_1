package com.example.vohoportunitysconect.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.vohoportunitysconect.R;
import com.example.vohoportunitysconect.models.Application;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ApplicationAdapter extends RecyclerView.Adapter<ApplicationAdapter.ApplicationViewHolder> {
    private List<Application> applications;
    private OnApplicationClickListener listener;
    private SimpleDateFormat dateFormat;

    public interface OnApplicationClickListener {
        void onApplicationClick(Application application);
    }

    public ApplicationAdapter(List<Application> applications, OnApplicationClickListener listener) {
        this.applications = applications;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public ApplicationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_application, parent, false);
        return new ApplicationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ApplicationViewHolder holder, int position) {
        Application application = applications.get(position);
        holder.bind(application);
    }

    @Override
    public int getItemCount() {
        return applications.size();
    }

    public void updateApplications(List<Application> newApplications) {
        this.applications = newApplications;
        notifyDataSetChanged();
    }

    class ApplicationViewHolder extends RecyclerView.ViewHolder {
        private ImageView organizationImage;
        private TextView titleText;
        private TextView organizationText;
        private TextView appliedDateText;
        private Chip statusChip;

        public ApplicationViewHolder(@NonNull View itemView) {
            super(itemView);
            organizationImage = itemView.findViewById(R.id.organization_image);
            titleText = itemView.findViewById(R.id.title_text);
            organizationText = itemView.findViewById(R.id.organization_text);
            appliedDateText = itemView.findViewById(R.id.applied_date_text);
            statusChip = itemView.findViewById(R.id.status_chip);

            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onApplicationClick(applications.get(position));
                }
            });
        }

        public void bind(Application application) {
            // Load organization image
            if (application.getOrganizationImageUrl() != null && !application.getOrganizationImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(application.getOrganizationImageUrl())
                        .placeholder(R.drawable.placeholder_organization)
                        .error(R.drawable.placeholder_organization)
                        .into(organizationImage);
            }

            // Set text fields
            titleText.setText(application.getOpportunityTitle());
            organizationText.setText(application.getOrganizationName());
            appliedDateText.setText("Applied: " + dateFormat.format(application.getAppliedDate()));

            // Set status chip
            switch (application.getStatusEnum()) {
                case PENDING:
                    statusChip.setText("Pending");
                    statusChip.setChipBackgroundColorResource(R.color.status_pending);
                    break;
                case ACCEPTED:
                    statusChip.setText("Accepted");
                    statusChip.setChipBackgroundColorResource(R.color.status_accepted);
                    break;
                case REJECTED:
                    statusChip.setText("Rejected");
                    statusChip.setChipBackgroundColorResource(R.color.status_rejected);
                    break;
            }
        }
    }
} 