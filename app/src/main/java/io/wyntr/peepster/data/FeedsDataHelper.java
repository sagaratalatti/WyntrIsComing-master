package io.wyntr.peepster.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by sagar on 11-02-2017.
 */

public class FeedsDataHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 3;



    private static final String DATABASE_NAME = "wyntr.db";


    public FeedsDataHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_FEEDS_TABLE = "CREATE TABLE " + FeedsContract.FeedsEntry.TABLE_NAME + " (" +
                FeedsContract.FeedsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                FeedsContract.FeedsEntry.COLUMN_USER + " TEXT NOT NULL, " +
                FeedsContract.FeedsEntry.COLUMN_USER_ID + " TEXT NOT NULL, " +
                FeedsContract.FeedsEntry.COLUMN_VIDEO + " TEXT NOT NULL, " +
                FeedsContract.FeedsEntry.COLUMN_TIME_STAMP + " TEXT NOT NULL, " +
                FeedsContract.FeedsEntry.COLUMN_THUMB + " TEXT NOT NULL, " +
                FeedsContract.FeedsEntry.COLUMN_POST_KEY + " TEXT NOT NULL, " +
                FeedsContract.FeedsEntry.COLUMN_POST_CAPTION + " TEXT, " +
                FeedsContract.FeedsEntry.COLUMN_USER_PROFILE + " TEXT NOT NULL" +
                ");";

        sqLiteDatabase.execSQL(SQL_CREATE_FEEDS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + FeedsContract.FeedsEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
