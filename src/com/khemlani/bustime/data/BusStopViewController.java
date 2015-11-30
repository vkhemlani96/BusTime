package com.khemlani.bustime.data;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.khemlani.bustime.R;
import com.khemlani.bustime.TaskListener;
import com.khemlani.bustime.busstop.DurationTask;
import com.khemlani.bustime.data.Direction.BusStop;
import com.steelhawks.hawkscout.favorites.Preferences;

public class BusStopViewController implements TaskListener {
	
	protected Route route;
	protected Direction direction;
	private int directionIndex;
	protected BusStop busStop;
	protected String preferenceString;
	private Context c;
	
	private LinearLayout rootView;
	
	private String[] busStops = new String[4];
	private String[] busLocations = new String[4];
	
	protected Preferences favPrefs;
	protected boolean isStopFavorite = true;
	private OnClickListener toggleFavoriteListener = new OnClickListener(){

		@Override
		public void onClick(View v) {
			if (isStopFavorite) {
				favPrefs.removeFavoriteStop(preferenceString);
			} else {
				favPrefs.addFavoriteStop(preferenceString);
			}
			isStopFavorite = !isStopFavorite;
			defineFavoriteView();
		}
		
	};

	public BusStopViewController(Context c, Route r, String preferenceString) {
		System.out.println(preferenceString);
		this.c = c;
		this.route = r;
		this.preferenceString = preferenceString;
		String directionName = preferenceString.split(Preferences.STOP_SPLIT)[1].trim();
		String stopName = preferenceString.split(Preferences.STOP_SPLIT)[2].trim();
		for (int x = 0; x<route.getDirections().length; x++) {
			if (route.getDirections()[x].getTitle().trim().toUpperCase(Locale.US).equals(directionName)) {
				this.direction = route.getDirections()[x];
				this.directionIndex = x;
				break;
			}
		}
		if (direction == null) {
			System.err.println("Could not find direction data for " + route.getRouteName());
			return;
		}
		for (int x=0; x<direction.getStops().size(); x++) {
			if (direction.getStops().get(x).getName().trim().equals(stopName)) {
				busStop = direction.getStops().get(x);
				break;
			}
		}
		if (busStop == null) {
			System.err.println("Could not find stop data for " + route.getRouteName());
			return;
		}
		
		this.favPrefs = new Preferences(c);
		this.isStopFavorite = isStopFavorite(favPrefs.getFavoriteStops(), preferenceString);
		createView(c);
	}
	
	private void createView(Context c) {
		LayoutInflater inflater = LayoutInflater.from(c);
		rootView = (LinearLayout) inflater.inflate(R.layout.activity_stop, null, false);

		((TextView) rootView.findViewById(R.id.direction_label)).setText(busStop.getName() + "\n" + direction.getTitle());
		getBusLocation(busStops, busLocations);
		
		((TextView) rootView.findViewById(R.id.bus_location)).setText(busStops[0]);
		if (busLocations[0] != null)
			((TextView) rootView.findViewById(R.id.bus_exact_location)).setText("Location of Bus: " + busLocations[0]);
		else {
			rootView.findViewById(R.id.bus_exact_location).setVisibility(View.GONE);
			((TextView) rootView.findViewById(R.id.bus_location)).setPadding(0, 0, 0, PX(8));
		}

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
		rootView.setTag(this);
		defineFavoriteView();
		setOnClickListener(toggleFavoriteListener);
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
		//TODO fix this
		else if (busStops[0] == null) return;
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
	
	public View getView() {
		return rootView;
	}
	
	private boolean isStopFavorite(Set<String> set, String preferenceString) {
		if (set == null) return false;
		for (String s : set) {
			if (s.equals(preferenceString)) return true;
		}
		return false;
	}
	
	protected void defineFavoriteView() {
		TextView favButton = (TextView) rootView.findViewById(R.id.favorite_button);
		if (isStopFavorite) {
			favButton.setText("Remove from Favorites");
			favButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_rating_important,0,0,0);
		}
		else {
			favButton.setText("Add to Favorites");
			favButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_rating_not_important,0,0,0);
		}
	}
	
	public void click() {
		if (isStopFavorite) {
			favPrefs.removeFavoriteStop(preferenceString);
		} else {
			favPrefs.addFavoriteStop(preferenceString);
		}
		isStopFavorite = !isStopFavorite;
		defineFavoriteView();
	}
	
	protected void setOnClickListener(OnClickListener l) {
		View favButtonLayout = (View) rootView.findViewById(R.id.favorite_button).getParent();
		favButtonLayout.setOnClickListener(l);
		defineFavoriteView();
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
	
	private int PX (int dp) {
		final float scale = c.getResources().getDisplayMetrics().density;
		int px = (int) (dp*scale+0.5f);
		return px;
	}
	
}
