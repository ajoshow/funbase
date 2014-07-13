package com.ajoshow.funbase;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ajoshow.funbase.libs.FirebaseAgent;
import com.ajoshow.funbase.model.Event;
import com.ajoshow.funbase.model.EventState;
import com.ajoshow.funbase.model.Message;
import com.ajoshow.funbase.utils.Constant;

import java.util.ArrayList;
import java.util.List;

import funbase.ajoshow.com.funbase.R;

public class EventActivity extends Activity implements FirebaseAgent.UserNodeListener, FirebaseAgent.EventNodeListener{

    private Button mStateBtn;
    private Button mConnectBtn;
    private Button mEchoBtn;
    private EditText mOtherUserIdEt;
    private TextView mUserIdTv;
    private EventListView mListView;
    private SharedPreferences mSharedPref;
    private String mUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        mSharedPref = getSharedPreferences(Constant.APP_NAME, Context.MODE_PRIVATE);
        mUserId = mSharedPref.getString(Constant.USER_ID, "YOUR ID");
        mUserIdTv = (TextView) findViewById(R.id.userid);
        mOtherUserIdEt = (EditText) findViewById(R.id.invteeId);
        mListView = (EventListView) findViewById(R.id.listView);
        mConnectBtn = (Button) findViewById(R.id.connectBtn);
        mStateBtn = (Button) findViewById(R.id.stateBtn);
        mEchoBtn = (Button) findViewById(R.id.echoBtn);

        init();
        readFromSharedPref();
    }

    private void init(){
        mUserIdTv.setText(mUserId);
        mListView.setEvents(new ArrayList<Event>());
        FirebaseAgent.getInstance().setUserNodeListener(this);
        FirebaseAgent.getInstance().setEventNodeListener(this);

        mConnectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String otherUserId = mOtherUserIdEt.getText().toString().trim();
                if (otherUserId.isEmpty()) {
                    Toast.makeText(EventActivity.this, "Invalid ID. Please try again.", Toast.LENGTH_SHORT).show();
                } else if(otherUserId.equals(mUserId)){
                    Toast.makeText(EventActivity.this, "Cannot invite yourself.", Toast.LENGTH_SHORT).show();
                }else {
                    FirebaseAgent.getInstance().createEventAndInvite(otherUserId);
                    Toast.makeText(EventActivity.this, "Request a connect... please wait for response.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mStateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(Event event : mListView.getEvents()){
                    FirebaseAgent.getInstance().updateEventState(event.getId(), event.getState().next());
                }
            }
        });

        mEchoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(Event event : mListView.getEvents()){
                    String content = System.currentTimeMillis() + "";
                    FirebaseAgent.getInstance().sendMessage(event.getId(), new Message(mUserId, content));
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        FirebaseAgent.getInstance().destroy();
        super.onDestroy();
    }

    public static Intent makeIntent(Context context){
        return new Intent(context, EventActivity.class);
    }

    private void addToSharedPref(Event event){
        StringBuilder sb = new StringBuilder(mSharedPref.getString(mUserId, ""));
        sb.append(event.getId()).append(",");
        mSharedPref.edit().putString(mUserId, sb.toString()).commit();
    }

    private void removeFromSharedPref(String removedEventId){
        String[] eventIds = mSharedPref.getString(mUserId, "").split(",");
        List<String> list = new ArrayList<String>();
        for(String eventId : eventIds){
            if(!eventId.equals(removedEventId)){
                list.add(eventId);
            }
        }
        updateSharedPrefById(list);
    }

    private void updateSharedPrefByEvent(List<Event> events){
        StringBuilder sb = new StringBuilder();
        for(Event event : events){
            sb.append(event.getId()).append(",");
        }
        mSharedPref.edit().putString(mUserId, sb.toString()).commit();
    }

    private void updateSharedPrefById(List<String> eventIds){
        StringBuilder sb = new StringBuilder();
        for(String eventId : eventIds){
            sb.append(eventId).append(",");
        }
        mSharedPref.edit().putString(mUserId, sb.toString()).commit();
    }

    private void readFromSharedPref(){
        String[] eventIds = mSharedPref.getString(mUserId, "").split(",");
        for(String eventId : eventIds){
            FirebaseAgent.getInstance().startListenEvent(eventId);
        }
    }

    private void acceptEvent(String eventId){
        // remove node from user/{userId}/invite/...
        FirebaseAgent.getInstance().removeInvite(mUserId, eventId);
        // join event
        FirebaseAgent.getInstance().startListenEvent(eventId);
        FirebaseAgent.getInstance().joinEvent(eventId, mUserId);
    }

    @Override
    public void onReceiveInivte(Event event) {
        acceptEvent(event.getId());
        Log.d("andy", "onReceiveInivte: " + event.getId());
    }

    @Override
    public void onRefreshInvite(List<Event> events) {
        for(Event event : events){
            acceptEvent(event.getId());
        }
        Log.d("andy", "onRefreshInivte: " + events);
    }

    @Override
    public void onInviteRemoved(List<Event> remainEvents) {
        Log.d("andy", "onInivteRemoved: " + remainEvents);
    }

    @Override
    public void onEventUpdated(String eventId, EventState newState) {
        Event event = mListView.getEvent(eventId);
        if(event != null){
            event.setState(newState);
            mListView.notifyDataSetChanged();
        }
        Log.d("andy", "onEventUpdated: " + eventId);
    }

    @Override
    public void onReceiveEvent(Event event) {
        mListView.addEvent(event);
        addToSharedPref(event);
        Log.d("andy", "onReceiveEvent: " + event.getId());
    }

    @Override
    public void onMessage(String eventId, Message message) {
        Event event = mListView.getEvent(eventId);
        if(event != null){
            event.addMessage(message);
            mListView.notifyDataSetChanged();
        }
        Log.d("andy", "onMessage: " + eventId);
    }

    @Override
    public void onRemovedEvent(String eventId) {
        mListView.removeEvent(eventId);
        removeFromSharedPref(eventId);
        Log.d("andy", "onRemovedEvent: " + eventId);
    }
}
