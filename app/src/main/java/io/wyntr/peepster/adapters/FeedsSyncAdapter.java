package io.wyntr.peepster.adapters;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import io.wyntr.peepster.R;
import io.wyntr.peepster.data.FeedsContract;
import io.wyntr.peepster.data.FeedsDataHelper;
import io.wyntr.peepster.models.Feeds;
import io.wyntr.peepster.utilities.Constants;
import io.wyntr.peepster.utilities.MyLocationListener;

import static android.content.Context.MODE_PRIVATE;
import static io.wyntr.peepster.utilities.Constants.GEO_POINTS;
import static io.wyntr.peepster.utilities.Constants.POSTS_STRING;

/**
 * Created by sagar on 11-02-2017.
 */

public class FeedsSyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = FeedsSyncAdapter.class.getSimpleName();
    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;
    public static final String ACTION_DATA_UPDATE = "io.wyntr.peepster.UPDATE";
    DatabaseReference mFirebaseRef;
    private FeedsDataHelper mOpenHelper;
    GeoFire geoFire;
    GeoQuery query;
    GeoLocation center;
    private Query mRef;
    private Class<Feeds> mModelClass;
    private List<Feeds> mModels;
    private List<String> mKeys = new ArrayList<>();
    private Map<String, Feeds> mModelKeys;
    ChildEventListener mListener;


    public FeedsSyncAdapter(Context context, boolean autoInitialize, Query mRef, Class<Feeds> mModelClass) {
        super(context, autoInitialize);
        this.mRef = mRef;
        this.mModelClass = mModelClass;
        mModels = new ArrayList<>();
        mModelKeys = new HashMap<>();
        mListener = this.mRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Feeds model = dataSnapshot.getValue(FeedsSyncAdapter.this.mModelClass);
                mModelKeys.put(dataSnapshot.getKey(), model);
                // Insert into the correct location, based on previousChildName
                if (previousChildName == null) {
                    mModels.add(0, model);
                } else {
                    Feeds previousModel = mModelKeys.get(previousChildName);
                    int previousIndex = mModels.indexOf(previousModel);
                    int nextIndex = previousIndex + 1;
                    if (nextIndex == mModels.size()) {
                        mModels.add(model);
                        mKeys.add(dataSnapshot.getKey());
                    } else {
                        mModels.add(nextIndex, model);
                        mKeys.add(dataSnapshot.getKey());
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                // One of the mModels changed. Replace it in our list and name mapping
                String modelName = dataSnapshot.getKey();
                Feeds oldModel = mModelKeys.get(modelName);
                Feeds newModel = dataSnapshot.getValue(FeedsSyncAdapter.this.mModelClass);
                int index = mModels.indexOf(oldModel);

                mModels.set(index, newModel);
                mModelKeys.put(modelName, newModel);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                // A model was removed from the list. Remove it from our list and the name mapping
                String modelName = dataSnapshot.getKey();
                Feeds oldModel = mModelKeys.get(modelName);
                mModels.remove(oldModel);
                mKeys.remove(dataSnapshot.getKey());
                mModelKeys.remove(modelName);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                // A model changed position in the list. Update our list accordingly
                String modelName = dataSnapshot.getKey();
                Feeds oldModel = mModelKeys.get(modelName);
                Feeds newModel = dataSnapshot.getValue(FeedsSyncAdapter.this.mModelClass);
                int index = mModels.indexOf(oldModel);
                mModels.remove(index);
                if (previousChildName == null) {
                    mModels.add(0, newModel);
                    mKeys.add(dataSnapshot.getKey());
                } else {
                    Feeds previousModel = mModelKeys.get(previousChildName);
                    int previousIndex = mModels.indexOf(previousModel);
                    int nextIndex = previousIndex + 1;
                    if (nextIndex == mModels.size()) {
                        mModels.add(newModel);
                        mKeys.add(dataSnapshot.getKey());
                    } else {
                        mModels.add(nextIndex, newModel);
                        mKeys.add(dataSnapshot.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("FirebaseListAdapter", "Listen was cancelled, no more updates will occur");
            }
        });
    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String s, ContentProviderClient contentProviderClient, SyncResult syncResult) {
        Log.d(TAG,"onPerformSync Started!");
        SharedPreferences prefs = getContext().getSharedPreferences(Constants.LOCATION_PREFERENCES, MODE_PRIVATE);
        double latitude = getLatitude(prefs, "Latitude", 18.5269552);
        double longitude = getLongitude(prefs, "Longitude", 73.8267434);

        mOpenHelper = new FeedsDataHelper(getContext());
        this.geoFire = new GeoFire(FirebaseDatabase.getInstance().getReference(GEO_POINTS));
        mFirebaseRef = FirebaseDatabase.getInstance().getReference(POSTS_STRING);
        if (latitude != 0 && longitude != 0){
            center = new GeoLocation(latitude, longitude);
            query = geoFire.queryAtLocation(center, 2);
            query.addGeoQueryEventListener(new GeoQueryEventListener() {

                @Override
                public void onKeyEntered(String key, GeoLocation location) {
                    DatabaseReference tempRef = mFirebaseRef.child(key);
                    tempRef.addValueEventListener(new ValueEventListener() {

                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String key = dataSnapshot.getKey();
                            if (!hasObject(key)) {
                                Log.d(TAG, "item added " + key);
                                addSingle(dataSnapshot);
                            } else{
                                update(dataSnapshot, key);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

                @Override
                public void onKeyExited(String key) {
                    remove(key);
                }

                @Override
                public void onKeyMoved(String key, GeoLocation location) {
                    DatabaseReference tempRef = mFirebaseRef.child(key);
                    tempRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String key = dataSnapshot.getKey();
                            if (!hasObject(key)) {
                                Log.d(TAG, "item added " + key);
                                addSingle(dataSnapshot);
                            } else{
                                update(dataSnapshot, key);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

                @Override
                public void onGeoQueryReady() {

                }

                @Override
                public void onGeoQueryError(DatabaseError error) {

                }
            });
        }

    }

    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    public static Account getSyncAccount(Context context) {
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));
        if ( null == accountManager.getPassword(newAccount) ) {
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        FeedsSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);
        syncImmediately(context);
    }

    double getLatitude(final SharedPreferences prefs, final String key, final double defaultValue) {
        if ( !prefs.contains(key))
            return defaultValue;
        return Double.longBitsToDouble(prefs.getLong(key, 0));
    }

    double getLongitude(final SharedPreferences prefs, final String key, final double defaultValue) {
        if ( !prefs.contains(key))
            return defaultValue;
        return Double.longBitsToDouble(prefs.getLong(key, 0));
    }


    private void remove(String key) {
        Feeds oldModel = mModelKeys.get(key);
        mModels.remove(oldModel);
        mKeys.remove(key);
        mModelKeys.remove(key);
        removeData(key);
    }

    private void addSingle(DataSnapshot snapshot) {
        Feeds model = snapshot.getValue(FeedsSyncAdapter.this.mModelClass);
        mModelKeys.put(snapshot.getKey(), model);
        mModels.add(model);
        mKeys.add(snapshot.getKey());
        populateData(model, snapshot.getKey());
    }

    private void update(DataSnapshot snapshot, String key) {
        Feeds oldModel = mModelKeys.get(key);
        Feeds newModel = snapshot.getValue(FeedsSyncAdapter.this.mModelClass);
        int index = mModels.indexOf(oldModel);
        if (index >= 0) {
            mModels.set(index, newModel);
            mModelKeys.put(key, newModel);
            updateData(newModel, key);
        }

    }

    private boolean hasObject(String id) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        String selectString = "SELECT * FROM " + FeedsContract.FeedsEntry.TABLE_NAME + " WHERE " + FeedsContract.FeedsEntry.COLUMN_POST_KEY + " =?";

        Cursor cursor = db.rawQuery(selectString, new String[] {id});
        boolean hasObject = false;
        if(cursor.moveToFirst()){
            hasObject = true;
            int count = 0;
            while(cursor.moveToNext()){
                count++;
            }
            Log.d(TAG, String.format("%d records found", count));
        }
        cursor.close();
        db.close();
        return hasObject;
    }

    private void populateData(Feeds model, String key) {
        Log.d(TAG, "Content Added!");
        try{
            ContentValues feedsValues = new ContentValues();
            feedsValues.put(FeedsContract.FeedsEntry.COLUMN_POST_KEY, key);
            feedsValues.put(FeedsContract.FeedsEntry.COLUMN_TIME_STAMP, (Long) model.getTimestamp());
            feedsValues.put(FeedsContract.FeedsEntry.COLUMN_THUMB, model.getThumb_url());
            feedsValues.put(FeedsContract.FeedsEntry.COLUMN_VIDEO, model.getVideo_url());
            feedsValues.put(FeedsContract.FeedsEntry.COLUMN_USER, model.getUser().getFull_name());
            feedsValues.put(FeedsContract.FeedsEntry.COLUMN_USER_ID, model.getUser().getUid());
            feedsValues.put(FeedsContract.FeedsEntry.COLUMN_POST_CAPTION, model.getText());
            feedsValues.put(FeedsContract.FeedsEntry.COLUMN_USER_PROFILE, model.getUser().getProfile_picture());
            getContext().getContentResolver().insert(FeedsContract.FeedsEntry.CONTENT_URI, feedsValues);
            updateWidget();
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    private void removeData(String key){
        Log.d(TAG, "Content Removed!");
        try{getContext().getContentResolver().delete(FeedsContract.FeedsEntry.CONTENT_URI,
                FeedsContract.FeedsEntry.COLUMN_POST_KEY + "=?",
                new String[] {key});
            updateWidget();
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    private void updateData(Feeds model, String key){
        Log.d(TAG, "Content Updated!");
        try{
            ContentValues feedsValues = new ContentValues();
            feedsValues.put(FeedsContract.FeedsEntry.COLUMN_POST_KEY, key);
            feedsValues.put(FeedsContract.FeedsEntry.COLUMN_TIME_STAMP, (Long) model.getTimestamp());
            feedsValues.put(FeedsContract.FeedsEntry.COLUMN_THUMB, model.getThumb_url());
            feedsValues.put(FeedsContract.FeedsEntry.COLUMN_VIDEO, model.getVideo_url());
            feedsValues.put(FeedsContract.FeedsEntry.COLUMN_USER, model.getUser().getFull_name());
            feedsValues.put(FeedsContract.FeedsEntry.COLUMN_USER_ID, model.getUser().getUid());
            feedsValues.put(FeedsContract.FeedsEntry.COLUMN_POST_CAPTION, model.getText());
            feedsValues.put(FeedsContract.FeedsEntry.COLUMN_USER_PROFILE, model.getUser().getProfile_picture());
            getContext().getContentResolver().update(FeedsContract.FeedsEntry.CONTENT_URI,
                    feedsValues,FeedsContract.FeedsEntry.COLUMN_POST_KEY + "=?",
                    new String[] {key});
            updateWidget();
        } catch (Exception e){
            e.printStackTrace();
        }


    }

    private void updateWidget(){
        Context context = getContext();
        Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATE)
                .setPackage(context.getPackageName());
        context.sendBroadcast(dataUpdatedIntent);
    }
}


