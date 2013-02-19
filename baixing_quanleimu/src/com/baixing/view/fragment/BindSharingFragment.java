package com.baixing.view.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baixing.activity.BaseActivity;
import com.baixing.activity.BaseFragment;
import com.baixing.activity.BaseFragment.TitleDef;
import com.baixing.sharing.BaseSharingManager;
import com.baixing.sharing.QZoneSharingManager;
import com.baixing.sharing.WeiboSSOSharingManager;
import com.baixing.sharing.WeiboSSOSharingManager.WeiboAccessTokenWrapper;
import com.baixing.util.Util;
import com.quanleimu.activity.R;

class BindSharingFragment extends BaseFragment implements OnClickListener{

	@Override
	protected View onInitializeView(LayoutInflater inflater,
			ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		LinearLayout layout = (LinearLayout)inflater.inflate(R.layout.bind_sharing, null);
		layout.findViewById(R.id.bindWeibo).setOnClickListener(this);
		layout.findViewById(R.id.bindQQ).setOnClickListener(this);
		return layout;
	}
	
	@Override
    public void initTitle(TitleDef title) {
        title.m_visible = true;
        title.m_title = "绑定转发帐号";
        title.m_leftActionHint = "返回";
    }
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		setBindingStatus();
	}
	
	private void setBindingStatus(){
		View view = this.getView();
		View rootView = view == null ? null : view.getRootView();
		if(rootView != null){
			if(isWeiboBinded()){			
				((TextView)rootView.findViewById(R.id.weiboBindStatus)).setText("已绑定");
			}else{
				((TextView)rootView.findViewById(R.id.weiboBindStatus)).setText("尚未绑定，点此绑定");
			}
			
			if(isQZoneBinded()){
				((TextView)rootView.findViewById(R.id.qqBindStatus)).setText("已绑定");
			}else{
				((TextView)rootView.findViewById(R.id.qqBindStatus)).setText("尚未绑定，点此绑定");
			}
		}
	}
	
	private boolean isWeiboBinded(){
		WeiboAccessTokenWrapper token = (WeiboAccessTokenWrapper) Util.loadDataFromLocate(this.getActivity(),
				WeiboSSOSharingManager.STRING_WEIBO_ACCESS_TOKEN, WeiboAccessTokenWrapper.class);
		return token != null;		
	}
	
	private boolean isQZoneBinded(){
		String accessToken = (String)Util.loadDataFromLocate(this.getActivity(), QZoneSharingManager.STRING_ACCESS_TOKEN, String.class);
//		String openId = (String)Util.loadDataFromLocate(this.getActivity(), QZoneSharingManager.STRING_OPENID, String.class);
		return accessToken != null && accessToken.length() > 0;// && openId != null && openId.length() > 0;
	}
	
	@Override
	public void onResume(){
		super.onResume();
		setBindingStatus();
	}
	
	enum BindType{
		BindType_Weibo,
		BindType_QZone
	}
	private void showUnBindConfirmDialog(final BindType type){
		new AlertDialog.Builder(this.getActivity())
		.setMessage("是否解除绑定？")
		.setPositiveButton("是", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(type == BindType.BindType_Weibo){
					Util.deleteDataFromLocate(getActivity(), WeiboSSOSharingManager.STRING_WEIBO_ACCESS_TOKEN);
					setBindingStatus();
				}else if(type == BindType.BindType_QZone){
					Util.deleteDataFromLocate(getActivity(), QZoneSharingManager.STRING_ACCESS_TOKEN);
					Util.deleteDataFromLocate(getActivity(), QZoneSharingManager.STRING_OPENID);
					setBindingStatus();
				}
			}
		})
		.setNegativeButton("否", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {	
				//tracker
				dialog.dismiss();
			}
		}).show();
	}

	private BaseSharingManager sharingMgr = null;
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v.getId() == R.id.bindWeibo){
			if(isWeiboBinded()){
				showUnBindConfirmDialog(BindType.BindType_Weibo);
			}else{
				if(sharingMgr != null){
					sharingMgr.release();
				}				
				sharingMgr = new WeiboSSOSharingManager((BaseActivity)getActivity());
				sharingMgr.auth();
			}
		}else if(v.getId() == R.id.bindQQ){
			if(isQZoneBinded()){
				showUnBindConfirmDialog(BindType.BindType_QZone);
			}else{
				if(sharingMgr != null){
					sharingMgr.release();
				}
				sharingMgr = new QZoneSharingManager(getActivity());
				sharingMgr.auth();
			}
		}
	}
	
}