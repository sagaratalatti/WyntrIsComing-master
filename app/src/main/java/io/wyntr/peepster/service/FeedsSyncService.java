package io.wyntr.peepster.service;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import io.wyntr.peepster.R;
import io.wyntr.peepster.adapters.FeedsSyncAdapter;
import io.wyntr.peepster.models.Feeds;
import io.wyntr.peepster.utilities.Constants;
import io.wyntr.peepster.utilities.MyLocationListener;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

import static io.wyntr.peepster.utilities.Constants.POSTS_STRING;

/**
 * Created by sagar on 11-02-2017.
 */

public class FeedsSyncService extends Service{

    private static final String TAG = FeedsSyncService.class.getSimpleName();

    private static final Object sSyncAdapterLock = new Object();
    private static FeedsSyncAdapter mSyncAdapter = null;
    DatabaseReference mFirebaseRef;

    @Override
    public void onCreate() {
        mFirebaseRef = FirebaseDatabase.getInstance().getReference(POSTS_STRING);
        synchronized (sSyncAdapterLock) {
            if (mSyncAdapter == null) {
                mSyncAdapter = new FeedsSyncAdapter(getApplicationContext(), true, mFirebaseRef.equalTo(Constants.GEO_POINTS), Feeds.class);
                FeedsSyncAdapter.initializeSyncAdapter(this);
                FeedsSyncAdapter.syncImmediately(this);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mSyncAdapter.getSyncAdapterBinder();
    }
}
