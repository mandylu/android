//liuchong@baixing.com
package com.baixing.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

import com.baixing.broadcast.CommonIntentAction;
import com.baixing.imageCache.ImageLoaderManager;
import com.baixing.view.fragment.PostGoodsFragment;
import com.quanleimu.activity.R;

public class PostActivity extends BaseTabActivity {
	
	private BroadcastReceiver postReceiver;
	
	@Override
	public void onCreate(Bundle savedBundle){
		super.onCreate(savedBundle);
		ImageLoaderManager.initImageLoader();
		this.setContentView(R.layout.main_post);
		final View rootV = findViewById(R.id.root);
		rootV.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() { //When user is input some thing. do not need show tab bar until user finish input.
			
			@Override
			public void onGlobalLayout() {
				int hDiff = rootV.getRootView().getHeight() - rootV.getHeight();
				if (hDiff > 100)
				{
					findViewById(R.id.common_tab_layout).setVisibility(View.GONE);
				}
				else
				{
					findViewById(R.id.common_tab_layout).setVisibility(View.VISIBLE);
				}
			}
		});
		
		
		if (this.getSupportFragmentManager().getBackStackEntryCount() == 0)
		{
            Intent intent = getIntent();
            Bundle bundle = new Bundle();
            if(intent != null && intent.hasExtra(PostGoodsFragment.KEY_INIT_CATEGORY)){
            	Bundle extra = intent.getExtras();
            	if(extra != null){
            		bundle.putString(PostGoodsFragment.KEY_INIT_CATEGORY, extra.getString(PostGoodsFragment.KEY_INIT_CATEGORY));
            	}
            }
            pushFragment(new PostGoodsFragment(), bundle, false);
		}
		
		globalTabCtrl.attachView(findViewById(R.id.common_tab_layout), 	this);
		initTitleAction();
	}
	
	private void registerBroadcast(){
		IntentFilter intentFilter = new IntentFilter(CommonIntentAction.ACTION_BROADCAST_POST_FINISH);
		if(postReceiver == null){
			postReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					String action = intent.getAction();

					if (action.equals(CommonIntentAction.ACTION_BROADCAST_POST_FINISH)) {
						Bundle bundle = intent.getExtras();
						
						Intent personalIntent = new Intent();
						personalIntent.setClass(PostActivity.this, PersonalActivity.class);
						personalIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
						personalIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
						personalIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
						personalIntent.setAction(CommonIntentAction.ACTION_BROADCAST_POST_FINISH);
						personalIntent.putExtras(bundle);
						
						startActivity(personalIntent);					
					}
				}
			};
		}
		this.registerReceiver(postReceiver, intentFilter);		
	}
	
	@Override
	public void onResume(){
		super.onResume();
		registerBroadcast();
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		BaseFragment bf = this.getCurrentFragment();
		if(bf != null && (bf instanceof PostGoodsFragment)){
            if(intent != null && intent.hasExtra(PostGoodsFragment.KEY_INIT_CATEGORY)){
            	Bundle extra = intent.getExtras();
            	if(extra != null){
            		((PostGoodsFragment)bf).updateNewCategoryLayout(extra.getString(PostGoodsFragment.KEY_INIT_CATEGORY));
            	}
            }
		}		
	}
	
	@Override
	public void onPause(){
		super.onPause();
		if(postReceiver != null){
			this.unregisterReceiver(postReceiver);
		}
	}
	
	@Override
	protected int getTabIndex() {
		return TAB_INDEX_POST;
	}
}
