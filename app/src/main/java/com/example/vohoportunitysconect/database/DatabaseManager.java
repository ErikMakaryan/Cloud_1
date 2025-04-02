package com.example.vohoportunitysconect.database;

import android.util.Log;
import com.example.vohoportunitysconect.models.User;
import com.example.vohoportunitysconect.models.Opportunity;
import com.example.vohoportunitysconect.models.UserActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseManager {
    private static final String TAG = "DatabaseManager";
    private static DatabaseManager instance;
    private final DatabaseReference databaseRef;

    private DatabaseManager() {
        databaseRef = FirebaseDatabase.getInstance("https://vvoohh-e2b0a-default-rtdb.firebaseio.com").getReference();
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
        databaseRef.child("users").child(user.getId())
                .setValue(user)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onError);
    }

    public void getUser(String userId, DatabaseCallback<User> callback) {
        databaseRef.child("users").child(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            User user = dataSnapshot.getValue(User.class);
                            callback.onSuccess(user);
                        } else {
                            callback.onError(new Exception("User not found"));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        callback.onError(new Exception(databaseError.getMessage()));
                    }
                });
    }

    // Opportunity operations
    public void saveOpportunity(Opportunity opportunity, DatabaseCallback<Void> callback) {
        databaseRef.child("opportunities").child(opportunity.getId())
                .setValue(opportunity)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onError);
    }

    public void getOpportunity(String opportunityId, DatabaseCallback<Opportunity> callback) {
        databaseRef.child("opportunities").child(opportunityId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Opportunity opportunity = dataSnapshot.getValue(Opportunity.class);
                            callback.onSuccess(opportunity);
                        } else {
                            callback.onError(new Exception("Opportunity not found"));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        callback.onError(new Exception(databaseError.getMessage()));
                    }
                });
    }

    public void getAllOpportunities(DatabaseCallback<List<Opportunity>> callback) {
        databaseRef.child("opportunities")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<Opportunity> opportunities = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Opportunity opportunity = snapshot.getValue(Opportunity.class);
                            if (opportunity != null) {
                                opportunity.setId(snapshot.getKey());
                                opportunities.add(opportunity);
                            }
                        }
                        callback.onSuccess(opportunities);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        callback.onError(new Exception(databaseError.getMessage()));
                    }
                });
    }

    // Activity operations
    public void saveActivity(UserActivity activity, DatabaseCallback<Void> callback) {
        databaseRef.child("activities").child(activity.getId())
                .setValue(activity)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onError);
    }

    public void getUserActivities(String userId, DatabaseCallback<List<UserActivity>> callback) {
        Query activitiesQuery = databaseRef.child("activities")
                .orderByChild("userId")
                .equalTo(userId);

        activitiesQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<UserActivity> activities = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UserActivity activity = snapshot.getValue(UserActivity.class);
                    if (activity != null) {
                        activity.setId(snapshot.getKey());
                        activities.add(activity);
                    }
                }
                callback.onSuccess(activities);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(new Exception(databaseError.getMessage()));
            }
        });
    }

    public void updateUserProfile(String userId, Map<String, Object> updates, DatabaseCallback<Void> callback) {
        databaseRef.child("users").child(userId)
                .updateChildren(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onError);
    }

    public void updateVolunteerHours(String userId, int hours, DatabaseCallback<Void> callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("volunteer_hours", hours);
        
        databaseRef.child("users").child(userId)
                .updateChildren(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onError);
    }

    public void searchOpportunities(String query, DatabaseCallback<List<Opportunity>> callback) {
        Query searchQuery = databaseRef.child("opportunities")
                .orderByChild("title")
                .startAt(query)
                .endAt(query + "\uf8ff");

        searchQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Opportunity> opportunities = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Opportunity opportunity = snapshot.getValue(Opportunity.class);
                    if (opportunity != null && opportunity.getTitle().toLowerCase().contains(query.toLowerCase())) {
                        opportunity.setId(snapshot.getKey());
                        opportunities.add(opportunity);
                    }
                }
                callback.onSuccess(opportunities);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(new Exception(databaseError.getMessage()));
            }
        });
    }
} 