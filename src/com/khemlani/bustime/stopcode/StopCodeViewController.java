package com.khemlani.bustime.stopcode;

import org.jaxen.Context;

import com.khemlani.bustime.data.Route;
import com.khemlani.bustime.favorites.FavoriteViewController;

public class StopCodeViewController extends FavoriteViewController {

	public StopCodeViewController(Context c, Route r, String preferenceString) {
		super(c, r, preferenceString);
		this.showAnimation = false;
	}

}
