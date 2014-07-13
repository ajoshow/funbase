package com.ajoshow.funbase.libs;

import android.util.Log;

import com.ajoshow.funbase.model.Event;
import com.ajoshow.funbase.model.EventState;
import com.ajoshow.funbase.model.Message;
import com.ajoshow.funbase.utils.Utils;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.GenericTypeIndicator;
import com.firebase.client.MutableData;
import com.firebase.client.Transaction;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Andy on 2014/7/11.
 */
final public class FirebaseAgent {
    public static final String HOST_URL = "https://funbase-app.firebaseio.com/";
    public static final String SECRET_TOKEN = "erW96OqhPim4UOm3agrmV9nQrJhrLLvNi9y5qn0B";

    private static FirebaseAgent mInstance;
    private String mUserId;
    private Firebase mUserRef;
    private Firebase mConnRef;
    private Map<String, Firebase> mEventRefs;

    private UserNodeListener mUserListener;
    private EventNodeListener mEventListener;

    private volatile int mInviteCount = 0;

    private FirebaseAgent(){
        mInviteCount = 0;
        mUserId = null;
        mEventRefs = null;
        mUserRef = null;
        mUserListener = new SimpleUserNodeListener();
        mEventListener = new SimpleEventNodeListener();

        mConnRef = new Firebase(HOST_URL + "/.info/connected");
        mConnRef.auth(SECRET_TOKEN, mAuthListener);
        mConnRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    // TODO notify other users in the events that we are online.
                } else {
                    // TODO notify other users in the events that we are offline
                }
            }

            @Override
            public void onCancelled(FirebaseError error) {
            }
        });
    }

    public static FirebaseAgent getInstance(){
        if(mInstance == null){
            mInstance = new FirebaseAgent();
        }
        return mInstance;
    }

    public static void destroy(){
        if(mInstance != null){
            mInstance.unregister();
            mInstance.stopListenAllEvents();
            mInstance = null;
        }
    }

    private Firebase newFirebase(String url){
        Firebase f = new Firebase(url);
        f.auth(SECRET_TOKEN, mAuthListener);
        return f;
    }

    /**
     * Start listen to {@link #HOST_URL}/user/{userId} node.
     * @param userId string of unique id.
     */
    public void register(String userId){
        if(mUserRef != null){
            throw new IllegalStateException("You already call register() once. Please call unregister() before it can be called again.");
        }

        mUserRef = newFirebase(HOST_URL + "/user/" + userId);
        mUserId = userId;
        mUserRef.addChildEventListener(mUserChildEventListener);
    }

    /**
     * Stop any exist listener on {@link #HOST_URL}/user/{oldUserId} node and
     * restart listen to {@link #HOST_URL}/user/{userId} node.
     * @param userId string of unique id.
     */
    public void registerOpt(String userId){
        unregister();
        register(userId);
    }

    /**
     * Stop listen to {@link #HOST_URL}/user/{userId} node.
     */
    public void unregister(){
        if(mUserRef != null){
            mUserRef.removeEventListener(mUserChildEventListener);
            mUserRef = null;
        }
    }

    private ChildEventListener mUserChildEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot snapshot, String s) {
            String name = snapshot.getName();
            Log.d("andy", "onUserAdded " + name  + " : " + s);

            GenericTypeIndicator<Object> t = new GenericTypeIndicator<Object>() {};
            Object object = snapshot.getValue(t);
            Map<String, String> map = null;
            String data = null;

            if(object instanceof Map){
                map = (Map<String, String>) object;
            }else if(object instanceof String){
                data = (String) object;
            }

            if(name.equals("invite")){
                String otherUserId = snapshot.getRef().getParent().getName();
                map = Utils.sortByValues(map);
                ArrayList<Event> list = new ArrayList<Event>();
                for(String eventId : map.keySet()){
                    list.add(new Event(eventId, otherUserId));
                }
                mUserListener.onRefreshInvite(list);
                mInviteCount = map.size();
            }

        }

        @Override
        public void onChildChanged(DataSnapshot snapshot, String s) {
            String name = snapshot.getName();
            Log.d("andy", "onUserChanged " + name  + " : " + s);

            GenericTypeIndicator<Object> t = new GenericTypeIndicator<Object>() {};
            Object object = snapshot.getValue(t);
            Map<String, String> map = null;
            String data = null;

            if(object instanceof Map){
                map = (Map<String, String>) object;
            }else if(object instanceof String){
                data = (String) object;
            }

            if(name.equals("invite")){
                String otherUserId = snapshot.getRef().getParent().getName();
                map = Utils.sortByValues(map);

                if(map.size() < mInviteCount){
                    // Deleted
                    ArrayList<Event> list = new ArrayList<Event>();
                    for(String eventId : map.keySet()){
                        list.add(new Event(eventId, otherUserId));
                    }
                    mUserListener.onInviteRemoved(list);
                }else if(map.size() > mInviteCount){
                    // incoming invite
                    ArrayList<String> list = new ArrayList<String>(map.keySet());
                    if (list.size() > 0) {
                        Event event = new Event(list.get(list.size() - 1), otherUserId);
                        mUserListener.onReceiveInivte(event);
                    }
                }
                mInviteCount = map.size();
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot snapshot) {
            String name = snapshot.getName();
            Log.d("andy", "onUserRemoved " + name);

            if(name.equals("invite")){
                mUserListener.onInviteRemoved(new ArrayList<Event>());
                mInviteCount = 0;
            }
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            Log.d("andy", "onUserMoved");
        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {

        }
    };

    /**
     * Start listen to {@link #HOST_URL}/event/{eventId} node.
     * @param eventId string of unique id.
     */
    public void startListenEvent(String eventId){
        if(mEventRefs == null){
            mEventRefs = new HashMap<String, Firebase>();
        }

        if(eventId == null){
            throw new NullPointerException("The given eventId is null.");
        }

        if(mEventRefs.containsKey(eventId)){
//            throw new IllegalStateException("You already started listening the node with this event id: " + eventId);
            return;
        }

        Firebase eventRef = newFirebase(HOST_URL + "/event/" + eventId);
        eventRef.addChildEventListener(mEventChildEventListener);
        mEventRefs.put(eventId, eventRef);
    }

    /**
     * Stop listen to {@link #HOST_URL}/event/{eventId} node.
     * @param eventId
     */
    public void stopListenEvent(String eventId){
        if(eventId == null){
            throw new NullPointerException("The given eventId is null.");
        }

        if(mEventRefs != null){
            if(mEventRefs.containsKey(eventId)){
                Firebase eventRef = mEventRefs.remove(eventId);
                eventRef.removeEventListener(mEventChildEventListener);
            }
        }
    }

    /**
     * Stop listen to {@link #HOST_URL}/event/{eventIds} node.
     */
    public void stopListenAllEvents(){
        if(mEventRefs != null){
            Iterator<String> iterator = mEventRefs.keySet().iterator();
            while(iterator.hasNext()){
                String eventId = iterator.next();
                Firebase eventRef = mEventRefs.get(eventId);
                iterator.remove();
                eventRef.removeEventListener(mEventChildEventListener);
            }
        }
    }

    private ChildEventListener mEventChildEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot snapshot, String s) {
            String name = snapshot.getName();
            Log.d("andy", "onEventAdded " + name  + " : " + s);

            GenericTypeIndicator<Object> t = new GenericTypeIndicator<Object>() {};
            Object object = snapshot.getValue(t);
            Map<String, String> map = null;
            String data = null;

            if(object instanceof Map){
                map = (Map<String, String>) object;
            }else if(object instanceof String){
                data = (String) object;
            }

            if(name.equals("user")){
                // Event just joined by other user.
                String eventId = snapshot.getRef().getParent().getName();
                if(snapshot.getChildrenCount() >= 2){
                    Iterator<DataSnapshot> iterator = snapshot.getChildren().iterator();
                    while(iterator.hasNext()){
                        DataSnapshot child = iterator.next();
                        String userId = child.getName();
                        if(!userId.equals(mUserId)){
                            mEventListener.onReceiveEvent(new Event(eventId, userId));
                        }
                    }
                }
            }else if(name.equals("state")){
                String eventId = snapshot.getRef().getParent().getName();
                if(data != null){
                    EventState state = EventState.find(data);
                    mEventListener.onEventUpdated(eventId, state);
                }
            }else if(name.equals("messages")){
                String eventId = snapshot.getRef().getParent().getName();
                Iterator<DataSnapshot> iterator = snapshot.getChildren().iterator();
                while(iterator.hasNext()){
                    DataSnapshot child = iterator.next();
                    String senderId = child.getName();
                    String content = (String) child.getValue();
                    mEventListener.onMessage(eventId, new Message(senderId, content));
                }
            }
        }

        @Override
        public void onChildChanged(DataSnapshot snapshot, String s) {
            String name = snapshot.getName();
            Log.d("andy", "onEventChanged " + name  + " : " + s);

            GenericTypeIndicator<Object> t = new GenericTypeIndicator<Object>() {};
            Object object = snapshot.getValue(t);
            Map<String, String> map = null;
            String data = null;

            if(object instanceof Map){
                map = (Map<String, String>) object;
            }else if(object instanceof String){
                data = (String) object;
            }

            if(name.equals("user")){
                // Event just joined by other user.
                String eventId = snapshot.getRef().getParent().getName();
                Iterator<DataSnapshot> iterator = snapshot.getChildren().iterator();
                while(iterator.hasNext()){
                    DataSnapshot child = iterator.next();
                    String userId = child.getName();
                    if(!userId.equals(mUserId)){
                        mEventListener.onReceiveEvent(new Event(eventId, userId));
                    }
                }
            }else if(name.equals("state")){
                String eventId = snapshot.getRef().getParent().getName();
                if(data != null){
                    EventState state = EventState.find(data);
                    mEventListener.onEventUpdated(eventId, state);
                }
            }else if(name.equals("messages")){
                String eventId = snapshot.getRef().getParent().getName();
                Iterator<DataSnapshot> iterator = snapshot.getChildren().iterator();
                while(iterator.hasNext()){
                    DataSnapshot child = iterator.next();
                    String senderId = child.getName();
                    String content = (String) child.getValue();
                    mEventListener.onMessage(eventId, new Message(senderId, content));
                }
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot snapshot) {
            String name = snapshot.getName();
            Log.d("andy", "onEventRemoved " + name);

            if(name.equals("user")){
                String eventId = snapshot.getRef().getParent().getName();
                mEventListener.onRemovedEvent(eventId);
            }
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {

        }
    };

    private Firebase.AuthListener mAuthListener = new Firebase.AuthListener() {
        @Override
        public void onAuthError(FirebaseError firebaseError) {

        }

        @Override
        public void onAuthSuccess(Object o) {

        }

        @Override
        public void onAuthRevoked(FirebaseError firebaseError) {

        }
    };

    public interface UserNodeListener{
        public void onReceiveInivte(Event event);
        public void onRefreshInvite(List<Event> events);
        public void onInviteRemoved(List<Event> remainEvents);
    }

    public interface EventNodeListener{
        public void onEventUpdated(String eventId, EventState newState);
        public void onReceiveEvent(Event event);
        public void onMessage(String eventId, Message message);
        public void onRemovedEvent(String eventId);
    }

    /**
     * Set {@link com.ajoshow.funbase.libs.FirebaseAgent.UserNodeListener}
     * @param listener
     */
    public void setUserNodeListener(UserNodeListener listener){
        mUserListener = listener;
    }

    /**
     * Set {@link com.ajoshow.funbase.libs.FirebaseAgent.EventNodeListener}
     * @param listener
     */
    public void setEventNodeListener(EventNodeListener listener){
        mEventListener = listener;
    }

    /**
     * Whether already start listening to {@link #HOST_URL}/user/{userId} node.
     * @return
     */
    public boolean isRegistered(){
        return mUserRef != null;
    }


    /**
     * Combination of {@link #createEvent()} and {@link #invite(String, String)}
     * @param otherUserId
     * @return eventId
     */
    public String createEventAndInvite(String otherUserId){
        String eventId = createEvent();
        invite(otherUserId, eventId);
        return eventId;
    }

    /**
     * Start listen to {@link #HOST_URL}/event/{autoGenEventId} node.
     * Update value {@link #HOST_URL}/event/{eventId}/user/{userId} node.
     *
     * @return eventId, auto generated by Firebase
     */
    public String createEvent(){
        if(mEventRefs == null){
            mEventRefs = new HashMap<String, Firebase>();
        }

        if(mUserId == null){
            throw new IllegalStateException("Please register() first before an event can be created.");
        }

        Firebase eventRef = newFirebase(HOST_URL + "/event/");
        Firebase eventChildRef = eventRef.push();
        String eventId = eventChildRef.getName();

        joinEvent(eventId, mUserId);
        startListenEvent(eventId);
        return eventId;
    }

    /**
     * Update value {@link #HOST_URL}/event/{eventId}/user/{userId} node.
     * @param eventId
     * @param anyUserId
     */
    public void joinEvent(String eventId, String anyUserId){
        Firebase ref = newFirebase(HOST_URL + "/event/" + eventId + "/user/");
        Map<String, Object> updates = new HashMap<String, Object>();
        updates.put(anyUserId, System.currentTimeMillis());
        ref.updateChildren(updates);
    }

    public void updateEventState(String eventId, final EventState state){
        Firebase ref = newFirebase(HOST_URL + "/event/" + eventId + "/state/");
        ref.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData currentData) {
                currentData.setValue(state);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(FirebaseError error, boolean committed, DataSnapshot currentData) {

            }
        });
    }

    public void sendMessage(String eventId, final Message message){
        Firebase ref = newFirebase(HOST_URL + "/event/" + eventId + "/messages/");
        ref.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData currentData) {
                Map<String, Object> updates = new HashMap<String, Object>();
                updates.put(message.getSenderId(), message.getContent());
                currentData.setValue(updates);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(FirebaseError error, boolean committed, DataSnapshot currentData) {

            }
        });
    }

    /**
     * Update value on {@link #HOST_URL}/user/{otherUserId}/invite/{eventId} node.
     * @param otherUserId
     * @param eventId
     */
    public void invite(String otherUserId, String eventId){
        Firebase ref = newFirebase(HOST_URL + "/user/" + otherUserId + "/invite/");
        Map<String, Object> updates = new HashMap<String, Object>();
        updates.put(eventId, System.currentTimeMillis());
        ref.updateChildren(updates);
    }

    public void removeInvite(String userId, String eventId){
        Firebase ref = newFirebase(HOST_URL + "/user/" + userId + "/invite/" + eventId);
        ref.removeValue();
    }

    public void removeEvent(String eventId){
        Firebase ref = newFirebase(HOST_URL + "/event/" + eventId);
        ref.removeValue();
        stopListenEvent(eventId);
    }
}
