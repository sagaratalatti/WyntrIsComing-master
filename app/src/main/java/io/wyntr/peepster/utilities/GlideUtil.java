package io.wyntr.peepster.utilities;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import io.wyntr.peepster.R;

/**
 * Created by sagar on 14-01-2017.
 */

public class GlideUtil {

    public static void loadImage(String url, ImageView imageView) {
        Context context = imageView.getContext();
        ColorDrawable cd = new ColorDrawable(ContextCompat.getColor(context, R.color.colorPrimary));
        Glide.with(context)
                .load(url)
                .placeholder(cd)
                .crossFade()
                .centerCrop()
                .into(imageView);
    }


    public static void loadProfileIcon(String url, ImageView imageView) {
        Context context = imageView.getContext();
        Glide.with(context)
                .load(url)
                .placeholder(R.drawable.ic_face_profile)
                .dontAnimate()
                .fitCenter()
                .into(imageView);
    }
}
