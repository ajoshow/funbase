package com.ajoshow.funbase.model;

/**
 * Created by Andy on 2014/7/12.
 */
public class User {
    private String mId;

    public User(String id){
        if(id == null){
            throw new NullPointerException("Id cannot be null.");
        }
        mId = id;
    }

    public String getId(){
        return mId;
    }
}
