package com.quanleimu.activity;

import android.os.Bundle;

import com.quanleimu.view.fragment.GridCateFragment;
import com.quanleimu.view.fragment.PersonalInfoFragment;
import com.quanleimu.view.fragment.PostGoodsFragment;

public class PostActivity extends BaseTabActivity {
	
	public void onCreate(Bundle savedBundle)
	{
		super.onCreate(savedBundle);
		this.setContentView(R.layout.main_post);
		
		if (this.getSupportFragmentManager().getBackStackEntryCount() == 0)
		{
			Bundle args = new Bundle(bundle);
			args.putInt(BaseFragment.ARG_COMMON_ANIMATION_IN, 0);
			args.putInt(BaseFragment.ARG_COMMON_ANIMATION_EXIT, 0);
			pushFragment(new GridCateFragment(), args, true);
		}
		
		globalTabCtrl.attachView(findViewById(R.id.common_tab_layout), 	this);
		initTitleAction();
	}
	
	@Override
	protected int getTabIndex() {
		return TAB_INDEX_POST;
	}
}
