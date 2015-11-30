package com.steelhawks.hawkscout.favorites;

import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {

	private static final String PREFERENCES_IDENTIFIER = "com.khemlani.FAVORITE_STOPS";
	private static final String SET_IDENTIFIER = "com.khemlani.STOPS_SET";
	private static final String VIEWED_ROUTE_IDENTIFIER = "com.khemlani.VIEWED_ROUTE";
	public static final String STOP_SPLIT = "STOPSPLIT";

	SharedPreferences preferences;
	SharedPreferences.Editor editor;

	public Preferences(Context c) {
		if (c == null) System.out.println("C is null");
		this.preferences = c.getSharedPreferences(PREFERENCES_IDENTIFIER, Context.MODE_PRIVATE);
		this.editor = this.preferences.edit();
	}

	public void addFavoriteStop(String preferenceString) {
		Set<String> currentStops = this.preferences.getStringSet(SET_IDENTIFIER, null);
		if (currentStops == null) {
			currentStops = new HashSet<String>();
		}
		currentStops.add(preferenceString);
		editor.clear();
		editor.putStringSet(SET_IDENTIFIER, currentStops).apply();
	}
	
	public void removeFavoriteStop(String preferenceString) {
		Set<String> currentStops = this.preferences.getStringSet(SET_IDENTIFIER, null);
		currentStops.remove(preferenceString);
		editor.clear();
		editor.putStringSet(SET_IDENTIFIER, currentStops).apply();
	}

	public Set<String> getFavoriteStops() {
		Set<String> currentStops = this.preferences.getStringSet(SET_IDENTIFIER, null);
		return currentStops;
	}
	
	public void setViewedRoute() {
		editor.clear();
		editor.putBoolean(VIEWED_ROUTE_IDENTIFIER, true);
		editor.putStringSet(SET_IDENTIFIER, getFavoriteStops()).apply();
	}
	
	public boolean getViewedRoute() {
		return this.preferences.getBoolean(VIEWED_ROUTE_IDENTIFIER, false);
	}

}
