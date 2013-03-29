//liuchong@baixing.com
package com.baixing.activity;

import java.lang.ref.WeakReference;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.baixing.broadcast.CommonIntentAction;
import com.baixing.data.GlobalDataManager;
import com.baixing.imageCache.ImageLoaderManager;
import com.baixing.util.PerformEvent.Event;
import com.baixing.util.PerformanceTracker;
import com.baixing.view.fragment.PostGoodsFragment;
import com.quanleimu.activity.R;

public class PostActivity extends BaseTabActivity {
	
	private BroadcastReceiver postReceiver;
	
	@Override
	public void onCreate(Bundle savedBundle){
		PerformanceTracker.stamp(Event.E_PostActivity_OnCreate_Begin);
		super.onCreate(savedBundle);
		if(GlobalDataManager.context == null || GlobalDataManager.context.get() == null){
			GlobalDataManager.context = new WeakReference<Context>(this);
		}
		ImageLoaderManager.initImageLoader();
		this.setContentView(R.layout.main_post);
		final View rootV = findViewById(R.id.root);
		
		onSetRootView(rootV);
		
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
		PerformanceTracker.stamp(Event.E_PostActivity_OnCreate_Leave);
	}
	
	protected void onFragmentEmpty() {
		
		this.afterChange(TAB_INDEX_CAT);
		finish();
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
						PerformanceTracker.stamp(Event.E_GetPostSuccessBroadcast);
						Intent personalIntent = new Intent();
						personalIntent.setClass(PostActivity.this, PersonalActivity.class);
						personalIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
						personalIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
						personalIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
						personalIntent.setAction(CommonIntentAction.ACTION_BROADCAST_POST_FINISH);
						personalIntent.putExtras(bundle);
						
						startActivity(personalIntent);
						
						BaseFragment current = PostActivity.this.getCurrentFragment();
						if(current != null && current instanceof PostGoodsFragment){
							current.finishFragment();
						}
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
	
	public void handleBack(){
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); 
        imm.hideSoftInputFromWindow(this.findViewById(R.id.contentLayout).getWindowToken(), 0); 

        BaseFragment currentFragmet = getCurrentFragment();
        
		try{
	    	if( currentFragmet != null && !currentFragmet.handleBack()){
    			if (getSupportFragmentManager().getBackStackEntryCount()>1)
    			{
    				popFragment(currentFragmet);
    			}
    			else
    			{
    				finish();
    			}
	    	}	
		}catch(Exception e){
			e.printStackTrace();
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
