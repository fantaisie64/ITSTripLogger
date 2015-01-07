package ucdavis.its.ITSTripLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
 
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.os.AsyncTask;
import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.Toast;
import android.util.Log;

public class ITSConnectionManager {
	public static final String TAG = "ITSConnectionManager";
	private static ITSConnectionManager instance = null;
	public ITSService iTSService = null; 
	
	private static final String iTSGlobalToken = "ITSTRIPPROJECT2014";
	private static String privateITSSessionToken = "";
	//private static final String iTSServerURLConnectionString = "http://wayne.cs.ucdavis.edu:5000/mobile_connection";
	private static final String iTSServerURLSigninString = "http://wayne.cs.ucdavis.edu:5000/mobile_signin";
	private static final String iTSServerURLInsertCarString = "http://wayne.cs.ucdavis.edu:5000/mobile_insertCar";
	private static final String iTSServerURLShowCarString = "http://wayne.cs.ucdavis.edu:5000/mobile_showCar";
	private static final String iTSServerURLInsertRecordString = "http://wayne.cs.ucdavis.edu:5000/mobile_insertRecord";
	private static final String iTSServerURLInsertStoredRecordString = "http://wayne.cs.ucdavis.edu:5000/mobile_insertStoredRecord";
	private static final String iTSServerURLShowRecordString = "http://wayne.cs.ucdavis.edu:5000/mobile_showRecord";
	
	private static DefaultHttpClient iTSServerClient = null;
	//private static String iTSServerURL = null;
	private static HttpPost iTSServerPostRequest = null;
	private static HttpGet iTSServerGetRequest = null;
	private static JSONObject iTSServerPostDict = null;
	private static JSONObject iTSServerPostDictVerification = null;
	private static JSONObject iTSServerPostDictData = null;
	private static StringEntity iTSServerPostData = null;
	private static HttpResponse iTSResponse = null;
	private static HttpEntity iTSReceivedData = null;
	//private static JSONObject iTSReceivedDict = null;
	
	public ITSBeaconFragment iTSBeaconDelegate;
	
	public boolean signingOut = false;
	
	public String user_id = "";
	public String user_name = "";
	public String user_email = "";
	public String user_lname = "";
	public String user_fname = "";
	public String user_household_id = "";
	public String user_household_name = "";
	public String user_timestamp = "";
	
	private JSONArray storedRecordData = null;
	

	private ITSConnectionManager(){
		super();
		iTSServerClient = new DefaultHttpClient();
	}
	public static ITSConnectionManager getInstance(){
		if(instance == null)
			instance = new ITSConnectionManager();
		return instance;
	}
	
	public void verifyUser(String username, String password){
		iTSServerPostDict = null;

		try{
	    	iTSServerPostDictVerification = new JSONObject();
	    	iTSServerPostDictVerification.put("globalToken", iTSGlobalToken);
	        iTSServerPostDictData = new JSONObject();
	        iTSServerPostDictData.put("username", username);
	        iTSServerPostDictData.put("password", password);
	        iTSServerPostDict = new JSONObject();
	        iTSServerPostDict.put("message", "signin");
	        iTSServerPostDict.put("verification", iTSServerPostDictVerification);
	        iTSServerPostDict.put("data", iTSServerPostDictData);
		}catch(Exception e) {
            e.printStackTrace();
		}finally{
			new ITSConnectionAsyncTask(iTSServerURLSigninString, iTSServerPostDict).execute((Integer)ITSService.MSG_SIGNIN);	
		}
	}
	
	public void callBackFromVerifyUser(JSONObject iTSReceivedDict){
		try{
			privateITSSessionToken = iTSReceivedDict.getJSONObject("verification").getString("token");
			JSONObject iTSReceivedDictData = iTSReceivedDict.getJSONObject("data");
			user_id = iTSReceivedDictData.getString("id");
			user_name = iTSReceivedDictData.getString("username");
			user_email = iTSReceivedDictData.getString("email");
			user_lname = iTSReceivedDictData.getString("lname");
			user_fname = iTSReceivedDictData.getString("fname");
			user_household_id = iTSReceivedDictData.getString("household_id");
			user_household_name = iTSReceivedDictData.getString("household_name");
			user_timestamp = iTSReceivedDictData.getString("timestamp");
		}catch(JSONException e){
			
		}
		
		if(iTSService != null){
			iTSService.callBackFromVerifyUser(iTSReceivedDict);
		}
	}
	
	public void offlineLogin(String sessionToken, JSONObject userJSONObjectData){
		try{
			privateITSSessionToken = sessionToken;
			user_id = userJSONObjectData.getString("id");
			user_name = userJSONObjectData.getString("username");
			user_email = userJSONObjectData.getString("email");
			user_lname = userJSONObjectData.getString("lname");
			user_fname = userJSONObjectData.getString("fname");
			user_household_id = userJSONObjectData.getString("household_id");
			user_household_name = userJSONObjectData.getString("household_name");
			user_timestamp = userJSONObjectData.getString("timestamp");
		}catch(JSONException e){
			
		}
	}

	public void getRecordData(){
		iTSServerPostDict = null;

		try{
			iTSServerPostDictVerification = new JSONObject();
	    	iTSServerPostDictVerification.put("globalToken", iTSGlobalToken);
	    	iTSServerPostDictVerification.put("token", privateITSSessionToken);
	    	iTSServerPostDictVerification.put("id", user_id);
	    	iTSServerPostDictVerification.put("username", user_name);
	    	iTSServerPostDictVerification.put("email", user_email);
	    	iTSServerPostDictData = null;
	    	iTSServerPostDict = new JSONObject();
	    	iTSServerPostDict.put("message", "showRecord");
	        iTSServerPostDict.put("verification", iTSServerPostDictVerification);
		}catch(Exception e) {
            e.printStackTrace();
		}finally{
			new ITSConnectionAsyncTask(iTSServerURLShowRecordString, iTSServerPostDict).execute((Integer)ITSService.MSG_GET_RECORDDATA);	
		}
	}
	
	public void callBackFromGetRecordData(JSONObject iTSReceivedDict){
		if(iTSService != null){
			iTSService.callBackFromGetRecordData(iTSReceivedDict);
		}
	}
	
	
	
	private class ITSConnectionAsyncTask extends AsyncTask<Integer, Void, JSONObject>{
		private int operation = -1;
		private String URL = "";
	    private JSONObject jsonObjSend = null;

	    public ITSConnectionAsyncTask(String URL, JSONObject jsonObjSend) {
	        this.URL = URL;
	        this.jsonObjSend = jsonObjSend;
	    }

	    @Override
	    protected JSONObject doInBackground(Integer... params) {
	    	JSONObject jsonObjRecv = null;
	    	operation = params[0];
	    	try {
	    		iTSServerPostRequest = new HttpPost(URL);
	    		iTSServerPostData = new StringEntity(jsonObjSend.toString());

                // Set HTTP parameters
	    		iTSServerPostRequest.setEntity(iTSServerPostData);
	    		iTSServerPostRequest.setHeader("Accept", "application/json");
	    		iTSServerPostRequest.setHeader("Content-type", "application/json");

                //long t = System.currentTimeMillis();
                iTSResponse = (HttpResponse) iTSServerClient.execute(iTSServerPostRequest);
                //Log.i(TAG, "HTTPResponse received in [" + (System.currentTimeMillis()-t) + "ms]");

                iTSReceivedData = iTSResponse.getEntity();
                if (iTSReceivedData != null) {
                        InputStream instream = iTSReceivedData.getContent();
                        String resultString = convertStreamToString(instream);
                        instream.close();

                        jsonObjRecv = new JSONObject(resultString);
                }
            }
            catch (Exception e) {
                    e.printStackTrace();
            }
            return jsonObjRecv;         
	    }

	    protected void onPostExecute(JSONObject result) {
	    	if(result != null){
		    	switch(operation){
		    	case ITSService.MSG_SIGNIN:
		    		callBackFromVerifyUser(result);
		    		break;
		    	case ITSService.MSG_UPLOAD_RECORDDATA:
		    		callBackFromUploadRecordData(result);
		    		break;
		    	case ITSService.MSG_UPLOAD_STOREDDATA:
		    		callBackFromUploadStoredData(result);
		    		break;
		    	case ITSService.MSG_GET_RECORDDATA:
		    		callBackFromGetRecordData(result);
		    		break;
		    	case ITSService.MSG_SHOW_CAR:
		    		callBackFromShowCarNames(result);
		    		break;
		    	case ITSService.MSG_INSERT_CAR:
		    		callBackFromInsertCarName(result);
		    		break;
		    	default:
		    		break;
		    	}
	    	}else{
	    		if(iTSService != null){
	    			iTSService.callBackFromConnectionFail();
	    		}
	    	}
	    }
	
		private String convertStreamToString(InputStream is) {
	
	        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	        StringBuilder sb = new StringBuilder();
	
	        String line = null;
	        try {
	                while ((line = reader.readLine()) != null) {
	                        sb.append(line + "\n");
	                }
	        } catch (IOException e) {
	                e.printStackTrace();
	        } finally {
	                try {
	                        is.close();
	                } catch (IOException e) {
	                        e.printStackTrace();
	                }
	        }
	        return sb.toString();
		}
		
	}
	
	public void uploadRecordData(ITSRecordData recordData){
		iTSServerPostDict = null;
		Log.i(TAG, "upload record data");
		recordData.user_id = user_id;
	    recordData.username = user_name;
	    recordData.email = user_email;
	    recordData.lname = user_lname;
	    recordData.fname = user_fname;
	    recordData.household_id = user_household_id;
	    recordData.household_name = user_household_name;
		try{
			iTSServerPostDictVerification = new JSONObject();
	    	iTSServerPostDictVerification.put("globalToken", iTSGlobalToken);
	    	iTSServerPostDictVerification.put("token", privateITSSessionToken);
	    	iTSServerPostDictVerification.put("id", user_id);
	    	iTSServerPostDictVerification.put("username", user_name);
	    	iTSServerPostDictVerification.put("email", user_email);
	    	iTSServerPostDictData = new JSONObject();
	    	iTSServerPostDictData.put("user_id", recordData.user_id);
	    	iTSServerPostDictData.put("user_username", recordData.username);
	    	iTSServerPostDictData.put("user_email", recordData.email);
	    	iTSServerPostDictData.put("user_lname", recordData.lname);
	    	iTSServerPostDictData.put("user_fname", recordData.fname);
	    	iTSServerPostDictData.put("household_id", recordData.household_id);
	    	iTSServerPostDictData.put("household_name", recordData.household_name);
	    	iTSServerPostDictData.put("type", recordData.type);
	    	iTSServerPostDictData.put("latitude", recordData.latitude);
	    	iTSServerPostDictData.put("longitude", recordData.longitude);
	    	iTSServerPostDictData.put("timestamp", this.getTime(recordData.time_stamp));
	    	iTSServerPostDictData.put("bt_id", recordData.bt_id);
	    	iTSServerPostDictData.put("bt_major", recordData.bt_major);
	    	iTSServerPostDictData.put("bt_minor", recordData.bt_minor);
	    	iTSServerPostDictData.put("bt_name", recordData.bt_name);
	    	iTSServerPostDict = new JSONObject();
	    	iTSServerPostDict.put("message", "insertRecord");
	        iTSServerPostDict.put("verification", iTSServerPostDictVerification);
	        iTSServerPostDict.put("data", iTSServerPostDictData);
		}catch(Exception e) {
            e.printStackTrace();
		}finally{
			Log.i(TAG, "sending data");
			//backup new data
			this.loadRecordPlist();
			if(storedRecordData != null && storedRecordData.length() != 0){
				storedRecordData.put(iTSServerPostDictData);
				this.saveRecordPlist();
				this.uploadStoredData(recordData);
			}else{
				storedRecordData = new JSONArray();
				storedRecordData.put(iTSServerPostDictData);
				this.saveRecordPlist();
				new ITSConnectionAsyncTask(iTSServerURLInsertRecordString, iTSServerPostDict).execute((Integer)ITSService.MSG_UPLOAD_RECORDDATA);	
			}
			if(signingOut){
				clearUserData();
			}
		}
    }
	
	@SuppressLint("SimpleDateFormat")
	private String getTime(Date date){
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		dateFormat.setTimeZone(TimeZone.getDefault());
		return dateFormat.format(date);
	}
	
	public void uploadStoredData(ITSRecordData recordData){
		Log.i(TAG, "upload stored data");
		try{
			iTSServerPostDictVerification = new JSONObject();
	    	iTSServerPostDictVerification.put("globalToken", iTSGlobalToken);
	    	iTSServerPostDictVerification.put("token", privateITSSessionToken);
	    	iTSServerPostDictVerification.put("id", user_id);
	    	iTSServerPostDictVerification.put("username", user_name);
	    	iTSServerPostDictVerification.put("email", user_email);
	    	JSONArray names = new JSONArray();
	    	for(int i=0; i<storedRecordData.length();i++) names.put(String.valueOf(i));
	    	iTSServerPostDictData = storedRecordData.toJSONObject(names);
	    	iTSServerPostDict = new JSONObject();
	    	iTSServerPostDict.put("message", "insertStoredRecord");
	        iTSServerPostDict.put("verification", iTSServerPostDictVerification);
	        iTSServerPostDict.put("data", iTSServerPostDictData);
		}catch(Exception e) {
            e.printStackTrace();
		}finally{
			new ITSConnectionAsyncTask(iTSServerURLInsertStoredRecordString, iTSServerPostDict).execute((Integer)ITSService.MSG_UPLOAD_STOREDDATA);
		}
	}
	
	private void callBackFromUploadRecordData(JSONObject iTSReceivedDict){
		if(iTSReceivedDict != null){
			try{
       			if(iTSReceivedDict.getString("message").equals("insertRecordSuccess")){
       				this.clearRecordPlist();
       			}
			}catch(Exception e){
	   			
	   		}
		}
	}
	
	private void callBackFromUploadStoredData(JSONObject iTSReceivedDict){
		if(iTSReceivedDict != null){
			try{
       			if(iTSReceivedDict.getString("message").equals("insertStoredRecordSuccess")){
       				this.clearRecordPlist();
       			}
			}catch(Exception e){
	   		
			}
		}
	}
	
	public void showCarNames(){
		iTSServerPostDict = null;

		try{
			iTSServerPostDictVerification = new JSONObject();
	    	iTSServerPostDictVerification.put("globalToken", iTSGlobalToken);
	    	iTSServerPostDictVerification.put("token", privateITSSessionToken);
	    	iTSServerPostDictVerification.put("id", user_id);
	    	iTSServerPostDictVerification.put("username", user_name);
	    	iTSServerPostDictVerification.put("email", user_email);
	    	iTSServerPostDictData = new JSONObject();
	    	iTSServerPostDictData.put("household_id", user_household_id);
	    	iTSServerPostDict = new JSONObject();
	    	iTSServerPostDict.put("message", "showCar");
	        iTSServerPostDict.put("verification", iTSServerPostDictVerification);
	        iTSServerPostDict.put("data", iTSServerPostDictData);
		}catch(Exception e) {
            e.printStackTrace();
		}finally{
			new ITSConnectionAsyncTask(iTSServerURLShowCarString, iTSServerPostDict).execute((Integer)ITSService.MSG_SHOW_CAR);	
		}
	}
	
	private void callBackFromShowCarNames(JSONObject iTSReceivedDict){
		if(iTSService != null){
			iTSService.callBackFromShowCarNames(iTSReceivedDict);
		}
	}
	
	public String getITSSessionToken(){
		return privateITSSessionToken;
	}
	
	public void insertCarName(String selectedBeaconName, String selectedBeaconUUID, 
			int selectedBeaconMajor, int selectedBeaconMinor, 
			int selectedBeaconRssiThresholdValue, int selectedBeaconRssiToleranceValue){
		iTSServerPostDict = null;

		try{
			iTSServerPostDictVerification = new JSONObject();
	    	iTSServerPostDictVerification.put("globalToken", iTSGlobalToken);
	    	iTSServerPostDictVerification.put("token", privateITSSessionToken);
	    	iTSServerPostDictVerification.put("id", user_id);
	    	iTSServerPostDictVerification.put("username", user_name);
	    	iTSServerPostDictVerification.put("email", user_email);
	    	iTSServerPostDictData = new JSONObject();
	    	iTSServerPostDictData.put("bt_id", selectedBeaconUUID);
	    	iTSServerPostDictData.put("bt_major", selectedBeaconMajor);
	    	iTSServerPostDictData.put("bt_minor", selectedBeaconMinor);
	    	iTSServerPostDictData.put("bt_name", selectedBeaconName);
	    	iTSServerPostDictData.put("bt_threshold", selectedBeaconRssiThresholdValue);
	    	iTSServerPostDictData.put("bt_tolerance", selectedBeaconRssiToleranceValue);
	    	iTSServerPostDictData.put("household_id", user_household_id);
	    	iTSServerPostDictData.put("household_name", user_household_name);
	    	iTSServerPostDictData.put("timestamp", this.getTime(new Date()));
	    	iTSServerPostDict = new JSONObject();
	    	iTSServerPostDict.put("message", "showCar");
	        iTSServerPostDict.put("verification", iTSServerPostDictVerification);
	        iTSServerPostDict.put("data", iTSServerPostDictData);
		}catch(Exception e) {
            e.printStackTrace();
		}finally{
			new ITSConnectionAsyncTask(iTSServerURLInsertCarString, iTSServerPostDict).execute((Integer)ITSService.MSG_INSERT_CAR);	
		}
	}
	
	private void callBackFromInsertCarName(JSONObject iTSReceivedDict){
		this.showCarNames();
	}
	
	private void loadRecordPlist(){
		try{
			storedRecordData = new JSONArray();
			JSONArray tempStoredRecordData = ITSJSONSharedPreferences.loadJSONArray(this.iTSService, "record", "record");
			if(tempStoredRecordData != null){
				storedRecordData = tempStoredRecordData;
			}
		}catch(Exception e){
   			
   		}
	}
	
	private void clearRecordPlist(){
		storedRecordData = new JSONArray();
		this.saveRecordPlist();
	}
	
	private void saveRecordPlist(){
		if(storedRecordData != null){
			try{
				ITSJSONSharedPreferences.saveJSONArray(this.iTSService, "record", "record", storedRecordData);
			}catch(Exception e){
	   			
	   		}
		}
	}
	
	public void clearUserData(){
		signingOut = false;
		//clear user data
		iTSServerPostRequest = null;
		iTSServerGetRequest = null;
		iTSServerPostDict = null;
		iTSServerPostDictVerification = null;
		iTSServerPostDictData = null;
		iTSServerPostData = null;
		iTSResponse = null;
		iTSReceivedData = null;
		
		user_id = "";
		user_name = "";
		user_email = "";
		user_lname = "";
		user_fname = "";
		user_household_id = "";
		user_household_name = "";
		user_timestamp = "";
	}
}
