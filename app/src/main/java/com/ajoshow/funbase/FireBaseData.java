package com.ajoshow.funbase;
//
//import android.content.Context;
//import android.location.Location;
//import android.os.Handler;
//import android.os.Message;
//import android.util.Log;
//
//import com.firebase.client.ChildEventListener;
//import com.firebase.client.DataSnapshot;
//import com.firebase.client.Firebase;
//import com.firebase.client.FirebaseError;
//import com.firebase.client.GenericTypeIndicator;
//import com.firebase.client.ValueEventListener;
//import com.greenhouseapps.jink.R;
//import com.greenhouseapps.jink.config.Property;
//import com.greenhouseapps.jink.utils.AnalyticsEvent;
//import com.greenhouseapps.jink.utils.Constants;
//import com.greenhouseapps.jink.utils.ErrorConstants;
//import com.greenhouseapps.jink.utils.LocationUtils;
//import com.greenhouseapps.jink.utils.Utils;
//
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.TimeZone;
//
///**
// * Created by ted on 2014/3/6.
// */
public class FireBaseData {
//    private Handler mActivityHandler;
//    private Context mContext;
//    private Firebase sendStatusFirebase = null;
//    private static String firebaseUrl = Property.FIREBASE_URL_HOST;
//    private ChildEventListener mSelfEventListener;
//    private HashMap<String, ChildEventListener> mEventListeners = new HashMap<String, ChildEventListener>();
//    private Firebase mSelfListenFirebase = null;
//    private Firebase mConnectedRef = null;
//    private Firebase.CompletionListener mSetVauleListener = null;
//    private boolean isConnected = false;
//
//    public FireBaseData(Context context, Handler handler) {
//        mActivityHandler = handler;
//        this.mContext = context;
//
//        mConnectedRef = newFirebase(firebaseUrl + ".info/connected");
//        mConnectedRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot snapshot) {
//                boolean connected = snapshot.getValue(Boolean.class);
//                if (connected) {
//                    isConnected = true;
//                } else {
//                    isConnected = false;
//                }
//            }
//
//            @Override
//            public void onCancelled(FirebaseError error) {
//            }
//        });
//    }
//
//    /**
//     * Detecting connection state is online or offline.
//     * @return
//     */
//    public synchronized boolean isConnected(){
//        return isConnected;
//    }
//
//    public void selfListen(final String objectId) {
//        //list self firebase and child
//        if (mSelfListenFirebase != null) {
//            return;
//        }
//        mSetVauleListener = new Firebase.CompletionListener(){
//            @Override
//            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
//                if(firebaseError != null){
//                    sendErrorCode(ErrorConstants.FIREBASE_SET_VAULE_FAIL,firebaseError);
//                }
//            }
//        };
//        mSelfListenFirebase = newFirebase(firebaseUrl + "user/" + objectId);
//        mSelfEventListener = new ChildEventListener() {
//            @Override
//            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                Log.i("Firebase", "OnChildAdded " + dataSnapshot.getName());
//                handleSelfChildEvent(dataSnapshot);
//            }
//
//            @Override
//            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//                Log.i("Firebase", "OnChildChanged " + dataSnapshot.getName());
//                handleSelfChildEvent(dataSnapshot);
//            }
//
//            @Override
//            public void onChildRemoved(DataSnapshot dataSnapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onCancelled(FirebaseError firebaseError) {
//                if(firebaseError != null){
//                    sendErrorCode(ErrorConstants.FIREBASE_CANCEL_FAIL,firebaseError);
//                }
//            }
//
//
//        };
//
//        mSelfListenFirebase.addChildEventListener(mSelfEventListener);
//
//    }
//
//    private void handleSelfChildEvent(DataSnapshot dataSnapshot) {
//        String name = dataSnapshot.getName();
//        if (name.equals("invite")) {
//            //user is invited
//            Log.i(getClass().getName(), "have invite");
//            GenericTypeIndicator<Map<String, Object>> t = new GenericTypeIndicator<Map<String, Object>>() {};
//            Map<String, Object> inviteData = dataSnapshot.getValue(t);
//
//            Iterator it = inviteData.entrySet().iterator();
//            HashMap<String, UserTable> map = new HashMap<String, UserTable>();
//            while (it.hasNext()) {
//                Map.Entry pairs = (Map.Entry)it.next();
//                String eventId = (String) pairs.getKey();
//                LinkedHashMap host = (LinkedHashMap) pairs.getValue();
//                LinkedHashMap hostData  = (LinkedHashMap) host.get("host");
//                String participatorName = hostData.get("name").toString();
//                String participatorPhone = hostData.get("phone").toString();
//                String participatorObjectId = hostData.get("userid").toString();
//                LinkedHashMap currentLocation = (LinkedHashMap) hostData.get("currentLocation");
//                UserTable userTable = new UserTable();
//                userTable.setObjectId(participatorObjectId);
//                userTable.setEventId(eventId);
//                userTable.setCreatedAt(new Date());
//                userTable.setPhone(participatorPhone);
//                userTable.setName(participatorName);
//                userTable.setLocation(LocationUtils.convertDoubleToString(Double.parseDouble(currentLocation.get("latitude").toString()),
//                        Double.parseDouble(currentLocation.get("longitude").toString())));
//
//                map.put(participatorObjectId, userTable);
//                mSelfListenFirebase.child("/invite/" + eventId).removeValue();//remove child
//                it.remove(); // avoids a ConcurrentModificationException
//            }
//
//            List<UserTable> list = new ArrayList<UserTable>(map.values());
//            Object[] objects = new Object[2];
//            objects[0] = list;
//            Message message = new Message();
//            message.what = Constants.DATA_RECEIVE_EVENT_INVITE;
//            message.obj = objects;
//            mActivityHandler.sendMessage(message);
//
//        } else if (name.equals("respond")) {
//            //other user's response
//            Log.i(getClass().getName(), "have response");
//            GenericTypeIndicator<Map<String, Object>> t = new GenericTypeIndicator<Map<String, Object>>() {
//            };
//            Map<String, Object> respondData = dataSnapshot.getValue(t);
//            Iterator it = respondData.entrySet().iterator();
//            List<Object[]> list = new ArrayList<Object[]>();
//            while (it.hasNext()) {
//                double latitude = 0, longitude = 0;
//                Object[] data = new Object[4];
//                Map.Entry pairs = (Map.Entry)it.next();
//                String eventId = (String) pairs.getKey();
//                LinkedHashMap key = (LinkedHashMap) pairs.getValue();
//                LinkedHashMap participatorData = (LinkedHashMap) key.get("participator");
//                boolean isAccept = Boolean.valueOf(key.get("isAccept").toString());
//                if(participatorData != null) {
//                    LinkedHashMap location = (LinkedHashMap) participatorData.get("currentLocation");
//                    latitude = Double.parseDouble(location.get("latitude").toString());
//                    longitude = Double.parseDouble(location.get("longitude").toString());
//                }
//                data[0] = eventId;
//                data[1] = isAccept;
//                data[2] = latitude;
//                data[3] = longitude;
//                list.add(data);
//                mSelfListenFirebase.child("/respond/" + eventId).removeValue();//remove child
//                it.remove(); // avoids a ConcurrentModificationException
//            }
//            Message message = new Message();
//            message.what = Constants.DATA_RECEIVE_EVENT_RESPONSE;
//            message.obj = list;
//            mActivityHandler.sendMessage(message);
//
//        } else if (name.equals("delete")) {
//            Log.i(getClass().getName(), "have delete");
//            GenericTypeIndicator<Map<String, Object>> t = new GenericTypeIndicator<Map<String, Object>>() {
//            };
//            Map<String, Object> deleteData = dataSnapshot.getValue(t);
//            Iterator it = deleteData.entrySet().iterator();
//            List<String> list = new ArrayList<String>();
//            while (it.hasNext()) {
//                Map.Entry pairs = (Map.Entry)it.next();
//                String eventId = (String) pairs.getKey();
//                String response = deleteData.get(eventId).toString();
//                if (response.equalsIgnoreCase("YES")) {
//                    list.add(eventId);
//                }
//                mSelfListenFirebase.child("/delete/" + eventId).removeValue();//remove child
//                it.remove();
//            }
//            if(list.size() > 0){//有刪除的事件才傳送
//                Message message = new Message();
//                message.what = Constants.DATA_RECEIVE_EVENT_DELETE;
//                message.obj = list;
//                mActivityHandler.sendMessage(message);
//            }
//
//        }else if(name.equals("active")){
//            GenericTypeIndicator<Map<String, Object>> t = new GenericTypeIndicator<Map<String, Object>>() {
//            };
//            Map<String, Object> meetData = dataSnapshot.getValue(t);
//            Iterator it = meetData.entrySet().iterator();
//            List<Object[]> list = new ArrayList<Object[]>();
//            while (it.hasNext()) {
//                Object[] objects = new Object[3];
//                Map.Entry pairs = (Map.Entry) it.next();
//                String eventId = (String) pairs.getKey();
//                LinkedHashMap data = (LinkedHashMap) meetData.get(eventId);
//                LinkedHashMap startLocation = (LinkedHashMap) data.get("startLocation");
//                String longitude = startLocation.get("longitude").toString();
//                String latitude = startLocation.get("latitude").toString();
//                objects[0] = eventId;
//                objects[1] = latitude;
//                objects[2] = longitude;
//                list.add(objects);
//                mSelfListenFirebase.child("/active/" + eventId).removeValue();//remove child
//                it.remove();
//            }
//
//            Message message = new Message();
//            message.what = Constants.DATA_RECEIVE_EVENT_ACTIVE;
//            message.obj = list;
//            mActivityHandler.sendMessage(message);
//
//        }else if(name.equals("start")){
//            GenericTypeIndicator<Map<String, Object>> t = new GenericTypeIndicator<Map<String, Object>>() {
//            };
//            Map<String, Object> startData = dataSnapshot.getValue(t);
//            Iterator it = startData.entrySet().iterator();
//            List<String> list = new ArrayList<String>();
//            while (it.hasNext()) {
//                Map.Entry pairs = (Map.Entry)it.next();
//                String eventId = (String) pairs.getKey();
//                String response = startData.get(eventId).toString();
//                if (response.equalsIgnoreCase("YES")) {
//                    list.add(eventId);
//                }
//                mSelfListenFirebase.child("/start/" + eventId).removeValue();//remove child
//                it.remove();
//            }
//            if(list.size() > 0){//有事件才傳送
//                Message message = new Message();
//                message.what = Constants.DATA_RECEIVE_EVENT_START;
//                message.obj = list;
//                mActivityHandler.sendMessage(message);
//            }
//
//        }else if(name.equals("meet")){
//            GenericTypeIndicator<Map<String, Object>> t = new GenericTypeIndicator<Map<String, Object>>() {
//            };
//            Map<String, Object> meetData = dataSnapshot.getValue(t);
//            Iterator it = meetData.entrySet().iterator();
//            List<Object[]> list = new ArrayList<Object[]>();
//            while (it.hasNext()) {
//                Object[] objects = new Object[2];
//                Map.Entry pairs = (Map.Entry)it.next();
//                String eventId = (String) pairs.getKey();
//                LinkedHashMap data = (LinkedHashMap) meetData.get(eventId);
//                String time = data.get("finishTime").toString();
//                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
//                simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
//                try {
//                    Date date = simpleDateFormat.parse(time);
//                    objects[0] = eventId;
//                    objects[1] = date;
//                    list.add(objects);
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                }
//                mSelfListenFirebase.child("/meet/" + eventId).removeValue();//remove child
//                it.remove();
//            }
//
//            Message message = new Message();
//            message.what = Constants.DATA_RECEIVE_EVENT_MEET;
//            message.obj = list;
//            mActivityHandler.sendMessage(message);
//        }else if(name.equals("finish")){
//            GenericTypeIndicator<Map<String, Object>> t = new GenericTypeIndicator<Map<String, Object>>() {
//            };
//            Map<String, Object> finishData = dataSnapshot.getValue(t);
//            Iterator it = finishData.entrySet().iterator();
//            List<String> list = new ArrayList<String>();
//            while (it.hasNext()) {
//                Map.Entry pairs = (Map.Entry)it.next();
//                String eventId = (String) pairs.getKey();
//                String response = finishData.get(eventId).toString();
//                if (response.equalsIgnoreCase("YES")) {
//                    list.add(eventId);
//                }
//                mSelfListenFirebase.child("/finish/" + eventId).removeValue();//remove child
//                it.remove();
//            }
//            if(list.size() > 0){//有事件才傳送
//                Message message = new Message();
//                message.what = Constants.DATA_RECEIVE_EVENT_FINISH;
//                message.obj = list;
//                mActivityHandler.sendMessage(message);
//            }
//
//
//        }else if(name.equals("expire")){
//
//            GenericTypeIndicator<Map<String, Object>> t = new GenericTypeIndicator<Map<String, Object>>() {
//            };
//            Map<String, Object> expireData = dataSnapshot.getValue(t);
//            Iterator it = expireData.entrySet().iterator();
//            List<String> list = new ArrayList<String>();
//            while (it.hasNext()) {
//                Map.Entry pairs = (Map.Entry)it.next();
//                String eventId = (String) pairs.getKey();
//                String response = expireData.get(eventId).toString();
//                if (response.equalsIgnoreCase("YES")) {
//                    list.add(eventId);
//                }
//                mSelfListenFirebase.child("/expire/" + eventId).removeValue();//remove child
//                it.remove();
//            }
//            if(list.size() > 0){//有事件才傳送
//                Message message = new Message();
//                message.what = Constants.DATA_RECEIVE_EVENT_EXPIRE;
//                message.obj = list;
//                mActivityHandler.sendMessage(message);
//            }
//        }else if (name.equals("sync")) {
//
//            GenericTypeIndicator<Map<String, Object>> t = new GenericTypeIndicator<Map<String, Object>>() {
//            };
//            Map<String, Object> syncData = dataSnapshot.getValue(t);
//            Iterator it = syncData.entrySet().iterator();
//            List<String> askList = new ArrayList<String>();
//            List<Object[]> objectList = new ArrayList<Object[]>();
//            while (it.hasNext()) {
//                Map.Entry pairs = (Map.Entry)it.next();
//                String eventId = (String) pairs.getKey();
//                Object value = syncData.get(eventId);
//
//                if(value instanceof String){
//                    String val = String.valueOf(value);
//                    if(val.equals("ask")){
//                        askList.add(eventId);
//                    }
//                }
//                else if(value instanceof Map){
//                    Map<String, Object> val = (Map<String, Object>) value;
//                    RestoreData data = new RestoreData();
//                    data.setFromHashMap(val);
//
//                    // it is possible to get null here
//                    // another user may not be using Jink at this point.
//                    Map<String, Object> userData = (Map<String, Object>)val.get("user");
//                    data.setFromHashMap(userData);
//                    Object[] objects = new Object[2];
//                    objects[0] = eventId;
//                    objects[1] = data;
//                    objectList.add(objects);
//                };
//
//                mSelfListenFirebase.child("/sync/" + eventId).removeValue();//remove child
//                it.remove();
//            }
//
//            if(askList.size() > 0){
//                Message message = new Message();
//                message.what = Constants.DATA_ASK_RESTORE_EVENT;
//                message.obj = askList;
//                mActivityHandler.sendMessage(message);
//            }
//
//            if(objectList.size() > 0){
//                Message message = new Message();
//                message.what = Constants.DATA_RESPOND_RESTORE_EVENT;
//                message.obj = objectList;
//                mActivityHandler.sendMessage(message);
//            }
//        }
//    }
//
//    public void eventListen(final String eventId,final String myObjectId) {
//        final Firebase firebaseEvent = newFirebase(firebaseUrl + "event/" + eventId);
//
//        if(mEventListeners.get(eventId) != null) {
//            //already listening
//            return;
//        }
//        ChildEventListener childEventListener = new ChildEventListener() {
//
//            @Override
//            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                GenericTypeIndicator<Map<String, Object>> t = new GenericTypeIndicator<Map<String, Object>>() {};
//                if(dataSnapshot.getName().equals("paused")) {
//                    handlePauseChange(dataSnapshot.getValue(t).get("count"), dataSnapshot.getValue(t).get("enabled"), eventId);
//                    return;
//                }
//                for (DataSnapshot child : dataSnapshot.getChildren()) {
//                    Log.i("FIREBASE", "ADDED:" + child.getValue(t).toString());
//                    Log.i("FIREBASE" , "ADDED: " + dataSnapshot.getChildrenCount());
//                    String objectId = child.getRef().getName();
//                    String eventId = child.getRef().getParent().getParent().getName();
//                    //if child has change ,exclude self objectId and send other side location
//                    if (myObjectId != null && !myObjectId.equals(objectId)) {
//
//                        if (child.getValue(t).get("location") != null) {
//                            String location = child.getValue(t).get("location").toString();
//                            Message message = new Message();
//                            message.what = Constants.DATA_RECEIVE_EVENT_LOCATION;
//                            Object[] result = new Object[3];
//                            result[0] = objectId;
//                            result[1] = location;
//                            result[2] = eventId;
//                            message.obj = result;
//                            mActivityHandler.sendMessage(message);
//                            //iOS does not remove location value
//                            firebaseEvent.child("/user/" + objectId + "/location").removeValue();
//                            Log.i("FIREBASE", "ADD_LOCATION");
//                        }
//                        else if(child.getValue(t).get("userData") != null){
//                            String userData = child.getValue(t).get("userData").toString();
//                            if("YES".equals(userData)){
//                                // have been notified that we need to sync another user.
//                                Message message = new Message();
//                                message.what = Constants.DATA_RECEIVE_SYNC_ANOTHER_USER;
//                                message.obj = objectId;
//                                mActivityHandler.sendMessage(message);
//                            }
//                            firebaseEvent.child("/user/" + objectId + "/userData").removeValue();
//                        }else if (child.getValue(t).get("statuses") != null) {
//                            LinkedHashMap<String, Object> statusList = (LinkedHashMap<String, Object>) child.getValue(t).get("statuses");
//                            Object[] statusId = statusList.keySet().toArray();
//                            //use HashMap to avoid duplicate
//                            List<StatusTable> statuses =  new ArrayList<StatusTable>();
//                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
//                            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
//
//                            for (int pos = 0; pos < statusList.size(); pos++) {//scan all status include older
//                                LinkedHashMap<String, Object> statusContentList = (LinkedHashMap<String, Object>) statusList.get(statusId[pos]);
//                                StatusTable status = new StatusTable();
//                                status.setObjectId(Long.valueOf(statusId[pos].toString()));
//                                status.setUserObjectId(objectId);
//                                status.setMessage(statusContentList.get("message").toString());
//                                status.setEventId(eventId);
//                                try {
//                                    status.setCreatedAt(simpleDateFormat.parse(statusContentList.get("time").toString()));
//                                } catch (ParseException e) {
//                                    e.printStackTrace();
//                                }catch (NullPointerException e){
//                                    status.setCreatedAt(new Date());
//                                    e.printStackTrace();
//                                }
//                                statuses.add(status);
//                                Log.i("FIREBASE","New Message " + status.getMessage());
////                                firebaseEvent.child("/user/" + objectId + "/statuses/" + String.valueOf(statusId[pos])).removeValue();//remove child
//                            }
//                            Log.i("FIREBASE",statuses.size() + " New Message ");
//                            Message message = new Message();
//                            message.what = Constants.DATA_RECEIVE_STATUS;
//                            message.obj = statuses;
//                            mActivityHandler.sendMessage(message);
//                            firebaseEvent.child("/user/" + objectId + "/statuses").removeValue();
//                            Log.i("FIREBASE","On Change, New Message from " + objectId);
//                        }
//
//                    }
//                }
//            }
//
//            @Override
//            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//                GenericTypeIndicator<Map<String, Object>> t = new GenericTypeIndicator<Map<String, Object>>() {
//                };
//                if(dataSnapshot.getName().equals("paused")) {
//                    handlePauseChange(dataSnapshot.getValue(t).get("count"), dataSnapshot.getValue(t).get("enabled"), eventId);
//                    return;
//                }
//                Log.i("FIREBASE" , "CHANGES: " + dataSnapshot.getChildrenCount());
//                for (DataSnapshot child : dataSnapshot.getChildren()) {
//                    Log.i("FIREBASE", "CHANGES:" + child.getValue(t).toString());
//                    String objectId = child.getRef().getName();
//
//                    //if child has change ,exclude self userObjectId and send other side location
//                    if (myObjectId != null && !myObjectId.equals(objectId)) {
//                        if (child.getValue(t).get("location") != null) {
//                            String location = child.getValue(t).get("location").toString();
//                            Message message = new Message();
//                            message.what = Constants.DATA_RECEIVE_EVENT_LOCATION;
//                            Object[] result = new Object[3];
//                            result[0] = objectId;
//                            result[1] = location;
//                            result[2] = eventId;
//                            message.obj = result;
//                            mActivityHandler.sendMessage(message);
//                            firebaseEvent.child("/user/" + objectId + "/location").removeValue();
//                            Log.i("FIREBASE","CHANGE_LOCATION");
//                        }
//
//                        // following node should only present on child_add
//                        // so if the condition here is true, then that means any accident happened
//                        // we have to remove it.
//                        else if(child.getValue(t).get("userData") != null){
//                            String userData = child.getValue(t).get("userData").toString();
//                            if("YES".equals(userData)){
//                                // have been notified that we need to sync another user.
//                                Message message = new Message();
//                                message.what = Constants.DATA_RECEIVE_SYNC_ANOTHER_USER;
//                                message.obj = objectId;
//                                mActivityHandler.sendMessage(message);
//                            }
//                            firebaseEvent.child("/user/" + objectId + "/userData").removeValue();
//                        }
//
//                        else if (child.getValue(t).get("statuses") != null) {
//                            LinkedHashMap<String, Object> statusList = (LinkedHashMap<String, Object>) child.getValue(t).get("statuses");
//                            Object[] statusId = statusList.keySet().toArray();
//                            //use HashMap to avoid duplicate
//                            List<StatusTable> statuses =  new ArrayList<StatusTable>();
//                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
//                            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
//
//                            for (int pos = 0; pos < statusList.size(); pos++) {//scan all status include older
//                                LinkedHashMap<String, Object> statusContentList = (LinkedHashMap<String, Object>) statusList.get(statusId[pos]);
//                                StatusTable status = new StatusTable();
//                                status.setObjectId(Long.valueOf(statusId[pos].toString()));
//                                status.setUserObjectId(objectId);
//                                status.setMessage(statusContentList.get("message").toString());
//                                status.setEventId(eventId);
//                                try {
//                                    status.setCreatedAt(simpleDateFormat.parse(statusContentList.get("time").toString()));
//                                } catch (ParseException e) {
//                                    e.printStackTrace();
//                                }catch (NullPointerException e){
//                                    status.setCreatedAt(new Date());
//                                    e.printStackTrace();
//                                }
//                                statuses.add(status);
//                                Log.i("FIREBASE","New Message " + status.getMessage());
////                                firebaseEvent.child("/user/" + objectId + "/statuses/" + String.valueOf(statusId[pos])).removeValue();//remove child
//                            }
//                            Log.i("FIREBASE",statuses.size() + " New Message ");
//                            Message message = new Message();
//                            message.what = Constants.DATA_RECEIVE_STATUS;
//                            message.obj = statuses;
//                            mActivityHandler.sendMessage(message);
//                            firebaseEvent.child("/user/" + objectId + "/statuses").removeValue();
//                            Log.i("FIREBASE","On Change, New Message from " + objectId);
//                        }
//                    }
//                }
//            }
//
//            @Override
//            public void onChildRemoved(DataSnapshot dataSnapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onCancelled(FirebaseError firebaseError) {
//                if(firebaseError != null){
//                    sendErrorCode(ErrorConstants.FIREBASE_CANCEL_FAIL,firebaseError);
//                }
//            }
//        };
//
//        //prevent double listener
//        for (ChildEventListener getChildEventListener : mEventListeners.values()) {
//            if (childEventListener == getChildEventListener) {
//                mEventListeners.remove(eventId);
//            }
//        }
//
//        firebaseEvent.addChildEventListener(childEventListener);
//        mEventListeners.put(eventId, childEventListener);
//        Log.i("Status", "now listening to " + eventId);
//    }
//
//    private void handlePauseChange(Object count, Object enabled, String eventId) {
//        if((Integer) count > 0) {
//            //only handle pause count > 0, since 0 is just for initiating, and will not pause
//            Message message = new Message();
//            message.what = Constants.DATA_RECEIEVE_EVENT_PAUSE;
//            Object[] results = new Object[3];
//            results[0] = count;
//            results[1] = enabled;
//            results[2] = eventId;
//            message.obj = results;
//            mActivityHandler.sendMessage(message);
//        }
//    }
//
//    private Firebase newFirebase(String url){
//        Firebase f = new Firebase(url);
//        f.auth(Property.FIREBASE_SECRETS_TOKEN, new Firebase.AuthListener() {
//            @Override
//            public void onAuthError(FirebaseError firebaseError) {
//
//            }
//
//            @Override
//            public void onAuthSuccess(Object o) {
//
//            }
//
//            @Override
//            public void onAuthRevoked(FirebaseError firebaseError) {
//
//            }
//        });
//        return f;
//    }
//
//
//    public void eventSetLocation(String eventId, String location, String objectId) {
//        Firebase firebaseLocation = newFirebase(firebaseUrl + "event/" + eventId + "/user/" + objectId + "/location/");
//        firebaseLocation.setValue(location);
//    }
//
//
//    //other user objectId , self location
//    public void activeEvent(String objectId, String eventId, Location location){
//        Firebase firebaseActiveEvent = newFirebase(firebaseUrl + "user/" + objectId + "/active/" + eventId + "/startLocation/");
//        Map<String, Object> locationMap = new HashMap<String, Object>();
//        locationMap.put("latitude", String.valueOf(location.getLatitude()));
//        locationMap.put("longitude", String.valueOf(location.getLongitude()));
//        firebaseActiveEvent.setValue(locationMap);
//    }
//
//    //other user objectId
//    public void startEvent(String objectId, String eventId){
//        Firebase firebaseStartEvent = newFirebase(firebaseUrl + "user/" + objectId + "/start/" + eventId);
//        firebaseStartEvent.setValue(mContext.getResources().getString(R.string.yes),mSetVauleListener);
//    }
//
//    public void meetEvent(String objectId, String eventId){
//        Firebase firebaseMeetEvent = newFirebase(firebaseUrl + "user/" + objectId + "/meet/" + eventId);
//        Date date = new Date();
//        //set to 5 minute after
//        date.setTime(date.getTime() + 1000 * 60 * 5);
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
//        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
//        firebaseMeetEvent.child("finishTime").setValue(simpleDateFormat.format(date), mSetVauleListener);
//    }
//
//    public void finishEvent(String objectId, String eventId) {
//        removeEventListener(eventId);
//        Firebase firebaseFinishEvent = newFirebase(firebaseUrl + "user/" + objectId + "/finish/" + eventId);
//        firebaseFinishEvent.setValue(mContext.getResources().getString(R.string.yes), mSetVauleListener);
//    }
//
//    public void pauseEvent(String objectId, String eventId, int pauseCount, boolean enable) {
//        Firebase firebasePauseEvent = newFirebase(firebaseUrl + "event/" + eventId + "/paused/");
//        Map<String, Object> pauseMap = new HashMap<String, Object>();
//        pauseMap.put("count", pauseCount);
//        pauseMap.put("enabled", enable);
//
//        firebasePauseEvent.setValue(pauseMap, mSetVauleListener);
//    }
//
//    /**
//     * Sending a request to Firebase, asking for restore event data.
//     * @param userId another party's user id
//     * @param eventId
//     */
//    public void askRestoreEvent(String userId, String eventId){
//        Firebase firebase = newFirebase(firebaseUrl +"user/"+ userId+"/sync/" + eventId);
//        firebase.setValue("ask",mSetVauleListener);
//    }
//
//    /**
//     * Sending a request to Firebase, set corresponding value for the event.
//     * @param userId another party's user id
//     * @param eventId
//     * @param data {@link RestoreData}
//     */
//    public void respondRestoreEvent(String userId, String eventId, RestoreData data){
//        Firebase firebase = newFirebase(firebaseUrl +"user/"+ userId+"/sync/" + eventId);
//        firebase.setValue(data.getEventHashMap(),mSetVauleListener);
//        firebase.child("user").setValue(data.getUserHashMap(), mSetVauleListener);
//    }
//
//    /**
//     * Sending a request to Firebase, notify another user that this user data is been updated.
//     * @param userId
//     * @param eventId
//     */
//    public void notifyUserUpdated(String userId, String eventId){
//        Firebase firebase = newFirebase(firebaseUrl + "event/" + eventId + "/user/" + userId + "/userData");
//        firebase.setValue("YES", mSetVauleListener);
//    }
//
//    public void removeSelfListener(){
//        if(mSelfListenFirebase != null){
//            mSelfListenFirebase.removeEventListener(mSelfEventListener);
//        }
//    }
//
//    public void removeEventListener(String eventId) {
//        Firebase firebaseEvent = newFirebase(firebaseUrl + "event/" + eventId);
//        if (mEventListeners.get(eventId) != null) {
//            firebaseEvent.removeEventListener(mEventListeners.get(eventId));
//        }
//
//    }
//
//
//    public void removeAllEventListener() {
//        Firebase firebaseEvent;
//
//        for(int i = 0; i < mEventListeners.size(); i ++) {
//            String eventId = (String) mEventListeners.keySet().toArray()[i];
//            firebaseEvent = newFirebase((firebaseUrl + "event/" + eventId));
//            firebaseEvent.removeEventListener(mEventListeners.get(eventId));
//            ;
//        }
//    }
//
//    private void sendErrorCode(int errorCase , FirebaseError firebaseError) {
//        Message message = new Message();
//        String[] results = new String[1];
//        results[0] = firebaseError.getMessage();
//        message.what = errorCase;
//        mActivityHandler.sendMessage(message);
//    }
//
//    public void sendStatus(final List<SendStatus> userList, final String sendStatus, final String myObjectId, final boolean isPreloaded){
//
//        Firebase connectRef = newFirebase(firebaseUrl +  ".info/connected");
//        connectRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot snapshot) {
//                boolean connected = snapshot.getValue(Boolean.class);
//                if (connected) {
//                    isConnected = true;
//                    _sendStatus(userList, sendStatus, myObjectId, isPreloaded);
//                } else {
//                    isConnected = false;
//                    Message message = new Message();
//                    Object[] results = {userList, sendStatus, isPreloaded};
//                    message.what = Constants.SEND_STATUS_ERROR;
//                    message.obj = results;
//                    mActivityHandler.sendMessage(message);
//                }
//            }
//
//            @Override
//            public void onCancelled(FirebaseError error) {
//            }
//        });
//    }
//
//    /**
//     * ErrorList is all firebase callback error list
//     * SuccessList is all success send out status will to notification
//     * Only use firebase to send status so getTime equal unique status ID for identification status
//     */
//    private void _sendStatus(final List<SendStatus> userList, final String sendStatus, final String myObjectId, final boolean isPreloaded) {
//        final List<SendStatus> errorList= new ArrayList<SendStatus>();
//        final List<SendStatus> successList= new ArrayList<SendStatus>();
//
//        for (final SendStatus status : userList) {
//            Date now = new Date();
//            sendStatusFirebase = newFirebase(firebaseUrl + "event/" + status.getEventId() + "/user/" + myObjectId + "/statuses/" + String.valueOf(now.getTime()));
//            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
//            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
//            Map<String, Object> updates = new HashMap<String, Object>();
//            updates.put("message", sendStatus);
//            updates.put("time", simpleDateFormat.format(now).toString());
//
//            sendStatusFirebase.setValue(updates, new Firebase.CompletionListener() {
//                @Override
//                public void onComplete(FirebaseError firebaseError, Firebase firebase) {
//                    if (firebaseError == null) {
//                        successList.add(status);
//                    } else {
//                        errorList.add(status);
//                    }
//                    if ((successList.size() + errorList.size()) == userList.size()) {//全部callback都已經處理完成以後
//                        sendCallBackResult();
//                    }
//                }
//
//                private void sendCallBackResult() {
//                    Message message = new Message();
//                    Object[] results = new Object[3];
//                    results[1] = sendStatus;
//                    results[2] = isPreloaded;
//
//                    if (successList.size() != 0) {
//                        HashMap<String, Object> properties = new HashMap<String, Object>();
//                        properties.put("preloaded", isPreloaded);
//                        Utils.getMixpanel().track(AnalyticsEvent.MESSAGING_SENT_MESSAGE, properties);
//
//                        results[0] = successList;
//                        message.what = Constants.SEND_STATUS_NOTIFICATION;
//                        message.obj = results;
//                        mActivityHandler.sendMessage(message);
//                    }
//
//                    if (errorList.size() != 0) {//have send fail status , will be resend
//                        results[0] = errorList;
//                        message.what = Constants.SEND_STATUS_ERROR;
//                        message.obj = results;
//                        mActivityHandler.sendMessage(message);
//                    }
//
//                }
//
//            });
//        }
//
//    }
//
}
