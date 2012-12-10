package com.quanleimu.activity;

import android.content.Intent;
import android.os.Bundle;

import com.baixing.broadcast.CommonIntentAction;
import com.baixing.view.fragment.PersonalInfoFragment;
import com.baixing.view.fragment.PersonalPostFragment;

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
		Intent intent = getIntent();
		
		String action = intent.getAction();
		if (action != null && action.equals(CommonIntentAction.ACTION_BROADCAST_POST_FINISH)) {
			Bundle extras = intent.getExtras();
			if(extras != null){
				pushFragment(new PersonalPostFragment(), extras, false);
			}		
		}
		
		globalTabCtrl.attachView(findViewById(R.id.common_tab_layout), 	this);
		initTitleAction();
	}

}
