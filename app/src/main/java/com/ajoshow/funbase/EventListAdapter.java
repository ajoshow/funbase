package com.ajoshow.funbase;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ajoshow.funbase.model.Event;
import com.ajoshow.funbase.model.EventState;
import com.ajoshow.funbase.model.Message;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import funbase.ajoshow.com.funbase.R;

/**
 * Created by Andy on 2014/7/12.
 */
public class EventListAdapter extends BaseAdapter {

    private Context mContext;
    private List<Event> mEvents = new ArrayList<Event>();

    public EventListAdapter(Context context){
        mContext = context;
    }

    public void setEvents(List<Event> events){
        mEvents = new ArrayList<Event>(events);
    }

    public void setEvent(int index, Event event){
        mEvents.set(index, event);
    }

    public void setEvent(String eventId, Event event){
        for(int i=0; i < mEvents.size(); i++){
            Event existEvent = mEvents.get(i);
            if(existEvent.getId().equals(eventId)){
                setEvent(i, event);
                return;
            }
        }
    }

    public void addEvent(Event event){
        mEvents.add(event);
    }

    public boolean removeEvent(Event event){
        return mEvents.remove(event);
    }

    public Event removeEvent(int index){
        return mEvents.remove(index);
    }

    public Event removeEvent(String eventId)
    {
        Iterator<Event> iterator = mEvents.iterator();
        while(iterator.hasNext()){
            Event event = iterator.next();
            if(event.getId().equals(eventId)){
                mEvents.remove(event);
                return event;
            }
        }
        return null;
    }

    public Event getEvent(int index){
        return mEvents.get(index);
    }

    public Event getEvent(String id){
        for(Event event : mEvents){
            if(event.getId().equals(id)){
                return event;
            }
        }
        return null;
    }

    public List<Event> getEvents(){
        return new WeakReference<List<Event>>(mEvents).get();
    }

    @Override
    public int getCount() {
        return mEvents.size();
    }

    @Override
    public Object getItem(int i) {
        return mEvents.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        ViewHolder holder;
        if(convertView == null){
            holder = new ViewHolder();
            convertView = ((Activity)mContext).getLayoutInflater().inflate(R.layout.eventlist_item, null);
            holder.mEventIdTv = (TextView) convertView.findViewById(R.id.eventId);
            holder.mEchoTv = (TextView) convertView.findViewById(R.id.echo);
            holder.mOtherUserIdsTv = (TextView) convertView.findViewById(R.id.otherUserId);
            holder.mStateTv = (TextView) convertView.findViewById(R.id.state);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        Event event = mEvents.get(i);

        holder.mEventIdTv.setText("ID: " + event.getId());
        holder.mStateTv.setText(event.getState().toString());
        holder.mStateTv.setTextColor(mContext.getResources().getColor(getColorCode(event.getState())));

        ArrayList<Message> messages = (ArrayList<Message>) event.getMessages();
        if(messages.size() > 0){
            Message message = messages.get(messages.size()-1);
            holder.mOtherUserIdsTv.setText(message.getSenderId() + " : " + message.getContent());
        }else{
            holder.mOtherUserIdsTv.setText("Connected with: " + event.getOtherUserId());
        }

        return convertView;
    }

    private int getColorCode(EventState state){
        switch(state){
            case PENDING:
                return R.color.yellow;
            case ACTIVE:
                return R.color.green;
            case START:
                return R.color.green;
            case MEET:
                return R.color.blue;
            case UNKNOWN:
            case FINISHED:
                return R.color.red;
            default:
                return android.R.color.black;
        }
    }

    static class ViewHolder{
        public TextView mEventIdTv;
        public TextView mOtherUserIdsTv;
        public TextView mStateTv;
        public TextView mEchoTv;
    }
}
