package com.khemlani.bustime.route;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.khemlani.bustime.R;
import com.khemlani.bustime.busstop.BusStopDialog;
import com.khemlani.bustime.data.Direction;
import com.khemlani.bustime.data.Direction.BusStop;
import com.khemlani.bustime.data.Route;
import com.steelhawks.hawkscout.favorites.Preferences;

public class RouteFragment extends DialogFragment implements OnItemClickListener {
	
	private Route route;
	private int directionIndex;
	private Direction routeDirection;
	private Context context;
	private ListView list;
	private String requestedStop;
	
	public RouteFragment() {}
	
	public RouteFragment newInstance(Context c, Route r, int index) {
		context = c;
		route = r;
		directionIndex = index;
		routeDirection = route.getDirections()[index];
		return this;
	}
	
	public void setStopToClick(String stopName) {
		this.requestedStop = stopName;
	}
	
	public void clickOnStop() {
		int requestedStopIndex = -1;
		int highestedFoundCount = 0;
		List<BusStop> stops = routeDirection.getStops();
		for (int x = 0; x < stops.size(); x++) {
			if (requestedStop.trim().toUpperCase(Locale.US).equals(stops.get(x).getName().trim().toUpperCase(Locale.US))) {
				list.smoothScrollToPosition(x);
				list.performItemClick(list.getChildAt(x), x, list.getItemIdAtPosition(x));
				return;
			}
		}
		for (int x = 0; x < stops.size(); x++) {
			int foundCount = compareStrings(requestedStop, stops.get(x).getName());
			if (foundCount > highestedFoundCount) {
				requestedStopIndex = x;
			}
		}
		System.out.println("Requested Stop:" + requestedStopIndex);
		if (requestedStopIndex != -1){
			System.out.println(list == null);
			System.out.println(list.getChildAt(requestedStopIndex) == null);
			System.out.println(list.getItemIdAtPosition(requestedStopIndex));
			list.performItemClick(list.getChildAt(requestedStopIndex), requestedStopIndex, list.getItemIdAtPosition(requestedStopIndex));
		}
	}
	
	private int compareStrings(String searchString, String controlString) {
		System.out.println(searchString);
		System.out.println(controlString);
		String[] searchArray = searchString.trim().split(" ");
		List<String> controlArray = Arrays.asList(controlString.trim().split(" "));
		int foundCount = 0;
		for (int x=0; x<searchArray.length; x++) {
			if (controlArray.contains(searchArray[x]))
				foundCount++;
		}
		System.out.println(foundCount);
		return foundCount;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LinearLayout rootView = (LinearLayout) inflater.inflate(R.layout.activity_route_fragment,
				container, false);
		
		list = (ListView) rootView.findViewById(R.id.stop_list);
		list.setAdapter(new BusStopAdapter(context, R.layout.activity_route_list_row, routeDirection.getStops()));
		list.setOnItemClickListener(this);
		if (requestedStop != null) clickOnStop();
		return rootView;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		String preferenceString = route.getRouteName() + Preferences.STOP_SPLIT +
				routeDirection.getTitle() + Preferences.STOP_SPLIT + 
				routeDirection.getStops().get(arg2).getName();
		new BusStopDialog().newInstance(context, route, preferenceString)
			.show(getActivity().getSupportFragmentManager(), "BUS_STOP_DIALOG");
	}
	
	private class BusStopAdapter extends ArrayAdapter<BusStop> {
		
		Context context;
		List<BusStop> busStops;

		public BusStopAdapter(Context context, int resource,
				List<BusStop> objects) {
			super(context, resource, objects);
			this.context = context;
			this.busStops = objects;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
		    LayoutInflater inflater = (LayoutInflater) context
		            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			LinearLayout rowLayout = (LinearLayout) inflater.inflate(R.layout.activity_route_list_row, parent, false);
			
			TextView stopName = (TextView) rowLayout.findViewById(R.id.stop_name);
			stopName.setText(capWords(busStops.get(position).getName()));
			
			if (!busStops.get(position).isBusAtStop()) {
				ImageView busIcon = (ImageView) rowLayout.findViewById(R.id.bus_icon);
				busIcon.setVisibility(View.GONE);
			} else {
				System.out.println(busStops.get(position).getBusLocation().size());
				if (busStops.get(position).getBusLocation().size() > 1) {
					((TextView) rowLayout.findViewById(R.id.multiple_buses))
					.setText("x" + busStops.get(position).getBusLocation().size());
				}
			}
			return rowLayout;
		}
		
	}
	
	private String capWords(String s) {
		char[] sChars = s.toLowerCase(Locale.US).toCharArray();
		sChars[0] = Character.toUpperCase(sChars[0]);
		Pattern p = Pattern.compile("\\W");
		for (int x=1; x<sChars.length; x++) {
			if (Pattern.matches("\\W", String.valueOf(sChars[x-1]))) sChars[x] = Character.toUpperCase(sChars[x]);
		}
		return String.valueOf(sChars);
	}
	
}
