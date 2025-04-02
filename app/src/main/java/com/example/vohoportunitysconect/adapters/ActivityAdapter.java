package com.example.vohoportunitysconect.adapters;

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
        this.activities = activities;
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
            titleText.setText(activity.getType());
            descriptionText.setText(activity.getDescription());
            
            dateText.setText(dateFormat.format(new Date(activity.getCreatedAt())));
            
            // Since Activity model doesn't have hours, we'll hide the hours text
            hoursText.setVisibility(View.GONE);
        }
    }
} 