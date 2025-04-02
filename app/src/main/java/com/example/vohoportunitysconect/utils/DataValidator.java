package com.example.vohoportunitysconect.utils;

import android.util.Patterns;
import com.example.vohoportunitysconect.models.User;
import com.example.vohoportunitysconect.models.Opportunity;
import com.example.vohoportunitysconect.models.UserActivity;
import java.util.regex.Pattern;

public class DataValidator {
    private static final String TAG = "DataValidator";
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[1-9]\\d{1,14}$");
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_BIO_LENGTH = 500;
    private static final int MAX_TITLE_LENGTH = 100;
    private static final int MAX_DESCRIPTION_LENGTH = 1000;

    public static class ValidationResult {
        public final boolean isValid;
        public final String errorMessage;

        public ValidationResult(boolean isValid, String errorMessage) {
            this.isValid = isValid;
            this.errorMessage = errorMessage;
        }
    }

    // User validation
    public static ValidationResult validateUser(User user) {
        if (user == null) {
            return new ValidationResult(false, "User object cannot be null");
        }

        if (user.getId() == null || user.getId().trim().isEmpty()) {
            return new ValidationResult(false, "User ID is required");
        }

        if (user.getName() == null || user.getName().trim().isEmpty()) {
            return new ValidationResult(false, "Name is required");
        }

        if (user.getEmail() == null || !isValidEmail(user.getEmail())) {
            return new ValidationResult(false, "Valid email is required");
        }

        if (user.getPhoneNumber() != null && !isValidPhoneNumber(user.getPhoneNumber())) {
            return new ValidationResult(false, "Invalid phone number format");
        }

        if (user.getBio() != null && user.getBio().length() > MAX_BIO_LENGTH) {
            return new ValidationResult(false, "Bio cannot exceed " + MAX_BIO_LENGTH + " characters");
        }

        return new ValidationResult(true, null);
    }

    public static ValidationResult validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            return new ValidationResult(false, "Password is required");
        }

        if (password.length() < MIN_PASSWORD_LENGTH) {
            return new ValidationResult(false, "Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
        }

        return new ValidationResult(true, null);
    }

    // Opportunity validation
    public static ValidationResult validateOpportunity(Opportunity opportunity) {
        if (opportunity == null) {
            return new ValidationResult(false, "Opportunity object cannot be null");
        }

        if (opportunity.getId() == null || opportunity.getId().trim().isEmpty()) {
            return new ValidationResult(false, "Opportunity ID is required");
        }

        if (opportunity.getTitle() == null || opportunity.getTitle().trim().isEmpty()) {
            return new ValidationResult(false, "Title is required");
        }

        if (opportunity.getTitle().length() > MAX_TITLE_LENGTH) {
            return new ValidationResult(false, "Title cannot exceed " + MAX_TITLE_LENGTH + " characters");
        }

        if (opportunity.getDescription() == null || opportunity.getDescription().trim().isEmpty()) {
            return new ValidationResult(false, "Description is required");
        }

        if (opportunity.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
            return new ValidationResult(false, "Description cannot exceed " + MAX_DESCRIPTION_LENGTH + " characters");
        }

        if (opportunity.getLocation() == null || opportunity.getLocation().trim().isEmpty()) {
            return new ValidationResult(false, "Location is required");
        }

        if (opportunity.getSkills() == null || opportunity.getSkills().trim().isEmpty()) {
            return new ValidationResult(false, "Skills are required");
        }

        if (opportunity.getOrganization() == null || opportunity.getOrganization().trim().isEmpty()) {
            return new ValidationResult(false, "Organization is required");
        }

        return new ValidationResult(true, null);
    }

    // Activity validation
    public static ValidationResult validateActivity(UserActivity activity) {
        if (activity == null) {
            return new ValidationResult(false, "Activity object cannot be null");
        }

        if (activity.getId() == null || activity.getId().trim().isEmpty()) {
            return new ValidationResult(false, "Activity ID is required");
        }

        if (activity.getUserId() == null || activity.getUserId().trim().isEmpty()) {
            return new ValidationResult(false, "User ID is required");
        }

        if (activity.getOpportunityId() == null || activity.getOpportunityId().trim().isEmpty()) {
            return new ValidationResult(false, "Opportunity ID is required");
        }

        if (activity.getHours() <= 0 || activity.getHours() > 24) {
            return new ValidationResult(false, "Hours must be between 0 and 24");
        }

        if (activity.getDescription() == null || activity.getDescription().trim().isEmpty()) {
            return new ValidationResult(false, "Description is required");
        }

        return new ValidationResult(true, null);
    }

    // Helper methods
    private static boolean isValidEmail(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private static boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber != null && PHONE_PATTERN.matcher(phoneNumber).matches();
    }

    // String validation
    public static ValidationResult validateString(String value, String fieldName, int maxLength) {
        if (value == null || value.trim().isEmpty()) {
            return new ValidationResult(false, fieldName + " is required");
        }

        if (value.length() > maxLength) {
            return new ValidationResult(false, fieldName + " cannot exceed " + maxLength + " characters");
        }

        return new ValidationResult(true, null);
    }

    // Number validation
    public static ValidationResult validateNumber(double value, String fieldName, double min, double max) {
        if (value < min || value > max) {
            return new ValidationResult(false, fieldName + " must be between " + min + " and " + max);
        }

        return new ValidationResult(true, null);
    }
} 