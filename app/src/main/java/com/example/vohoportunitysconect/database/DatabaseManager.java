package com.example.vohoportunitysconect.database;

import android.util.Log;
import com.example.vohoportunitysconect.models.User;
import com.example.vohoportunitysconect.models.Opportunity;
import com.example.vohoportunitysconect.models.UserActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseManager {
    private static final String TAG = "DatabaseManager";
    private static DatabaseManager instance;
    private final FirebaseFirestore db;

    private DatabaseManager() {
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public interface DatabaseCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }

    // User operations
    public void saveUser(User user, DatabaseCallback<Void> callback) {
        db.collection("users").document(user.getId())
                .set(user)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onError);
    }

    public void getUser(String userId, DatabaseCallback<User> callback) {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        callback.onSuccess(user);
                    } else {
                        callback.onError(new Exception("User not found"));
                    }
                })
                .addOnFailureListener(callback::onError);
    }

    // Opportunity operations
    public void saveOpportunity(Opportunity opportunity, DatabaseCallback<Void> callback) {
        db.collection("opportunities").document(opportunity.getId())
                .set(opportunity)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onError);
    }

    public void getOpportunity(String opportunityId, DatabaseCallback<Opportunity> callback) {
        db.collection("opportunities").document(opportunityId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Opportunity opportunity = documentSnapshot.toObject(Opportunity.class);
                        callback.onSuccess(opportunity);
                    } else {
                        callback.onError(new Exception("Opportunity not found"));
                    }
                })
                .addOnFailureListener(callback::onError);
    }

    public void getAllOpportunities(DatabaseCallback<List<Opportunity>> callback) {
        db.collection("opportunities")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Opportunity> opportunities = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Opportunity opportunity = document.toObject(Opportunity.class);
                        opportunities.add(opportunity);
                    }
                    callback.onSuccess(opportunities);
                })
                .addOnFailureListener(callback::onError);
    }

    // Activity operations
    public void saveActivity(UserActivity activity, DatabaseCallback<Void> callback) {
        db.collection("activities").document(activity.getId())
                .set(activity)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onError);
    }

    public void getUserActivities(String userId, DatabaseCallback<List<UserActivity>> callback) {
        db.collection("activities")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<UserActivity> activities = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        UserActivity activity = document.toObject(UserActivity.class);
                        activities.add(activity);
                    }
                    callback.onSuccess(activities);
                })
                .addOnFailureListener(callback::onError);
    }

    // User profile operations
    public void updateUserProfile(String userId, Map<String, Object> updates, DatabaseCallback<Void> callback) {
        db.collection("users").document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onError);
    }

    public void updateVolunteerHours(String userId, int hours, DatabaseCallback<Void> callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("volunteer_hours", FieldValue.increment(hours));
        
        db.collection("users").document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onError);
    }

    // Search operations
    public void searchOpportunities(String query, DatabaseCallback<List<Opportunity>> callback) {
        db.collection("opportunities")
                .whereGreaterThanOrEqualTo("title", query)
                .whereLessThanOrEqualTo("title", query + "\uf8ff")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Opportunity> opportunities = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Opportunity opportunity = document.toObject(Opportunity.class);
                        opportunities.add(opportunity);
                    }
                    callback.onSuccess(opportunities);
                })
                .addOnFailureListener(callback::onError);
    }
} 