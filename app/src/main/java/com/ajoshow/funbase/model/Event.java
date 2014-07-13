package com.ajoshow.funbase.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andy on 2014/7/12.
 */
public class Event {
    private String mId;
    private EventState mState = EventState.PENDING;
    private String mOtherUserId;
    private List<Message> mMessages;

    public Event(String eventId, String otherUserId){
        if(eventId == null){
            throw new NullPointerException("Id cannot be null.");
        }else if(otherUserId == null){
            throw new NullPointerException("OtherUserId cannot be null.");
        }
        mId = eventId;
        mOtherUserId = otherUserId;
        mMessages = new ArrayList<Message>();
    }

    public String getId(){
        return mId;
    }

    public String getOtherUserId(){
        return mOtherUserId;
    }

    public void setState(EventState state){
        mState = state;
    }

    public EventState getState(){
        return mState;
    }

    public void addMessage(Message msg){
        mMessages.add(msg);
    }

    public List<Message> getMessages(){
        return mMessages;
    }
}
