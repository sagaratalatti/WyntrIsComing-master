package io.wyntr.peepster.models;

/**
 * Created by sagar on 27-02-2017.
 */

public class Likes {

    private Users users;

    public Likes(){}

    public Likes(Users users){
        this.users = users;
    }

    public Users getUsers() {
        return users;
    }
}
