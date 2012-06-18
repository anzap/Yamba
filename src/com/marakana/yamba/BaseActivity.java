package com.marakana.yamba;

import com.marakane.yamba.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class BaseActivity extends Activity {

	YambaApplication yamba;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		yamba = (YambaApplication) getApplication();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.itemPrefs:
			startActivity(new Intent(this, PrefsActivity.class));
			break;
		case R.id.itemToggleService:
			if (yamba.isServiceRunning()) {
				stopService(new Intent(this, UpdaterService.class));
			} else {
				startService(new Intent(this, UpdaterService.class));
			}
			break;
		default:
			break;
		}
		return true;
	}
	
	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		MenuItem toggleServiceItem = menu.findItem(R.id.itemToggleService);
		
		if(yamba.isServiceRunning()) {
			toggleServiceItem.setTitle(R.string.titleServiceStop);
			toggleServiceItem.setIcon(android.R.drawable.ic_media_pause);
		} else {
			toggleServiceItem.setTitle(R.string.titleServiceStart);
			toggleServiceItem.setIcon(android.R.drawable.ic_media_play);
		}
		return true;
	}

}
