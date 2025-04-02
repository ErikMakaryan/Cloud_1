package com.example.vohoportunitysconect.utils;

import android.content.Context;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.StorageException;

public class FirebaseErrorHandler {
    private static final String TAG = "FirebaseErrorHandler";

    public static void handleAuthError(Context context, Exception e) {
        String errorMessage;
        String recoveryAction = "Please try again.";

        if (e instanceof FirebaseAuthException) {
            FirebaseAuthException authException = (FirebaseAuthException) e;
            switch (authException.getErrorCode()) {
                case "ERROR_INVALID_EMAIL":
                    errorMessage = "Invalid email address.";
                    recoveryAction = "Please enter a valid email address.";
                    break;
                case "ERROR_WRONG_PASSWORD":
                    errorMessage = "Incorrect password.";
                    recoveryAction = "Please check your password and try again.";
                    break;
                case "ERROR_USER_NOT_FOUND":
                    errorMessage = "Account not found.";
                    recoveryAction = "Please sign up first.";
                    break;
                case "ERROR_EMAIL_ALREADY_IN_USE":
                    errorMessage = "Email already registered.";
                    recoveryAction = "Please use a different email or sign in.";
                    break;
                case "ERROR_WEAK_PASSWORD":
                    errorMessage = "Password is too weak.";
                    recoveryAction = "Please use a stronger password (at least 6 characters).";
                    break;
                case "ERROR_NETWORK_REQUEST_FAILED":
                    errorMessage = "Network error.";
                    recoveryAction = "Please check your internet connection.";
                    break;
                default:
                    errorMessage = "Authentication error occurred.";
                    break;
            }
        } else {
            errorMessage = "An unexpected error occurred.";
        }

        showError(context, errorMessage, recoveryAction);
    }

    public static void handleFirestoreError(Context context, Exception e) {
        String errorMessage;
        String recoveryAction = "Please try again.";

        if (e instanceof FirebaseFirestoreException) {
            FirebaseFirestoreException firestoreException = (FirebaseFirestoreException) e;
            switch (firestoreException.getCode()) {
                case PERMISSION_DENIED:
                    errorMessage = "Access denied.";
                    recoveryAction = "Please check your permissions.";
                    break;
                case NOT_FOUND:
                    errorMessage = "Document not found.";
                    recoveryAction = "The requested data is not available.";
                    break;
                case UNAVAILABLE:
                    errorMessage = "Service unavailable.";
                    recoveryAction = "Please try again later.";
                    break;
                case CANCELLED:
                    errorMessage = "Operation cancelled.";
                    recoveryAction = "Please try again.";
                    break;
                case DEADLINE_EXCEEDED:
                    errorMessage = "Request timed out.";
                    recoveryAction = "Please check your connection and try again.";
                    break;
                case RESOURCE_EXHAUSTED:
                    errorMessage = "Too many requests.";
                    recoveryAction = "Please wait a moment and try again.";
                    break;
                default:
                    errorMessage = "Database error occurred.";
                    break;
            }
        } else {
            errorMessage = "An unexpected error occurred.";
        }

        showError(context, errorMessage, recoveryAction);
    }

    public static void handleStorageError(Context context, Exception e) {
        String errorMessage;
        String recoveryAction = "Please try again.";

        if (e instanceof StorageException) {
            StorageException storageException = (StorageException) e;
            switch (storageException.getErrorCode()) {
                case StorageException.ERROR_OBJECT_NOT_FOUND:
                    errorMessage = "File not found.";
                    recoveryAction = "The requested file is not available.";
                    break;
                case StorageException.ERROR_BUCKET_NOT_FOUND:
                    errorMessage = "Storage bucket not found.";
                    recoveryAction = "Please contact support.";
                    break;
                case StorageException.ERROR_PROJECT_NOT_FOUND:
                    errorMessage = "Project not found.";
                    recoveryAction = "Please contact support.";
                    break;
                case StorageException.ERROR_QUOTA_EXCEEDED:
                    errorMessage = "Storage quota exceeded.";
                    recoveryAction = "Please free up some space and try again.";
                    break;
                case StorageException.ERROR_NOT_AUTHENTICATED:
                    errorMessage = "Not authenticated.";
                    recoveryAction = "Please sign in again.";
                    break;
                case StorageException.ERROR_NOT_AUTHORIZED:
                    errorMessage = "Not authorized.";
                    recoveryAction = "Please check your permissions.";
                    break;
                case StorageException.ERROR_RETRY_LIMIT_EXCEEDED:
                    errorMessage = "Upload failed.";
                    recoveryAction = "Please check your connection and try again.";
                    break;
                default:
                    errorMessage = "Storage error occurred.";
                    break;
            }
        } else {
            errorMessage = "An unexpected error occurred.";
        }

        showError(context, errorMessage, recoveryAction);
    }

    private static void showError(Context context, String errorMessage, String recoveryAction) {
        String fullMessage = errorMessage + " " + recoveryAction;
        Toast.makeText(context, fullMessage, Toast.LENGTH_LONG).show();
    }
} 