package io.wyntr.peepster.viewholders;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import io.wyntr.peepster.activities.UserVideoView;
import io.wyntr.peepster.utilities.Constants;
import io.wyntr.peepster.utilities.GlideUtil;

/**
 * Created by sagar on 04-03-2017.
 */

public class GridViewHolder extends RecyclerView.ViewHolder {

    private ImageView thumbnail;
    private Context context;

    public GridViewHolder(ImageView itemView) {
        super(itemView);
        thumbnail = itemView;
        context = thumbnail.getContext();
    }

    public void setThumbnail(String path, final String videoUrl, final String key){
        GlideUtil.loadImage(path, thumbnail);
        thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, UserVideoView.class);
                intent.putExtra(Constants.KEY, key);
                intent.putExtra(Constants.INTENT_VIDEO, videoUrl);
                context.startActivity(intent);
            }
        });
    }
}
