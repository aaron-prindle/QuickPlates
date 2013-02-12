package com.quickplates;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class QuickPlatesMainActivity extends Activity implements
		LocationListener, OnInfoWindowClickListener { // , OnMarkerClickListener

	private GoogleMap mMap;
	private LocationManager locationManager;
	private String provider;
	private static final String PREFS_NAME = "QuickPlatesPrefs";
	private Location curLocation = new Location("dummyprovider");
	boolean cameraInitialized = false;
	int zoomlvl = 14;
	private String username;
	private String group_id="";
	private TimerTask doAsynchronousTask;
	private boolean inGroup=true,has_not_shown=true;

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Bundle extras = intent.getExtras();
		if (extras != null) {
			String methodName = extras.getString("methodName");
			if (methodName.equals("normal")) {
				group_id = extras.getString("group_id");
				inGroup = true;
			} else if (methodName.equals("suggestion")) {
				Double lat = Double.parseDouble(extras.getString("lat"));
				Double lng = Double.parseDouble(extras.getString("lng"));
				String name = extras.getString("name");
				String reference = extras.getString("reference");
				Toast.makeText(getBaseContext(),
						"lat: " + lat + " lng: " + lng, Toast.LENGTH_SHORT)
						.show();

				Marker new_marker = mMap.addMarker(new MarkerOptions()
						.position(new LatLng(lat, lng))
						.title(name)
						.snippet(reference)
						.icon(BitmapDescriptorFactory
								.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
				
				new_marker.showInfoWindow();
				

				CameraPosition cp = new CameraPosition.Builder()
						.target(new LatLng(lat, lng)).zoom(zoomlvl).build();

				mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cp));
			} 
//			else if (methodName.equals("nothing")){
//				
//			}
			
		}
	}
	
	public void createDialogue(final ArrayList<HashMap<String,String>> result){
		new AlertDialog.Builder(this)
	    .setTitle("New Invitation Recieved!")
	    .setMessage("Do you want to accept an invitation from: "+result.get(0).get("username") +"?")
	    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	            group_id = result.get(0).get("group_id");
//	            doAsynchronousTask.cancel();
	    		Toast.makeText(getBaseContext(), "Now in " + result.get(0).get("username") + "'s group", // DEBUG
	    				Toast.LENGTH_SHORT).show();
	            
	        }
	     })
	    .setNegativeButton("No", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	            
	        }
	     })
	     .show();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
				.getMap();

		mMap.setOnInfoWindowClickListener(this);
		mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			username = extras.getString("username_text");
		}
		final Button button = (Button) findViewById(R.id.friends);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(getApplicationContext(),
						FriendActivity.class);
				i.putExtra("username", username);
				startActivity(i);
			}
		});

		final Button load_button = (Button) findViewById(R.id.suggestions);
		load_button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if(!inGroup){
					Toast.makeText(getApplicationContext(), "Need to be in a group to view suggestions!", Toast.LENGTH_SHORT).show();
				}
				else{
				Intent i = new Intent(getApplicationContext(),
						SuggestionActivity.class);
				i.putExtra("username", username);
				i.putExtra("group_id", group_id);
				startActivity(i);
				}
			}
		});
		
		final Button logout_button = (Button) findViewById(R.id.logout);
		logout_button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
				SharedPreferences.Editor editor = settings.edit();
				editor.putString("username_text", "");
				editor.commit();
				Intent i = new Intent(getApplicationContext(),
						UserLoginActivity.class);
				startActivity(i);
			}
		});

		// Restore preferences
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		float lat = settings.getFloat("lat", (float) 42.356065);
		float lng = settings.getFloat("lng", (float) -71.095426);

		CameraPosition cp = new CameraPosition.Builder()
				.target(new LatLng(lat, lng)).zoom(zoomlvl).build();

		mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cp));
		mMap.setMyLocationEnabled(true);
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		provider = locationManager.getBestProvider(criteria, false);
		Location location = locationManager.getLastKnownLocation(provider);
		curLocation.setLatitude(42.356065);
		curLocation.setLongitude(-71.095426);
		
		

		// new loadCustomMarkersTask().execute();
		new loadPlacesMarkersTask().execute();
		callAsynchronousTask();
	}
	
	public void callAsynchronousTask() {
	    final Handler handler = new Handler();
	    Timer timer = new Timer();
	    doAsynchronousTask = new TimerTask() {       
	        @Override
	        public void run() {
	            handler.post(new Runnable() {
	                public void run() {       
	                    try {
	                        inviteListenerTask inviteTask = new inviteListenerTask();
	                        // PerformBackgroundTask this class is the class that extends AsynchTask 
	                        inviteTask.execute();
	                    } catch (Exception e) {
	                        // TODO Auto-generated catch block
	                    }
	                }
	            });
	        }
	    };
	    timer.schedule(doAsynchronousTask, 0, 10000); //execute in every 10000 ms
	}



	/* Request updates at startup */
	@Override
	protected void onResume() {
		super.onResume();
		locationManager.requestLocationUpdates(provider, 400, 1, this);
	}

	/* Remove the locationlistener updates when Activity is paused */
	@Override
	protected void onPause() {
		super.onPause();
		locationManager.removeUpdates(this);
	}

	// @Override
	// protected void onStop() {
	// super.onStop();
	// savePreferences();
	// }

	@Override
	protected void onDestroy() {
		super.onDestroy();
		savePreferences();
	}

	@Override
	public void onInfoWindowClick(Marker marker) {
		Intent i = new Intent(getApplicationContext(),
				RestaurantViewActivity.class);
		i.putExtra("inGroup", inGroup);
		i.putExtra("reference", marker.getSnippet());
		i.putExtra("group_id", group_id);
		i.putExtra("name", marker.getTitle());
		i.putExtra("lat", Double.toString(marker.getPosition().latitude));
		i.putExtra("lng", Double.toString(marker.getPosition().longitude));
		startActivity(i);
	}

	public class loadCustomMarkersTask extends
			AsyncTask<String, Void, ArrayList<HashMap<String, String>>> {

		@Override
		protected ArrayList<HashMap<String, String>> doInBackground(
				String... params) {
			// This parts gets builds a GET request (requesting json) to our
			// server
			// START
			DefaultHttpClient httpclient = new DefaultHttpClient(
					new BasicHttpParams());
			HttpGet httpget = new HttpGet(
					"http://frozen-waters-2700.herokuapp.com/restaurants.json");
			// this is the URL to our rails server

			httpget.setHeader("Content-type", "application/json");
			// END
			InputStream inputStream = null;
			String result = null;
			HttpResponse response;
			String value = "";
			ArrayList<HashMap<String, String>> result_array = new ArrayList<HashMap<String, String>>();
			// This part sends the request and receives the JSON response from
			// the server
			// START
			try {
				response = httpclient.execute(httpget);
				HttpEntity entity = response.getEntity();

				inputStream = entity.getContent();
				// json is UTF-8 by default i believe
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(inputStream, "UTF-8"), 8);
				StringBuilder sb = new StringBuilder();

				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}
				result = sb.toString();
				// END

				// This part parses the JSON into a a list of dictionaries. Each
				// list item is a restaurant, and the dict holds the restuarant
				// info
				// START
				JSONArray json = new JSONArray(result);
				for (int i = 0; i < json.length(); ++i) {
					HashMap<String, String> dict = new HashMap<String, String>();
					JSONObject jObject = new JSONObject(json.get(i).toString());
					value = jObject.getString("name");
					dict.put("name", value);
					value = jObject.getString("lat");
					dict.put("lat", value);
					value = jObject.getString("lng");
					dict.put("lng", value);
					value = jObject.getString("wait");
					dict.put("wait", value);
					result_array.add(dict);
					// END
				}

			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return result_array;
		}

		private float getHue(int wait) {
			if (wait >= 0 && wait <= 5)
				return BitmapDescriptorFactory.HUE_GREEN;
			else if (wait > 5 && wait <= 20)
				return BitmapDescriptorFactory.HUE_ORANGE;
			else if (wait > 20)
				return BitmapDescriptorFactory.HUE_RED;
			else
				return BitmapDescriptorFactory.HUE_VIOLET;
		}

		@Override
		protected void onPostExecute(ArrayList<HashMap<String, String>> result) {
			mMap = ((MapFragment) getFragmentManager().findFragmentById(
					R.id.map)).getMap();
			for (HashMap<String, String> dict : result) {
				mMap.addMarker(new MarkerOptions()
						.position(
								new LatLng(Double.parseDouble(dict.get("lat")),
										Double.parseDouble(dict.get("lng"))))
						.snippet(dict.get("wait"))
						.title(dict.get("name"))
						.icon(BitmapDescriptorFactory
								.defaultMarker(getHue(Integer.parseInt(dict
										.get("wait"))))));
			}

		}

	}

	public class loadPlacesMarkersTask extends
			AsyncTask<String, Void, ArrayList<HashMap<String, String>>> {

		@Override
		protected ArrayList<HashMap<String, String>> doInBackground(
				String... params) {
			// This parts gets builds a GET request (requesting json) to our
			// server
			// START
			DefaultHttpClient httpclient = new DefaultHttpClient(
					new BasicHttpParams());
			HttpGet httpget = new HttpGet(
					"https://maps.googleapis.com/maps/api/place/nearbysearch/json?"
							+ "location=" + curLocation.getLatitude() + ","
							+ curLocation.getLongitude() + "&" + "radius=700&"
							+ "types=restaurant&" + "sensor=true&"
							+ "key=AIzaSyCl8M8OsDglFkLeOkzugqpNvPOAYs5xuUU");

			httpget.setHeader("Content-type", "application/json");
			// END
			InputStream inputStream = null;
			String result = null;
			HttpResponse response;
			ArrayList<HashMap<String, String>> result_array = new ArrayList<HashMap<String, String>>();
			// This part sends the request and receives the JSON response from
			// the server
			// START
			try {
				response = httpclient.execute(httpget);
				HttpEntity entity = response.getEntity();

				inputStream = entity.getContent();
				// json is UTF-8 by default i believe
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(inputStream, "UTF-8"), 8);
				StringBuilder sb = new StringBuilder();

				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}
				result = sb.toString();
				// END

				// This part parses the JSON into a a list of dictionaries. Each
				// list item is a restaurant, and the dict holds the restuarant
				// info
				// START
				JSONObject jobj = new JSONObject(result);
				JSONArray json = jobj.getJSONArray("results");
				String value = "";
				JSONObject geometry = new JSONObject();
				JSONObject location_json = new JSONObject();
				for (int i = 0; i < json.length(); ++i) {
					HashMap<String, String> dict = new HashMap<String, String>();
					JSONObject jObject = new JSONObject(json.get(i).toString());
					value = jObject.getString("name");
					dict.put("name", value);
					value = jObject.getString("reference");
					dict.put("reference", value);
					geometry = jObject.getJSONObject("geometry");
					location_json = geometry.getJSONObject("location");
					value = location_json.getString("lat");
					dict.put("lat", value);
					value = location_json.getString("lng");
					dict.put("lng", value);
					result_array.add(dict);
					// END
				}

			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return result_array;
		}

		@Override
		// This updates the MapFragment (the view) with a map marker that is
		// places where the restaurant is and with its correct metadata
		protected void onPostExecute(ArrayList<HashMap<String, String>> result) {
			mMap = ((MapFragment) getFragmentManager().findFragmentById(
					R.id.map)).getMap();
			for (HashMap<String, String> dict : result) {
				mMap.addMarker(new MarkerOptions()
						.position(
								new LatLng(Double.parseDouble(dict.get("lat")),
										Double.parseDouble(dict.get("lng"))))
						.snippet(dict.get("reference"))
						.title(dict.get("name"))
						.icon(BitmapDescriptorFactory
								.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
			}

			new loadCustomMarkersTask().execute();

		}

	}

	@Override
	public void onLocationChanged(Location location) {
		curLocation = location;
		if (!cameraInitialized) {
			Toast.makeText(getBaseContext(), "Using GPS", // DEBUG
					Toast.LENGTH_LONG).show();
			CameraPosition cp = new CameraPosition.Builder()
					.target(new LatLng(location.getLatitude(), location
							.getLongitude())).zoom(zoomlvl).build();
			mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cp));
			cameraInitialized = true;
		}

	}

	@Override
	public void onProviderDisabled(String provider) {
		if(has_not_shown){
			 Toast.makeText(this, "Enable GPS for Current Location Info" +
					 provider,
					 Toast.LENGTH_LONG).show();
			 has_not_shown = false;
		}


	}

	@Override
	public void onProviderEnabled(String provider) {
		Toast.makeText(this, "Disabled provider " + provider,
				Toast.LENGTH_SHORT).show();

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	private void savePreferences() {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putFloat("lat", (float) curLocation.getLatitude());
		editor.putFloat("lng", (float) curLocation.getLongitude());
		editor.commit();
	}

	/** Demonstrates customizing the info window and/or its contents. */
	class CustomInfoWindowAdapter implements InfoWindowAdapter {

		// These a both viewgroups containing an ImageView with id "badge" and
		// two TextViews with id
		// "title" and "snippet".
		private final View mWindow;

		CustomInfoWindowAdapter() {
			mWindow = getLayoutInflater().inflate(R.layout.custom_info_window,
					null);
		}

		@Override
		public View getInfoWindow(Marker marker) {
			render(marker, mWindow);
			return mWindow;
		}

		@Override
		public View getInfoContents(Marker marker) {
			return null;
		}

		private void render(Marker marker, View view) {
			int badge = R.drawable.leftarrow_md;
			((ImageView) view.findViewById(R.id.badge)).setImageResource(badge);

			String title = marker.getTitle();
			TextView titleUi = ((TextView) view.findViewById(R.id.title));
			if (title != null) {
				// Spannable string allows us to edit the formatting of the
				// text.
				SpannableString titleText = new SpannableString(title);
				titleText.setSpan(new ForegroundColorSpan(Color.RED), 0,
						titleText.length(), 0);
				titleUi.setText(titleText);
			} else {
				titleUi.setText("");
			}

			String snippet = marker.getSnippet();
			TextView snippetUi = ((TextView) view.findViewById(R.id.snippet));
			if (snippet != null && snippet.length()<10) { //check to see if from Places or Rails
				SpannableString snippetText = new SpannableString(snippet);
				snippetText.setSpan(new ForegroundColorSpan(Color.MAGENTA), 0,
						snippet.length(), 0);
				snippetUi.setText("wait: " + snippetText);
			} else {
				snippetUi.setText("");
			}
		}
	}

	// @Override
	// public void onActivityResult(int requestCode, int resultCode, Intent
	// data) {
	// super.onActivityResult(requestCode, resultCode, data);
	// Session.getActiveSession().onActivityResult(this, requestCode,
	// resultCode, data);
	// }

	public class inviteListenerTask extends
			AsyncTask<String, Void, ArrayList<HashMap<String, String>>> {

		@Override
		protected ArrayList<HashMap<String, String>> doInBackground(
				String... params) {
			// This parts gets builds a GET request (requesting json) to our
			// server
			// START
			DefaultHttpClient httpclient = new DefaultHttpClient(
					new BasicHttpParams());
			HttpGet httpget = new HttpGet(
					"http://frozen-waters-2700.herokuapp.com/invites?username="+username);
			// this is the URL to our rails server

			httpget.setHeader("Content-type", "application/json");
			// END
			InputStream inputStream = null;
			String result = null;
			HttpResponse response;
			String value = "";
			ArrayList<HashMap<String, String>> result_array = new ArrayList<HashMap<String, String>>();
			// This part sends the request and receives the JSON response from
			// the server
			// START
			try {
				response = httpclient.execute(httpget);
				HttpEntity entity = response.getEntity();

				inputStream = entity.getContent();
				// json is UTF-8 by default i believe
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(inputStream, "UTF-8"), 8);
				StringBuilder sb = new StringBuilder();

				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}
				result = sb.toString();
				// END

				// This part parses the JSON into a a list of dictionaries. Each
				// list item is a restaurant, and the dict holds the restuarant
				// info
				// START
				JSONObject jObject = new JSONObject(result);
				HashMap<String, String> dict = new HashMap<String, String>();
				value = jObject.getString("username");
				dict.put("username", value);
				value = jObject.getString("group_id");
				dict.put("group_id", value);
				result_array.add(dict);
			} catch (Exception e) {
				HashMap<String, String> dict = new HashMap<String, String>();
				dict.put("username", "");
				result_array.clear();
				result_array.add(dict);
			}
			return result_array;
		}

		@Override
		// This updates the MapFragment (the view) with a map marker that is
		// places where the restaurant is and with its correct metadata
		protected void onPostExecute(final ArrayList<HashMap<String, String>> result) {
			String uname = result.get(0).get("username");
//			Toast.makeText(getApplicationContext(), "result.get(0).get('username'): " + uname, // DEBUG
//					Toast.LENGTH_SHORT).show();
			
			if(!uname.equals("")){
				createDialogue(result);
			}


		}

	}

}
