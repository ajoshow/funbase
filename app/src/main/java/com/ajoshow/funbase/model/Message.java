package com.ajoshow.funbase.model;

/**
 * Created by Andy on 2014/7/13.
 */
public class Message {
    private String mSenderId;
    private String mContent;

    public Message(String senderId, String content){
        if(senderId == null){
            throw new NullPointerException("senderId cannot be null.");
        }
        mSenderId = senderId;
        mContent = content;
    }

    public String getContent(){
        if(mContent == null){
            return "";
        }
        return mContent.trim();
    }

    public String getSenderId(){
        return mSenderId;
    }

}
