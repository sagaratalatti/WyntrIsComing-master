package io.wyntr.peepster.fragments;

import android.app.Dialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import io.wyntr.peepster.R;
import io.wyntr.peepster.adapters.LikesQueryAdapter;
import io.wyntr.peepster.utilities.DividerItemDecoration;
import io.wyntr.peepster.viewholders.LikesViewHolder;

import static io.wyntr.peepster.utilities.Constants.LIKES_STRING;

/**
 * Created by sagar on 27-02-2017.
 */

public class LikesFragment extends DialogFragment {

    public LikesFragment(){}

    private static final String TAG = LikesFragment.class.getSimpleName();

    RecyclerView mRecyclerView;
    private int mRecyclerViewPosition = 0;
    LikesQueryAdapter mAdapter = null;
    private static final String KEY_LAYOUT_POSITION = "layoutPosition";
    DatabaseReference mFirebaseRef;
    String likesKey;

    public static LikesFragment newIstance(String key){
        LikesFragment likesFragment = new LikesFragment();
        Bundle args = new Bundle();
        args.putString("likesKey", key);
        likesFragment.setArguments(args);
        return likesFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mFirebaseRef = FirebaseDatabase.getInstance().getReference(LIKES_STRING);
        mFirebaseRef.keepSynced(true);
        likesKey = getArguments().getString("likesKey");
      return inflater.inflate(R.layout.dialog_layout, container);
    }

    @Override
    public void onStart(){
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            getDialog().getWindow().setLayout(width, height);
            getDialog().getWindow().setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.rounded_dialog));
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState){
        mRecyclerView = (RecyclerView) view.findViewById(R.id.dialog_recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        Drawable dividerLine = ContextCompat.getDrawable(getActivity(), R.drawable.dialog_divider_line);
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(dividerLine);
        mRecyclerView.addItemDecoration(itemDecoration);
        if (savedInstanceState != null) {
            // Restore saved layout manager type.
            mRecyclerViewPosition = (int) savedInstanceState
                    .getSerializable(KEY_LAYOUT_POSITION);
            mRecyclerView.scrollToPosition(mRecyclerViewPosition);
            // TODO: RecyclerView only restores position properly for some tabs.
        }
        mAdapter = new LikesQueryAdapter(mFirebaseRef.child(likesKey), R.layout.likes_dialog_item, getActivity(), LikesViewHolder.class);
        if (likesKey != null){
            loadLikes();
        }
    }

    private void loadLikes(){
        mFirebaseRef.child(likesKey).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                DatabaseReference tempRef = mFirebaseRef.child(likesKey).child(dataSnapshot.getKey());
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
