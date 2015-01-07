package ucdavis.its.ITSTripLogger;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class ITSInfoFragment extends Fragment {
	ITSTabBarActivity iTSTabBarActivity;
	ITSConnectionManager iTSConnectionManager;
	ITSBeaconManager iTSBeaconManager;
	View mainView;
	TextView userIdTextView;
	TextView userNameTextView;
	TextView emailTextView;
	TextView lnameTextView;
	TextView fnameTextView;
	TextView householdIdTextView;
	TextView householdNameTextView;
	TextView registerTextView;
	Switch locationServiceSwitch;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	
		iTSTabBarActivity = (ITSTabBarActivity)activity;
		iTSTabBarActivity.iTSInfoFragment = this;
		iTSConnectionManager = ITSConnectionManager.getInstance();
		iTSBeaconManager = ITSBeaconManager.getInstance();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mainView = inflater.inflate(R.layout.itsinfo, container, false);
		userIdTextView = (TextView)mainView.findViewById(R.id.userIdTextView);
		userNameTextView = (TextView)mainView.findViewById(R.id.userNameTextView);
		emailTextView = (TextView)mainView.findViewById(R.id.emailTextView);
		lnameTextView = (TextView)mainView.findViewById(R.id.lnameTextView);
		fnameTextView = (TextView)mainView.findViewById(R.id.fnameTextView);
		householdIdTextView = (TextView)mainView.findViewById(R.id.householdIdTextView);
		householdNameTextView = (TextView)mainView.findViewById(R.id.householdNameTextView);
		registerTextView = (TextView)mainView.findViewById(R.id.registerTimeTextView);
		locationServiceSwitch = (Switch)mainView.findViewById(R.id.locationServiceSwitch);
		if(iTSBeaconManager != null){
			locationServiceSwitch.setChecked(iTSBeaconManager.getLocationServiceEnabled());
    	}
		locationServiceSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
		    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				iTSBeaconManager = ITSBeaconManager.getInstance();
		        if(isChecked){
		        	if(iTSBeaconManager != null){
		        		iTSBeaconManager.setLocationServiceEnabled(true);
		        	}
		        }else{
		        	if(iTSBeaconManager != null){
		        		iTSBeaconManager.setLocationServiceEnabled(false);
		        	}
		        }
		    }
		});
		getUserInfo();
		return mainView;
	}
	
	private void getUserInfo(){
		if(iTSConnectionManager != null){
			userIdTextView.setText(iTSConnectionManager.user_id);
			userNameTextView.setText(iTSConnectionManager.user_name);
			emailTextView.setText(iTSConnectionManager.user_email);
			lnameTextView.setText(iTSConnectionManager.user_lname);
			fnameTextView.setText(iTSConnectionManager.user_fname);
			householdIdTextView.setText(iTSConnectionManager.user_household_id);
			householdNameTextView.setText(iTSConnectionManager.user_household_name);
			registerTextView.setText(iTSConnectionManager.user_timestamp);
		}
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	
	}

	public void clearUserData(){
		//clear user data
		userIdTextView.setText("");
		userNameTextView.setText("");
		emailTextView.setText("");
		lnameTextView.setText("");
		fnameTextView.setText("");
		householdIdTextView.setText("");
		householdNameTextView.setText("");
		registerTextView.setText("");
	}
}