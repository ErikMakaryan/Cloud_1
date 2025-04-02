package com.example.vohoportunitysconect.firebase;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.HashMap;
import java.util.Map;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

public class FirebaseManager {
    private static final String TAG = "FirebaseManager";
    private static FirebaseManager instance;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private Context context;

    private FirebaseManager() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized FirebaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseManager();
        }
        return instance;
    }

    public void initialize(Context context) {
        this.context = context.getApplicationContext();
        if (!checkGooglePlayServices()) {
            Log.e(TAG, "Google Play Services not available");
            return;
        }
    }

    private boolean checkGooglePlayServices() {
        if (context == null) {
            Log.e(TAG, "Context not initialized");
            return false;
        }

        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context);
        if (resultCode != ConnectionResult.SUCCESS) {
            Log.e(TAG, "Google Play Services not available: " + resultCode);
            return false;
        }
        return true;
    }

    // Authentication Methods
    public void signUp(String email, String password, String name, OnCompleteListener listener) {
        if (!checkGooglePlayServices()) {
            listener.onComplete(null);
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().getUser() != null) {
                    // Create user profile in Firestore
                    createUserProfile(task.getResult().getUser().getUid(), name, email);
                }
                listener.onComplete(task);
            });
    }

    public void signIn(String email, String password, OnCompleteListener listener) {
        if (!checkGooglePlayServices()) {
            listener.onComplete(null);
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(listener);
    }

    public void signOut() {
        auth.signOut();
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    // Firestore Methods
    private void createUserProfile(String userId, String name, String email) {
        if (!checkGooglePlayServices()) {
            return;
        }

        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("email", email);
        user.put("totalHours", 0);
        user.put("completedProjects", 0);
        user.put("createdAt", System.currentTimeMillis());

        db.collection("users").document(userId)
            .set(user)
            .addOnSuccessListener(aVoid -> Log.d(TAG, "User profile created"))
            .addOnFailureListener(e -> Log.e(TAG, "Error creating user profile", e));
    }

    public void getUserProfile(String userId, OnCompleteListener<DocumentSnapshot> listener) {
        if (!checkGooglePlayServices()) {
            listener.onComplete(null);
            return;
        }

        db.collection("users").document(userId)
            .get()
            .addOnCompleteListener(listener);
    }

    public void getFeaturedOpportunities(OnCompleteListener<QuerySnapshot> listener) {
        if (!checkGooglePlayServices()) {
            listener.onComplete(null);
            return;
        }

        db.collection("opportunities")
            .whereEqualTo("featured", true)
            .limit(5)
            .get()
            .addOnCompleteListener(listener);
    }

    public void getUserActivities(String userId, OnCompleteListener<QuerySnapshot> listener) {
        if (!checkGooglePlayServices()) {
            listener.onComplete(null);
            return;
        }

        db.collection("activities")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp")
            .limit(5)
            .get()
            .addOnCompleteListener(listener);
    }

    public void logVolunteerHours(String userId, String opportunityId, double hours, String description) {
        if (!checkGooglePlayServices()) {
            return;
        }

        Map<String, Object> activity = new HashMap<>();
        activity.put("userId", userId);
        activity.put("opportunityId", opportunityId);
        activity.put("hours", hours);
        activity.put("description", description);
        activity.put("timestamp", System.currentTimeMillis());

        db.collection("activities")
            .add(activity)
            .addOnSuccessListener(documentReference -> {
                // Update user's total hours
                updateUserStats(userId, hours);
            })
            .addOnFailureListener(e -> Log.e(TAG, "Error logging hours", e));
    }

    private void updateUserStats(String userId, double newHours) {
        if (!checkGooglePlayServices()) {
            return;
        }

        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    double currentHours = documentSnapshot.getDouble("totalHours") != null ? 
                        documentSnapshot.getDouble("totalHours") : 0;
                    long completedProjects = documentSnapshot.getLong("completedProjects") != null ?
                        documentSnapshot.getLong("completedProjects") : 0;

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("totalHours", currentHours + newHours);
                    updates.put("completedProjects", completedProjects + 1);

                    documentSnapshot.getReference().update(updates)
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "User stats updated"))
                        .addOnFailureListener(e -> Log.e(TAG, "Error updating user stats", e));
                }
            });
    }

    private void handleTask(Task<?> task, OnCompleteListener<Object> listener) {
        if (!task.isSuccessful()) {
            Exception exception = task.getException();
            Log.e(TAG, "Error: " + (exception != null ? exception.getMessage() : "Unknown error"));
            listener.onComplete(Tasks.forException(exception != null ? exception : new Exception("Unknown error")));
            return;
        }
        listener.onComplete(Tasks.forResult(task.getResult()));
    }

    public void executeTask(Task<?> task, OnCompleteListener<Object> listener) {
        if (task == null) {
            listener.onComplete(Tasks.forResult(null));
            return;
        }

        task.addOnCompleteListener(t -> handleTask(t, listener));
    }
} 