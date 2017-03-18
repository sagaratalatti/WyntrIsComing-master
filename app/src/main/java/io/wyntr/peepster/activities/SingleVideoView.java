package io.wyntr.peepster.activities;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import io.wyntr.peepster.R;
import io.wyntr.peepster.models.Comment;
import io.wyntr.peepster.models.Likes;
import io.wyntr.peepster.models.Users;
import io.wyntr.peepster.utilities.Constants;
import io.wyntr.peepster.utilities.FirebaseUtil;

public class SingleVideoView extends Activity {

    private static final String TAG = SingleVideoView.class.getSimpleName();

    private static final DecelerateInterpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();
    private static final AccelerateInterpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();

    VideoView mVideoView = null;
    String videoUrl;
    EditText CommentBox;
    Button Send;
    String videoID;
    String view;
    ImageView likedView;
    ProgressDialog progressDialog;
    FrameLayout likeViewContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.video_layout);
        likeViewContainer = (FrameLayout)findViewById(R.id.like_view_container);
        likedView = (ImageView)findViewById(R.id.liked_view);
        mVideoView = (VideoView) findViewById(R.id.videoView);
        CommentBox = (EditText) findViewById(R.id.comment_text);
        Send = (Button) findViewById(R.id.send_comment);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.loading_video));
        progressDialog.show();
        Intent i = getIntent();
        videoUrl = i.getStringExtra(Constants.INTENT_VIDEO);
        videoID = i.getStringExtra(Constants.KEY);

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
        mVideoView.setOnTouchListener(new View.OnTouchListener() {

            private GestureDetector gestureDetector = new GestureDetector(SingleVideoView.this, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    final String userKey = FirebaseUtil.getCurrentUserId();
                    final DatabaseReference postLikesRef = FirebaseUtil.getLikesRef();
                    postLikesRef.child(videoID).child(userKey).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            if (user == null) {
                                Toast.makeText(SingleVideoView.this, R.string.user_logged_out_error,
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Users author = FirebaseUtil.getUser();
                                Likes likes = new Likes(author);
                                postLikesRef.child(videoID).child(user.getUid()).setValue(likes, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError error, DatabaseReference firebase) {
                                        if (error != null) {
                                            Log.w(TAG, "Error posting like: " + error.getMessage());
                                            Toast.makeText(SingleVideoView.this, getString(R.string.error_post_like), Toast
                                                    .LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError firebaseError) {

                        }
                    });
                    likeViewContainer.setVisibility(View.VISIBLE);
                    likedView.setVisibility(View.VISIBLE);
                    likedView.setScaleY(0.1f);
                    likedView.setScaleX(0.1f);

                    AnimatorSet animatorSet = new AnimatorSet();

                    ObjectAnimator imgScaleUpYAnim = ObjectAnimator.ofFloat(likedView, "scaleY", 0.1f, 1f);
                    imgScaleUpYAnim.setDuration(300);
                    imgScaleUpYAnim.setInterpolator(DECELERATE_INTERPOLATOR);
                    ObjectAnimator imgScaleUpXAnim = ObjectAnimator.ofFloat(likedView, "scaleX", 0.1f, 1f);
                    imgScaleUpXAnim.setDuration(300);
                    imgScaleUpXAnim.setInterpolator(DECELERATE_INTERPOLATOR);

                    ObjectAnimator imgScaleDownYAnim = ObjectAnimator.ofFloat(likedView, "scaleY", 1f, 0f);
                    imgScaleDownYAnim.setDuration(300);
                    imgScaleDownYAnim.setInterpolator(ACCELERATE_INTERPOLATOR);

                    ObjectAnimator imgScaleDownXAnim = ObjectAnimator.ofFloat(likedView, "scaleX", 1f, 0f);
                    imgScaleDownXAnim.setDuration(300);
                    imgScaleDownXAnim.setInterpolator(ACCELERATE_INTERPOLATOR);

                    animatorSet.playTogether(imgScaleUpYAnim, imgScaleUpXAnim);

                    animatorSet.play(imgScaleDownYAnim).with(imgScaleDownXAnim).after(imgScaleUpYAnim);

                    animatorSet.start();
                    return super.onDoubleTap(e);

                }

                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                    // TODO Auto-generated method stub
                    //Defining Sensitivity
                    float sensitivity = 50;
                    //Swipe Up Check
                    if (e1.getY() - e2.getY() > sensitivity) {
                        CommentBox.setVisibility(View.VISIBLE);
                    }
                    return true;
                }
            });


            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
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

                                                     final String userKey = FirebaseUtil.getCurrentUserId();
                                                     Users author = FirebaseUtil.getUser();
                                                     Likes likes = new Likes(author);
                                                     FirebaseUtil.getViewsRef().child(videoID).child(userKey).setValue(likes, new DatabaseReference.CompletionListener() {
                                                         @Override
                                                         public void onComplete(DatabaseError error, DatabaseReference firebase) {
                                                             if (error != null) {
                                                                 Log.w(TAG, "Error posting like: " + error.getMessage());
                                                             }
                                                         }
                                                     });

                                                     if (CommentBox.hasFocus()) {
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
                            (InputMethodManager) SingleVideoView.this.
                                    getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(
                            CommentBox.getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user == null) {
                        Toast.makeText(SingleVideoView.this, R.string.user_logged_out_error,
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
                                Toast.makeText(SingleVideoView.this, getString(R.string.error_post_comment), Toast
                                        .LENGTH_SHORT).show();
                                CommentBox.setText(commentText);
                            } else {
                               Toast.makeText(SingleVideoView.this, getString(R.string.comment_success), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
        });
    }
}
