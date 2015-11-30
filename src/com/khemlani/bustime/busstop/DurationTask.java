package com.khemlani.bustime.busstop;

import java.io.IOException;
import java.net.SocketTimeoutException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import android.os.AsyncTask;

import com.khemlani.bustime.TaskListener;

public class DurationTask extends AsyncTask<String, String, String> {

	TaskListener listener;
	private static final String locationSuffix = "_New_York_NY";
	
	public DurationTask newInstance(TaskListener listener) {
		this.listener = listener;
		return this;
	}
	
	@Override
	protected String doInBackground(String... params) {
		String origin = rewriteLocation(params[0]);
		String destination = rewriteLocation(params[1]);
		System.out.println(params.length);
		try {
			Document d;
			if (params.length == 3) d = Jsoup.connect("http://maps.googleapis.com/maps/api/directions/xml?origin=" + origin + 
					"&destination=" + destination + "&waypoints=" + rewriteLocation(params[2]) + "&sensor=false").get();
			else d = Jsoup.connect("http://maps.googleapis.com/maps/api/directions/xml?origin=" + origin + 
						"&destination=" + destination + "&sensor=false").get();
			Elements legs = d.getElementsByTag("leg");
			Elements duration = legs.get(0).getElementsByTag("duration");
			return duration.get(duration.size()-1).getElementsByTag("text").text();
		} catch(SocketTimeoutException e) {
			return doInBackground(params);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private String rewriteLocation(String s) {
		return s.replaceAll("\\W", "_") + locationSuffix;
	}
	
	@Override
	protected void onPostExecute(String arg0) {
		listener.onTaskFinished(arg0);
	}

}
