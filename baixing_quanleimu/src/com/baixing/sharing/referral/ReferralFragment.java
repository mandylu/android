package com.baixing.sharing.referral;

import java.util.Observable;
import java.util.Observer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.baixing.activity.BaseFragment;
import com.baixing.data.GlobalDataManager;
import com.baixing.entity.UserBean;
import com.baixing.message.BxMessageCenter;
import com.baixing.message.IBxNotificationNames;
import com.baixing.util.Util;
import com.quanleimu.activity.R;
import com.xiaomi.mipush.MiPushService;

public class ReferralFragment extends BaseFragment implements View.OnClickListener, Observer {
    
    private static Context context;
    
    View referralmain = null;
    String phone;
    
    @Override
    public void onCreate(Bundle savedInstanceState){
    	super.onCreate(savedInstanceState);
    	BxMessageCenter.defaultMessageCenter().registerObserver(this, IBxNotificationNames.NOTIFICATION_LOGOUT);
    	
    	context = GlobalDataManager.getInstance().getApplicationContext();
    	UserBean curUser = GlobalDataManager.getInstance().getAccountManager().getCurrentUser();
    	if (curUser != null && Util.isValidMobile(curUser.getPhone())) {
    		MiPushService.setAlias(context, curUser.getPhone());
    		phone = curUser.getPhone();
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
        ((Button) referralmain.findViewById(R.id.btn_referral_post)).setOnClickListener(this);
        ((Button) referralmain.findViewById(R.id.btn_referral_haibao)).setOnClickListener(this);
        
        ((Button) referralmain.findViewById(R.id.btn_referral_posts_detail)).setOnClickListener(this);
        ((Button) referralmain.findViewById(R.id.btn_referral_haibao_detail)).setOnClickListener(this);

        return referralmain;
    }
    
    @Override
    public int[] excludedOptionMenus() {
    	return new int[]{OPTION_SETTING, OPTION_FEEDBACK, OPTION_LOGOUT, OPTION_EXIT, OPTION_LOGIN};
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
		Intent intent = new Intent();

		switch (v.getId()) {
		case R.id.btn_referral_post:
			intent.setClass(context, com.baixing.activity.PostActivity.class);
			break;
		case R.id.btn_referral_haibao:
			intent.setClass(context, com.baixing.sharing.referral.PosterActivity.class);
			break;
		case R.id.btn_referral_posts_detail:
		{
			Bundle bundle=new Bundle();
			bundle.putString("title", getString(R.string.button_referral_detail_post));
			bundle.putString("url", ReferralDetailFragment.PROMO_DETIAL_URL + "?mobile=" + phone + "&taskType=" + ReferralUtil.TASK_POST);
			pushFragment(new ReferralDetailFragment(), bundle);	
			return;
		}
		case R.id.btn_referral_haibao_detail:
		{
			Bundle bundle=new Bundle();
			bundle.putString("title", getString(R.string.button_referral_detail_haibao));
			bundle.putString("url", ReferralDetailFragment.PROMO_DETIAL_URL + "?mobile=" + phone + "&taskType=" + ReferralUtil.TASK_HAIBAO);
			pushFragment(new ReferralDetailFragment(), bundle);	
			return;
		}
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