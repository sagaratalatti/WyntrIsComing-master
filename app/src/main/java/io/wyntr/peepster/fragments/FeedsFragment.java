package io.wyntr.peepster.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import io.wyntr.peepster.R;
import io.wyntr.peepster.adapters.FirebasePostsQueryAdapter;
import io.wyntr.peepster.viewholders.PostViewHolder;
import io.wyntr.peepster.utilities.Constants;
import io.wyntr.peepster.utilities.FirebaseUtil;

import static io.wyntr.peepster.utilities.Constants.POSTS_STRING;

/**
 * Created by sagar on 19-01-2017.
 */

public class FeedsFragment extends Fragment {

    private static final String TAG = FeedsFragment.class.getSimpleName();

    private int mRecyclerViewPosition = 0;
    private RecyclerView mRecyclerView;
    FirebasePostsQueryAdapter mAdapter = null;
    private static final String KEY_LAYOUT_POSITION = "layoutPosition";
    DatabaseReference mFirebaseRef;
    View rootView;
    TextView emptyView;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mAdapter = new FirebasePostsQueryAdapter(mFirebaseRef.equalTo(Constants.POSTS_STRING), R.layout.geo_feeds_items, getActivity(), PostViewHolder.class);

        if (savedInstanceState != null) {
            // Restore saved layout manager type.
            mRecyclerViewPosition = (int) savedInstanceState
                    .getSerializable(KEY_LAYOUT_POSITION);
            mRecyclerView.scrollToPosition(mRecyclerViewPosition);
            // TODO: RecyclerView only restores position properly for some tabs.
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFirebaseRef = FirebaseDatabase.getInstance().getReference(POSTS_STRING);
        mFirebaseRef.keepSynced(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.feeds_fragment_layout, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.geo_recycler_view);
        emptyView = (TextView)rootView.findViewById(R.id.empty_recycler_text);
        if (FirebaseUtil.getCurrentUserId() != null) {
            loadFeeds();
        }
        return rootView;
    }

    private void loadFeeds(){
        FirebaseUtil.getCurrentUserRef().child("following").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(final DataSnapshot followedUserSnapshot, String s) {
                String followedUserId = followedUserSnapshot.getKey();
                FirebaseUtil.getPeopleRef().child(followedUserId).child("posts").addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(final DataSnapshot postSnapshot, String s) {
                        FirebaseUtil.getPostsRef().child(postSnapshot.getKey()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String key = dataSnapshot.getKey();
                                Log.d(TAG, "item added " + key);
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
                        isListEmpty();
                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        isListEmpty();
                    }
                });
                bindRecyclerView();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                isListEmpty();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void bindRecyclerView() {
        mRecyclerView.setAdapter(mAdapter);
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save currently selected layout manager.
        int recyclerViewScrollPosition = getRecyclerViewScrollPosition();
        Log.d(TAG, "Recycler view scroll position: " + recyclerViewScrollPosition);
        savedInstanceState.putSerializable(KEY_LAYOUT_POSITION, recyclerViewScrollPosition);
        super.onSaveInstanceState(savedInstanceState);
    }

    private int getRecyclerViewScrollPosition() {
        int scrollPosition = 0;
        // TODO: Is null check necessary?
        if (mRecyclerView != null && mRecyclerView.getLayoutManager() != null) {
            scrollPosition = ((LinearLayoutManager) mRecyclerView.getLayoutManager())
                    .findFirstCompletelyVisibleItemPosition();
        }
        return scrollPosition;
    }

    public void isListEmpty() {
        if (mAdapter.getItemCount() == 0) {
            emptyView.setVisibility(View.VISIBLE);
            emptyView.setText(getString(R.string.feeds_empty_label));
        } else {
            emptyView.setVisibility(View.GONE);
        }
    }
}
