package com.example.vohoportunitysconect.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.vohoportunitysconect.R;
import com.example.vohoportunitysconect.models.Application;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ApplicationAdapter extends RecyclerView.Adapter<ApplicationAdapter.ApplicationViewHolder> {
    private List<Application> applications;
    private OnApplicationClickListener listener;
    private SimpleDateFormat dateFormat;
    private final DatabaseReference databaseRef;
    private final String userId;

    public interface OnApplicationClickListener {
        void onApplicationClick(Application application);
    }

    public ApplicationAdapter(List<Application> applications, OnApplicationClickListener listener) {
        this.applications = applications;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        this.userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.databaseRef = FirebaseDatabase.getInstance("https://vvoohh-e2b0a-default-rtdb.firebaseio.com").getReference();
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
        private MaterialButton cancelButton;

        public ApplicationViewHolder(@NonNull View itemView) {
            super(itemView);
            organizationImage = itemView.findViewById(R.id.organization_image);
            titleText = itemView.findViewById(R.id.title_text);
            organizationText = itemView.findViewById(R.id.organization_text);
            appliedDateText = itemView.findViewById(R.id.applied_date_text);
            statusChip = itemView.findViewById(R.id.status_chip);
            cancelButton = itemView.findViewById(R.id.cancel_button);

            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onApplicationClick(applications.get(position));
                }
            });

            cancelButton.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Application application = applications.get(position);
                    cancelApplication(application);
                }
            });
        }

        private void cancelApplication(Application application) {
            databaseRef.child("applications")
                .child(userId)
                .child(application.getOpportunityId())
                .removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(itemView.getContext(), "Application cancelled", Toast.LENGTH_SHORT).show();
                    // Remove from local list
                    int position = getBindingAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        applications.remove(position);
                        notifyItemRemoved(position);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(itemView.getContext(), "Error cancelling application: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
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
                    statusChip.setText(R.string.pending);
                    statusChip.setChipBackgroundColorResource(R.color.status_pending);
                    cancelButton.setVisibility(View.VISIBLE);
                    cancelButton.setText(R.string.cancel);
                    break;
                case ACCEPTED:
                    statusChip.setText(R.string.accepted);
                    statusChip.setChipBackgroundColorResource(R.color.status_accepted);
                    cancelButton.setVisibility(View.GONE);
                    break;
                case REJECTED:
                    statusChip.setText(R.string.rejected);
                    statusChip.setChipBackgroundColorResource(R.color.status_rejected);
                    cancelButton.setVisibility(View.GONE);
                    break;
            }
        }
    }
} 