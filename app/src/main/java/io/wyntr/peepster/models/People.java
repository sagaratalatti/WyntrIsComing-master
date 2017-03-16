package io.wyntr.peepster.models;

import java.util.Map;

/**
 * Created by sagar on 19-01-2017.
 */

public class People {

    private String displayName;
    private String photoUrl;
    private Map<String, Boolean> posts;
    private Map<String, Object> following;
    private String userId;

    public People() {

    }

    public People(String displayName, String photoUrl) {
        this.displayName = displayName;
        this.photoUrl = photoUrl;
    }

    public String getUserId(){return userId;}

    public String getDisplayName() {
        return displayName;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public Map<String, Boolean> getPosts() {
        return posts;
    }

    public Map<String, Object> getFollowing() {
        return following;
    }
}
