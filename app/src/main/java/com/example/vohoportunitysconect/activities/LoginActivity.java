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

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final String PREF_NAME = "VOHPrefs";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_PASSWORD = "user_password";

    private EditText emailInput;
    private EditText passwordInput;
    private Button loginButton;
    private Button signUpButton;
    private ProgressBar progressBar;
    private FirebaseManager firebaseManager;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_login);

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
            String savedEmail = sharedPreferences.getString(KEY_USER_EMAIL, null);
            String savedPassword = sharedPreferences.getString(KEY_USER_PASSWORD, null);

            if (savedEmail != null && savedPassword != null) {
                // Auto login with saved credentials
                emailInput.setText(savedEmail);
                passwordInput.setText(savedPassword);
                handleLogin();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Error initializing app: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initializeViews() {
        try {
            emailInput = findViewById(R.id.email_input);
            passwordInput = findViewById(R.id.password_input);
            loginButton = findViewById(R.id.login_button);
            signUpButton = findViewById(R.id.signup_button);
            progressBar = findViewById(R.id.progress_bar);

            if (emailInput == null || passwordInput == null || loginButton == null || signUpButton == null || progressBar == null) {
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
            loginButton.setOnClickListener(v -> handleLogin());
            signUpButton.setOnClickListener(v -> navigateToSignUp());
        } catch (Exception e) {
            Log.e(TAG, "Error setting up click listeners: " + e.getMessage(), e);
            Toast.makeText(this, "Error setting up buttons", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isNetworkAvailable() {
        // Use NetworkManager consistently for all network checks
        return NetworkManager.getInstance(this).isNetworkAvailable();
    }

    private void handleLogin() {
        try {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (!isNetworkAvailable()) {
                Toast.makeText(this, "No internet connection. Please check your network and try again.", Toast.LENGTH_LONG).show();
                return;
            }

            if (validateInput(email, password)) {
                showLoading(true);
                
                // Disable UI elements during login
                setLoginEnabled(false);
                
                firebaseManager.signIn(email, password, task -> {
                    try {
                        if (task.isSuccessful() && task.getResult() != null && task.getResult().getUser() != null) {
                            String userId = task.getResult().getUser().getUid();
                            Log.d(TAG, "User signed in successfully with ID: " + userId);
                            
                            // Save credentials for auto-login
                            saveUserCredentials(email, password);
                            
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
                                                                .saveUserData(userId, email, name);
                                                        VOHApplication.getInstance().getDataManager()
                                                                .saveUserType(userTypeValue);
                                                        
                                                        runOnUiThread(() -> {
                                                            Log.d(TAG, "User data saved successfully");
                                                            navigateToMain();
                                                        });
                                                    } catch (Exception e) {
                                                        runOnUiThread(() -> {
                                                            showLoading(false);
                                                            setLoginEnabled(true);
                                                            Log.e(TAG, "Error saving user data: " + e.getMessage(), e);
                                                            Toast.makeText(LoginActivity.this,
                                                                    "Error saving user data. Please try again.",
                                                                    Toast.LENGTH_LONG).show();
                                                        });
                                                    }
                                                }).start();
                                            } else {
                                                showLoading(false);
                                                setLoginEnabled(true);
                                                Log.e(TAG, "User data is incomplete");
                                                Toast.makeText(LoginActivity.this,
                                                        "Error: User data is incomplete. Please contact support.",
                                                        Toast.LENGTH_LONG).show();
                                            }
                                        } else {
                                            showLoading(false);
                                            setLoginEnabled(true);
                                            Log.e(TAG, "User data not found");
                                            Toast.makeText(LoginActivity.this,
                                                    "Error: User data not found. Please contact support.",
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    } catch (Exception e) {
                                        showLoading(false);
                                        setLoginEnabled(true);
                                        Log.e(TAG, "Error processing user data: " + e.getMessage(), e);
                                        Toast.makeText(LoginActivity.this,
                                                "Error processing user data. Please try again.",
                                                Toast.LENGTH_LONG).show();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    showLoading(false);
                                    setLoginEnabled(true);
                                    Log.e(TAG, "Database error: " + databaseError.getMessage());
                                    Toast.makeText(LoginActivity.this,
                                            "Error accessing database. Please try again.",
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                        } else {
                            showLoading(false);
                            setLoginEnabled(true);
                            String errorMessage = task.getException() instanceof FirebaseAuthException ?
                                    "Invalid email or password" :
                                    "Login failed. Please try again.";
                            Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                            Log.e(TAG, "Login error: " + task.getException().getMessage());
                        }
                    } catch (Exception e) {
                        showLoading(false);
                        setLoginEnabled(true);
                        Log.e(TAG, "Error during login: " + e.getMessage(), e);
                        Toast.makeText(LoginActivity.this,
                                "Error during login. Please try again.",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        } catch (Exception e) {
            showLoading(false);
            setLoginEnabled(true);
            Log.e(TAG, "Error in handleLogin: " + e.getMessage(), e);
            Toast.makeText(this, "Error processing login. Please try again.", Toast.LENGTH_LONG).show();
        }
    }

    private boolean validateInput(String email, String password) {
        try {
            boolean isValid = true;
            
            if (email == null || email.trim().isEmpty()) {
                emailInput.setError("Email is required");
                isValid = false;
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailInput.setError("Please enter a valid email address");
                isValid = false;
            }
            
            if (password == null || password.trim().isEmpty()) {
                passwordInput.setError("Password is required");
                isValid = false;
            } else if (password.length() < 6) {
                passwordInput.setError("Password must be at least 6 characters");
                isValid = false;
            }
            
            return isValid;
        } catch (Exception e) {
            Log.e(TAG, "Error in validateInput: " + e.getMessage(), e);
            Toast.makeText(this, "Error validating input", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void showLoading(boolean isLoading) {
        try {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            loginButton.setEnabled(!isLoading);
            emailInput.setEnabled(!isLoading);
            passwordInput.setEnabled(!isLoading);
            signUpButton.setEnabled(!isLoading);
        } catch (Exception e) {
            Log.e(TAG, "Error in showLoading: " + e.getMessage(), e);
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

    private void setLoginEnabled(boolean enabled) {
        try {
            emailInput.setEnabled(enabled);
            passwordInput.setEnabled(enabled);
            loginButton.setEnabled(enabled);
            signUpButton.setEnabled(enabled);
        } catch (Exception e) {
            Log.e(TAG, "Error setting login enabled state: " + e.getMessage(), e);
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