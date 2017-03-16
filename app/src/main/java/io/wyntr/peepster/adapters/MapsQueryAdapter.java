package io.wyntr.peepster.adapters;

import android.app.Activity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Map;

import io.wyntr.peepster.models.Feeds;
import io.wyntr.peepster.utilities.FirebaseUtil;
import io.wyntr.peepster.viewholders.ProfileViewHolder;

public class MapsQueryAdapter extends FirebaseRecyclerAdapter<Feeds, ProfileViewHolder>{

    private Activity mActivity;

    /**
     * @param mRef            The Firebase location to watch for data changes. Can also be a slice of a location, using some
     *                        combination of <code>limit()</code>, <code>startAt()</code>, and <code>endAt()</code>,
     * @param mLayout         This is the mLayout used to represent a single list item. You will be responsible for populating an
     *                        instance of the corresponding view with the data from an instance of mModelClass.
     * @param activity        The activity containing the ListView
     * @param viewHolderClass This is the PostsViewHolder Class which will be used to populate data.
     */
    public MapsQueryAdapter(Query mRef,int mLayout, Activity activity, Class<ProfileViewHolder> viewHolderClass) {
        super(mRef, Feeds.class, mLayout, activity, viewHolderClass);
        this.mActivity = activity;
    }

    @Override
    protected void populateViewHolder(final ProfileViewHolder viewHolder, Feeds model, final int position, final List<String> mKeys) {
        viewHolder.setMapThumbnails(model.getThumb_url(), model.getVideo_url(), mKeys.get(position), mActivity);

        ValueEventListener likesListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                viewHolder.setMapHeartsCount(String.valueOf(dataSnapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        ValueEventListener commentsListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                viewHolder.setMapCommentsCount(String.valueOf(dataSnapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        ValueEventListener viewsListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                viewHolder.setMapViewsCount(String.valueOf(dataSnapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        FirebaseUtil.getLikesRef().child(mKeys.get(position)).addValueEventListener(likesListener);
        FirebaseUtil.getCommentsRef().child(mKeys.get(position)).addValueEventListener(commentsListener);
        FirebaseUtil.getViewsRef().child(mKeys.get(position)).addValueEventListener(viewsListener);
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
