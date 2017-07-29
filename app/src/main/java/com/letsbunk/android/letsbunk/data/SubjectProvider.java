package com.letsbunk.android.letsbunk.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.letsbunk.android.letsbunk.data.SubjectContract.SubjectEntry;

/**
 * Created by intel on 6/2/2017.
 */

public class SubjectProvider extends ContentProvider {

    public static final String LOG_TAG = SubjectProvider.class.getSimpleName();

    private SubjectDbHelper mDbHelper;

    private static final int SUBJECTS = 100;

    private static final int SUBJECT_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(SubjectContract.CONTENT_AUTHORITY,SubjectContract.PATH_SUBJECTS,SUBJECTS);

        sUriMatcher.addURI(SubjectContract.CONTENT_AUTHORITY,SubjectContract.PATH_SUBJECTS + "/#", SUBJECT_ID);
    }


    @Override
    public boolean onCreate() {
        mDbHelper = new SubjectDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case SUBJECTS:
                cursor = database.query(SubjectEntry.TABLE_NAME,projection,selection,
                        selectionArgs,null,null,sortOrder);
                break;
            case SUBJECT_ID:
                selection = SubjectEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                cursor = database.query(SubjectEntry.TABLE_NAME,projection,selection,
                        selectionArgs,null,null,sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(),uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case SUBJECTS:
                return SubjectEntry.CONTENT_LIST_TYPE;
            case SUBJECT_ID:
                return SubjectEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case SUBJECTS:
                return insertItem(uri,values);
            default:
                throw new IllegalArgumentException("Insertion not supported for " + uri);
        }
    }

    private Uri insertItem(Uri uri,ContentValues values) {

        String name = values.getAsString(SubjectEntry.COLUMN_SUBJECT_NAME);
        if(name == null) {
            throw new IllegalArgumentException("Subject requires a name");
        }

        Integer attended = values.getAsInteger(SubjectEntry.COLUMN_SUBJECT_ATTENDED);
        if(attended == null) {
            throw new IllegalArgumentException("Subject needs attended lectures");
        }

        Integer bunked = values.getAsInteger(SubjectEntry.COLUMN_SUBJECT_BUNKED);
        if(bunked == null) {
            throw new IllegalArgumentException("Subject needs bunked lectures");
        }

        Integer minimum = values.getAsInteger(SubjectEntry.COLUMN_SUBJECT_MINIMUM);
        if(minimum == null) {
            throw new IllegalArgumentException("Subject needs Minimum percentage");
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        long id = database.insert(SubjectEntry.TABLE_NAME,null,values);

        if(id == -1) {
            Log.e(LOG_TAG,"Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri,null);

        return ContentUris.withAppendedId(uri,id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case SUBJECTS:
                rowsDeleted = database.delete(SubjectEntry.TABLE_NAME,selection,selectionArgs);
                break;
            case SUBJECT_ID:
                selection = SubjectEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(SubjectEntry.TABLE_NAME,selection,selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        if(rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri,null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case SUBJECTS:
                return updateItem(uri, values, selection, selectionArgs);
            case SUBJECT_ID:
                // For the PET_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = SubjectEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateItem(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateItem(Uri uri,ContentValues values,String selection,String[] selectionArgs) {

        if(values.containsKey(SubjectEntry.COLUMN_SUBJECT_NAME)) {
            String name = values.getAsString(SubjectEntry.COLUMN_SUBJECT_NAME);
            if(name == null) {
                throw new IllegalArgumentException("Subject requires a name");
            }
        }

        if(values.containsKey(SubjectEntry.COLUMN_SUBJECT_ATTENDED)) {
            Integer attended = values.getAsInteger(SubjectEntry.COLUMN_SUBJECT_ATTENDED);
            if(attended == null || attended < 0) {
                throw new IllegalArgumentException("Subject requires attended lectures");
            }
        }

        if(values.containsKey(SubjectEntry.COLUMN_SUBJECT_BUNKED)) {
            Integer bunked = values.getAsInteger(SubjectEntry.COLUMN_SUBJECT_BUNKED);
            if(bunked == null || bunked < 0) {
                throw new IllegalArgumentException("Subject requires Bunked Lectures");
            }
        }

        if(values.containsKey(SubjectEntry.COLUMN_SUBJECT_MINIMUM)) {
            Integer minimum = values.getAsInteger(SubjectEntry.COLUMN_SUBJECT_MINIMUM);
            if(minimum == null || minimum < 0) {
                throw new IllegalArgumentException("Subject requires a Minimum percentage");
            }
        }

        if(values.size() == 0) {
            return 0;
        }

        SQLiteDatabase database =mDbHelper.getWritableDatabase();

        int rowsUpdated = database.update(SubjectEntry.TABLE_NAME,values,selection,selectionArgs);

        if(rowsUpdated !=0) {
            getContext().getContentResolver().notifyChange(uri,null);
        }

        return rowsUpdated;

    }
}
