package com.example.vohoportunitysconect.activities;

import android.content.Intent;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Source;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.example.vohoportunitysconect.utils.NetworkManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private EditText emailInput;
    private EditText passwordInput;
    private Button loginButton;
    private Button signUpButton;
    private ProgressBar progressBar;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_login);

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

            // Initialize Firebase
            firebaseAuth = FirebaseAuth.getInstance();
            databaseRef = FirebaseDatabase.getInstance();

            // Initialize views
            initializeViews();
            setupClickListeners();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Error initializing app: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void initializeViews() {
        try {
            emailInput = findViewById(R.id.email_input);
            passwordInput = findViewById(R.id.password_input);
            loginButton = findViewById(R.id.login_button);
            signUpButton = findViewById(R.id.signup_button);
            progressBar = findViewById(R.id.progress_bar);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage(), e);
            Toast.makeText(this, "Error initializing views", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(this, "No internet connection. Please check your network and try again.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (validateInput(email, password)) {
                showLoading(true);
                firebaseAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, task -> {
                            try {
                                if (task.isSuccessful() && task.getResult() != null && task.getResult().getUser() != null) {
                                    String userId = task.getResult().getUser().getUid();
                                    Log.d(TAG, "User signed in successfully with ID: " + userId);
                                    
                                    // Get user data from Realtime Database
                                    databaseRef.child("users").child(userId)
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    try {
                                                        if (dataSnapshot.exists()) {
                                                            String name = dataSnapshot.child("name").getValue(String.class);
                                                            String userType = dataSnapshot.child("userType").getValue(String.class);
                                                            
                                                            if (name != null && userType != null) {
                                                                // Save user data
                                                                VOHApplication.getInstance().getDataManager()
                                                                        .saveUserData(userId, email, name);
                                                                VOHApplication.getInstance().getDataManager()
                                                                        .saveUserType(userType);
                                                                // Save password for persistent sign-in
                                                                VOHApplication.getInstance().getDataManager()
                                                                        .saveCurrentUserPassword(passwordInput.getText().toString().trim());
                                                                
                                                                Log.d(TAG, "User data saved successfully");
                                                                navigateToMain();
                                                            } else {
                                                                showLoading(false);
                                                                Log.e(TAG, "User data is incomplete");
                                                                Toast.makeText(LoginActivity.this,
                                                                        "Error: User data is incomplete",
                                                                        Toast.LENGTH_SHORT).show();
                                                            }
                                                        } else {
                                                            showLoading(false);
                                                            Log.e(TAG, "User document not found");
                                                            Toast.makeText(LoginActivity.this,
                                                                    "Error: User data not found",
                                                                    Toast.LENGTH_SHORT).show();
                                                        }
                                                    } catch (Exception e) {
                                                        showLoading(false);
                                                        Log.e(TAG, "Error processing user data", e);
                                                        Toast.makeText(LoginActivity.this,
                                                                "Error: Unable to process user data",
                                                                Toast.LENGTH_SHORT).show();
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {
                                                    showLoading(false);
                                                    Log.e(TAG, "Error getting user data", databaseError.toException());
                                                    Toast.makeText(LoginActivity.this,
                                                            "Error: Unable to access user data. Please check your connection.",
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                } else {
                                    showLoading(false);
                                    Exception e = task.getException();
                                    Log.e(TAG, "Login failed", e);
                                    
                                    String errorMessage = "Login failed";
                                    if (e instanceof FirebaseAuthException) {
                                        FirebaseAuthException authException = (FirebaseAuthException) e;
                                        switch (authException.getErrorCode()) {
                                            case "ERROR_INVALID_EMAIL":
                                                errorMessage = "Invalid email address";
                                                break;
                                            case "ERROR_WRONG_PASSWORD":
                                                errorMessage = "Incorrect password";
                                                break;
                                            case "ERROR_USER_NOT_FOUND":
                                                errorMessage = "No account found with this email";
                                                break;
                                            case "ERROR_USER_DISABLED":
                                                errorMessage = "This account has been disabled";
                                                break;
                                            case "ERROR_TOO_MANY_REQUESTS":
                                                errorMessage = "Too many attempts. Please try again later";
                                                break;
                                            case "ERROR_NETWORK_REQUEST_FAILED":
                                                errorMessage = "Network error. Please check your connection and try again.";
                                                break;
                                            default:
                                                errorMessage = "Login failed: " + e.getMessage();
                                        }
                                    } else if (e instanceof java.lang.SecurityException) {
                                        errorMessage = "Google Play Services error. Please update Google Play Services and try again.";
                                    }
                                    
                                    Toast.makeText(LoginActivity.this,
                                            errorMessage,
                                            Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception e) {
                                showLoading(false);
                                Log.e(TAG, "Error in login completion: " + e.getMessage(), e);
                                Toast.makeText(LoginActivity.this,
                                        "Error during login: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        } catch (Exception e) {
            showLoading(false);
            Log.e(TAG, "Error in handleLogin: " + e.getMessage(), e);
            Toast.makeText(this, "Error during login: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateInput(String email, String password) {
        try {
            boolean isValid = true;
            
            if (email.isEmpty()) {
                emailInput.setError("Email is required");
                isValid = false;
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailInput.setError("Please enter a valid email address");
                isValid = false;
            }
            
            if (password.isEmpty()) {
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
} 