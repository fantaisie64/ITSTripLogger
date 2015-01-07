package ucdavis.its.ITSTripLogger;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.utils.L;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class ITSBeaconManager {
	public enum UploadType {
		TypeStart,
	    TypeEnd
	}
	private static final String TAG = "ITSBeaconManager";
	private static final String ESTIMOTE_PROXIMITY_UUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";
	private static final Region ALL_ESTIMOTE_BEACONS = new Region("regionId", ESTIMOTE_PROXIMITY_UUID, null, null);
	private static ITSBeaconManager instance = null;
	public ITSService iTSService = null;
	private BeaconManager beaconManager;
	public ITSBeaconFragment iTSBeaconDelegate;
	
	private JSONArray iTSCars = null;

	//location
	LocationManager locationManager = null;
    Location location = null;
	double currentLatitude = 0.0;
    double currentLongitude = 0.0;
    Date now;
	
	private boolean gotBeaconBoolean = false;
	private Beacon gotBeacon = null;
	private String gotBeaconUUID = null;
	private int gotBeaconMajor = -1;
	private int gotBeaconMinor = -1;
	private String gotBeaconName = "";
	private int gotBeaconThresholdValue = -1;
	private int gotBeaconToleranceValue = -1;
	private int rssiThresholdValue = -76; //default
	private int rssiToleranceValue = 8; //default
	private boolean locationServiceEnabled = true; //default
	private int sensingCount = 5; //default
	private Map<Integer, Integer> gotBeaconCount = null;
	private int loseBeaconCount = 0;
    
	private boolean testBeaconMode = false;
	private int testBeaconMajor = -1;
	private int testBeaconMinor = -1;
	private int testBeaconCountThreshold = 5;
    private float testBeaconCount = 0;
    private float testBeaconRssiValue = 0;
    
    public JSONObject typeJSONObject = null;
    public JSONObject parameterJSONObject = null;
    
	
	private ITSBeaconManager(){
		super();
	}
	
	private ITSBeaconManager(ITSService iTSService){
		super();
		this.iTSService = iTSService;
		this.createBeaconManager(iTSService);
	}
	
	public static ITSBeaconManager getInstance(ITSService iTSService){
		if(instance == null)
			instance = new ITSBeaconManager(iTSService);
		else if(instance.iTSService != iTSService){
			instance.iTSService = iTSService;
			instance.createBeaconManager(iTSService);
		}
		return instance;
	}
	
	//only for ITSInfoFragment
	public static ITSBeaconManager getInstance(){
		return instance;
	}
	
	public void setRssiThresholdValue(int newRssiThresholdValue){
		rssiThresholdValue = newRssiThresholdValue;
		saveParameterPlist();
	}
	
	public void setRssiToleranceValue(int newRssiToleranceValue){
		rssiToleranceValue = newRssiToleranceValue;
		saveParameterPlist();
	}
	
	public int getRssiThresholdValue(){
		return rssiThresholdValue;
	}
	
	public int getRssiToleranceValue(){
		return rssiToleranceValue;
	}
	
	public void setLocationServiceEnabled(boolean isEnabled){
		locationServiceEnabled = isEnabled;
		saveParameterPlist();
	}
	
	public boolean getLocationServiceEnabled(){
		return locationServiceEnabled;
	}
	
	private void initRegion(){
		//initial
		Log.i(TAG, "initial");
		rssiThresholdValue = -76; //default
	    rssiToleranceValue = 8; //default
	    locationServiceEnabled = true; //default
	    sensingCount = 5; //default
	    gotBeaconCount = new HashMap<Integer, Integer>();
	    loseBeaconCount = 0;
	    this.loadParameterPlist();
	    this.saveParameterPlist();
	    
	    locationManager = (LocationManager)iTSService.getSystemService(Context.LOCATION_SERVICE);
	    
	    //check last type
	    gotBeaconBoolean = false;
	    gotBeacon = null;
	    gotBeaconUUID = null;
	    gotBeaconMajor = -1;
	    gotBeaconMinor = -1;
	    gotBeaconName = "";
	    gotBeaconThresholdValue = rssiThresholdValue;
	    gotBeaconToleranceValue = rssiToleranceValue;
	    this.loadTypePlist();
	    this.saveTypePlist();
	    
	    iTSCars = new JSONArray();
	    this.loadCarPlist();
	    
	    //for testing beacon
	    testBeaconMode = false;
	    testBeaconCountThreshold = 5;
	    testBeaconCount = 0;
	    testBeaconRssiValue = 0;
	}
	
	private void createBeaconManager(ITSService iTSService){
		beaconManager = new BeaconManager(iTSService);
		
		initRegion();
		
		// Should be invoked in #onCreate.
		beaconManager.setRangingListener(new BeaconManager.RangingListener() {
			@Override public void onBeaconsDiscovered(Region region, List<Beacon> beacons) {
				Log.d(TAG, "Ranged beacons: " + beacons);
				if(ITSBeaconManager.this.iTSService != null){
					ITSCarList iTSCarList = new ITSCarList();
					iTSCarList.beacons = beacons;
					iTSCarList.iTSCars = iTSCars;
					ITSBeaconManager.this.iTSService.callBackFromUpdatingBeacon(iTSCarList);
				}
				
				//handling beacons
				boolean gotSpecificBeacon = false;
				int tempCount;
				String tempBeaconName = "";
			    int tempBeaconThresholdValue = rssiThresholdValue;
			    int tempBeaconToleranceValue = rssiToleranceValue;
			    
			    for(Beacon b : beacons){
			    	if(b.getRssi()!= 0){
			    		//test beacon **********************************************************************************
			            if(testBeaconMode){
			                if(testBeaconMajor != -1 && testBeaconMinor != -1 &&
			                   testBeaconMajor == b.getMajor() && testBeaconMinor == b.getMinor()){
			                    ITSBeaconManager.this.returnTestBeaconStep(b.getRssi());
			                }
			            }
			            //end test beacon ******************************************************************************
			            
			            //find beacon **********************************************************************************
			            tempBeaconName = "";
			            tempBeaconThresholdValue = rssiThresholdValue;
			            tempBeaconToleranceValue = rssiToleranceValue;
			            if(!gotBeaconBoolean){
			            	//search beacon
			            	if(iTSCars != null){
			            		for(int i=0; i<iTSCars.length(); i++){
			            			try{
			            				JSONObject car = iTSCars.getJSONObject(i);
			            				int tempMajor = Integer.parseInt(car.getString("bt_major"));
			            				int tempMinor = Integer.parseInt(car.getString("bt_minor"));
				            			if(b.getMajor() == tempMajor && b.getMinor() == tempMinor){
				            				if(car.getString("bt_name") != null)
				            					tempBeaconName = car.getString("bt_name");
				            				if(car.has("bt_threshold"))
				            					tempBeaconThresholdValue = car.getInt("bt_threshold");
				            				if(car.has("bt_tolerance"))
				            					tempBeaconToleranceValue = car.getInt("bt_tolerance");
				            				break;
				            			}
			            			}catch(Exception e){
			            			}
			            		}
			            	}
			            	
			            	if(b.getRssi() > (tempBeaconThresholdValue+tempBeaconToleranceValue)){
			            		gotSpecificBeacon = true; // always true for getting beacon
			            		if(gotBeaconCount.get(Integer.valueOf(b.getMajor())) == null){
			            			gotBeaconCount.put(Integer.valueOf(b.getMajor()), Integer.valueOf(1));
			            			tempCount = 1;
			            		}else{
			            			tempCount = gotBeaconCount.get(Integer.valueOf(b.getMajor())).intValue();
			                        tempCount += 1;
			            		}
			            		
			            		//check if larger than sensingCount *************************
			                    if(tempCount >= sensingCount){
			                    	gotBeaconBoolean = true;
			                        gotBeacon = b;
			                        gotBeaconUUID = b.getProximityUUID().toString();
			                        gotBeaconMajor = b.getMajor();
			                        gotBeaconMinor = b.getMinor();
			                        gotBeaconName = tempBeaconName;
			                        gotBeaconThresholdValue = tempBeaconThresholdValue;
			                        gotBeaconToleranceValue = tempBeaconToleranceValue;
			                        ITSBeaconManager.this.saveTypePlist();
			                        if(gotBeaconName != null && !gotBeaconName.equals("")){
			                        	Log.i(TAG, "In Car " + gotBeaconName);
			                        	ITSBeaconManager.this.updateActionBarTitle("In Car " + gotBeaconName + " Now");
			                        }else{
			                        	Log.i(TAG, "In Car " + gotBeaconMajor + "," + gotBeaconMinor);
			                        	ITSBeaconManager.this.updateActionBarTitle("In Car " + gotBeaconMajor + "," + gotBeaconMinor + " Now");
			                        }
			                        ITSBeaconManager.this.uploadRecordData(ITSBeaconManager.UploadType.TypeStart, gotBeacon.getRssi(), gotBeaconUUID, gotBeaconMajor, gotBeaconMinor, gotBeaconName);
			                        gotBeaconCount.clear();
			                        break;
			                    }
			                    gotBeaconCount.put(Integer.valueOf(b.getMajor()), Integer.valueOf(tempCount));
			                    
			                  //end check if larger than sensingCount *************************
			            		
			            	}else{
			            		ITSBeaconManager.this.updateActionBarTitle("");
			            		gotBeaconCount.put(Integer.valueOf(b.getMajor()), Integer.valueOf(0));
			                }
			            //end find beacon **********************************************************************************
			                
			                
			            //lose beacon **************************************************************************************
			            }else if(gotBeaconBoolean){
			            	if(b.getRssi() < (gotBeaconThresholdValue-gotBeaconToleranceValue)){
			            		if(gotBeaconUUID != null && gotBeaconMajor != -1 && gotBeaconMinor != -1){
									if(b.getProximityUUID().toString().equals(gotBeaconUUID) &&
									b.getMajor() == gotBeaconMajor && b.getMinor() == gotBeaconMinor){
										gotBeacon = b;
			                            gotBeaconUUID = b.getProximityUUID().toString();
			                            gotBeaconMajor = b.getMajor();
			                            gotBeaconMinor = b.getMinor();
			                            gotSpecificBeacon = true;
			                            loseBeaconCount += 1;
			                            
			                          //check if larger than sensingCount *************************
			                            if(loseBeaconCount >= sensingCount){
			                            	if(gotBeaconName != null && !gotBeaconName.equals("")){
					                        	Log.i(TAG, "Left1 Car " + gotBeaconName);
				                            	ITSBeaconManager.this.updateActionBarTitle("");
					                        }else{
					                        	Log.i(TAG, "Left1 Car " + gotBeaconMajor + "," + gotBeaconMinor);
					                        	ITSBeaconManager.this.updateActionBarTitle("");
					                        }
			                            	ITSBeaconManager.this.uploadRecordData(ITSBeaconManager.UploadType.TypeEnd, gotBeacon.getRssi(), gotBeaconUUID, gotBeaconMajor, gotBeaconMinor, gotBeaconName);
			                            	gotBeaconBoolean = false;
			                            	gotBeacon = null;
			                                gotBeaconUUID = null;
			                                gotBeaconMajor = -1;
			                                gotBeaconMinor = -1;
			                                gotBeaconName = "";
			                                gotBeaconThresholdValue = rssiThresholdValue;
			                                gotBeaconToleranceValue = rssiToleranceValue;
			                                ITSBeaconManager.this.saveTypePlist();
			                                loseBeaconCount = 0;
			                            	break;
			                            }
			                            //end check if larger than sensingCount *************************
									}
								}
			            	}else{
			            		if(gotBeaconUUID != null && gotBeaconMajor != -1 && gotBeaconMinor != -1){
			            			if(b.getProximityUUID().toString().equals(gotBeaconUUID) &&
									b.getMajor() == gotBeaconMajor && b.getMinor() == gotBeaconMinor){
			            				if(gotBeaconName != null && !gotBeaconName.equals("")){
				                        	ITSBeaconManager.this.updateActionBarTitle("In Car " + gotBeaconName + " Now");
				                        }else{
				                        	ITSBeaconManager.this.updateActionBarTitle("In Car " + gotBeaconMajor + "," + gotBeaconMinor + " Now");
				                        }
			            				loseBeaconCount = 0;
			                            gotSpecificBeacon = true;
			                            break;
			            			}
			            		}
			            	}
			            }
			            //end lose beacon **********************************************************************************
			    	}
			    }
			    
			  //if not found existing beacon **********************************************************************************
			  if(gotBeaconBoolean && !gotSpecificBeacon){
				  loseBeaconCount += 1;
				  if(loseBeaconCount >= sensingCount){
					  if(gotBeaconName != null && !gotBeaconName.equals("")){
						  Log.i(TAG, "Left Car2 " + gotBeaconName);
                      	ITSBeaconManager.this.updateActionBarTitle("");
                      }else{
                      	Log.i(TAG, "Left Car2 " + gotBeaconMajor + "," + gotBeaconMinor);
                      	ITSBeaconManager.this.updateActionBarTitle("");
                      }
					  ITSBeaconManager.this.uploadRecordData(ITSBeaconManager.UploadType.TypeEnd, gotBeacon.getRssi(), gotBeaconUUID, gotBeaconMajor, gotBeaconMinor, gotBeaconName);
					  gotBeaconBoolean = false;
					  gotBeacon = null;
                      gotBeaconUUID = null;
                      gotBeaconMajor = -1;
                      gotBeaconMinor = -1;
                      gotBeaconName = "";
                      gotBeaconThresholdValue = rssiThresholdValue;
                      gotBeaconToleranceValue = rssiToleranceValue;
                      ITSBeaconManager.this.saveTypePlist();
                      loseBeaconCount = 0;
				  }
			  }
			  //end if not found existing beacon **********************************************************************************
			}
		});
	}
	
	public void connectBeaconManager(){
		beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
			@Override public void onServiceReady() {
				try {
					beaconManager.startRanging(ALL_ESTIMOTE_BEACONS);
				}catch (RemoteException e) {
					Log.e(TAG, "Cannot start ranging", e);
				}
				ITSBeaconManager.this.showCarNames();
		    }
		});
	}
	
	public void stopBeaconManager(){
		// Should be invoked in #onStop.
		try {
			beaconManager.stopRanging(ALL_ESTIMOTE_BEACONS);
		} catch (RemoteException e) {
			Log.e(TAG, "Cannot stop but it does not matter now", e);
		}
	}
	
	public void disconnectBeaconManager(){
		// When no longer needed. Should be invoked in #onDestroy.
		  beaconManager.disconnect();
	}
	
	public void testBeaconRSSIMajor(int beaconMajor, int beaconMinor) {
	    testBeaconMajor = beaconMajor;
	    testBeaconMinor = beaconMinor;
	    testBeaconMode = true;
	}
	
	private void returnTestBeaconStep(int newTestBeaconRssiValue){
		testBeaconCount+=1.0;
		testBeaconRssiValue += ((float)newTestBeaconRssiValue - testBeaconRssiValue) / testBeaconCount;
		if(iTSBeaconDelegate != null){
	        iTSBeaconDelegate.returnTestBeaconStep((int)testBeaconCount);
	    }
		if(testBeaconCount >= testBeaconCountThreshold){
	        this.returnTestBeaconValue();
	    }
	}
	
	private void returnTestBeaconValue(){
		testBeaconMode = false;
	    testBeaconMajor = -1;
	    testBeaconMinor = -1;
	    testBeaconCount = 0;
	    int testBeaconRssiNumber = (int)testBeaconRssiValue;
	    if(iTSBeaconDelegate != null){
	    	iTSBeaconDelegate.returnTestBeaconValue(testBeaconRssiNumber);
	    }
	    testBeaconRssiValue = 0;
	}
	
	private void updateActionBarTitle(String newString){
		if(iTSService != null){
			iTSService.updateActionBarTitle(newString);
		}
	}
	
	private void uploadRecordData(final ITSBeaconManager.UploadType type, final int rssi, final String uuid, final int major, final int minor, final String name){
		if(locationServiceEnabled){
			if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
				location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				LocationListener locationListener = new LocationListener() {
					@Override
				    public void onLocationChanged(Location location) {
				    	currentLatitude = location.getLatitude();
						currentLongitude = location.getLongitude();
						ITSBeaconManager.this.subUploadRecordData(type, rssi, uuid, major, minor, name);
				    }
	
					@Override
					public void onStatusChanged(String provider, int status,
							Bundle extras) {
						// TODO Auto-generated method stub
						
					}
	
					@Override
					public void onProviderEnabled(String provider) {
						// TODO Auto-generated method stub
						
					}
	
					@Override
					public void onProviderDisabled(String provider) {
						// TODO Auto-generated method stub
						
					}
				};
				locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
			}else{
				currentLatitude = 0.0;
		        currentLongitude = 0.0;
		        subUploadRecordData(type, rssi, uuid, major, minor, name);
			}
		}else{
			currentLatitude = 0.0;
	        currentLongitude = 0.0;
	        subUploadRecordData(type, rssi, uuid, major, minor, name);
		}
	}
	
	private void subUploadRecordData(ITSBeaconManager.UploadType type, int rssi, String uuid, int major, int minor, String name){
		now = new Date();
		
		ITSRecordData recordData = new ITSRecordData();
		if(type == ITSBeaconManager.UploadType.TypeStart)
	        recordData.type = "start";
	    else
	        recordData.type = "end";
		recordData.latitude = currentLatitude;
		recordData.longitude = currentLongitude;
		recordData.time_stamp = now;
	    recordData.bt_id = uuid.toUpperCase();
	    recordData.bt_major =  String.valueOf(major);
	    recordData.bt_minor = String.valueOf(minor);
	    recordData.bt_name = name;
	    if(iTSService != null){
	    	iTSService.uploadRecordData(recordData);
	    }
	    
	    //notification
	    String rssiString = String.valueOf(rssi);
	    if(name != null && !name.equals("")){
	    	if(iTSService != null){
	    		showNotification(recordData.type + " trip with " + name + ", " + rssiString,iTSService);
	    	}
	    }else{
	    	if(iTSService != null){
	    		showNotification(recordData.type + " trip with Major:" + recordData.bt_major + ", Minor:" + recordData.bt_minor + ", " + rssiString,iTSService);
	    	}
	    }
	}
	
	private void showNotification(String text, Context ctx) {
		NotificationManager nManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		NotificationCompat.Builder ncomp = new NotificationCompat.Builder(ctx);
		ncomp.setContentTitle("ITSTripLogger");
		ncomp.setContentText(text);
		ncomp.setTicker("");
		ncomp.setSmallIcon(R.drawable.ic_launcher);
		ncomp.setAutoCancel(true);
		nManager.notify((int)System.currentTimeMillis(),ncomp.build());  
	}
	
	private void showCarNames(){
		if(iTSService != null){
			iTSService.showCarNames();
		}
	}
	
	public void callBackFromShowCarNames(JSONObject iTSReceivedDict){
		if(iTSReceivedDict != null){
			try{
	  			String message = iTSReceivedDict.getString("message");
	  			if(message.equals("showCarSuccess")){
		  			iTSCars = iTSReceivedDict.getJSONArray("data");
		  			this.saveCarPlist();
	  			}else{
	  				Log.i(TAG, message);
	  			}
	  		}catch(JSONException e){
	  			
	  		}
		}
    }
	
	private void saveTypePlist(){
		if(typeJSONObject != null){
			try{
				if(gotBeaconBoolean){
					typeJSONObject = new JSONObject();
					typeJSONObject.put("type", gotBeaconBoolean);
					if(gotBeaconUUID != null){
						typeJSONObject.put("beaconUUID", gotBeaconUUID);
					}
					typeJSONObject.put("beaconMajor", gotBeaconMajor);
					typeJSONObject.put("beaconMinor", gotBeaconMinor);
					if(gotBeaconName != null){
						typeJSONObject.put("beaconName", gotBeaconName);
					}
					typeJSONObject.put("beaconThreshold", gotBeaconThresholdValue);
					typeJSONObject.put("beaconTolerance", gotBeaconToleranceValue);
				}else{
					typeJSONObject = new JSONObject();
					typeJSONObject.put("type", gotBeaconBoolean);
				}
			}catch(Exception e){
	   			
	   		}
			ITSJSONSharedPreferences.saveJSONObject(this.iTSService, "type", "type", typeJSONObject);
		}
	}

	private void loadTypePlist(){
		try{
			typeJSONObject = ITSJSONSharedPreferences.loadJSONObject(this.iTSService, "type", "type");
			if(typeJSONObject != null){
				gotBeaconBoolean = typeJSONObject.getBoolean("type");
				Log.i(TAG, "last type is " + (gotBeaconBoolean ? "YES" : "NO"));
				if(gotBeaconBoolean){
					if(typeJSONObject.getString("beaconUUID") != null)
						gotBeaconUUID = typeJSONObject.getString("beaconUUID");
					gotBeaconMajor = typeJSONObject.getInt("beaconMajor");
					gotBeaconMinor = typeJSONObject.getInt("beaconMinor");
					if(typeJSONObject.getString("beaconName") != null)
						gotBeaconName = typeJSONObject.getString("beaconName");
					gotBeaconThresholdValue = typeJSONObject.getInt("beaconThreshold");
					gotBeaconToleranceValue = typeJSONObject.getInt("beaconTolerance");
				}
			}else{
				gotBeaconBoolean = false;
			}
		}catch(Exception e){
   			
   		}
	}
	
	private void saveParameterPlist(){
		if(parameterJSONObject != null){
			try{
				parameterJSONObject = new JSONObject();
				parameterJSONObject.put("locationServiceEnabled", locationServiceEnabled);
				parameterJSONObject.put("rssiThreshold", rssiThresholdValue);
				parameterJSONObject.put("rssiTolerance", rssiToleranceValue);
				ITSJSONSharedPreferences.saveJSONObject(this.iTSService, "parameter", "parameter", parameterJSONObject);
			}catch(Exception e){
			}
		}
	}
	
	private void loadParameterPlist(){
		try{
			parameterJSONObject = ITSJSONSharedPreferences.loadJSONObject(this.iTSService, "parameter", "parameter");
			if(parameterJSONObject != null){
			    locationServiceEnabled = parameterJSONObject.getBoolean("locationServiceEnabled");
			    rssiThresholdValue = parameterJSONObject.getInt("rssiThreshold");
			    rssiToleranceValue = parameterJSONObject.getInt("rssiTolerance");
			}
		}catch(Exception e){
   			
   		}
	}
	
	private void saveCarPlist(){
		if(iTSCars != null){
			try{
				ITSJSONSharedPreferences.saveJSONArray(this.iTSService, "car", "car", iTSCars);
			}catch(Exception e){
	   			
	   		}
		}
	}
	
	private void loadCarPlist(){
		try{
			JSONArray tempITSCars = ITSJSONSharedPreferences.loadJSONArray(this.iTSService, "car", "car");
			if(tempITSCars != null){
				iTSCars = tempITSCars;
			}
		}catch(Exception e){
   			
   		}
	}
	
	public void resetTestBeacon(){
		
	}
	
	public void clearUserData(){
		//set end if in car now
	    if(gotBeaconBoolean){
			if(gotBeaconName != null && !gotBeaconName.equals("")){
				Log.i(TAG, "Left Car3 " + gotBeaconName);
				ITSBeaconManager.this.updateActionBarTitle("");
			}else{
				Log.i(TAG, "Left Car3 " + gotBeaconMajor + "," + gotBeaconMinor);
				ITSBeaconManager.this.updateActionBarTitle("");
			}
			//workaround
			int tempRssi = 0;
			if(gotBeacon != null)
				tempRssi = gotBeacon.getRssi();
			uploadRecordData(ITSBeaconManager.UploadType.TypeEnd, tempRssi, gotBeaconUUID, gotBeaconMajor, gotBeaconMinor, gotBeaconName);
			gotBeaconBoolean = false;
			gotBeacon = null;
			gotBeaconUUID = null;
			gotBeaconMajor = -1;
			gotBeaconMinor = -1;
			gotBeaconName = "";
			gotBeaconThresholdValue = rssiThresholdValue;
			gotBeaconToleranceValue = rssiToleranceValue;
			ITSBeaconManager.this.saveTypePlist();
			loseBeaconCount = 0;
	    }else{
	    	if(iTSService != null){
	    		iTSService.clearConnectionManagerUserData();
	    	}
	    }
		
		//clear user data
		rssiThresholdValue = -76; //default
	    rssiToleranceValue = 8; //default
	    locationServiceEnabled = true; //default
	    sensingCount = 5; //default
	    gotBeaconCount.clear();
	    loseBeaconCount = 0;
	    
	    //check last type
	    gotBeacon = null;
	    gotBeaconUUID = null;
	    gotBeaconMajor = -1;
	    gotBeaconMinor = -1;
	    gotBeaconName = "";
	    gotBeaconThresholdValue = rssiThresholdValue;
	    gotBeaconToleranceValue = rssiToleranceValue;
	    
	    //for testing beacon
	    testBeaconMode = false;
	    testBeaconCountThreshold = 5;
	    testBeaconCount = 0;
	    testBeaconRssiValue = 0;  
	    
	    iTSCars = new JSONArray();
	    this.saveCarPlist();
	}
}
