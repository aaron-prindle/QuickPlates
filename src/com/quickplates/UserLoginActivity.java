package com.quickplates;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class UserLoginActivity extends Activity {
	private static final String PREFS_NAME = "QuickPlatesPrefs";
	private String username_text, password_text, phone_text;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Restore preferences
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		username_text = settings.getString("username_text", "");
		if(!username_text.equals("")){
			Intent i = new Intent(getApplicationContext(),
					QuickPlatesMainActivity.class);
			i.putExtra("username_text", username_text);
			startActivity(i);
		}
		setContentView(R.layout.activity_login);
		final Button button = (Button) findViewById(R.id.submit);

		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				new userLoginTask().execute();
			}
		});

		// // start Facebook Login
		// Session.openActiveSession(this, true, new Session.StatusCallback() {
		//
		// // callback when session changes state
		// @Override
		// public void call(Session session, SessionState state,
		// Exception exception) {
		// if (session.isOpened()) {
		// // make request to the /me API
		// Request.executeMeRequestAsync(session,
		// new Request.GraphUserCallback() {
		//
		// // callback after Graph API response with user
		// // object
		// @Override
		// public void onCompleted(GraphUser user,
		// Response response) {
		// if (user != null) {
		// Toast.makeText(
		// getBaseContext(),
		// "Hello " + user.getName() + "!",
		// Toast.LENGTH_SHORT).show();
		// }
		// }
		// });
		// }
		//
		// }
		// });
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_user_login, menu);
		return true;
	}

	private void savePreferences() {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("username_text", username_text);
		editor.commit();
	}

	public class userLoginTask extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			EditText username = (EditText) findViewById(R.id.username);
			EditText password = (EditText) findViewById(R.id.password);
			EditText phone = (EditText) findViewById(R.id.phone);
			username_text = username.getText().toString();
			password_text = password.getText().toString();
			phone_text = phone.getText().toString();

			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(
					"http://frozen-waters-2700.herokuapp.com/users");

			// Request parameters and other properties.
			List<NameValuePair> parameters = new ArrayList<NameValuePair>(3);
			parameters.add(new BasicNameValuePair("user[username]",
					username_text));
			parameters.add(new BasicNameValuePair("user[password]",
					password_text));
			parameters.add(new BasicNameValuePair("user[phone]", phone_text));
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
		// This updates the MapFragment (the view) with a map marker that is
		// places where the restaurant is and with its correct metadata
		protected void onPostExecute(Void result) {
			savePreferences();
			Intent i = new Intent(getApplicationContext(),
					QuickPlatesMainActivity.class);
			i.putExtra("username_text", username_text);
			startActivity(i);
		}

	}

}
