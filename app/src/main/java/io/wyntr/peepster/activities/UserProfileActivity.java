package io.wyntr.peepster.activities;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.wyntr.peepster.R;
import io.wyntr.peepster.adapters.GridQueryAdapter;
import io.wyntr.peepster.models.People;
import io.wyntr.peepster.utilities.Constants;
import io.wyntr.peepster.utilities.FirebaseUtil;
import io.wyntr.peepster.utilities.GlideUtil;
import io.wyntr.peepster.viewholders.GridViewHolder;

import static io.wyntr.peepster.utilities.Constants.POSTS_STRING;


public class UserProfileActivity extends AppCompatActivity {

    private static final String TAG = UserProfileActivity.class.getSimpleName();

    ImageView profile;
    ValueEventListener mPersonInfoListener;
    private RecyclerView mRecyclerGrid;
    private GridQueryAdapter mGridAdapter;
    String userKey;
    DatabaseReference mPeopleRef;
    DatabaseReference mUserRef;
    private ValueEventListener mFollowingListener;
    DatabaseReference mPostsRef;
    private static final int GRID_NUM_COLUMNS = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myactivity);
        profile = (ImageView) findViewById(R.id.backdrop);
        mPostsRef = FirebaseUtil.getPostsRef();
        mPostsRef.keepSynced(true);
        mGridAdapter = new GridQueryAdapter(mPostsRef.equalTo(POSTS_STRING), this, GridViewHolder.class);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        mPeopleRef = FirebaseUtil.getPeopleRef();
        userKey = getIntent().getStringExtra(Constants.USER_ID);

        final FloatingActionButton followUserFab = (FloatingActionButton) findViewById(R.id
                .follow_user_fab);
        mFollowingListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        followUserFab.setImageResource(R.drawable.ic_account_remove_white_18dp);
                        loadPosts();
                    }
                } else {
                    followUserFab.setImageResource(R.drawable.ic_account_plus_white_18dp);
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {

            }
        };
        mUserRef = FirebaseUtil.getPeopleRef().child(userKey);
        mPersonInfoListener = mUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                People person = dataSnapshot.getValue(People.class);
                if (person.getPhotoUrl() != null){
                    GlideUtil.loadImage(person.getPhotoUrl(), profile);
                }

                String name = person.getDisplayName();
                if (name == null) {
                    name = getString(R.string.user_info_no_name);
                }
                collapsingToolbar.setTitle(name);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        final String currentUserId = FirebaseUtil.getCurrentUserId();
        if (currentUserId != null) {
            mPeopleRef.child(currentUserId).child("following").child(userKey)
                    .addValueEventListener(mFollowingListener);
        }
        followUserFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentUserId == null) {
                    Toast.makeText(UserProfileActivity.this, getString(R.string.follow_someone),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                // TODO: Convert these to actually not be single value, for live updating when
                // current user follows.
                mPeopleRef.child(currentUserId).child("following").child(userKey).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Map<String, Object> updatedUserData = new HashMap<>();
                        if (dataSnapshot.exists()) {
                            // Already following, need to unfollow
                            FirebaseUtil.getCurrentUserRef().child("followers").child(currentUserId).removeValue();
                            updatedUserData.put("people/" + currentUserId + "/following/" + userKey, null);
                            updatedUserData.put("followers/" + userKey + "/" + currentUserId, null);
                            mGridAdapter.cleanup();
                            mGridAdapter.notifyDataSetChanged();
                        } else {
                            FirebaseUtil.getCurrentUserRef().child("followers").setValue(currentUserId, ServerValue.TIMESTAMP);
                            updatedUserData.put("people/" + currentUserId + "/following/" + userKey, true);
                            updatedUserData.put("followers/" + userKey + "/" + currentUserId, true);
                            loadPosts();
                        }
                        FirebaseUtil.getBaseRef().updateChildren(updatedUserData, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError firebaseError, DatabaseReference firebase) {
                                if (firebaseError != null) {
                                    Toast.makeText(UserProfileActivity.this, R.string
                                            .follow_user_error, Toast.LENGTH_LONG).show();
                                    Log.d(TAG, getString(R.string.follow_user_error) + "\n" +
                                            firebaseError.getMessage());
                                }
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError firebaseError) {

                    }
                });
            }
        });

        mRecyclerGrid = (RecyclerView) findViewById(R.id.user_posts_list);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, GRID_NUM_COLUMNS);
        mRecyclerGrid.setLayoutManager(gridLayoutManager);
    }

    private void loadPosts(){
        FirebaseUtil.getPeopleRef().child(userKey).child(POSTS_STRING).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
              FirebaseUtil.getPostsRef().child(dataSnapshot.getKey()).addValueEventListener(new ValueEventListener() {
                  @Override
                  public void onDataChange(DataSnapshot dataSnapshot) {
                      String key = dataSnapshot.getKey();
                      Log.d(TAG, "item added " + key);
                      if (!mGridAdapter.exists(key)) {
                          Log.d(TAG, "item added " + key);
                          mGridAdapter.addSingle(dataSnapshot);
                          mGridAdapter.notifyDataSetChanged();
                      } else {
                          //...otherwise I will update the record
                          Log.d(TAG, "item updated: " + key);
                          mGridAdapter.update(dataSnapshot, key);
                          mGridAdapter.notifyDataSetChanged();
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
                mGridAdapter.remove(dataSnapshot.getKey());
                mGridAdapter.notifyDataSetChanged();
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

    private void bindRecyclerView() {
        mRecyclerGrid.setAdapter(mGridAdapter);
    }



    @Override
    protected void onDestroy() {
        if (FirebaseUtil.getCurrentUserId() != null) {
            mPeopleRef.child(FirebaseUtil.getCurrentUserId()).child("following").child(userKey)
                    .removeEventListener(mFollowingListener);
        }
        super.onDestroy();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}
