package io.wyntr.peepster.utilities;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import io.wyntr.peepster.adapters.FeedsSyncAdapter;
import io.wyntr.peepster.data.FeedsContract;
import io.wyntr.peepster.fragments.GeoFragment;

import static io.wyntr.peepster.utilities.Constants.LOCATION_PREFERENCES;

/**
 * Created by sagar on 18-02-2017.
 */

public class MyLocationListener implements android.location.LocationListener {

    private static final int QUARTER_MINUTE = 1000 * 150;
    public static String TAG = MyLocationListener.class.getName();
    private LocationManager locationManager;
    private boolean running = false;
    private Context context;
    private boolean paused;
    SharedPreferences sharedPreferences;
    private Location currentBestLocation;

    public MyLocationListener() {
    }

    public MyLocationListener(LocationManager locationManager, Context context) {
        this.locationManager = locationManager;
        this.context = context;
    }


    @Override
    public void onLocationChanged(Location location) {
        sharedPreferences = context.getSharedPreferences(LOCATION_PREFERENCES, Context.MODE_PRIVATE);
        if (currentBestLocation != null) {
            if (isBetterLocation(location, currentBestLocation)) {
                currentBestLocation = location;
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putLong("Latitude", Double.doubleToLongBits(currentBestLocation.getLatitude()));
                Log.d(TAG, "Updated to SharedPreferences: " + Double.doubleToLongBits(currentBestLocation.getLatitude()));
                editor.putLong("Longitude", Double.doubleToLongBits(currentBestLocation.getLongitude()));
                Log.d(TAG, "Updated to SharedPreferences: " + Double.doubleToLongBits(currentBestLocation.getLongitude()));
                editor.apply();
            }
        } else {
            currentBestLocation = location;
        }
    }

    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > QUARTER_MINUTE;
        boolean isSignificantlyOlder = timeDelta < -QUARTER_MINUTE;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        Log.d(TAG, "The status of the provider " + s + " has changed");
        if (i == 0) {
            ContentResolver.requestSync(FeedsSyncAdapter.getSyncAccount(context), FeedsContract.CONTENT_AUTHORITY, Bundle.EMPTY);
        } else if (i == 1) {
            ContentResolver.cancelSync(FeedsSyncAdapter.getSyncAccount(context), FeedsContract.CONTENT_AUTHORITY);
        } else {
            doStart();
        }
    }

    @Override
    public void onProviderEnabled(String s) {
        Log.d(TAG, "Location provider " + s + " has been enabled");
    }

    @Override
    public void onProviderDisabled(String s) {
        Log.d(TAG, "Location provider '" + s + "' disabled.");
    }


    public void doStart() {
        if (this.locationManager.getProvider(LocationManager.GPS_PROVIDER) != null) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER); //<5>
            if (location != null) {
                this.onLocationChanged(location); //
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, QUARTER_MINUTE, 0, this);
        }
        if (this.locationManager.getProvider(LocationManager.NETWORK_PROVIDER) != null) {
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER); //<5>
            if (location != null) {
                Log.d(TAG, location.toString());
                this.onLocationChanged(location); //
            }
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, QUARTER_MINUTE, 0, this);
        }
    }

    public void stop() {
        if (this.running) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            this.locationManager.removeUpdates(this);
            this.running = false;
        }
    }

    public Context getCtx() {
        return context;
    }
    public void setCtx(Context context) {
        this.context = context;
    }


    public void setLocationManager(LocationManager locationManager) {
        this.locationManager = locationManager;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public Location getCurrentBestLocation() {
        return currentBestLocation;
    }

}
