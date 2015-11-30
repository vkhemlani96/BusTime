package com.khemlani.bustime.busstop;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.khemlani.bustime.data.BusStopViewController;
import com.khemlani.bustime.data.Route;

public class BusStopDialog extends DialogFragment {

	private BusStopViewController stop;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
		return stop.getView();
	}
	
	public BusStopDialog newInstance(Context c, Route route, String preferenceString) {
		this.stop = new BusStopViewController(c, route, preferenceString);
		return this;
	}
	
}
