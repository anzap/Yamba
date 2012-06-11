package com.marakane.yamba;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;

public class TimelineActivity extends Activity {
	private static final String TAG = TimelineActivity.class.getSimpleName();
	private TextView textTimeline;
	private YambaApplication yamba;
	private Cursor statusUpdates;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.timeline);
		textTimeline = (TextView) findViewById(R.id.textTimeline);
		yamba = (YambaApplication) getApplication();
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

		while (statusUpdates.moveToNext()) {
			String user = statusUpdates.getString(statusUpdates
					.getColumnIndex(StatusData.C_USER));
			String text = statusUpdates.getString(statusUpdates
					.getColumnIndex(StatusData.C_TEXT));

			textTimeline.append(String.format("%s: %s\n", user, text));
		}
	}
}
