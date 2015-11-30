package com.khemlani.bustime;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.internal.fi;
import com.khemlani.bustime.dialogs.DidYouMeanDialog;
import com.khemlani.bustime.dialogs.NoResultsDialog;
import com.khemlani.bustime.route.RouteTask;
import com.steelhawks.hawkscout.favorites.Preferences;

public class HomeActivity extends FragmentActivity implements TaskListener {

	private static final String SHOW_CONTENT_KEY = "com.khemlani.bustime.SHOW_CONTENT_KEY";

	EditText searchBar;
	Button submitButton;
	Button locateButton;
	Button scanButton;
	ProgressDialog progress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (new Preferences(this).getFavoriteStops() != null && new Preferences(this).getFavoriteStops().size() > 0) {
			startActivity(new Intent(getBaseContext(), FavoritesActivity.class));
			finish();
		}
		getCurrentLocation();
		setContentView(R.layout.activity_home);
		setupLayout();
	}  

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home, menu);
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (searchBar != null) searchBar.setEnabled(true);
		if (submitButton != null) submitButton.setEnabled(true);
		if (locateButton != null) locateButton.setEnabled(true);
		if (scanButton != null) scanButton.setEnabled(true);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		String result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
		if (result != null && result.contains("http://bustime.mta.info/s/"))
			StopCodeActivity.start(this, result.replaceAll("http://bustime.mta.info/s/", "").trim());
	}

	public static void start(Activity a) {
		Intent i = new Intent(a.getBaseContext(), HomeActivity.class);
		i.putExtra(SHOW_CONTENT_KEY, false);
		a.startActivity(i);
	}

	private void setupLayout() {
		searchBar = (EditText) findViewById(R.id.search);
		submitButton = (Button) findViewById(R.id.go);
		locateButton = (Button) findViewById(R.id.locate);
		scanButton = (Button) findViewById(R.id.scan);
	}

	private void disableLayout() {
		searchBar.setEnabled(false);
		submitButton.setEnabled(false);
		locateButton.setEnabled(false);
		scanButton.setEnabled(false);
	}

	public void goClicked(View v) {
		getHTMLQuery(searchBar.getText().toString());
		disableLayout();
		progress = ProgressDialog.show(this, null, "Searching...");
	}

	public void locateClicked(View v) {
//		LocationHelper.getCurrentLocation(this, this);
//		disableLayout();
		Location currentLocation = getCurrentLocation();
		new RouteTask().newInstance(this, "&l=" + currentLocation.getLatitude() + "%2C" + currentLocation.getLongitude() + "&t=stops").execute();
		progress = ProgressDialog.show(this, null, "Obtaining Location...");

	}

	public void scanClicked(View v) {
		IntentIntegrator integrator = new IntentIntegrator(this);
		integrator.setMessage("This feature requires Barcode Scanner to be installed. Would you like to install it?");
		integrator.initiateScan(IntentIntegrator.QR_CODE_TYPES); 
	}

	private Location getCurrentLocation() {
		LocationManager locationManager = (LocationManager) 
				getSystemService(LOCATION_SERVICE);
		float highestAccuracy = Float.MAX_VALUE;
		Location bestLocation = null;
		String bestProvider = "";

		for (String provider : locationManager.getAllProviders()) {
			Location location = locationManager.getLastKnownLocation(provider);
			if (location != null && location.getAccuracy() < highestAccuracy) {
				bestLocation = location;
				bestProvider = provider;
			}

		}
		System.out.println("Best Provider: " + bestProvider);
		return bestLocation;
	}

	private void getHTMLQuery(String query) {
		new RouteTask().newInstance(this, query).execute();
	}

	@Override
	public void onTaskFinished(String response) {
		progress.cancel();
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
			RouteActivity.start(HomeActivity.this, response);
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

}
