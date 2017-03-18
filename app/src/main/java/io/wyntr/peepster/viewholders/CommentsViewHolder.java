package io.wyntr.peepster.viewholders;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import de.hdodenhof.circleimageview.CircleImageView;
import io.wyntr.peepster.R;
import io.wyntr.peepster.activities.UserProfileActivity;
import io.wyntr.peepster.utilities.Constants;
import io.wyntr.peepster.utilities.GlideUtil;

/**
 * Created by sagar on 27-02-2017.
 */

public class CommentsViewHolder extends RecyclerView.ViewHolder{

    public TextView commentsText, userName, timeStamp;
    CircleImageView userImage;
    RelativeLayout userContainer;
    View mView;

    public CommentsViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        commentsText = (TextView)itemView.findViewById(R.id.comment_text_view);
        userName = (TextView)itemView.findViewById(R.id.comments_user_name);
        userImage = (CircleImageView)itemView.findViewById(R.id.user_profile_comments);
        userContainer = (RelativeLayout)itemView.findViewById(R.id.comments_profile_layout);
        timeStamp = (TextView)itemView.findViewById(R.id.comments_timestamp);
    }

    public void setPhoto(String url, String author) {
        userImage.setContentDescription(author);
        GlideUtil.loadProfileIcon(url, userImage);
    }

    public void setTimestamp(String timestamp) {
        timeStamp.setText(timestamp);
    }

    public void setAuthor(String author, final String authorId) {
        if (author == null || author.isEmpty()) {
            author = mView.getResources().getString(R.string.user_info_no_name);
        }
        userName.setText(author);
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
}
