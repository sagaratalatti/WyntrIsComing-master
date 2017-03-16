package io.wyntr.peepster.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import io.wyntr.peepster.fragments.CommentsFragment;
import io.wyntr.peepster.fragments.GeoFragment;
import io.wyntr.peepster.fragments.LikesFragment;
import io.wyntr.peepster.utilities.FirebaseUtil;
import io.wyntr.peepster.viewholders.PostViewHolder;

/**
 * Created by sagar on 06-03-2017.
 */

public class GeoFeedsAdapter extends RecyclerView.Adapter<PostViewHolder> {

    private Cursor mCursor;
    final private Context mContext;
    private int layoutId;

    public GeoFeedsAdapter(Context mContext, int layoutId) {
        this.mContext = mContext;
        this.layoutId = layoutId;
    }

    @Override
    public PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if ( parent instanceof RecyclerView ){
            View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
            view.setFocusable(true);
            return new PostViewHolder(view);
        } else{
            throw new RuntimeException("Not bound to RecyclerView");
        }
    }

    @Override
    public void onBindViewHolder(final PostViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        final String postKey = mCursor.getString(GeoFragment.COL_KEY);
        holder.setTimestamp(mCursor.getString(GeoFragment.COL_TIMESTAMP));
        holder.setPhoto(mCursor.getString(GeoFragment.COL_THUMB), mCursor.getString(GeoFragment.COL_VIDEO), postKey, mCursor.getString(GeoFragment.COL_POST_CAPTION) );
        holder.setAuthor(mCursor.getString(GeoFragment.COL_USER), mCursor.getString(GeoFragment.COL_USER_ID));
        holder.setIcon(mCursor.getString(GeoFragment.COL_USER_PROFILE), mCursor.getString(GeoFragment.COL_USER_ID));
        holder.setText(mCursor.getString(GeoFragment.COL_POST_CAPTION));

        final ValueEventListener likeListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                holder.setNumLikes(dataSnapshot.getChildrenCount());
                if (dataSnapshot.hasChild(FirebaseUtil.getCurrentUserId())) {
                    holder.setLikeStatus(PostViewHolder.LikeStatus.LIKED, mContext);
                } else {
                    holder.setLikeStatus(PostViewHolder.LikeStatus.NOT_LIKED, mContext);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        final ValueEventListener commentsListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                holder.setNumComments(dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        final ValueEventListener viewsListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                holder.setmNumViewsView(dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        holder.mCommentIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = ((AppCompatActivity)mContext).getSupportFragmentManager();
                CommentsFragment commentsFragment = CommentsFragment.newIstance(postKey);
                commentsFragment.show(fragmentManager,"comments_fragment");
            }
        });

        holder.mLikeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = ((AppCompatActivity)mContext).getSupportFragmentManager();
                LikesFragment likesFragment = LikesFragment.newIstance(postKey);
                likesFragment.show(fragmentManager,"likes_fragment");
            }
        });

        FirebaseUtil.getLikesRef().child(postKey).addValueEventListener(likeListener);
        FirebaseUtil.getCommentsRef().child(postKey).addValueEventListener(commentsListener);
        FirebaseUtil.getViewsRef().child(postKey).addValueEventListener(viewsListener);
        holder.mLikeListener = likeListener;
        holder.mCommentsListener = commentsListener;
        holder.mViewsListener = viewsListener;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
       // mEmptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    public Cursor getCursor() {
        return mCursor;
    }

    @Override
    public int getItemCount() {
        if ( null == mCursor ) return 0;
        return mCursor.getCount();
    }
}
