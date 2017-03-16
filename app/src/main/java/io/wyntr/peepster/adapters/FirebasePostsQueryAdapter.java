package io.wyntr.peepster.adapters;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Map;

import io.wyntr.peepster.activities.SingleVideoView;
import io.wyntr.peepster.fragments.CommentsFragment;
import io.wyntr.peepster.fragments.LikesFragment;
import io.wyntr.peepster.models.Feeds;
import io.wyntr.peepster.utilities.Constants;
import io.wyntr.peepster.utilities.FirebaseUtil;
import io.wyntr.peepster.viewholders.PostViewHolder;

/**
 * Created by sagar on 14-01-2017.
 */

public class FirebasePostsQueryAdapter extends FirebaseRecyclerAdapter<Feeds, PostViewHolder> {
    Activity mActivity;


    /**
     * @param mRef            The Firebase location to watch for data changes. Can also be a slice of a location, using some
     *                        combination of <code>limit()</code>, <code>startAt()</code>, and <code>endAt()</code>,
     * @param mLayout         This is the mLayout used to represent a single list item. You will be responsible for populating an
     *                        instance of the corresponding view with the data from an instance of mModelClass.
     * @param activity        The activity containing the ListView
     * @param viewHolderClass This is the PostsViewHolder Class which will be used to populate data.
     */
    public FirebasePostsQueryAdapter(Query mRef, int mLayout, Activity activity, Class<PostViewHolder> viewHolderClass) {
        super(mRef, Feeds.class, mLayout, activity, viewHolderClass);
        this.mActivity = activity;
    }

    @Override
    protected void populateViewHolder(final PostViewHolder viewHolder, final Feeds model, final int position, final List<String> mKeys) {
        viewHolder.setPhoto(model.getThumb_url(), model.getVideo_url(), mKeys.get(position), model.getText());
        viewHolder.setTimestamp(DateUtils.getRelativeTimeSpanString(
                (long) model.getTimestamp()).toString());
        viewHolder.setAuthor(model.getUser().getFull_name(), model.getUser().getUid());
        viewHolder.setIcon(model.getUser().getProfile_picture(), model.getUser().getUid());
        viewHolder.setText(model.getText());

        final ValueEventListener likeListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                    viewHolder.setNumLikes(dataSnapshot.getChildrenCount());
                if (dataSnapshot.hasChild(FirebaseUtil.getCurrentUserId())) {
                    viewHolder.setLikeStatus(PostViewHolder.LikeStatus.LIKED, mActivity);
                } else {
                    viewHolder.setLikeStatus(PostViewHolder.LikeStatus.NOT_LIKED, mActivity);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        final  ValueEventListener commentsListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                viewHolder.setNumComments(dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        final ValueEventListener viewsListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                viewHolder.setmNumViewsView(dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        viewHolder.mCommentIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = ((AppCompatActivity)mActivity).getSupportFragmentManager();
                CommentsFragment commentsFragment = CommentsFragment.newIstance(mKeys.get(position));
                commentsFragment.show(fragmentManager,"comments_fragment");
            }
        });

        viewHolder.mLikeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = ((AppCompatActivity)mActivity).getSupportFragmentManager();
                LikesFragment likesFragment = LikesFragment.newIstance(mKeys.get(position));
                likesFragment.show(fragmentManager,"likes_fragment");
            }
        });

        FirebaseUtil.getLikesRef().child(mKeys.get(position)).addValueEventListener(likeListener);
        FirebaseUtil.getCommentsRef().child(mKeys.get(position)).addValueEventListener(commentsListener);
        FirebaseUtil.getViewsRef().child(mKeys.get(position)).addValueEventListener(viewsListener);
        viewHolder.mLikeListener = likeListener;
        viewHolder.mCommentsListener = commentsListener;
        viewHolder.mViewsListener = viewsListener;
    }

    @Override
    protected List<Feeds> filters(List<Feeds> models, CharSequence constraint) {
        return null;
    }

    @Override
    protected Map<String, Feeds> filterKeys(List<Feeds> mModels) {
        return null;
    }
}