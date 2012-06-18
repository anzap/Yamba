package com.marakana.yamba;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class UpdaterService extends Service {

	private static final String TAG = UpdaterService.class.getSimpleName();
	private static final int DELAY = 60000;
	protected static final String NEW_STATUS_CONTENT = "com.marakana.yamba.NEW_STATUS";
	protected static final String NEW_STATUS_EXTRA_COUNT = "com.marakana.yamba.NEW_STATUS_EXTRA_COUNT";
	private boolean runFlag = false;
	private Updater updater;
	private YambaApplication yamba;
	private Intent intent;

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
		this.yamba.getStatusData().close();

	}

	private class Updater extends Thread {
		
		private static final String RECEIVE_TIMELINE_NOTIFICATIONS = "com.marakana.yamba.RECEIVE_TIMELINE_NOTIFICATIONS";

		public Updater() {
			super("UpdaterService-Updater");
		}

		@Override
		public void run() {
			UpdaterService updaterService = UpdaterService.this;

			while (updaterService.runFlag) {
				Log.d(TAG, "Updater running");
				try {
					int newUpdates = yamba.fetchStatusUpdates();

					if (newUpdates > 0) {
						Log.d(TAG, "We have a new status");
						intent = new Intent(NEW_STATUS_CONTENT);
						intent.putExtra(NEW_STATUS_EXTRA_COUNT, newUpdates);
						updaterService.sendBroadcast(intent, RECEIVE_TIMELINE_NOTIFICATIONS);
					}
					Log.d(TAG, "Updater ran");
					Thread.sleep(DELAY);
				} catch (InterruptedException e) {
					updaterService.runFlag = false;
				}

			}
		}
	}

}
