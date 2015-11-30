package com.khemlani.bustime.data;

import java.io.Serializable;

public class Route implements Serializable {

	private Direction[] directionsArray;
	private String routeName;
	
	public Route(String routeName, Direction[] directionsArray) {
		this.routeName = routeName;
		this.directionsArray = directionsArray;
	}
	
	public Direction[] getDirections() {
		return directionsArray;
	}
	
	public String getRouteName() {
		return routeName;
	}
	
}
