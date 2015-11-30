package com.khemlani.bustime.route;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;

import android.os.AsyncTask;

import com.khemlani.bustime.TaskListener;

public class RouteTask extends AsyncTask<Void, Void, Void> {

	private String resString;
	private boolean isRunning = true;
	private TaskListener listener;
	private String routeNumber;
	boolean retrying = false;
	
	public RouteTask newInstance(TaskListener listener, String routeNumber) {
		this.listener = listener;
		this.routeNumber = routeNumber;
		return this;
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		System.out.println("Calling routefragment");
		try {
			System.out.println("http://bustime.mta.info/m/index?q=" + routeNumber.toLowerCase(Locale.US));
			resString = routeNumber.indexOf("/s/") == 0 ?
					Jsoup.connect("http://bustime.mta.info" + routeNumber.toLowerCase(Locale.US)).get().toString() :
					Jsoup.connect("http://bustime.mta.info/m/index?q=" + routeNumber.toLowerCase(Locale.US)).get().toString();
			resString = resString.replaceAll("&nbsp;", " ");
		} catch (SocketTimeoutException e) {
			retrying = true;
			e.printStackTrace();
			tryAgain();
//			new RouteTask().newInstance(listener, routeNumber).execute();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	protected void onPostExecute(Void arg) {
		if (!retrying) listener.onTaskFinished(resString);
	}
	
	private void tryAgain() {
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet("http://bustime.mta.info/m/index?q=" + routeNumber.toLowerCase(Locale.US));
		try {
			HttpResponse response = httpClient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			InputStream is = entity.getContent(); // Create an InputStream with the response
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) // Read line by line
				sb.append(line + "\n");

			resString = sb.toString(); // Result is here

			is.close(); // Close the stream
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}


