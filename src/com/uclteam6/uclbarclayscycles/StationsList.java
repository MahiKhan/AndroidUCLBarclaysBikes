package com.uclteam6.uclbarclayscycles;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

public class StationsList extends Activity implements OnItemClickListener {

	ProgressBar progress;
	public static String URL = "http://www.tfl.gov.uk/tfl/syndication/feeds/cycle-hire/livecyclehireupdates.xml";
	ArrayList<HashMap<String, String>> dataList;
	StationAdapter adapter;
	ListView list;
	String reference;
	LatLng targetPosition, startPosition;
	boolean searchByRef, alreadySearched = false;
	String placeName, startPlaceName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.stations_list);
		progress = (ProgressBar) findViewById(R.id.progress);
		list = (ListView) findViewById(R.id.list);
		reference = getIntent().getStringExtra("reference");
		searchByRef = getIntent().getBooleanExtra("searchByRef", false);
		if (!searchByRef) {
			placeName = getIntent().getStringExtra("placeName");
			// new Code Start
			startPlaceName = getIntent().getStringExtra("startPlaceName");
			// new Code end
		}
		new GetPlaceLocation().execute(new String[] { placeName });

		list.setOnItemClickListener(this);
	}

	public class GetPlaceLocation extends AsyncTask<String, Void, LatLng> {

		private static final String LOG_TAG = "Bicycles App";

		private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place/details";
		private static final String MAP_API_BASE = "https://maps.googleapis.com/maps/api/geocode/json?address=";

		private static final String OUT_JSON = "/json?&sensor=true&";

		private static final String API_KEY = "AIzaSyAN3Zf0_TzOujpAhuHxAYtY_-0IQgY7JE8";

		@Override
		protected LatLng doInBackground(String... params) {
			// TODO Auto-generated method stub
			return getLocation(params[0]);
		}

		@Override
		protected void onPostExecute(LatLng result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			// Modified Code start
			Log.i("lat : ", "" + result.latitude);
			Log.i("lat : ", "" + result.longitude);
			if (result.latitude == 0 && result.longitude == 0) {
				Toast.makeText(getApplicationContext(), "No Place Found ! ",
						Toast.LENGTH_LONG).show();
				progress.setVisibility(View.GONE);
			} else {
				if (!alreadySearched) {
					targetPosition = result;
					new GetXmlAndParse().execute(new Double[] {
							result.latitude, result.longitude });
					alreadySearched = true;
				} else {
					startPosition = result;
					progress.setVisibility(View.GONE);
					adapter = new StationAdapter(StationsList.this, dataList);
					list.setAdapter(adapter);
				}
			}
			// Modified Code End
		}

		private LatLng getLocation(String placeName) {

			LatLng position = new LatLng(0, 0);
			HttpURLConnection conn = null;
			StringBuilder jsonResults = new StringBuilder();
			try {
				StringBuilder sb;
				if (searchByRef) {
					sb = new StringBuilder(PLACES_API_BASE + OUT_JSON);
					sb.append("key=" + API_KEY);
					sb.append("&reference=" + reference);
					Log.i("POST", sb.toString());
				} else {
					placeName = placeName.replace(" ", "");
					sb = new StringBuilder(MAP_API_BASE + placeName);
					Log.i("POST", sb.toString());
				}
				URL url = new URL(sb.toString());
				conn = (HttpURLConnection) url.openConnection();
				InputStreamReader in = new InputStreamReader(
						conn.getInputStream());

				// Load the results into a StringBuilder
				int read;
				char[] buff = new char[1024];
				while ((read = in.read(buff)) != -1) {
					jsonResults.append(buff, 0, read);
				}
			} catch (MalformedURLException e) {
				Log.e(LOG_TAG, "Error processing Places API URL", e);
				return position;
			} catch (IOException e) {
				Log.e(LOG_TAG, "Error connecting to Places API", e);
				return position;
			} finally {
				if (conn != null) {
					conn.disconnect();
				}
			}

			try {
				// Create a JSON object hierarchy from the results
				JSONObject jsonObj = new JSONObject(jsonResults.toString());
				JSONObject jsonresult;
				if (searchByRef)
					jsonresult = jsonObj.getJSONObject("result");
				else {
					JSONArray jsonresults = jsonObj.getJSONArray("results");
					jsonresult = jsonresults.getJSONObject(0);
				}
				JSONObject jsonGeo = jsonresult.getJSONObject("geometry");
				JSONObject jsonloc = jsonGeo.getJSONObject("location");
				position = new LatLng(jsonloc.getDouble("lat"),
						jsonloc.getDouble("lng"));

			} catch (JSONException e) {
				Log.e(LOG_TAG, "Cannot process JSON results", e);
			}

			return position;
		}

	}

	public class GetXmlAndParse extends AsyncTask<Double, Void, Void> {

		@Override
		protected Void doInBackground(Double... params) {
			// TODO Auto-generated method stub
			XMLParser parser = new XMLParser();
			String xml = parser.getXmlFromUrl(URL);
			Document doc = parser.getDomElement(xml);
			Log.i("XML", xml);
			Node n = doc.getFirstChild();
			NodeList nl = n.getChildNodes();
			Log.i("Count : ", "" + nl.getLength());
			ArrayList<Station> stationList = new ArrayList<Station>();
			dataList = new ArrayList<HashMap<String, String>>();
			for (int i = 0; i < nl.getLength(); i++) {
				try {
					String id = ((((Element) nl.item(i))
							.getElementsByTagName("id")).item(0))
							.getFirstChild().getNodeValue();
					String name = ((((Element) nl.item(i))
							.getElementsByTagName("name")).item(0))
							.getFirstChild().getNodeValue();
					String terminalName = ((((Element) nl.item(i))
							.getElementsByTagName("terminalName")).item(0))
							.getFirstChild().getNodeValue();
					String lat = ((((Element) nl.item(i))
							.getElementsByTagName("lat")).item(0))
							.getFirstChild().getNodeValue();
					String lng = ((((Element) nl.item(i))
							.getElementsByTagName("long")).item(0))
							.getFirstChild().getNodeValue();
					String installed = ((((Element) nl.item(i))
							.getElementsByTagName("installed")).item(0))
							.getFirstChild().getNodeValue();
					String locked = ((((Element) nl.item(i))
							.getElementsByTagName("locked")).item(0))
							.getFirstChild().getNodeValue();
					String installDate = ((((Element) nl.item(i))
							.getElementsByTagName("installDate")).item(0))
							.getFirstChild().getNodeValue();
					String temporary = ((((Element) nl.item(i))
							.getElementsByTagName("temporary")).item(0))
							.getFirstChild().getNodeValue();
					String nbBikes = ((((Element) nl.item(i))
							.getElementsByTagName("nbBikes")).item(0))
							.getFirstChild().getNodeValue();
					String nbEmptyDocks = ((((Element) nl.item(i))
							.getElementsByTagName("nbEmptyDocks")).item(0))
							.getFirstChild().getNodeValue();
					String nbDocks = ((((Element) nl.item(i))
							.getElementsByTagName("nbDocks")).item(0))
							.getFirstChild().getNodeValue();

					double distance = distance(
							new LatLng(params[0], params[1]),
							new LatLng(Double.parseDouble(lat), Double
									.parseDouble(lng)));
					Station station = new Station(id, name, terminalName, lat,
							lng, installed, locked, installDate, temporary,
							nbBikes, nbEmptyDocks, nbDocks, distance, 0);
					stationList.add(station);
				} catch (Exception e) {

				}
			}
			Collections.sort(stationList, new Comparator<Station>() {
				@Override
				public int compare(Station station1, Station station2) {
					if (station1.distance <= station2.distance)
						return -1;
					return 1;
				}
			});

			// New Code Start
			ArrayList<Station> subStationList = new ArrayList<Station>(
					stationList.subList(0, 10));
			ArrayList<Station> orderedStationList = new ArrayList<Station>(10);
			for (int j = 0; j < subStationList.size(); j++) {
				Station station = subStationList.get(j);
				station.score += (j + 1);
				Log.i("Score Normal : ", "" + station.score);
				Log.i("nbBikes : ", "" + station.nbBikes);
				Log.i("nbEmptyDocks : ", "" + station.nbEmptyDocks);
				if (Integer.parseInt(station.nbBikes) < 10
						&& Integer.parseInt(station.nbEmptyDocks) < 10)
					station.score += 2;
				Log.i("New Score : ", "" + station.score);
				orderedStationList.add(station);
			}

			Collections.sort(orderedStationList, new Comparator<Station>() {
				@Override
				public int compare(Station station1, Station station2) {
					if (station1.score <= station2.score)
						return -1;
					return 1;
				}
			});

			// New Code End

			// Modified Code Start
			for (int j = 0; j < orderedStationList.size(); j++) {
				HashMap<String, String> map = new HashMap<String, String>();
				Log.i("Score : " + j, "" + orderedStationList.get(j).score);
				map.put("id", orderedStationList.get(j).id);
				map.put("name", orderedStationList.get(j).name);
				map.put("nbBikes", orderedStationList.get(j).nbBikes);
				map.put("nbEmptyDocks", orderedStationList.get(j).nbEmptyDocks);
				map.put("lat", orderedStationList.get(j).lat);
				map.put("lng", orderedStationList.get(j).lng);
				map.put("distance", ""
						+ (int) orderedStationList.get(j).distance);
				dataList.add(map);
			}
			// Modified Code end
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			new GetPlaceLocation().execute(new String[] { startPlaceName });
		}

	}

	public float distance(LatLng StartP, LatLng EndP) {
		double earthRadius = 3958.75;
		double latDiff = Math.toRadians(EndP.latitude - StartP.latitude);
		double lngDiff = Math.toRadians(EndP.longitude - StartP.longitude);
		double a = Math.sin(latDiff / 2) * Math.sin(latDiff / 2)
				+ Math.cos(Math.toRadians(StartP.latitude))
				* Math.cos(Math.toRadians(EndP.latitude))
				* Math.sin(lngDiff / 2) * Math.sin(lngDiff / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double distance = earthRadius * c;

		int meterConversion = 1609;

		return new Float(distance * meterConversion).floatValue();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		Intent intent = new Intent(this, MapActivity.class);
		intent.putExtra("lat", dataList.get(position).get("lat"));
		intent.putExtra("lng", dataList.get(position).get("lng"));
		intent.putExtra("name", dataList.get(position).get("name"));
		intent.putExtra("distance", dataList.get(position).get("distance"));
		intent.putExtra("nbEmptyDocks",
				dataList.get(position).get("nbEmptyDocks"));
		intent.putExtra("nbBikes", dataList.get(position).get("nbBikes"));
		intent.putExtra("targetLat", targetPosition.latitude);
		intent.putExtra("targetLng", targetPosition.longitude);
		// New Code start
		intent.putExtra("startLat", startPosition.latitude);
		intent.putExtra("startLng", startPosition.longitude);
		// new Code End
		startActivity(intent);
	}

}
