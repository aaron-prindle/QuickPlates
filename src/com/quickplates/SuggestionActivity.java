package com.quickplates;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SuggestionActivity extends Activity implements OnItemClickListener {

	ListView listView;
	MyAdapter adapter;
	List<Model> list = new ArrayList<Model>();
	private String user_username;
	private String group_id;
	private TimerTask doAsynchronousTask;

	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.activity_suggestion);
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			user_username = extras.getString("username");
			group_id = extras.getString("group_id");
		}
		new loadSuggestionsTask(this).execute();
//		callAsynchronousTask(this);
		
	}
	
	public void callAsynchronousTask(final SuggestionActivity suggestionActivity) {
	    final Handler handler = new Handler();
	    Timer timer = new Timer();
	    doAsynchronousTask = new TimerTask() {       
	        @Override
	        public void run() {
	            handler.post(new Runnable() {
	                public void run() {       
	                    try {
	                    	loadSuggestionsTask suggestTask = new loadSuggestionsTask(suggestionActivity);
	                        // PerformBackgroundTask this class is the class that extends AsynchTask 
	                        suggestTask.execute();
	                    } catch (Exception e) {
	                        // TODO Auto-generated catch block
	                    }
	                }
	            });
	        }
	    };
	    timer.schedule(doAsynchronousTask, 0, 10000); //execute in every 10000 ms
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
		TextView label = (TextView) v.getTag(R.id.label);
//		CheckBox checkbox = (CheckBox) v.getTag(R.id.check);
		String[] values = label.getText().toString().split(",");
		String name = values[0];
		String lat = values[1];
		String lng = values[2];
		String reference = values[3];
		Intent i = new Intent(getApplicationContext(),
				QuickPlatesMainActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		i.putExtra("name", name);
		i.putExtra("lat", lat);
		i.putExtra("lng", lng);
		i.putExtra("reference", reference);
		i.putExtra("methodName", "suggestion");
		startActivity(i);
	}

	public class loadSuggestionsTask extends
			AsyncTask<String, Void, ArrayList<HashMap<String, String>>> {
		private WeakReference<SuggestionActivity> mParentActivity = null;


	    public loadSuggestionsTask(SuggestionActivity suggestionActivity) {
	        mParentActivity = new WeakReference<SuggestionActivity>(suggestionActivity);
	    }
		
		@Override
		protected ArrayList<HashMap<String, String>> doInBackground(
				String... params) {

			DefaultHttpClient httpclient = new DefaultHttpClient(
					new BasicHttpParams());
			HttpGet httpget = new HttpGet(
					"http://frozen-waters-2700.herokuapp.com/suggestions.json?group_id="
							+ group_id);

			httpget.setHeader("Content-type", "application/json");
			InputStream inputStream = null;
			String result = null;
			HttpResponse response;
			String value = "";
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
					value = jObject.getString("reference");
					dict.put("reference", value);
					result_array.add(dict);
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
		protected void onPostExecute(ArrayList<HashMap<String, String>> result) {
			for (HashMap<String, String> data : result){
				String list_value = data.get("name") + "," + data.get("lat") + "," + data.get("lng") + "," + data.get("reference");
				list.add(new Model(list_value));
			}
			
			
			listView = (ListView) findViewById(R.id.my_suggestion_list);
			final SuggestionActivity parentActivity = mParentActivity.get();
			adapter = new MyAdapter(parentActivity, list);
			listView.setAdapter(adapter);
			listView.setOnItemClickListener(parentActivity);
		}
	}

}
