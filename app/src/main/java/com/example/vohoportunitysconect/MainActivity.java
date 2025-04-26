package com.example.vohoportunitysconect;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.vohoportunitysconect.activities.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigation.NavigationView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.example.vohoportunitysconect.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "MainActivity";
    private static final String PREF_NAME = "VOHPrefs";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_PASSWORD = "user_password";
    
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    private FirebaseAuth mAuth;
    private ActionBarDrawerToggle toggle;
    private SharedPreferences sharedPreferences;

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        
        // Initialize views first
        drawerLayout = binding.drawerLayout;
        navigationView = binding.navView;
        bottomNavigationView = binding.bottomNavigation;
        
        // Setup toolbar
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        // Initialize NavController
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment == null) {
            throw new IllegalStateException("NavHostFragment not found");
        }
        navController = navHostFragment.getNavController();

        // Check if user is signed in
        if (currentUser == null) {
            // Try to restore session
            restoreUserSession();
            return;
        }

        setupNavigation(toolbar);
        updateNavigationHeader();

        // Setup Navigation
        NavigationUI.setupWithNavController(binding.bottomNavigation, navController);

        // Handle navigation item selection
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.opportunitiesFragment) {
                navController.navigate(R.id.opportunitiesFragment);
                return true;
            } else if (itemId == R.id.savedOpportunitiesFragment) {
                navController.navigate(R.id.savedOpportunitiesFragment);
                return true;
            } else if (itemId == R.id.myApplicationsFragment) {
                navController.navigate(R.id.myApplicationsFragment);
                return true;
            } else if (itemId == R.id.nav_profile) {
                navController.navigate(R.id.nav_profile);
                return true;
            }
            return false;
        });
    }

    private void restoreUserSession() {
        String email = sharedPreferences.getString(KEY_USER_EMAIL, null);
        String password = sharedPreferences.getString(KEY_USER_PASSWORD, null);

        if (email != null && password != null) {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Session restored successfully");
                            setupNavigation(binding.toolbar);
                            updateNavigationHeader();
                        } else {
                            Log.e(TAG, "Error restoring session", task.getException());
                            clearUserSession();
                            redirectToLogin();
                        }
                    });
        } else {
            redirectToLogin();
        }
    }

    public void saveUserSession(String email, String password) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_PASSWORD, password);
        editor.apply();
    }

    private void clearUserSession() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_USER_EMAIL);
        editor.remove(KEY_USER_PASSWORD);
        editor.apply();
    }

    private void setupNavigation(Toolbar toolbar) {
        // Configure the drawer layout
        toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Setup navigation view
        navigationView.setNavigationItemSelectedListener(this);

        // Configure AppBarConfiguration
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.opportunitiesFragment,
                R.id.savedOpportunitiesFragment,
                R.id.myApplicationsFragment,
                R.id.nav_profile
        ).setOpenableLayout(drawerLayout).build();

        // Setup UI with NavController
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        // Setup destination change listener
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            // Hide bottom navigation for specific destinations
            if (destination.getId() == R.id.nav_opportunity_details) {
                bottomNavigationView.setVisibility(View.GONE);
            } else {
                bottomNavigationView.setVisibility(View.VISIBLE);
            }

            // Update toolbar title
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(destination.getLabel());
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        
        if (itemId == R.id.nav_logout) {
            handleLogout();
            return true;
        }
        
        boolean handled = NavigationUI.onNavDestinationSelected(item, navController);
        if (handled) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        return handled;
    }

    private void handleLogout() {
        try {
            mAuth.signOut();
            clearUserSession();
            redirectToLogin();
        } catch (Exception e) {
            Log.e(TAG, "Error during logout: " + e.getMessage(), e);
            Toast.makeText(this, "Error logging out: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void updateNavigationHeader() {
        try {
            if (navigationView == null) {
                Log.e(TAG, "Navigation view is null");
                return;
            }

            View headerView = navigationView.getHeaderView(0);
            if (headerView == null) {
                Log.e(TAG, "Header view is null");
                return;
            }

            ShapeableImageView profileImage = headerView.findViewById(R.id.nav_header_image);
            TextView userNameText = headerView.findViewById(R.id.nav_header_name);
            TextView userEmailText = headerView.findViewById(R.id.nav_header_email);

            // Check if views are properly inflated
            if (userNameText == null || userEmailText == null) {
                Log.e(TAG, "Navigation header views not found");
                return;
            }

            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                // Set email
                String email = user.getEmail();
                if (email != null && !email.isEmpty()) {
                    userEmailText.setText(email);
                } else {
                    userEmailText.setText(R.string.no_email);
                }

                // Set name
                String displayName = user.getDisplayName();
                if (displayName != null && !displayName.isEmpty()) {
                    userNameText.setText(displayName);
                } else {
                    userNameText.setText(R.string.anonymous_user);
                }

                // Load profile image if view exists
                if (profileImage != null && user.getPhotoUrl() != null) {
                    Glide.with(this)
                            .load(user.getPhotoUrl())
                            .placeholder(R.drawable.default_profile_image)
                            .error(R.drawable.default_profile_image)
                            .circleCrop()
                            .into(profileImage);
                } else if (profileImage != null) {
                    profileImage.setImageResource(R.drawable.default_profile_image);
                }
            } else {
                Log.e(TAG, "Current user is null");
                redirectToLogin();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating navigation header: " + e.getMessage(), e);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (toggle != null) {
            toggle.syncState();
        }
    }
}