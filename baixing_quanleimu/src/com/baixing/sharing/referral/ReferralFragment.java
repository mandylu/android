package com.baixing.sharing.referral;

import java.util.Observable;
import java.util.Observer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.baixing.activity.BaseFragment;
import com.baixing.activity.PostActivity;
import com.baixing.data.GlobalDataManager;
import com.baixing.entity.UserBean;
import com.baixing.message.BxMessageCenter;
import com.baixing.message.IBxNotificationNames;
import com.baixing.util.PerformEvent.Event;
import com.baixing.util.PerformanceTracker;
import com.quanleimu.activity.R;
import com.xiaomi.mipush.MiPushService;

public class ReferralFragment extends BaseFragment implements View.OnClickListener, Observer {
    
    private static Context context;
    
    View referralmain = null;
    
    @Override
    public void onCreate(Bundle savedInstanceState){
    	super.onCreate(savedInstanceState);
    	BxMessageCenter.defaultMessageCenter().registerObserver(this, IBxNotificationNames.NOTIFICATION_LOGOUT);
    	
    	context = GlobalDataManager.getInstance().getApplicationContext();
    	UserBean curUser = GlobalDataManager.getInstance().getAccountManager().getCurrentUser();
    	if (curUser != null && !TextUtils.isEmpty(curUser.getPhone())) {
    		MiPushService.setAlias(context, curUser.getPhone());
    	}
    	
    	ReferralNetwork referralNetwork = ReferralNetwork.getInstance();
    	referralNetwork.addObserver(this);
    	referralNetwork.updateReferral("info", null);
    }
    
    @Override
    public void initTitle(TitleDef title) {
        title.m_visible = true;
        title.m_title = getString(R.string.title_referral_setting);
        title.m_leftActionHint = "完成";
    }
    
    @Override
    public boolean hasGlobalTab() {
		return true;
	}
    
    @Override
    public View onInitializeView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

    	referralmain = inflater.inflate(R.layout.referral_info, null);
        ((Button) referralmain.findViewById(R.id.btn_referral_post)).setOnClickListener(new BtnPostOnClickListener());

        return referralmain;
    }
    
    class BtnPostOnClickListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent();
			PerformanceTracker.stamp(Event.E_Start_PostActivity);
			intent.setClass(context, PostActivity.class);
    		intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
    		intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
    		intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
    		context.startActivity(intent);
		}
    	
    }

	@Override
	public void update(Observable observable, Object data) {
		if (referralmain != null && data instanceof String) {
			displayInfo(referralmain, (String)data);
		}
	}
	
	private void displayInfo(View view, String jsonData) {
		try {
			JSONObject jsonInfo = new JSONObject(jsonData);
			JSONArray postInfo = jsonInfo.getJSONArray("info");
			((TextView) view.findViewById(R.id.textAllPostsNum)).setText(postInfo.getString(0));
			((TextView) view.findViewById(R.id.textEffectivePostsNum)).setText(postInfo.getString(1));
			((TextView) view.findViewById(R.id.textPendingPostsNum)).setText(postInfo.getString(2));
			JSONArray appInfo = jsonInfo.getJSONArray("app");
			((TextView) view.findViewById(R.id.textAlreadyShareNum)).setText(appInfo.getString(0));
			((TextView) view.findViewById(R.id.textEffectiveShareNum)).setText(appInfo.getString(1));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
}
