package com.letsbunk.android.letsbunk.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.letsbunk.android.letsbunk.data.SubjectContract.SubjectEntry;

/**
 * Created by intel on 6/2/2017.
 */

public class SubjectDbHelper extends SQLiteOpenHelper{

    private static final String DATABASE_NAME = "subjects.db";

    private static final int DATABASE_VERSION = 1;

    public SubjectDbHelper(Context context) {
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_SUBJECTS_TABLE = "CREATE TABLE " + SubjectEntry.TABLE_NAME +"(" +
                SubjectEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SubjectEntry.COLUMN_SUBJECT_NAME + " TEXT NOT NULL, " +
                SubjectEntry.COLUMN_SUBJECT_ATTENDED + " INTEGER NOT NULL, " +
                SubjectEntry.COLUMN_SUBJECT_BUNKED + " INTEGER NOT NULL, " +
                SubjectEntry.COLUMN_SUBJECT_MINIMUM + " INTEGER NOT NULL);";

        db.execSQL(SQL_CREATE_SUBJECTS_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
