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

import com.baixing.activity.BaseFragment;
import com.baixing.data.GlobalDataManager;
import com.baixing.entity.UserBean;
import com.baixing.message.BxMessageCenter;
import com.baixing.message.IBxNotificationNames;
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
    	BtnOnClickListener btnOnClickListener = new BtnOnClickListener();
        ((Button) referralmain.findViewById(R.id.btn_referral_post)).setOnClickListener(btnOnClickListener);
        ((Button) referralmain.findViewById(R.id.btn_referral_haibao)).setOnClickListener(btnOnClickListener);

        return referralmain;
    }
    
    class BtnOnClickListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent();

			switch (v.getId()) {
			case R.id.btn_referral_post:
				intent.setClass(context, com.baixing.activity.PostActivity.class);
				break;
			case R.id.btn_referral_haibao:
				intent.setClass(context, com.baixing.sharing.referral.PosterActivity.class);
				break;
			default:
				break;
			}

			intent.putExtra("isReferral", true);
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
			JSONArray appInfo = jsonInfo.getJSONArray("app");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

	}
}