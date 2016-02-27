package com.example.aagam.apptest;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;

public class MainActivity extends Activity {

	private static String APP_ID = "235790260089630"; // Replace with your App ID

	private Facebook facebook = new Facebook(APP_ID);
	private AsyncFacebookRunner mAsyncRunner;
	String FILENAME = "AndroidSSO_data";
	private SharedPreferences mPrefs;

	Button btnFbLogin;
	Button btnFbGetProfile;
	Button btnPostToWall;
	Button btnShowAccessTokens;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		btnFbLogin = (Button) findViewById(R.id.btn_fblogin);
		btnFbGetProfile = (Button) findViewById(R.id.btn_get_profile);
		btnPostToWall = (Button) findViewById(R.id.btn_fb_post_to_wall);
		btnShowAccessTokens = (Button) findViewById(R.id.btn_show_access_tokens);
		mAsyncRunner = new AsyncFacebookRunner(facebook);

		btnFbLogin.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.d("Image Button", "button Clicked");
				loginToFacebook();
			}
		});

		btnFbGetProfile.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				getProfileInformation();
			}
		});

		btnPostToWall.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				postToWall();
			}
		});

		btnShowAccessTokens.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				showAccessTokens();
			}
		});

	}
	public void loginToFacebook() {

		mPrefs = getPreferences(MODE_PRIVATE);
		String access_token = mPrefs.getString("access_token", null);
		long expires = mPrefs.getLong("access_expires", 0);

		if (access_token != null) {
			facebook.setAccessToken(access_token);
			
			btnFbLogin.setVisibility(View.INVISIBLE);
	
			btnFbGetProfile.setVisibility(View.VISIBLE);

			btnPostToWall.setVisibility(View.VISIBLE);

			btnShowAccessTokens.setVisibility(View.VISIBLE);

			Log.d("FB Sessions", "" + facebook.isSessionValid());
		}

		if (expires != 0) {
			facebook.setAccessExpires(expires);
		}

		if (!facebook.isSessionValid()) {
			facebook.authorize(this,
					new String[] { "email", "publish_stream" },
					new DialogListener() {

						@Override
						public void onCancel() {
						}

						@Override
						public void onComplete(Bundle values) {
							SharedPreferences.Editor editor = mPrefs.edit();
							editor.putString("access_token",
									facebook.getAccessToken());
							editor.putLong("access_expires",
									facebook.getAccessExpires());
							editor.commit();

							btnFbLogin.setVisibility(View.INVISIBLE);

							btnFbGetProfile.setVisibility(View.VISIBLE);

							btnPostToWall.setVisibility(View.VISIBLE);

							btnShowAccessTokens.setVisibility(View.VISIBLE);
						}

						@Override
						public void onError(DialogError error) {

						}

						@Override
						public void onFacebookError(FacebookError fberror) {

						}

					});
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		facebook.authorizeCallback(requestCode, resultCode, data);
	}


	public void getProfileInformation() {
		mAsyncRunner.request("me", new RequestListener() {
			@Override
			public void onComplete(String response, Object state) {
				Log.d("Profile", response);
				String json = response;
				try {
					JSONObject profile = new JSONObject(json);
					
					final String name = profile.getString("name");
					
					final String email = profile.getString("email");
					
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							Toast.makeText(getApplicationContext(), "Name: " + name + "\nEmail: " + email, Toast.LENGTH_LONG).show();
						}

					});

					
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onIOException(IOException e, Object state) {
			}

			@Override
			public void onFileNotFoundException(FileNotFoundException e,
					Object state) {
			}

			@Override
			public void onMalformedURLException(MalformedURLException e,
					Object state) {
			}

			@Override
			public void onFacebookError(FacebookError e, Object state) {
			}
		});
	}

	public void postToWall() {
		facebook.dialog(this, "feed", new DialogListener() {

			@Override
			public void onFacebookError(FacebookError e) {
			}

			@Override
			public void onError(DialogError e) {
			}

			@Override
			public void onComplete(Bundle values) {
			}

			@Override
			public void onCancel() {
			}
		});

	}

	public void showAccessTokens() {
		String access_token = facebook.getAccessToken();

		Toast.makeText(getApplicationContext(),
				"Access Token: " + access_token, Toast.LENGTH_LONG).show();
	}
	
	public void logoutFromFacebook() {
		mAsyncRunner.logout(this, new RequestListener() {
			@Override
			public void onComplete(String response, Object state) {
				Log.d("Logout from Facebook", response);
				if (Boolean.parseBoolean(response) == true) {
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							btnFbLogin.setVisibility(View.VISIBLE);

							btnFbGetProfile.setVisibility(View.INVISIBLE);
							btnPostToWall.setVisibility(View.INVISIBLE);
							btnShowAccessTokens.setVisibility(View.INVISIBLE);
						}

					});

				}
			}

			@Override
			public void onIOException(IOException e, Object state) {
			}

			@Override
			public void onFileNotFoundException(FileNotFoundException e,
					Object state) {
			}

			@Override
			public void onMalformedURLException(MalformedURLException e,
					Object state) {
			}

			@Override
			public void onFacebookError(FacebookError e, Object state) {
			}
		});
	}

}