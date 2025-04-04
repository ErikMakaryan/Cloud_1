package com.example.vohoportunitysconect.database;

import android.util.Log;
import androidx.annotation.NonNull;
import com.example.vohoportunitysconect.BuildConfig;
import com.example.vohoportunitysconect.models.User;
import com.example.vohoportunitysconect.models.Opportunity;
import com.example.vohoportunitysconect.models.UserActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.google.android.gms.tasks.OnCompleteListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseManager {
    private static final String TAG = "DatabaseManager";
    private static volatile DatabaseManager instance;
    private final DatabaseReference databaseRef;
    private final FirebaseAuth mAuth;
    private final FirebaseAppCheck firebaseAppCheck;
    private final Map<String, ValueEventListener> activeListeners;
    private boolean isInitialized = false;

    static {
        // Enable persistence before getting database instance
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        } catch (Exception e) {
            Log.e(TAG, "Error enabling persistence: " + e.getMessage());
        }
    }

    private DatabaseManager() {
        try {
            // Initialize Firebase components
            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            databaseRef = firebaseDatabase.getReference();
            mAuth = FirebaseAuth.getInstance();
            firebaseAppCheck = FirebaseAppCheck.getInstance();
            activeListeners = new HashMap<>();

            // Configure App Check based on build type
            if (BuildConfig.DEBUG) {
                firebaseAppCheck.installAppCheckProviderFactory(
                    DebugAppCheckProviderFactory.getInstance()
                );
            } else {
                firebaseAppCheck.installAppCheckProviderFactory(
                    PlayIntegrityAppCheckProviderFactory.getInstance()
                );
            }

            // Test database connection
            databaseRef.child(".info/connected").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean connected = snapshot.getValue(Boolean.class);
                    if (connected) {
                        Log.d(TAG, "Database connected successfully");
                        isInitialized = true;
                    } else {
                        Log.e(TAG, "Database disconnected");
                        isInitialized = false;
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Database connection error: " + error.getMessage());
                    isInitialized = false;
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error initializing DatabaseManager: " + e.getMessage(), e);
            isInitialized = false;
        }
    }

    public static DatabaseManager getInstance() {
        DatabaseManager result = instance;
        if (result == null) {
            synchronized (DatabaseManager.class) {
                result = instance;
                if (result == null) {
                    instance = result = new DatabaseManager();
                }
            }
        }
        return result;
    }

    public interface DatabaseCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }

    private void checkAuthentication(DatabaseCallback<?> callback, Runnable onSuccess) {
        if (!isInitialized) {
            callback.onError(new Exception("DatabaseManager not properly initialized"));
            return;
        }

        if (callback == null) {
            Log.e(TAG, "Callback cannot be null");
            return;
        }

        if (mAuth.getCurrentUser() == null) {
            callback.onError(new Exception("User not authenticated"));
            return;
        }

        // Run the success callback directly
        onSuccess.run();
    }

    // Helper method to manage listeners
    private void registerListener(String key, ValueEventListener listener) {
        if (key == null || listener == null) {
            Log.e(TAG, "Cannot register null key or listener");
            return;
        }

        if (activeListeners.containsKey(key)) {
            removeListener(key);
        }
        activeListeners.put(key, listener);
    }

    public void removeListener(String key) {
        if (key == null) {
            Log.e(TAG, "Cannot remove listener with null key");
            return;
        }

        ValueEventListener listener = activeListeners.remove(key);
        if (listener != null && databaseRef != null) {
            try {
                databaseRef.removeEventListener(listener);
            } catch (Exception e) {
                Log.e(TAG, "Error removing listener: " + e.getMessage());
            }
        }
    }

    public void removeAllListeners() {
        if (databaseRef == null) {
            Log.e(TAG, "DatabaseReference is null");
            return;
        }

        for (Map.Entry<String, ValueEventListener> entry : activeListeners.entrySet()) {
            try {
                databaseRef.removeEventListener(entry.getValue());
            } catch (Exception e) {
                Log.e(TAG, "Error removing listener: " + e.getMessage());
            }
        }
        activeListeners.clear();
    }

    // User operations
    public void saveUser(User user, DatabaseCallback<Void> callback) {
        checkAuthentication(callback, () -> {
            if (user == null || user.getId() == null) {
                callback.onError(new Exception("Invalid user data"));
                return;
            }

            databaseRef.child("users").child(user.getId())
                    .setValue(user)
                    .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error saving user: " + e.getMessage());
                        callback.onError(e);
                    });
        });
    }

    public void getUser(String userId, DatabaseCallback<User> callback) {
        if (!isInitialized) {
            Log.e(TAG, "DatabaseManager not initialized");
            callback.onError(new Exception("Database not initialized"));
            return;
        }

        if (userId == null || userId.trim().isEmpty()) {
            Log.e(TAG, "Invalid user ID");
            callback.onError(new Exception("Invalid user ID"));
            return;
        }

        try {
            databaseRef.child("users").child(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DataSnapshot dataSnapshot = task.getResult();
                        if (dataSnapshot != null && dataSnapshot.exists()) {
                            try {
                                User user = dataSnapshot.getValue(User.class);
                                if (user != null) {
                                    user.setId(userId);
                                    Log.d(TAG, "User data retrieved successfully");
                                    callback.onSuccess(user);
                                } else {
                                    Log.e(TAG, "Failed to parse user data");
                                    callback.onError(new Exception("Failed to parse user data"));
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing user data: " + e.getMessage());
                                callback.onError(e);
                            }
                        } else {
                            Log.e(TAG, "User not found");
                            callback.onError(new Exception("User not found"));
                        }
                    } else {
                        Log.e(TAG, "Error getting user data: " + task.getException().getMessage());
                        callback.onError(task.getException());
                    }
                });
        } catch (Exception e) {
            Log.e(TAG, "Exception getting user data: " + e.getMessage());
            callback.onError(e);
        }
    }

    // Opportunity operations
    public void saveOpportunity(Opportunity opportunity, DatabaseCallback<Void> callback) {
        checkAuthentication(callback, () -> {
            if (opportunity == null || opportunity.getId() == null) {
                callback.onError(new Exception("Invalid opportunity data"));
                return;
            }

            databaseRef.child("opportunities").child(opportunity.getId())
                    .setValue(opportunity)
                    .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error saving opportunity: " + e.getMessage());
                        callback.onError(e);
                    });
        });
    }

    public void getOpportunity(String opportunityId, DatabaseCallback<Opportunity> callback) {
        checkAuthentication(callback, () -> {
            if (opportunityId == null || opportunityId.trim().isEmpty()) {
                callback.onError(new Exception("Invalid opportunity ID"));
                return;
            }

            ValueEventListener listener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    try {
                        if (dataSnapshot.exists()) {
                            Opportunity opportunity = dataSnapshot.getValue(Opportunity.class);
                            if (opportunity != null) {
                                opportunity.setId(dataSnapshot.getKey());
                                callback.onSuccess(opportunity);
                            } else {
                                callback.onError(new Exception("Failed to parse opportunity data"));
                            }
                        } else {
                            callback.onError(new Exception("Opportunity not found"));
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing opportunity data: " + e.getMessage());
                        callback.onError(e);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "Error getting opportunity: " + databaseError.getMessage());
                    callback.onError(new Exception(databaseError.getMessage()));
                }
            };

            String listenerKey = "opportunity_" + opportunityId;
            registerListener(listenerKey, listener);
            databaseRef.child("opportunities").child(opportunityId).addValueEventListener(listener);
        });
    }

    public void getAllOpportunities(DatabaseCallback<List<Opportunity>> callback) {
        checkAuthentication(callback, () -> {
            ValueEventListener listener = new ValueEventListener() {
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
                    Log.e(TAG, "Error getting opportunities: " + databaseError.getMessage());
                    callback.onError(new Exception(databaseError.getMessage()));
                }
            };

            String listenerKey = "all_opportunities";
            registerListener(listenerKey, listener);
            databaseRef.child("opportunities").addValueEventListener(listener);
        });
    }

    // Activity operations
    public void saveActivity(UserActivity activity, DatabaseCallback<Void> callback) {
        checkAuthentication(callback, () -> {
            if (activity == null || activity.getId() == null) {
                callback.onError(new Exception("Invalid activity data"));
                return;
            }

            databaseRef.child("activities").child(activity.getId())
                    .setValue(activity)
                    .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error saving activity: " + e.getMessage());
                        callback.onError(e);
                    });
        });
    }

    public void getUserActivities(String userId, DatabaseCallback<List<UserActivity>> callback) {
        checkAuthentication(callback, () -> {
            if (userId == null || userId.trim().isEmpty()) {
                callback.onError(new Exception("Invalid user ID"));
                return;
            }

            Query activitiesQuery = databaseRef.child("activities")
                    .orderByChild("userId")
                    .equalTo(userId);

            ValueEventListener listener = new ValueEventListener() {
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
                    Log.e(TAG, "Error getting user activities: " + databaseError.getMessage());
                    callback.onError(new Exception(databaseError.getMessage()));
                }
            };

            String listenerKey = "activities_" + userId;
            registerListener(listenerKey, listener);
            activitiesQuery.addValueEventListener(listener);
        });
    }

    public void updateUserProfile(String userId, Map<String, Object> updates, DatabaseCallback<Void> callback) {
        checkAuthentication(callback, () -> {
            if (userId == null || userId.trim().isEmpty()) {
                callback.onError(new Exception("Invalid user ID"));
                return;
            }

            databaseRef.child("users").child(userId)
                    .updateChildren(updates)
                    .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error updating user profile: " + e.getMessage());
                        callback.onError(e);
                    });
        });
    }

    public void updateVolunteerHours(String userId, int hours, DatabaseCallback<Void> callback) {
        checkAuthentication(callback, () -> {
            if (userId == null || userId.trim().isEmpty()) {
                callback.onError(new Exception("Invalid user ID"));
                return;
            }

            Map<String, Object> updates = new HashMap<>();
            updates.put("volunteer_hours", hours);
            
            databaseRef.child("users").child(userId)
                    .updateChildren(updates)
                    .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error updating volunteer hours: " + e.getMessage());
                        callback.onError(e);
                    });
        });
    }

    public void searchOpportunities(String query, DatabaseCallback<List<Opportunity>> callback) {
        checkAuthentication(callback, () -> {
            if (query == null || query.trim().isEmpty()) {
                callback.onError(new Exception("Invalid search query"));
                return;
            }

            Query searchQuery = databaseRef.child("opportunities")
                    .orderByChild("title")
                    .startAt(query)
                    .endAt(query + "\uf8ff");

            ValueEventListener listener = new ValueEventListener() {
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
                    Log.e(TAG, "Error searching opportunities: " + databaseError.getMessage());
                    callback.onError(new Exception(databaseError.getMessage()));
                }
            };

            String listenerKey = "search_" + query;
            registerListener(listenerKey, listener);
            searchQuery.addValueEventListener(listener);
        });
    }

    public void isOpportunitySaved(String userId, String opportunityId, DatabaseCallback<Boolean> callback) {
        checkAuthentication(callback, () -> {
            databaseRef.child("users").child(userId).child("savedOpportunities")
                    .child(opportunityId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            callback.onSuccess(snapshot.exists());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, "Error checking if opportunity is saved: " + error.getMessage());
                            callback.onError(new Exception(error.getMessage()));
                        }
                    });
        });
    }

    public void applyForOpportunity(String userId, String opportunityId, DatabaseCallback<Void> callback) {
        checkAuthentication(callback, () -> {
            // First check if the opportunity exists and has available slots
            databaseRef.child("opportunities").child(opportunityId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (!snapshot.exists()) {
                                callback.onError(new Exception("Opportunity not found"));
                                return;
                            }

                            Opportunity opportunity = snapshot.getValue(Opportunity.class);
                            if (opportunity == null) {
                                callback.onError(new Exception("Failed to load opportunity"));
                                return;
                            }

                            if (opportunity.getCurrentVolunteers() >= opportunity.getMaxVolunteers()) {
                                callback.onError(new Exception("Opportunity is full"));
                                return;
                            }

                            // Check if user has already applied
                            databaseRef.child("applications")
                                    .child(opportunityId)
                                    .child(userId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists()) {
                                                callback.onError(new Exception("You have already applied for this opportunity"));
                                                return;
                                            }

                                            // Create application
                                            Map<String, Object> application = new HashMap<>();
                                            application.put("userId", userId);
                                            application.put("opportunityId", opportunityId);
                                            application.put("status", "pending");
                                            application.put("appliedAt", System.currentTimeMillis());

                                            // Save application
                                            databaseRef.child("applications")
                                                    .child(opportunityId)
                                                    .child(userId)
                                                    .setValue(application)
                                                    .addOnSuccessListener(aVoid -> {
                                                        // Update opportunity's current volunteers count
                                                        Map<String, Object> updates = new HashMap<>();
                                                        updates.put("currentVolunteers", opportunity.getCurrentVolunteers() + 1);
                                                        
                                                        databaseRef.child("opportunities")
                                                                .child(opportunityId)
                                                                .updateChildren(updates)
                                                                .addOnSuccessListener(aVoid1 -> callback.onSuccess(null))
                                                                .addOnFailureListener(e -> {
                                                                    Log.e(TAG, "Error updating opportunity: " + e.getMessage());
                                                                    callback.onError(e);
                                                                });
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Log.e(TAG, "Error saving application: " + e.getMessage());
                                                        callback.onError(e);
                                                    });
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Log.e(TAG, "Error checking application: " + error.getMessage());
                                            callback.onError(new Exception(error.getMessage()));
                                        }
                                    });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, "Error loading opportunity: " + error.getMessage());
                            callback.onError(new Exception(error.getMessage()));
                        }
                    });
        });
    }

    public void toggleSaveOpportunity(String userId, String opportunityId, boolean isSaving, DatabaseCallback<Void> callback) {
        checkAuthentication(callback, () -> {
            if (isSaving) {
                // Save the opportunity
                databaseRef.child("users").child(userId).child("savedOpportunities")
                        .child(opportunityId)
                        .setValue(true)
                        .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error saving opportunity: " + e.getMessage());
                            callback.onError(e);
                        });
            } else {
                // Remove the saved opportunity
                databaseRef.child("users").child(userId).child("savedOpportunities")
                        .child(opportunityId)
                        .removeValue()
                        .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error removing saved opportunity: " + e.getMessage());
                            callback.onError(e);
                        });
            }
        });
    }

    public void getUserSavedOpportunities(String userId, DatabaseCallback<List<Opportunity>> callback) {
        checkAuthentication(callback, () -> {
            databaseRef.child("users").child(userId).child("savedOpportunities")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            List<Opportunity> savedOpportunities = new ArrayList<>();
                            if (snapshot.exists()) {
                                for (DataSnapshot opportunitySnapshot : snapshot.getChildren()) {
                                    String opportunityId = opportunitySnapshot.getKey();
                                    if (opportunityId != null) {
                                        databaseRef.child("opportunities").child(opportunityId)
                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot opportunityData) {
                                                        if (opportunityData.exists()) {
                                                            Opportunity opportunity = opportunityData.getValue(Opportunity.class);
                                                            if (opportunity != null) {
                                                                savedOpportunities.add(opportunity);
                                                            }
                                                        }
                                                        if (savedOpportunities.size() == snapshot.getChildrenCount()) {
                                                            callback.onSuccess(savedOpportunities);
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {
                                                        Log.e(TAG, "Error loading opportunity: " + error.getMessage());
                                                        callback.onError(new Exception(error.getMessage()));
                                                    }
                                                });
                                    }
                                }
                                if (snapshot.getChildrenCount() == 0) {
                                    callback.onSuccess(savedOpportunities);
                                }
                            } else {
                                callback.onSuccess(savedOpportunities);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, "Error loading saved opportunities: " + error.getMessage());
                            callback.onError(new Exception(error.getMessage()));
                        }
                    });
        });
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            removeAllListeners();
        } finally {
            super.finalize();
        }
    }
} 