package io.wyntr.peepster.fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import io.wyntr.peepster.R;
import io.wyntr.peepster.adapters.CommentsQueryAdapter;
import io.wyntr.peepster.viewholders.CommentsViewHolder;
import io.wyntr.peepster.models.Comment;
import io.wyntr.peepster.models.Users;
import io.wyntr.peepster.utilities.FirebaseUtil;

import static io.wyntr.peepster.utilities.Constants.COMMENTS_STRING;

/**
 * Created by sagar on 27-02-2017.
 */

public class CommentsFragment extends DialogFragment {

    public CommentsFragment(){}

    private static final String TAG = CommentsFragment.class.getSimpleName();

    RecyclerView mRecyclerView;
    private int mRecyclerViewPosition = 0;
    CommentsQueryAdapter mAdapter = null;
    private static final String KEY_LAYOUT_POSITION = "layoutPosition";
    DatabaseReference mFirebaseRef;
    String commentsKey;

    public static CommentsFragment newIstance(String key){
        CommentsFragment commentsFragment = new CommentsFragment();
        Bundle args = new Bundle();
        args.putString("commentsKey", key);
        commentsFragment.setArguments(args);
        return commentsFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        commentsKey = getArguments().getString("commentsKey");
        mFirebaseRef = FirebaseDatabase.getInstance().getReference(COMMENTS_STRING);
        mFirebaseRef.keepSynced(true);
        mAdapter = new CommentsQueryAdapter(mFirebaseRef.child(commentsKey), R.layout.comments_item_layout, getActivity(), CommentsViewHolder.class);
        return inflater.inflate(R.layout.comments_dialog, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.comments_recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        if (savedInstanceState != null) {
            // Restore saved layout manager type.
            mRecyclerViewPosition = (int) savedInstanceState
                    .getSerializable(KEY_LAYOUT_POSITION);
            mRecyclerView.scrollToPosition(mRecyclerViewPosition);
            // TODO: RecyclerView only restores position properly for some tabs.
        }
        final EditText commentBox = (EditText)view.findViewById(R.id.comment_box);
        final Button postComment = (Button)view.findViewById(R.id.post_comment);
        commentBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() != 0)
                    postComment.setVisibility(View.VISIBLE);
                if (charSequence.length() == 0) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            postComment.setVisibility(View.GONE);
                        }
                    }, 3000);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() == 0) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            postComment.setVisibility(View.GONE);

                        }
                    }, 3000);
                }
            }
        });
        postComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Editable commentText = commentBox.getText();
                if (!commentText.toString().isEmpty()){
                    InputMethodManager inputManager =
                            (InputMethodManager) getActivity().
                                    getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(
                            commentBox.getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                    final DatabaseReference commentsRef = FirebaseUtil.getCommentsRef().child(commentsKey);
                    Users author = FirebaseUtil.getUser();
                    final Comment comment = new Comment(author, commentText.toString(),
                            ServerValue.TIMESTAMP);
                    commentsRef.push().setValue(comment, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError error, DatabaseReference firebase) {
                            if (error != null) {
                                Log.w(TAG, "Error posting comment: " + error.getMessage());
                                Toast.makeText(getActivity(), "Error posting comment.", Toast
                                        .LENGTH_SHORT).show();
                                commentBox.setText(commentText);
                            } else {
                                commentBox.setText("");
                                Toast.makeText(getActivity(), getString(R.string.comment_success), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(getActivity(), "Write something...", Toast.LENGTH_SHORT).show();
                }

            }
        });
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        loadComments();
    }

    @Override
    public void onStart(){
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            getDialog().getWindow().setLayout(width, height);
            getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            getDialog().getWindow().setLayout(width, height);
            getDialog().getWindow().setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.rounded_dialog));
        }
    }

    private void loadComments(){
        if (commentsKey != null){
            mFirebaseRef.child(commentsKey).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Log.d(TAG, "onChildAdded: " + dataSnapshot.getKey());
                    DatabaseReference tempRef = mFirebaseRef.child(commentsKey).child(dataSnapshot.getKey());
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
}
