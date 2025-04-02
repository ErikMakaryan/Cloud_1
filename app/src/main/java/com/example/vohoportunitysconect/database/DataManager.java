package com.example.vohoportunitysconect.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.vohoportunitysconect.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class DataManager {
    private static final String TAG = "DataManager";
    private static final String PREF_NAME = "VOHPrefs";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_TYPE = "userType";
    private static final String KEY_USER_PASSWORD = "userPassword";

    private static DataManager instance;
    private final SharedPreferences prefs;
    private final DatabaseReference databaseRef;

    private DataManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        databaseRef = FirebaseDatabase.getInstance("https://vvoohh-e2b0a-default-rtdb.firebaseio.com").getReference();
    }

    public static synchronized DataManager getInstance(Context context) {
        if (instance == null) {
            instance = new DataManager(context.getApplicationContext());
        }
        return instance;
    }

    public void saveUserData(String userId, String email, String name) {
        try {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_USER_ID, userId);
            editor.putString(KEY_USER_EMAIL, email);
            editor.putString(KEY_USER_NAME, name);
            editor.apply();

            // Save to Realtime Database
            Map<String, Object> userData = new HashMap<>();
            userData.put("email", email);
            userData.put("name", name);
            userData.put("updatedAt", System.currentTimeMillis());

            databaseRef.child("users").child(userId).updateChildren(userData)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "User data saved successfully"))
                    .addOnFailureListener(e -> Log.e(TAG, "Error saving user data", e));
        } catch (Exception e) {
            Log.e(TAG, "Error in saveUserData: " + e.getMessage(), e);
        }
    }

    public void saveUserType(String userType) {
        try {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_USER_TYPE, userType);
            editor.apply();

            String userId = getCurrentUserId();
            if (userId != null) {
                databaseRef.child("users").child(userId).child("userType").setValue(userType)
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "User type saved successfully"))
                        .addOnFailureListener(e -> Log.e(TAG, "Error saving user type", e));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in saveUserType: " + e.getMessage(), e);
        }
    }

    public void saveCurrentUserPassword(String password) {
        try {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_USER_PASSWORD, password);
            editor.apply();
        } catch (Exception e) {
            Log.e(TAG, "Error in saveCurrentUserPassword: " + e.getMessage(), e);
        }
    }

    public String getCurrentUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    public String getCurrentUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, null);
    }

    public String getCurrentUserName() {
        return prefs.getString(KEY_USER_NAME, null);
    }

    public String getCurrentUserType() {
        return prefs.getString(KEY_USER_TYPE, null);
    }

    public String getCurrentUserPassword() {
        return prefs.getString(KEY_USER_PASSWORD, null);
    }

    public void clearUserData() {
        try {
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();

            String userId = getCurrentUserId();
            if (userId != null) {
                databaseRef.child("users").child(userId).removeValue()
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "User data cleared successfully"))
                        .addOnFailureListener(e -> Log.e(TAG, "Error clearing user data", e));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in clearUserData: " + e.getMessage(), e);
        }
    }

    public User getUserByEmail(String email) {
        try {
            String userId = getCurrentUserId();
            if (userId != null) {
                User user = new User();
                user.setId(userId);
                user.setEmail(email);
                user.setName(getCurrentUserName());
                user.setUserType(getCurrentUserType());
                return user;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in getUserByEmail: " + e.getMessage(), e);
        }
        return null;
    }

    public void updateUserProfile(String userId, Map<String, Object> updates) {
        try {
            databaseRef.child("users").child(userId).updateChildren(updates)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "User profile updated successfully"))
                    .addOnFailureListener(e -> Log.e(TAG, "Error updating user profile", e));
        } catch (Exception e) {
            Log.e(TAG, "Error in updateUserProfile: " + e.getMessage(), e);
        }
    }

    public void getUserData(String userId, DataCallback<User> callback) {
        try {
            databaseRef.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        User user = dataSnapshot.getValue(User.class);
                        if (user != null) {
                            user.setId(userId);
                            callback.onSuccess(user);
                        } else {
                            callback.onError(new Exception("Failed to parse user data"));
                        }
                    } else {
                        callback.onError(new Exception("User not found"));
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    callback.onError(databaseError.toException());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in getUserData: " + e.getMessage(), e);
            callback.onError(e);
        }
    }

    public interface DataCallback<T> {
        void onSuccess(T data);
        void onError(Exception e);
    }
} 