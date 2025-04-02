package com.example.vohoportunitysconect.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vohoportunitysconect.R;
import com.example.vohoportunitysconect.models.Certificate;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CertificateAdapter extends RecyclerView.Adapter<CertificateAdapter.CertificateViewHolder> {
    private List<Certificate> certificates = new ArrayList<>();
    private final OnCertificateClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public interface OnCertificateClickListener {
        void onCertificateClick(Certificate certificate);
    }

    public CertificateAdapter(OnCertificateClickListener listener) {
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
        holder.bind(certificate);
        holder.itemView.setOnClickListener(v -> {
            int adapterPosition = holder.getBindingAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                listener.onCertificateClick(certificates.get(adapterPosition));
            }
        });
    }

    @Override
    public int getItemCount() {
        return certificates.size();
    }

    public void setCertificates(List<Certificate> certificates) {
        this.certificates = certificates;
        notifyDataSetChanged();
    }

    class CertificateViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardView;
        private final TextView titleText;
        private final TextView skillsText;
        private final TextView hoursText;
        private final TextView dateText;

        CertificateViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            titleText = itemView.findViewById(R.id.certificate_title);
            skillsText = itemView.findViewById(R.id.certificate_skills);
            hoursText = itemView.findViewById(R.id.certificate_hours);
            dateText = itemView.findViewById(R.id.certificate_date);
        }

        void bind(Certificate certificate) {
            titleText.setText(certificate.getTitle());
            skillsText.setText(certificate.getSkills());
            hoursText.setText(String.format(Locale.getDefault(), "%d hours", certificate.getHoursCompleted()));
            dateText.setText(dateFormat.format(certificate.getIssueDate()));
        }
    }
} 