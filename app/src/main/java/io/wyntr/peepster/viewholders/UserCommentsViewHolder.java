package io.wyntr.peepster.viewholders;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import de.hdodenhof.circleimageview.CircleImageView;
import io.wyntr.peepster.R;
import io.wyntr.peepster.activities.UserProfileActivity;
import io.wyntr.peepster.utilities.Constants;
import io.wyntr.peepster.utilities.GlideUtil;

/**
 * Created by sagar on 01-03-2017.
 */

public class UserCommentsViewHolder extends RecyclerView.ViewHolder {

    RelativeLayout userContainer;
    private CircleImageView profilePhoto;
    TextView profileName, commentText, timeStamp;
    View mView;

    public UserCommentsViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        userContainer = (RelativeLayout)itemView.findViewById(R.id.users_comments_container);
        profilePhoto = (CircleImageView)itemView.findViewById(R.id.users_comments_profile_picture);
        profileName = (TextView)itemView.findViewById(R.id.users_comments_profile_name);
        timeStamp = (TextView)itemView.findViewById(R.id.users_comments_timestamp);
        commentText = (TextView)itemView.findViewById(R.id.users_comments_text);
    }

    public void setIcon(String url) {
        GlideUtil.loadProfileIcon(url, profilePhoto);
    }

    public void setAuthor(String author, final String authorId) {
        if (author == null || author.isEmpty()) {
            author = mView.getResources().getString(R.string.user_info_no_name);
        }
        profileName.setText(author);
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

    public void setTimestamp(String timestamp) {
        timeStamp.setText(timestamp);
    }

    public void setCommentText(String comment){
        commentText.setText(comment);
    }

}
