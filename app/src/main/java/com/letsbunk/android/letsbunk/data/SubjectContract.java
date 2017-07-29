package com.letsbunk.android.letsbunk.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by intel on 6/2/2017.
 */

public final class SubjectContract {

    private SubjectContract() {}

    public static final String CONTENT_AUTHORITY = "com.letsbunk.android.letsbunk";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_SUBJECTS = "subjects";

    public static final class SubjectEntry implements BaseColumns {

        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SUBJECTS;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SUBJECTS;

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI,PATH_SUBJECTS);

        public static final String TABLE_NAME = "subjects";

        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_SUBJECT_NAME = "name";
        public final static String COLUMN_SUBJECT_ATTENDED = "attended";
        public final static String COLUMN_SUBJECT_BUNKED = "bunked";
        public final static String COLUMN_SUBJECT_MINIMUM = "minimum";

    }
}
