package io.wyntr.peepster.viewholders;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.database.ValueEventListener;

import io.wyntr.peepster.R;
import io.wyntr.peepster.activities.SingleVideoView;
import io.wyntr.peepster.activities.UserProfileActivity;
import io.wyntr.peepster.utilities.Constants;
import io.wyntr.peepster.utilities.GlideUtil;

/**
 * Created by sagar on 14-01-2017.
 */

public class PostViewHolder extends RecyclerView.ViewHolder {
    private final View mView;

    public enum LikeStatus { LIKED, NOT_LIKED }
    public ImageView mLikeIcon;
    private static final int POST_TEXT_MAX_LINES = 6;
    public ImageView mPhotoView;
    private ImageView mIconView;
    private TextView mAuthorView, mNumCommentsView,  mPostTextView, mTimestampView, mNumLikesView, mNumViewsView;
    public String mPostKey;
    public ImageView mCommentIcon;
    public ValueEventListener mLikeListener, mViewsListener, mCommentsListener;
    private RelativeLayout userContainer;

    public PostViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mPhotoView = (ImageView) itemView.findViewById(R.id.post_photo);
        mIconView = (ImageView) mView.findViewById(R.id.post_author_icon);
        mNumCommentsView = (TextView) itemView.findViewById(R.id.comment_num);
        mNumViewsView = (TextView) itemView.findViewById(R.id.views_num);
        mAuthorView = (TextView) mView.findViewById(R.id.post_author_name);
        mPostTextView = (TextView) itemView.findViewById(R.id.post_text);
        mTimestampView = (TextView) itemView.findViewById(R.id.post_timestamp);
        mNumLikesView = (TextView) itemView.findViewById(R.id.post_num_likes);
        mLikeIcon = (ImageView) itemView.findViewById(R.id.post_like_icon);
        mCommentIcon = (ImageView) itemView.findViewById(R.id.post_comment_icon);
        userContainer = (RelativeLayout) itemView.findViewById(R.id.users_container);

    }

    public void setPhoto(String url, final String videoUrl, final String key, String description) {
        GlideUtil.loadImage(url, mPhotoView);
        if (description == null || description.isEmpty()) {
            description = mView.getResources().getString(R.string.no_description);
        }
        mPhotoView.setContentDescription(description);
        mPhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mPhotoView.getContext(), SingleVideoView.class);
                intent.putExtra(Constants.INTENT_VIDEO, videoUrl);
                intent.putExtra(Constants.KEY, key);
                mPhotoView.getContext().startActivity(intent);
            }
        });
    }

    public void setIcon(String url, final String authorId) {
        GlideUtil.loadProfileIcon(url, mIconView);
        mIconView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showUserDetail(authorId);
            }
        });
    }

    public void setAuthor(String author, final String authorId) {
        if (author == null || author.isEmpty()) {
            author = mView.getResources().getString(R.string.user_info_no_name);
        }
        mAuthorView.setText(author);
        userContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showUserDetail(authorId);
            }
        });
    }

    private void showUserDetail(String authorId) {
        Context context = mView.getContext();
        Intent userDetailIntent = new Intent(context, UserProfileActivity.class);
        userDetailIntent.putExtra(Constants.USER_ID, authorId);
        context.startActivity(userDetailIntent);
    }


    public void setText(final String text) {
        if (text == null || text.isEmpty()) {
            mPostTextView.setVisibility(View.GONE);
        } else {
            mPostTextView.setVisibility(View.VISIBLE);
            mPostTextView.setText(text);
            mPostTextView.setMaxLines(POST_TEXT_MAX_LINES);
            mPostTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mPostTextView.getMaxLines() == POST_TEXT_MAX_LINES) {
                        mPostTextView.setMaxLines(Integer.MAX_VALUE);
                    } else {
                        mPostTextView.setMaxLines(POST_TEXT_MAX_LINES);
                    }
                }
            });
        }
    }

    public void setKey(final String key){
        mCommentIcon.setContentDescription(key);
    }

    public void setTimestamp(String timestamp) {
        mTimestampView.setText(timestamp);
    }

    public void setNumLikes(long numLikes) {
        String suffix = numLikes == 1 ? " like" : " likes";
        mNumLikesView.setText(numLikes + suffix);
    }

    public void setNumComments(long numComments){
        String suffix = numComments == 1 ? " Comment" : " Comments";
        mNumCommentsView.setText(numComments + suffix);
    }

    public void setmNumViewsView(long numViews){
        String suffix = numViews == 1 ? " View" : " Views";
        mNumViewsView.setText(numViews + suffix);
    }



    public void setLikeStatus(LikeStatus status, Context context) {
        mLikeIcon.setImageDrawable(ContextCompat.getDrawable(context,
                status == LikeStatus.LIKED ? R.drawable.heart : R.drawable.ic_heart_outline_grey600_24dp));
    }
}
