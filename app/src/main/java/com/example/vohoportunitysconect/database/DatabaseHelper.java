package com.example.vohoportunitysconect.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "VOHDatabase.db";
    private static final int DATABASE_VERSION = 1;

    // Table names
    public static final String TABLE_USERS = "users";
    public static final String TABLE_OPPORTUNITIES = "opportunities";
    public static final String TABLE_APPLICATIONS = "applications";

    // Common column names
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_CREATED_AT = "created_at";

    // Users table columns
    public static final String COLUMN_USER_EMAIL = "email";
    public static final String COLUMN_USER_PASSWORD = "password";
    public static final String COLUMN_USER_NAME = "name";
    public static final String COLUMN_USER_PHONE = "phone";
    public static final String COLUMN_USER_BIO = "bio";

    // Opportunities table columns
    public static final String COLUMN_OPP_TITLE = "title";
    public static final String COLUMN_OPP_DESCRIPTION = "description";
    public static final String COLUMN_OPP_LOCATION = "location";
    public static final String COLUMN_OPP_CATEGORY = "category";
    public static final String COLUMN_OPP_DEADLINE = "deadline";
    public static final String COLUMN_OPP_REMOTE = "is_remote";
    public static final String COLUMN_OPP_CREATED_BY = "created_by";

    // Applications table columns
    public static final String COLUMN_APP_USER_ID = "user_id";
    public static final String COLUMN_APP_OPP_ID = "opportunity_id";
    public static final String COLUMN_APP_STATUS = "status";
    public static final String COLUMN_APP_APPLIED_DATE = "applied_date";

    // Create table queries
    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_USER_EMAIL + " TEXT UNIQUE NOT NULL,"
            + COLUMN_USER_PASSWORD + " TEXT NOT NULL,"
            + COLUMN_USER_NAME + " TEXT NOT NULL,"
            + COLUMN_USER_PHONE + " TEXT,"
            + COLUMN_USER_BIO + " TEXT,"
            + COLUMN_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP"
            + ")";

    private static final String CREATE_TABLE_OPPORTUNITIES = "CREATE TABLE " + TABLE_OPPORTUNITIES + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_OPP_TITLE + " TEXT NOT NULL,"
            + COLUMN_OPP_DESCRIPTION + " TEXT NOT NULL,"
            + COLUMN_OPP_LOCATION + " TEXT,"
            + COLUMN_OPP_CATEGORY + " TEXT NOT NULL,"
            + COLUMN_OPP_DEADLINE + " DATETIME NOT NULL,"
            + COLUMN_OPP_REMOTE + " INTEGER DEFAULT 0,"
            + COLUMN_OPP_CREATED_BY + " INTEGER NOT NULL,"
            + COLUMN_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
            + "FOREIGN KEY(" + COLUMN_OPP_CREATED_BY + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + ")"
            + ")";

    private static final String CREATE_TABLE_APPLICATIONS = "CREATE TABLE " + TABLE_APPLICATIONS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_APP_USER_ID + " INTEGER NOT NULL,"
            + COLUMN_APP_OPP_ID + " INTEGER NOT NULL,"
            + COLUMN_APP_STATUS + " TEXT NOT NULL,"
            + COLUMN_APP_APPLIED_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
            + "FOREIGN KEY(" + COLUMN_APP_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + "),"
            + "FOREIGN KEY(" + COLUMN_APP_OPP_ID + ") REFERENCES " + TABLE_OPPORTUNITIES + "(" + COLUMN_ID + ")"
            + ")";

    private static DatabaseHelper instance;
    private SQLiteDatabase database;

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(CREATE_TABLE_USERS);
            db.execSQL(CREATE_TABLE_OPPORTUNITIES);
            db.execSQL(CREATE_TABLE_APPLICATIONS);
            Log.d(TAG, "Database tables created successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error creating database tables: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_APPLICATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_OPPORTUNITIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);

        // Create tables again
        onCreate(db);
    }

    public SQLiteDatabase getDatabase() {
        if (database == null || !database.isOpen()) {
            database = this.getWritableDatabase();
        }
        return database;
    }

    // User operations
    public long insertUser(String email, String password, String name, String phone, String bio) {
        SQLiteDatabase db = getDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_EMAIL, email);
        values.put(COLUMN_USER_PASSWORD, password);
        values.put(COLUMN_USER_NAME, name);
        values.put(COLUMN_USER_PHONE, phone);
        values.put(COLUMN_USER_BIO, bio);
        return db.insert(TABLE_USERS, null, values);
    }

    public Cursor getUserByEmail(String email) {
        SQLiteDatabase db = getDatabase();
        return db.query(TABLE_USERS, null,
                COLUMN_USER_EMAIL + "=?", new String[]{email},
                null, null, null);
    }

    // Opportunity operations
    public long insertOpportunity(String title, String description, String location,
                                String category, String deadline, boolean isRemote, long createdBy) {
        SQLiteDatabase db = getDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_OPP_TITLE, title);
        values.put(COLUMN_OPP_DESCRIPTION, description);
        values.put(COLUMN_OPP_LOCATION, location);
        values.put(COLUMN_OPP_CATEGORY, category);
        values.put(COLUMN_OPP_DEADLINE, deadline);
        values.put(COLUMN_OPP_REMOTE, isRemote ? 1 : 0);
        values.put(COLUMN_OPP_CREATED_BY, createdBy);
        return db.insert(TABLE_OPPORTUNITIES, null, values);
    }

    public Cursor getAllOpportunities() {
        SQLiteDatabase db = getDatabase();
        return db.query(TABLE_OPPORTUNITIES, null, null, null, null, null, COLUMN_CREATED_AT + " DESC");
    }

    public Cursor getOpportunityById(long id) {
        SQLiteDatabase db = getDatabase();
        return db.query(TABLE_OPPORTUNITIES, null,
                COLUMN_ID + "=?", new String[]{String.valueOf(id)},
                null, null, null);
    }

    // Application operations
    public long insertApplication(long userId, long oppId, String status) {
        SQLiteDatabase db = getDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_APP_USER_ID, userId);
        values.put(COLUMN_APP_OPP_ID, oppId);
        values.put(COLUMN_APP_STATUS, status);
        return db.insert(TABLE_APPLICATIONS, null, values);
    }

    public Cursor getUserApplications(long userId) {
        SQLiteDatabase db = getDatabase();
        return db.query(TABLE_APPLICATIONS, null,
                COLUMN_APP_USER_ID + "=?", new String[]{String.valueOf(userId)},
                null, null, COLUMN_APP_APPLIED_DATE + " DESC");
    }

    public void updateApplicationStatus(long appId, String status) {
        SQLiteDatabase db = getDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_APP_STATUS, status);
        db.update(TABLE_APPLICATIONS, values,
                COLUMN_ID + "=?", new String[]{String.valueOf(appId)});
    }
} 