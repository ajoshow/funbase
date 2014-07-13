package com.ajoshow.funbase.libs;

import com.ajoshow.funbase.model.Event;
import com.ajoshow.funbase.model.EventState;
import com.ajoshow.funbase.model.Message;

import java.util.List;

/**
 * Created by Andy on 2014/7/12.
 */
public class SimpleEventNodeListener implements FirebaseAgent.EventNodeListener {


    @Override
    public void onEventUpdated(String evnetId, EventState newState) {

    }

    @Override
    public void onReceiveEvent(Event event) {

    }

    @Override
    public void onMessage(String eventId, Message message) {

    }

    @Override
    public void onRemovedEvent(String eventId) {

    }
}
