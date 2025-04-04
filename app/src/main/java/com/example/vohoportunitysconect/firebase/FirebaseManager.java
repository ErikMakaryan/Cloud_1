package com.example.vohoportunitysconect.firebase;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.Map;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.database.DatabaseError;

public class FirebaseManager {
    private static final String TAG = "FirebaseManager";
    private static FirebaseManager instance;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference dbRef;
    private Context context;
    private boolean isInitialized = false;
    private Map<String, DataSnapshot> userDataCache;

    private FirebaseManager() {
        auth = FirebaseAuth.getInstance();
        userDataCache = new HashMap<>();
    }

    public static synchronized FirebaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseManager();
        }
        return instance;
    }

    public void initialize(Context context) {
        if (context == null) {
            Log.e(TAG, "Context cannot be null");
            return;
        }
        this.context = context.getApplicationContext();
        
        if (!checkGooglePlayServices()) {
            Log.e(TAG, "Google Play Services not available");
            return;
        }

        try {
            // Initialize Firebase Database with persistence
            database = FirebaseDatabase.getInstance("https://vohoportunitysconect-default-rtdb.firebaseio.com");
            database.setPersistenceEnabled(true);
            dbRef = database.getReference();
            
            // Add auth state listener
            auth.addAuthStateListener(firebaseAuth -> {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "User is signed in: " + user.getUid());
                    // Pre-fetch user data when signed in
                    prefetchUserData(user.getUid());
                } else {
                    Log.d(TAG, "User is signed out");
                    userDataCache.clear();
                }
            });
            
            isInitialized = true;
            Log.d(TAG, "FirebaseManager initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing FirebaseManager: " + e.getMessage(), e);
        }
    }

    private void prefetchUserData(String userId) {
        if (userDataCache.containsKey(userId)) {
            return;
        }
        
        dbRef.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    userDataCache.put(userId, dataSnapshot);
                    Log.d(TAG, "User data prefetched for: " + userId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error prefetching user data: " + databaseError.getMessage());
            }
        });
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

    private void checkInitialization() {
        if (!isInitialized) {
            throw new IllegalStateException("FirebaseManager must be initialized with a context before use");
        }
    }

    // Authentication Methods
    public void signUp(String email, String password, String name, OnCompleteListener<AuthResult> listener) {
        checkInitialization();
        if (!checkGooglePlayServices()) {
            listener.onComplete(null);
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().getUser() != null) {
                    // Create user profile in Realtime Database
                    createUserProfile(task.getResult().getUser().getUid(), name, email, "user");
                }
                listener.onComplete(task);
            });
    }

    public void signIn(String email, String password, OnCompleteListener<AuthResult> listener) {
        checkInitialization();
        if (!checkGooglePlayServices()) {
            listener.onComplete(null);
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().getUser() != null) {
                    String userId = task.getResult().getUser().getUid();
                    // Check cache first
                    if (userDataCache.containsKey(userId)) {
                        DataSnapshot cachedData = userDataCache.get(userId);
                        if (cachedData != null && cachedData.exists()) {
                            Log.d(TAG, "Using cached user data for: " + userId);
                            listener.onComplete(task);
                            return;
                        }
                    }
                }
                listener.onComplete(task);
            });
    }

    public void signOut() {
        checkInitialization();
        auth.signOut();
    }

    public FirebaseUser getCurrentUser() {
        checkInitialization();
        return auth.getCurrentUser();
    }

    // Realtime Database Methods
    public Task<Void> createUserProfile(String userId, String name, String email, String userType) {
        checkInitialization();
        if (!checkGooglePlayServices()) {
            return Tasks.forException(new Exception("Google Play Services not available"));
        }
        
        Map<String, Object> userProfile = new HashMap<>();
        userProfile.put("name", name);
        userProfile.put("email", email);
        userProfile.put("userType", userType);
        userProfile.put("createdAt", System.currentTimeMillis());
        userProfile.put("totalHours", 0);
        userProfile.put("opportunitiesCount", 0);
        
        return dbRef.child("users").child(userId).setValue(userProfile);
    }
    
    public void getUserProfile(String userId, OnCompleteListener<DataSnapshot> listener) {
        if (!checkGooglePlayServices()) {
            listener.onComplete(null);
            return;
        }
        
        dbRef.child("users").child(userId).get()
            .addOnCompleteListener(listener);
    }
    
    public void getFeaturedOpportunities(OnCompleteListener<DataSnapshot> listener) {
        if (!checkGooglePlayServices()) {
            listener.onComplete(null);
            return;
        }
        
        dbRef.child("opportunities")
            .orderByChild("isFeatured")
            .equalTo(true)
            .limitToFirst(10)
            .get()
            .addOnCompleteListener(listener);
    }
    
    public void getUserActivities(String userId, OnCompleteListener<DataSnapshot> listener) {
        if (!checkGooglePlayServices()) {
            listener.onComplete(null);
            return;
        }
        
        dbRef.child("activities")
            .orderByChild("userId")
            .equalTo(userId)
            .limitToLast(20)
            .get()
            .addOnCompleteListener(listener);
    }
    
    public void logVolunteerHours(String userId, String opportunityId, double hours, String description) {
        if (!checkGooglePlayServices()) {
            return;
        }
        
        String activityId = dbRef.child("activities").push().getKey();
        if (activityId == null) {
            Log.e(TAG, "Failed to generate activity ID");
            return;
        }
        
        Map<String, Object> activity = new HashMap<>();
        activity.put("userId", userId);
        activity.put("opportunityId", opportunityId);
        activity.put("hours", hours);
        activity.put("description", description);
        activity.put("timestamp", System.currentTimeMillis());
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("/activities/" + activityId, activity);
        
        dbRef.updateChildren(updates)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Volunteer hours logged successfully");
                    updateUserStats(userId, hours);
                } else {
                    Log.e(TAG, "Failed to log volunteer hours", task.getException());
                }
            });
    }
    
    private void updateUserStats(String userId, double newHours) {
        if (!checkGooglePlayServices()) {
            return;
        }
        
        dbRef.child("users").child(userId).get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().getValue() != null) {
                    DataSnapshot snapshot = task.getResult();
                    double currentHours = 0;
                    int opportunitiesCount = 0;
                    
                    if (snapshot.child("totalHours").getValue() != null) {
                        currentHours = ((Number) snapshot.child("totalHours").getValue()).doubleValue();
                    }
                    
                    if (snapshot.child("opportunitiesCount").getValue() != null) {
                        opportunitiesCount = ((Number) snapshot.child("opportunitiesCount").getValue()).intValue();
                    }
                    
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("totalHours", currentHours + newHours);
                    updates.put("opportunitiesCount", opportunitiesCount + 1);
                    
                    dbRef.child("users").child(userId).updateChildren(updates)
                        .addOnCompleteListener(updateTask -> {
                            if (updateTask.isSuccessful()) {
                                Log.d(TAG, "User stats updated successfully");
                            } else {
                                Log.e(TAG, "Failed to update user stats", updateTask.getException());
                            }
                        });
                }
            });
    }
    
    private <T> void handleTask(Task<T> task, OnCompleteListener<T> listener) {
        if (task.isSuccessful()) {
            listener.onComplete(task);
        } else {
            Log.e(TAG, "Task failed", task.getException());
            listener.onComplete(null);
        }
    }
    
    public <T> void executeTask(Task<T> task, OnCompleteListener<T> listener) {
        if (!checkGooglePlayServices()) {
            listener.onComplete(null);
            return;
        }
        
        task.addOnCompleteListener(result -> handleTask(result, listener));
    }

    public void deleteAllUsers(OnCompleteListener<Void> listener) {
        if (!checkGooglePlayServices()) {
            listener.onComplete(null);
            return;
        }

        // Delete all users from Realtime Database
        dbRef.child("users").removeValue()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "All users deleted from Realtime Database");
                } else {
                    Log.e(TAG, "Error deleting users from Realtime Database", task.getException());
                }
            });

        // Delete all users from Firebase Authentication
        auth.getCurrentUser().delete()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Current user deleted from Firebase Authentication");
                } else {
                    Log.e(TAG, "Error deleting current user from Firebase Authentication", task.getException());
                }
                listener.onComplete(task);
            });
    }

    public void getUserData(String userId, ValueEventListener listener) {
        checkInitialization();
        
        // Check cache first
        if (userDataCache.containsKey(userId)) {
            DataSnapshot cachedData = userDataCache.get(userId);
            if (cachedData != null && cachedData.exists()) {
                Log.d(TAG, "Using cached user data for: " + userId);
                listener.onDataChange(cachedData);
                return;
            }
        }
        
        // If not in cache, fetch from database
        dbRef.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    userDataCache.put(userId, dataSnapshot);
                }
                listener.onDataChange(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onCancelled(databaseError);
            }
        });
    }
} 