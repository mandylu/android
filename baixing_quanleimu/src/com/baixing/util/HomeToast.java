// lumengdi@baixing.net

package com.baixing.util;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

import com.devspark.appmsg.AppMsg;
import com.quanleimu.activity.R;

public class HomeToast {

	public static void show(Context context, LayoutInflater inflater, String tip, boolean isLongTime) {
		
		int duration = isLongTime ? AppMsg.LENGTH_LONG : AppMsg.LENGTH_SHORT;
		AppMsg.Style msgStyle = new AppMsg.Style(duration, R.color.toast_info_bg);
		AppMsg appMsg = AppMsg.makeText((Activity)context, tip, msgStyle);
		
		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.AXIS_Y_SHIFT);
		layoutParams.topMargin = (int)context.getResources().getDimension(R.dimen.title_height);
		appMsg.setLayoutParams(layoutParams);
		
		appMsg.show();
    }
	
}
