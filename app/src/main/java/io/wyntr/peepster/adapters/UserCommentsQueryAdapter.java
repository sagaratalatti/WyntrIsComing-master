package io.wyntr.peepster.adapters;

import android.app.Activity;
import android.text.format.DateUtils;

import com.google.firebase.database.Query;

import java.util.List;
import java.util.Map;

import io.wyntr.peepster.models.Comment;
import io.wyntr.peepster.viewholders.UserCommentsViewHolder;

/**
 * Created by sagar on 01-03-2017.
 */

public class UserCommentsQueryAdapter extends FirebaseRecyclerAdapter<Comment, UserCommentsViewHolder> {
    /**
     * @param mRef            The Firebase location to watch for data changes. Can also be a slice of a location, using some
     *                        combination of <code>limit()</code>, <code>startAt()</code>, and <code>endAt()</code>,
     * @param mLayout         This is the mLayout used to represent a single list item. You will be responsible for populating an
     *                        instance of the corresponding view with the data from an instance of mModelClass.
     * @param activity        The activity containing the ListView
     * @param viewHolderClass
     */
    public UserCommentsQueryAdapter(Query mRef, int mLayout, Activity activity, Class<UserCommentsViewHolder> viewHolderClass) {
        super(mRef, Comment.class, mLayout, activity, viewHolderClass);
    }

    @Override
    protected void populateViewHolder(UserCommentsViewHolder viewHolder, Comment model, int position, List<String> mKeys) {
        viewHolder.setAuthor(model.getUser().getFull_name(), model.getUser().getUid());
        viewHolder.setIcon(model.getUser().getProfile_picture());
        viewHolder.setTimestamp(DateUtils.getRelativeTimeSpanString(
                (long) model.getTimestamp()).toString());
        viewHolder.setCommentText(model.getText());
    }

    @Override
    protected List<Comment> filters(List<Comment> models, CharSequence constraint) {
        return null;
    }

    @Override
    protected Map<String, Comment> filterKeys(List<Comment> mModels) {
        return null;
    }
}
