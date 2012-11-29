package com.quanleimu.activity;

import android.os.Bundle;

import com.quanleimu.view.fragment.PersonalInfoFragment;

public class PersonalActivity extends BaseTabActivity {

	@Override
	protected int getTabIndex() {
		return TAB_INDEX_PERSONAL;
	}
	
	public void onCreate(Bundle savedBundle)
	{
		super.onCreate(savedBundle);
		this.setContentView(R.layout.main_post);
		
		if (this.getSupportFragmentManager().getBackStackEntryCount() == 0)
		{
			pushFragment(new PersonalInfoFragment(), bundle, true);
		}
		
		globalTabCtrl.attachView(findViewById(R.id.common_tab_layout), 	this);
		initTitleAction();
	}

}
