package io.wyntr.peepster.models;

/**
 * Created by sagar on 19-01-2017.
 */

public class Comment {

    private Users user;
    private String text;
    private Object timestamp;

    public Comment() {
        // empty default constructor, necessary for Firebase to be able to deserialize comments
    }

    public Comment(Users user, String text, Object timestamp) {
        this.user = user;
        this.text = text;
        this.timestamp = timestamp;
    }

    public Users getUser() {
        return user;
    }

    public String getText() {
        return text;
    }

    public Object getTimestamp() {
        return timestamp;
    }
}