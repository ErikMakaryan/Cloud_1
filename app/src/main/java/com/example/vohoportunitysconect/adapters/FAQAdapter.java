package com.example.vohoportunitysconect.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vohoportunitysconect.R;

import java.util.ArrayList;
import java.util.List;

public class FAQAdapter extends RecyclerView.Adapter<FAQAdapter.FAQViewHolder> {
    private List<FAQItem> faqItems = new ArrayList<>();

    public FAQAdapter() {
        // Add FAQ items
        faqItems.add(new FAQItem(
            "How do I find volunteer opportunities?",
            "You can browse available opportunities in the Opportunities tab. Use filters to find opportunities that match your interests, location, and schedule."
        ));
        faqItems.add(new FAQItem(
            "How do I track my volunteer hours?",
            "After completing a volunteer opportunity, you can log your hours in the app. Go to the opportunity details and tap 'Log Hours'. Enter the number of hours you volunteered and submit."
        ));
    }

    @NonNull
    @Override
    public FAQViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_faq, parent, false);
        return new FAQViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FAQViewHolder holder, int position) {
        FAQItem item = faqItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return faqItems.size();
    }

    static class FAQViewHolder extends RecyclerView.ViewHolder {
        private TextView questionText;
        private TextView answerText;
        private ImageButton expandButton;
        private boolean isExpanded = false;

        public FAQViewHolder(@NonNull View itemView) {
            super(itemView);
            questionText = itemView.findViewById(R.id.question_text);
            answerText = itemView.findViewById(R.id.answer_text);
            expandButton = itemView.findViewById(R.id.expand_button);

            expandButton.setOnClickListener(v -> {
                isExpanded = !isExpanded;
                answerText.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
                expandButton.setRotation(isExpanded ? 180 : 0);
            });
        }

        public void bind(FAQItem item) {
            questionText.setText(item.getQuestion());
            answerText.setText(item.getAnswer());
        }
    }

    static class FAQItem {
        private String question;
        private String answer;

        public FAQItem(String question, String answer) {
            this.question = question;
            this.answer = answer;
        }

        public String getQuestion() {
            return question;
        }

        public String getAnswer() {
            return answer;
        }
    }
} 