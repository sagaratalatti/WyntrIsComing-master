package io.wyntr.peepster;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class MapPosts implements ClusterItem {

    public String key;
    private final LatLng mPosition;

    public MapPosts(LatLng position, String key) {
        this.mPosition = position;
        this.key = key;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    public String getKey(){
        return key;
    }

}
