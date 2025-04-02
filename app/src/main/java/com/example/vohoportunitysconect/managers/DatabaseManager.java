package com.example.vohoportunitysconect.managers;

import android.util.Log;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import java.util.Map;
import java.util.HashMap;

public class DatabaseManager {
    private static final String TAG = "DatabaseManager";
    private static DatabaseManager instance;
    private final DatabaseReference databaseReference;

    private DatabaseManager() {
        try {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            database.setPersistenceEnabled(true); // Enable offline persistence
            databaseReference = database.getReference();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase Database: " + e.getMessage());
            throw e;
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    // Write data to a specific path
    public void writeData(String path, Object data, DatabaseCallback callback) {
        try {
            DatabaseReference ref = databaseReference.child(path);
            ref.setValue(data)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error writing data: " + e.getMessage());
                    if (callback != null) callback.onError(e);
                });
        } catch (Exception e) {
            Log.e(TAG, "Error in writeData: " + e.getMessage());
            if (callback != null) callback.onError(e);
        }
    }

    // Read data from a specific path
    public void readData(String path, DatabaseCallback callback) {
        try {
            DatabaseReference ref = databaseReference.child(path);
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (callback != null) callback.onDataReceived(dataSnapshot);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "Error reading data: " + databaseError.getMessage());
                    if (callback != null) callback.onError(databaseError.toException());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in readData: " + e.getMessage());
            if (callback != null) callback.onError(e);
        }
    }

    // Update data at a specific path
    public void updateData(String path, Object data, DatabaseCallback callback) {
        try {
            DatabaseReference ref = databaseReference.child(path);
            
            if (data instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> typedData = (Map<String, Object>) data;
                ref.updateChildren(typedData)
                    .addOnSuccessListener(aVoid -> {
                        if (callback != null) callback.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error updating data: " + e.getMessage());
                        if (callback != null) callback.onError(e);
                    });
            } else {
                // If data is not a Map, use setValue
                ref.setValue(data)
                    .addOnSuccessListener(aVoid -> {
                        if (callback != null) callback.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error updating data: " + e.getMessage());
                        if (callback != null) callback.onError(e);
                    });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in updateData: " + e.getMessage());
            if (callback != null) callback.onError(e);
        }
    }

    // Delete data at a specific path
    public void deleteData(String path, DatabaseCallback callback) {
        try {
            DatabaseReference ref = databaseReference.child(path);
            ref.removeValue()
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting data: " + e.getMessage());
                    if (callback != null) callback.onError(e);
                });
        } catch (Exception e) {
            Log.e(TAG, "Error in deleteData: " + e.getMessage());
            if (callback != null) callback.onError(e);
        }
    }

    // Query data with specific conditions
    public Query queryData(String path, String orderBy, Object value) {
        try {
            Query query = databaseReference.child(path).orderByChild(orderBy);
            
            if (value instanceof String) {
                return query.equalTo((String) value);
            } else if (value instanceof Double) {
                return query.equalTo((Double) value);
            } else if (value instanceof Boolean) {
                return query.equalTo((Boolean) value);
            } else if (value instanceof Long) {
                return query.equalTo((Long) value);
            } else {
                throw new IllegalArgumentException("Unsupported value type for query: " + value.getClass().getName());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in queryData: " + e.getMessage());
            throw e;
        }
    }

    // Interface for database callbacks
    public interface DatabaseCallback {
        void onSuccess();
        void onError(Exception e);
        void onDataReceived(DataSnapshot dataSnapshot);
    }
} 