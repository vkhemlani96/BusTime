package com.khemlani.bustime.busstop;

import java.io.Serializable;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.khemlani.bustime.R;
import com.khemlani.bustime.R.id;
import com.khemlani.bustime.R.layout;
import com.khemlani.bustime.R.menu;
import com.khemlani.bustime.data.Direction;
import com.khemlani.bustime.data.Direction.BusStop;
import com.khemlani.bustime.data.Route;

public class BusStopActivityDEPRECATED extends Activity {

	private final static String ROUTE_KEY = "com.khemlani.bustime.BusStopActivity.ROUTE_KEY";
	private final static String DIRECTION_INDEX = "com.khemlani.bustime.BusStopActivity.DIRECTION_INDEX";
	private final static String BUS_STOP_INDEX = "com.khemlani.bustime.BusStopActivity.BUS_STOP_INDEX";

	private Route route;
	private Direction direction;
	private BusStop busStop;
	private int directionIndex;
	private int busStopIndex;
	private String[] busStops = new String[4];
	private String[] busLocations = new String[4];
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stop);
		
		route = (Route) getIntent().getSerializableExtra(ROUTE_KEY);
		directionIndex = getIntent().getIntExtra(DIRECTION_INDEX, -1);
		direction = route.getDirections()[directionIndex];
		busStopIndex = getIntent().getIntExtra(BUS_STOP_INDEX, -1);
		busStop = direction.getStops().get(busStopIndex);
		
		// Show the Up button in the action bar.
		setupActionBar();

		((TextView) findViewById(R.id.direction_label)).setText("Next Bus " + direction.getTitle());
		getBusLocation(busStops, busLocations);
		((TextView) findViewById(R.id.bus_location)).setText(busStops[0]);
		if (busLocations[0] != null)
			((TextView) findViewById(R.id.bus_exact_location)).setText("Location of Bus: " + busLocations[0]);
		else findViewById(R.id.bus_exact_location).setVisibility(View.GONE);

		((TextView) findViewById(R.id.next_bus_stop_1)).setText(busStops[1]);
		((TextView) findViewById(R.id.next_bus_location_1)).setText(busLocations[1]);
		((TextView) findViewById(R.id.next_bus_stop_2)).setText(busStops[2]);
		((TextView) findViewById(R.id.next_bus_location_2)).setText(busLocations[2]);
		((TextView) findViewById(R.id.next_bus_stop_3)).setText(busStops[3]);
		((TextView) findViewById(R.id.next_bus_location_3)).setText(busLocations[3]);
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		if (route == null) {
			System.out.println("Route is null");
			return;
		}
		actionBar.setTitle(route.getRouteName());
		actionBar.setSubtitle(busStop.getName());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.stop, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public static void start(Activity a, Serializable route, int directionIndex, int busStopIndex) {
		Intent intent = new Intent(a.getBaseContext(), BusStopActivityDEPRECATED.class);
		intent.putExtra(ROUTE_KEY, route);
		intent.putExtra(DIRECTION_INDEX, directionIndex);
		intent.putExtra(BUS_STOP_INDEX, busStopIndex);
		a.startActivity(intent);
	}
	
	public void getBusLocation(String[] busStops, String[] busLocations) {
		int arrayIndex = 0;
		if (!busStop.getBusLocation().equals("")) {
			busStops[arrayIndex++] = busStop.getBusLocation().get(0);
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
	}
	
}
