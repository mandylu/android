package com.baixing.sharing.referral;

import java.util.List;

import android.content.Context;
import android.os.Bundle;

import com.baixing.activity.BaseActivity;
import com.baixing.activity.BaseFragment;
import com.baixing.data.GlobalDataManager;
import com.baixing.widget.CustomizeGridView.GridInfo;
import com.quanleimu.activity.R;

public class ReferralEntrance {

	private static ReferralEntrance instance = null;
	
	public static ReferralEntrance getInstance() {
		if (instance != null) {
			return instance;
		}
		instance = new ReferralEntrance();
		return instance;
	}
	
	public void addAppShareGrid(Context context, List<GridInfo> gitems) {
		GridInfo gi = new GridInfo();
		gi.img = GlobalDataManager.getInstance().getImageManager().loadBitmapFromResource(R.drawable.icon_category_referral);
		gi.text = context.getString(R.string.title_referral_promote);
		gitems.add(gi);
	}

	public void pushFragment(BaseFragment fragement, GridInfo info) {
		BaseActivity activity = (BaseActivity) fragement.getActivity();
		if (info.text.equals(activity.getString(R.string.title_referral_promote))) {
			activity.pushFragment(new AppShareFragment(), new Bundle(), false);
		}
	}

}
