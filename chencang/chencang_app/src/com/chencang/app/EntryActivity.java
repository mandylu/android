package com.chencang.app;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.baixing.activity.BaseActivity;
import com.baixing.activity.BaseFragment;
import com.baixing.data.GlobalDataManager;
import com.baixing.tracking.Sender;
import com.baixing.tracking.TrackConfig;
import com.baixing.tracking.Tracker;
import com.baixing.util.LocationService;
import com.baixing.util.Util;
import com.baixing.view.fragment.ListingFragment;
import com.chencang.core.R;

/**
 * 
 * @author liuchong
 *
 */
public class EntryActivity extends BaseActivity implements SplashJob.JobDoneListener {

	private SplashJob splashJob;
	private List<Runnable> pendingTask;
	
	boolean isRestored = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.e("ABC", "entry started with intent " + this.getIntent() + " and saved instacne " + savedInstanceState);
		super.onCreate(savedInstanceState);
		setContentView(com.chencang.core.R.layout.main_activity);
	
		splashJob = new SplashJob(this, this);
		pendingTask = new ArrayList<Runnable>();
		
		isRestored = savedInstanceState != null;
	}
	
	
	public void onJobDone() {
		if (this.getSupportFragmentManager().getBackStackEntryCount() == 0)
		{
			String catName = GlobalDataManager.getInstance().getCategoryEnglishName();
			String cityName = GlobalDataManager.getInstance().getCityEnglishName();
			Bundle b = new Bundle(bundle);
			
			if (TextUtils.isEmpty(catName) || TextUtils.isEmpty(cityName)) {
				Intent intent = new Intent();
				intent.setClass(this, CityAndCategoryActivity.class);
				View cover =findViewById(R.id.splash_cover);
				if (cover != null) {
					cover.setVisibility(View.GONE);
				}
				this.startActivityForResult(intent, 100);
//				this.pushFragment(new ListingFragment(), b, true);
			} else {
				b.putString("categoryEnglishName", catName);
				this.pushFragment(new ListingFragment(), b, true);
			}
		}
		else
		{
			this.notifyStackTop();
		}
		
		this.splashJob = null; //Remove splash job reference.
		if (pendingTask != null && pendingTask.size() > 0)
		{
			for (Runnable task : pendingTask)
			{
				task.run();
			}
		}
	}
	
	
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (this.getSupportFragmentManager().getBackStackEntryCount() == 0) {
			if (resultCode == Activity.RESULT_OK) {
				Bundle b = new Bundle(bundle);
				b.putString("categoryEnglishName", GlobalDataManager.getInstance().getCategoryEnglishName());
				this.pushFragment(new ListingFragment(), b, true);
			} else if (resultCode == Activity.RESULT_CANCELED) {
				Util.clearData(this, "cityName");
				Util.clearData(this, "lastCategory");
				this.finish();
			}
		} else {
			BaseFragment fragment = this.getCurrentFragment();
			fragment.onActivityResult(requestCode, resultCode, data);
		}
	}


	protected void onResume() {
		bundle.putString("backPageName", "");

		Tracker.getInstance().event(TrackConfig.TrackMobile.BxEvent.APP_RESUME).end();
		
		super.onResume();
		BaseFragment bf = this.getCurrentFragment();
		if(bf == null && splashJob == null){
			splashJob = new SplashJob(this, this);
		}
		if (splashJob != null && !splashJob.isJobDone())
		{
			View cover =findViewById(R.id.splash_cover);
			if (cover != null && isRestored)
			{
				cover.setVisibility(View.GONE);
			} 
			splashJob.doSplashWork();
		}
		
//		for (Runnable task : resumeTask) {
//			task.run();
//		}
//		resumeTask.clear();
	} 

	protected void onDestory()
	{
		LocationService.getInstance().stop();
		isRestored = false;
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		Tracker.getInstance().event(TrackConfig.TrackMobile.BxEvent.APP_PAUSE).end();
		super.onPause();
	}

	@Override
	protected void onStart() {
		Tracker.getInstance().event(TrackConfig.TrackMobile.BxEvent.APP_START).append(TrackConfig.TrackMobile.Key.CITY, GlobalDataManager.getInstance().getCityEnglishName()).end();
		Tracker.getInstance().save();
		Sender.getInstance().notifySendMutex();
		super.onStart();
	}

	@Override
	protected void onStop() {
		Tracker.getInstance().event(TrackConfig.TrackMobile.BxEvent.APP_STOP).end();
		Tracker.getInstance().save();
		Sender.getInstance().notifySendMutex();
		super.onStop();
	}
}
