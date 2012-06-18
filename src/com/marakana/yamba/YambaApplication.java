package com.marakana.yamba;

import java.util.List;

import winterwell.jtwitter.Twitter;
import winterwell.jtwitter.Twitter.Status;
import android.app.Application;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.util.Log;

public class YambaApplication extends Application implements
		OnSharedPreferenceChangeListener {

	private static final String TAG = YambaApplication.class.getSimpleName();

	private SharedPreferences prefs;
	private Twitter twitter;
	private boolean serviceRunning;
	private StatusData statusData;

	@Override
	public void onCreate() {
		super.onCreate();

		// Setup preferences
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);

		Log.i(TAG, "onCreated");

	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		Log.i(TAG, "onTerminated");
	}

	public synchronized Twitter getTwitter() {
		if (twitter == null) {
			String username = prefs.getString("username", "");
			String password = prefs.getString("password", "");
			String apiRoot = prefs.getString("apiRoot",
					"http://yamba.marakana.com/api");

			twitter = new Twitter(username, password);
			twitter.setAPIRootUrl(apiRoot);
		}
		return twitter;
	}

	public synchronized void onSharedPreferenceChanged(
			SharedPreferences sharedPreferences, String key) {
		this.twitter = null;
	}

	public boolean isServiceRunning() {
		return serviceRunning;
	}

	public void setServiceRunning(boolean serviceRunning) {
		this.serviceRunning = serviceRunning;
	}

	public StatusData getStatusData() {
		if (statusData == null) {
			statusData = new StatusData(this);
		}
		return statusData;
	}

	public synchronized int fetchStatusUpdates() {
		Log.d(TAG, "Fetching status updates.");
		Twitter twitter = getTwitter();
		if (twitter == null) {
			Log.d(TAG, "Twitter connection is not initialized.");
			return 0;
		}

		try {
			List<Status> statuses = twitter.getFriendsTimeline();
			long latestStatusCreatedAtTime = this.getStatusData()
					.getLatestStatusCreatedAtTime();

			int count = 0;
			ContentValues values = new ContentValues();
			for (Status status : statuses) {
				values.put(StatusData.C_ID, status.getId());
				values.put(StatusData.C_TEXT, status.text);
				long createdAt = status.createdAt.getTime();
				values.put(StatusData.C_CREATED_AT, createdAt);
				values.put(StatusData.C_USER, status.user.name);

				Log.d(TAG, "Got update with id " + status.id + ".Saving.");

				this.getStatusData().insertOrIgnore(values);

				if (latestStatusCreatedAtTime < createdAt) {
					count++;
				}
				return count;
			}
		} catch (RuntimeException e) {
			Log.e(TAG, "Failed to fetch status updates", e);
		}
		return 0;
	}
	
	public SharedPreferences getPrefs() {
		return prefs;
	}

}
