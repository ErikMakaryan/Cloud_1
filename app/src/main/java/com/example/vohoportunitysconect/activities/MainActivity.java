package com.example.vohoportunitysconect.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.vohoportunitysconect.R;
import com.example.vohoportunitysconect.database.DatabaseManager;
import com.example.vohoportunitysconect.models.User;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "MainActivity";
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    private NavigationView navigationView;
    private DatabaseManager databaseManager;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase and DatabaseManager
        mAuth = FirebaseAuth.getInstance();
        databaseManager = DatabaseManager.getInstance();

        // Check if user is authenticated
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // User is not authenticated, redirect to login
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Setup navigation
        setupNavigation();
        
        // Update navigation header with user info
        updateNavigationHeader();
    }

    private void setupNavigation() {
        // Get the NavHostFragment
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        }

        // Get the DrawerLayout
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        if (drawerLayout != null) {
            // Setup the AppBarConfiguration
            appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.nav_home,
                    R.id.nav_saved,
                    R.id.nav_applications,
                    R.id.nav_profile)
                    .setOpenableLayout(drawerLayout)
                    .build();

            // Get the NavigationView
            navigationView = findViewById(R.id.nav_view);
            if (navigationView != null) {
                // Setup the NavigationView with the NavController
                NavigationUI.setupWithNavController(navigationView, navController);
                // Set the navigation item click listener
                navigationView.setNavigationItemSelectedListener(this);
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.nav_logout) {
            // Handle logout
            mAuth.signOut();
            Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show();
            
            // Clear any stored data
            if (databaseManager != null) {
                databaseManager.clearUserData();
            }
            
            // Redirect to login screen
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        
        // Let the NavigationUI handle other navigation items
        return NavigationUI.onNavDestinationSelected(item, navController);
    }

    private void updateNavigationHeader() {
        if (navigationView == null) {
            Log.e(TAG, "NavigationView is null");
            return;
        }

        View headerView = navigationView.getHeaderView(0);
        if (headerView == null) {
            Log.e(TAG, "Header view is null");
            return;
        }

        TextView nameTextView = headerView.findViewById(R.id.nav_header_name);
        TextView emailTextView = headerView.findViewById(R.id.nav_header_email);

        if (nameTextView == null || emailTextView == null) {
            Log.e(TAG, "Header text views not found");
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "No current user");
            nameTextView.setText(getString(R.string.anonymous_user));
            emailTextView.setText(getString(R.string.no_email));
            return;
        }

        // Set email
        String email = currentUser.getEmail();
        emailTextView.setText(email != null ? email : getString(R.string.no_email));

        // Get user name from database
        String userId = currentUser.getUid();
        Log.d(TAG, "Attempting to load user data for ID: " + userId);
        
        // Check if database reference is initialized
        if (databaseManager == null) {
            Log.e(TAG, "DatabaseManager is not initialized");
            nameTextView.setText(getString(R.string.anonymous_user));
            return;
        }

        databaseManager.getUser(userId, new DatabaseManager.DatabaseCallback<User>() {
            @Override
            public void onSuccess(User user) {
                Log.d(TAG, "Successfully loaded user data");
                if (user != null && user.getName() != null) {
                    nameTextView.setText(user.getName());
                } else {
                    Log.e(TAG, "User data is null or name is missing");
                    nameTextView.setText(getString(R.string.anonymous_user));
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error loading user data: " + e.getMessage(), e);
                nameTextView.setText(getString(R.string.anonymous_user));
                Toast.makeText(MainActivity.this, 
                    "Error loading user data. Please try again later.", 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }
} 