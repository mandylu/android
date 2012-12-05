package com.quanleimu.activity;

import android.os.Bundle;

import com.baixing.view.fragment.PostGoodsFragment;

public class PostActivity extends BaseTabActivity {
	
	public void onCreate(Bundle savedBundle)
	{
		super.onCreate(savedBundle);
		this.setContentView(R.layout.main_post);
		
		if (this.getSupportFragmentManager().getBackStackEntryCount() == 0)
		{
            QuanleimuApplication.postEntryFlag = 0;
            pushFragment(new PostGoodsFragment(), new Bundle(), false);
		}
		
		globalTabCtrl.attachView(findViewById(R.id.common_tab_layout), 	this);
		initTitleAction();
	}
	
	@Override
	protected int getTabIndex() {
		return TAB_INDEX_POST;
	}
}
