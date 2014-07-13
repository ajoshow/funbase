package com.ajoshow.funbase.libs;

import android.util.Log;

import com.ajoshow.funbase.model.Event;

import java.util.List;

/**
 * Created by Andy on 2014/7/12.
 */
public class SimpleUserNodeListener implements FirebaseAgent.UserNodeListener {

    @Override
    public void onReceiveInivte(Event event) {

    }

    @Override
    public void onRefreshInvite(List<Event> eventIds) {

    }

    @Override
    public void onInviteRemoved(List<Event> remainEvents) {

    }
}
