package com.example.vohoportunitysconect.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.vohoportunitysconect.R;
import com.example.vohoportunitysconect.models.Application;
import java.util.List;

public class ApplicationAdapter extends RecyclerView.Adapter<ApplicationAdapter.ApplicationViewHolder> {
    private List<Application> applications;
    private OnApplicationClickListener listener;
    private boolean isOrganizer;

    public interface OnApplicationClickListener {
        void onAcceptClick(Application application);
        void onRejectClick(Application application);
        void onCancelClick(Application application);
    }

    public ApplicationAdapter(List<Application> applications, OnApplicationClickListener listener, boolean isOrganizer) {
        this.applications = applications;
        this.listener = listener;
        this.isOrganizer = isOrganizer;
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
        private TextView volunteerName;
        private TextView volunteerEmail;
        private TextView applicationStatus;
        private TextView applicationMessage;
        private View actionButtons;
        private View acceptButton;
        private View rejectButton;

        public ApplicationViewHolder(@NonNull View itemView) {
            super(itemView);
            volunteerName = itemView.findViewById(R.id.volunteer_name);
            volunteerEmail = itemView.findViewById(R.id.volunteer_email);
            applicationStatus = itemView.findViewById(R.id.application_status);
            applicationMessage = itemView.findViewById(R.id.application_message);
            actionButtons = itemView.findViewById(R.id.action_buttons);
            acceptButton = itemView.findViewById(R.id.accept_button);
            rejectButton = itemView.findViewById(R.id.reject_button);
        }

        public void bind(Application application) {
            if (isOrganizer) {
                volunteerName.setText(application.getVolunteerName());
                volunteerEmail.setText(application.getVolunteerEmail());
                actionButtons.setVisibility(View.VISIBLE);
            } else {
                volunteerName.setText(application.getOpportunityTitle());
                volunteerEmail.setText(application.getOrganizationName());
                actionButtons.setVisibility(View.GONE);
            }

            applicationStatus.setText(application.getStatus());
            applicationMessage.setText(application.getMessage());

            // Set status color based on application status
            switch (application.getStatus().toLowerCase()) {
                case "pending":
                    applicationStatus.setTextColor(itemView.getContext().getColor(R.color.status_pending));
                    break;
                case "accepted":
                    applicationStatus.setTextColor(itemView.getContext().getColor(R.color.status_accepted));
                    break;
                case "rejected":
                    applicationStatus.setTextColor(itemView.getContext().getColor(R.color.status_rejected));
                    break;
            }

            // Set up click listeners
            if (isOrganizer) {
                acceptButton.setOnClickListener(v -> listener.onAcceptClick(application));
                rejectButton.setOnClickListener(v -> listener.onRejectClick(application));
            }
        }
    }
} 