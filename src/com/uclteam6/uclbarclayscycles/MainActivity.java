package com.uclteam6.uclbarclayscycles;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.google.android.gms.maps.model.LatLng;

public class MainActivity extends Activity implements OnItemClickListener {

	Button search;
	LocationManager locationManager;
	LatLng latLng;
	ViewFlipper viewFlipper;
	AutoCompleteTextView autoCompView, autoCompViewStart;
	Animation animFlipIn, animFlipOut;
	String reference;

	ArrayList<HashMap<String, String>> resultList;
	static Activity activity;
	static LayoutInflater inflater = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
		autoCompView = (AutoCompleteTextView) findViewById(R.id.autoCompView);
		search = (Button) findViewById(R.id.search);

		// New Code Start
		autoCompViewStart = (AutoCompleteTextView) findViewById(R.id.autoCompViewStart);
		autoCompViewStart.setAdapter(new PlacesAutoCompleteAdapter(this));
		autoCompViewStart.setOnItemClickListener(this);
		// New Code end

		autoCompView.setAdapter(new PlacesAutoCompleteAdapter(this));
		autoCompView.setOnItemClickListener(this);

		Handler myHandler = new Handler();

		Runnable flipController = new Runnable() {
			@Override
			public void run() {
				animFlipIn = AnimationUtils.loadAnimation(
						getApplicationContext(), R.anim.flipin);
				animFlipOut = AnimationUtils.loadAnimation(
						getApplicationContext(), R.anim.flipout);
				viewFlipper.setInAnimation(animFlipIn);
				viewFlipper.setOutAnimation(animFlipOut);
				viewFlipper.showNext();
			}
		};
		myHandler.postDelayed(flipController, 2000);

		search.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MainActivity.this,
						StationsList.class);
				intent.putExtra("reference", reference);
				intent.putExtra("searchByRef", false);
				intent.putExtra("placeName", autoCompView.getText().toString());
				// New Code Start
				intent.putExtra("startPlaceName", autoCompViewStart.getText()
						.toString());
				// New Code End
				startActivity(intent);
			}
		});
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		// reference = resultList.get(position).get("reference");
		// Log.i("reference : ", reference);
		// Intent intent = new Intent(this, StationsList.class);
		// intent.putExtra("reference", reference);
		// intent.putExtra("searchByRef", true);
		// startActivity(intent);
	}

	class PlacesAutoCompleteAdapter extends BaseAdapter implements Filterable {

		public PlacesAutoCompleteAdapter(Activity activity) {
			MainActivity.activity = activity;

			inflater = (LayoutInflater) activity
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public Filter getFilter() {
			Filter filter = new Filter() {
				@Override
				protected FilterResults performFiltering(CharSequence constraint) {
					FilterResults filterResults = new FilterResults();
					if (constraint != null) {
						// Retrieve the autocomplete results.
						resultList = autocomplete(constraint.toString());

						// Assign the data to the FilterResults
						filterResults.values = resultList;
						filterResults.count = resultList.size();
					}
					return filterResults;
				}

				@Override
				protected void publishResults(CharSequence constraint,
						FilterResults results) {
					if (results != null && results.count > 0) {
						notifyDataSetChanged();
					} else {
						notifyDataSetInvalidated();
					}
				}
			};
			return filter;
		}

		private static final String LOG_TAG = "ExampleApp";

		private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
		private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
		private static final String OUT_JSON = "/json";

		private static final String API_KEY = "AIzaSyAN3Zf0_TzOujpAhuHxAYtY_-0IQgY7JE8";

		private ArrayList<HashMap<String, String>> autocomplete(String input) {
			ArrayList<HashMap<String, String>> resultList = null;

			HttpURLConnection conn = null;
			StringBuilder jsonResults = new StringBuilder();
			try {
				StringBuilder sb = new StringBuilder(PLACES_API_BASE
						+ TYPE_AUTOCOMPLETE + OUT_JSON);
				sb.append("?key=" + API_KEY);
				sb.append("&components=country:uk");
				sb.append("&input=" + URLEncoder.encode(input, "utf8"));

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
				return resultList;
			} catch (IOException e) {
				Log.e(LOG_TAG, "Error connecting to Places API", e);
				return resultList;
			} finally {
				if (conn != null) {
					conn.disconnect();
				}
			}

			try {
				// Create a JSON object hierarchy from the results
				JSONObject jsonObj = new JSONObject(jsonResults.toString());
				JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

				// Extract the Place descriptions from the results
				resultList = new ArrayList<HashMap<String, String>>(
						predsJsonArray.length());
				for (int i = 0; i < predsJsonArray.length(); i++) {
					HashMap<String, String> map = new HashMap<String, String>();
					map.put("description", predsJsonArray.getJSONObject(i)
							.getString("description"));
					map.put("reference", predsJsonArray.getJSONObject(i)
							.getString("reference"));
					resultList.add(map);
				}
			} catch (JSONException e) {
				Log.e(LOG_TAG, "Cannot process JSON results", e);
			}

			return resultList;
		}

		public int getCount() {
			return resultList.size();
		}

		public Object getItem(int position) {
			return resultList.get(position).get("description");
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View vi = convertView;
			if (convertView == null) {
				vi = inflater.inflate(R.layout.row_list_item, null);
				Log.i("view", "exist");
			}

			TextView text = (TextView) vi.findViewById(R.id.text);

			HashMap<String, String> mSession = new HashMap<String, String>();
			mSession = resultList.get(position);
			text.setText(mSession.get("description"));

			return vi;
		}
	}

}
