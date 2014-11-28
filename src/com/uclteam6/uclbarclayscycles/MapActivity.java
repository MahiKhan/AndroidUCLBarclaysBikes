package com.uclteam6.uclbarclayscycles;

import java.util.List;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.uclteam6.uclbarclayscycles.route.GoogleParser;
import com.uclteam6.uclbarclayscycles.route.Parser;
import com.uclteam6.uclbarclayscycles.route.Route;

public class MapActivity extends FragmentActivity {
	GoogleMap mMap;
	Marker marker;
	LatLng position, startposition;

	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		setContentView(R.layout.activity_map);
		mMap = ((SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map)).getMap();

		double lat = Double.parseDouble(getIntent().getStringExtra("lat"));
		double lng = Double.parseDouble(getIntent().getStringExtra("lng"));
		double targetLat = getIntent().getDoubleExtra("targetLat", 0);
		double targetLng = getIntent().getDoubleExtra("targetLng", 0);
		double startLat = getIntent().getDoubleExtra("startLat", 0);
		double startLng = getIntent().getDoubleExtra("startLng", 0);
		String name = getIntent().getStringExtra("name");
		String nbBikes = getIntent().getStringExtra("nbBikes");
		String nbEmptyDocks = getIntent().getStringExtra("nbEmptyDocks");
		String distance = getIntent().getStringExtra("distance");

		position = new LatLng(lat, lng);
		startposition = new LatLng(startLat, startLng);
		// animate camera to location
		marker = mMap.addMarker(new MarkerOptions().position(position).icon(
				BitmapDescriptorFactory.fromResource(R.drawable.marker)));
		marker.setTitle(name);
		marker.setSnippet(nbBikes + " bikes currently available\n"
				+ nbEmptyDocks + " Empty docks. " + " Distance : " + distance);
		mMap.addMarker(new MarkerOptions().position(
				new LatLng(targetLat, targetLng)).icon(
				BitmapDescriptorFactory.fromResource(R.drawable.star)));
		mMap.addMarker(new MarkerOptions().position(startposition).icon(
				BitmapDescriptorFactory.fromResource(R.drawable.marker_start)));
		mMap.moveCamera(CameraUpdateFactory.newLatLng(position));
		mMap.animateCamera(CameraUpdateFactory.zoomTo(14));

		new getRoute().execute();

	}

	/**
	 * This is New CODE
	 */

	private class getRoute extends AsyncTask<Void, Void, Void> {

		private Route route;

		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			route = directions(startposition, position);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			try {
				List<LatLng> list = route.getPoints();
				PolylineOptions plo = new PolylineOptions();
				plo.color(Color.rgb(58, 140, 194));
				for (int k = 0; k < list.size(); k++)
					plo.add(list.get(k));
				plo.add(position);
				mMap.addPolyline(plo);
			} catch (Exception e) {
				Toast.makeText(getApplicationContext(),
						"Could not draw Route !", Toast.LENGTH_LONG).show();
			}

		}
	}

	/**
	 * @param start
	 * @param dest
	 * @return
	 */
	private Route directions(final LatLng start, final LatLng dest) {
		Parser parser;
		String jsonURL = "http://maps.googleapis.com/maps/api/directions/json?";
		final StringBuffer sBuf = new StringBuffer(jsonURL);
		sBuf.append("origin=");
		sBuf.append(start.latitude);
		sBuf.append(',');
		sBuf.append(start.longitude);
		sBuf.append("&destination=");
		sBuf.append(dest.latitude);
		sBuf.append(',');
		sBuf.append(dest.longitude);
		sBuf.append("&sensor=true&mode=bicycling");
		parser = new GoogleParser(sBuf.toString());
		Route r = parser.parse();
		return r;
	}

	/**
	 * End of new CODE
	 */
}
