package com.example.vohoportunitysconect;

import android.app.Application;
import android.os.StrictMode;
import android.util.Log;
import com.example.vohoportunitysconect.database.DataManager;
import com.example.vohoportunitysconect.firebase.FirebaseManager;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.example.vohoportunitysconect.BuildConfig;
import com.example.vohoportunitysconect.database.DatabaseManager;

public class VOHApplication extends Application {
    private static final String TAG = "VOHApplication";
    private static VOHApplication instance;
    private DataManager dataManager;
    private ExecutorService executorService;
    private FirebaseManager firebaseManager;
    private boolean isInitialized = false;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        
        try {
            // Initialize Firebase
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this);
            }
            
            // Enable Firebase persistence
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            
            // Initialize FirebaseManager first
            firebaseManager = FirebaseManager.getInstance();
            firebaseManager.initialize(this);
            
            // Initialize DatabaseManager
            DatabaseManager.getInstance();
            
            // Initialize executor service
            executorService = Executors.newFixedThreadPool(4);
            
            // Initialize data manager
            dataManager = DataManager.getInstance(this);
            
            // Enable strict mode in debug builds
            if (BuildConfig.DEBUG) {
                StrictMode.enableDefaults();
            }
            
            isInitialized = true;
            Log.d(TAG, "Application initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing application: " + e.getMessage(), e);
            isInitialized = false;
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
        if (instance == null) {
            throw new IllegalStateException("VOHApplication not initialized");
        }
        return instance;
    }

    public DataManager getDataManager() {
        if (!isInitialized) {
            throw new IllegalStateException("VOHApplication not initialized");
        }
        return dataManager;
    }

    public ExecutorService getExecutorService() {
        if (!isInitialized) {
            throw new IllegalStateException("VOHApplication not initialized");
        }
        return executorService;
    }

    public FirebaseManager getFirebaseManager() {
        if (!isInitialized) {
            throw new IllegalStateException("VOHApplication not initialized");
        }
        return firebaseManager;
    }

    public boolean isInitialized() {
        return isInitialized;
    }
} 