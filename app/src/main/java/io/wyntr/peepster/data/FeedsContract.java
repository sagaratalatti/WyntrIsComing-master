package io.wyntr.peepster.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.DateUtils;
import android.util.Log;

import static io.wyntr.peepster.data.FeedsContract.FeedsEntry.CONTENT_URI;

/**
 * Created by sagar on 11-02-2017.
 */

public class FeedsContract {

    private static final String TAG = FeedsContract.class.getSimpleName();

    public static final String CONTENT_AUTHORITY = "io.wyntr.peepster";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_FEEDS = "feeds";

    public static CharSequence normalizeDate(long startDate) {
       return DateUtils.getRelativeTimeSpanString(
                startDate);
    }

    public static final class FeedsEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_FEEDS).build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FEEDS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FEEDS;
        public static final String TABLE_NAME = "feeds";
        public static final String COLUMN_USER = "username";
        public static final String COLUMN_USER_ID = "userId";
        public static final String COLUMN_VIDEO = "video";
        public static final String COLUMN_TIME_STAMP = "timestamp";
        public static final String COLUMN_THUMB = "thumb";
        public static final String COLUMN_POST_KEY = "key";
        public static final String COLUMN_USER_PROFILE = "profile";
        public static final String COLUMN_POST_CAPTION = "caption";

        public static Uri buildFeedsUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
