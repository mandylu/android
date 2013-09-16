package com.baixing.sharing.referral;

import java.util.Observable;
import java.util.Observer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.baixing.activity.BaseFragment;
import com.baixing.activity.PostActivity;
import com.baixing.data.GlobalDataManager;
import com.baixing.message.BxMessageCenter;
import com.baixing.message.IBxNotificationNames;
import com.baixing.util.PerformEvent.Event;
import com.baixing.util.PerformanceTracker;
import com.baixing.widget.EditUsernameDialogFragment.ICallback;
import com.quanleimu.activity.R;

public class ReferralFragment extends BaseFragment implements View.OnClickListener, ICallback, Observer {
    
    private static Context context;
    
    @Override
    public void onCreate(Bundle savedInstanceState){
    	super.onCreate(savedInstanceState);
    	BxMessageCenter.defaultMessageCenter().registerObserver(this, IBxNotificationNames.NOTIFICATION_LOGOUT);
    	
    	context = GlobalDataManager.getInstance().getApplicationContext();
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

    	View referralmain = inflater.inflate(R.layout.referral_info, null);
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onEditSucced(String newUserName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
}
