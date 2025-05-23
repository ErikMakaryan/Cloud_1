package com.example.vohoportunitysconect.adapters;

import android.annotation.SuppressLint;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.vohoportunitysconect.R;
import com.example.vohoportunitysconect.models.Certificate;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CertificateAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_CERTIFICATE = 0;
    private static final int TYPE_EMPTY = 1;

    private List<Certificate> certificates;
    private final OnCertificateClickListener listener;

    public interface OnCertificateClickListener {
        void onCertificateClick(Certificate certificate);
        void onDeleteClick(Certificate certificate);
    }

    public CertificateAdapter(List<Certificate> certificates, OnCertificateClickListener listener) {
        this.certificates = certificates;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return certificates.isEmpty() ? TYPE_EMPTY : TYPE_CERTIFICATE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_EMPTY) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_empty_certificate, parent, false);
            return new EmptyViewHolder(view);
        }
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_certificate, parent, false);
        return new CertificateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof CertificateViewHolder) {
            Certificate certificate = certificates.get(position);
            CertificateViewHolder viewHolder = (CertificateViewHolder) holder;
            
            viewHolder.nameText.setText(certificate.getName());
            
            // Format the date using Instant
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            String formattedDate = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                formattedDate = dateFormat.format(Date.from(Instant.ofEpochMilli(certificate.getUploadDate())));
            }
            viewHolder.dateText.setText(formattedDate);

            // Set click listeners
            viewHolder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCertificateClick(certificate);
                }
            });

            viewHolder.deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(certificate);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return certificates.isEmpty() ? 1 : certificates.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateCertificates(List<Certificate> newCertificates) {
        this.certificates = newCertificates;
        notifyDataSetChanged();
    }

    static class CertificateViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        TextView dateText;
        ImageButton deleteButton;

        CertificateViewHolder(View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.certificate_name);
            dateText = itemView.findViewById(R.id.certificate_date);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }

    static class EmptyViewHolder extends RecyclerView.ViewHolder {
        EmptyViewHolder(View itemView) {
            super(itemView);
        }
    }
} 