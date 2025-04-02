package com.example.vohoportunitysconect;

import android.app.Application;
import android.os.StrictMode;
import android.util.Log;
import com.example.vohoportunitysconect.database.DataManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.example.vohoportunitysconect.BuildConfig;

public class VOHApplication extends Application {
    private static final String TAG = "VOHApplication";
    private static VOHApplication instance;
    private DataManager dataManager;
    private ExecutorService executorService;

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            instance = this;
            
            // Configure thread pool
            executorService = Executors.newFixedThreadPool(4);
            
            // Initialize data manager
            dataManager = DataManager.getInstance(this);
            
            // Enable Firestore offline persistence
            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .setSslEnabled(true)
                .build();
            FirebaseFirestore.getInstance().setFirestoreSettings(settings);
            
            // Enable strict mode in debug builds
            if (BuildConfig.DEBUG) {
                StrictMode.enableDefaults();
            }
            
            Log.d(TAG, "Application initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing application: " + e.getMessage(), e);
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    public static VOHApplication getInstance() {
        return instance;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }
} 