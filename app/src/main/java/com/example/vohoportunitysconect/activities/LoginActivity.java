package com.example.vohoportunitysconect.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vohoportunitysconect.MainActivity;
import com.example.vohoportunitysconect.R;
import com.example.vohoportunitysconect.VOHApplication;
import com.example.vohoportunitysconect.models.UserType;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.example.vohoportunitysconect.utils.NetworkManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.vohoportunitysconect.firebase.FirebaseManager;
import java.util.HashMap;
import java.util.Map;
import com.example.vohoportunitysconect.databinding.ActivityLoginBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final String PREF_NAME = "VOHPrefs";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_PASSWORD = "user_password";
    private static final String DEMO_EMAIL = "individualproject2025@gmail.com";
    private static final String DEMO_PASSWORD = "Samsung2025";

    private EditText emailInput;
    private EditText passwordInput;
    private Button loginButton;
    private Button signUpButton;
    private Button quickLoginButton;
    private ProgressBar progressBar;
    private FirebaseManager firebaseManager;
    private SharedPreferences sharedPreferences;
    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        // Initialize views first
        initializeViews();

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Check if application is initialized
        if (!VOHApplication.getInstance().isInitialized()) {
            Toast.makeText(this, "Application not initialized properly. Please restart the app.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Check Google Play Services availability
        int googlePlayServicesStatus = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (googlePlayServicesStatus != ConnectionResult.SUCCESS) {
            if (GoogleApiAvailability.getInstance().isUserResolvableError(googlePlayServicesStatus)) {
                GoogleApiAvailability.getInstance().getErrorDialog(this, googlePlayServicesStatus, 9000).show();
            } else {
                Toast.makeText(this, "This device is not supported", Toast.LENGTH_SHORT).show();
                finish();
            }
            return;
        }

        // Get FirebaseManager instance from VOHApplication
        try {
            firebaseManager = VOHApplication.getInstance().getFirebaseManager();
        } catch (IllegalStateException e) {
            Log.e(TAG, "Error getting FirebaseManager: " + e.getMessage());
            Toast.makeText(this, "Error initializing Firebase. Please restart the app.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Setup click listeners
        setupClickListeners();

        // Check if user credentials are saved
        // String savedEmail = sharedPreferences.getString(KEY_USER_EMAIL, null);
        // String savedPassword = sharedPreferences.getString(KEY_USER_PASSWORD, null);

        // if (savedEmail != null && savedPassword != null) {
        //     emailInput.setText(savedEmail);
        //     passwordInput.setText(savedPassword);
        //     handleLogin();
        // }
    }

    private void initializeViews() {
        try {
            emailInput = binding.emailInput;
            passwordInput = binding.passwordInput;
            loginButton = binding.loginButton;
            signUpButton = binding.signupButton;
            quickLoginButton = binding.quickLoginButton;
            progressBar = binding.progressBar;

            if (emailInput == null || passwordInput == null || loginButton == null || 
                signUpButton == null || quickLoginButton == null || progressBar == null) {
                throw new IllegalStateException("One or more views could not be initialized");
            }

            // Set input type and text change listeners to prevent span errors
            emailInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            passwordInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);

            // Add text change listeners to handle input properly
            emailInput.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s != null && s.length() > 0) {
                        emailInput.setError(null);
                    }
                }

                @Override
                public void afterTextChanged(android.text.Editable s) {
                    if (s != null && s.length() > 0) {
                        emailInput.setError(null);
                    }
                }
            });

            passwordInput.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s != null && s.length() > 0) {
                        passwordInput.setError(null);
                    }
                }

                @Override
                public void afterTextChanged(android.text.Editable s) {
                    if (s != null && s.length() > 0) {
                        passwordInput.setError(null);
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage(), e);
            throw new IllegalStateException("Error initializing views: " + e.getMessage());
        }
    }

    private void setupClickListeners() {
        try {
            loginButton.setOnClickListener(v -> loginUser());
            signUpButton.setOnClickListener(v -> navigateToSignUp());
            quickLoginButton.setOnClickListener(v -> handleQuickLogin());
        } catch (Exception e) {
            Log.e(TAG, "Error setting up click listeners: " + e.getMessage(), e);
            Toast.makeText(this, "Error setting up buttons", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isNetworkAvailable() {
        // Use NetworkManager consistently for all network checks
        return NetworkManager.getInstance(this).isNetworkAvailable();
    }

    private void loginUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Validate inputs
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading dialog
        progressBar.setVisibility(View.VISIBLE);
        loginButton.setEnabled(false);

        // Sign in user
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        if (user.isEmailVerified()) {
                            // Email is verified, proceed to main activity
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } else {
                            // Email is not verified
                            showVerificationRequiredDialog(user);
                        }
                    }
                } else {
                    progressBar.setVisibility(View.GONE);
                    loginButton.setEnabled(true);
                    Toast.makeText(LoginActivity.this,
                        "Login failed: " + task.getException().getMessage(),
                        Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void showVerificationRequiredDialog(FirebaseUser user) {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Email Not Verified")
            .setMessage("Please verify your email before logging in. Would you like to resend the verification email?")
            .setPositiveButton("Resend", (dialog, which) -> {
                user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this,
                                "Verification email sent. Please check your inbox.",
                                Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(LoginActivity.this,
                                "Failed to send verification email: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                        }
                        // Sign out the user
                        mAuth.signOut();
                        progressBar.setVisibility(View.GONE);
                        loginButton.setEnabled(true);
                    });
            })
            .setNegativeButton("Cancel", (dialog, which) -> {
                // Sign out the user
                mAuth.signOut();
                progressBar.setVisibility(View.GONE);
                loginButton.setEnabled(true);
            })
            .setCancelable(false)
            .show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in and email is verified
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
    }

    private void navigateToMain() {
        try {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to main: " + e.getMessage(), e);
            Toast.makeText(this, "Error navigating to main screen", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToSignUp() {
        try {
            startActivity(new Intent(this, SignUpActivity.class));
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to signup: " + e.getMessage(), e);
            Toast.makeText(this, "Error navigating to signup screen", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleQuickLogin() {
        try {
            if (!isNetworkAvailable()) {
                Toast.makeText(this, "No internet connection. Please check your network and try again.", Toast.LENGTH_LONG).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            loginButton.setEnabled(false);

            // First, try to create the account if it doesn't exist
            firebaseManager.createUser(DEMO_EMAIL, DEMO_PASSWORD, task -> {
                if (task.isSuccessful() || (task.getException() instanceof FirebaseAuthException && 
                    ((FirebaseAuthException) task.getException()).getErrorCode().equals("ERROR_EMAIL_ALREADY_IN_USE"))) {
                    // If account creation was successful or account already exists, proceed with login
                    firebaseManager.signIn(DEMO_EMAIL, DEMO_PASSWORD, signInTask -> {
                        try {
                            if (signInTask.isSuccessful() && signInTask.getResult() != null && 
                                signInTask.getResult().getUser() != null) {
                                String userId = signInTask.getResult().getUser().getUid();
                                Log.d(TAG, "User signed in successfully with ID: " + userId);
                                
                                // Save credentials for auto-login
                                saveUserCredentials(DEMO_EMAIL, DEMO_PASSWORD);
                                
                                // Get user data from Realtime Database
                                firebaseManager.getUserData(userId, new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        try {
                                            if (dataSnapshot.exists()) {
                                                String name = dataSnapshot.child("name").getValue(String.class);
                                                String userTypeValue = dataSnapshot.child("userType").getValue(String.class);
                                                
                                                if (name != null && userTypeValue != null) {
                                                    // Save user data in background
                                                    new Thread(() -> {
                                                        try {
                                                            VOHApplication.getInstance().getDataManager()
                                                                    .saveUserData(userId, DEMO_EMAIL, name);
                                                            VOHApplication.getInstance().getDataManager()
                                                                    .saveUserType(userTypeValue);
                                                            
                                                            runOnUiThread(() -> {
                                                                Log.d(TAG, "User data saved successfully");
                                                                navigateToMain();
                                                            });
                                                        } catch (Exception e) {
                                                            runOnUiThread(() -> {
                                                                progressBar.setVisibility(View.GONE);
                                                                loginButton.setEnabled(true);
                                                                Log.e(TAG, "Error saving user data: " + e.getMessage(), e);
                                                                Toast.makeText(LoginActivity.this,
                                                                        "Error saving user data. Please try again.",
                                                                        Toast.LENGTH_LONG).show();
                                                            });
                                                        }
                                                    }).start();
                                                } else {
                                                    // If user data doesn't exist, create it
                                                    createDemoUserData(userId);
                                                }
                                            } else {
                                                // If user data doesn't exist, create it
                                                createDemoUserData(userId);
                                            }
                                        } catch (Exception e) {
                                            progressBar.setVisibility(View.GONE);
                                            loginButton.setEnabled(true);
                                            Log.e(TAG, "Error processing user data: " + e.getMessage(), e);
                                            Toast.makeText(LoginActivity.this,
                                                    "Error processing user data. Please try again.",
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        progressBar.setVisibility(View.GONE);
                                        loginButton.setEnabled(true);
                                        Log.e(TAG, "Database error: " + databaseError.getMessage());
                                        Toast.makeText(LoginActivity.this,
                                                "Error accessing database. Please try again.",
                                                Toast.LENGTH_LONG).show();
                                    }
                                });
                            } else {
                                progressBar.setVisibility(View.GONE);
                                loginButton.setEnabled(true);
                                String errorMessage = signInTask.getException() instanceof FirebaseAuthException ?
                                        "Invalid email or password" :
                                        "Login failed. Please try again.";
                                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                Log.e(TAG, "Login error: " + signInTask.getException().getMessage());
                            }
                        } catch (Exception e) {
                            progressBar.setVisibility(View.GONE);
                            loginButton.setEnabled(true);
                            Log.e(TAG, "Error during login: " + e.getMessage(), e);
                            Toast.makeText(LoginActivity.this,
                                    "Error during login. Please try again.",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    progressBar.setVisibility(View.GONE);
                    loginButton.setEnabled(true);
                    Log.e(TAG, "Error creating demo account: " + task.getException().getMessage());
                    Toast.makeText(LoginActivity.this,
                            "Error creating demo account. Please try again.",
                            Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            loginButton.setEnabled(true);
            Log.e(TAG, "Error in handleQuickLogin: " + e.getMessage(), e);
            Toast.makeText(this, "Error processing quick login. Please try again.", Toast.LENGTH_LONG).show();
        }
    }

    private void createDemoUserData(String userId) {
        try {
            // Create a basic user profile
            Map<String, Object> userData = new HashMap<>();
            userData.put("name", "Demo User");
            userData.put("email", DEMO_EMAIL);
            userData.put("userType", "VOLUNTEER");
            userData.put("createdAt", System.currentTimeMillis());

            firebaseManager.getDatabase().getReference("users").child(userId)
                    .setValue(userData)
                    .addOnSuccessListener(aVoid -> {
                        // Save user data to local preferences
                        VOHApplication.getInstance().getDataManager()
                                .saveUserData(userId, DEMO_EMAIL, "Demo User");
                        VOHApplication.getInstance().getDataManager()
                                .saveUserType("VOLUNTEER");
                        
                        runOnUiThread(() -> {
                            Log.d(TAG, "Demo user data created successfully");
                            navigateToMain();
                        });
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        loginButton.setEnabled(true);
                        Log.e(TAG, "Error creating demo user data: " + e.getMessage());
                        Toast.makeText(LoginActivity.this,
                                "Error creating demo user data. Please try again.",
                                Toast.LENGTH_LONG).show();
                    });
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            loginButton.setEnabled(true);
            Log.e(TAG, "Error in createDemoUserData: " + e.getMessage(), e);
            Toast.makeText(LoginActivity.this,
                    "Error creating demo user data. Please try again.",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void saveUserCredentials(String email, String password) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_PASSWORD, password);
        editor.apply();
    }

    private void clearUserCredentials() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_USER_EMAIL);
        editor.remove(KEY_USER_PASSWORD);
        editor.apply();
    }
} 