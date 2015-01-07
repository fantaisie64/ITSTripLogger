package ucdavis.its.ITSTripLogger;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.estimote.sdk.Beacon;
import com.estimote.sdk.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

public class BeaconListAdapter extends BaseAdapter {

  private ArrayList<Beacon> beacons;
  private ArrayList<String> names;
  private LayoutInflater inflater;

  public BeaconListAdapter(Context context) {
    this.inflater = LayoutInflater.from(context);
    this.beacons = new ArrayList<Beacon>();
    this.names = new ArrayList<String>();
  }

  public void replaceWith(Collection<Beacon> newBeacons, Collection<String> newNames) {
    this.beacons.clear();
    this.names.clear();
    this.beacons.addAll(newBeacons);
    this.names.addAll(newNames);
    notifyDataSetChanged();
  }

  @Override
  public int getCount() {
    return beacons.size();
  }

  @Override
  public Beacon getItem(int position) {
	  return beacons.get(position);
  }
  
  public String getName(int position) {
	  return names.get(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(int position, View view, ViewGroup parent) {
    view = inflateIfRequired(view, position, parent);
    bind(getItem(position), getName(position), view);
    return view;
  }

  private void bind(Beacon beacon, String name, View view) {
	ViewHolder holder = (ViewHolder) view.getTag();
	holder.imageView.setImageResource(R.drawable.car);
	
	if(name != null){
		holder.titleTextView.setText(name);
		holder.subtitleTextView.setText("Major: " + beacon.getMajor() + " Minor: " + beacon.getMinor() + " RSSI: " + beacon.getRssi());
	}else{
		holder.titleTextView.setText("Major: " + beacon.getMajor() + " Minor: " + beacon.getMinor());
		holder.subtitleTextView.setText("RSSI: " + beacon.getRssi());
	}
  }

  private View inflateIfRequired(View view, int position, ViewGroup parent) {
    if (view == null) {
      view = inflater.inflate(R.layout.itsbeacon_cell, null, true);
      view.setTag(new ViewHolder(view));
    }
    return view;
  }

  static class ViewHolder {
	final ImageView imageView;
    final TextView titleTextView;
    final TextView subtitleTextView;

    ViewHolder(View view) {
    	imageView = (ImageView) view.findViewById(R.id.img);
    	titleTextView = (TextView) view.findViewById(R.id.title);
    	subtitleTextView = (TextView) view.findViewById(R.id.subtitle);
    }
  }
}
