package com.example.vohoportunitysconect;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
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
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.activity.OnBackPressedCallback;

import com.example.vohoportunitysconect.activities.LoginActivity;
import com.example.vohoportunitysconect.models.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigation.NavigationView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "MainActivity";
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_main);

            mAuth = FirebaseAuth.getInstance();
            FirebaseUser currentUser = mAuth.getCurrentUser();
            
            // Check if user is signed in
            if (currentUser == null) {
                // Check if we have saved user data
                String savedEmail = VOHApplication.getInstance().getDataManager().getCurrentUserEmail();
                if (savedEmail != null) {
                    // Try to sign in with saved credentials
                    mAuth.signInWithEmailAndPassword(savedEmail, 
                            VOHApplication.getInstance().getDataManager().getCurrentUserPassword())
                            .addOnCompleteListener(this, task -> {
                                if (task.isSuccessful()) {
                                    setupToolbarAndDrawer();
                                    setupNavigation();
                                    updateNavigationHeader();
                                } else {
                                    // If saved credentials don't work, clear them and redirect to login
                                    VOHApplication.getInstance().getDataManager().clearUserData();
                                    redirectToLogin();
                                }
                            });
                } else {
                    redirectToLogin();
                }
                return;
            }

            setupToolbarAndDrawer();
            setupNavigation();
            updateNavigationHeader();

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Error initializing app: " + e.getMessage(), Toast.LENGTH_LONG).show();
            redirectToLogin();
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    private void setupToolbarAndDrawer() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Setup drawer toggle
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Setup bottom navigation
        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }

    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            
            appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.nav_home,
                    R.id.nav_opportunities,
                    R.id.nav_applications,
                    R.id.nav_saved,
                    R.id.nav_profile
            ).setOpenableLayout(drawerLayout).build();

            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
            NavigationUI.setupWithNavController(navigationView, navController);
            NavigationUI.setupWithNavController(bottomNavigationView, navController);

            setupDestinationChangeListener();
        } else {
            Log.e(TAG, "NavHostFragment not found");
            Toast.makeText(this, "Error initializing navigation", Toast.LENGTH_LONG).show();
            redirectToLogin();
        }
    }

    private void setupDestinationChangeListener() {
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            // Hide bottom navigation for non-main destinations
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
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }
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
        
        // Handle navigation
        try {
            boolean handled = NavigationUI.onNavDestinationSelected(item, navController);
            if (handled) {
                drawerLayout.closeDrawer(GravityCompat.START);
            }
            return handled;
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to destination: " + e.getMessage(), e);
            Toast.makeText(this, "Error navigating to selected item", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void handleLogout() {
        try {
            mAuth.signOut();
            redirectToLogin();
        } catch (Exception e) {
            Log.e(TAG, "Error during logout: " + e.getMessage(), e);
            Toast.makeText(this, "Error logging out: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void redirectToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void updateNavigationHeader() {
        try {
            View headerView = navigationView.getHeaderView(0);
            ShapeableImageView profileImage = headerView.findViewById(R.id.nav_header_image);
            TextView userNameText = headerView.findViewById(R.id.nav_header_name);
            TextView userEmailText = headerView.findViewById(R.id.nav_header_email);

            // Get current user info from local database
            String currentUserEmail = VOHApplication.getInstance().getDataManager().getCurrentUserEmail();
            if (currentUserEmail != null) {
                User currentUser = VOHApplication.getInstance().getDataManager().getUserByEmail(currentUserEmail);
                if (currentUser != null) {
                    userNameText.setText(currentUser.getName());
                    userEmailText.setText(currentUser.getEmail());

                    // Load profile image using Glide
                    Glide.with(this)
                            .load(R.drawable.default_profile_image)
                            .circleCrop()
                            .into(profileImage);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating navigation header: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}