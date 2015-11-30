package com.khemlani.bustime.stopcode;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.os.AsyncTask;

import com.khemlani.bustime.StopCodeActivity.ExpectedRoutesListener;
import com.khemlani.bustime.TaskListener;
import com.khemlani.bustime.route.RouteTask;
import com.steelhawks.hawkscout.favorites.Preferences;

public class StopCodeTask extends AsyncTask<Void, Void, Void> {

	private Document doc;
	private TaskListener listener;
	private ExpectedRoutesListener expectedRouteListener;
	private String stopCode;
	private Element stop;
	private Elements directions;
	private String stopHeader;
	boolean retrying = false;
	
	public StopCodeTask newInstance(TaskListener listener, ExpectedRoutesListener expectedRouteListener, String stopCode) {
		this.listener = listener;
		this.stopCode = stopCode;
		this.expectedRouteListener = expectedRouteListener;
		return this;
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		try {
			doc = Jsoup.connect("http://bustime.mta.info/s/" + stopCode.toLowerCase(Locale.US)).get();
			stop = doc.getElementsByClass("stop").get(0);
			if (stop == null) return null;
			stopHeader = stop.getElementsByClass("stopHeader").size() > 0 ? stop.getElementsByClass("stopHeader").get(0).text() : null;
		} catch (SocketTimeoutException e) {
			retrying = true;
			new StopCodeTask().newInstance(listener, expectedRouteListener, stopCode).execute();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		directions = stop.getElementsByClass("directionAtStop");
		
		for (Element direction : directions) {
			String routeTitle = direction.getElementsByTag("a").size() > 0 ? direction.getElementsByTag("a").get(0).text() : null;
			Matcher m = Pattern.compile("^([A-Z])+([0-9]){1,3}").matcher(routeTitle);
			if (m.find()) {
				String preferenceString = m.group() + Preferences.STOP_SPLIT 
						+ "TO " + routeTitle.replaceAll(m.group(), "").replaceAll("^\\W*", "").trim().toUpperCase(Locale.US)
						+ Preferences.STOP_SPLIT + stopHeader;
				expectedRouteListener.onTaskFinished(preferenceString);
			}
		}
		
		for (Element direction : directions) {
			String routeTitle = direction.getElementsByTag("a").size() > 0 ? direction.getElementsByTag("a").get(0).text() : null;
			Matcher m = Pattern.compile("^([A-Z])+([0-9]){1,3}").matcher(routeTitle);
			if (m.find()) {
				new RouteTask().newInstance(listener, m.group()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			}
		}
	}
	
	

}
