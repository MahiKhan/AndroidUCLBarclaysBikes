package com.uclteam6.uclbarclayscycles;

public class Station {

	String id, name, terminalName, lat, lng, installed, locked, installDate,
			temporary, nbBikes, nbEmptyDocks, nbDocks;
	double distance;
	int score;

	public Station(String id, String name, String terminalName, String lat,
			String lng, String installed, String locked, String installDate,
			String temporary, String nbBikes, String nbEmptyDocks,
			String nbDocks, double distance, int score) {
		this.id = id;
		this.name = name;
		this.terminalName = terminalName;
		this.lat = lat;
		this.lng = lng;
		this.installed = installed;
		this.locked = locked;
		this.installDate = installDate;
		this.temporary = temporary;
		this.nbBikes = nbBikes;
		this.nbEmptyDocks = nbEmptyDocks;
		this.nbDocks = nbDocks;
		this.distance = distance;
		this.score = score;
	}

}
