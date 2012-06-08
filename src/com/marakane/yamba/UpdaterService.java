package com.marakane.yamba;

import java.util.List;

import winterwell.jtwitter.Twitter.Status;
import winterwell.jtwitter.TwitterException;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.util.Log;

public class UpdaterService extends Service {

	private static final String TAG = UpdaterService.class.getSimpleName();
	private static final int DELAY = 60000;
	private boolean runFlag = false;
	private Updater updater;
	private YambaApplication yamba;
	private DBHelper dbHelper;
	private SQLiteDatabase db;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreated");
		this.updater = new Updater();
		this.yamba = (YambaApplication) getApplication();
		dbHelper = new DBHelper(this);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		Log.d(TAG, "onStarted");
		this.runFlag = true;
		this.updater.start();
		this.yamba.setServiceRunning(true);
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroyed");
		this.runFlag = false;
		this.updater.interrupt();
		this.updater = null;
		this.yamba.setServiceRunning(false);

	}

	private class Updater extends Thread {
		List<winterwell.jtwitter.Twitter.Status> timeline;

		public Updater() {
			super("UpdaterService-Updater");
		}

		@Override
		public void run() {
			UpdaterService updaterService = UpdaterService.this;

			while (updaterService.runFlag) {
				Log.d(TAG, "Updater running");
				try {

					try {
						// get the timeline
						timeline = yamba.getTwitter().getFriendsTimeline();
					} catch (TwitterException e) {
						Log.e(TAG, "Failed to connect to twitter api", e);
					}

					db = dbHelper.getWritableDatabase();
					ContentValues values = new ContentValues();
					for (Status status : timeline) {
						values.clear();
						values.put(DBHelper.C_ID, status.id);
						values.put(DBHelper.C_SOURCE, status.source);
						values.put(DBHelper.C_TEXT, status.text);
						values.put(DBHelper.C_CREATED_AT,
								status.createdAt.getTime());
						values.put(DBHelper.C_USER, status.user.name);
						db.insertOrThrow(DBHelper.TABLE, null, values);

						Log.d(TAG, String.format("%s: %s", status.user.name,
								status.text));

					}

					db.close();

					Log.d(TAG, "Updater ran");
					Thread.sleep(DELAY);
				} catch (InterruptedException e) {
					updaterService.runFlag = false;
				}

			}
		}
	}

}
