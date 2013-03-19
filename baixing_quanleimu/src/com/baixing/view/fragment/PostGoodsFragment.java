//xumengyi@baixing.com
package com.baixing.view.fragment;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.text.InputFilter;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.baixing.activity.BaseActivity;
import com.baixing.activity.BaseFragment;
import com.baixing.activity.PersonalActivity;
import com.baixing.broadcast.CommonIntentAction;
import com.baixing.data.GlobalDataManager;
import com.baixing.entity.AdList;
import com.baixing.entity.BXLocation;
import com.baixing.entity.BXThumbnail;
import com.baixing.entity.PostGoodsBean;
import com.baixing.entity.UserBean;
import com.baixing.imageCache.ImageCacheManager;
import com.baixing.jsonutil.JsonUtil;
import com.baixing.network.api.ApiError;
import com.baixing.network.api.ApiParams;
import com.baixing.network.api.BaseApiCommand;
import com.baixing.tracking.TrackConfig.TrackMobile.BxEvent;
import com.baixing.tracking.TrackConfig.TrackMobile.Key;
import com.baixing.tracking.TrackConfig.TrackMobile.PV;
import com.baixing.tracking.Tracker;
import com.baixing.util.ErrorHandler;
import com.baixing.util.PerformEvent.Event;
import com.baixing.util.PerformanceTracker;
import com.baixing.util.Util;
import com.baixing.util.VadListLoader;
import com.baixing.util.ViewUtil;
import com.baixing.util.post.ImageUploader;
import com.baixing.util.post.ImageUploader.Callback;
import com.baixing.util.post.PostCommonValues;
import com.baixing.util.post.PostLocationService;
import com.baixing.util.post.PostNetworkService;
import com.baixing.util.post.PostNetworkService.PostResultData;
import com.baixing.util.post.PostUtil;
import com.baixing.widget.CustomDialogBuilder;
import com.quanleimu.activity.R;
import com.tencent.mm.sdk.platformtools.Log;

public class PostGoodsFragment extends BaseFragment implements OnClickListener, Callback{
	
	private static final int IMG_STATE_UPLOADING = 1;
	private static final int IMG_STATE_UPLOADED = 2;
	private static final int IMG_STATE_FAIL = 3;
	private static final int MSG_GEOCODING_TIMEOUT = 0x00010011;
	static final public String KEY_INIT_CATEGORY = "cateNames";
	static final String KEY_LAST_POST_CONTACT_USER = "lastPostContactIsRegisteredUser";
	static final String KEY_IS_EDITPOST = "isEditPost"; 
	static final String KEY_CATE_ENGLISHNAME = "cateEnglishName";
	static final private String KEY_IMG_BUNDLE = "key_image_bundle";
	static final private String FILE_LAST_CATEGORY = "lastCategory";
	static final int MSG_POST_SUCCEED = 0xF0000010; 
	protected String categoryEnglishName = "";
	private String categoryName = "";
	protected LinearLayout layout_txt;
	private LinkedHashMap<String, PostGoodsBean> postList = new LinkedHashMap<String, PostGoodsBean>();
	private static final int NONE = 0;
	private static final int PHOTORESOULT = 3;
	private static final int MSG_CATEGORY_SEL_BACK = 11;
	private static final int MSG_DIALOG_BACK_WITH_DATA = 12;
	private static final int MSG_UPDATE_IMAGE_LIST = 13;
	private static final int MSG_IMAGE_STATE_CHANGE = 14;
	private static final int MSG_GET_AD_FAIL = 15;
	private static final int MSG_GET_AD_SUCCED = 16;
	protected PostParamsHolder params = new PostParamsHolder();
	protected boolean editMode = false;
//	protected ArrayList<String> listUrl = new ArrayList<String>();
	protected Bundle imgSelBundle = null;
	private View locationView = null;
	private BXLocation detailLocation = null;
    protected List<String> bmpUrls = new ArrayList<String>();
    private EditText etDescription = null;
    private EditText etContact = null;
    private PostLocationService postLBS;
    private PostNetworkService postNS;
    
    protected ArrayList<String> photoList = new ArrayList<String>();
//    private Bitmap firstImage = null;
    protected boolean isNewPost = true;
    private boolean finishRightNow = false;
    
    @Override
	public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		if (resultCode == NONE) {
			return;
		} else if (resultCode == Activity.RESULT_FIRST_USER) {
			finishRightNow = true;
			return;
		}
		
		if (resultCode == Activity.RESULT_OK) {
			photoList.clear();
			if (data.getExtras().containsKey(CommonIntentAction.EXTRA_IMAGE_LIST)){
				ArrayList<String> result = data.getStringArrayListExtra(CommonIntentAction.EXTRA_IMAGE_LIST);
				photoList.addAll(result);
			}
			
//			if (photoList != null && photoList.size() > 0) {
//				firstImage = ImageUploader.getInstance().getThumbnail(photoList.get(0));
//				for(int i = 0; i < photoList.size(); ++ i){
//					ImageUploader.getInstance().registerCallback(photoList.get(i), this);
//				}				
//			}
//			else {
//				firstImage = null;
//			}
		}
		
		handler.sendEmptyMessage(MSG_UPDATE_IMAGE_LIST);
    }
    
    private void initWithCategoryNames(String categoryNames) {
    	if(categoryNames == null || categoryNames.length() == 0){
			categoryNames = (String)Util.loadDataFromLocate(this.getActivity(), FILE_LAST_CATEGORY, String.class);
		}
		if(categoryNames != null && !categoryNames.equals("")){
			String[] names = categoryNames.split(",");
			if(names.length == 2){
				this.categoryEnglishName = names[0];
				this.categoryName = names[1];
				
			}else if(names.length == 1){
				this.categoryEnglishName = names[0];
			}
			Util.saveDataToLocate(this.getActivity(), FILE_LAST_CATEGORY, categoryNames);
		}
    }
    
	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		PerformanceTracker.stamp(Event.E_PGFrag_OnCreate_Start);
		super.onCreate(savedInstanceState);
		
		if (savedInstanceState != null) {
			isNewPost = false;
		} else {
			isNewPost = !editMode;
		}
		
		String categoryNames = this.getArguments().getString(KEY_INIT_CATEGORY);
		initWithCategoryNames(categoryNames);
				
		if (savedInstanceState != null){
			postList.putAll( (HashMap<String, PostGoodsBean>)savedInstanceState.getSerializable("postList"));
			params = (PostParamsHolder) savedInstanceState.getSerializable("params");
//			listUrl.addAll((List<String>) savedInstanceState.getSerializable("listUrl"));
			photoList.addAll((List<String>) savedInstanceState.getSerializable("listUrl"));
			imgHeight = savedInstanceState.getInt("imgHeight");
			imgSelBundle = savedInstanceState.getBundle(KEY_IMG_BUNDLE);
		}
		
		if(imgSelBundle == null){
			imgSelBundle =  new Bundle();
		}

//		if(imgSelDlg == null){ //FIXME: remove 
//			imgSelDlg = new ImageSelectionDialog(imgSelBundle);
//			imgSelDlg.setMsgOutHandler(handler);
//		}
		
		String appPhone = GlobalDataManager.getInstance().getPhoneNumber();
		if(!editMode && (appPhone == null || appPhone.length() == 0)){
			UserBean user = GlobalDataManager.getInstance().getAccountManager().getCurrentUser();
			if(user != null && user.getPhone() != null && !user.getPhone().equals("")){
				String mobile = user.getPhone();
				GlobalDataManager.getInstance().setPhoneNumber(mobile);
			}
		}
		
		this.postLBS = new PostLocationService(this.handler);
		postNS =  new PostNetworkService(handler);
	}
		
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		PostUtil.extractInputData(layout_txt, params);
		outState.putSerializable("params", params);
		outState.putSerializable("postList", postList);
		outState.putSerializable("listUrl", photoList);
		outState.putInt("imgHeight", imgHeight);
		outState.putBundle(KEY_IMG_BUNDLE, imgSelBundle);
	}
	
	private void doClearUpImages() {
		//Clear the upload image list.
		this.photoList.clear();
//		this.firstImage = null;
		ImageUploader.getInstance().clearAll();
	}

	@Override
	public boolean handleBack() {
//		if(imgSelDlg != null)
//			if(imgSelDlg.handleBack()){
//				return true;
//		}		
//		return super.handleBack();
		AlertDialog.Builder builder = new Builder(getActivity());
		builder.setMessage("退出发布？");
		builder.setNegativeButton("否", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//Do nothing.
			}
		});
		builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				doClearUpImages();
				finishFragment();
			}
		});
		builder.create().show();
		
		return true;
	}	

	@Override
	public void onResume() {
		super.onResume();
		isActive = true;
		postLBS.start();
		//Disable on version 3.2.1
		if(!editMode /*&& !isNewPost && !finishRightNow*/) { //isNewPost==true ==> will show camera immediately, no PV; finishRightNow==true ==> cancel post on camera screen, no PV.
			this.pv = PV.POST;
			Tracker.getInstance()
			.pv(this.pv)
			.append(Key.SECONDCATENAME, categoryEnglishName)
			.end();
		}	
		
		if (finishRightNow) {
			finishRightNow = false;
			doClearUpImages();
			finishFragment();
		}
	}
	
	private boolean isActive = false;
	@Override
	public void onPause() {
		postLBS.stop();
		PostUtil.extractInputData(layout_txt, params);
		setPhoneAndAddress();
		isActive = false;
		super.onPause();
	}

	@Override
	public void onStackTop(boolean isBack) {
		if(isBack){
			final ScrollView scroll = (ScrollView) this.getView().findViewById(R.id.goodscontent);
			scroll.post(new Runnable() {            
			    @Override
			    public void run() {
			           scroll.fullScroll(View.FOCUS_DOWN);              
			    }
			});
		}
		
		if (isNewPost) {
			isNewPost = false;
			this.startImgSelDlg(Activity.RESULT_FIRST_USER, "跳过\n拍照");
		} else {
			
		}
		
	}	

	@Override
	public void onViewCreated (View view, Bundle savedInstanceState){
		super.onViewCreated(view, savedInstanceState);
		showPost();
	}

	@Override
	public View onInitializeView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {		
		ViewGroup v = (ViewGroup) inflater.inflate(R.layout.postgoodsview, null);		
		layout_txt = (LinearLayout) v.findViewById(R.id.layout_txt);		
		Button button = (Button) v.findViewById(R.id.iv_post_finish);
		button.setOnClickListener(this);
		if (!editMode)
			button.setText("立即免费发布");
		else
			button.setText("立即更新信息");
		return v;
	}
	
	protected void startImgSelDlg(final int cancelResultCode, String finishActionLabel){
//		if(container != null){
//			imgSelBundle.putSerializable(ImageSelectionDialog.KEY_IMG_CONTAINER, container);
//		}
//		imgSelDlg.setMsgOutBundle(imgSelBundle);
//		imgSelDlg.show(getFragmentManager(), null);
		PerformanceTracker.stamp(Event.E_Send_Camera_Bootup);
		Intent backIntent = new Intent();
		backIntent.setClass(getActivity(), getActivity().getClass());
		
		Intent goIntent = new Intent();
		goIntent.putExtra(CommonIntentAction.EXTRA_COMMON_INTENT, backIntent);
		goIntent.setAction(CommonIntentAction.ACTION_IMAGE_CAPTURE);
		goIntent.putExtra(CommonIntentAction.EXTRA_COMMON_REQUST_CODE, CommonIntentAction.PhotoReqCode.PHOTOHRAPH);
		goIntent.putStringArrayListExtra(CommonIntentAction.EXTRA_IMAGE_LIST, this.photoList);
		goIntent.putExtra(CommonIntentAction.EXTRA_FINISH_ACTION_LABEL, finishActionLabel);
		goIntent.putExtra(CommonIntentAction.EXTRA_COMMON_FINISH_CODE, cancelResultCode);
//		BXLocation loc = GlobalDataManager.getInstance().getLocationManager().getCurrentPosition(true); 
//		if (loc != null) {
//			goIntent.putExtra("location", loc);
//		}
//		getActivity().startActivity(goIntent);
		getActivity().startActivityForResult(goIntent, 0);
	}

	private void deployDefaultLayout(){
		addCategoryItem();
		HashMap<String, PostGoodsBean> pl = new HashMap<String, PostGoodsBean>();
		for(int i = 1; i < PostCommonValues.fixedItemNames.length; ++ i){
			PostGoodsBean bean = new PostGoodsBean();
			bean.setControlType("input");
			bean.setDisplayName(PostCommonValues.fixedItemDisplayNames[i]);
			bean.setName(PostCommonValues.fixedItemNames[i]);
			bean.setUnit("");
			if(PostCommonValues.fixedItemNames[i].equals("价格")){
				bean.setNumeric(1);//.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
				bean.setUnit("元");
			}
			pl.put(PostCommonValues.fixedItemNames[i], bean);
		}
		buildPostLayout(pl);
	}
	
	public void updateNewCategoryLayout(String cateNames){
		if(cateNames == null) return;
		String[] names = cateNames.split(",");
		if(names != null){
			if(categoryEnglishName.equals(names[0])) return;
		}
		initWithCategoryNames(cateNames);
		resetData(false);
		Util.saveDataToLocate(getActivity(), FILE_LAST_CATEGORY, cateNames);
		this.showPost();
	}
	
	protected String getCityEnglishName(){
		return GlobalDataManager.getInstance().getCityEnglishName();
	}
	
	private void showGettingMetaProgress(boolean show){
		for(int i = 0; i < layout_txt.getChildCount(); ++ i){
			View v = layout_txt.getChildAt(i);
			if(v == null) continue;
			View progress = v.findViewById(R.id.metaLoadingBar);
			if(progress != null){
				if(show){
					progress.setVisibility(View.VISIBLE);
					v.findViewById(R.id.post_next).setVisibility(View.GONE);
				}else{
					progress.setVisibility(View.GONE);
					v.findViewById(R.id.post_next).setVisibility(View.VISIBLE);					
				}				
			}
		}
	}
	
	private void showPost(){
		if(this.categoryEnglishName == null || categoryEnglishName.length() == 0){
			deployDefaultLayout();
			return;
		}

		String cityEnglishName = getCityEnglishName();		
		Pair<Long, String> pair = Util.loadJsonAndTimestampFromLocate(this.getActivity(), categoryEnglishName + cityEnglishName);
		String json = pair.second;
		if (json != null && json.length() > 0) {			
			if (pair.first + (24 * 3600) >= System.currentTimeMillis()/1000) {
				if(postList == null || postList.size() == 0){
					postList = JsonUtil.getPostGoodsBean(json);
				}
				addCategoryItem();
				buildPostLayout(postList);
				loadCachedData();
				return;
			}
		}
//		showSimpleProgress();
		showGettingMetaProgress(true);
		postNS.retreiveMetaAsync(cityEnglishName, categoryEnglishName);
	}
	
	@Override
	public void onClick(final View v) {
		switch(v.getId()){
		case R.id.delete_btn:
			final String img = (String) v.getTag();
			
			this.showAlert(null, "是否删除该照片", new DialogAction(R.string.yes) {
				public void doAction() {
					ImageUploader.getInstance().cancel(img);
					if (photoList.remove(img)) {
						ViewGroup parent = (ViewGroup) getView().findViewById(R.id.image_list_parent);
						View v = findImageViewByTag(img);
						if (v != null) parent.removeView(v); 
						showAddImageButton(parent, LayoutInflater.from(v.getContext()), true);
					}
				}
			}, null);
			
			break;
		case R.id.iv_post_finish:
			Tracker.getInstance()
			.event(!editMode ? BxEvent.POST_POSTBTNCONTENTCLICKED:BxEvent.EDITPOST_POSTBTNCONTENTCLICKED)
			.append(Key.SECONDCATENAME, categoryEnglishName).end();			
			this.postAction();
			break;
		case R.id.location:
			Tracker.getInstance().event((!editMode)?BxEvent.POST_INPUTING:BxEvent.EDITPOST_INPUTING).append(Key.ACTION, PostCommonValues.STRING_DETAIL_POSITION).end();			
			if(this.detailLocation != null && locationView != null){
				setDetailLocationControl(detailLocation);
			}else if(detailLocation == null){
				ViewUtil.showToast(this.getActivity(), "无法获得当前位置", false);
			}
			break;
//		case R.id.myImg:
		case R.id.add_post_image:
			Tracker.getInstance().event((!editMode)?BxEvent.POST_INPUTING:BxEvent.EDITPOST_INPUTING).append(Key.ACTION, "image").end();
			
//			if(!editMode){
				startImgSelDlg(Activity.RESULT_CANCELED, "完成");
//			}
				break;
		case R.id.img_description:
			final View et = v.findViewById(R.id.description_input);
			if(et != null){
				et.postDelayed(new Runnable(){
					@Override
					public void run(){
						if (et != null){
							Tracker.getInstance().event((!editMode)?BxEvent.POST_INPUTING:BxEvent.EDITPOST_INPUTING).append(Key.ACTION, PostCommonValues.STRING_DESCRIPTION).end();
							et.requestFocus();
							InputMethodManager inputMgr = (InputMethodManager) et.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
							inputMgr.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);
						}
					}			
				}, 100);
			}
			break;
		case R.id.postinputlayout:
			final View et2 = v.findViewById(R.id.postinput);
			if(et2 != null){
				et2.postDelayed(new Runnable(){
					@Override
					public void run(){
						if (et2 != null){
							et2.requestFocus();
							InputMethodManager inputMgr = (InputMethodManager) et2.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
							inputMgr.showSoftInput(et2, InputMethodManager.SHOW_IMPLICIT);
						}
					}			
				}, 100);
			}			
			break;
		}
	}
	
	private void setPhoneAndAddress(){
		String phone = params.getData("contact");
		if(phone != null && phone.length() > 0 && !editMode){
			GlobalDataManager.getInstance().setPhoneNumber(phone);
		}
		String address = params.getData(PostCommonValues.STRING_DETAIL_POSITION);
		if(address != null && address.length() > 0){
			GlobalDataManager.getInstance().setAddress(address);
		}		
	}
	
	private void postAction() {
		if((this.postList == null || postList.size() == 0) 
				&& ((this.categoryEnglishName != null && categoryEnglishName.length() > 0)
						|| (this.categoryName != null && categoryName.length() > 0))){
			return;
		}
		PostUtil.extractInputData(layout_txt, params);
		setPhoneAndAddress();
		if(!this.checkInputComplete()){
			return;
		}
		PerformanceTracker.stamp(Event.E_Start_PostAction);
		String detailLocationValue = params.getUiData(PostCommonValues.STRING_DETAIL_POSITION);
		if(this.detailLocation != null && (detailLocationValue == null || detailLocationValue.length() == 0)){
			showProgress(R.string.dialog_title_info, R.string.dialog_message_waiting, false);
			PerformanceTracker.stamp(Event.E_PostAction_Direct_Start);
			postAd(detailLocation);
		}else{
			this.sendMessageDelay(MSG_GEOCODING_TIMEOUT, null, 5000);
			showProgress(R.string.dialog_title_info, R.string.dialog_message_waiting, false);
			PerformanceTracker.stamp(Event.E_PostAction_GetLocation_Start);
			postLBS.retreiveLocation(GlobalDataManager.getInstance().cityName, getFilledLocation());			
		}
	}

	private boolean checkInputComplete() {
		if(this.categoryEnglishName == null || this.categoryEnglishName.equals("")){
			ViewUtil.showToast(this.getActivity(), "请选择分类", false);
			popupCategorySelectionDialog();
			return false;
		}
		
		for (int i = 0; i < postList.size(); i++) {
			String key = (String) postList.keySet().toArray()[i];
			PostGoodsBean postGoodsBean = postList.get(key);
			if (postGoodsBean.getName().equals(PostCommonValues.STRING_DESCRIPTION) || 
					(postGoodsBean.getRequired().endsWith("required") && !PostUtil.inArray(postGoodsBean.getName(), PostCommonValues.hiddenItemNames) && !postGoodsBean.getName().equals("title") && !postGoodsBean.getName().equals(PostCommonValues.STRING_AREA))) {
				if(!params.containsKey(postGoodsBean.getName()) 
						|| params.getData(postGoodsBean.getName()).equals("")
						|| (postGoodsBean.getUnit() != null && params.getData(postGoodsBean.getName()).equals(postGoodsBean.getUnit()))){
					if(postGoodsBean.getName().equals("images"))continue;
					postResultFail("please entering " + postGoodsBean.getDisplayName() + "!");
					ViewUtil.showToast(this.getActivity(), "请填写" + postGoodsBean.getDisplayName() + "!", false);
					this.changeFocusAfterPostError(postGoodsBean.getDisplayName());
					return false;
				}
			}
		}
		
		if (ImageUploader.getInstance().hasPendingJob()) {
			ViewUtil.showToast(this.getActivity(), "图片上传中", false);
			return false;
		}
		
		return true;
	}
	
	private String getFilledLocation(){
		String toRet = "";
		for(int m = 0; m < layout_txt.getChildCount(); ++ m){
			View v = layout_txt.getChildAt(m);
			PostGoodsBean bean = (PostGoodsBean)v.getTag(PostCommonValues.HASH_POST_BEAN);
			if(bean == null) continue;
			if(bean.getName().equals(PostCommonValues.STRING_DETAIL_POSITION)){
				TextView tv = (TextView)v.getTag(PostCommonValues.HASH_CONTROL);
				if(tv != null && !tv.getText().toString().equals("")){
					toRet = tv.getText().toString();
				}
				break;
			}
		}
		return toRet;
	}
	
	protected void mergeParams(HashMap<String, String> list){}
	
	protected void postAd(BXLocation location){
		HashMap<String, String> list = new HashMap<String, String>();
		list.put("categoryEnglishName", categoryEnglishName);
		list.put("cityEnglishName", GlobalDataManager.getInstance().getCityEnglishName());
		
		HashMap<String, String> mapParams = new HashMap<String, String>();
		Iterator<String> ite = params.keyIterator();
		while(ite.hasNext()){
			String key = ite.next();
			String value = params.getData(key);
			mapParams.put(key, value);
		}
		mergeParams(list);
//		this.postNS.postAdAsync(mapParams, list, postList, bmpUrls, location, editMode);
		bmpUrls.clear();
		bmpUrls.addAll(ImageUploader.getInstance().getServerUrlList());
		PerformanceTracker.stamp(Event.E_Post_Request_Sent);
		this.postNS.postAdAsync(mapParams, list, postList, bmpUrls, location, editMode);
	}

	private int getLineCount() {
		return etDescription != null ? etDescription.getLineCount() : 1;
	}
	
	private int getDescLength() {
		return etDescription != null ? etDescription.getText().length() : 0;
	}
	
	private int getContactLength() {
		return etContact != null ? etContact.getText().length() : 0;
	}
	
	private int getImgCount() {
		int imgCount = 0;
		for (int i = 0; i < bmpUrls.size(); i++) {				
			if(bmpUrls.get(i) != null && bmpUrls.get(i).contains("http:")){
				imgCount++;
			}
		}
		return imgCount;
	}
	
	private void postResultSuccess() {
		BxEvent event = editMode ? BxEvent.EDITPOST_POSTRESULT : BxEvent.POST_POSTRESULT;
		Tracker.getInstance().event(event)
		.append(Key.SECONDCATENAME, categoryEnglishName)
		.append(Key.POSTSTATUS, 1)
		.append(Key.POSTPICSCOUNT, getImgCount())
		.append(Key.POSTDESCRIPTIONLINECOUNT, getLineCount())
		.append(Key.POSTDESCRIPTIONTEXTCOUNT, getDescLength())
		.append(Key.POSTCONTACTTEXTCOUNT, getContactLength()).end();
	}
	
	private void postResultFail(String errorMsg) {
		BxEvent event = editMode ? BxEvent.EDITPOST_POSTRESULT : BxEvent.POST_POSTRESULT;
		Tracker.getInstance().event(event)
			.append(Key.SECONDCATENAME, categoryEnglishName)
			.append(Key.POSTSTATUS, 0)
			.append(Key.POSTFAILREASON, errorMsg)
			.append(Key.POSTPICSCOUNT, getImgCount())
			.append(Key.POSTDESCRIPTIONLINECOUNT, getLineCount())
			.append(Key.POSTDESCRIPTIONTEXTCOUNT, getDescLength())
			.append(Key.POSTCONTACTTEXTCOUNT, getContactLength()).end();
	}
	
	private void loadCachedData(){
		if(params.size() == 0) return;

		Iterator<String> it = params.keyIterator();
		while (it.hasNext()){
			String name = it.next();
			for (int i=0; i<layout_txt.getChildCount(); i++)
			{
				View v = layout_txt.getChildAt(i);
				PostGoodsBean bean = (PostGoodsBean)v.getTag(PostCommonValues.HASH_POST_BEAN);
				if(bean == null || 
						!bean.getName().equals(name)//check display name 
						) continue;
				View control = (View)v.getTag(PostCommonValues.HASH_CONTROL);
				String displayValue = params.getUiData(name);
				
				if(control instanceof CheckBox){
					if(displayValue.contains(((CheckBox)control).getText())){
						((CheckBox)control).setChecked(true);
					}
					else{
						((CheckBox)control).setChecked(false);
					}
				}else if(control instanceof TextView){
					((TextView)control).setText(displayValue);
				}
			}
		}	
	}
	
	private void clearCategoryParameters(){//keep fixed(common) parameters there
		Iterator<String> ite = params.keyIterator();
		while(ite.hasNext()){
			String key = ite.next();
			if(!PostUtil.inArray(key, PostCommonValues.fixedItemNames)){
				params.remove(key);
				ite = params.keyIterator();
			}
		}
	}
	
	private void resetData(boolean clearImgs){
		if(this.layout_txt != null){
			View imgView = layout_txt.findViewById(R.id.image_list);
			View desView = layout_txt.findViewById(R.id.img_description);
			View catView = layout_txt.findViewById(R.id.categoryItem);
			layout_txt.removeAllViews();
			layout_txt.addView(imgView);
			layout_txt.addView(desView);
			if(catView != null){
				layout_txt.addView(catView);
			}
		}
		postList.clear();
		
		if(null != Util.loadDataFromLocate(getActivity(), FILE_LAST_CATEGORY, String.class)){
			clearCategoryParameters();
			if(clearImgs){
//				listUrl.clear();
				this.doClearUpImages();
				this.bmpUrls.clear();
				
//				layout_txt.findViewById(R.id.imgCout).setVisibility(View.INVISIBLE);
				
				params.remove(PostCommonValues.STRING_DESCRIPTION);
				params.remove("价格");
			}
		}
	}

	private void handleBackWithData(int message, Object obj) {
		if(message == MSG_CATEGORY_SEL_BACK && obj != null){
			String[] names = ((String)obj).split(",");
			if(names.length == 2){
				if(names[0].equals(this.categoryEnglishName)){
					return;
				}
				this.categoryEnglishName = names[0];
				this.categoryName = names[1];
				
			}else if(names.length == 1){
				if(names[0].equals(this.categoryEnglishName)){
					return;
				}
				this.categoryEnglishName = names[0];
			}
			
			resetData(false);
			Util.saveDataToLocate(getActivity(), FILE_LAST_CATEGORY, obj);
			this.showPost();
		}
		PostUtil.fetchResultFromViewBack(message, obj, layout_txt, params);
	}
	
	@Override
	public void onFragmentBackWithData(int message, Object obj){	
		handleBackWithData(message, obj);
	}
	
	protected String getAdContact(){
		return "";
	}

	private void appendBeanToLayout(PostGoodsBean postBean){
		UserBean user = GlobalDataManager.getInstance().getAccountManager().getCurrentUser();
		if (postBean.getName().equals("contact") &&
			(postBean.getValues() == null || postBean.getValues().isEmpty()) &&
			(user != null && user.getPhone() != null && user.getPhone().length() > 0)){
			List<String> valueList = new ArrayList<String>(1);
			valueList.add(user.getPhone());
			postBean.setValues(valueList);
			postBean.setLabels(valueList);
		}	
		
		ViewGroup layout = createItemByPostBean(postBean);//FIXME:
		if(layout != null && layout.findViewById(R.id.postinputlayout) != null){
			layout.setClickable(true);
			layout.setOnClickListener(this);
		}

		if(layout != null && !postBean.getName().equals(PostCommonValues.STRING_DETAIL_POSITION)){
			ViewGroup.LayoutParams lp = layout.getLayoutParams();
			lp.height = getResources().getDimensionPixelOffset(R.dimen.post_item_height);
			layout.setLayoutParams(lp);
		}

		if(postBean.getName().equals(PostCommonValues.STRING_DETAIL_POSITION)){
			layout.findViewById(R.id.location).setOnClickListener(this);
			((TextView)layout.findViewById(R.id.postinput)).setHint("填写或点击按钮定位");
			locationView = layout;
			
			String address = GlobalDataManager.getInstance().getAddress();
			if(address != null && address.length() > 0){
				((TextView)layout.findViewById(R.id.postinput)).setText(address);
			}
		}else if(postBean.getName().equals("contact") && layout != null){
			etContact = ((EditText)layout.getTag(PostCommonValues.HASH_CONTROL));
			((TextView)layout.findViewById(R.id.postinput)).setHint("手机或座机");
			etContact.setFilters(new InputFilter[]{new InputFilter.LengthFilter(15)});
			String phone = GlobalDataManager.getInstance().getPhoneNumber();
			if(editMode){
				etContact.setText(getAdContact());
			}else{
				if(phone != null && phone.length() > 0){
					etContact.setText(phone);
				}
			}
		}else if (postBean.getName().equals(PostCommonValues.STRING_DESCRIPTION) && layout != null){
			etDescription = (EditText) layout.getTag(PostCommonValues.HASH_CONTROL);
		}else if(postBean.getName().equals("价格")){
			((TextView)layout.findViewById(R.id.postinput)).setHint("越便宜成交越快");
		}else if(postBean.getName().equals("faburen")){
			List<String> labels = postBean.getLabels();
			List<String> values = postBean.getValues();
 			if(labels != null){
				for(int i = 0; i < labels.size(); ++ i){
					if(labels.get(i).equals("个人")){
						((TextView)layout.findViewById(R.id.posthint)).setText(labels.get(i));
						params.put(postBean.getName(), labels.get(i), values.get(i));
					}
				}
			}
//			
		}
		
		if(layout != null){
			layout_txt.addView(layout);
		}
	}

	private void popupCategorySelectionDialog(){
		Bundle bundle = createArguments(null, null);
		bundle.putSerializable("items", (Serializable) Arrays.asList(PostCommonValues.mainCategories));
		bundle.putInt("maxLevel", 1);
		bundle.putInt(ARG_COMMON_REQ_CODE, MSG_CATEGORY_SEL_BACK);
		if(categoryEnglishName != null && !categoryEnglishName.equals("") && categoryName != null) {
			bundle.putString("selectedValue", categoryName);
		}
		PostUtil.extractInputData(layout_txt, params);
		CustomDialogBuilder cdb = new CustomDialogBuilder(getActivity(), PostGoodsFragment.this.getHandler(), bundle);
		cdb.start();
	}
	
	private void addCategoryItem(){
		Activity activity = getActivity();
		if(layout_txt != null){
			if(layout_txt.findViewById(R.id.arrow_down) != null) return;
		}
//		LayoutInflater inflater = LayoutInflater.from(activity);
//		View categoryItem = inflater.inflate(R.layout.item_post_category, null);
		
		View categoryItem = layout_txt.findViewById(R.id.categoryItem);
		if(editMode){
			layout_txt.removeView(categoryItem);
			return;
		}
		categoryItem.setTag(PostCommonValues.HASH_CONTROL, categoryItem.findViewById(R.id.posthint));//tag
		((TextView)categoryItem.findViewById(R.id.postshow)).setText("分类");
		categoryItem.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Tracker.getInstance().event(BxEvent.POST_INPUTING).append(Key.ACTION, "类目").end();
				popupCategorySelectionDialog();
			}				
		});//categoryItem.setOnClickListener
		
		if(categoryEnglishName != null && !categoryEnglishName.equals("") && categoryName != null){
			((TextView)categoryItem.findViewById(R.id.posthint)).setText(categoryName);
		}else{
			((TextView)categoryItem.findViewById(R.id.posthint)).setText("请选择分类");
		}
		PostUtil.adjustMarginBottomAndHeight(categoryItem);
//		layout_txt.addView(categoryItem);
	}
	
	private void buildFixedPostLayout(HashMap<String, PostGoodsBean> pl){
		if(pl == null || pl.size() == 0) return;
		
		HashMap<String, PostGoodsBean> pm = new HashMap<String, PostGoodsBean>();
		Object[] postListKeySetArray = pl.keySet().toArray();
		for(int i = 0; i < pl.size(); ++ i){
			for(int j = 0; j < PostCommonValues.fixedItemNames.length; ++ j){
				PostGoodsBean bean = pl.get(postListKeySetArray[i]);
				if(bean.getName().equals(PostCommonValues.fixedItemNames[j])){					
					pm.put(PostCommonValues.fixedItemNames[j], bean);
					break;
				}
			}
		}
		
		if(pm.containsKey(PostCommonValues.STRING_DESCRIPTION)){
			PostGoodsBean bean = pm.get(PostCommonValues.STRING_DESCRIPTION);
			if(bean != null){
				View v = layout_txt.findViewById(R.id.img_description);
				EditText text = (EditText)v.findViewById(R.id.description_input);
				text.setText("");
				text.setOnTouchListener(new OnTouchListener() {
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						if (event.getAction() == MotionEvent.ACTION_DOWN) {
							Tracker.getInstance().event((editMode)?BxEvent.POST_INPUTING:BxEvent.EDITPOST_INPUTING).append(Key.ACTION, PostCommonValues.STRING_DESCRIPTION).end();
						}
						return false;
					}
				});
				text.setOnFocusChangeListener(new PostUtil.BorderChangeListener(this.getActivity(), v));

				text.setHint("请输入" + bean.getDisplayName());
				v.setTag(PostCommonValues.HASH_POST_BEAN, bean);
				v.setTag(PostCommonValues.HASH_CONTROL, text);
				v.setOnClickListener(this);
				
//				v.findViewById(R.id.myImg).setOnClickListener(this);
//				((ImageView)v.findViewById(R.id.myImg)).setImageBitmap(ImageCacheManager.getInstance().loadBitmapFromResource(R.drawable.btn_add_picture));
				this.updateImageInfo(layout_txt);
			}			
		}
		
		for(int i = 0; i < PostCommonValues.fixedItemNames.length; ++ i){
			if(pm.containsKey(PostCommonValues.fixedItemNames[i]) && !PostCommonValues.fixedItemNames[i].equals(PostCommonValues.STRING_DESCRIPTION)){
				this.appendBeanToLayout(pm.get(PostCommonValues.fixedItemNames[i]));
			}else if(!pm.containsKey(PostCommonValues.fixedItemNames[i])){
				params.remove(PostCommonValues.fixedItemNames[i]);
			}
		}
	}
	
	private void addHiddenItemsToParams(){
		if (postList == null || postList.isEmpty())
			return ;
		Set<String> keySet = postList.keySet();
		for (String key : keySet){
			PostGoodsBean bean = postList.get(key);
			for (int i = 0; i< PostCommonValues.hiddenItemNames.length; i++){
				if (bean.getName().equals(PostCommonValues.hiddenItemNames[i])){
					String defaultValue = bean.getDefaultValue();
					if (defaultValue != null && defaultValue.length() > 0) {
						this.params.put(bean.getName(), defaultValue, defaultValue);
					} else {
						this.params.put(bean.getName(), bean.getLabels().get(0), bean.getValues().get(0));
					}
					break;
				}
			}
		}
	}
	
	protected void buildPostLayout(HashMap<String, PostGoodsBean> pl){
		this.getView().findViewById(R.id.goodscontent).setVisibility(View.VISIBLE);
		this.getView().findViewById(R.id.networkErrorView).setVisibility(View.GONE);
		this.reCreateTitle();
		this.refreshHeader();
		if(pl == null || pl.size() == 0){
			return;
		}
		buildFixedPostLayout(pl);
		addHiddenItemsToParams();
		
		Object[] postListKeySetArray = pl.keySet().toArray();
		for (int i = 0; i < pl.size(); i++) {
			String key = (String) postListKeySetArray[i];
			PostGoodsBean postBean = pl.get(key);
			
			if(PostUtil.inArray(postBean.getName(), PostCommonValues.fixedItemNames) || postBean.getName().equals("title") || PostUtil.inArray(postBean.getName(), PostCommonValues.hiddenItemNames))
				continue;
			
			if(postBean.getName().equals(PostCommonValues.STRING_AREA)){
				continue;
			}
			
			this.appendBeanToLayout(postBean);
		}
		
		this.showInputMethod();
	}
	
	private View searchEditText(View parent, int resourceId){
		View v = parent.findViewById(resourceId);
		if(v != null && v instanceof EditText){
			if(((EditText)v).getText() == null || ((EditText)v).getText().length() == 0){
				return v;
			}
		}
		return null;
	}
	
	private View getEmptyEditText(){
		View edit = null;
		for(int i = 0; i < layout_txt.getChildCount(); ++  i){
			View child = layout_txt.getChildAt(i);
			if(child == null) continue;
			edit = searchEditText(child, R.id.description_input);
			if(edit != null){
				break;
			}
			edit = searchEditText(child, R.id.postinput);
			if(edit != null){
				break;
			}
			edit = null;
		}
		return edit;
	}
	
	private void showInputMethod() {
		final View root = this.getView();
		if (root != null) {
			root.postDelayed(new Runnable() {
				public void run() {
//					EditText ed = (EditText) root.findViewById(R.id.description_input);
					View ed = getEmptyEditText();
					if (ed != null){// && ed.getText().length() == 0) {
						ed.requestFocus();
						InputMethodManager mgr = (InputMethodManager) root.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
						mgr.showSoftInput(ed, InputMethodManager.SHOW_IMPLICIT);
					}
				}
			}, 200);
		}
	}

	final protected void updateImageInfo(View rootView) {
		if (rootView != null) {
			ViewGroup list = (ViewGroup) rootView.findViewById(R.id.image_list_parent);
			if (list == null) {
				return;
			}
			list.removeAllViews();
			
			LayoutInflater inflator = LayoutInflater.from(rootView.getContext());
			for (String img : this.photoList) {
				View imgParent = inflator.inflate(R.layout.post_image, null);
				imgParent.setTag(img);
				
				imgParent.setOnClickListener(this);
				imgParent.setId(R.id.delete_btn);
				
				final int margin = (int) getResources().getDimension(R.dimen.post_img_margin);
				final int wh = (int) getResources().getDimension(R.dimen.post_img_size);
				MarginLayoutParams layParams = new MarginLayoutParams(wh + margin, wh + 2 * margin);
				layParams.setMargins(0, margin, margin, margin);
				list.addView(imgParent, layParams);
				
				ImageUploader.getInstance().registerCallback(img, this);
			}
			
			if (this.photoList == null || this.photoList.size() < 6) {
				showAddImageButton(list, inflator, false);
			}
		}
	}
	
	
	private void showAddImageButton(ViewGroup parent, LayoutInflater inflator, boolean scroolNow) {
		
		try {
			View addBtn = parent.getChildAt(parent.getChildCount()-1);
			if (addBtn != null && addBtn.getId() == R.id.add_post_image) {
				return ;
			}
			
			addBtn = inflator.inflate(R.layout.post_image, null);
			((ImageView)addBtn.findViewById(R.id.result_image)).setImageResource(R.drawable.btn_add_picture);
			addBtn.setOnClickListener(this);
			addBtn.setId(R.id.add_post_image);
			
			final int margin = (int) getResources().getDimension(R.dimen.post_img_margin);
			final int wh = (int) getResources().getDimension(R.dimen.post_img_size);
			MarginLayoutParams layParams = new MarginLayoutParams(wh, wh + 2 * margin);
			layParams.setMargins(0, margin, 0, margin);
			parent.addView(addBtn, layParams);
		} finally {
			final HorizontalScrollView hs = (HorizontalScrollView) parent.getParent();
			hs.postDelayed(new Runnable() {
				public void run() {
					hs.scrollBy(1000, 0);
				}
			}, scroolNow ? 0 : 300);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void handleMessage(Message msg, final Activity activity, final View rootView) {
		hideProgress();
		
		switch (msg.what) {
		case MSG_GET_AD_SUCCED:
			this.hideSoftKeyboard();
			this.hideProgress();
			try {
				AdList gl = JsonUtil.getGoodsListFromJson((String) msg.obj);
				if(gl != null && gl.getData() != null && gl.getData().size() > 0){
					
					GlobalDataManager.getInstance().updateMyAd(gl.getData().get(0));
					
					VadListLoader glLoader = new VadListLoader(null, null, null, gl);
					glLoader.setGoodsList(gl);
					glLoader.setHasMore(false);		
					Bundle bundle2 = createArguments("", "close");
					bundle2.putSerializable("loader", glLoader);
					bundle2.putInt("index", 0);
					bundle2.putInt(ARG_COMMON_ANIMATION_IN, 0);
					bundle2.putInt(ARG_COMMON_ANIMATION_EXIT, 0);
					this.pushAndFinish(new VadFragment(), bundle2);
				} else {
					this.hideProgress();
					this.finishFragment();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case MSG_GET_AD_FAIL:
			this.hideProgress();
			this.hideSoftKeyboard();
			this.finishFragment();//FIXME: should tell user network fail.
			break;
		case MSG_IMAGE_STATE_CHANGE: {
			BXThumbnail img = (BXThumbnail) msg.obj;
			int state = msg.arg1;
			ViewGroup imgParent = (ViewGroup) this.findImageViewByTag(img.getLocalPath());
			if (imgParent != null) {
				ImageView imgView = (ImageView) imgParent.findViewById(R.id.result_image);
				View loadingState = imgParent.findViewById(R.id.loading_status);
				if (state == IMG_STATE_UPLOADING || state == IMG_STATE_UPLOADED) {
					imgView.setImageBitmap(img.getThumbnail());
					loadingState.setVisibility(state == IMG_STATE_UPLOADING ? View.VISIBLE : View.INVISIBLE);
				} else {
					imgView.setImageResource(R.drawable.icon_load_fail);
					loadingState.setVisibility(View.GONE);
				}
			}
			
			break;
		}
		case MSG_DIALOG_BACK_WITH_DATA:{
			Bundle bundle = (Bundle)msg.obj;
			handleBackWithData(bundle.getInt(ARG_COMMON_REQ_CODE), bundle.getSerializable("lastChoise"));
			break;
		}		
		case MSG_UPDATE_IMAGE_LIST:{
			updateImageInfo(rootView);
			
			showInputMethod();
			
			break;
		}
		case PostCommonValues.MSG_GET_META_SUCCEED:{
			Button button = (Button) layout_txt.getRootView().findViewById(R.id.iv_post_finish);
			if(button != null){
				button.setEnabled(true);
			}

			postList = (LinkedHashMap<String, PostGoodsBean>)msg.obj;
			addCategoryItem();
			buildPostLayout(postList);
			loadCachedData();
			this.showGettingMetaProgress(false);
			break;
		}

		case PostCommonValues.MSG_GET_META_FAIL:
			hideProgress();
			Button button = (Button) layout_txt.getRootView().findViewById(R.id.iv_post_finish);
			if(button != null){
				button.setEnabled(false);
			}
			addCategoryItem();
			if(msg.obj != null){
				String mesg = "";
				if(msg.obj instanceof PostResultData){
					mesg = ((PostResultData)msg.obj).message;
				}else if(msg.obj instanceof String){
					mesg = (String)msg.obj;
				}
				if(!mesg.equals("")){
					ViewUtil.showToast(activity, mesg, false);
				}
			}
			this.showGettingMetaProgress(false);
			break;
		case PostCommonValues.MSG_POST_SUCCEED:
			PerformanceTracker.stamp(Event.E_POST_SUCCEEDED);
			hideProgress();
			
			doClearUpImages();
			
			String id = ((PostResultData)msg.obj).id;
			boolean isRegisteredUser = ((PostResultData)msg.obj).isRegisteredUser;
			String message = ((PostResultData)msg.obj).message;
			int code = ((PostResultData)msg.obj).error;
			if (!id.equals("") && code == 0) {
				postResultSuccess();
				ViewUtil.showToast(activity, message, false);
				final Bundle args = createArguments(null, null);
				args.putInt("forceUpdate", 1);
				if(!editMode || (editMode && isActive)){
					resetData(!editMode);
					Util.deleteDataFromLocate(this.getActivity(), FILE_LAST_CATEGORY);
					categoryEnglishName = "";
					categoryName = "";
				}
//				showPost();
				if(!editMode){
					handlePostFinish(id);
//					showPost();
//					String lp = getArguments().getString("lastPost");
//					if(lp != null && !lp.equals("")){
//						lp += "," + id;
//					}else{
//						lp = id;
//					}
//					args.putString("lastPost", lp);
//					
//					args.putString("cateEnglishName", categoryEnglishName);
//					args.putBoolean(KEY_IS_EDITPOST, editMode); 
//					
//					args.putBoolean(KEY_LAST_POST_CONTACT_USER,  isRegisteredUser);
//					if(activity != null){							
//						args.putInt(MyAdFragment.TYPE_KEY, MyAdFragment.TYPE_MYPOST);
//						
//						Intent intent = new Intent(CommonIntentAction.ACTION_BROADCAST_POST_FINISH);
//						intent.putExtras(args);
//						PerformanceTracker.stamp(Event.E_Post_Send_Success_Broadcast);
//						activity.sendBroadcast(intent);
//					}
					doClearUpImages();
//					finishFragment();
				}else{
//					showPost();
					PostGoodsFragment.this.finishFragment(PostGoodsFragment.MSG_POST_SUCCEED, null);
				}
			}else{
				postResultFail(message);
				if(msg.obj != null){
					handlePostFail((PostResultData)msg.obj);
				}
			}
			break;
		case PostCommonValues.MSG_POST_FAIL:
			hideProgress();
			if(msg.obj != null){
				if(msg.obj instanceof String){
					ViewUtil.showToast(activity, (String)msg.obj, false);
					this.changeFocusAfterPostError((String)msg.obj);
					postResultFail((String)msg.obj);
				}else if(msg.obj instanceof PostResultData){
					handlePostFail((PostResultData)msg.obj);
					postResultFail(((PostResultData)msg.obj).message);
				}
			}
			break;
		case PostCommonValues.MSG_POST_EXCEPTION:
			hideProgress();
			ViewUtil.showToast(activity, "网络连接异常", false);
			break;
		case ErrorHandler.ERROR_SERVICE_UNAVAILABLE:
			hideProgress();
			ErrorHandler.getInstance().handleMessage(msg);
//			this.getView().findViewById(R.id.goodscontent).setVisibility(View.GONE);
//			this.getView().findViewById(R.id.networkErrorView).setVisibility(View.VISIBLE);		
//			this.reCreateTitle();
//			this.refreshHeader();
			break;
		case MSG_GEOCODING_TIMEOUT:
		case PostCommonValues.MSG_GEOCODING_FETCHED:
			Event evt = msg.what == MSG_GEOCODING_TIMEOUT ? Event.E_GeoCoding_Timeout : Event.E_GeoCoding_Fetched;
			PerformanceTracker.stamp(evt);
			showProgress(R.string.dialog_title_info, R.string.dialog_message_waiting, false);
			handler.removeMessages(MSG_GEOCODING_TIMEOUT);
			handler.removeMessages(PostCommonValues.MSG_GEOCODING_FETCHED);
			postAd(msg.obj == null ? null : (BXLocation)msg.obj);
			break;
		case PostCommonValues.MSG_GPS_LOC_FETCHED:
			detailLocation = (BXLocation)msg.obj;
			break;
		}
	}
	
	private void changeFocusAfterPostError(String errMsg){
		if(postList == null) return;
		Set<String> keys = postList.keySet();
		if(keys == null) return;
		for(String key : keys){
			PostGoodsBean bean = postList.get(key);
			if(errMsg.contains(bean.getDisplayName())){
				for(int j = 0; j < layout_txt.getChildCount(); ++ j){
					final View child = layout_txt.getChildAt(j);
					if(child != null){
						PostGoodsBean tag = (PostGoodsBean)child.getTag(PostCommonValues.HASH_POST_BEAN);
						if(tag != null && tag.getName().equals(postList.get(key).getName())){
							View et = child.findViewById(R.id.postinput);
							if(et == null){
								et = child.findViewById(R.id.description_input);
							}
							if(et != null){
								final View inputView = et;
								inputView.postDelayed(new Runnable(){
									@Override
									public void run(){
										inputView.requestFocus();
										InputMethodManager inputMgr = 
												(InputMethodManager) inputView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
										inputMgr.showSoftInput(inputView, InputMethodManager.SHOW_IMPLICIT);									
									}
								}, 100);
							} else {
								child.postDelayed(new Runnable() {
									public void run() {
										child.performClick();
									}
								}, 100);
							}
							return;
						}
					}
				}
			}
		}

	}
	
	private void handlePostFail(final PostResultData result){
		if(result == null) return;
		if(result.error == 505){
			AlertDialog.Builder bd = new AlertDialog.Builder(this.getActivity());
	        bd.setTitle("")
	                .setMessage(result.message)
	                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
	                	@Override
	                    public void onClick(DialogInterface dialog, int which) {
	                        dialog.dismiss();
//							if(getActivity() != null){
//								resetData(true);
//								showPost();
//								Bundle args = createArguments(null, null);
//								args.putInt(MyAdFragment.TYPE_KEY, MyAdFragment.TYPE_MYPOST);
//								Intent intent = new Intent(CommonIntentAction.ACTION_BROADCAST_POST_FINISH);
//								intent.putExtras(args);
//								getActivity().sendBroadcast(intent);
//							}
	                        handlePostFinish(result.id);
	                    }
	                });
	        AlertDialog alert = bd.create();
	        alert.show();	
		}else if(result.message != null && !result.message.equals("")){
			ViewUtil.showToast(getActivity(), result.message, false);
		}
	}

	private int imgHeight = 0;
	
	////to fix stupid system error. all text area will be the same content after app is brought to front when activity not remain is checked
	private void setInputContent(){		
		if(layout_txt == null) return;
		for(int i = 0; i < layout_txt.getChildCount(); ++ i){
			View v = layout_txt.getChildAt(i);
			PostGoodsBean bean = (PostGoodsBean)v.getTag(PostCommonValues.HASH_POST_BEAN);
			if(bean == null) continue;
			View control = (View)v.getTag(PostCommonValues.HASH_CONTROL);
			if(control != null && control instanceof TextView){
				if(params != null && params.containsKey(bean.getName())){
					String value = params.getUiData(bean.getName());
					if(value == null){
						value = params.getUiData(bean.getName());
					}
					if(bean.getName().equals("contact")){
						if(editMode){
							((TextView)control).setText(getAdContact());
						}else{
							String phone = GlobalDataManager.getInstance().getPhoneNumber();
							if(phone != null && phone.length() > 0){
								((TextView)control).setText(phone);
								continue;
							}
						}
					}
					((TextView)control).setText(value);
				}
			}
		}
	}

	@Override
	public void onStart(){
		super.onStart();
		setInputContent();
	}
	
	@Override
	public void initTitle(TitleDef title){
		title.m_visible = true;
		title.m_leftActionHint = "返回";
		title.m_leftActionImage  = R.drawable.icon_close;
		title.m_title = "免费发布";
	}
	
	private ViewGroup createItemByPostBean(PostGoodsBean postBean){
		Activity activity = getActivity();
		ViewGroup layout = PostUtil.createItemByPostBean(postBean, activity);

		if (layout == null)
			return null;

		if(postBean.getControlType().equals("select") || postBean.getControlType().equals("checkbox")){
			final String actionName = ((PostGoodsBean)layout.getTag(PostCommonValues.HASH_POST_BEAN)).getDisplayName();
			layout.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					Tracker.getInstance().event((!editMode) ? BxEvent.POST_INPUTING:BxEvent.EDITPOST_INPUTING).append(Key.ACTION, actionName).end();

					PostGoodsBean postBean = (PostGoodsBean) v.getTag(PostCommonValues.HASH_POST_BEAN);

					if (postBean.getControlType().equals("select") || postBean.getControlType().equals("tableSelect")) {
							if(postBean.getLevelCount() > 0){
									ArrayList<MultiLevelSelectionFragment.MultiLevelItem> items = 
											new ArrayList<MultiLevelSelectionFragment.MultiLevelItem>();
									for(int i = 0; i < postBean.getLabels().size(); ++ i){
										MultiLevelSelectionFragment.MultiLevelItem t = new MultiLevelSelectionFragment.MultiLevelItem();
										t.txt = postBean.getLabels().get(i);
										t.id = postBean.getValues().get(i);
										items.add(t);
									}
									Bundle bundle = createArguments(null, null);
									bundle.putInt(ARG_COMMON_REQ_CODE, postBean.getName().hashCode());
									bundle.putSerializable("items", items);
									bundle.putInt("maxLevel", postBean.getLevelCount() - 1);
									String selectedValue = null;
									selectedValue = params.getData(postBean.getName());
									
									if (selectedValue != null)
										bundle.putString("selectedValue", selectedValue);

									PostUtil.extractInputData(layout_txt, params);
									CustomDialogBuilder cdb = new CustomDialogBuilder(getActivity(), getHandler(), bundle);
									cdb.start();
							}else{
								Bundle bundle = createArguments(postBean.getDisplayName(), null);
								bundle.putInt(ARG_COMMON_REQ_CODE, postBean.getName().hashCode());
								bundle.putBoolean("singleSelection", false);
								bundle.putSerializable("properties",(ArrayList<String>) postBean.getLabels());
								TextView txview = (TextView)v.getTag(PostCommonValues.HASH_CONTROL);
								if (txview !=  null)
								{
									bundle.putString("selected", txview.getText().toString());
								}
								((BaseActivity)getActivity()).pushFragment(new OtherPropertiesFragment(), bundle, false);
							}//postBean.getLevelCount() <= 0
					}else if(postBean.getControlType().equals("checkbox")){
						if(postBean.getLabels().size() > 1){
							Bundle bundle = createArguments(postBean.getDisplayName(), null);
							bundle.putInt(ARG_COMMON_REQ_CODE, postBean.getName().hashCode());
							bundle.putBoolean("singleSelection", false);
							bundle.putSerializable("properties",(ArrayList<String>) postBean.getLabels());
							TextView txview = (TextView)v.getTag(PostCommonValues.HASH_CONTROL);
							if (txview !=  null){
								bundle.putString("selected", txview.getText().toString());
							}
							((BaseActivity)getActivity()).pushFragment(new OtherPropertiesFragment(), bundle, false);
						}
						else{
							View checkV = v.findViewById(R.id.checkitem);
							if(checkV != null && checkV instanceof CheckBox){
								((CheckBox)checkV).setChecked(!((CheckBox)checkV).isChecked());
							}
						}
					}
				}
			});//layout.setOnClickListener:select or checkbox
		} else {//not select or checkbox
			final String actionName = ((PostGoodsBean)layout.getTag(PostCommonValues.HASH_POST_BEAN)).getDisplayName();
			((View)layout.getTag(PostCommonValues.HASH_CONTROL)).setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						Tracker.getInstance().event((!editMode) ? BxEvent.POST_INPUTING:BxEvent.EDITPOST_INPUTING).append(Key.ACTION, actionName).end();
					}
					return false;
				}
			});
			
			layout.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					View ctrl = (View) v.getTag(PostCommonValues.HASH_CONTROL);
					ctrl.requestFocus();
					InputMethodManager inputMgr = (InputMethodManager) ctrl.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
					inputMgr.showSoftInput(ctrl, InputMethodManager.SHOW_IMPLICIT);
				}
			});			
		}		
		PostUtil.adjustMarginBottomAndHeight(layout);
		
		return layout;
	}
	
	private void setDetailLocationControl(BXLocation location){
		if(location == null) return;
		if(locationView != null && locationView.findViewById(R.id.postinput) != null){
			String address = (location.detailAddress == null || location.detailAddress.equals("")) ? 
            		((location.subCityName == null || location.subCityName.equals("")) ?
							"" 
							: location.subCityName)
					: location.detailAddress;
            if(address == null || address.length() == 0) return;
            if(location.adminArea != null && location.adminArea.length() > 0){
            	address = address.replaceFirst(location.adminArea, "");
            }
            if(location.cityName != null && location.cityName.length() > 0){
            	address = address.replaceFirst(location.cityName, "");
            }
			((TextView)locationView.findViewById(R.id.postinput)).setText(address);
		}		
	}
	
	private void handlePostFinish(String adId) {
		ApiParams param = new ApiParams();
		param.addParam("newAdIds", adId);
		param.addParam("start", 0);
		param.addParam("rt", 1);
		param.addParam("rows", 1);
		param.addParam("wanted", 0);
		param.addParam("status", 3);
		this.showProgress("", "正在获取您发布信息的状态，请耐心等候", false);
		
		BaseApiCommand.createCommand("ad_user_list", true, param).execute(getActivity(), new BaseApiCommand.Callback() {
			public void onNetworkFail(String apiName, ApiError error) {
				PostGoodsFragment.this.sendMessage(MSG_GET_AD_FAIL, "");
			}
			
			public void onNetworkDone(String apiName, String responseData) {
				PostGoodsFragment.this.sendMessage(MSG_GET_AD_SUCCED, responseData);
			}
		});
	}
	
	public boolean hasGlobalTab() {
		return false;
	}

	@Override
	public void onUploadDone(String imagePath, String serverUrl,
			Bitmap thumbnail) {
		Message msg = this.handler.obtainMessage(MSG_IMAGE_STATE_CHANGE, IMG_STATE_UPLOADED, 0, BXThumbnail.createThumbnail(imagePath, thumbnail));
		handler.sendMessage(msg);
	}

	@Override
	public void onUploading(String imagePath, Bitmap thumbnail) {
		Message msg = this.handler.obtainMessage(MSG_IMAGE_STATE_CHANGE, IMG_STATE_UPLOADING, 0, BXThumbnail.createThumbnail(imagePath, thumbnail));
		handler.sendMessage(msg);
	}

	@Override
	public void onUploadFail(String imagePath, Bitmap thumbnail) {
		Message msg = this.handler.obtainMessage(MSG_IMAGE_STATE_CHANGE, IMG_STATE_FAIL, 0, BXThumbnail.createThumbnail(imagePath, thumbnail));
		handler.sendMessage(msg);
		
//		firstImage = ImageCacheManager.getInstance().loadBitmapFromResource(R.drawable.icon_load_fail);
//		if(getView() != null && getView().getRootView() != null){
//			getActivity().runOnUiThread(new Runnable(){
//				@Override
//				public void run(){
//					updateImageInfo(getView().getRootView());
//				}
//			});
//		}
	}
	
	private View findImageViewByTag(String imagePath) {
		ViewGroup root = (ViewGroup) this.getView().findViewById(R.id.image_list_parent);
		if (root == null) {
			return null;
		}
		
		int c = root.getChildCount();
		
		for (int i=0; i<c; i++) {
			View child = root.getChildAt(i);
			if (imagePath.equals(child.getTag())) {
				return child;
			}
		}
		
		return null;
	}
}
