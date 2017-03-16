package io.wyntr.peepster.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import io.wyntr.peepster.R;
import io.wyntr.peepster.adapters.FriendsQueryAdapter;
import io.wyntr.peepster.viewholders.FriendsViewHolder;
import io.wyntr.peepster.utilities.FirebaseUtil;

public class AddFriends extends AppCompatActivity {

    private static final String TAG = AddFriends.class.getSimpleName();
    DatabaseReference mFriendsReference;
    private RecyclerView mRecyclerView;
    FriendsQueryAdapter mAdapter = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friends);
        Toolbar toolbar = (Toolbar)findViewById(R.id.add_friends_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setTitle(getString(R.string.add_friends));
        toolbar.setNavigationIcon(ContextCompat.getDrawable(this, R.drawable.ic_arrow_left_black_24dp));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AddFriends.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        mRecyclerView = (RecyclerView)findViewById(R.id.friendsRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mFriendsReference = FirebaseUtil.getPeopleRef();
        String currentUserId = FirebaseUtil.getCurrentUserId();
        mFriendsReference.keepSynced(true);
        if (currentUserId != null){
            mAdapter = new FriendsQueryAdapter(mFriendsReference, R.layout.friends_item_layout, this, FriendsViewHolder.class, currentUserId);
        }
        getPeople();
    }

    private void getPeople(){
        FirebaseUtil.getPeopleRef().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, dataSnapshot.getKey());
                DatabaseReference tempRef = mFriendsReference.child(dataSnapshot.getKey());
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

}
