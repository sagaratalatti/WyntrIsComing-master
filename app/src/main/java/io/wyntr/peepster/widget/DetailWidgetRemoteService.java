package io.wyntr.peepster.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.util.Base64;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.AppWidgetTarget;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.database.ServerValue;

import java.util.concurrent.ExecutionException;

import io.wyntr.peepster.R;
import io.wyntr.peepster.data.FeedsContract;
import io.wyntr.peepster.utilities.Constants;

/**
 * Created by sagar on 06-03-2017.
 */

@TargetApi(Build.VERSION_CODES.HONEYCOMB)

public class DetailWidgetRemoteService extends RemoteViewsService {

    private static final String[] FEEDS_COLUMNS = {
            FeedsContract.FeedsEntry._ID,
            FeedsContract.FeedsEntry.COLUMN_POST_CAPTION,
            FeedsContract.FeedsEntry.COLUMN_POST_KEY,
            FeedsContract.FeedsEntry.COLUMN_THUMB,
            FeedsContract.FeedsEntry.COLUMN_VIDEO

    };

    static final int INDEX_FEED_ID = 0;
    static final int INDEX_POST_CAPTION= 1;
    static final int INDEX_FEED_KEY = 2;
    static final int INDEX_FEEDS_THUMB = 3;
    static final int INDEX_FEEDS_VIDEO = 4;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                final long identityToken = Binder.clearCallingIdentity();
                data = getContentResolver().query(FeedsContract.FeedsEntry.CONTENT_URI,
                        FEEDS_COLUMNS,
                        null,
                        null,
                        FeedsContract.FeedsEntry._ID + " ASC");
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_items);

                String imageUrl = data.getString(INDEX_FEEDS_THUMB);
                Bitmap feedsImage = null;
                try {
                    feedsImage = Glide.with(getApplicationContext())
                                .load(imageUrl)
                                .asBitmap()
                                .into(200,200).get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
                String description = data.getString(INDEX_POST_CAPTION);
                String postKey = data.getString(INDEX_FEED_KEY);
                String videoUrl = data.getString(INDEX_FEEDS_VIDEO);
                if (feedsImage != null){
                    views.setImageViewBitmap(R.id.widget_image, feedsImage);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    setRemoteContentDescription(views, description);
                }

                final Intent fillInIntent = new Intent();
                fillInIntent.putExtra(Constants.INTENT_VIDEO, videoUrl);
                fillInIntent.putExtra(Constants.KEY, postKey);
                views.setOnClickFillInIntent(R.id.widget_image, fillInIntent);
                return views;
            }

            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            private void setRemoteContentDescription(RemoteViews views, String description) {
                views.setContentDescription(R.id.widget_image, description);
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_layout);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(INDEX_FEED_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
