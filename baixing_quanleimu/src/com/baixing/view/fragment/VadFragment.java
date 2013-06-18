//liuchong@baixing.com
package com.baixing.view.fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baixing.activity.BaseActivity;
import com.baixing.activity.BaseFragment;
import com.baixing.adapter.VadImageAdapter;
import com.baixing.broadcast.CommonIntentAction;
import com.baixing.data.GlobalDataManager;
import com.baixing.entity.Ad;
import com.baixing.entity.Ad.EDATAKEYS;
import com.baixing.entity.AdList;
import com.baixing.entity.UserBean;
import com.baixing.jsonutil.JsonUtil;
import com.baixing.message.BxMessageCenter;
import com.baixing.message.BxMessageCenter.IBxNotification;
import com.baixing.message.IBxNotificationNames;
import com.baixing.network.api.ApiError;
import com.baixing.network.api.ApiParams;
import com.baixing.network.api.BaseApiCommand;
import com.baixing.network.api.BaseApiCommand.Callback;
import com.baixing.tracking.TrackConfig.TrackMobile.BxEvent;
import com.baixing.util.ErrorHandler;
import com.baixing.util.TextUtil;
import com.baixing.util.Util;
import com.baixing.util.VadListLoader;
import com.baixing.util.ViewUtil;
import com.baixing.util.post.PostCommonValues;
import com.baixing.view.AdViewHistory;
import com.baixing.view.vad.VadLogger;
import com.baixing.view.vad.VadPageController;
import com.baixing.view.vad.VadPageController.ActionCallback;
import com.baixing.widget.ContextMenuItem;
import com.baixing.widget.FavAndReportDialog;
import com.quanleimu.activity.R;
import com.tencent.mm.sdk.platformtools.Log;

public class VadFragment extends BaseFragment implements View.OnTouchListener,View.OnClickListener, OnItemSelectedListener, VadListLoader.HasMoreListener, VadListLoader.Callback, ActionCallback, Callback, Observer {

	public interface IListHolder{
		public void startFecthingMore();
		public boolean onResult(int msg, VadListLoader loader);//return true if getMore succeeded, else otherwise
	};
	
//	private static int NETWORK_REQ_DELETE = 1;
//	private static int NETWORK_REQ_REFRESH = 2;
//	private static int NETWORK_REQ_UPDATE = 3;
	private static final int MSG_REFRESH_CONFIRM = 4;
	private static final int MSG_REFRESH = 5;
	private static final int MSG_UPDATE = 6;
	private static final int MSG_DELETE = 7;
	private static final int MSG_LOAD_AD_EVENT = 8;
	private static final int MSG_NETWORK_FAIL = 9;
	private static final int MSG_FINISH_FRAGMENT = 10;
	public static final int MSG_ADINVERIFY_DELETED = 0x00010000;
	public static final int MSG_MYPOST_DELETED = 0x00010001;
	private static final int MSG_LOGIN_TO_PROSECUTE = 11;

	public Ad detail = new Ad();
	private String json = "";
	
//	private WeakReference<Bitmap> mb_loading = null;
	
	private boolean keepSilent = false;
	
	private VadListLoader mListLoader;
	
	private IListHolder mHolder = null;
	
	private VadPageController pageController;
	
	List<View> pages = new ArrayList<View>();
	
	enum REQUEST_TYPE{

		REQUEST_TYPE_REFRESH(MSG_REFRESH, "ad_refresh"),
		REQUEST_TYPE_UPDATE(MSG_UPDATE, "ad_list"),
		REQUEST_TYPE_DELETE(MSG_DELETE, "ad_delete");
		public int reqCode;
		public String apiName;
		REQUEST_TYPE(int requestCode, String apiName) {
			this.reqCode = requestCode;
			this.apiName = apiName;
		}
	}
	
	
	@Override
	public void onDestroy(){
		this.keepSilent = true;
		BxMessageCenter.defaultMessageCenter().removeObserver(this);	
		super.onDestroy();
	}
	
	@Override
	public boolean handleBack(){
		this.keepSilent = false;

		return false;
	}
	
	@Override
	public void onPause() {
		this.keepSilent = true;
		super.onPause();
		pages.clear();
	}
	
	@Override
	public void onResume(){
		
		VadLogger.trackPageView(detail, getAppContext());
		
		this.keepSilent = false;
		super.onResume();
		
		Boolean firstIn = (Boolean)Util.loadDataFromLocate(getAppContext(), "firstInVadAfter3.3", Boolean.class);
		if(!isMyAd() && (firstIn == null || firstIn)){
			Util.saveDataToLocate(getAppContext(), "firstInVadAfter3.3", false);
			
			getTitleDef().m_titleControls.findViewById(R.id.vad_title_fav_parent).post(new Runnable(){
				@Override
				public void run(){
					handleStoreBtnClicked();
				}
			});
		}
	}
	
	private boolean isMyAd(){
		if(detail == null) return false;
//		return GlobalDataManager.getInstance().isMyAd(detail.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_ID));
		return GlobalDataManager.getInstance().isMyAd(detail);
	}
	
	public boolean onTouch (View v, MotionEvent event){
	    switch (event.getAction()) {
	    case MotionEvent.ACTION_DOWN:
	    case MotionEvent.ACTION_MOVE: 
	    	if(getView() != null && getView().findViewById(R.id.svDetail) != null){
	    		((ViewPager)getView().findViewById(R.id.svDetail)).requestDisallowInterceptTouchEvent(true);
	    	}
	        break;
	    case MotionEvent.ACTION_OUTSIDE:
	    case MotionEvent.ACTION_UP:
	    	if(getView() != null && getView().findViewById(R.id.svDetail) != null){
	    		((ViewPager)getView().findViewById(R.id.svDetail)).requestDisallowInterceptTouchEvent(false);
	    	}
	        break;		
	    }
		return this.keepSilent;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.mListLoader = (VadListLoader) getArguments().getSerializable("loader");
		int index = getArguments().getInt("index", 0);
		if(mListLoader == null 
				|| mListLoader.getGoodsList() == null 
				|| mListLoader.getGoodsList().getData() == null
				|| mListLoader.getGoodsList().getData().size() <= index){
			return;
		}
		detail = mListLoader.getGoodsList().getData().get(index);
		if (savedInstanceState != null) //
		{
//			this.mListLoader.setHandler(handler);
			this.mListLoader.setHasMoreListener(this);
		}
   		BxMessageCenter.defaultMessageCenter().registerObserver(this, IBxNotificationNames.NOTIFICATION_LOGOUT);
	}
	
	public void onStackTop(boolean isBack) {
		if (this.isVadPreview() && detail != null && detail.isValidMessage()) {
			(new SharingFragment(detail, "myViewad")).show(getFragmentManager(), null);
			getArguments().putBoolean("autoShared", true);
		}
	}
	
	
	@Override
	public View onInitializeView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if(detail == null || mListLoader == null) return null;
		if(mListLoader.getGoodsList() == null 
				|| mListLoader.getGoodsList().getData() == null
				|| mListLoader.getGoodsList().getData().size() == 0){
			if(getActivity() != null){
				getActivity().sendBroadcast(new Intent(CommonIntentAction.ACTION_BROADCAST_MYAD_LOGOUT));
//				return null;
			}
		}
		final int originalSelect = getArguments().getInt("index", 0);
		this.keepSilent = false;//magic flag to refuse unexpected touch event
		
		final View v = inflater.inflate(R.layout.gooddetailview, null);
		
		pageController = new VadPageController(v, detail, this, originalSelect);
		        
        mListLoader.setSelection(originalSelect);
        mListLoader.setCallback(this);       
        
        return v;
	}
	
	private void notifyPageDataChange(boolean hasMore)
	{
		if(keepSilent) return;
		pageController.resetLoadingPage(hasMore);
	}
	
	private void updateContactBar(View rootView, boolean forceHide)
	{
		AdViewHistory.getInstance().markRead(detail.getValueByKey(EDATAKEYS.EDATAKEYS_ID));
		
		if (!detail.isValidMessage() && !forceHide)
		{
			String tips = detail.getValueByKey("tips"); 
			if(tips == null || tips.equals("")){
				tips  = "该信息不符合《百姓网公约》";
			}
			
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			AlertDialog dialog = builder.setTitle(R.string.dialog_title_info)
			.setMessage(tips)
			.setNegativeButton(R.string.delete, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					postDelete(true, new OnCancelListener() {
						
						@Override
						public void onCancel(DialogInterface dialog) {
							finishFragment();
						}
					});
				}
			})
			.setPositiveButton(R.string.appeal, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					VadLogger.trackMofifyEvent(detail, BxEvent.MYVIEWAD_APPEAL);
					Bundle bundle = createArguments("申诉", null);
					bundle.putInt("type", 1);
					bundle.putString("adId", detail.getValueByKey(EDATAKEYS.EDATAKEYS_ID));
					pushAndFinish(new FeedbackFragment(), bundle);
				}
			}).create();
			dialog.setCanceledOnTouchOutside(false);
			dialog.setOnCancelListener(new OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					finishFragment();
				}
			});
			dialog.show();
		}
		
		LinearLayout rl_phone = (LinearLayout)rootView.findViewById(R.id.phonelayout);
		if (forceHide)
		{
			rl_phone.setVisibility(View.GONE);
			return;
		}
		else if (isMyAd() || !detail.isValidMessage())
		{
			rootView.findViewById(R.id.phone_parent).setVisibility(View.GONE);
			rootView.findViewById(R.id.vad_tool_bar).setVisibility(View.VISIBLE);
			
			
			rootView.findViewById(R.id.vad_btn_edit).setOnClickListener(this);
			rootView.findViewById(R.id.vad_btn_refresh).setOnClickListener(this);
			rootView.findViewById(R.id.vad_btn_delete).setOnClickListener(this);
			rootView.findViewById(R.id.vad_btn_forward).setOnClickListener(this);
			
			if (!detail.isValidMessage())
			{
				rootView.findViewById(R.id.vad_btn_edit).setVisibility(View.GONE);
				rootView.findViewById(R.id.vad_btn_refresh).setVisibility(View.GONE);
			}
			return;
		}
		
		
		rootView.findViewById(R.id.phone_parent).setVisibility(View.VISIBLE);
		rootView.findViewById(R.id.vad_tool_bar).setVisibility(View.GONE);

		final String contactS = detail.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_CONTACT);
		final String mobileArea = detail.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_MOBILE_AREA);
		ViewGroup btnBuzz = (ViewGroup) rootView.findViewById(R.id.vad_buzz_btn);
		ImageView btnImg = (ImageView) btnBuzz.findViewById(R.id.vad_buzz_btn_img);
//		TextView btnTxt = (TextView) btnBuzz.findViewById(R.id.vad_buzz_btn_txt);
//		btnTxt.setTextColor(getResources().getColor(R.color.vad_sms));
		
		final boolean buzzEnable = TextUtil.isNumberSequence(contactS) && mobileArea != null && !"".equals(mobileArea) ? true : false;
		btnBuzz.setEnabled(buzzEnable);
		if (!buzzEnable)
		{
//			btnTxt.setTextColor(getResources().getColor(R.color.common_button_disable));
			btnImg.setImageBitmap(GlobalDataManager.getInstance().getImageManager().loadBitmapFromResource(R.drawable.icon_sms_disable));
		}
		
		rootView.findViewById(R.id.vad_buzz_btn).setOnClickListener(this);
		rl_phone.setVisibility(View.VISIBLE);

		//Enable or disable call button
		final boolean callEnable = TextUtil.isNumberSequence(contactS);
		rootView.findViewById(R.id.vad_call_btn).setEnabled(callEnable);
		rootView.findViewById(R.id.vad_call_btn).setOnClickListener(callEnable ? this : null);
		View callImg = rootView.findViewById(R.id.icon_call);
		callImg.setBackgroundResource(callEnable ? R.drawable.icon_call : R.drawable.icon_call_disable);
		TextView txtCall = (TextView) rootView.findViewById(R.id.txt_call);
		String text = "立即拨打" + contactS;
		if (mobileArea != null && mobileArea.length() > 0 && !GlobalDataManager.getInstance().getCityName().equals(mobileArea))
		{
//			text = contactS + "(" + mobileArea + ")";
		}
		else if (mobileArea == null || "".equals(mobileArea.trim()))
		{
//			text = contactS + "(非手机号)";
			ContextMenuItem opts = (ContextMenuItem) rootView.findViewById(R.id.vad_call_nonmobile);
			opts.updateOptionList("", getResources().getStringArray(R.array.item_call_nonmobile), 
					new int[] {R.id.vad_call_nonmobile + 1, R.id.vad_call_nonmobile + 2});
		}
		
		txtCall.setText(callEnable ? text : "无联系方式");
		txtCall.setTextColor(getResources().getColor(callEnable ? R.color.vad_call_btn_text : R.color.common_button_disable));
		
	}
	
	
	private boolean handleRightBtnIfInVerify(){
		if(!detail.getValueByKey("status").equals("0")){
			showSimpleProgress();
			executeModify(REQUEST_TYPE.REQUEST_TYPE_DELETE, 0);

			return true;	
		}
		return false;
	}
	
	private boolean isVadPreview() {
		return getArguments() != null && getArguments().getBoolean("isVadPreview", false) && !getArguments().getBoolean("autoShared", false);
	}
	
	public void handleRightAction() {
		this.finishFragment();
	}
	
	private void handleStoreBtnClicked(){
		if(handleRightBtnIfInVerify()) return;
		
		View parent = this.getView().findViewById(R.id.vad_title_fav_parent);
		int[] location = {0, 0};
		parent.getLocationInWindow(location);
		int width = parent.getWidth();
		int height = parent.getHeight();
		
		FavAndReportDialog menu = new FavAndReportDialog((BaseActivity)getActivity(), detail, handler);
		
		menu.show(location[0] + width - menu.getWindow().getAttributes().width - 5, height - 1);
	}
	
	class ManagerAlertDialog extends AlertDialog{
		public ManagerAlertDialog(Context context, int theme){
			super(context, theme);
		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.vad_title_fav_parent:
			handleStoreBtnClicked();
			break;
		case R.id.vad_call_btn:
		{
			VadLogger.trackContactEvent(BxEvent.VIEWAD_MOBILECALLCLICK, detail);
			
			final String mobileArea = detail.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_MOBILE_AREA);
			if (mobileArea == null || "".equals(mobileArea.trim()))
			{
				getView().findViewById(R.id.vad_call_nonmobile).performLongClick();
			}
			else
			{
				startContact(false);
			}
			
			break;
		}
		case R.id.vad_buzz_btn:
			startContact(true);
			break;
		case R.id.vad_btn_refresh:{
			showSimpleProgress();
			executeModify(REQUEST_TYPE.REQUEST_TYPE_REFRESH, 0);

			VadLogger.trackMofifyEvent(detail, BxEvent.MYVIEWAD_REFRESH);
			break;
		}
		case R.id.vad_btn_edit:{
			
			Bundle args = createArguments(null, null);
			args.putSerializable("goodsDetail", detail);
			args.putString("cateNames", detail.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_CATEGORYENGLISHNAME));
			pushFragment(new EditAdFragment(), args);
            VadLogger.trackMofifyEvent(detail, BxEvent.MYVIEWAD_EDIT);
			break;
		}
		case R.id.vad_btn_delete:{
			postDelete(true, null);
			break;
		}
		case R.id.vad_btn_forward:{
			//my viewad share
			(new SharingFragment(detail, "myViewad")).show(getFragmentManager(), null);
			break;
		}
		}
	}
	
	private void postDelete(boolean cancelable, OnCancelListener listener)
	{
		Builder builder = new AlertDialog.Builder(getActivity()).setTitle("提醒")
		.setMessage("是否确定删除")
		.setPositiveButton("确定", new DialogInterface.OnClickListener() {							
			@Override
			public void onClick(DialogInterface dialog, int which) {
				showSimpleProgress();
				executeModify(REQUEST_TYPE.REQUEST_TYPE_DELETE, 0);
				VadLogger.trackMofifyEvent(detail, BxEvent.MYVIEWAD_DELETE);
			}
		});
		
		if (cancelable)
		{
			builder = builder.setNegativeButton(
					"取消", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();							
						}
					});
		}
		
		AlertDialog dialog = builder.create();
		dialog.show();
		if (listener != null)
		{
			dialog.setOnCancelListener(listener);
		}
		
		dialog.show();
	}


	private boolean galleryReturned = true;
	
	@Override
	protected void onFragmentBackWithData(int requestCode, Object result){
		if(PostGoodsFragment.MSG_POST_SUCCEED == requestCode){
			this.finishFragment(requestCode, result);
		}else if(BigGalleryFragment.MSG_GALLERY_BACK == requestCode){
//			Log.d("haha", "hahaha,   from gallery back");
			galleryReturned = true;
		}else if(PostCommonValues.MSG_POST_EDIT_SUCCEED == requestCode){
			if(result != null){
				Ad newDetail = (Ad) result;
				detail = newDetail;
				try {
					AdList list = this.mListLoader.getGoodsList();
					List<Ad> dataList = list.getData();
					for (int i=0; i<dataList.size(); i++) {
						Ad ad = dataList.get(i);
						if (ad.getValueByKey(EDATAKEYS.EDATAKEYS_ID).equals(newDetail.getValueByKey(EDATAKEYS.EDATAKEYS_ID))) {
							dataList.add(i, newDetail);
							dataList.remove(i+1);
							break;
						}
					}
				} catch (Throwable t) {
					Log.d(TAG, "error when update ad in adlist. " + t.getMessage());
				} finally {
					this.notifyPageDataChange(false);
				}
			}
		}else if(MSG_LOGIN_TO_PROSECUTE == requestCode){
			(new AsyncTask<Ad, Integer, Boolean>(){
				@Override
				protected Boolean doInBackground(Ad... ads) {
					// TODO Auto-generated method stub
					ApiParams params = new ApiParams();
					params.addParam("adId", ads[0].getValueByKey(EDATAKEYS.EDATAKEYS_ID));
					params.addParam("mobile", GlobalDataManager.getInstance().getAccountManager().getCurrentUser().getPhone());
					String response = BaseApiCommand.createCommand("ad_reported", true, params).executeSync(getAppContext());
					try {
						JSONObject json = new JSONObject(response);
						JSONObject jsonErr = json.getJSONObject("error");
						if(jsonErr.getInt("code") == 0){
							return json.getBoolean("reported");
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return false;
				}
				
				@Override
				protected void onPostExecute(Boolean reported) {
					if(reported){
						ViewUtil.showToast(getAppContext(), "您已举报过该信息", false);
					}else{
						showProsecute();
					}
				}
			
			}).execute(detail);
		}
	}

	private void popRefresh(String message) {
		new AlertDialog.Builder(getActivity()).setTitle("提醒")
		.setMessage(message)
		.setPositiveButton("确定", new DialogInterface.OnClickListener() {							
			@Override
			public void onClick(DialogInterface dialog, int which) {
				showSimpleProgress();
				executeModify(REQUEST_TYPE.REQUEST_TYPE_REFRESH, 1);
				dialog.dismiss();
			}
		})
		.setNegativeButton(
	     "取消", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();							
			}
		})
	     .show();
	}
	
	@Override
	protected void handleMessage(Message msg, Activity activity, View rootView) {

		switch (msg.what) {
		case MSG_NETWORK_FAIL:
			ViewUtil.showToast(getActivity(), (String) msg.obj, true);
			break;
		case MSG_FINISH_FRAGMENT:
			finishFragment();
			break;
		case MSG_LOAD_AD_EVENT: {
			Pair<Integer, Object> data = (Pair<Integer, Object>)msg.obj;
			processEvent(data.first.intValue(), data.second);
			break;
		}
		case MSG_REFRESH_CONFIRM: {
			ApiError error = (ApiError) msg.obj;
			hideProgress();
			this.popRefresh(error.getMsg());
			break;
		}
		case MSG_REFRESH:
			if(json == null){
				ViewUtil.showToast(activity, "刷新失败，请稍后重试！", false);
				break;
			}
			try {
				JSONObject jb = new JSONObject(json);
				JSONObject js = jb.getJSONObject("error");
				String message = js.getString("message");
				int code = js.getInt("code");
				if (code == 0) {
					executeModify(REQUEST_TYPE.REQUEST_TYPE_UPDATE, 0);
					ViewUtil.showToast(getActivity(), message, false);
				}else if(2 == code){
					hideProgress();
					popRefresh(message);
				}else {
					hideProgress();
					ViewUtil.showToast(getActivity(), message, false);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;			
		case MSG_UPDATE:
			hideProgress();
			AdList goods = JsonUtil.getGoodsListFromJson(json);
			List<Ad> goodsDetails = goods.getData();
			if(goodsDetails != null && goodsDetails.size() > 0){
				for(int i = 0; i < goodsDetails.size(); ++ i){
					if(goodsDetails.get(i).getValueByKey(Ad.EDATAKEYS.EDATAKEYS_ID)
							.equals(detail.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_ID))){
						detail = goodsDetails.get(i);
						break;
					}
				}
				List<Ad>listMyPost = GlobalDataManager.getInstance().getListMyPost();
				if(listMyPost != null){
					for(int i = 0; i < listMyPost.size(); ++ i){
						if(listMyPost.get(i).getValueByKey(Ad.EDATAKEYS.EDATAKEYS_ID)
								.equals(detail.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_ID))){
							listMyPost.set(i, detail);
							break;
						}
					}
				}
				//QuanleimuApplication.getApplication().setListMyPost(listMyPost);
			}

//			setMetaObject(); FIXME: should update current UI.
			break;
		case MSG_DELETE:
			hideProgress();
			try {
				JSONObject jb = new JSONObject(json);
				JSONObject js = jb.getJSONObject("error");
				String message = js.getString("message");
				int code = js.getInt("code");
				if (code == 0) {
					if(detail.getValueByKey("status").equals("0")){
						List<Ad> listMyPost = GlobalDataManager.getInstance().getListMyPost();
						if(null != listMyPost){
							for(int i = 0; i < listMyPost.size(); ++ i){
								if(listMyPost.get(i).getValueByKey(Ad.EDATAKEYS.EDATAKEYS_ID)
										.equals(detail.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_ID))){
									listMyPost.remove(i);
									break;
								}
							}
						}
						finishFragment(MSG_MYPOST_DELETED, null);
					}
					else{
						finishFragment(MSG_ADINVERIFY_DELETED, detail.getValueByKey(EDATAKEYS.EDATAKEYS_ID));
					}
//					finish();
					ViewUtil.showToast(activity, message, false);
				} else {
					// 删除失败
					ViewUtil.showToast(activity, "删除失败,请稍后重试！", false);
					finishFragment();
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case FavAndReportDialog.MSG_PROSECUTE:
			UserBean user = GlobalDataManager.getInstance().getAccountManager().getCurrentUser();
			Bundle arg = this.createArguments(null, null);
			arg.putInt(LoginFragment.KEY_RETURN_CODE, MSG_LOGIN_TO_PROSECUTE);
			if(user == null || TextUtils.isEmpty(user.getPhone())){
				pushFragment(new LoginFragment(), arg);
			}else{
				showProsecute();
			}
			break;
		default:
			break;
		}
	
	}
	
	private void showProsecute(){
		Bundle bundle = new Bundle();
		bundle.putInt("type", 0);
		bundle.putString("adId", this.detail.getValueByKey(EDATAKEYS.EDATAKEYS_ID));
		bundle.putString(ARG_COMMON_TITLE, "举报");
		pushFragment(new FeedbackFragment(), bundle);		
	}
	
	private void executeModify(REQUEST_TYPE request, int pay) {
		json = "";
		
		ApiParams params = new ApiParams();
		UserBean user = (UserBean) Util.loadDataFromLocate(this.getActivity(), "user", UserBean.class);
		if(user != null && user.getPhone() != null && !user.getPhone().equals("")){
			params.appendAuthInfo(user.getPhone(), user.getPassword());//(user);
		}
		params.addParam("rt", 1);
		
		switch(request) {
		case REQUEST_TYPE_DELETE:
			params.addParam("adId", detail.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_ID));
			break;
		case REQUEST_TYPE_REFRESH:
			params.addParam("adId", detail.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_ID));
			if(pay != 0){
				params.addParam("pay", 1);
			}
			break;
		case REQUEST_TYPE_UPDATE:
			params.addParam("query", "id:" + detail.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_ID));
		}
			
		BaseApiCommand cmd = BaseApiCommand.createCommand(request.apiName, false, params);
		cmd.execute(getActivity(), this);
	}
	
	@Override
	public void initTitle(TitleDef title){
		title.m_leftActionHint = isVadPreview() ? "" : "返回";
		title.m_rightActionHint = isVadPreview() ? "完成" : "";
		if(this.mListLoader != null && mListLoader.getGoodsList() != null && mListLoader.getGoodsList().getData() != null){
			title.m_title = ( this.mListLoader.getSelection() + 1 ) + "/" + 
					this.mListLoader.getGoodsList().getData().size();	
		}
		title.m_visible = true;
		
		title.m_leftActionImage = getArguments() != null && "close".equalsIgnoreCase(getArguments().getString(ARG_COMMON_BACK_HINT)) ? R.drawable.icon_close : R.drawable.icon_back;
		
		if(!isMyAd()){
			LayoutInflater inflater = LayoutInflater.from(this.getActivity());
			title.m_titleControls = inflater.inflate(R.layout.vad_title, null); 
			title.m_titleControls.findViewById(R.id.vad_title_fav_parent).setOnClickListener(this);
		}

		if(detail != null){
			if(!isMyAd()){
				TextView viewTimes = (TextView) getTitleDef().m_titleControls.findViewById(R.id.vad_viewed_time);
				viewTimes.setText(detail.getValueByKey("count") + "次查看");
			}else{
				title.m_title = detail.getValueByKey("count") + "次查看";
			}			
		}

		updateTitleBar(title);
		
	
	}
	
	private void updateTitleBar(TitleDef title)
	{
		
		if(isMyAd() || !detail.isValidMessage()){
			if(title.m_titleControls != null){
				title.m_titleControls.findViewById(R.id.vad_title_fav_parent).setVisibility(View.INVISIBLE);
			}
		}
		else{
			if(title.m_titleControls != null){
				title.m_titleControls.findViewById(R.id.vad_title_fav_parent).setVisibility(View.VISIBLE);
			}
		}
	}
	
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
    	if (parent.getAdapter() instanceof VadImageAdapter)
    	{
    		VadImageAdapter mainAdapter = (VadImageAdapter) parent.getAdapter();
    		
    		List<String> listUrl = mainAdapter.getImages();
    		ArrayList<String> urls = new ArrayList<String>();
    		urls.add(listUrl.get(position));
    		for(int index = 0; (index + position < listUrl.size() || position - index >= 0); ++index){
    			if(index + position < listUrl.size())
    				urls.add(listUrl.get(index+position));
    			
    			if(position - index >= 0)
    				urls.add(listUrl.get(position-index));				
    		}
    		GlobalDataManager.getInstance().getImageLoaderMgr().AdjustPriority(urls);
    	}
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0)
    {
        // TODO Auto-generated method stub
    }	


	@Override
	public void onHasMoreStatusChanged() {
	}
	
	public void setListHolder(IListHolder holder)
	{
		this.mHolder = holder;
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem menuItem) {
		
		switch (menuItem.getItemId())
		{
			case R.id.vad_call_nonmobile + 1: {
				startContact(false);
				return true;
			}
			case R.id.vad_call_nonmobile + 2: {
				ClipboardManager clipboard = (ClipboardManager)
				        getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
				clipboard.setText(detail.getValueByKey(EDATAKEYS.EDATAKEYS_CONTACT));
				ViewUtil.postShortToastMessage(getView(), R.string.tip_clipd_contact, 0);
				return true;
			}
		}
		
		return super.onContextItemSelected(menuItem);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == 100){
			this.handleStoreBtnClicked();
		}
	}
	
	private void startContact(boolean sms)
	{
		if (sms){//右下角发短信
			VadLogger.trackContactEvent(BxEvent.VIEWAD_SMS, detail);
		}
			
		String contact = detail.getValueByKey(EDATAKEYS.EDATAKEYS_CONTACT);
		if (contact != null)
		{
			Intent intent = new Intent(
					sms ? Intent.ACTION_SENDTO : Intent.ACTION_DIAL,
					Uri.parse((sms ? "smsto:" : "tel:") + contact));
			if (sms) {
				intent.putExtra("sms_body", "你好，我在百姓网看到你发的\"" + detail.getValueByKey(EDATAKEYS.EDATAKEYS_TITLE) + "\"，");
			}
			
			List<ResolveInfo> ls = getActivity().getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
			if (ls != null && ls.size() > 0)
			{
				if(sms){
					startActivity(intent);
				}else{
					this.startActivityForResult(intent, 100);
				}
			}
			else
			{
				ViewUtil.postShortToastMessage(getView(), sms ? R.string.warning_no_sms_app_install : R.string.warning_no_phone_app_install, 0);
			}
		}
	}
	
	public void showMap() {
		VadLogger.trackShowMapEvent(detail);
		if (keepSilent) { // FIXME:
			ViewUtil.showToast(getActivity(), "当前无法显示地图", false);
			return;
		}
		else
		{
			ViewUtil.startMapForAds(getActivity(), detail);
		}
					
	}
	
	public boolean hasGlobalTab()
	{
		return false;
	}

	@Override
	public void onRequestComplete(int respCode, Object data) {
		sendMessage(MSG_LOAD_AD_EVENT, Pair.create(respCode, data));
	}
	
	private void processEvent(int respCode, Object data) {


		if(null != mHolder){
			if(mHolder.onResult(respCode, mListLoader)){
//				onGotMore();
				pageController.loadMoreSucced();
			}else{
				onNoMore();
			}
			
			if(respCode == ErrorHandler.ERROR_NETWORK_UNAVAILABLE){
				pageController.loadMoreFail();
			}
		}else{
			switch (respCode) {
			case VadListLoader.MSG_FINISH_GET_FIRST:				 
				AdList goodsList = JsonUtil.getGoodsListFromJson(mListLoader.getLastJson());
				mListLoader.setGoodsList(goodsList);
				if (goodsList == null || goodsList.getData().size() == 0) {
					ErrorHandler.getInstance().handleError(ErrorHandler.ERROR_COMMON_FAILURE, "没有符合的结果，请稍后并重试！");
				} else {
					//QuanleimuApplication.getApplication().setListGoods(goodsList.getData());
				}
				mListLoader.setHasMore(true);
				notifyPageDataChange(true);
				break;
			case VadListLoader.MSG_NO_MORE:					
				onNoMore();
				
				mListLoader.setHasMore(false);
				notifyPageDataChange(false);
				
				break;
			case VadListLoader.MSG_FINISH_GET_MORE:	
				AdList goodsList1 = JsonUtil.getGoodsListFromJson(mListLoader.getLastJson());
				if (goodsList1 == null || goodsList1.getData().size() == 0) {
					ErrorHandler.getInstance().handleError(ErrorHandler.ERROR_COMMON_WARNING, "后面没有啦！");
					
					onNoMore();
					
					mListLoader.setHasMore(false);
					notifyPageDataChange(false);
				} else {
					List<Ad> listCommonGoods =  goodsList1.getData();
					for(int i=0;i<listCommonGoods.size();i++)
					{
						mListLoader.getGoodsList().getData().add(listCommonGoods.get(i));
					}
					//QuanleimuApplication.getApplication().setListGoods(mListLoader.getGoodsList().getData());	
					
					mListLoader.setHasMore(true);
					notifyPageDataChange(true);
					pageController.loadMoreSucced();
				}
				break;
			case ErrorHandler.ERROR_NETWORK_UNAVAILABLE:
				ErrorHandler.getInstance().handleError(ErrorHandler.ERROR_NETWORK_UNAVAILABLE, null);
				pageController.loadMoreFail();
				
				break;
			}
		}
	
	}
	
	private void onNoMore() {
		View root = getView();
		if (root != null)
		{
			ViewUtil.showToast(getActivity(), "后面没有啦！", false);
		}
	}

	@Override
	public int totalPages() {
			if(mListLoader == null || mListLoader.getGoodsList() == null || mListLoader.getGoodsList().getData() == null){
			return 0;
		}
		return mListLoader.getGoodsList().getData().size();//+ (mListLoader.hasMore() ? 1 : 0);
		
	}

	@Override
	public Ad getAd(int pos) {
		if(mListLoader == null 
				|| mListLoader.getGoodsList() == null
				|| mListLoader.getGoodsList().getData() == null
				|| mListLoader.getGoodsList().getData().size() <= pos){
			return null;
		}
		return mListLoader.getGoodsList().getData().get(pos);
	}

	@Override
	public void onLoadMore() {
		if (null != mHolder) {
			mHolder.startFecthingMore();
		} else {
			mListLoader
					.startFetching(getAppContext(),
							false,
							(VadListLoader.E_LISTDATA_STATUS.E_LISTDATA_STATUS_ONLINE != mListLoader
									.getDataStatus()));
		}
	}

	@Override
	public void onPageSwitchTo(int pos) {
		keepSilent = false;//magic flag to refuse unexpected touch event
		//tracker
		VadLogger.trackPageView(detail, VadFragment.this.getAppContext());
		
		if (pos != totalPages())
		{
			detail = mListLoader.getGoodsList().getData().get(pos);
			mListLoader.setSelection(pos);
			updateTitleBar(getTitleDef());
			updateContactBar(getView(), false);
		}
		else
		{
			updateTitleBar(getTitleDef());
			updateContactBar(getView(), true);
		}
	}

	@Override
	public void onRequestBigPic(int pos, Ad detail) {
		if(galleryReturned){
			Bundle bundle = createArguments(null, null);
			bundle.putInt("postIndex", pos);
			bundle.putSerializable("goodsDetail", detail);
			galleryReturned = false;
			pushFragment(new BigGalleryFragment(), bundle);		
		}
	}

	@Override
	public void onRequestMap() {
		showMap();
	}
	
	public void onRequestUserAd(int userId, String userNick) {
		Bundle args = createArguments(null, null);
		args.putInt("userId", userId);
		args.putString("userNick", userNick);
		args.putString("secondCategoryName", detail.getValueByKey(EDATAKEYS.EDATAKEYS_CATEGORYENGLISHNAME));
		args.putString("adId", detail.getValueByKey(EDATAKEYS.EDATAKEYS_ID));

		pushFragment(new UserAdFragment(), args);
	}

	@Override
	public void onPageInitDone(ViewPager pager, final int pageIndex) {
		final ViewPager vp = pager != null ? pager : (ViewPager) getActivity().findViewById(R.id.svDetail);
		if (vp != null && pageIndex == vp.getCurrentItem())
		{
			updateTitleBar(getTitleDef());
			updateContactBar(vp.getRootView(), false);
		}		
	}

	@Override
	public boolean hasMore() {
		return mListLoader == null ? false : mListLoader.hasMore();
	}

	@Override
	public void onNetworkDone(String apiName, String responseData) {
		json = responseData;
		int msgId = "ad_refresh".equals(apiName) ? MSG_REFRESH : ("ad_delete".equals(apiName) ? MSG_DELETE : MSG_UPDATE);
		sendMessage(msgId, null);
	}

	@Override
	public void onNetworkFail(String apiName, ApiError error) {
		if ("ad_refresh".equals(apiName) && "2".equals(error.getErrorCode())) {
			sendMessage(MSG_REFRESH_CONFIRM, error);
		} else {
//			ErrorHandler.getInstance().handleError(ErrorHandler.ERROR_NETWORK_UNAVAILABLE, null);
			hideProgress();
			
			this.sendMessage(MSG_NETWORK_FAIL, error.getMsg());
			if ("ad_delete".equals(apiName) && detail != null && !detail.isValidMessage()) {
				sendMessage(MSG_FINISH_FRAGMENT, null);
			}
		}
		
	}
	
	@Override
	public void update(Observable observable, Object data) {
		// TODO Auto-generated method stub
		if (data instanceof IBxNotification){
			IBxNotification note = (IBxNotification) data;
			if (IBxNotificationNames.NOTIFICATION_LOGOUT.equals(note.getName())){
				View edit = getView() == null ? null : getView().findViewById(R.id.vad_tool_bar);
				if(edit != null){
					int visibility = edit.getVisibility();
					if(visibility == View.VISIBLE){
						finishFragment();
						getActivity().sendBroadcast(new Intent(CommonIntentAction.ACTION_BROADCAST_MYAD_LOGOUT));
					}else{
						getActivity().sendBroadcast(new Intent(CommonIntentAction.ACTION_BROADCAST_COMMON_AD_LOGOUT));
					}
				}
			}
		}
	}	
}
