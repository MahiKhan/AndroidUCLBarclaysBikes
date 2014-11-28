package com.uclteam6.uclbarclayscycles;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class StationAdapter extends BaseAdapter {

	private Activity activity;
	private ArrayList<HashMap<String, String>> data;
	private static LayoutInflater inflater = null;

	public StationAdapter(Activity a, ArrayList<HashMap<String, String>> sList) {
		activity = a;
		data = sList;

		inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public int getCount() {
		return data.size();
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View vi = convertView;
		if (convertView == null) {
			vi = inflater.inflate(R.layout.row, null);
			Log.i("view", "exist");
		}

		TextView name = (TextView) vi.findViewById(R.id.name);
		TextView nbBikes = (TextView) vi.findViewById(R.id.nbBikes);
		TextView nbEmptyDocks = (TextView) vi.findViewById(R.id.nbEmptyDocks);
		TextView distance = (TextView) vi.findViewById(R.id.distance);

		HashMap<String, String> mSession = new HashMap<String, String>();
		mSession = data.get(position);
		name.setText(mSession.get("name"));
		nbBikes.setText("Number of bikes : " + mSession.get("nbBikes"));
		nbEmptyDocks.setText("Number of empty docks : "
				+ mSession.get("nbEmptyDocks"));
		distance.setText("Distance : " + mSession.get("distance") + " meters");

		return vi;
	}

}
