package io.wyntr.peepster.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;

/**
 * Created by sagar on 11-02-2017.
 */

public class FeedsProvider extends ContentProvider {

    private static final String TAG = FeedsProvider.class.getSimpleName();

    private FeedsDataHelper mOpenHelper;

    @Override
    public boolean onCreate() {
        mOpenHelper = new FeedsDataHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        retCursor = mOpenHelper.getReadableDatabase().query(
                FeedsContract.FeedsEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return FeedsContract.FeedsEntry.CONTENT_TYPE;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Uri returnUri;
        convertData(contentValues);
        long _id = db.insert(FeedsContract.FeedsEntry.TABLE_NAME, null, contentValues);
        if (_id > 0)
            returnUri = FeedsContract.FeedsEntry.buildFeedsUri(_id);
        else
            throw new android.database.SQLException("Failed to insert row into " + uri);
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rowsDeleted;
        if (null == selection) selection = "1";
        rowsDeleted = db.delete(
                FeedsContract.FeedsEntry.TABLE_NAME, selection, selectionArgs);
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rowsUpdated;
        convertData(contentValues);
        rowsUpdated = db.update(FeedsContract.FeedsEntry.TABLE_NAME, contentValues, selection,
                selectionArgs);
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransaction();
        int returnCount = 0;
        try {
            for (ContentValues value : values) {
                convertData(value);
                long _id = db.insert(FeedsContract.FeedsEntry.TABLE_NAME, null, value);
                if (_id != -1) {
                    returnCount++;
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnCount;
    }

    @Override
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }

    private void convertData(ContentValues values) {
        if (values.containsKey(FeedsContract.FeedsEntry.COLUMN_TIME_STAMP)) {
            long dateValue = values.getAsLong(FeedsContract.FeedsEntry.COLUMN_TIME_STAMP);
            values.put(FeedsContract.FeedsEntry.COLUMN_TIME_STAMP, String.valueOf(FeedsContract.normalizeDate(dateValue)));
        }
    }

}
