package io.wyntr.peepster.viewholders;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import io.wyntr.peepster.R;
import io.wyntr.peepster.utilities.GlideUtil;

/**
 * Created by sagar on 19-02-2017.
 */

public class FriendsViewHolder extends RecyclerView.ViewHolder{

    public ImageView mPhotoView;
    public ImageView addButton;
    public TextView Name;
    private View mView;
    public CardView userContainer;


    public FriendsViewHolder(View view){
        super(view);
        mView = view;
        mPhotoView = (ImageView)view.findViewById(R.id.friends_profile_photo);
        Name = (TextView)view.findViewById(R.id.friends_display_name);
        addButton = (ImageView)view.findViewById(R.id.add_button);
        userContainer = (CardView) view.findViewById(R.id.friends_container);
    }

    public void setPhoto(String url) {
        GlideUtil.loadImage(url, mPhotoView);
    }

}
