package io.wyntr.peepster.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import io.wyntr.peepster.R;
import io.wyntr.peepster.adapters.CommentsQueryAdapter;
import io.wyntr.peepster.adapters.UserCommentsQueryAdapter;
import io.wyntr.peepster.utilities.DividerItemDecoration;
import io.wyntr.peepster.viewholders.CommentsViewHolder;
import io.wyntr.peepster.models.Comment;
import io.wyntr.peepster.models.Users;
import io.wyntr.peepster.utilities.Constants;
import io.wyntr.peepster.utilities.FirebaseUtil;
import io.wyntr.peepster.viewholders.PostViewHolder;
import io.wyntr.peepster.viewholders.UserCommentsViewHolder;

import static io.wyntr.peepster.utilities.Constants.COMMENTS_STRING;

/**
 * Created by sagar on 27-02-2017.
 */

public class UserVideoView extends AppCompatActivity {

    private static final String TAG = UserVideoView.class.getSimpleName();

    VideoView mVideoView;
    String videoUrl;
    EditText CommentBox;
    Button Send;
    String videoID;
    String view;
    TextView heartsCount, viewsCount;
    ProgressDialog progressDialog;
    SlidingUpPanelLayout commentsPanel;
    DatabaseReference mFirebaseRef;
    RecyclerView mRecyclerView;
    ValueEventListener likeListener, viewsListener;
    UserCommentsQueryAdapter mAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.user_video_layout);
        mFirebaseRef = FirebaseDatabase.getInstance().getReference(COMMENTS_STRING);
        mFirebaseRef.keepSynced(true);
        mVideoView = (VideoView) findViewById(R.id.users_videoView);
        heartsCount = (TextView) findViewById(R.id.users_hearts_count);
        viewsCount = (TextView) findViewById(R.id.users_views_count);
        commentsPanel = (SlidingUpPanelLayout) findViewById(R.id.users_sliding_layout);
        commentsPanel.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                Log.i(TAG, "onPanelSlide, offset " + slideOffset);
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                Log.i(TAG, "onPanelStateChanged " + newState);
            }
        });
        CommentBox = (EditText) findViewById(R.id.users_comments_box);
        Send = (Button) findViewById(R.id.users_post_comment);
        CommentBox.setVisibility(View.GONE);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.loading_video));
        progressDialog.setCancelable(false);
        progressDialog.show();
        Intent i = getIntent();
        videoUrl = i.getStringExtra(Constants.INTENT_VIDEO);
        videoID = i.getStringExtra(Constants.KEY);
        mRecyclerView = (RecyclerView)findViewById(R.id.users_recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        Drawable dividerLine = ContextCompat.getDrawable(this, R.drawable.divider_line);
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(dividerLine);
        mRecyclerView.addItemDecoration(itemDecoration);
        mAdapter = new UserCommentsQueryAdapter(mFirebaseRef.child(videoID), R.layout.users_comments_item, this, UserCommentsViewHolder.class);
        loadComments();
        updateCounts();

        CommentBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() != 0)
                    Send.setVisibility(View.VISIBLE);
                if (s.length() == 0) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            CommentBox.setVisibility(View.GONE);
                            Send.setVisibility(View.GONE);
                        }
                    }, 3000);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

                if (s.length() == 0) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            CommentBox.setVisibility(View.GONE);
                            Send.setVisibility(View.GONE);

                        }
                    }, 3000);
                }

            }
        });

        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(mVideoView);
        mVideoView.setVideoPath(videoUrl);
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener()

                                         {
                                             @Override
                                             public void onPrepared(final MediaPlayer mp) {
                                                 mVideoView.start();

                                                 mp.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                                                     @Override
                                                     public void onVideoSizeChanged(final MediaPlayer mp, int width, int height) {
                                                         progressDialog.dismiss();

                                                     }
                                                 });
                                                 if (mVideoView.isPlaying()) {
                                                     mp.setLooping(false);

                                                     if (commentsPanel.isShown()) {
                                                         mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                                             @Override
                                                             public void onCompletion(MediaPlayer mp) {
                                                                 mp.start();
                                                                 mp.setLooping(true);
                                                             }
                                                         });
                                                     }
                                                 }
                                             }

                                         }

        );

        CommentBox.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            mp.start();
                            mp.setLooping(true);
                        }
                    });
                } else if (!hasFocus) {
                    mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener()

                                                       {
                                                           @Override
                                                           public void onCompletion(MediaPlayer mp1) {
                                                               mp1.setLooping(false);
                                                               finish();
                                                           }
                                                       }

                    );
                }
            }
        });

        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                               @Override
                                               public void onCompletion(MediaPlayer mp) {
                                                   mp.stop();
                                                   finish();
                                               }
                                           }
        );





        Send.setVisibility(View.GONE);
        Send.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View v) {
                CommentBox.clearFocus();
                Send.setVisibility(View.GONE);
                CommentBox.setVisibility(View.GONE);
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                // create the new user!
                final Editable commentText = CommentBox.getText();
                CommentBox.setText("");
                InputMethodManager inputManager =
                        (InputMethodManager) UserVideoView.this.
                                getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(
                        CommentBox.getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null) {
                    Toast.makeText(UserVideoView.this, R.string.user_logged_out_error,
                            Toast.LENGTH_SHORT).show();
                }
                final DatabaseReference commentsRef = FirebaseUtil.getCommentsRef().child(videoID);
                Users author = FirebaseUtil.getUser();
                Comment comment = new Comment(author, commentText.toString(),
                        ServerValue.TIMESTAMP);
                commentsRef.push().setValue(comment, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError error, DatabaseReference firebase) {
                        if (error != null) {
                            Log.w(TAG, "Error posting comment: " + error.getMessage());
                            Toast.makeText(UserVideoView.this, "Error posting comment.", Toast
                                    .LENGTH_SHORT).show();
                            CommentBox.setText(commentText);
                        } else {
                            Toast.makeText(UserVideoView.this, getString(R.string.comment_success), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    private void loadComments(){
        if (videoID != null){
            mFirebaseRef.child(videoID).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Log.d(TAG, "onChildAdded: " + dataSnapshot.getKey());
                    DatabaseReference tempRef = mFirebaseRef.child(videoID).child(dataSnapshot.getKey());
                    tempRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String key = dataSnapshot.getKey();
                            if (!mAdapter.exists(key)) {
                                Log.d(TAG, "item added " + key);
                                mAdapter.addSingle(dataSnapshot);
                                mAdapter.notifyDataSetChanged();
                            } else {
                                //...otherwise I will update the record
                                Log.d(TAG, "item updated: " + key);
                                mAdapter.update(dataSnapshot, key);
                                mAdapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    mAdapter.remove(dataSnapshot.getKey());
                    mAdapter.notifyDataSetChanged();
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            bindRecyclerView();
        }
    }

    public void bindRecyclerView() {
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        commentsPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
    }

    @Override
    public void onStop(){
        super.onStop();
        if (progressDialog.isShowing()){
            progressDialog.hide();
        }
    }

    private void updateCounts(){
        likeListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                heartsCount.setText(String.valueOf(dataSnapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        viewsListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                viewsCount.setText(String.valueOf(dataSnapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        FirebaseUtil.getLikesRef().child(videoID).addValueEventListener(likeListener);
        FirebaseUtil.getViewsRef().child(videoID).addValueEventListener(viewsListener);
    }

}
