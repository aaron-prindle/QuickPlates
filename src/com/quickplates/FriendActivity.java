package com.quickplates;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class FriendActivity extends Activity {
	private String user_username;
	private String friend_username;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_friend);
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			user_username = extras.getString("username");
		}

		final Button add_button = (Button) findViewById(R.id.add_friend);
		add_button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				new addFriendTask().execute();
			}
		});
		final Button load_button = (Button) findViewById(R.id.view_friends);
		load_button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(getApplicationContext(),
						Friends2Activity.class);
				i.putExtra("username", user_username);
				startActivity(i);
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_friend, menu);
		return true;
	}

	public class addFriendTask extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			EditText friend_editText = (EditText) findViewById(R.id.friend_username);
			friend_username = friend_editText.getText().toString();
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(
					"http://frozen-waters-2700.herokuapp.com/friendships");
			List<NameValuePair> parameters = new ArrayList<NameValuePair>(2);
			parameters.add(new BasicNameValuePair("user_username",
					user_username));
			parameters.add(new BasicNameValuePair("friend_username",
					friend_username));
			try {
				httppost.setEntity(new UrlEncodedFormEntity(parameters, "UTF-8"));
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
			Toast.makeText(getApplicationContext(), "Successfully added friend", Toast.LENGTH_SHORT).show();
		}

	}

}
