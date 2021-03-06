package com.healthslife.healthtest;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.healthslife.R;
import com.healthslife.adapters.HeartRateHisListAdapter;

public class HeartRateHisActivity extends Activity {
	private ActionBar actionBar;
	private ListView hisListView;
	private BaseAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_heart_rate_his);
		hisListView = (ListView) findViewById(R.id.heart_rate_his_listview);
		mAdapter = new HeartRateHisListAdapter(this);
		hisListView.setAdapter(mAdapter);

		setActionBar();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.heart_rate_his, menu);
		return true;
	}

	private void setActionBar() {
		actionBar = getActionBar();
		actionBar.setTitle("心率测量历史记录");
		actionBar.setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}
