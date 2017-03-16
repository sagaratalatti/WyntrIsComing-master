package io.wyntr.peepster.viewholders;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import de.hdodenhof.circleimageview.CircleImageView;
import io.wyntr.peepster.R;
import io.wyntr.peepster.activities.SingleVideoView;
import io.wyntr.peepster.activities.UserVideoView;
import io.wyntr.peepster.fragments.CommentsFragment;
import io.wyntr.peepster.fragments.LikesFragment;
import io.wyntr.peepster.fragments.ViewsFragment;
import io.wyntr.peepster.utilities.Constants;
import io.wyntr.peepster.utilities.FirebaseUtil;
import io.wyntr.peepster.utilities.GlideUtil;

import static android.R.attr.dial;
import static android.R.attr.key;
import static io.wyntr.peepster.utilities.Constants.POSTS_STRING;

/**
 * Created by sagar on 27-02-2017.
 */

public class ProfileViewHolder extends RecyclerView.ViewHolder {

    private CircleImageView thumbnails;
    private TextView heartsCount, commentsCount, viewsCount;
    private CardView postsContainer;
    private View mView;

    public ProfileViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        thumbnails = (CircleImageView)itemView.findViewById(R.id.users_posts_photo);
        heartsCount = (TextView)itemView.findViewById(R.id.users_posts_heart_counts);
        commentsCount = (TextView)itemView.findViewById(R.id.users_posts_comments_counts);
        viewsCount = (TextView)itemView.findViewById(R.id.users_posts_views_counts);
        postsContainer = (CardView)itemView.findViewById(R.id.users_posts_container);
    }

    public void setThumbnails(String path, final String videoUrl, final String key, final Activity mActivity){
        GlideUtil.loadProfileIcon(path, thumbnails);

        postsContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mActivity, UserVideoView.class);
                intent.putExtra(Constants.KEY, key);
                intent.putExtra(Constants.INTENT_VIDEO, videoUrl);
                mActivity.startActivity(intent);
            }
        });

        postsContainer.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(mActivity, android.R.style.Theme_Dialog));
                alertDialog.setIcon(R.drawable.ic_delete_forever_black_24dp);
                alertDialog.setMessage(mActivity.getString(R.string.delete_video));
                alertDialog.setPositiveButton(mActivity.getString(R.string.delete), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        FirebaseUtil.getPostsRef().child(key).removeValue();
                        FirebaseUtil.getCurrentUserRef().child(POSTS_STRING).child(key).removeValue();
                    }
                });
                alertDialog.setNegativeButton(mActivity.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                       dialogInterface.dismiss();
                    }
                });

                AlertDialog alertdialog = alertDialog.create();
                alertdialog.show();
                return true;
            }
        });
    }

    public void setMapThumbnails(String path, final String videoUrl, final String key, final Activity mActivity) {
        GlideUtil.loadProfileIcon(path, thumbnails);

        postsContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mActivity, SingleVideoView.class);
                intent.putExtra(Constants.KEY, key);
                intent.putExtra(Constants.INTENT_VIDEO, videoUrl);
                mActivity.startActivity(intent);
            }
        });
    }

    public void setMapHeartsCount(String counts){
        heartsCount.setText(counts);
    }

    public void setMapCommentsCount(String counts){
        commentsCount.setText(counts);
    }

    public void setHeartsCount(String counts, final Activity activity, final String key){
        heartsCount.setText(counts);
        heartsCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = ((AppCompatActivity)activity).getSupportFragmentManager();
                LikesFragment likesFragment = LikesFragment.newIstance(key);
                likesFragment.show(fragmentManager,"likes_fragment");
            }
        });
    }

    public void setCommentsCount(String counts, final Activity activity, final String key){
        commentsCount.setText(counts);
        commentsCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = ((AppCompatActivity)activity).getSupportFragmentManager();
                CommentsFragment commentsFragment = CommentsFragment.newIstance(key);
                commentsFragment.show(fragmentManager,"comments_fragment");
            }
        });
    }

    public void setViewsCount(String counts, final Activity activity, final String key){
        viewsCount.setText(counts);
        viewsCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = ((AppCompatActivity)activity).getSupportFragmentManager();
                ViewsFragment viewsFragment = ViewsFragment.newIstance(key);
                viewsFragment.show(fragmentManager,"views_fragment");
            }
        });
    }

    public void setMapViewsCount(String counts){
        viewsCount.setText(counts);
    }
}
