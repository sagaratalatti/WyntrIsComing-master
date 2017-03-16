package io.wyntr.peepster.adapters;

import android.app.Activity;

import com.google.firebase.database.Query;

import java.util.List;
import java.util.Map;

import io.wyntr.peepster.models.Likes;
import io.wyntr.peepster.viewholders.LikesViewHolder;

/**
 * Created by sagar on 27-02-2017.
 */

public class LikesQueryAdapter extends FirebaseRecyclerAdapter<Likes, LikesViewHolder> {
    /**
     * @param mRef            The Firebase location to watch for data changes. Can also be a slice of a location, using some
     *                        combination of <code>limit()</code>, <code>startAt()</code>, and <code>endAt()</code>
     * @param mLayout         This is the mLayout used to represent a single list item. You will be responsible for populating an
     *                        instance of the corresponding view with the data from an instance of mModelClass.
     * @param activity        The activity containing the ListView
     * @param viewHolderClass
     */
    public LikesQueryAdapter(Query mRef, int mLayout, Activity activity, Class<LikesViewHolder> viewHolderClass) {
        super(mRef, Likes.class, mLayout, activity, viewHolderClass);
    }

    @Override
    protected void populateViewHolder(LikesViewHolder viewHolder, Likes model, int position, List<String> mKeys) {
        viewHolder.setPhoto(model.getUsers().getProfile_picture());
        viewHolder.setAuthor(model.getUsers().getFull_name(), model.getUsers().getUid());
    }

    @Override
    protected List<Likes> filters(List<Likes> models, CharSequence constraint) {
        return null;
    }

    @Override
    protected Map<String, Likes> filterKeys(List<Likes> mModels) {
        return null;
    }
}
