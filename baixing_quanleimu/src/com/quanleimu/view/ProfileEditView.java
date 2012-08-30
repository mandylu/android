package com.quanleimu.view;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.quanleimu.activity.BaseActivity;
import com.quanleimu.activity.QuanleimuApplication;
import com.quanleimu.activity.R;
import com.quanleimu.activity.ThirdpartyTransitActivity;
import com.quanleimu.entity.PostGoodsBean;
import com.quanleimu.entity.UserProfile;
import com.quanleimu.jsonutil.JsonUtil;
import com.quanleimu.util.Communication;
import com.quanleimu.util.ParameterHolder;
import com.quanleimu.util.ViewUtil;
import com.quanleimu.view.MultiLevelSelectionView.MultiLevelItem;

public class ProfileEditView extends BaseView {

	final int BACK_EVENT_ID = 100;
	
	final int MSG_UPDATE_SUCCED = 1;
	final int MSG_UPDATE_FAIL = 2;
	final int MSG_UPDATE_ERROR = 3;
	final int MSG_GOT_CITY_LIST = 4;
			
	
	private UserProfile up;
	private LinkedHashMap<String, PostGoodsBean> beans;
	private String newCityId;
	
	public ProfileEditView(Context context, UserProfile up){
		super(context);
		this.up = up;
		init();
	}
	
	@Override
	public TitleDef getTitleDef(){
		TitleDef title = new TitleDef();
		title.m_visible = true;
		title.m_title = "编辑个人信息";
		title.m_rightActionHint = "完成";
		title.m_leftActionHint = "返回";
		
		return title;
	}
	
	@Override
	public TabDef getTabDef(){
		TabDef tabDef = new TabDef();
		tabDef.m_visible = true;
		
		return tabDef;
	}	
	
	protected void init(){
		LayoutInflater inflator = LayoutInflater.from(this.getContext());
		View v = inflator.inflate(R.layout.edit_profile, null);
		addView(v);
		
		newCityId = up.location;
		updateText(up.nickName, R.id.username);
		updateText(up.gender, R.id.gender);
//		updateText(up.location, R.id.city);
		findViewById(R.id.change_city).setOnClickListener(
				new OnClickListener() {

					public void onClick(View v) {
						MultiLevelSelectionView ml = 
								new MultiLevelSelectionView((BaseActivity)getContext(), 
										"china", 
										"选择常居地", 
										BACK_EVENT_ID,
										3); 
						
						m_viewInfoListener.onNewView(ml);
					}

				});

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
		updateText(format.format(new Date(Long.parseLong(up.createTime) * 1000)), R.id.create_time);
		
		if (newCityId != null && newCityId.length() > 0)
		{
			loadCityMapping(newCityId);
		}
		
	}
	
	private void updateText(String text, int resId)
	{
		TextView tx = (TextView) findViewById(resId);
		tx.setText(text);
	}
	
	private String getTextData(int resId)
	{
		TextView tx = (TextView) findViewById(resId);
		return tx.getText().toString();
	}

	@Override
	public void onPreviousViewBack(int message, Object obj) {
		if (message == BACK_EVENT_ID)
		{
			if (obj instanceof MultiLevelItem)
			{
				MultiLevelItem item = (MultiLevelItem) obj;
				updateText(item.txt, R.id.city);//
				newCityId = item.id;
			}
		}
	}
	
	private Handler myHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (pd != null && pd.isShowing()) {
				pd.dismiss();
			}
			
			switch(msg.what)
			{
			case MSG_UPDATE_ERROR:
				ViewUtil.postShortToastMessage(ProfileEditView.this, "更新失败，请检查网络后重试", 10);
				break;
			case MSG_UPDATE_SUCCED:
				ViewUtil.postShortToastMessage(ProfileEditView.this, "更新成功", 10);
				saveAndexit();
				break;
			case MSG_UPDATE_FAIL:
				ViewUtil.postShortToastMessage(ProfileEditView.this, (String)msg.obj, 10);
				break;
			case MSG_GOT_CITY_LIST:
				beans = (LinkedHashMap) msg.obj;
				updateText(findCityName(up.location), R.id.city);
				break;
			}
		}
	};
	
	private String findCityName(String metaId)
	{
		PostGoodsBean bean = beans.get((String)beans.keySet().toArray()[0]);
		return bean.getDisplayName();
		
	}
	
	public boolean onRightActionPressed()
	{
		if (validate())
		{
			pd = ProgressDialog.show(getContext(),"提示", "更新中，请稍等...");
			
			updateProfile();
			
			return true;
		}
		
		return false;
	}
	
	private boolean validate()
	{
		String nick = getTextData(R.id.username);
		if (!nick.equals(up.nickName))
		{
			if (nick.trim().length() == 0)
			{
				ViewUtil.postShortToastMessage(ProfileEditView.this,R.string.nickname_empty, 10);
				return false;
			}
		}
		
		return true;
	}
	
	private void updateProfile() {
		ParameterHolder params = new ParameterHolder();
		params.addParameter("nickname", getTextData(R.id.username));
		params.addParameter("gender", getTextData(R.id.gender));
		params.addParameter("所在地", newCityId);
		params.addParameter("userId", up.userId);
		
		
		Communication.executeAsyncTask("user_profile_update", params, new Communication.CommandListener() {
			
			@Override
			public void onServerResponse(String serverMessage) {
				try {
					JSONObject obj = new JSONObject(serverMessage).getJSONObject("error");
					if (!"0".equals(obj.getString("code")))
					{
						Message msg = myHandler.obtainMessage(MSG_UPDATE_FAIL, obj.get("message"));
						myHandler.sendMessage(msg);
					}
					else
					{
						myHandler.sendEmptyMessage(MSG_UPDATE_SUCCED);
					}
				} catch (JSONException e) {
					myHandler.sendEmptyMessage(MSG_UPDATE_ERROR);
				}
				
			}
			
			@Override
			public void onException(Exception ex) {
				myHandler.sendEmptyMessage(MSG_UPDATE_ERROR);
			}
		});
	}
	
	private void saveAndexit()
	{
		up.nickName = getTextData(R.id.username);
		up.gender = getTextData(R.id.gender);
		up.location = newCityId;
		
		if(null != m_viewInfoListener){
			m_viewInfoListener.onBack();
		}
	
	}
	
	private void loadCityMapping(String cityId)
	{
		ParameterHolder params = new ParameterHolder();
		params.addParameter("objIds", cityId);
		
		Communication.executeAsyncTask("metaobject", params, new Communication.CommandListener() {
			
			public void onServerResponse(String serverMessage) {
				Message msg = myHandler.obtainMessage(MSG_GOT_CITY_LIST, JsonUtil.getPostGoodsBean(serverMessage));
				myHandler.sendMessage(msg);
			}
			
			public void onException(Exception ex) {
				//Ignor
			}
		});
	}
	
	
	private void startPickupPhoto()
	{
		Intent thirdparty = new Intent(this.getContext(), ThirdpartyTransitActivity.class);
		Bundle ext = new Bundle();
		ext.putString(ThirdpartyTransitActivity.ThirdpartyKey, ThirdpartyTransitActivity.ThirdpartyType_Albam);
		thirdparty.putExtras(ext);
		getContext().startActivity(thirdparty);
	}
	
}
