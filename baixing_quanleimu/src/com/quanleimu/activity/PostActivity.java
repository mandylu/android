package com.quanleimu.activity;

import android.content.Intent;
import android.os.Bundle;

import com.baixing.view.fragment.PostGoodsFragment;

public class PostActivity extends BaseTabActivity {
	
	@Override
	public void onCreate(Bundle savedBundle){
		super.onCreate(savedBundle);
		this.setContentView(R.layout.main_post);
		
		if (this.getSupportFragmentManager().getBackStackEntryCount() == 0)
		{
            QuanleimuApplication.postEntryFlag = 0;
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
	
	@Override
	public void onResume(){
		super.onResume();
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		BaseFragment bf = this.getCurrentFragment();
		if(bf != null && (bf instanceof PostGoodsFragment)){
            if(intent != null && intent.hasExtra(PostGoodsFragment.KEY_INIT_CATEGORY)){
            	Bundle extra = intent.getExtras();
            	if(extra != null){
            		((PostGoodsFragment)bf).updateNewCategoryLayout(extra.getString(PostGoodsFragment.KEY_INIT_CATEGORY));
            	}
            }
		}
		super.onNewIntent(intent);
	}
	
	@Override
	protected int getTabIndex() {
		return TAB_INDEX_POST;
	}
}
