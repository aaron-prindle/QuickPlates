package com.quickplates;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.quickplates.QuickPlatesMainActivity.inviteListenerTask;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class RestaurantViewActivity extends Activity {

	ListView listView;
	MyAdapter adapter;
	List<Model> list = new ArrayList<Model>();
	private String reference, group_id, name, lat, lng;
	private boolean inGroup;

	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.activity_restaurant_view);
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			reference = extras.getString("reference");
			group_id = extras.getString("group_id");
			name = extras.getString("name");
			lat = extras.getString("lat");
			lng = extras.getString("lng");
			inGroup = extras.getBoolean("inGroup");

		}
		TextView name_view = (TextView) findViewById(R.id.name_view);
		name_view.setText(name);
		final Button add_suggestion_button = (Button) findViewById(R.id.add_suggestion);
		add_suggestion_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!inGroup){
					Toast.makeText(getApplicationContext(), "Need to be in a group to add a suggestion!", Toast.LENGTH_SHORT).show();
				}
				else{
					new addSuggestionTask().execute();
				}
			}
		});

		new loadRestaurantTask(this).execute();
	}

	public class loadRestaurantTask extends
			AsyncTask<String, Void, ArrayList<HashMap<String, String>>> {
		private WeakReference<RestaurantViewActivity> mParentActivity = null;

		public loadRestaurantTask(RestaurantViewActivity restaurantViewActivity) {
			mParentActivity = new WeakReference<RestaurantViewActivity>(
					restaurantViewActivity);
		}

		@Override
		protected ArrayList<HashMap<String, String>> doInBackground(
				String... params) {

			DefaultHttpClient httpclient = new DefaultHttpClient(
					new BasicHttpParams());
			HttpGet httpget = new HttpGet(
					"https://maps.googleapis.com/maps/api/place/details/json?reference="
							+ reference
							+ "&sensor=true&key=AIzaSyCl8M8OsDglFkLeOkzugqpNvPOAYs5xuUU");

			httpget.setHeader("Content-type", "application/json");
			InputStream inputStream = null;
			String result = null;
			HttpResponse response;
			ArrayList<HashMap<String, String>> result_array = new ArrayList<HashMap<String, String>>();
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
				JSONObject json = new JSONObject(result);
				JSONObject jObject = json.getJSONObject("result");
				String value = "";
				HashMap<String, String> dict = new HashMap<String, String>();

				value = jObject.getString("formatted_address");
				dict.put("address", value);
				value = jObject.getString("formatted_phone_number");
				dict.put("phone", value);
				value = jObject.getString("price_level");
				dict.put("price_level", value);
				value = jObject.getString("rating");
				dict.put("rating", value);
				value = jObject.getString("url");
				dict.put("url", value);

				// get review info here
				result_array.add(dict);

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
		protected void onPostExecute(ArrayList<HashMap<String, String>> result) {
			for (Map.Entry<String, String> entry : result.get(0).entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();
				list.add(new Model(key + ": " + value));
			}

			listView = (ListView) findViewById(R.id.my_restaurant_list);
			final RestaurantViewActivity parentActivity = mParentActivity.get();
			adapter = new MyAdapter(parentActivity, list);
			listView.setAdapter(adapter);
		}
	}

	public class addSuggestionTask extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(
					"http://frozen-waters-2700.herokuapp.com/suggestions");

			// Request parameters and other properties.
			List<NameValuePair> parameters = new ArrayList<NameValuePair>(5);
			parameters.add(new BasicNameValuePair("group_id", group_id));
			parameters.add(new BasicNameValuePair("suggestion[name]", name));
			parameters.add(new BasicNameValuePair("suggestion[lat]", lat));
			parameters.add(new BasicNameValuePair("suggestion[lng]", lng));
			parameters.add(new BasicNameValuePair("suggestion[reference]",
					reference));
			try {
				httppost.setEntity(new UrlEncodedFormEntity(parameters, "UTF-8"));
				// Execute and get the response.
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();

				if (entity != null) {
					InputStream instream = entity.getContent();
					try {
						// do something useful
					} finally {
						instream.close();
					}
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			Intent i = new Intent(getApplicationContext(),
					QuickPlatesMainActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
					| Intent.FLAG_ACTIVITY_SINGLE_TOP);
			i.putExtra("methodName", "nothing");
			startActivity(i);
		}
	}
}
