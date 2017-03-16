package io.wyntr.peepster.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import io.wyntr.peepster.R;

import io.wyntr.peepster.adapters.MapsQueryAdapter;
import io.wyntr.peepster.utilities.Constants;
import io.wyntr.peepster.viewholders.ProfileViewHolder;
import static io.wyntr.peepster.utilities.Constants.GEO_POINTS;
import static io.wyntr.peepster.utilities.Constants.POSTS_STRING;

/**
 * Created by sagar on 13-03-2017.
 */

public class ClusterDetailFragment extends DialogFragment {

    private static final String TAG = ClusterDetailFragment.class.getSimpleName();

    public ClusterDetailFragment(){}
    RecyclerView mRecyclerView;
    private int mRecyclerViewPosition = 0;
    MapsQueryAdapter mAdapter = null;
    DatabaseReference mFirebaseRef;
    ArrayList<String> postKey;

    public static ClusterDetailFragment newIstance(ArrayList<String> key){
        ClusterDetailFragment detailFragment = new ClusterDetailFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(Constants.KEY, key);
        detailFragment.setArguments(args);
        return detailFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        postKey = getArguments().getStringArrayList(Constants.KEY);
        mFirebaseRef = FirebaseDatabase.getInstance().getReference(POSTS_STRING);
        mFirebaseRef.keepSynced(true);
        mAdapter = new MapsQueryAdapter(mFirebaseRef.equalTo(GEO_POINTS), R.layout.users_posts_item_layout, getActivity(), ProfileViewHolder.class);
        return inflater.inflate(R.layout.cluster_detail_dialog, container);
    }

    @Override
    public void onStart(){
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            getDialog().getWindow().setLayout(width, height);
            //getDialog().getWindow().setGravity(Gravity.BOTTOM);
            getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            getDialog().getWindow().setLayout(width, height);
            getDialog().getWindow().setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.rounded_dialog));
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.comments_recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        if (postKey.size() <= 0){
            loadPosts();
        }
    }

    private void loadPosts(){
        int i = postKey.size();

        DatabaseReference tempRef = mFirebaseRef.child(postKey.get(i));
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

        bindRecyclerView();
    }

    private void bindRecyclerView(){
        mRecyclerView.setAdapter(mAdapter);
    }
}

