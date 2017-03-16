package io.wyntr.peepster.activities;

import android.location.Location;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;

import io.wyntr.peepster.MapPosts;
import io.wyntr.peepster.R;
import io.wyntr.peepster.adapters.MapsQueryAdapter;
import io.wyntr.peepster.fragments.ClusterDetailFragment;
import io.wyntr.peepster.fragments.LikesFragment;
import io.wyntr.peepster.viewholders.ProfileViewHolder;

import static io.wyntr.peepster.utilities.Constants.GEO_POINTS;
import static io.wyntr.peepster.utilities.Constants.POSTS_STRING;


public class CustomClustering extends MapsActivity implements ClusterManager.OnClusterClickListener<MapPosts>, ClusterManager.OnClusterInfoWindowClickListener<MapPosts>, ClusterManager.OnClusterItemClickListener<MapPosts>, ClusterManager.OnClusterItemInfoWindowClickListener<MapPosts> {

    private static final String TAG = CustomClustering.class.getSimpleName();

    private ClusterManager<MapPosts> mClusterManager;
    DatabaseReference mFirebaseRef;
    GeoFire geoFire;
    MapsQueryAdapter mAdapter = null;
    GeoQuery query;
    GeoLocation center;
    RecyclerView mRecyclerView;

    @Override
    public boolean onClusterClick(Cluster<MapPosts> cluster) {
        for (MapPosts posts : cluster.getItems()){
            DatabaseReference tempRef = mFirebaseRef.child(posts.getKey());
            tempRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String key = dataSnapshot.getKey();
                    if (!mAdapter.exists(key)) {
                        Log.d(TAG, "item added " + key);
                        mAdapter.addSingle(dataSnapshot);
                        mAdapter.notifyDataSetChanged();
                    } else {
                        //...otherwise I will update the record
                        Log.d(TAG, "item updated: " + key);
                        mAdapter.update(dataSnapshot, key);
                        mAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            bindRecyclerView();
        }
        return true;
    }


    @Override
    public void onClusterInfoWindowClick(Cluster<MapPosts> cluster) {

    }


    @Override
    public boolean onClusterItemClick(MapPosts item) {
        return true;
    }

    @Override
    public void onClusterItemInfoWindowClick(MapPosts item) {
    }


    @Override
    protected void startDemo(Location location) {
        mFirebaseRef = FirebaseDatabase.getInstance().getReference(POSTS_STRING);
        mFirebaseRef.keepSynced(true);
        mAdapter = new MapsQueryAdapter(mFirebaseRef.equalTo(GEO_POINTS), R.layout.users_posts_item_layout, this, ProfileViewHolder.class);
        mRecyclerView = (RecyclerView)findViewById(R.id.maps_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);
            getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 9.5f));
            mClusterManager = new ClusterManager<>(this, getMap());
            getMap().setOnCameraIdleListener(mClusterManager);
            getMap().setOnMarkerClickListener(mClusterManager);
            getMap().setOnInfoWindowClickListener(mClusterManager);
            mClusterManager.setOnClusterClickListener(this);
            mClusterManager.setOnClusterInfoWindowClickListener(this);
            mClusterManager.setOnClusterItemClickListener(this);
            mClusterManager.setOnClusterItemInfoWindowClickListener(this);
            addItems(location, 9.5f);
            mClusterManager.cluster();
    }

    protected void addItems(final Location location, float radius) {
        this.geoFire = new GeoFire(FirebaseDatabase.getInstance().getReference(GEO_POINTS));
        mFirebaseRef = FirebaseDatabase.getInstance().getReference(POSTS_STRING);
        if (location != null){
            center = new GeoLocation(location.getLatitude(), location.getLongitude());
            query = geoFire.queryAtLocation(center, radius);
            query.addGeoQueryEventListener(new GeoQueryEventListener() {
                @Override
                public void onKeyEntered(String key, GeoLocation location) {
                    mClusterManager.addItem(new MapPosts(position(new LatLng(location.latitude, location.longitude)), key));
                }

                @Override
                public void onKeyExited(String key) {
                }

                @Override
                public void onKeyMoved(String key, GeoLocation location) {
                    mClusterManager.addItem(new MapPosts(position(new LatLng(location.latitude, location.longitude)), key));
                }

                @Override
                public void onGeoQueryReady() {

                }

                @Override
                public void onGeoQueryError(DatabaseError error) {

                }
            });
        }
    }

    private LatLng position(LatLng latLng) {
        return latLng;
    }

    private void bindRecyclerView(){
        mRecyclerView.setVisibility(View.VISIBLE);
        mRecyclerView.setAdapter(mAdapter);
    }

}
