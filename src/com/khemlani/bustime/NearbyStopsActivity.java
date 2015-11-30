package com.khemlani.bustime;

import java.util.Locale;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.khemlani.bustime.route.RouteTask;

public class NearbyStopsActivity extends Activity implements TaskListener {

	private static final String HTML_RESPONSE_KEY = "com.khemlani.bustime.NearbyStopsActivity.HTML_RESPONSE_KEY";

	String requestedDirection;
	String requestedStop;
	MapView mapView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nearby_stops);
		String searchQuery = getIntent().getStringExtra(HTML_RESPONSE_KEY).toUpperCase(Locale.US);
		createViews(searchQuery);
		setupActionBar();
		
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);
        
        MapsInitializer.initialize(this);
		GoogleMap map = mapView.getMap();
		map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		map.setMyLocationEnabled(true);
		Location loc = getCurrentLocation();
		LatLng currentLocation = new LatLng(loc.getLatitude(), loc.getLongitude());
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 13));
		map.addMarker(new MarkerOptions()
		.title("Sydney")
		.snippet("The most populous city in Australia.")
		.position(currentLocation));
	}
	
	@Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
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

	private void setupActionBar() {
		final ActionBar actionBar = getActionBar();
		actionBar.setLogo(R.drawable.ic_logo);
		setTitle("Nearby Routes");
		//		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM|ActionBar.DISPLAY_SHOW_HOME);
		//		actionBar.setCustomView(R.layout.action_bar_custom_text);
		//		TextView actionBarView = (TextView) actionBar.getCustomView();
		//		actionBarView.setText("Nearby Routes");
	}

	private void createViews(String searchQuery) {
		Document doc = Jsoup.parse(searchQuery);
		LinearLayout container = (LinearLayout) findViewById(R.id.container);
		LayoutParams textViewParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		LayoutParams sepParams = new LayoutParams(LayoutParams.MATCH_PARENT, PX(1));

		Elements stops = doc.getElementsByClass("stop");
		for (Element stop : stops) {
			Elements directions = stop.getElementsByClass("directionAtStop");
			if (directions.size() == 0) continue;
			Element stopHeader = stop.getElementsByClass("stopHeader").size() > 0 ? stop.getElementsByClass("stopHeader").get(0) : null;
			if (stopHeader == null) continue;

			TextView stopName = getStopView();
			stopName.setText(capWords(stopHeader.text()));
			container.addView(stopName, textViewParams);
			container.addView(getBlueSeperator(), sepParams);

			for (Element direction : directions) {
				Element directionInfo = direction.getElementsByTag("a").size() > 0 ? direction.getElementsByTag("a").get(0) : null;
				if (directionInfo == null) continue;
				String directionInfoText = directionInfo.text();
				container.addView(getDirectionText(stopHeader.text(), directionInfoText), textViewParams);
			}
		}

	}

	@SuppressLint("NewApi")
	private View getDirectionText(final String directionName, String directionText) {
		final String[] directionTextArray = directionText.split(" ", 2);
		ViewGroup parent = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.two_line_text_item_horizontal, null, false);
		parent.setBackgroundResource(R.drawable.content_background_blue);
		parent.setClickable(true);
		parent.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				requestedDirection = directionTextArray[1];
				requestedStop = directionName;
				new RouteTask().newInstance(NearbyStopsActivity.this, directionTextArray[0]).execute();
			}
		});
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) parent.setPaddingRelative(0, 0, 0, 0);
		TextView text1 = (TextView) parent.findViewById(android.R.id.text1);
		text1.setText(directionTextArray[0]);
		TextView text2 = (TextView) parent.findViewById(android.R.id.text2);
		text2.setText("TO " + directionTextArray[1]);
		return parent;
	}

	private View getBlueSeperator() {
		View sep = new View(this);
		sep.setBackgroundColor(getResources().getColor(R.color.dark_blue));
		return sep;
	}
	private View getThinSeperator() {
		TextView sep = new TextView(this);
		sep.setTextAppearance(this, R.style.ThinSeperator);
		return sep;
	}

	private TextView getStopView() {
		TextView stopView = new TextView(this);
		stopView.setTextAppearance(this, R.style.BlueSectionTitle);
		stopView.setPadding(PX(16), 0, PX(16), 0);
		return stopView;
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

	private int PX (int dp) {
		final float scale = getResources().getDisplayMetrics().density;
		int px = (int) (dp*scale+0.5f);
		return px;
	}

	public static void start(Activity a, String responseString) {
		Intent i = new Intent(a.getBaseContext(), NearbyStopsActivity.class);
		i.putExtra(HTML_RESPONSE_KEY, responseString);
		a.startActivity(i);
	}

	@Override
	public void onTaskFinished(String response) {
		RouteActivity.start(this, response, requestedDirection, requestedStop);
	}


}
