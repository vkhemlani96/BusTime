package com.khemlani.bustime;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import com.khemlani.bustime.data.Direction;
import com.khemlani.bustime.data.Route;
import com.khemlani.bustime.stopcode.StopCodeTask;
import com.khemlani.bustime.stopcode.StopCodeViewController;
import com.steelhawks.hawkscout.favorites.BusStopViewAdapter;
import com.steelhawks.hawkscout.favorites.FavoriteViewController;
import com.steelhawks.hawkscout.favorites.Preferences;

public class StopCodeActivity extends Activity implements TaskListener {
	
	private static final String STOP_CODE_KEY = "com.khemlani.bustime.FOUND_ROUTES_KEY";
	
	private List<String> expectedPreferenceStrings = new ArrayList<String>();
	private List<Route> routes = new ArrayList<Route>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		System.out.println(routes.size());
		setContentView(R.layout.activity_stop_code);
		setTitle("Stop Information");
		String stopCode = getIntent().getExtras().getString(STOP_CODE_KEY);
		if (stopCode != null) new StopCodeTask().newInstance(this, new ExpectedRoutesListener(), stopCode).execute();
	}
	
	public static void start(Activity a, String stopCode) {
		Intent i = new Intent(a.getBaseContext(), StopCodeActivity.class);
		i.putExtra(STOP_CODE_KEY, stopCode);
		a.startActivity(i);
	}
	
	private void createViews() {
		ListView list = (ListView) findViewById(R.id.stop_code_list);
		List<FavoriteViewController> favoriteData = new ArrayList<FavoriteViewController>();
		BusStopViewAdapter adapter = new BusStopViewAdapter(this, R.layout.activity_favorites, favoriteData, list);
		for (Route route : routes) {
			for (String s : expectedPreferenceStrings) {
				if (route.getRouteName().trim().equals(s.split(Preferences.STOP_SPLIT)[0]))
					favoriteData.add(new StopCodeViewController(this, route, s));
			}
		}
		list.setAdapter(adapter);
	}
	
	private void addRoute(Route r) {
		routes.add(r);
		if (routes.size() == expectedPreferenceStrings.size()) createViews();
	}

	@Override
	public void onTaskFinished(String response) {
		Document doc = Jsoup.parse(response);
		String routeNumber = doc.title().replaceAll(".+(?=(( \\w+)$))", "").trim();
		System.out.println(routeNumber);
		Elements directions = doc.getElementsByClass("direction-link");
		Elements stopsOnRoute = doc.getElementsByClass("stopsOnRoute");
		Direction[] directionsArray = new Direction[directions.size()];
		for (int x=0; x<directions.size(); x++)
			directionsArray[x] = new Direction(directions.get(x).text(), stopsOnRoute.get(x));
		addRoute(new Route(routeNumber, directionsArray));
	}
	
	public class ExpectedRoutesListener implements TaskListener {

		@Override
		public void onTaskFinished(String response) {
			if (expectedPreferenceStrings.size() == 0) 
				getActionBar().setSubtitle(capWords(response.split(Preferences.STOP_SPLIT)[2]));
			expectedPreferenceStrings.add(response);
			System.out.println(expectedPreferenceStrings.size());
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
}
