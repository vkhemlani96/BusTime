package com.khemlani.bustime.busstop;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.khemlani.bustime.R;
import com.khemlani.bustime.TaskListener;
import com.khemlani.bustime.data.Direction;
import com.khemlani.bustime.data.Direction.BusStop;
import com.khemlani.bustime.data.Route;
import com.steelhawks.hawkscout.favorites.Preferences;

public class BusStopDialogDEPRECATED extends DialogFragment implements TaskListener {

	private Route route;
	private int directionIndex;
	private Direction direction;
	private int busStopIndex;
	private BusStop busStop;
	private Preferences favStops;
	private boolean isStopFavorite;
	private String preferenceString;
	
	private LinearLayout rootView;
	
	private String[] busStops = new String[4];
	private String[] busLocations = new String[4];
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.favStops = new Preferences(getActivity());
		this.preferenceString = route.getRouteName() + Preferences.STOP_SPLIT + 
				direction.getTitle() + Preferences.STOP_SPLIT + busStop.getName();
		this.isStopFavorite = isStopFavorite(favStops.getFavoriteStops());	
		return getView(inflater, container);
	}
	
	public View getView(LayoutInflater inflater, ViewGroup container) {
		rootView = (LinearLayout) inflater.inflate(R.layout.activity_stop, container, false);
		
		((TextView) rootView.findViewById(R.id.direction_label)).setText(busStop.getName());
		getBusLocation(busStops, busLocations);
		((TextView) rootView.findViewById(R.id.bus_location)).setText(busStops[0]);
		if (busLocations[0] != null)
			((TextView) rootView.findViewById(R.id.bus_exact_location)).setText("Location of Bus: " + busLocations[0]);
		else rootView.findViewById(R.id.bus_exact_location).setVisibility(View.GONE);

		if (busStops[1] == null) {
			rootView.findViewById(R.id.next_bus_stop_1).setVisibility(View.GONE);
			rootView.findViewById(R.id.next_bus_location_1).setVisibility(View.GONE);
		} else {
			((TextView) rootView.findViewById(R.id.next_bus_stop_1)).setText(busStops[1]);
			if (busLocations[1] == null) {
				((TextView) rootView.findViewById(R.id.next_bus_stop_1)).setTypeface(null, Typeface.BOLD);
				((TextView) rootView.findViewById(R.id.next_bus_location_1)).setVisibility(View.GONE);
			} else 
				((TextView) rootView.findViewById(R.id.next_bus_location_1)).setText(busLocations[1]);
		}
		if (busStops[2] == null) {
			((View) rootView.findViewById(R.id.next_bus_stop_2).getParent()).setVisibility(View.GONE);
			rootView.findViewById(R.id.seperator_2).setVisibility(View.GONE);
		} else {
			((TextView) rootView.findViewById(R.id.next_bus_stop_2)).setText(busStops[2]);
			if (busLocations[2] == null) {
				((TextView) rootView.findViewById(R.id.next_bus_stop_2)).setTypeface(null, Typeface.BOLD);
				((TextView) rootView.findViewById(R.id.next_bus_location_2)).setVisibility(View.GONE);
			} else 
				((TextView) rootView.findViewById(R.id.next_bus_location_2)).setText(busLocations[2]);
		}
		if (busStops[3] == null) {
			((View) rootView.findViewById(R.id.next_bus_stop_3).getParent()).setVisibility(View.GONE);
			rootView.findViewById(R.id.seperator_3).setVisibility(View.GONE);
		} else {
			((TextView) rootView.findViewById(R.id.next_bus_stop_3)).setText(busStops[3]);
			if (busLocations[3] == null) {
				((TextView) rootView.findViewById(R.id.next_bus_stop_3)).setTypeface(null, Typeface.BOLD);
				((TextView) rootView.findViewById(R.id.next_bus_location_3)).setVisibility(View.GONE);
			} else 
				((TextView) rootView.findViewById(R.id.next_bus_location_3)).setText(busLocations[3]);
		}
		View favButtonLayout = (View) rootView.findViewById(R.id.favorite_button).getParent();
		favButtonLayout.setOnClickListener(toggleFavoriteListener);
		defineFavoriteView();
		return rootView;
	}
	
	public BusStopDialogDEPRECATED newInstance(Route route, int directionIndex, int busStopIndex) {
		this.route = route;
		this.directionIndex = directionIndex;
		this.direction = route.getDirections()[directionIndex];
		this.busStopIndex = busStopIndex;
		this.busStop = direction.getStops().get(busStopIndex);
		return this;
	}
	
	private boolean isStopFavorite(Set<String> set) {
		if (set == null) return false;
		for (String s : set) {
			if (s.equals(preferenceString)) return true;
		}
		return false;
	}
	
	private void defineFavoriteView() {
		if (isStopFavorite) {
			TextView favButton = (TextView) rootView.findViewById(R.id.favorite_button);
			favButton.setText("Remove from Favorites");
			favButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_rating_important,0,0,0);
		}
		else {
			TextView favButton = (TextView) rootView.findViewById(R.id.favorite_button);
			favButton.setText("Add to Favorites");
			favButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_rating_not_important,0,0,0);
		}
	}
	
	public void getBusLocation(String[] busStops, String[] busLocations) {
		int arrayIndex = 0;
		if (busStop.getBusLocation().size()>0) {
			for (int x=0; x<busStop.getBusLocation().size() && arrayIndex<4; x++) {
				busStops[arrayIndex++] = busStop.getBusLocation().get(x);
			}
		}
		int indexOfStop = direction.getStops().indexOf(busStop);
		int stopsAway = 0;
		for (int x = indexOfStop-1; x>=0 && arrayIndex < 4; x--) {
			stopsAway++;
			if (direction.getStops().get(x).isBusAtStop()) {
				busStops[arrayIndex] = stopsAway + (stopsAway == 1 ? " Stop Away" : " Stops Away");
				busLocations[arrayIndex++] = direction.getStops().get(x).getName();
			}
		}
		if (arrayIndex < 4) {
			Direction alternateDirection = 
					route.getDirections().length == directionIndex+1 ? route.getDirections()[directionIndex-1] :
						route.getDirections()[directionIndex+1];
			for (int x=alternateDirection.getStops().size()-1; x>=0 && arrayIndex < 4; x--) {
				stopsAway++;
				if (alternateDirection.getStops().get(x).isBusAtStop()) {
					busStops[arrayIndex] = 
							stopsAway + (stopsAway == 1 ? " Stop Away + Layover" : " Stops Away + Layover");
					busLocations[arrayIndex++] = alternateDirection.getStops().get(x).getName();
				}
			}
		}
		if (busStop.getBusLocation().contains(busStops[0])) onTaskFinished(null);
		else if (busStops[0].contains("Layover")) {
			System.out.println("Contains layover....");
			String[] locations = {busLocations[0], busStop.getName(), direction.getStops().get(0).getName()};
			new DurationTask().newInstance(this).execute(locations);
		} else {
			System.out.println("Doesn't contain layover....");
			String[] locations = {busLocations[0], busStop.getName()};
			new DurationTask().newInstance(this).execute(locations);
		}
	}

	@Override
	public void onTaskFinished(String response) {
		if (response == null) {
			rootView.findViewById(R.id.eta).setVisibility(View.GONE);
		} else {
			double minutes = Integer.parseInt(busStops[0].replaceAll("\\D", "")) * (40.0/60);
			Pattern minutesPattern = Pattern.compile("\\d{1,}");
			Matcher m = minutesPattern.matcher(response);
			if (m.find()) {
				String minutesString = m.group(m.groupCount());
				minutes += Integer.parseInt(minutesString);
				response = response.replace(minutesString + " mins", ((int) (minutes + .5)) + " mins");
			} else {
				System.out.println("Not found");
			}
			System.out.println(busStops[0]);
			if (busStops[0].contains("Layover"))
				((TextView) rootView.findViewById(R.id.eta)).setText("Approx. " + response + " away + layover time");
			else ((TextView) rootView.findViewById(R.id.eta)).setText("Approx. " + response + " away");

		}
	}
	
	private OnClickListener toggleFavoriteListener = new OnClickListener(){

		@Override
		public void onClick(View v) {
			if (isStopFavorite) {
				favStops.removeFavoriteStop(preferenceString);
			} else {
				favStops.addFavoriteStop(preferenceString);
			}
			isStopFavorite = !isStopFavorite;
			defineFavoriteView();
		}
		
	};
	
}
