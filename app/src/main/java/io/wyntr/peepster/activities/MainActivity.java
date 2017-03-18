package io.wyntr.peepster.activities;

import android.Manifest;
import android.accounts.Account;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.LocationManager;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.tapadoo.alerter.Alerter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;

import io.wyntr.peepster.AppStatus;
import io.wyntr.peepster.adapters.SectionsPagerAdapter;
import io.wyntr.peepster.fragments.*;
import io.wyntr.peepster.R;
import io.wyntr.peepster.service.FeedsSyncService;
import io.wyntr.peepster.utilities.MyLocationListener;
import io.wyntr.peepster.video.MediaController;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {


    private static final int RESULT_CODE_COMPRESS_VIDEO = 2;
    public static final int MEDIA_TYPE_IMAGE = 3;
    public static final int MEDIA_TYPE_VIDEO = 4;
    public static final int STORAGE_PERMISSIONS = 7;
    private static final int LOCATION_PERMISSIONS = 6;
    private static final String TAG = MainActivity.class.getSimpleName();
    private ProgressBar progressBar;
    public Uri uri;
    File mediaFile;
    public Uri senduri;
    Bitmap thumb;
    Account accounts;
    LocationManager locationManager;
    MyLocationListener myLocationListener;
    ViewPager mViewPager;
    SectionsPagerAdapter mSectionsPagerAdapter;
    private static final String[] locationPermissions = new String[]{
            android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION
    };

    private static final String[] storagePermission = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        setupViewPager(mViewPager);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        myLocationListener = new MyLocationListener(locationManager, this);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        if (!AppStatus.getInstance(this).isOnline()){
            Alerter.create(this)
                    .setTitle(getString(R.string.no_internet_connection))
                    .setIcon(R.drawable.ic_access_point_network_white_24dp)
                    .setBackgroundColor(R.color.colorAccent)
                    .show();
        }

        if (!AppStatus.getInstance(this).isLocationAvailable(locationManager)){
            Alerter.create(this)
                    .setTitle(getString(R.string.no_location_found))
                    .setText(getString(R.string.enable_location_settings))
                    .setIcon(R.drawable.ic_crosshairs_gps_white_24dp)
                    .setBackgroundColor(R.color.colorAccent)
                    .setDuration(10000)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(myIntent);
                        }
                    })
                    .show();
        }

        if (AppStatus.getInstance(this).isOnline() && AppStatus.getInstance(this).isLocationAvailable(locationManager)){
            startSyncService();
        }

    }


    public void openMapsActivity(View view){
        view.getId();
        if (EasyPermissions.hasPermissions(this, locationPermissions)){
            Intent intent = new Intent(MainActivity.this, CustomClustering.class);
            startActivity(intent);
            finish();
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.recipients_location_permission),
                    LOCATION_PERMISSIONS, locationPermissions);
        }
    }

    public void openCamera(View view){
        view.getId();
        if (EasyPermissions.hasPermissions(this, storagePermission)){
            Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            uri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);
            if (uri == null) {
                // display an error
                Toast.makeText(MainActivity.this, R.string.error_external_storage,
                        Toast.LENGTH_LONG).show();
            } else {
                videoIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                videoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10);
                videoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                // 0 = lowest res

                startActivityForResult(videoIntent, RESULT_CODE_COMPRESS_VIDEO);

            }
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.storage_permission_request),
                    STORAGE_PERMISSIONS, storagePermission);
        }
    }

    public void buttonOnClick(View view) {
        switch (view.getId()) {

            case R.id.account_icon:
                Intent intent = new Intent(MainActivity.this, MyActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;

            case R.id.friends_icon:
                intent = new Intent(MainActivity.this, AddFriends.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
        }
    }


    private void setupViewPager(ViewPager mViewPager) {
        mSectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        mSectionsPagerAdapter.addFragment(new GeoFragment(), getString(R.string.around_you));
        mSectionsPagerAdapter.addFragment(new FeedsFragment(), getString(R.string.following));
        mViewPager.setAdapter(mSectionsPagerAdapter);
    }

    private Uri getOutputMediaFileUri(int mediaType) {
        if (isExternalStorageAvailable()) {
            String appName = MainActivity.this.getString(R.string.app_name);
            File mediaStorageDir = new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    appName);

            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.e(TAG, "Failed to create directory.");
                    return null;
                }
            }

            String path = mediaStorageDir.getPath() + File.separator;
            if (mediaType == MEDIA_TYPE_IMAGE) {
                mediaFile = new File(path + "thumb" + ".jpg");
            } else if (mediaType == MEDIA_TYPE_VIDEO) {
                mediaFile = new File(path + "video" + ".mp4");
            } else {
                return null;
            }

            Log.d(TAG, "File: " + Uri.fromFile(mediaFile));

            // 5. Return the file's URI
            return Uri.fromFile(mediaFile);

        } else {
            return null;
        }
    }

    private boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();

        if (state.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        myLocationListener.stop();
    }


    protected void onActivityResult(int reqCode, int resCode, Intent data) {
        if (reqCode == RESULT_CODE_COMPRESS_VIDEO) {
            if (resCode == RESULT_OK) {
                if (uri != null) {
                    compress();
                }
            } else if (resCode == RESULT_CANCELED) {
                Toast.makeText(this, getString(R.string.cancel_recording), Toast.LENGTH_SHORT).show();
            }

        }
    }

    @AfterPermissionGranted(LOCATION_PERMISSIONS)
    private void startSyncService(){
        if (EasyPermissions.hasPermissions(this, locationPermissions)){
            Intent intent = new Intent(MainActivity.this, FeedsSyncService.class);
            startService(intent);
            myLocationListener.doStart();
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.recipients_location_permission),
                    LOCATION_PERMISSIONS, locationPermissions);
        }
    }


    private void deleteTempFile() {
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File file = new File(path + "/" + "Wyntr-Beta" + "/" + "video.mp4");
        boolean delete = file.delete();
        Log.v("log_tag", "deleted: " + delete);
    }

    public void compress() {
        MediaController.getInstance().scheduleVideoConvert(mediaFile.getPath());
        new VideoCompressor().execute();
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        Log.d(TAG, "onPermissionsGranted:" + requestCode + ":" + perms.size());
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


    class VideoCompressor extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            Log.d(TAG, "Start video compression");
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            return MediaController.getInstance().convertVideo(mediaFile.getPath());
        }

        @Override
        protected void onPostExecute(Boolean compressed) {
            super.onPostExecute(compressed);
            senduri = MediaController.finalUri;
            if (senduri != null) {

            } else
                Toast.makeText(getApplicationContext(), getString(R.string.video_file_not_found), Toast.LENGTH_SHORT).show();
            thumb = ThumbnailUtils.createVideoThumbnail(senduri.getPath(), MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            progressBar.setVisibility(View.GONE);
            if (compressed) {
                Toast.makeText(MainActivity.this, getString(R.string.video_compressed), Toast.LENGTH_SHORT).show();
               deleteTempFile();
            }
            Intent recipientsIntent = new Intent(MainActivity.this, RecipientsActivity.class);
            recipientsIntent.setData(senduri);
            recipientsIntent.putExtra("image", thumb);
            startActivity(recipientsIntent);
        }
    }


}