package com.quickplates;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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

public class Friends2Activity extends Activity{// implements OnItemClickListener {

	ListView listView;
	MyAdapter adapter;
	List<Model> list = new ArrayList<Model>();
	private String user_username;
	private String group_id;

	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.activity_friends2);
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			user_username = extras.getString("username");
		}
		new loadFriendsTask(this).execute();
		final Button create_groupButton = (Button) findViewById(R.id.create_group);
		create_groupButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if(adapter.getChecked_Friends().size() == 0){
					Toast.makeText(getApplicationContext(), "Need to have checked friends to create group!", Toast.LENGTH_SHORT).show();
				}
				else{
					new groupInviteTask().execute();
				}
			}
		});
	}

	public class loadFriendsTask extends
			AsyncTask<String, Void, ArrayList<HashMap<String, String>>> {
		private WeakReference<Friends2Activity> mParentActivity = null;


	    public loadFriendsTask(Friends2Activity parentActivity) {
	        mParentActivity = new WeakReference<Friends2Activity>(parentActivity);
	    }
		
		@Override
		protected ArrayList<HashMap<String, String>> doInBackground(
				String... params) {

			DefaultHttpClient httpclient = new DefaultHttpClient(
					new BasicHttpParams());
			HttpGet httpget = new HttpGet(
					"http://frozen-waters-2700.herokuapp.com/friendships.json?username="
							+ user_username);

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
					value = jObject.getString("username");
					dict.put("username", value);
					value = jObject.getString("phone");
					dict.put("phone", value);
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
				list.add(new Model(data.get("username")));
			}
			
			
			listView = (ListView) findViewById(R.id.my_list);
			final Friends2Activity parentActivity = mParentActivity.get();
			adapter = new MyAdapter(parentActivity, list);
			listView.setAdapter(adapter);
//			listView.setOnItemClickListener(parentActivity);
		}
	}

	public class groupInviteTask extends
			AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(
				String... params) {

			DefaultHttpClient httpclient = new DefaultHttpClient(
					new BasicHttpParams());
			String query_string = "http://frozen-waters-2700.herokuapp.com/invites.json?username="
					+ user_username;

			ArrayList<String> invited_friends = adapter.getChecked_Friends();
			for(String friend:invited_friends){
				query_string+="&invited_usernames[]="+friend;
			}

			HttpPost httppost = new HttpPost(query_string);

			httppost.setHeader("Content-type", "application/json");
			InputStream inputStream = null;
			String result = null;
			HttpResponse response;
			try {
				response = httpclient.execute(httppost);
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
				JSONObject jObject = new JSONObject(result);
				group_id = jObject.getString("group_id");


			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			Toast.makeText(getApplicationContext(), "You are now in a group!", Toast.LENGTH_LONG).show();
			Intent i = new Intent(getApplicationContext(),
					QuickPlatesMainActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			i.putExtra("group_id", group_id);
			i.putExtra("methodName", "normal");
			startActivity(i);
		}

	}

}
