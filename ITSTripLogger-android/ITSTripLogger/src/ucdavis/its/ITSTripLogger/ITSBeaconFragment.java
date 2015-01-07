package ucdavis.its.ITSTripLogger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.estimote.sdk.Beacon;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class ITSBeaconFragment extends ListFragment {
	public enum CalibrationType {
		TypeNone,
	    TypeInCar,
	    TypeOffCar
	}
	
	public static final String TAG = "ITSBeaconFragment";
	ITSTabBarActivity iTSTabBarActivity;
	ITSConnectionManager iTSConnectionManager;
	ITSBeaconManager iTSBeaconManager;
	
	List<Beacon> beacons;
	JSONArray iTSCars;
	
	View mainView;
	TextView thresholdTextView;
	TextView toleranceTextView;
	SeekBar thresholdSeekBar;
	SeekBar toleranceSeekBar;
	
	Button editButton;
	
	View changeNameView;
	TextView changeNameTextView;
	EditText changeNameEditText;
	Button changeNameButton;
	
	Button changeAutoButton;
	View changeAutoView;
	Button inCarButton;
	Button offCarButton;
	TextView messageTextView;
	Button resetButton;
	
	Button changeManualButton;
	View changeManualView;
	TextView beaconThresholdTextView;
	TextView beaconToleranceTextView;
	SeekBar beaconThresholdSeekBar;
	SeekBar beaconToleranceSeekBar;
	
	//calibration
	Boolean editingMode;
	Boolean calibrationMode;
	CalibrationType calibrationSubMode;
	
	String selectedBeaconName;
    String selectedBeaconUUID;
    int selectedBeaconMajor;
    int selectedBeaconMinor;
    int selectedBeaconThreshold;
    int selectedBeaconTolerance;
    
    int newRssiThresholdValue;
    int newRssiToleranceValue;
    int tempHighValue;
    int tempLowValue;
	
	
	private BeaconListAdapter adapter;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		iTSTabBarActivity = (ITSTabBarActivity)activity;
		iTSTabBarActivity.iTSBeaconFragment = this;
		iTSConnectionManager = ITSConnectionManager.getInstance();
		iTSConnectionManager.iTSBeaconDelegate = this;
		iTSBeaconManager = ITSBeaconManager.getInstance();
		iTSBeaconManager.iTSBeaconDelegate = this;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		beacons = new ArrayList<Beacon>();

		mainView = inflater.inflate(R.layout.itsbeacon, container, false);
		thresholdTextView = (TextView)mainView.findViewById(R.id.thresholdTextView);
		toleranceTextView = (TextView)mainView.findViewById(R.id.toleranceTextView);
		thresholdSeekBar = (SeekBar)mainView.findViewById(R.id.thresholdSeekBar);
		toleranceSeekBar = (SeekBar)mainView.findViewById(R.id.toleranceSeekBar);
		
		editButton = (Button)mainView.findViewById(R.id.editButton);    
		
		changeNameView = (View)mainView.findViewById(R.id.changeNameView);
		changeNameTextView = (TextView)mainView.findViewById(R.id.changeNameTextView);
		changeNameEditText = (EditText)mainView.findViewById(R.id.changeNameEditText);
		changeNameButton = (Button)mainView.findViewById(R.id.changeNameButton);
		
		changeAutoButton = (Button)mainView.findViewById(R.id.changeAutoButton);
		changeAutoView = (View)mainView.findViewById(R.id.changeAutoView);
		inCarButton = (Button)mainView.findViewById(R.id.inCarButton);
		offCarButton = (Button)mainView.findViewById(R.id.offCarButton);
		messageTextView = (TextView)mainView.findViewById(R.id.messageTextView);
		resetButton = (Button)mainView.findViewById(R.id.resetbutton);
		
		changeManualButton = (Button)mainView.findViewById(R.id.changeManualButton);
		changeManualView = (View)mainView.findViewById(R.id.changeManualView);
		beaconThresholdTextView = (TextView)mainView.findViewById(R.id.beaconThresholdTextView);
		beaconToleranceTextView = (TextView)mainView.findViewById(R.id.beaconToleranceTextView);
		beaconThresholdSeekBar = (SeekBar)mainView.findViewById(R.id.beaconThresholdSeekBar);
		beaconToleranceSeekBar = (SeekBar)mainView.findViewById(R.id.beaconToleranceSeekBar);
		
		editingMode = false;
		calibrationMode = false;
		calibrationSubMode = CalibrationType.TypeNone;
		
		editButton.setOnClickListener(new Button.OnClickListener(){ 
            @Override
            public void onClick(View v) {
            	if(editingMode){
            		editButton.setText("Edit");
            		editButton.setBackgroundResource(R.drawable.edit_button);
            		changeNameView.setVisibility(View.INVISIBLE);
            		InputMethodManager inputManager = (InputMethodManager) iTSTabBarActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            		inputManager.hideSoftInputFromWindow(mainView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            		editingMode = false;
            		changeNameTextView.setText("Not Selected");
            		changeNameEditText.setText("");
            		changeAutoButton.setBackgroundColor(0xFFFFFFFF);
                	changeManualButton.setBackgroundColor(0xFF00FFFF);
                	changeAutoView.setVisibility(View.VISIBLE);
                	changeManualView.setVisibility(View.INVISIBLE);
            		resetAll();
            	}else{
            		editButton.setText("Done");
            		editButton.setBackgroundResource(R.drawable.done_button);
            		changeNameView.setVisibility(View.VISIBLE);
            		InputMethodManager inputManager = (InputMethodManager) iTSTabBarActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            		inputManager.hideSoftInputFromWindow(mainView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            		editingMode = true;
            		changeNameTextView.setText("Not Selected");
            		changeNameEditText.setText("");
            		changeAutoButton.setBackgroundColor(0xFFFFFFFF);
                	changeManualButton.setBackgroundColor(0xFF00FFFF);
                	changeAutoView.setVisibility(View.VISIBLE);
                	changeManualView.setVisibility(View.INVISIBLE);
            		resetAll();
            	}
            }
        });
		
		changeAutoButton.setOnClickListener(new Button.OnClickListener(){ 
            @Override
            public void onClick(View v) {
            	changeAutoButton.setBackgroundColor(0xFFFFFFFF);
            	changeManualButton.setBackgroundColor(0xFF00FFFF);
            	changeAutoView.setVisibility(View.VISIBLE);
            	changeManualView.setVisibility(View.INVISIBLE);
            }
        });
		
		changeManualButton.setOnClickListener(new Button.OnClickListener(){ 
            @Override
            public void onClick(View v) {
            	changeAutoButton.setBackgroundColor(0xFF00FFFF);
            	changeManualButton.setBackgroundColor(0xFFFFFFFF);
            	changeAutoView.setVisibility(View.INVISIBLE);
            	changeManualView.setVisibility(View.VISIBLE);
            }
        });
		
		//SeekBars
		if(iTSBeaconManager != null){
			thresholdSeekBar.setProgress(iTSBeaconManager.getRssiThresholdValue() + 90);
			thresholdTextView.setText("Threshold: " + iTSBeaconManager.getRssiThresholdValue());
		}
		thresholdSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				thresholdTextView.setText("Threshold: " + (thresholdSeekBar.getProgress() - 90));
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				thresholdTextView.setText("Threshold: " + (thresholdSeekBar.getProgress() - 90));
				if(iTSBeaconManager != null){
					iTSBeaconManager.setRssiThresholdValue(thresholdSeekBar.getProgress() - 90);
				}
			}
			
		});
		
		
		if(iTSBeaconManager != null){
			toleranceSeekBar.setProgress(iTSBeaconManager.getRssiToleranceValue());
			toleranceTextView.setText("Tolerance: " + iTSBeaconManager.getRssiToleranceValue());
		}
		toleranceSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				toleranceTextView.setText("Tolerance: " + toleranceSeekBar.getProgress());
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				toleranceTextView.setText("Tolerance: " + toleranceSeekBar.getProgress());
				if(iTSBeaconManager != null){
					iTSBeaconManager.setRssiToleranceValue(toleranceSeekBar.getProgress());
				}
			}
			
		});
		
		//beacon SeekBars
		beaconThresholdSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				beaconThresholdTextView.setText("Threshold: " + (beaconThresholdSeekBar.getProgress() - 90));
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				beaconThresholdTextView.setText("Threshold: " + (beaconThresholdSeekBar.getProgress() - 90));
				newRssiThresholdValue = (int)(beaconThresholdSeekBar.getProgress() - 90);
			}
			
		});
		
		beaconToleranceSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				beaconToleranceTextView.setText("Tolerance: " + beaconToleranceSeekBar.getProgress());
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				beaconToleranceTextView.setText("Tolerance: " + beaconToleranceSeekBar.getProgress());
				newRssiToleranceValue = (int)beaconToleranceSeekBar.getProgress();
			}
			
		});
		
		changeNameButton.setOnClickListener(new Button.OnClickListener(){
			@Override
            public void onClick(View v) {
				InputMethodManager inputManager = (InputMethodManager) iTSTabBarActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        		inputManager.hideSoftInputFromWindow(mainView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        		if(selectedBeaconUUID != null && !selectedBeaconUUID.equals("") && selectedBeaconMajor != -1 && selectedBeaconMinor != -1){
        	        if(!changeNameEditText.getText().toString().equals("")){
        	        	if(iTSConnectionManager != null){
        	        		iTSConnectionManager.insertCarName(changeNameEditText.getText().toString(), selectedBeaconUUID, 
        	        				selectedBeaconMajor, selectedBeaconMinor, 
        	        				newRssiThresholdValue, newRssiToleranceValue);
        	        	}
        	        }
    	        }
			}
		});
		
		inCarButton.setOnClickListener(new Button.OnClickListener(){
			@Override
            public void onClick(View v) {
				if(iTSBeaconManager!=null && selectedBeaconMajor != -1 && selectedBeaconMinor != -1){
			        calibrationMode = true;
			        calibrationSubMode = CalibrationType.TypeInCar;
			        inCarButton.setText("Sensing...0");
			        iTSBeaconManager.testBeaconRSSIMajor(selectedBeaconMajor, selectedBeaconMinor);
				}
			}
		});
		
		offCarButton.setOnClickListener(new Button.OnClickListener(){
			@Override
            public void onClick(View v) {
				if(iTSBeaconManager!=null && selectedBeaconMajor != -1 && selectedBeaconMinor != -1){
			        calibrationMode = true;
			        calibrationSubMode = CalibrationType.TypeOffCar;
			        offCarButton.setText("Sensing...0");
			        iTSBeaconManager.testBeaconRSSIMajor(selectedBeaconMajor, selectedBeaconMinor);
				}
			}
		});
		
		resetButton.setOnClickListener(new Button.OnClickListener(){
			@Override
            public void onClick(View v) {
				ITSBeaconFragment.this.reset();
			}
		});
		
		//ArrayAdapter<String> adapter = new ArrayAdapter<String>(inflater.getContext(), android.R.layout.simple_list_item_1,countries);  
		
		adapter = new BeaconListAdapter(inflater.getContext());
		adapter.replaceWith(Collections.<Beacon>emptyList(), Collections.<String>emptyList());
		setListAdapter(adapter);
		return mainView;
	}
	
	public void replaceAdatperData(ITSCarList iTSCarList){
		this.iTSCars = iTSCarList.iTSCars;
		List<Beacon> oldBeacons = iTSCarList.beacons;
		List<String> oldNames = new ArrayList<String>();
		for(Beacon b : oldBeacons){
			boolean foundName = false;
			if(iTSCars != null && iTSCars.length() != 0){
        		for(int i=0; i<iTSCars.length(); i++){
        			try{
        				JSONObject car = iTSCars.getJSONObject(i);
        				int tempMajor = Integer.parseInt(car.getString("bt_major"));
        				int tempMinor = Integer.parseInt(car.getString("bt_minor"));
            			if(b.getMajor() == tempMajor && b.getMinor() == tempMinor){
            				if(car.getString("bt_name") != null){
            					oldNames.add(car.getString("bt_name"));
            					foundName = true;
            				}
            				break;
            			}
        			}catch(Exception e){
        			}
        		}
        	}
			
			if(!foundName){
				oldNames.add(null);
        	}
		}
		if(adapter != null){
			//sorting
			List<Beacon> newBeacons = new ArrayList<Beacon>();
			List<String> newNames   = new ArrayList<String>();
			for(int i=0; i<oldBeacons.size(); i++){
				Beacon beacon = oldBeacons.get(i);
				if(newBeacons.size() != 0){
					for(int j=0; j<=newBeacons.size(); j++){
						if(j==newBeacons.size()){
							newBeacons.add(j, beacon);
							newNames.add(j, oldNames.get(i));
							break;
						}
						
						if(beacon.getMajor() <= newBeacons.get(0).getMajor()){
							newBeacons.add(j, beacon);
							newNames.add(j, oldNames.get(i));
							break;
						}
					}
				}else{
					newBeacons.add(beacon);
					newNames.add(oldNames.get(i));
				}
			}
			beacons = newBeacons;
			adapter.replaceWith(newBeacons, newNames);
		}
//		for(Beacon b : beacons){
//			Log.i(TAG, "b = " + b.getMajor());
//		}
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	 
	}
  
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
//		Log.i("FragmentList", "Item clicked: " + id);
		Beacon selectedBeacon;
		if(!calibrationMode){
			if(beacons != null && !beacons.isEmpty() && beacons.get(position) != null){
				selectedBeacon = beacons.get(position);
				changeNameTextView.setText("Major:"+selectedBeacon.getMajor()+", Minor:"+selectedBeacon.getMinor());
			}else{
				return;
			}
			
			this.resetSelected();
			
			//Search Beacon
			if(iTSCars != null && iTSCars.length() != 0){
        		for(int i=0; i<iTSCars.length(); i++){
        			try{
        				JSONObject car = iTSCars.getJSONObject(i);
        				int tempMajor = Integer.parseInt(car.getString("bt_major"));
        				int tempMinor = Integer.parseInt(car.getString("bt_minor"));
            			if(selectedBeacon.getMajor() == tempMajor && selectedBeacon.getMinor() == tempMinor){
            				if(car.getString("bt_name") != null){
            					selectedBeaconName = car.getString("bt_name");
            					selectedBeaconThreshold = car.getInt("bt_threshold");
            					selectedBeaconTolerance = car.getInt("bt_tolerance");
            				}
            				break;
            			}
        			}catch(Exception e){
        			}
        		}
        	}
			changeNameEditText.setText(selectedBeaconName);
			
			selectedBeaconUUID = selectedBeacon.getProximityUUID().toString();
	        selectedBeaconMajor = selectedBeacon.getMajor();
	        selectedBeaconMinor = selectedBeacon.getMinor();
	        
	        this.resetCalibration();
	        this.resetCalibrationUI();
	        this.resetManualUI();
		}
	}

	public void clearUserData(){
		//clear user data
		adapter.replaceWith(Collections.<Beacon>emptyList(), Collections.<String>emptyList());
	}
	
	public void returnTestBeaconStep(int testBeaconCount){
		if(calibrationSubMode == CalibrationType.TypeInCar){
			inCarButton.setText("Sensing..." + testBeaconCount);
			messageTextView.setText("Sensing On Car Signal Strength...");
		}else if(calibrationSubMode == CalibrationType.TypeOffCar){
			offCarButton.setText("Sensing..." + testBeaconCount);
			messageTextView.setText("Sensing Off Car Signal Strength...");
		}
	}
	
	public void returnTestBeaconValue(int testBeaconRssiValue) {
		if(calibrationSubMode == CalibrationType.TypeInCar){
			inCarButton.setText("Done");
			calibrationSubMode = CalibrationType.TypeNone;
			inCarButton.setBackgroundResource(R.drawable.red_button);
			offCarButton.setBackgroundResource(R.drawable.plum_button);
			inCarButton.setEnabled(false);
			offCarButton.setEnabled(true);
			tempHighValue = testBeaconRssiValue;
			messageTextView.setText("Get off your car, and press \"Off Car\" Button");
		}else if(calibrationSubMode == CalibrationType.TypeOffCar){
			offCarButton.setText("Done");
			calibrationMode = false;
	        calibrationSubMode = CalibrationType.TypeNone;
	        inCarButton.setBackgroundResource(R.drawable.red_button);
			offCarButton.setBackgroundResource(R.drawable.red_button);
			inCarButton.setEnabled(false);
			offCarButton.setEnabled(false);
			tempLowValue = testBeaconRssiValue;
			
			//find threshold and tolerance
	        newRssiThresholdValue = (tempHighValue + tempLowValue) / 2;
	        newRssiToleranceValue = Math.abs(tempHighValue - newRssiThresholdValue);
	        
	        //update manual
	        this.resetManualUI();
	        
	        messageTextView.setText("Done Calibration with Threshold:" + newRssiThresholdValue + " Tolerance:" + newRssiToleranceValue);
		}
	}
	
	//calibration
	private void resetAll(){
		this.resetMode();
		this.resetSelected();
		this.resetCalibration();
		this.resetCalibrationUI();
		this.resetManualUI();
		iTSBeaconManager.resetTestBeacon();
	}
	
	private void reset(){
		this.resetMode();
		this.resetCalibration();
		this.resetCalibrationUI();
		this.resetManualUI();
		iTSBeaconManager.resetTestBeacon();
	}
	
	private void resetMode(){
		calibrationMode = false;
	    calibrationSubMode = CalibrationType.TypeNone;
	}
	
	private void resetSelected(){
		selectedBeaconName = "";
	    selectedBeaconUUID = "";
	    selectedBeaconMajor = -1;
	    selectedBeaconMinor = -1;
	    if(iTSBeaconManager != null){
	    	selectedBeaconThreshold = iTSBeaconManager.getRssiThresholdValue();
	    	selectedBeaconTolerance = iTSBeaconManager.getRssiToleranceValue();
	    }
	}
	
	private void resetCalibration(){
		newRssiThresholdValue = selectedBeaconThreshold;
	    newRssiToleranceValue = selectedBeaconTolerance;
	    tempHighValue = newRssiThresholdValue + newRssiToleranceValue;
	    tempLowValue = newRssiThresholdValue - newRssiToleranceValue;
	}
	
	private void resetCalibrationUI(){
		inCarButton.setBackgroundResource(R.drawable.plum_button);
		offCarButton.setBackgroundResource(R.drawable.cyan_button);
		inCarButton.setEnabled(true);
		offCarButton.setEnabled(false);
		inCarButton.setText("On Car");
		offCarButton.setText("Off Car");
		messageTextView.setText("Get into your car, and press \"On Car\" Button");
	}
	
	private void resetManualUI(){
		beaconThresholdTextView.setText("Threshold: " + newRssiThresholdValue);
	    beaconToleranceTextView.setText("Tolerance: " + newRssiToleranceValue);
	    beaconThresholdSeekBar.setProgress(newRssiThresholdValue + 90);
	    beaconToleranceSeekBar.setProgress(newRssiToleranceValue);
	}
}