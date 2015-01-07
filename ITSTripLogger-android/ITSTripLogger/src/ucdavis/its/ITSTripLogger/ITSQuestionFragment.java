package ucdavis.its.ITSTripLogger;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ITSQuestionFragment extends Fragment {
	ITSTabBarActivity iTSTabBarActivity;
	ITSConnectionManager iTSConnectionManager;
	ITSBeaconManager iTSBeaconManager;
	View mainView;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	
		iTSTabBarActivity = (ITSTabBarActivity)activity;
		iTSTabBarActivity.iTSQuestionFragment = this;
		iTSConnectionManager = ITSConnectionManager.getInstance();
		iTSBeaconManager = ITSBeaconManager.getInstance();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mainView = inflater.inflate(R.layout.itsquestion, container, false);
		return mainView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	
	}

}
