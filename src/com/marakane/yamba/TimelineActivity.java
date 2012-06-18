package com.marakane.yamba;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import android.widget.Toast;

public class TimelineActivity extends BaseActivity {
	private static final String TAG = TimelineActivity.class.getSimpleName();
	private static final String SEND_TIMELINE_NOTIFICATIONS = "com.marakana.yamba.SEND_TIMELINE_NOTIFICATIONS";
	private ListView listTimeline;
	private Cursor statusUpdates;
	private SimpleCursorAdapter adapter;
	private static final String[] FROM = { StatusData.C_CREATED_AT,
			StatusData.C_USER, StatusData.C_TEXT };
	private static int[] TO = { R.id.textCreatedAt, R.id.textUser,
			R.id.textText };
	private TimelineReceiver receiver;
	private IntentFilter filter;

	private static final ViewBinder VIEW_BINDER = new ViewBinder() {

		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			if (view.getId() != R.id.textCreatedAt) {
				return false;
			}
			long timestamp = cursor.getLong(columnIndex);
			CharSequence relativeTimeSpanString = DateUtils
					.getRelativeTimeSpanString(timestamp);
			((TextView) view).setText(relativeTimeSpanString);
			return true;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.timeline);
		listTimeline = (ListView) findViewById(R.id.listTimeline);

		if (yamba.getPrefs().getString("username", null) == null) {
			startActivity(new Intent(this, PrefsActivity.class));
			Toast.makeText(this, R.string.msgSetupPrefs, Toast.LENGTH_LONG)
					.show();
		}

		filter = new IntentFilter(UpdaterService.NEW_STATUS_CONTENT);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		yamba.getStatusData().close();
	}

	@Override
	protected void onResume() {
		super.onResume();

		statusUpdates = yamba.getStatusData().getStatusUpdates();
		startManagingCursor(statusUpdates);

		adapter = new SimpleCursorAdapter(this, R.layout.row, statusUpdates,
				FROM, TO);
		adapter.setViewBinder(VIEW_BINDER);
		listTimeline.setAdapter(adapter);

		registerReceiver(receiver, filter, SEND_TIMELINE_NOTIFICATIONS, null);
	}

	@Override
	protected void onPause() {
		super.onPause();

		unregisterReceiver(receiver);
	}

	class TimelineReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			statusUpdates.requery();
			adapter.notifyDataSetChanged();
			Log.d("TimelineReceiver", "onReceived");
		}

	}
}
