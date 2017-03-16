package io.wyntr.peepster.adapters;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import io.wyntr.peepster.R;
import io.wyntr.peepster.models.People;
import io.wyntr.peepster.utilities.FirebaseUtil;
import io.wyntr.peepster.viewholders.FriendsViewHolder;

/**
 * Created by sagar on 02-02-2017.
 */

public class FriendsQueryAdapter extends FriendsAdapter<People, FriendsViewHolder>{

    private Activity mActivity;
    private static final String TAG = FriendsQueryAdapter.class.getSimpleName();
    private String currentUserId;
    public ValueEventListener mFollowListener;

    /**
     * @param mRef        The Firebase location to watch for data changes. Can also be a slice of a location, using some
     *                    combination of <code>limit()</code>, <code>startAt()</code>, and <code>endAt()</code>,
     * @param mLayout     This is the mLayout used to represent a single list item. You will be responsible for populating an
     *                    instance of the corresponding view with the data from an instance of mModelClass.
     * @param activity    The activity containing the ListView
     */

    public FriendsQueryAdapter(Query mRef, int mLayout, Activity activity, Class<FriendsViewHolder> viewHolderClass, String currentUserId) {
        super(mRef, People.class, mLayout, activity, viewHolderClass);
        this.mActivity = activity;
        this.currentUserId = currentUserId;
    }

    @Override
    protected void populateViewHolder(final FriendsViewHolder viewHolder, final People model, int position) {
        viewHolder.Name.setText(model.getDisplayName());
        viewHolder.setPhoto(model.getPhotoUrl());
        viewHolder.mPhotoView.setContentDescription(model.getDisplayName());
        mFollowListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    viewHolder.addButton.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_account_remove_grey600_24dp));
                } else {
                    viewHolder.addButton.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_account_plus_black_24dp));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        FirebaseUtil.getPeopleRef().child(currentUserId).child("following").child(model.getUserId())
                    .addValueEventListener(mFollowListener);

        viewHolder.userContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                        if (currentUserId == null) {
                            Toast.makeText(mActivity, "You need to sign in to follow someone.",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        // TODO: Convert these to actually not be single value, for live updating when
                        // current user follows.
                        FirebaseUtil.getPeopleRef().child(currentUserId).child("following").child(model.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Map<String, Object> updatedUserData = new HashMap<>();
                                if (dataSnapshot.exists()) {
                                    // Already following, need to unfollow
                                    updatedUserData.put("people/" + currentUserId + "/following/" + model.getUserId(), null);
                                    updatedUserData.put("followers/" + currentUserId + "/" + model.getUserId(), null);
                                } else {
                                    updatedUserData.put("people/" + currentUserId + "/following/" + model.getUserId(), true);
                                    updatedUserData.put("followers/" + model.getUserId() + "/" + currentUserId, true);
                                }
                                FirebaseUtil.getBaseRef().updateChildren(updatedUserData, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError firebaseError, DatabaseReference firebase) {
                                        if (firebaseError != null) {
                                            Toast.makeText(mActivity, R.string
                                                    .follow_user_error, Toast.LENGTH_LONG).show();
                                            Log.d(TAG, mActivity.getString(R.string.follow_user_error) + "\n" +
                                                    firebaseError.getMessage());
                                        }                                  }
                                });
                            }

                            @Override
                            public void onCancelled(DatabaseError firebaseError) {

                            }
                        });
                    }
                });
    }
}
