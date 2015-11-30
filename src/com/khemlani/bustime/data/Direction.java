package com.khemlani.bustime.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

@SuppressWarnings("serial")
public class Direction implements Serializable {

	private String name;
	private List<BusStop> stopsOnRoute = new ArrayList<BusStop>();
	
	public Direction (String name, Element stopsHTML) {
		this.name = name;
		Elements stops = stopsHTML.getElementsByTag("li");
		for (int x = 0; x<stops.size(); x++) {
			Element currentStop = stops.get(x);
			boolean isBusAtStop = currentStop.child(0).tagName().toUpperCase(Locale.US).equals("STRONG");
			if (isBusAtStop && currentStop.child(0).children().size() == 0) continue;
			List<String> busLocation = new ArrayList<String>();
			String stopText = currentStop.text();
			if (stopText.contains("approaching")) {
				int occurances = (stopText.length() - stopText.replaceAll("approaching", "").length())/("approaching").length();
				for (int y=0; y<occurances; y++) busLocation.add("Approaching Stop");
			} if (stopText.contains("at stop")) {
				int occurances = (stopText.length() - stopText.replaceAll("at stop", "").length())/("at stop").length();
				for (int y=0; y<occurances; y++) busLocation.add("At Stop");
			} if (stopText.contains("1 stop away")) {
				int occurances = (stopText.length() - stopText.replaceAll("1 stop away", "").length())/("1 stop away").length();
				for (int y=0; y<occurances; y++) busLocation.add("< 1 Stop Away");
			}
			String stopName = isBusAtStop ? currentStop.child(0).child(0).text() : currentStop.child(0).text();
			stopsOnRoute.add(new BusStop(stopName, isBusAtStop, busLocation));
		}
	}
	
	public String getTitle() {
		return name;
	}
	
	public List<BusStop> getStops() {
		return stopsOnRoute;
	}
	
	public class BusStop implements Serializable {
		
		String name;
		boolean isBusAtStop;
		List<String> location;
		
		public BusStop(String name, boolean isBusAtStop, List<String> location) {
			this.name = name;
			this.isBusAtStop = isBusAtStop;
			this.location = location;
		}
		
		public String getName() {
			return name;
		}
		
		public boolean isBusAtStop() {
			return isBusAtStop;
		}
		
		public List<String> getBusLocation() {
			return location;
		}
		
	}
	
}
