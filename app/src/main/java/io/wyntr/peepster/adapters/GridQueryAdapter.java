package io.wyntr.peepster.adapters;

import android.app.Activity;

import com.google.firebase.database.Query;

import java.util.List;
import java.util.Map;

import io.wyntr.peepster.models.Feeds;
import io.wyntr.peepster.viewholders.GridViewHolder;

/**
 * Created by sagar on 04-03-2017.
 */

public class GridQueryAdapter extends FirebaseGridRecyclerAdapter<Feeds, GridViewHolder> {
    /**
     * @param mRef            The Firebase location to watch for data changes. Can also be a slice of a location, using some
     *                        combination of <code>limit()</code>, <code>startAt()</code>, and <code>endAt()</code>,
     * @param activity        The activity containing the ListView
     * @param viewHolderClass
     */
    public GridQueryAdapter(Query mRef, Activity activity, Class<GridViewHolder> viewHolderClass) {
        super(mRef, Feeds.class, activity, viewHolderClass);
    }

    @Override
    protected void populateViewHolder(GridViewHolder viewHolder, Feeds model, int position, List<String> mKeys) {
        viewHolder.setThumbnail(model.getThumb_url(), model.getVideo_url(), mKeys.get(position));
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
