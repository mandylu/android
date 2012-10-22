package com.quanleimu.view.fragment;

import java.io.File;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;

import com.quanleimu.util.Util;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.quanleimu.activity.BaseFragment;
import com.quanleimu.activity.R;
import com.quanleimu.entity.PostGoodsBean;
import com.quanleimu.entity.UserProfile;
import com.quanleimu.jsonutil.JsonUtil;
import com.quanleimu.util.Communication;
import com.quanleimu.util.ParameterHolder;
import com.quanleimu.util.ViewUtil;
import com.quanleimu.view.fragment.MultiLevelSelectionFragment.MultiLevelItem;
import com.quanleimu.widget.GenderPopupDialog;
import com.quanleimu.widget.StateImage;
import com.quanleimu.widget.StateImage.UploadListener;

public class ProfileEditFragment extends BaseFragment implements UploadListener {
	final int BACK_EVENT_ID = 100;
	
	public static final int NONE = 0;
	public static final int PHOTOHRAPH = 1;
	public static final int PHOTOZOOM = 2; 
	public static final int PHOTORESOULT = 3;
	
	final int MSG_UPDATE_SUCCED = 1;
	final int MSG_UPDATE_FAIL = 2;
	final int MSG_UPDATE_ERROR = 3;
	final int MSG_GOT_CITY_LIST = 4;
	final int MSG_NEW_IMAGE = 5;
	final int MSG_UPLOAD_IMG_FAIL = 6;
	final int MSG_UPLOAD_IMG_DONE = 7;
	
	private UserProfile up;
	private LinkedHashMap<String, PostGoodsBean> beans;
	private String newCityId;
	private String newGender;
	private Bundle bundle = null;
	
	private StateImage profileImg;
	
	
	@Override
	public void initTitle(TitleDef title){
		title.m_visible = true;
		title.m_title = "编辑个人信息";
		title.m_rightActionHint = "完成";
		title.m_leftActionHint = "返回";
	}
	
	@Override
	public void initTab(TabDef tab){
		tab.m_visible = true;
	}
	
	
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		
		outState.putSerializable("image", profileImg);
		outState.putString("gender", newGender);
		outState.putString("newCity", newCityId);
		super.onSaveInstanceState(outState);
	}

	public void onCreate(Bundle savedData)
	{
		super.onCreate(savedData);
		this.up = (UserProfile) Util.loadDataFromLocate(getAppContext(), "userProfile");
		newCityId = up.location;
		newGender = up.gender;
		
		if (savedData != null)
		{
			profileImg = (StateImage) savedData.getSerializable("image");
			newGender = savedData.getString("gender");
			newCityId = savedData.getString("newCity");
//			Log.d(TAG, "restore image" + thumb);
		}
		
		if (profileImg == null)
		{
			profileImg = new StateImage(R.drawable.pic_my_avator_girl);
		}
		
		profileImg.setContext(getActivity());
	}
	
	@Override
	public void onFragmentBackWithData(int message, Object obj) {
		if (message == BACK_EVENT_ID)
		{
			if (obj instanceof MultiLevelItem)
			{
				MultiLevelItem item = (MultiLevelItem) obj;
				newCityId = item.id;
				String newCityName = item.txt;//findCityName(newCityId);
				getArguments().putString("cityName", newCityName);
				updateText(newCityName, R.id.city, getView());//
				
//				loadCityMapping(newCityId);
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		logCreateView(savedInstanceState);
		if (savedInstanceState != null)
		{
			Log.d(TAG, "recreate view from saved data." + this.getClass().getName());
		}
		final Activity activity = this.getActivity();
		final View v = inflater.inflate(R.layout.edit_profile, null);

		updateText(up.nickName, R.id.username, v);
		updateText(newGender, R.id.gender, v);
//		PostGoodsBean bean = new
		v.findViewById(R.id.city).setOnClickListener(
				new OnClickListener() {

					public void onClick(View v) {
						Bundle bundle = createArguments("选择常居地", null);
						bundle.putInt(ARG_COMMON_REQ_CODE, BACK_EVENT_ID);
						bundle.putString("metaId", "china");
						bundle.putInt("maxLevel", 3);
						pushFragment(new MultiLevelSelectionFragment(), bundle);
					}

				});

		v.findViewById(R.id.gender).setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View cV) {
				GenderPopupDialog dlg = new GenderPopupDialog(v.getContext(), 
						((TextView)v.findViewById(R.id.gender)).getText().equals("男"));
				dlg.show();
				dlg.setOnDismissListener(new DialogInterface.OnDismissListener(){

					@Override
					public void onDismiss(DialogInterface dialog) {
						if(dialog != null){
							newGender = ((GenderPopupDialog)dialog).isBoy() ? "男" : "女";
							((TextView)v.findViewById(R.id.gender)).setText(newGender);
						}
					}
					
				});
			}
		});
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
		updateText(format.format(new Date(Long.parseLong(up.createTime) * 1000)), R.id.create_time, v);
		
		if (newCityId != null && newCityId.length() > 0)
		{
			if(beans != null){
				String name = this.findCityName(newCityId);
				if(name != null && !name.equals("")){
					this.updateText(name, R.id.city, v);
				}
			}else if(getArguments().containsKey("cityName")){				
				String name = (String)getArguments().getSerializable("cityName");
				if(name != null && !name.equals("")){
					this.updateText(name, R.id.city, v);
				}				
			}else{
				loadCityMapping(newCityId);
			}
		}
		
		v.findViewById(R.id.personalImage).setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				ViewUtil.pickupPhoto(activity, 0);
			}
			
		});
		
		
//		if (thumb != null)
//		{
//			((ImageView)v.findViewById(R.id.personalImage)).setImageBitmap(thumb);
//		}
//		else 
		if(up.resize180Image != null && !up.resize180Image.equals("") && !up.resize180Image.equals("null")){
//			updateImage(up.resize180Image, v);
			profileImg.assignServerImage(up.resize180Image);
		}

		if(up.gender != null && up.gender.equals("女")){
//			((ImageView)v.findViewById(R.id.personalImage)).setImageResource(R.drawable.pic_my_avator_girl);
			profileImg.setDefault(R.drawable.pic_my_avator_girl);
		}else{
//			((ImageView)v.findViewById(R.id.personalImage)).setImageResource(R.drawable.pic_my_avator_boy);
			profileImg.setDefault(R.drawable.pic_my_avator_boy);
		}
		
		profileImg.attachView((ImageView)v.findViewById(R.id.personalImage));
		
		return v;
	}
	
	public void onResume()
	{
		super.onResume();
	}
	
	public void onPause()
	{
		super.onPause();
	}
	
	public void onDestory()
	{
		super.onDestroy();
		profileImg.cancelUpload();
	}
	
	private void updateText(String text, int resId, View parent)
	{
		TextView tx = null;
		if (parent != null)
		{
			tx = (TextView) parent.findViewById(resId);
		}
		else if (getActivity() != null)
		{
			tx = (TextView) getActivity().findViewById(resId);
		}
		
		if (tx != null)
		{
			tx.setText(text);
		}
	}
	
	private String getTextData(int resId)
	{
		TextView tx = (TextView) getActivity().findViewById(resId);
		return tx.getText().toString();
	}
	
	
	private void loadCityMapping(String cityId)
	{
		ParameterHolder params = new ParameterHolder();
		params.addParameter("objIds", cityId);
		
		Communication.executeAsyncGetTask("metaobject", params, new Communication.CommandListener() {
			
			public void onServerResponse(String serverMessage) {
				beans = JsonUtil.getPostGoodsBean(serverMessage);
				sendMessage(MSG_GOT_CITY_LIST, null);
			}
			
			public void onException(Exception ex) {
				//Ignor
			}
		});
	}
	
	
	
	private static int getClosestResampleSize(int cx, int cy, int maxDim)
    {
        int max = Math.max(cx, cy);
        
        int resample = 1;
        for (resample = 1; resample < Integer.MAX_VALUE; resample++)
        {
            if (resample * maxDim > max)
            {
                resample--;
                break;
            }
        }
        
        if (resample > 0)
        {
            return resample;
        }
        return 1;
    }
	
	public String getRealPathFromURI(Uri contentUri) {
		String[] proj = { MediaStore.Images.Media.DATA };
		Cursor cursor = getActivity().managedQuery(contentUri, proj, null, null, null);

		if (cursor == null)
			return null;

		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

		cursor.moveToFirst();

		String ret = cursor.getString(column_index);
//		cursor.close();
		
		return ret;
	}
	
	
	@Override
	protected void handleMessage(Message msg, Activity activity, View rootView) {

		hideProgress();
		
		switch(msg.what)
		{
		case MSG_UPDATE_ERROR:
			ViewUtil.postShortToastMessage(ProfileEditFragment.this.getView(), "更新失败，请检查网络后重试", 10);
			break;
		case MSG_UPDATE_SUCCED:
			ViewUtil.postShortToastMessage(ProfileEditFragment.this.getView(), "更新成功", 10);
			saveAndexit();
			break;
		case MSG_UPDATE_FAIL:
			ViewUtil.postShortToastMessage(ProfileEditFragment.this.getView(), (String)msg.obj, 10);
			break;
		case MSG_GOT_CITY_LIST:
			updateText(findCityName(newCityId), R.id.city, null);
			break;
		case MSG_NEW_IMAGE://FIXME:
//			profileUri = (Uri) msg.obj;
//			Bitmap bp = BitmapFactory.decodeFile(profileUri.toString());
//			((ImageView) findViewById(R.id.personalImage)).setImageBitmap(bp);
//			updateImageView(profileUri.toString());
			break;
		case MSG_UPLOAD_IMG_DONE:
//			newServerImage = (String) msg.obj;//FIXME:
			hideProgress();
			
			continueUpdateProfile();
			break;
		case MSG_UPLOAD_IMG_FAIL:
			ViewUtil.postShortToastMessage(ProfileEditFragment.this.getView(), "上传图片失败", 10);
			break;
		}
	
	}

	private void updateProfile() {
		ParameterHolder params = new ParameterHolder();
		params.addParameter("nickname", getTextData(R.id.username));
		params.addParameter("gender", getTextData(R.id.gender));
		params.addParameter(URLEncoder.encode("所在地"), newCityId);
		params.addParameter("userId", up.userId);
		if (profileImg.getServerUri() != null && !profileImg.getServerUri().equals(up.resize180Image))
		{
			params.addParameter("image_i", profileImg.getServerUri());
		}
		
		
		Communication.executeAsyncGetTask("user_profile_update", params, new Communication.CommandListener() {
			
			@Override
			public void onServerResponse(String serverMessage) {
				try {
					JSONObject obj = new JSONObject(serverMessage).getJSONObject("error");
					if (!"0".equals(obj.getString("code")))
					{
						sendMessage(MSG_UPDATE_FAIL, obj.get("message"));
					}
					else
					{
						sendMessage(MSG_UPDATE_SUCCED, null);
					}
				} catch (JSONException e) {
					sendMessage(MSG_UPDATE_ERROR, null);
				}
				
			}
			
			@Override
			public void onException(Exception ex) {
				sendMessage(MSG_UPDATE_ERROR, null);
			}
		});
	}
	
	private void continueUpdateProfile()
	{
		showProgress(R.string.dialog_title_info, R.string.dialog_message_updating, true);
		
		updateProfile();
	}
	
	private String findCityName(String metaId)
	{
		PostGoodsBean bean = beans.get((String)beans.keySet().toArray()[0]);
		return bean.getDisplayName();
		
	}
	
	public boolean handleBack()
	{
		finishFragment(requestCode, null);
		return true;
	}
	
	private void saveAndexit()
	{
		up.nickName = getTextData(R.id.username);
		up.gender = getTextData(R.id.gender);
		up.location = newCityId;
		//TODO: how about the image url?
		
		this.finishFragment(requestCode, up);
	
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode == NONE) {
			return;
		}
		
		Uri uri = null;
		if (requestCode == PHOTOHRAPH) {
			File picture = new File(Environment.getExternalStorageDirectory(), "temp" + 0 + ".jpg");
			uri = Uri.fromFile(picture);
		}
		else if (data != null && requestCode == PHOTOZOOM)
		{
			uri = data.getData();
		}
		
		Log.e("IMG", "img url : " + uri);
		if (uri != null)
		{
//			sendMessage(MSG_NEW_IMAGE, uri);
			profileImg.assignLocalImage(uri.toString());
		}
	
	}
	
	
	@Override
	public void handleRightAction()
	{
		if (validate())
		{
			if (profileImg != null && profileImg.getLocalUri() != null)
			{
				showProgress(R.string.dialog_title_info, R.string.dialog_message_uploading, true);
				profileImg.startUpload(this);
			}
			else
			{
				continueUpdateProfile();
			}
			
		}
	}
	
	@Override
	public void onSucced(String newLoc) {
		sendMessage(MSG_UPLOAD_IMG_DONE, newLoc);						
	}
	
	@Override
	public void onFailed() {
		sendMessage(MSG_UPLOAD_IMG_FAIL, null);
	}
	
	private boolean validate()
	{
		String nick = getTextData(R.id.username);
		if (!nick.equals(up.nickName))
		{
			if (nick.trim().length() == 0)
			{
				ViewUtil.postShortToastMessage(getView(),R.string.nickname_empty, 10);
				return false;
			}
		}
		
		return true;
	}
	
	
	
	
}
