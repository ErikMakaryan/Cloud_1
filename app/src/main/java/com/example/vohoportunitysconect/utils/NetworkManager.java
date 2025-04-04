package com.example.vohoportunitysconect.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.MainThread;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class NetworkManager {
    private static final String TAG = NetworkManager.class.getSimpleName();
    private static volatile NetworkManager instance;
    private final Context context;
    private final ConnectivityManager connectivityManager;
    private final List<NetworkCallback> callbacks;
    private final AtomicBoolean isOnline;
    private ConnectivityManager.NetworkCallback networkCallback;
    private volatile boolean isNetworkAvailable;

    private NetworkManager(@NonNull Context context) {
        this.context = context.getApplicationContext();
        this.connectivityManager = (ConnectivityManager) this.context.getSystemService(Context.CONNECTIVITY_SERVICE);
        this.callbacks = new ArrayList<>();
        this.isOnline = new AtomicBoolean(false);
        
        setupNetworkCallback();
    }

    @MainThread
    public static NetworkManager getInstance(@NonNull Context context) {
        if (instance == null) {
            synchronized (NetworkManager.class) {
                if (instance == null) {
                    instance = new NetworkManager(context);
                }
            }
        }
        return instance;
    }

    private void setupNetworkCallback() {
        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
                boolean hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                        networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
                
                boolean hasValidTransport = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET);
                
                boolean isConnected = hasInternet && hasValidTransport;
                isNetworkAvailable = isConnected;
                isOnline.set(isConnected);
                
                if (isConnected) {
                    Log.d(TAG, "Network is available - Transport: " + 
                        (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ? "WIFI" :
                         networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ? "CELLULAR" :
                         networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ? "ETHERNET" : "UNKNOWN"));
                }
                notifyCallbacks(isConnected);
            }

            @Override
            public void onLost(@NonNull Network network) {
                isNetworkAvailable = false;
                isOnline.set(false);
                Log.d(TAG, "Network connection lost");
                notifyCallbacks(false);
            }
        };

        NetworkRequest networkRequest = new NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
            .build();

        try {
            connectivityManager.registerDefaultNetworkCallback(networkCallback);
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException registering network callback: " + e.getMessage());
            try {
                connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
            } catch (Exception ex) {
                Log.e(TAG, "Failed to register network callback: " + ex.getMessage());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error registering network callback: " + e.getMessage());
        }
    }

    public interface NetworkCallback {
        void onNetworkStateChanged(boolean isOnline);
    }

    @MainThread
    public void addCallback(@NonNull NetworkCallback callback) {
        if (!callbacks.contains(callback)) {
            callbacks.add(callback);
            // Immediately notify the new callback of the current state
            callback.onNetworkStateChanged(isNetworkAvailable);
        }
    }

    @MainThread
    public void removeCallback(@NonNull NetworkCallback callback) {
        callbacks.remove(callback);
    }

    private void notifyCallbacks(boolean isOnline) {
        for (NetworkCallback callback : callbacks) {
            if (callback != null) {
                callback.onNetworkStateChanged(isOnline);
            }
        }
    }

    @MainThread
    public boolean isNetworkAvailable() {
        Network network = connectivityManager.getActiveNetwork();
        if (network == null) {
            return false;
        }
        
        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
        if (capabilities == null) {
            return false;
        }

        boolean hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
        
        boolean hasValidTransport = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET);
        
        return hasInternet && hasValidTransport;
    }

    @MainThread
    public void unregisterCallback() {
        if (networkCallback != null) {
            try {
                connectivityManager.unregisterNetworkCallback(networkCallback);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Error unregistering network callback: " + e.getMessage());
            }
        }
    }
} 