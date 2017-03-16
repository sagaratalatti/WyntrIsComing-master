package io.wyntr.peepster.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;

import io.wyntr.peepster.R;
import io.wyntr.peepster.activities.MainActivity;
import io.wyntr.peepster.adapters.GeoFeedsAdapter;
import io.wyntr.peepster.data.FeedsContract;

/**
 * Created by sagar on 08-01-2017.
 */

public class GeoFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = GeoFragment.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private GeoFeedsAdapter mAdapter;
    private static final String KEY_LAYOUT_POSITION = "layoutPosition";
    private long mInitialSelectedTime = -1;

    View rootView;

    private static final String[] FEEDS_COLUMNS = {
            FeedsContract.FeedsEntry.COLUMN_USER,
            FeedsContract.FeedsEntry.COLUMN_USER_ID,
            FeedsContract.FeedsEntry.COLUMN_VIDEO,
            FeedsContract.FeedsEntry.COLUMN_TIME_STAMP,
            FeedsContract.FeedsEntry.COLUMN_THUMB,
            FeedsContract.FeedsEntry.COLUMN_POST_KEY,
            FeedsContract.FeedsEntry.COLUMN_USER_PROFILE,
            FeedsContract.FeedsEntry.COLUMN_POST_CAPTION
    };

    private boolean mHoldForTransition;

    private static final int FEEDS_LOADER = 0;
    public static final int COL_USER = 0;
    public static final int COL_USER_ID = 1;
    public static final int COL_VIDEO = 2;
    public static final int COL_TIMESTAMP = 3;
    public static final int COL_THUMB = 4;
    public static final int COL_KEY = 5;
    public static final int COL_USER_PROFILE = 6;
    public static final int COL_POST_CAPTION = 7;

    int recyclerViewScrollPosition;
    TextView emptyView;

    public GeoFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if ( mHoldForTransition ) {
            getActivity().supportPostponeEnterTransition();
        }
        getLoaderManager().initLoader(FEEDS_LOADER, null, this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        if (savedInstanceState != null) {
            // Restore saved layout manager type.
            int mRecyclerViewPosition = (int) savedInstanceState
                    .getSerializable(KEY_LAYOUT_POSITION);
            mRecyclerView.scrollToPosition(mRecyclerViewPosition);
            // TODO: RecyclerView only restores position properly for some tabs.
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.feeds_fragment_layout, container, false);
        emptyView= (TextView) rootView.findViewById(R.id.empty_recycler_text);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.geo_recycler_view);
        mAdapter = new GeoFeedsAdapter(getActivity(), R.layout.geo_feeds_items);
        bindRecyclerView();

        return rootView;
    }

    public void bindRecyclerView() {
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save currently selected layout manager.
        recyclerViewScrollPosition = getRecyclerViewScrollPosition();
        Log.d(TAG, "Recycler view scroll position: " + recyclerViewScrollPosition);
        savedInstanceState.putInt(KEY_LAYOUT_POSITION, recyclerViewScrollPosition);
        super.onSaveInstanceState(savedInstanceState);
    }


    private int getRecyclerViewScrollPosition() {
        RecyclerView.LayoutManager layoutManager =  mRecyclerView.getLayoutManager();
        if(layoutManager != null && layoutManager instanceof LinearLayoutManager){
            recyclerViewScrollPosition = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
        }
        return recyclerViewScrollPosition;
    }



    public void isListEmpty(Cursor data) {
        if (data.getCount() == 0) {
            emptyView.setVisibility(View.VISIBLE);
            emptyView.setText(getString(R.string.empty_geo_feeds_label));
        } else {
            emptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String sortOrder = FeedsContract.FeedsEntry._ID + " ASC";

        return new CursorLoader(getActivity(),
                FeedsContract.FeedsEntry.CONTENT_URI,
                FEEDS_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
        isListEmpty(data);
        if ( data.getCount() == 0 ) {
            getActivity().supportStartPostponedEnterTransition();
        } else {
            mRecyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    if (mRecyclerView.getChildCount() > 0) {
                        mRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                        int position = getRecyclerViewScrollPosition();
                        if (position == RecyclerView.NO_POSITION &&
                                -1 != mInitialSelectedTime) {
                            Cursor data = mAdapter.getCursor();
                            int count = data.getCount();
                            int dateColumn = data.getColumnIndex(FeedsContract.FeedsEntry._ID);
                            for ( int i = 0; i < count; i++ ) {
                                data.moveToPosition(i);
                                if ( data.getLong(dateColumn) == mInitialSelectedTime ) {
                                    position = i;
                                    break;
                                }
                            }
                        }
                        if (position == RecyclerView.NO_POSITION) position = 0;
                        mRecyclerView.smoothScrollToPosition(position);
                        if ( mHoldForTransition ) {
                            getActivity().supportStartPostponedEnterTransition();
                        }
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mRecyclerView) {
            mRecyclerView.clearOnScrollListeners();
        }
    }

}
