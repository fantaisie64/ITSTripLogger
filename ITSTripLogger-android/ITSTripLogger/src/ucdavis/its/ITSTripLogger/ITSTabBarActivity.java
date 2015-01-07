package ucdavis.its.ITSTripLogger;
import org.json.JSONObject;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import java.util.Collections;
import java.util.List;

import com.estimote.sdk.Beacon;

public class ITSTabBarActivity extends FragmentActivity {
	public static final String TAG = "ITSTabBarActivity";
	private FragmentTabHost tabHost;
	public ITSRecordFragment iTSRecordFragment = null;
	public ITSInfoFragment iTSInfoFragment = null;
	public ITSBeaconFragment iTSBeaconFragment = null;
	public ITSQuestionFragment iTSQuestionFragment = null;
	private boolean regetRecordData = false;
	private TextView titleTextView = null;
	
	Messenger mService = null;
    boolean mIsBound;
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    
    @SuppressLint("HandlerLeak")
	class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
        	String message;
            switch (msg.what) {
            case ITSService.MSG_UPDATE_ACTIONBAR_TITLE:
            	message = msg.getData().getString("message");
            	if(message.equals("success")){
            		String newString = (String) msg.obj;
            		if(titleTextView != null){
            			titleTextView.setText(newString);
            		}
            	}
            	break;
            case ITSService.MSG_GET_RECORDDATA:
            	message = msg.getData().getString("message");
            	if(message.equals("showRecordSuccess")){
            		JSONObject iTSReceivedDict = (JSONObject)msg.obj;
            		if(iTSRecordFragment != null){
            			iTSRecordFragment.callBackFromGetRecords(iTSReceivedDict);
            		}
            	}else if(message.equals("showRecordErrorFail")){
            		
            	}
            	break;
            case ITSService.MSG_UPDATE_BEACON:
            	message = msg.getData().getString("message");
            	if(message.equals("success")){
            		ITSCarList iTSCarList = (ITSCarList) msg.obj;
            		if(iTSBeaconFragment != null){
            			iTSBeaconFragment.replaceAdatperData(iTSCarList);
            		}
//            		for(Beacon b : beacons){
//            			Log.i(ITSTabBarActivity.TAG, "b = " + b.getMajor());
//            		}
            	}
            	break;
            default:
                super.handleMessage(msg);
                break;
            }
        }
    }
    
    private ServiceConnection mConnection = new ServiceConnection() {
    	@Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            //Log.i("ITSTabBarActivity", "onServiceConnected...");
            
            //if(iTSBeaconFragment != null)
            //	iTSBeaconFragment.replaceAdatperData(Collections.<Beacon>emptyList());
            
            try {
                Message msg = Message.obtain(null, ITSService.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even do anything with it
            }
            if(regetRecordData){
            	getRecordData();
            	regetRecordData = false;
            }
        }

    	@Override
        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been unexpectedly disconnected - process crashed.
            mService = null;
        }
    };
    
    void doBindService() {
        bindService(new Intent(this, ITSService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }
    void doUnbindService() {
        if (mIsBound) {
            // If we have received the service, and hence registered with it, then now is the time to unregister.
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null, ITSService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service has crashed.
                }
            }
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
        }
    }
    
    private void CheckIfServiceIsRunning() {
        if (ITSService.isRunning()) {
            doBindService();
        }else{
        	startService(new Intent(this, ITSService.class));
        	doBindService();
        }
    }
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		CheckIfServiceIsRunning();
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		this.getActionBar().setTitle("Sign Out");
	
	    setContentView(R.layout.itstabbar);
	    tabHost = (FragmentTabHost)findViewById(android.R.id.tabhost);
	    tabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);

	    //1
	    tabHost.addTab(tabHost.newTabSpec("Records")
	                          .setIndicator("Records"), 
	                   ITSRecordFragment.class, 
	                   null);
	    ((TextView)tabHost.getTabWidget().getChildAt(0).findViewById(android.R.id.title)).setTextSize(8);
	    
	    //2
	    tabHost.addTab(tabHost.newTabSpec("Info")
	                          .setIndicator("Info"), 
	                   ITSInfoFragment.class, 
	                   null);
	    ((TextView)tabHost.getTabWidget().getChildAt(1).findViewById(android.R.id.title)).setTextSize(8);
	    
	    //3
	    tabHost.addTab(tabHost.newTabSpec("Cars")
	                          .setIndicator("Cars"), 
	                   ITSBeaconFragment.class, 
	                   null);
	    ((TextView)tabHost.getTabWidget().getChildAt(2).findViewById(android.R.id.title)).setTextSize(8);
	    
	    //4
	    tabHost.addTab(tabHost.newTabSpec("Questions")
	                          .setIndicator("Questions"), 
	                   ITSQuestionFragment.class, 
	                   null);
	    ((TextView)tabHost.getTabWidget().getChildAt(3).findViewById(android.R.id.title)).setTextSize(8);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		titleTextView = new TextView(this);
		titleTextView.setText("");
		titleTextView.setTextColor(getResources().getColor(android.R.color.white));
		titleTextView.setPadding(10, 0, 10, 0);
		titleTextView.setTypeface(null, Typeface.BOLD);
		titleTextView.setTextSize(18);
        menu.add(0, 100, 1, null).setActionView(titleTextView).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		return true;
	}
	
	//sign out button
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
        	this.logout();
        	super.onBackPressed();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
	
	public void getRecordData(){
		if (mIsBound) {
			if (mService != null) {
				try {
                    Message msg = Message.obtain(null, ITSService.MSG_GET_RECORDDATA);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {
                }
			}else{
				regetRecordData = true;
			}
		}
	}
	
	private void logout(){
		//clear UI
		clearUserData();
		
		//clear service data
		if (mIsBound) {
			if (mService != null) {
				try {
                    Message msg = Message.obtain(null, ITSService.MSG_SIGNOUT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {
                }
			}
		}
	}
	
	//Block back button
	@Override
	public void onBackPressed() {
		
	}
	
	private void clearUserData(){
		//clear user data
		if(titleTextView != null)
			titleTextView.setText("");
		if(iTSRecordFragment != null){
			iTSRecordFragment.clearUserData();
		}
		if(iTSInfoFragment != null){
			iTSInfoFragment.clearUserData();
		}
		if(iTSBeaconFragment != null){
			iTSBeaconFragment.clearUserData();
		}
	}
}
