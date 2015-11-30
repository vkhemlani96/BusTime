package com.khemlani.bustime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.khemlani.bustime.data.BusStopViewController;
import com.khemlani.bustime.data.Direction;
import com.khemlani.bustime.data.Route;
import com.khemlani.bustime.dialogs.DidYouMeanDialog;
import com.khemlani.bustime.dialogs.NoResultsDialog;
import com.khemlani.bustime.route.RouteTask;
import com.steelhawks.hawkscout.favorites.BusStopViewAdapter;
import com.steelhawks.hawkscout.favorites.Preferences;
import com.steelhawks.hawkscout.favorites.FavoriteViewController;

public class FavoritesActivity extends FragmentActivity implements TaskListener {

	List<Route> routes = new ArrayList<Route>();
	Preferences stops;
	Set<String> favoriteSet;
	int routeCount;
	ListView list;
	ProgressDialog progress;
	List<String> routeNumberList;
	MenuItem refreshItem;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_favorites);
		setTitle("BusTime");

		stops = new Preferences(this);
		favoriteSet = stops.getFavoriteStops();
		routeNumberList = new ArrayList<String>();
		for (String s : favoriteSet) {
			String routeNumber = s.split(Preferences.STOP_SPLIT)[0];
			if (!routeNumberList.contains(routeNumber)) routeNumberList.add(routeNumber);
		}
		routeCount = routeNumberList.size();
		getData();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.favorites, menu);
		refreshItem = menu.findItem(R.id.action_refresh)
				.setOnMenuItemClickListener(new OnMenuItemClickListener() {

					@Override
					public boolean onMenuItemClick(MenuItem item) {
						refreshData();
						return false;
					}
				});
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onResume() {
		super.onResume();
		if (list != null)
			((ArrayAdapter<FavoriteViewController>) list.getAdapter()).notifyDataSetChanged();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		String result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
		if (result != null && result.contains("http://bustime.mta.info/s/"))
			StopCodeActivity.start(this, result.replaceAll("http://bustime.mta.info/s/", "").trim());
	}
	
	private void refreshData() {
		refreshItem.setVisible(false);
		setProgressBarIndeterminateVisibility(true);
		getData();
	}
	
	private void getData() {
		routes.clear();
		for (String s : routeNumberList) {
			getHTML(s);
		}
	}

	public void onClick(View view) {
		View busStopView  = (View) view.getParent();
		BusStopViewController viewController = (BusStopViewController) busStopView.getTag();
		viewController.click();
	}

	public void searchClicked(View v) {
		final View searchLayout = findViewById(R.id.favorites_search);
		final boolean hidden = searchLayout.getVisibility() != View.VISIBLE;
		final int initialHeight = PX(55);
		System.out.println(DP(initialHeight));
		Animation anim = new Animation() {
			@Override
			protected void applyTransformation(float interpolatedTime, Transformation t) {
				if (interpolatedTime == 1) {
					if (!hidden) searchLayout.setVisibility(View.GONE);
				}
				else {
					if (hidden) {
						searchLayout.getLayoutParams().height = (int)(initialHeight * interpolatedTime);
						searchLayout.setVisibility(View.VISIBLE);
					} else 
						searchLayout.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
					searchLayout.requestLayout();
				}
			}

			@Override
			public boolean willChangeBounds() {
				return true;
			}
		};
		anim.setDuration(200);
		searchLayout.startAnimation(anim);
	}

	public void locatedClicked(View v) {
		LocationHelper.getCurrentLocation(this, this);
		progress = ProgressDialog.show(this, null, "Obtaining Location...");
	}

	public void scanClicked(View v) {
		IntentIntegrator integrator = new IntentIntegrator(this);
		integrator.setMessage("This feature requires Barcode Scanner to be installed. Would you like to install it?");
		integrator.initiateScan(IntentIntegrator.QR_CODE_TYPES); 
	}

	public void goClicked(View v) {
		String query = ((EditText) findViewById(R.id.search)).getText().toString();
		new RouteTask().newInstance(this, query).execute();
		progress = ProgressDialog.show(this, null, "Searching...");
	}

	@Override
	public void onTaskFinished(String response) {
		if (progress != null) progress.cancel();
		if (response == null) {
			Toast.makeText(this, "An error has occured.", Toast.LENGTH_LONG).show();
			onResume();
			return;
		} else if (response == LocationHelper.LOCATION_FAILED) {
			Toast.makeText(this, "Unable to determine your location.", Toast.LENGTH_LONG).show();
			onResume();
			return;
		}
		Document doc = Jsoup.parse(response);
		if (doc.title().contains("Route")) {
			RouteActivity.start(FavoritesActivity.this, response);
		} else if (doc.title().contains("Stop")) {
			System.out.println(doc.title());
		} else if (response.contains("Did you mean?") && (response.contains("class=\"routeList\"") || response.contains("class=\"ambiguousLocations\""))) {
			new DidYouMeanDialog().newInstance(this, response).show(getSupportFragmentManager(), "com.steelhawks.hawkscout.DID_YOU_MEAN_DIALOG");
		} else if(response.contains("<h3>Nearby Stops:</h3>")) {
			NearbyStopsActivity.start(this, response);
		} else {
			new NoResultsDialog().show(getSupportFragmentManager(), "com.steelhawks.hawkscout.NO_RESULTS");
			onResume();
		}
	}

	private void getHTML(String routeNumber) {
		new RouteTask().newInstance(new FavoriteTaskListener(routeNumber), routeNumber)
		.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private void createFavoriteData() {
		ViewFlipper flipper = (ViewFlipper) findViewById(R.id.favorites_flipper);
		if (flipper.getDisplayedChild() == 0) flipper.showNext();
		else {
			refreshItem.setVisible(true);
			setProgressBarIndeterminateVisibility(false);
		}
		
		Calendar c = Calendar.getInstance();
		String refreshString = "Refreshed at " + c.get(Calendar.HOUR) + ":" + 
				(c.get(Calendar.MINUTE)<10 ? "0" + c.get(Calendar.MINUTE) : c.get(Calendar.MINUTE)) + 
				" " + (c.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM");
		getActionBar().setSubtitle(refreshString);

		if (list == null) list = (ListView) findViewById(R.id.favorite_list);
		List<FavoriteViewController> favoriteData = new ArrayList<FavoriteViewController>();
		BusStopViewAdapter adapter = new BusStopViewAdapter(this, R.layout.activity_favorites, favoriteData, list);
		for (String s : favoriteSet) {
			String routeNumber = s.split(Preferences.STOP_SPLIT)[0].trim();
			Route r = getCorrespondingRoute(routeNumber);
			if (r != null) favoriteData.add(new FavoriteViewController(this, r, s));
		}
		list.setAdapter(adapter);
	}

	private int PX (int dp) {
		final float scale = getResources().getDisplayMetrics().density;
		int px = (int) (dp*scale+0.5f);
		return px;
	}

	private int DP (int px) {
		final float scale = getResources().getDisplayMetrics().density;
		int dp = (int) ((int) (px-0.5f)/scale);
		return dp;
	}

	private Route getCorrespondingRoute(String routeNumber) {
		for (int x=0; x<routes.size(); x++)
			if (routes.get(x).getRouteName().trim().equals(routeNumber)) {
				return routes.get(x);
			}

		System.out.println("Could not find corresponding route for " + routeNumber);
		return null;
	}

	public static void start(Activity a) {
		Intent i = new Intent(a.getBaseContext(), FavoritesActivity.class);
		a.startActivity(i);
	}

	private void addRoute(Route r) {
		routes.add(r);
		System.out.println(r.getRouteName());
		if (routes.size() == routeCount) createFavoriteData();
	}

	private class FavoriteTaskListener implements TaskListener {

		String routeNumber;

		public FavoriteTaskListener (String routeNumber) {
			this.routeNumber = routeNumber;
		}

		@Override
		public void onTaskFinished(String response) {
			Document doc = Jsoup.parse(response);
			Elements directions = doc.getElementsByClass("direction-link");
			Elements stopsOnRoute = doc.getElementsByClass("stopsOnRoute");
			Direction[] directionsArray = new Direction[directions.size()];
			for (int x=0; x<directions.size(); x++)
				directionsArray[x] = new Direction(directions.get(x).text(), stopsOnRoute.get(x));
			addRoute(new Route(routeNumber, directionsArray));
		}

	}

}
