package com.example.vohoportunitysconect.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vohoportunitysconect.R;
import com.example.vohoportunitysconect.models.Activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder> {
    private List<Activity> activities = new ArrayList<>();
    private SimpleDateFormat dateFormat;

    public ActivityAdapter() {
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_activity, parent, false);
        return new ActivityViewHolder(view, dateFormat);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        Activity activity = activities.get(position);
        holder.bind(activity);
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }

    public void setActivities(List<Activity> activities) {
        if (activities != null) {
            this.activities = activities;
        } else {
            this.activities = new ArrayList<>();
        }
        notifyDataSetChanged();
    }

    static class ActivityViewHolder extends RecyclerView.ViewHolder {
        private TextView titleText;
        private TextView descriptionText;
        private TextView dateText;
        private TextView hoursText;
        private SimpleDateFormat dateFormat;

        public ActivityViewHolder(@NonNull View itemView, SimpleDateFormat dateFormat) {
            super(itemView);
            this.dateFormat = dateFormat;
            titleText = itemView.findViewById(R.id.activity_title);
            descriptionText = itemView.findViewById(R.id.activity_description);
            dateText = itemView.findViewById(R.id.activity_date);
            hoursText = itemView.findViewById(R.id.activity_hours);
        }

        public void bind(Activity activity) {
            if (activity == null) {
                return;
            }

            try {
                // Set title
                String title = activity.getType();
                if (title == null || title.isEmpty()) {
                    title = "Activity";
                }
                titleText.setText(title);

                // Set description
                String description = activity.getDescription();
                if (description == null || description.isEmpty()) {
                    description = "No description available";
                }
                descriptionText.setText(description);

                // Set date
                Date timestamp = activity.getTimestamp();
                if (timestamp != null) {
                    dateText.setText(dateFormat.format(timestamp));
                } else {
                    dateText.setText("Date not available");
                }

                // Set hours if available
                if (activity.getHours() > 0) {
                    hoursText.setVisibility(View.VISIBLE);
                    hoursText.setText(String.format(Locale.getDefault(), "%d hours", activity.getHours()));
                } else {
                    hoursText.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                Log.e("ActivityAdapter", "Error binding activity: " + e.getMessage());
            }
        }
    }
} 