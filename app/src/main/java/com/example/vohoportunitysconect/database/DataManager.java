package com.example.vohoportunitysconect.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.example.vohoportunitysconect.models.User;
import com.example.vohoportunitysconect.models.UserType;

public class DataManager {
    private static final String TAG = "DataManager";
    private static final String PREF_NAME = "VOHPrefs";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_TYPE = "userType";
    private static final String KEY_USER_PASSWORD = "userPassword";

    private static DataManager instance;
    private final Context context;
    private final SharedPreferences preferences;
    private final FirebaseAuth firebaseAuth;
    private final DatabaseHelper dbHelper;

    private DataManager(Context context) {
        this.context = context.getApplicationContext();
        this.preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public static synchronized DataManager getInstance(Context context) {
        if (instance == null) {
            instance = new DataManager(context);
        }
        return instance;
    }

    public boolean isUserLoggedIn() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        return currentUser != null;
    }

    public String getCurrentUserId() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        return currentUser != null ? currentUser.getUid() : null;
    }

    public String getCurrentUserEmail() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        return currentUser != null ? currentUser.getEmail() : null;
    }

    public void saveUserData(String userId, String email, String name) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_NAME, name);
        editor.apply();
    }

    public void saveUserType(String userType) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_USER_TYPE, userType);
        editor.apply();
    }

    public String getUserType() {
        return preferences.getString(KEY_USER_TYPE, null);
    }

    public void saveCurrentUserPassword(String password) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_USER_PASSWORD, password);
        editor.apply();
    }

    public String getCurrentUserPassword() {
        return preferences.getString(KEY_USER_PASSWORD, null);
    }

    public void clearUserData() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(KEY_USER_ID);
        editor.remove(KEY_USER_EMAIL);
        editor.remove(KEY_USER_NAME);
        editor.remove(KEY_USER_TYPE);
        editor.remove(KEY_USER_PASSWORD);
        editor.apply();
    }

    public String getSavedUserId() {
        return preferences.getString(KEY_USER_ID, null);
    }

    public String getSavedUserEmail() {
        return preferences.getString(KEY_USER_EMAIL, null);
    }

    public String getSavedUserName() {
        return preferences.getString(KEY_USER_NAME, null);
    }

    public void logoutUser() {
        try {
            firebaseAuth.signOut();
            clearUserData();
        } catch (Exception e) {
            Log.e(TAG, "Error logging out user: " + e.getMessage());
        }
    }

    // User operations
    public long registerUser(String email, String password, String name, String phone, String bio) {
        try {
            return dbHelper.insertUser(email, password, name, phone, bio);
        } catch (Exception e) {
            Log.e(TAG, "Error registering user: " + e.getMessage());
            return -1;
        }
    }

    public Map<String, Object> loginUser(String email, String password) {
        try (Cursor cursor = dbHelper.getUserByEmail(email)) {
            if (cursor != null && cursor.moveToFirst()) {
                int passwordColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_PASSWORD);
                if (passwordColumnIndex != -1) {
                    String storedPassword = cursor.getString(passwordColumnIndex);
                    if (storedPassword.equals(password)) {
                        Map<String, Object> user = new HashMap<>();
                        addColumnToMap(cursor, DatabaseHelper.COLUMN_ID, "id", user);
                        addColumnToMap(cursor, DatabaseHelper.COLUMN_USER_EMAIL, "email", user);
                        addColumnToMap(cursor, DatabaseHelper.COLUMN_USER_NAME, "name", user);
                        addColumnToMap(cursor, DatabaseHelper.COLUMN_USER_PHONE, "phone", user);
                        addColumnToMap(cursor, DatabaseHelper.COLUMN_USER_BIO, "bio", user);
                        return user;
                    }
                }
            }
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Error logging in user: " + e.getMessage());
            return null;
        }
    }

    private void addColumnToMap(Cursor cursor, String columnName, String mapKey, Map<String, Object> map) {
        int columnIndex = cursor.getColumnIndex(columnName);
        if (columnIndex != -1) {
            switch (cursor.getType(columnIndex)) {
                case Cursor.FIELD_TYPE_INTEGER:
                    map.put(mapKey, cursor.getLong(columnIndex));
                    break;
                case Cursor.FIELD_TYPE_STRING:
                    map.put(mapKey, cursor.getString(columnIndex));
                    break;
                case Cursor.FIELD_TYPE_FLOAT:
                    map.put(mapKey, cursor.getDouble(columnIndex));
                    break;
                default:
                    // Handle other types if needed
                    break;
            }
        }
    }

    public User getUserByEmail(String email) {
        try (Cursor cursor = dbHelper.getUserByEmail(email)) {
            if (cursor != null && cursor.moveToFirst()) {
                User userObj = new User();
                
                int idColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ID);
                int nameColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_NAME);
                int emailColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_EMAIL);
                int phoneColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_PHONE);
                int bioColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_BIO);
                
                if (idColumnIndex != -1) {
                    userObj.setId(String.valueOf(cursor.getLong(idColumnIndex)));
                }
                if (nameColumnIndex != -1) {
                    userObj.setName(cursor.getString(nameColumnIndex));
                }
                if (emailColumnIndex != -1) {
                    userObj.setEmail(cursor.getString(emailColumnIndex));
                }
                if (phoneColumnIndex != -1) {
                    userObj.setPhoneNumber(cursor.getString(phoneColumnIndex));
                }
                if (bioColumnIndex != -1) {
                    userObj.setBio(cursor.getString(bioColumnIndex));
                }
                
                FirebaseUser user = firebaseAuth.getCurrentUser();
                UserType userType = user != null && user.getEmail() != null && 
                                  user.getEmail().endsWith("@organization.com") ? 
                                  UserType.ORGANIZATION : UserType.VOLUNTEER;
                userObj.setUserType(userType);
                
                return userObj;
            }
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Error getting user by email: " + e.getMessage());
            return null;
        }
    }

    // Opportunity operations
    public long createOpportunity(String title, String description, String location,
                                String category, String deadline, boolean isRemote, long createdBy) {
        try {
            return dbHelper.insertOpportunity(title, description, location, category, deadline, isRemote, createdBy);
        } catch (Exception e) {
            Log.e(TAG, "Error creating opportunity: " + e.getMessage());
            return -1;
        }
    }

    public List<Map<String, Object>> getAllOpportunities() {
        List<Map<String, Object>> opportunities = new ArrayList<>();
        try (Cursor cursor = dbHelper.getAllOpportunities()) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Map<String, Object> opportunity = new HashMap<>();
                    addColumnToMap(cursor, DatabaseHelper.COLUMN_ID, "id", opportunity);
                    addColumnToMap(cursor, DatabaseHelper.COLUMN_OPP_TITLE, "title", opportunity);
                    addColumnToMap(cursor, DatabaseHelper.COLUMN_OPP_DESCRIPTION, "description", opportunity);
                    addColumnToMap(cursor, DatabaseHelper.COLUMN_OPP_LOCATION, "location", opportunity);
                    addColumnToMap(cursor, DatabaseHelper.COLUMN_OPP_CATEGORY, "category", opportunity);
                    addColumnToMap(cursor, DatabaseHelper.COLUMN_OPP_DEADLINE, "deadline", opportunity);
                    addColumnToMap(cursor, DatabaseHelper.COLUMN_OPP_REMOTE, "isRemote", opportunity);
                    addColumnToMap(cursor, DatabaseHelper.COLUMN_OPP_CREATED_BY, "createdBy", opportunity);
                    addColumnToMap(cursor, DatabaseHelper.COLUMN_CREATED_AT, "createdAt", opportunity);
                    opportunities.add(opportunity);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting opportunities: " + e.getMessage());
        }
        return opportunities;
    }

    public Map<String, Object> getOpportunityById(long id) {
        try (Cursor cursor = dbHelper.getOpportunityById(id)) {
            if (cursor != null && cursor.moveToFirst()) {
                Map<String, Object> opportunity = new HashMap<>();
                addColumnToMap(cursor, DatabaseHelper.COLUMN_ID, "id", opportunity);
                addColumnToMap(cursor, DatabaseHelper.COLUMN_OPP_TITLE, "title", opportunity);
                addColumnToMap(cursor, DatabaseHelper.COLUMN_OPP_DESCRIPTION, "description", opportunity);
                addColumnToMap(cursor, DatabaseHelper.COLUMN_OPP_LOCATION, "location", opportunity);
                addColumnToMap(cursor, DatabaseHelper.COLUMN_OPP_CATEGORY, "category", opportunity);
                addColumnToMap(cursor, DatabaseHelper.COLUMN_OPP_DEADLINE, "deadline", opportunity);
                addColumnToMap(cursor, DatabaseHelper.COLUMN_OPP_REMOTE, "isRemote", opportunity);
                addColumnToMap(cursor, DatabaseHelper.COLUMN_OPP_CREATED_BY, "createdBy", opportunity);
                addColumnToMap(cursor, DatabaseHelper.COLUMN_CREATED_AT, "createdAt", opportunity);
                return opportunity;
            }
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Error getting opportunity: " + e.getMessage());
            return null;
        }
    }

    // Application operations
    public long applyForOpportunity(long userId, long oppId, String status) {
        try {
            return dbHelper.insertApplication(userId, oppId, status);
        } catch (Exception e) {
            Log.e(TAG, "Error applying for opportunity: " + e.getMessage());
            return -1;
        }
    }

    public List<Map<String, Object>> getUserApplications(long userId) {
        List<Map<String, Object>> applications = new ArrayList<>();
        try (Cursor cursor = dbHelper.getUserApplications(userId)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Map<String, Object> application = new HashMap<>();
                    addColumnToMap(cursor, DatabaseHelper.COLUMN_ID, "id", application);
                    addColumnToMap(cursor, DatabaseHelper.COLUMN_APP_USER_ID, "userId", application);
                    addColumnToMap(cursor, DatabaseHelper.COLUMN_APP_OPP_ID, "oppId", application);
                    addColumnToMap(cursor, DatabaseHelper.COLUMN_APP_STATUS, "status", application);
                    addColumnToMap(cursor, DatabaseHelper.COLUMN_APP_APPLIED_DATE, "appliedDate", application);
                    applications.add(application);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting user applications: " + e.getMessage());
        }
        return applications;
    }

    public boolean updateApplicationStatus(long appId, String status) {
        try {
            dbHelper.updateApplicationStatus(appId, status);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error updating application status: " + e.getMessage());
            return false;
        }
    }
} 