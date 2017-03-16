package io.wyntr.peepster.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.wyntr.peepster.adapters.FriendsAdapter;
import io.wyntr.peepster.R;


public class PostsVideoView extends Activity {

    private static final String TAG = "VideoActivity";
    VideoView mVideoView = null;
    String phone;
    Button Send;
    String senderId;
    String videoID;
    private SlidingUpPanelLayout mLayout;
    ListView listView;
    FriendsAdapter adapter;
    String view;
    int countobjects;
    ImageButton delete;
   // ParseQueryAdapter<ParseCommentsClass> commentsQueryAdapter;
    private Timer autoUpdate;
    String commentsObId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.posts_video_layout);
        mVideoView = (VideoView) findViewById(R.id.posts_videoView);
        mVideoView.setKeepScreenOn(true);
        delete = (ImageButton) findViewById(R.id.delete_video);

       /* delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(PostsVideoView.this)
                        .setIcon(R.drawable.ic_delete_forever_grey600_36dp)
                        .setTitle("Delete!")
                        .setMessage("Are you sure you want to delete this video?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ParseObject.createWithoutData("AroundMe", videoID).deleteEventually();
                                ParseObject.createWithoutData("Comments", commentsObId).deleteEventually();
                                finish();
                                // intent.putExtra("delete",po);
                            }

                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        }); */


        // Get the intent from ListViewAdapter
        Intent i = getIntent();
        // Get the intent from ListViewAdapter
        phone = i.getStringExtra("video");
        senderId = i.getStringExtra("userId");
        videoID = i.getStringExtra("objectId");
        view = i.getStringExtra("views");


        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(mVideoView);
        mVideoView.setVideoPath(phone);
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mVideoView.start();

            }
        });


        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
                    mp.setLooping(true);
                    mp.start();
                } else if (mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    mp.stop();
                    finish();
                }
            }
        });

        mLayout = (SlidingUpPanelLayout) findViewById(R.id.posts_sliding_layout);
        mLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                Log.i(TAG, "onPanelSlide, offset " + slideOffset);
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                Log.i(TAG, "onPanelStateChanged " + newState);
            }
        });
        mLayout.setFadeOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        });

//Set up a customized query
    /*    final ParseQueryAdapter.QueryFactory<ParseCommentsClass> factory =
                new ParseQueryAdapter.QueryFactory<ParseCommentsClass>() {
                    public ParseQuery<ParseCommentsClass> create() {
                        ParseQuery<ParseCommentsClass> query = ParseCommentsClass.getQuery();
                        query.whereEqualTo("videoId", videoID);
                        query.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
                        query.orderByDescending("createdAt");
                        query.include("User");
                        return query;
                    }
                };

        // Set up the query adapter
        commentsQueryAdapter = new ParseQueryAdapter<ParseCommentsClass>(this, factory) {
            @Override
            public View getItemView(final ParseCommentsClass post, View view, ViewGroup parent) {
                if (view == null) {
                    view = View.inflate(getContext(), R.layout.comments_layout, null);
                }
                ImageView ProfileView = (ImageView) view.findViewById(R.id.profilePic);
                TextView usernameView = (TextView) view.findViewById(R.id.name);
                TextView commentsView = (TextView) view.findViewById(R.id.comments);
                ParseFile profilePicture = (ParseFile) post.getUser().get("profilePicture");
                String ProfileThumb = String.valueOf(profilePicture.getUrl());
                Picasso.with(PostsVideoView.this)
                        .load(ProfileThumb)
                        .transform(new CircleTransform())
                        .resize(70, 70)
                        .centerCrop()
                        .into(ProfileView);

                usernameView.setText(post.getUser().getString("name"));
                commentsView.setText(post.getComment());
                commentsObId = post.getObjectId();

                return view;
            }
        };

        // Disable pagination, we'll manage the query limit ourselves
        // commentsQueryAdapter.setPaginationEnabled(false);
        listView = (ListView) findViewById(R.id.posts_comments_list);
        listView.setAdapter(commentsQueryAdapter);
        commentsQueryAdapter.setAutoload(true);
        commentsQueryAdapter.setPaginationEnabled(false);
    } */
    }


    @Override
    protected void onResume() {
        super.onResume();
        autoUpdate = new Timer();
        autoUpdate.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                       // commentsQueryAdapter.loadObjects();
                    }
                });
            }
        }, 0, 2000); // updates each 2 secs
    }


    @Override
    public void onBackPressed() {
        if (mLayout != null &&
                (mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED || mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)) {
            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else {
            super.onBackPressed();
        }
    }
}
