package com.ajoshow.funbase.model;

/**
 * Created by Andy on 2014/7/12.
 */
public enum EventState {

    UNKNOWN(-1),
    PENDING(0),
    ACTIVE(10),
    START(20),
    MEET(30),
    FINISHED(40)
    ;

    private EventState(int id){
        mID = id;
    }

    private int mID;

    public int getId(){
        return mID;
    }

    public static EventState find(int id){
        for(EventState state : values()){
            if(state.getId() == id){
                return state;
            }
        }
        return UNKNOWN;
    }

    public static EventState find(String name){
        for(EventState state : values()){
            if(state.toString().equals(name)){
                return state;
            }
        }
        return UNKNOWN;
    }

    public EventState next(){
        if(this != UNKNOWN && this != FINISHED){
            return EventState.find(mID + 10);
        }
        return this;
    }
}
