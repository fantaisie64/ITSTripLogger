package ucdavis.its.ITSTripLogger;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

public class ITSRecordFragment extends ListFragment {
	public static final String TAG = "ITSRecordFragment";
	ITSTabBarActivity iTSTabBarActivity = null;
	private JSONArray iTSRecordArray = null;
	private ArrayList<String> iTSRecordStringArray = null;
	Button refreshButton;
	SupportMapFragment mapFragment;
	GoogleMap map;
	CustomListAdapter adapter;
	View lastView = null;

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);

    iTSTabBarActivity = (ITSTabBarActivity)activity;
    iTSTabBarActivity.iTSRecordFragment = this;
    iTSRecordArray = new JSONArray();
    iTSRecordStringArray = new ArrayList<String>();
    getRecordData();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	  	View v = inflater.inflate(R.layout.itsrecord, container, false);
	  	refreshButton = (Button)v.findViewById(R.id.refresehButton);
	  	refreshButton.setOnClickListener(new Button.OnClickListener(){ 
            @Override
            public void onClick(View v) {
            	getRecordData();
            }
        });
	  	mapFragment = (SupportMapFragment) this.getActivity().getSupportFragmentManager().findFragmentById(R.id.map);
	  	map = mapFragment.getMap();
	  	map.setMyLocationEnabled(true);
	  	
//	  	mapView = (MapView) v.findViewById(R.id.mapview);
//	  	mapView.onCreate(savedInstanceState);
//	  	map = mapView.getMap();
//		map.getUiSettings().setMyLocationButtonEnabled(false);
//		map.setMyLocationEnabled(true);
//		// Needs to call MapsInitializer before doing any CameraUpdateFactory calls
//		try {
//			MapsInitializer.initialize(this.getActivity());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		
//		// Updates the location and zoom of the MapView
//		CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(43.1, -87.9), 10);
//		map.animateCamera(cameraUpdate);
	  
	  //adapter = new ArrayAdapter<String>(inflater.getContext(), android.R.layout.simple_list_item_1,countries);
	  adapter = new CustomListAdapter(this.getActivity());
	  
      /** Setting the list adapter for the ListFragment */
      setListAdapter(adapter);
      return v;
  }
  
  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
  }
  
  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
//      Log.i("FragmentList", "Item clicked: " + id);
	  if(lastView != null)
		  lastView.setBackgroundColor(Color.TRANSPARENT);
	  v.setBackgroundColor(Color.LTGRAY);
	  lastView = v;
	  try{
		  JSONObject cellData = iTSRecordArray.getJSONObject(position);
		  String type = cellData.getString("type");
		  double latitude = cellData.getDouble("latitude");
		  double longitude = cellData.getDouble("longitude");
		  LatLng latLng = new LatLng(latitude, longitude);
		  map.clear();
		  map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
		  map.animateCamera(CameraUpdateFactory.zoomTo(15));
		  map.addMarker(new MarkerOptions().position(latLng).title(type));
		  Log.i(TAG, "Location:" + latitude + ", " + longitude);
	  }catch(Exception e){
		  
	  }
  }
  
  public void onDestroyView() 
  {
	  //if(this.getFragmentManager() != null){
	  //Log.i(TAG, "onDestroyView");
		 if (mapFragment != null){
			 try{
			     FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
			     ft.remove(mapFragment);
			     ft.commit();
			 }catch(Exception e){
				 e.printStackTrace();
			 }
		 }
	  //}
     super.onDestroyView();
 }
  
//  	@Override
//	public void onResume() {
//		mapView.onResume();
//		super.onResume();
//	}
//	
//	@Override
//	public void onDestroy() {
//		super.onDestroy();
//		mapView.onDestroy();
//	}
//	
//	@Override
//	public void onLowMemory() {
//		super.onLowMemory();
//		mapView.onLowMemory();
//	}
  
  	private void getRecordData(){
  		iTSTabBarActivity.getRecordData();
  	}
  	
  	public void callBackFromGetRecords(JSONObject iTSReceivedDict){
  		try{
  			iTSRecordArray = iTSReceivedDict.getJSONArray("data");
  			iTSRecordStringArray.clear();
  			for(int i=0; i<iTSRecordArray.length();i++){
  				try{
  					iTSRecordStringArray.add(iTSRecordArray.getJSONObject(i).getString("timestamp"));
  				}catch(Exception e){
  					
  				}
  			}
  			adapter.notifyDataSetChanged();
  		}catch(JSONException e){
  			
  		}
  	}
  	
  	public class CustomListAdapter extends ArrayAdapter<String>{
  		private final Activity context;
  		
  		public CustomListAdapter(Activity context) {
	  		super(context, R.layout.itsrecord_cell, iTSRecordStringArray);
	  		this.context = context;
  		}
  		
  		@Override
  		public View getView(int position, View view, ViewGroup parent) {
	  		LayoutInflater inflater = context.getLayoutInflater();
	  		View rowView= inflater.inflate(R.layout.itsrecord_cell, null, true);
	  		TextView titleTextView = (TextView) rowView.findViewById(R.id.title);
	  		TextView subtitleTextView = (TextView) rowView.findViewById(R.id.subtitle);
	  		ImageView imageView = (ImageView) rowView.findViewById(R.id.img);
	  		
	  		String type = "start";
	  		String title = "";
	  		String subtitle = "";
	  		String bt_name = null;
	  		String bt_major = "";
	  		String bt_minor = "";
	  		try{
	  			JSONObject cellData = iTSRecordArray.getJSONObject(position);
	  			type = cellData.getString("type");
	  			title = type + ": " + cellData.getString("timestamp");
	  			bt_major = cellData.getString("bt_major");
	  			bt_minor = cellData.getString("bt_minor");
	  			bt_name = cellData.getString("bt_name");
	  		}catch(Exception e){
	  		}
	  		
	  		if(bt_name != null && !bt_name.equals("")){
	  			subtitle = bt_name;
	  		}else{
	  			subtitle = "Major: " + bt_major + ", Minor: " + bt_minor;
	  		}
	  		
	  		titleTextView.setText(title);
	  		subtitleTextView.setText(subtitle);
	  		
	  		if(type.equals("start"))
	  			imageView.setImageResource(R.drawable.route1);
	  		else
	  			imageView.setImageResource(R.drawable.route2);
	  		return rowView;
  		}
  	}
  	
  	public void clearUserData(){
		//clear user data
  	    iTSRecordArray = new JSONArray();
  	    iTSRecordStringArray.clear();
	}
}