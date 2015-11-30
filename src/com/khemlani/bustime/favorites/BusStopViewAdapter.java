package com.steelhawks.hawkscout.favorites;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.khemlani.bustime.data.Direction;
import com.khemlani.bustime.data.Direction.BusStop;
import com.khemlani.bustime.data.Route;

public class BusStopViewAdapter extends ArrayAdapter<FavoriteViewController> {

	List<FavoriteViewController> stops;
	Context context;
	LinearLayout rootView;
	Route r;
	Direction d;
	BusStop s;
	int resource;
	
	ListView listView;
	
	public BusStopViewAdapter(Context context, int resource, List<FavoriteViewController> stops, ListView listView) {
		super(context, resource, stops);
		this.context = context;
		this.stops = stops;
		this.resource = resource;
		this.listView = listView;
	}	

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		System.out.println("Getting View");
		LinearLayout v = (LinearLayout) stops.get(position).getView(position == stops.size()-1);
		return v;
	}
	
	private int PX (int dp) {
		final float scale = context.getResources().getDisplayMetrics().density;
		int px = (int) (dp*scale+0.5f);
		return px;
	}

}
