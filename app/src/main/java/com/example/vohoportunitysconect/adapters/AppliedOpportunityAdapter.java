package com.example.vohoportunitysconect.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.vohoportunitysconect.R;
import com.example.vohoportunitysconect.models.Application;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AppliedOpportunityAdapter extends RecyclerView.Adapter<AppliedOpportunityAdapter.ViewHolder> {
    private List<Application> applications;
    private OnItemClickListener listener;
    private SimpleDateFormat dateFormat;

    public interface OnItemClickListener {
        void onItemClick(Application application);
    }

    public AppliedOpportunityAdapter(List<Application> applications, OnItemClickListener listener) {
        this.applications = applications;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_applied_opportunity, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Application application = applications.get(position);
        holder.titleText.setText(application.getTitle());
        holder.organizationText.setText(application.getOrganization());
        holder.statusText.setText(application.getStatus());
        holder.dateText.setText("Applied on " + dateFormat.format(new Date(application.getAppliedAt())));

        // Set status color
        int statusColor;
        switch (application.getStatus().toUpperCase()) {
            case "ACCEPTED":
                statusColor = holder.itemView.getContext().getColor(R.color.status_accepted);
                break;
            case "REJECTED":
                statusColor = holder.itemView.getContext().getColor(R.color.status_rejected);
                break;
            default:
                statusColor = holder.itemView.getContext().getColor(R.color.status_pending);
                break;
        }
        holder.statusText.setTextColor(statusColor);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(application);
            }
        });
    }

    @Override
    public int getItemCount() {
        return applications.size();
    }

    public void updateApplications(List<Application> newApplications) {
        this.applications = newApplications;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;
        TextView organizationText;
        TextView statusText;
        TextView dateText;

        ViewHolder(View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.title_text);
            organizationText = itemView.findViewById(R.id.organization_text);
            statusText = itemView.findViewById(R.id.status_text);
            dateText = itemView.findViewById(R.id.date_text);
        }
    }
} 