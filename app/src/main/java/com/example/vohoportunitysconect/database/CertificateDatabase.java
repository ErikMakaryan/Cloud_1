package com.example.vohoportunitysconect.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.vohoportunitysconect.models.Certificate;

public class CertificateDatabase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "certificates.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_CERTIFICATES = "certificates";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_FILE_URL = "file_url";
    private static final String COLUMN_UPLOAD_DATE = "upload_date";
    private static final String COLUMN_USER_ID = "user_id";

    public CertificateDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_CERTIFICATES + " (" +
                COLUMN_ID + " TEXT PRIMARY KEY," +
                COLUMN_NAME + " TEXT," +
                COLUMN_FILE_URL + " TEXT," +
                COLUMN_UPLOAD_DATE + " INTEGER," +
                COLUMN_USER_ID + " TEXT" +
                ")";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CERTIFICATES);
        onCreate(db);
    }

    public void saveCertificate(Certificate certificate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, certificate.getId());
        values.put(COLUMN_NAME, certificate.getName());
        values.put(COLUMN_FILE_URL, certificate.getFileUrl());
        values.put(COLUMN_UPLOAD_DATE, certificate.getUploadDate());
        values.put(COLUMN_USER_ID, certificate.getUserId());

        db.insertWithOnConflict(TABLE_CERTIFICATES, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public Certificate getCertificate(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CERTIFICATES,
                new String[]{COLUMN_ID, COLUMN_NAME, COLUMN_FILE_URL, COLUMN_UPLOAD_DATE, COLUMN_USER_ID},
                COLUMN_ID + "=?",
                new String[]{id},
                null, null, null);

        Certificate certificate = null;
        if (cursor != null && cursor.moveToFirst()) {
            certificate = new Certificate();
            certificate.setId(cursor.getString(0));
            certificate.setName(cursor.getString(1));
            certificate.setFileUrl(cursor.getString(2));
            certificate.setUploadDate(cursor.getLong(3));
            certificate.setUserId(cursor.getString(4));
            cursor.close();
        }
        return certificate;
    }

    public void deleteCertificate(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CERTIFICATES, COLUMN_ID + "=?", new String[]{id});
    }
} 