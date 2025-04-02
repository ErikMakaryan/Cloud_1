package com.example.vohoportunitysconect.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vohoportunitysconect.R;
import com.example.vohoportunitysconect.adapters.FAQAdapter;
import com.example.vohoportunitysconect.databinding.FragmentHelpSupportBinding;
import com.google.android.material.button.MaterialButton;

public class HelpSupportFragment extends Fragment {
    private FragmentHelpSupportBinding binding;
    private FAQAdapter faqAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHelpSupportBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup FAQ RecyclerView
        faqAdapter = new FAQAdapter();
        binding.faqRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.faqRecyclerView.setAdapter(faqAdapter);

        // Setup contact buttons
        binding.emailSupportButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:support@vohoportunitysconect.com"));
            intent.putExtra(Intent.EXTRA_SUBJECT, "Support Request");
            try {
                startActivity(Intent.createChooser(intent, "Send email..."));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(requireContext(), "No email clients installed.", Toast.LENGTH_SHORT).show();
            }
        });

        binding.chatSupportButton.setOnClickListener(v -> {
            // TODO: Implement live chat functionality
            Toast.makeText(requireContext(), "Live chat coming soon!", Toast.LENGTH_SHORT).show();
        });

        binding.submitFeedbackButton.setOnClickListener(v -> {
            // TODO: Implement feedback submission
            Toast.makeText(requireContext(), "Feedback submission coming soon!", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 