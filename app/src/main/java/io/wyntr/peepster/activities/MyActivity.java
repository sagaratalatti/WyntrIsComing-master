package io.wyntr.peepster.activities;

import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import io.wyntr.peepster.R;
import io.wyntr.peepster.adapters.ProfileQueryAdapter;
import io.wyntr.peepster.viewholders.ProfileViewHolder;
import io.wyntr.peepster.models.People;
import io.wyntr.peepster.utilities.FirebaseUtil;
import io.wyntr.peepster.utilities.GlideUtil;

import static io.wyntr.peepster.utilities.Constants.POSTS_STRING;

/**
 * Created by sagar on 27-02-2017.
 */

public class MyActivity extends AppCompatActivity {

    private static final String TAG = MyActivity.class.getSimpleName();

    ImageView profile;
    CollapsingToolbarLayout collapsingToolbar;
    ProfileQueryAdapter mAdapter;
    String currentUserId;
    DatabaseReference mPeopleRef;
    RecyclerView mRecyclerView;
    ChildEventListener mPersonRef;
    DatabaseReference mPostsRef;
    ValueEventListener mPersonInfoListener;
    DatabaseReference mUserRef;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myactivity);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        profile = (ImageView) findViewById(R.id.backdrop);
        mPeopleRef = FirebaseUtil.getPeopleRef();
        mPeopleRef.keepSynced(true);
        currentUserId = FirebaseUtil.getCurrentUserId();
        mPostsRef = FirebaseUtil.getPostsRef();
        mPeopleRef.keepSynced(true);
        FloatingActionButton followUserFab = (FloatingActionButton) findViewById(R.id.follow_user_fab);
        followUserFab.setVisibility(View.GONE);
        if (currentUserId != null){
            mRecyclerView = (RecyclerView)findViewById(R.id.user_posts_list);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setReverseLayout(true);
            linearLayoutManager.setStackFromEnd(true);
            mRecyclerView.setLayoutManager(linearLayoutManager);
            mAdapter = new ProfileQueryAdapter(mPostsRef.equalTo(POSTS_STRING), R.layout.users_posts_item_layout, this, ProfileViewHolder.class);
            loadUserPosts();
        }

    }

    private void loadUserPosts(){

        mUserRef = FirebaseUtil.getPeopleRef().child(currentUserId);
        mPersonInfoListener = mUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                People person = dataSnapshot.getValue(People.class);
                Log.w(TAG, "mPersonRef:" + mUserRef.getKey());
                String pictureUrl = person.getPhotoUrl();
                if (pictureUrl != null){
                    GlideUtil.loadImage(person.getPhotoUrl(), profile);
                }
                String name = person.getDisplayName();
                if (name == null) {
                    name = getString(R.string.user_info_no_name);
                    profile.setContentDescription(name);
                }
                collapsingToolbar.setTitle(name);

            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {

            }
        });

        mPersonRef = FirebaseUtil.getPeopleRef().child(currentUserId).child(POSTS_STRING).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                DatabaseReference tempRef = FirebaseUtil.getPostsRef().child(dataSnapshot.getKey());
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

    public void bindRecyclerView() {
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}
