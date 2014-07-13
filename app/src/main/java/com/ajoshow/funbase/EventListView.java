package com.ajoshow.funbase;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AbsListView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.ajoshow.funbase.libs.FirebaseAgent;
import com.ajoshow.funbase.model.Event;
import com.nhaarman.listviewanimations.itemmanipulation.OnDismissCallback;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.SwipeDismissAdapter;
import com.nhaarman.listviewanimations.swinginadapters.AnimationAdapter;
import com.nhaarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;
import com.nhaarman.listviewanimations.swinginadapters.prepared.SwingRightInAnimationAdapter;

import java.util.List;

/**
 * Created by Andy on 2014/7/12.
 */
public class EventListView extends ListView implements OnDismissCallback {

    private EventListAdapter mAdapter;

    public EventListView(Context context) {
        super(context);
        init();
    }
    public EventListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public EventListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init(){
        mAdapter = new EventListAdapter(getContext());
        SwipeDismissAdapter adapter = new SwipeDismissAdapter(mAdapter, this);
        AnimationAdapter animAdapter = new SwingBottomInAnimationAdapter(adapter);
        animAdapter.setAbsListView(this);
        super.setAdapter(animAdapter);
    }

    /**
     * @deprecated the adapter is already preset, and this method does no effect.
     * @param adapter
     */
    @Override
    public void setAdapter(ListAdapter adapter) {
        // DO NOTHING
    }

    public EventListAdapter getEventListAdapter(){
        return mAdapter;
    }

    public void notifyDataSetChanged(){
        mAdapter.notifyDataSetChanged();
    }

    public void setEvents(List<Event> events){
        mAdapter.setEvents(events);
        notifyDataSetChanged();
    }

    public void setEvent(int index, Event event){
        mAdapter.setEvent(index, event);
        notifyDataSetChanged();
    }

    public void setEvent(String eventId, Event event){
        mAdapter.setEvent(eventId, event);
        notifyDataSetChanged();
    }

    public void addEvent(Event event){
        mAdapter.addEvent(event);
        notifyDataSetChanged();
    }

    public void addEvents(List<Event> events){
        for(Event event : events){
            mAdapter.addEvent(event);
        }
        notifyDataSetChanged();
    }

    public Event getEvent(int index){
        return mAdapter.getEvent(index);
    }

    public Event getEvent(String eventId){
        return mAdapter.getEvent(eventId);
    }

    public List<Event> getEvents(){
        return mAdapter.getEvents();
    }

    public boolean removeEvent(Event event){
        Boolean bool = mAdapter.removeEvent(event);
        notifyDataSetChanged();
        return bool;
    }

    public void removeEvent(String eventId){
        mAdapter.removeEvent(eventId);
        notifyDataSetChanged();
    }

    public Event removeEvent(int index){
        Event event = mAdapter.removeEvent(index);
        notifyDataSetChanged();
        return event;
    }

    public void removeEvents(List<Event> events){
        for(Event event : events){
            mAdapter.removeEvent(event);
        }
        notifyDataSetChanged();
    }

    @Override
    public void onDismiss(AbsListView absListView, int[] reverseSortedPositions) {
        EventListAdapter adapter = getEventListAdapter();
        for (int position : reverseSortedPositions) {
            Event event = adapter.getEvent(position);
            FirebaseAgent.getInstance().removeEvent(event.getId());
        }
    }
}
