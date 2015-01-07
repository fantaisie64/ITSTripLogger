package ucdavis.its.ITSTripLogger;

import java.util.ArrayList;

import org.json.JSONObject;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.*;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import android.widget.TextView.OnEditorActionListener;
import android.text.Editable;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.TextWatcher;


public class ITSSignInActivity extends Activity {
	private static String TAG = "ITSSignInActivity";
	private EditText userNameEditText;
	private EditText passwordEditText;
	private TextView messageTextView;
	private Button signInButton;
	private TextView signUpTextView;
	
	Messenger mService = null;
    boolean mIsBound;
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    ArrayList<String> userInfo = new ArrayList<String>();
	
	@SuppressLint("HandlerLeak")
	class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
        	String message;
            switch (msg.what) {
            case ITSService.MSG_SIGNIN:
            	//String mode = msg.getData().getString("mode");
            	message = msg.getData().getString("message");
            	if(message.equals("signinSuccess")){
	            	JSONObject resultData = (JSONObject)msg.obj;
	            	messageTextView.setText("signin success");
	            	Intent intent = new Intent(ITSSignInActivity.this, ITSTabBarActivity.class);
	            	startActivity(intent);
	            	passwordEditText.setText("");
	            	messageTextView.setText("");
            	}else if(message.equals("signinErrorFail")){
            		messageTextView.setText("signin error fail");
            	}else if(message.equals("signinFail")){
            		messageTextView.setText("wrong username or password");
            	}
                break;
            case ITSService.MSG_CONNECTION:
            	message = msg.getData().getString("message");
            	if(message != null)
            		messageTextView.setText(message);
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
            try {
                Message msg = Message.obtain(null, ITSService.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even do anything with it
            }
            //load user data
            if(ITSService.userName != null && !ITSService.userName.equals(""))
            	userNameEditText.setText(ITSService.userName);
            if(ITSService.password != null && !ITSService.password.equals(""))
            	passwordEditText.setText(ITSService.password);
            //off-line login
            if(ITSService.isLoggedIn){
	        	messageTextView.setText("signin success");
	        	Intent intent = new Intent(ITSSignInActivity.this, ITSTabBarActivity.class);
	        	startActivity(intent);
	        	passwordEditText.setText("");
	        	messageTextView.setText("");
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
		
		this.getActionBar().setTitle("Sign In");
		
		setContentView(R.layout.itssignin);
		userNameEditText = (EditText)findViewById(R.id.userNameEditText);
		passwordEditText = (EditText)findViewById(R.id.passwordEditText);
//		userNameEditText.setText("waynehsu");
//		passwordEditText.setText("19820604");
//		userNameEditText.setText("jack");
//		passwordEditText.setText("1234567890");
		userNameEditText.setOnFocusChangeListener(new OnFocusChangeListener(){

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				messageTextView.setText("");
			}
			
		});
		userNameEditText.setOnEditorActionListener(new OnEditorActionListener() {
		    @Override
		    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		    	passwordEditText.setText("");
		    	messageTextView.setText("");
		        if (actionId == EditorInfo.IME_ACTION_DONE) {
		        	verifyUser();
		        }
		        return false;
		    }
		});
		userNameEditText.addTextChangedListener(new TextWatcher(){
			@Override
	        public void afterTextChanged(Editable s) {
				passwordEditText.setText("");
	        }
			@Override
	        public void beforeTextChanged(CharSequence s, int start, int count, int after){}
			@Override
	        public void onTextChanged(CharSequence s, int start, int before, int count){}
	    }); 
		passwordEditText.setOnFocusChangeListener(new OnFocusChangeListener(){

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				messageTextView.setText("");
			}
			
		});
		passwordEditText.setOnEditorActionListener(new OnEditorActionListener() {
		    @Override
		    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		    	messageTextView.setText("");
		        if (actionId == EditorInfo.IME_ACTION_DONE) {
		        	verifyUser();
		        }
		        return false;
		    }
		});
		messageTextView = (TextView)findViewById(R.id.messageTextView);
		signInButton = (Button)findViewById(R.id.signInButton);
		signInButton.setOnClickListener(new Button.OnClickListener(){ 
            @Override
            public void onClick(View v) {
            	//iTSConnectionManager.test();
            	verifyUser();
//            	Intent intent = new Intent(ITSSignInActivity.this, ITSTabBarActivity.class);
//                startActivity(intent);
            }
        });     
		signUpTextView = (TextView)findViewById(R.id.signUpTextView);
		signUpTextView.setMovementMethod(LinkMovementMethod.getInstance());
		signUpTextView.setClickable(true);
		signUpTextView.setText(Html.fromHtml("<a href='http://wayne.cs.ucdavis.edu:5000/web_signupPage'> Not a member? Sign up here </a>"));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
//		if (id == R.id.action_settings) {
//			return true;
//		}
		return false; //super.onOptionsItemSelected(item);
	}
	
	@Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            doUnbindService();
        } catch (Throwable t) {
            Log.e("MainActivity", "Failed to unbind from the service", t);
        }
    }
	
	private void verifyUser() {
		if(userNameEditText.getText().toString().equals("") || passwordEditText.getText().toString().equals("")){
			messageTextView.setText("empty username or password");
			return;
		}
        if (mIsBound) {
            if (mService != null) {
                try {
                	userInfo.clear();
                	userInfo.add(userNameEditText.getText().toString());
                	userInfo.add(passwordEditText.getText().toString());
                    Message msg = Message.obtain(null, ITSService.MSG_SIGNIN, userInfo);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {
                }
            }
        }
    }
}
