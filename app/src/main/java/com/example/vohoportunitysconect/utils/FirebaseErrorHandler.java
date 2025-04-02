package com.example.vohoportunitysconect.utils;

import android.content.Context;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.database.DatabaseError;
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

    public static void handleDatabaseError(Context context, Exception e) {
        String errorMessage;
        String recoveryAction = "Please try again.";

        if (e instanceof DatabaseError) {
            DatabaseError databaseError = (DatabaseError) e;
            switch (databaseError.getCode()) {
                case DatabaseError.PERMISSION_DENIED:
                    errorMessage = "Access denied.";
                    recoveryAction = "Please check your permissions.";
                    break;
                case DatabaseError.DISCONNECTED:
                    errorMessage = "Disconnected from database.";
                    recoveryAction = "Please check your internet connection.";
                    break;
                case DatabaseError.EXPIRED_TOKEN:
                    errorMessage = "Authentication expired.";
                    recoveryAction = "Please sign in again.";
                    break;
                case DatabaseError.INVALID_TOKEN:
                    errorMessage = "Invalid authentication.";
                    recoveryAction = "Please sign in again.";
                    break;
                case DatabaseError.MAX_RETRIES:
                    errorMessage = "Maximum retries exceeded.";
                    recoveryAction = "Please try again later.";
                    break;
                case DatabaseError.NETWORK_ERROR:
                    errorMessage = "Network error.";
                    recoveryAction = "Please check your internet connection.";
                    break;
                case DatabaseError.OPERATION_FAILED:
                    errorMessage = "Operation failed.";
                    recoveryAction = "Please try again.";
                    break;
                case DatabaseError.OVERRIDDEN_BY_SET:
                    errorMessage = "Data was overridden.";
                    recoveryAction = "Your changes may not have been saved.";
                    break;
                case DatabaseError.UNAVAILABLE:
                    errorMessage = "Service unavailable.";
                    recoveryAction = "Please try again later.";
                    break;
                case DatabaseError.USER_CODE_EXCEPTION:
                    errorMessage = "An error occurred in your code.";
                    recoveryAction = "Please contact support.";
                    break;
                case DatabaseError.WRITE_CANCELED:
                    errorMessage = "Write operation cancelled.";
                    recoveryAction = "Your changes were not saved.";
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