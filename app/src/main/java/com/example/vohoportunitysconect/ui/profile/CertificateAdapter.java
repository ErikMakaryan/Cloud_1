package com.example.vohoportunitysconect.ui.profile;

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
import java.util.ArrayList;
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
        this.certificates = certificates != null ? certificates : new ArrayList<>();
        this.listener = listener;
    }

    public void updateCertificates(List<Certificate> newCertificates) {
        this.certificates = newCertificates != null ? newCertificates : new ArrayList<>();
        notifyDataSetChanged();
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
            viewHolder.bind(certificate);

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

    static class CertificateViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameText;
        private final TextView dateText;
        private final ImageButton deleteButton;

        public CertificateViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.certificate_name);
            dateText = itemView.findViewById(R.id.certificate_date);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }

        public void bind(Certificate certificate) {
            nameText.setText(certificate.getName());
            
            // Format the upload date
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            String formattedDate = dateFormat.format(new Date(certificate.getUploadDate()));
            dateText.setText(formattedDate);
        }
    }

    static class EmptyViewHolder extends RecyclerView.ViewHolder {
        public EmptyViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
} 