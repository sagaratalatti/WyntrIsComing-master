package io.wyntr.peepster.models;


import android.provider.ContactsContract;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Feeds {
    private Users user;
    private String video_url;
    private String thumb_storage_uri;
    private String thumb_url;
    private String text;
    private Object timestamp;
    private String video_storage_uri;

    public Feeds() {
        // empty default constructor, necessary for Firebase to be able to deserialize blog posts
    }

    public Feeds(Users user, String video_url, String video_storage_uri, String thumb_url, String thumb_storage_uri, String text, Object timestamp) {
        this.user = user;
        this.video_url = video_url;
        this.text = text;
        this.timestamp = timestamp;
        this.thumb_storage_uri = thumb_storage_uri;
        this.thumb_url = thumb_url;
        this.video_storage_uri = video_storage_uri;
    }

    public Users getUser() {
        return user;
    }

    public String getVideo_url() {
        return video_url;
    }

    public String getText() {
        return text;
    }

    public Object getTimestamp() {
        return timestamp;
    }

    public String getThumb_storage_uri() {
        return thumb_storage_uri;
    }

    @JsonProperty("thumb_url")
    public String getThumb_url() {
        return thumb_url;
    }

    public String getVideo_storage_uri() {
        return video_storage_uri;
    }
}
