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

import java.util.List;

public class CertificateAdapter extends RecyclerView.Adapter<CertificateAdapter.CertificateViewHolder> {
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

    @NonNull
    @Override
    public CertificateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_certificate, parent, false);
        return new CertificateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CertificateViewHolder holder, int position) {
        Certificate certificate = certificates.get(position);
        holder.bind(certificate, listener);
    }

    @Override
    public int getItemCount() {
        return certificates.size();
    }

    public void updateCertificates(List<Certificate> newCertificates) {
        this.certificates = newCertificates;
        notifyDataSetChanged();
    }

    static class CertificateViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameText;
        private final ImageButton deleteButton;

        public CertificateViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.certificate_name);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }

        public void bind(Certificate certificate, OnCertificateClickListener listener) {
            nameText.setText(certificate.getName());

            itemView.setOnClickListener(v -> listener.onCertificateClick(certificate));
            deleteButton.setOnClickListener(v -> listener.onDeleteClick(certificate));
        }
    }
} 