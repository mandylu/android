// zengjin@baixing.net
package com.baixing.sharing.referral;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.baixing.activity.BaseFragment;
import com.baixing.activity.BaseTabActivity;
import com.baixing.data.GlobalDataManager;
import com.baixing.util.PerformEvent.Event;
import com.baixing.util.PerformanceTracker;
import com.quanleimu.activity.R;
import com.umeng.common.Log;

public class PosterActivity extends BaseTabActivity {
	
	private static final String TAG = PosterActivity.class.getSimpleName();

	@Override
	public void onCreate(Bundle savedBundle) {
		PerformanceTracker.stamp(Event.E_PostActivity_OnCreate_Begin);
		super.onCreate(savedBundle);
		if (GlobalDataManager.context == null
				|| GlobalDataManager.context.get() == null) {
			GlobalDataManager.context = new WeakReference<Context>(this);
		}

		this.setContentView(R.layout.main_post);
		final View rootV = findViewById(R.id.root);

		onSetRootView(rootV);

		if (this.getSupportFragmentManager().getBackStackEntryCount() == 0) {
			Bundle bundle = new Bundle();
			Log.d(TAG, bundle.toString());
			pushFragment(new PosterFragment(), bundle, false);
		}

		globalTabCtrl.attachView(findViewById(R.id.common_tab_layout), this);
		initTitleAction();
		PerformanceTracker.stamp(Event.E_PostActivity_OnCreate_Leave);
	}
	
	protected void onFragmentEmpty() {
		this.afterChange(TAB_INDEX_PERSONAL);
		finish();
	}
	
	public void handleBack() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(this.findViewById(R.id.contentLayout)
				.getWindowToken(), 0);

		BaseFragment currentFragmet = getCurrentFragment();

		try {
			if (currentFragmet != null && !currentFragmet.handleBack()) {
				if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
					popFragment(currentFragmet);
				} else {
					finish();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected int getTabIndex() {
		return TAB_INDEX_POST;
	}
}
