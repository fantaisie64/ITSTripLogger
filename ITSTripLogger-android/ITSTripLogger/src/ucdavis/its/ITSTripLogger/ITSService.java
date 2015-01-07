package ucdavis.its.ITSTripLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.estimote.sdk.Beacon;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

public class ITSService extends Service {
	public static final String TAG = "ITSService";
	private ITSConnectionManager iTSConnectionManager;
	private ITSBeaconManager iTSBeaconManager;
	private NotificationManager nm;
    private static boolean isRunning = false;
    public static boolean isLoggedIn = false;
    public static String userName = null;
    public static String password = null;
    private ArrayList<String> userInfo = null;

    ArrayList<Messenger> mClients = new ArrayList<Messenger>(); // Keeps track of all current registered clients.
    int mValue = 0; // Holds last value set by a client.
    public static final int MSG_SIGNIN = 0;
    public static final int MSG_SIGNOUT = 1;
    public static final int MSG_UPDATE_ACTIONBAR_TITLE = 2;
    public static final int MSG_REGISTER_CLIENT = 3;
    public static final int MSG_UNREGISTER_CLIENT = 4;
    public static final int MSG_GET_RECORDDATA = 5;
    public static final int MSG_UPDATE_BEACON = 6;
    public static final int MSG_UPLOAD_RECORDDATA = 7;
    public static final int MSG_UPLOAD_STOREDDATA = 8;
    public static final int MSG_SHOW_CAR = 9;
    public static final int MSG_INSERT_CAR = 10;
    public static final int MSG_CONNECTION = 11;
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    
    public JSONObject userJSONObject = null;


    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }
    
    @SuppressLint("HandlerLeak")
	class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_REGISTER_CLIENT:
                mClients.add(msg.replyTo);
                break;
            case MSG_UNREGISTER_CLIENT:
                mClients.remove(msg.replyTo);
                break;
            case MSG_SIGNIN:
            	//if(msg.obj instanceof ArrayList<?>) {
            	userInfo = (ArrayList<String>) msg.obj;
            	//}
                Log.i("ITSService", "signing in..." + userInfo.get(0));
                iTSConnectionManager.verifyUser(userInfo.get(0), userInfo.get(1));
                break;
            case MSG_SIGNOUT:
            	Log.i("ITSService", "signing out");
            	ITSService.this.logout();
            	break;
            case MSG_GET_RECORDDATA:
            	Log.i("ITSService", "getting record data...");
            	iTSConnectionManager.getRecordData();
            	break;
            default:
                super.handleMessage(msg);
            }
        }
    }
    
    private void sendMessageToUI(int msgWhat, JSONObject iTSReceivedDict){
        for (int i=mClients.size()-1; i>=0; i--) {
        	try {
        		Bundle b = new Bundle();
                b.putString("mode", iTSReceivedDict.getString("mode"));
                b.putString("message", iTSReceivedDict.getString("message"));
                Message msg = Message.obtain(null, msgWhat, iTSReceivedDict);
                msg.setData(b);
                try {
                    //Send data as a String
                    mClients.get(i).send(msg);

                } catch (RemoteException e) {
                    // The client is dead. Remove it from the list; we are going through the list from back to front so this is safe to do inside the loop.
                    mClients.remove(i);
                }
        	} catch (Exception e){
        		e.printStackTrace();
        	}
        }
    }
    
    private void sendMessageToUIwithObject(int msgWhat, String mode, String message, Object iTSObject){
        for (int i=mClients.size()-1; i>=0; i--) {
        	try {
        		Bundle b = new Bundle();
                b.putString("mode", mode);
                b.putString("message", message);
                Message msg = Message.obtain(null, msgWhat, iTSObject);
                msg.setData(b);
                try {
                    //Send data as a String
                    mClients.get(i).send(msg);

                } catch (RemoteException e) {
                    // The client is dead. Remove it from the list; we are going through the list from back to front so this is safe to do inside the loop.
                    mClients.remove(i);
                }
        	} catch (Exception e){
        		e.printStackTrace();
        	}
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("ITSService", "Service Started.");
        iTSConnectionManager = ITSConnectionManager.getInstance();
        iTSConnectionManager.iTSService = this;
        iTSBeaconManager = ITSBeaconManager.getInstance(this);
        iTSBeaconManager.iTSService = this;
        isRunning = true;
    }
    
    private void showNotification() {
//    	String s = "ITSService Working";
//        nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);	
//        CharSequence text = s;
//        @SuppressWarnings("deprecation")
//		Notification notification = new Notification(R.drawable.ic_launcher, text, System.currentTimeMillis());
//        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, ITSSignInActivity.class), 0);
//        //notification.setLatestEventInfo(this, s, text, contentIntent);
//        nm.notify(s, notification);
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Received start id " + startId + ": " + intent);
        //off-line login
        loadUserPlist();
        if(userJSONObject != null){
        	try{
        	if(userJSONObject.getString("username") != null &&
        		!userJSONObject.getString("username").equals("")){
        		ITSService.userName = userJSONObject.getString("username");
        		if(userJSONObject.getString("password") != null &&
        		!userJSONObject.getString("password").equals("")){
	        		ITSService.password = userJSONObject.getString("password");
	        		if(userJSONObject.getString("sessionToken") != null &&
	                	!userJSONObject.getString("sessionToken").equals("")){
	        			Log.i(TAG, "offline logging in...");
	        			if(iTSConnectionManager != null)
	        				iTSConnectionManager.offlineLogin(userJSONObject.getString("sessionToken"), userJSONObject.getJSONObject(("userData")));
	        			this.login();
	        		}
        		}
        	}
        	}catch(Exception e){
        		
        	}
        }
        return START_STICKY; // run until explicitly stopped.
    }

    public static boolean isRunning()
    {
        return isRunning;
    }

    @Override
    public void onDestroy() {
    	ITSService.isLoggedIn = false;
    	iTSBeaconManager.stopBeaconManager();
    	iTSBeaconManager.disconnectBeaconManager();
    	iTSConnectionManager.clearUserData();
    	iTSBeaconManager.clearUserData();
        super.onDestroy();
        Log.i("ITSService", "Service Stopped.");
        isRunning = false;
    }
    
    public void login(){
    	ITSService.isLoggedIn = true;
    	iTSBeaconManager.connectBeaconManager();
    }
    
    public void logout(){
    	ITSService.isLoggedIn = false;
    	ITSService.userName = null;
    	ITSService.password = null;
    	userJSONObject.remove("password");
    	userJSONObject.remove("sessionToken");
    	userJSONObject.remove("userData");
    	saveUserPlist();
    	iTSConnectionManager.signingOut = true;
    	iTSBeaconManager.clearUserData();
    	iTSBeaconManager.stopBeaconManager();
    }
    
    public void clearConnectionManagerUserData(){
    	iTSConnectionManager.clearUserData();
    }
    
    public void callBackFromVerifyUser(JSONObject iTSReceivedDict){
       	if(iTSReceivedDict != null){
       		sendMessageToUI(ITSService.MSG_SIGNIN, iTSReceivedDict);
       		try{
       			if(iTSReceivedDict.getString("message").equals("signinSuccess")){
       				this.login();
       				if(userInfo != null){
       					ITSService.userName = userInfo.get(0);
       					ITSService.password = userInfo.get(1);
       					userJSONObject.put("username", userInfo.get(0));
       					userJSONObject.put("password", userInfo.get(1));
       					userJSONObject.put("sessionToken", iTSConnectionManager.getITSSessionToken());
       					userJSONObject.put("userData", iTSReceivedDict.getJSONObject("data"));
       					saveUserPlist();
       				}
       			}
       		}catch(Exception e){
       			
       		}
        }
	}
    
    public void callBackFromConnectionFail(){
    	for (int i=mClients.size()-1; i>=0; i--) {
        	try {
        		Bundle b = new Bundle();
                b.putString("mode", "connection");
                b.putString("message", "connection fail");
                Message msg = Message.obtain(null, ITSService.MSG_CONNECTION, null);
                msg.setData(b);
                try {
                    //Send data as a String
                    mClients.get(i).send(msg);

                } catch (RemoteException e) {
                    // The client is dead. Remove it from the list; we are going through the list from back to front so this is safe to do inside the loop.
                    mClients.remove(i);
                }
        	} catch (Exception e){
        		e.printStackTrace();
        	}
        }
    }
    
    public void callBackFromGetRecordData(JSONObject iTSReceivedDict){
    	if(iTSReceivedDict != null){
    		sendMessageToUI(ITSService.MSG_GET_RECORDDATA, iTSReceivedDict);
    	}
    }
    
    public void callBackFromUpdatingBeacon(ITSCarList iTSCarList){
    	if(iTSCarList != null){
    		sendMessageToUIwithObject(ITSService.MSG_UPDATE_BEACON, "updateBeacon", "success", iTSCarList);
    	}
    }
    
    public void updateActionBarTitle(String newString){
		if(newString != null){
			sendMessageToUIwithObject(ITSService.MSG_UPDATE_ACTIONBAR_TITLE, "updateActionBarTitle", "success", newString);
		}
	}
    
    public void uploadRecordData(ITSRecordData recordData){
    	if(iTSConnectionManager != null){
    		iTSConnectionManager.uploadRecordData(recordData);
    	}
    }
    
    public void showCarNames(){
    	if(iTSConnectionManager != null){
    		iTSConnectionManager.showCarNames();
    	}
	}
    
    public void callBackFromShowCarNames(JSONObject iTSReceivedDict){
    	if(iTSBeaconManager != null){
    		iTSBeaconManager.callBackFromShowCarNames(iTSReceivedDict);
		}
    }
	
	private void loadUserPlist() {
		try{
			userJSONObject = ITSJSONSharedPreferences.loadJSONObject(this, "user", "user");
		}catch(Exception e){
   			
   		}
	}
	
	private void saveUserPlist(){
		 ITSJSONSharedPreferences.saveJSONObject(this, "user", "user", userJSONObject);
	}
}
