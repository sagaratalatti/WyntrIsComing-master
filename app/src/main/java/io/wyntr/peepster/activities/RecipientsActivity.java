package io.wyntr.peepster.activities;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.LocationManager;
import android.media.Image;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.firebase.database.DatabaseReference;
import com.tapadoo.alerter.Alerter;

import java.util.List;

import io.wyntr.peepster.AppStatus;
import io.wyntr.peepster.R;
import io.wyntr.peepster.fragments.UploadFragment;
import io.wyntr.peepster.utilities.FileHelper;
import io.wyntr.peepster.utilities.FirebaseUtil;
import io.wyntr.peepster.utilities.MyLocationListener;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class RecipientsActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks, UploadFragment.TaskCallbacks{

    public static final String TAG = RecipientsActivity.class.getSimpleName();
    private static final int LOCATION_PERMISSIONS = 6;
    private static final String[] locationPermissions = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION
    };
    public static final String TAG_TASK_FRAGMENT = "UploadFragment";
    private static final int THUMBNAIL_MAX_DIMENSION = 640;
    byte[] fileBytes;
    public static Uri mMediaUri;
    VideoView mVideoView = null;
    private UploadFragment mTaskFragment;
    EditText Caption;
    private Bitmap mThumbnail;
    Button shareAround;
    Activity mActivity;
    MyLocationListener myLocationListener;
    private LocationManager locationManager;
    ImageView cancelPost;
    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.uploading_post));
        progressDialog.setCancelable(false);
        setContentView(R.layout.activity_recipients);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        myLocationListener = new MyLocationListener(locationManager, this);
        mVideoView = (VideoView) findViewById(R.id.recipients_videoView);
        mVideoView.setKeepScreenOn(true);
        mVideoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Caption.setVisibility(View.VISIBLE);
            }
        });

        cancelPost = (ImageView)findViewById(R.id.cancel_post);
        cancelPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getParentActivityIntent();
                finish();
            }
        });
        FragmentManager fm = getSupportFragmentManager();
        mTaskFragment = (UploadFragment) fm.findFragmentByTag(TAG_TASK_FRAGMENT);

        // create the fragment and data the first time
        if (mTaskFragment == null) {
            // add the fragment
            mTaskFragment = new UploadFragment();
            fm.beginTransaction().add(mTaskFragment, TAG_TASK_FRAGMENT).commit();
        }

        shareAround = (Button) findViewById(R.id.share_round_button);
        shareAround.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (AppStatus.getInstance(getApplicationContext()).isOnline() &&
                        AppStatus.getInstance(getApplicationContext()).isLocationAvailable(locationManager)){
                    send();
                } else if (!AppStatus.getInstance(getApplicationContext()).isOnline()){
                    Alerter.create(mActivity)
                            .setTitle(getString(R.string.no_internet_connection))
                            .setIcon(R.drawable.ic_access_point_network_white_24dp)
                            .setBackgroundColor(R.color.colorAccent)
                            .show();
                } else if (!AppStatus.getInstance(getApplicationContext()).isLocationAvailable(locationManager)){
                    Alerter.create(mActivity)
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

            }
        });

        Caption = (EditText) findViewById(R.id.caption);

        mMediaUri = getIntent().getData();
        fileBytes = FileHelper.getByteArrayFromFile(this, mMediaUri);
        mThumbnail = getIntent().getParcelableExtra("image");
        Bitmap thumbnail = mTaskFragment.getThumbnail();
        if (thumbnail != null) {
            mThumbnail = thumbnail;
        }

        mVideoView.setOnTouchListener(new View.OnTouchListener() {

            private GestureDetector gestureDetector = new GestureDetector(RecipientsActivity.this, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                    // TODO Auto-generated method stub
                    //Defining Sensitivity
                    float sensitivity = 50;
                    //Swipe Up Check
                    if (e1.getY() - e2.getY() > sensitivity) {
                        Caption.setVisibility(View.VISIBLE);
                    }
                    return true;
                }
            });
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                gestureDetector.onTouchEvent(motionEvent);
                return true;
            }
        });

        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(mVideoView);
        mVideoView.setVideoPath(String.valueOf(mMediaUri));
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mVideoView.start();

            }
        });

        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.setLooping(true);
                mp.start();
            }
        });
    }

    @AfterPermissionGranted(LOCATION_PERMISSIONS)
    protected void send() {
        if (EasyPermissions.hasPermissions(this, locationPermissions)){
            myLocationListener.doStart();
            String postText = Caption.getText().toString();

            if (TextUtils.isEmpty(postText)) {
                postText = "";
            }
            Long timestamp = System.currentTimeMillis();
            String bitmapPath = "/" + FirebaseUtil.getCurrentUserId() + "/video/" + timestamp.toString() + "/";
            String thumbnailPath = "/" + FirebaseUtil.getCurrentUserId() + "/thumb/" + timestamp.toString() + "/";
            if (myLocationListener.getCurrentBestLocation() != null) {
                mTaskFragment.uploadPost(fileBytes, bitmapPath, mThumbnail, thumbnailPath, mMediaUri.getLastPathSegment(),
                        postText, myLocationListener.getCurrentBestLocation().getLatitude(), myLocationListener.getCurrentBestLocation().getLongitude());
            }
            progressDialog.show();
            shareAround.setEnabled(false);
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.recipients_location_permission),
                    LOCATION_PERMISSIONS, locationPermissions);
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        Log.d(TAG, "onPermissionsGranted:" + requestCode + ":" + perms.size());
        if (myLocationListener == null) {
            myLocationListener = new MyLocationListener();
            myLocationListener.setLocationManager(locationManager);
            myLocationListener.setCtx(this);
            myLocationListener.setPaused(false);

            myLocationListener.doStart();
        }
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

    @Override
    public void onBitmapResized(Bitmap resizedBitmap, int mMaxDimension) {
        if (resizedBitmap == null) {
            Toast.makeText(getApplicationContext(), "Couldn't resize bitmap.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (mMaxDimension == THUMBNAIL_MAX_DIMENSION) {
            mThumbnail = resizedBitmap;
        }
        if (mThumbnail != null) {
            shareAround.setEnabled(true);
        }
    }

    @Override
    public void onPostUploaded(final String error) {
        RecipientsActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                shareAround.setEnabled(true);
                if (error == null) {
                    progressDialog.dismiss();
                    Toast.makeText(RecipientsActivity.this, "Shared Around!", Toast.LENGTH_SHORT).show();
                    shareAround.setEnabled(false);
                    finish();
                } else {
                    Toast.makeText(RecipientsActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        Caption.setVisibility(View.GONE);
    }
}

