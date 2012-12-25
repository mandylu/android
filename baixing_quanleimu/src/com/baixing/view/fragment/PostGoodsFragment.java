package com.baixing.view.fragment;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.InputFilter;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.baixing.activity.BaseActivity;
import com.baixing.activity.BaseFragment;
import com.baixing.activity.GlobalDataManager;
import com.baixing.broadcast.CommonIntentAction;
import com.baixing.entity.BXLocation;
import com.baixing.entity.GoodsDetail;
import com.baixing.entity.GoodsDetail.EDATAKEYS;
import com.baixing.entity.PostGoodsBean;
import com.baixing.entity.UserBean;
import com.baixing.imageCache.SimpleImageLoader;
import com.baixing.jsonutil.JsonUtil;
import com.baixing.tracking.Tracker;
import com.baixing.tracking.TrackConfig.TrackMobile.BxEvent;
import com.baixing.tracking.TrackConfig.TrackMobile.Key;
import com.baixing.tracking.TrackConfig.TrackMobile.PV;
import com.baixing.util.Communication;
import com.baixing.util.PostLocationService;
import com.baixing.util.PostUtil;
import com.baixing.widget.ImageSelectionDialog;
import com.baixing.util.Util;
import com.baixing.widget.CustomDialogBuilder;
import com.quanleimu.activity.R;

public class PostGoodsFragment extends BaseFragment implements OnClickListener{
	private static final int MSG_GETLOCATION_TIMEOUT = 8;
	private static final int VALUE_LOGIN_SUCCEEDED = 9;
	
	private static final int MSG_GEOCODING_TIMEOUT = 0x00010011;
	static final public String KEY_INIT_CATEGORY = "cateNames";
	static final String KEY_LAST_POST_CONTACT_USER = "lastPostContactIsRegisteredUser";
	static final String KEY_IS_EDITPOST = "isEditPost"; 
	static final String KEY_CATE_ENGLISHNAME = "cateEnglishName";
	static final private String KEY_IMG_BUNDLE = "key_image_bundle";
	static final private String STRING_DETAIL_POSITION = "具体地点";
	protected static final String STRING_AREA = "地区";
	static final private String FILE_LAST_CATEGORY = "lastCategory";
	static final private String STRING_DESCRIPTION = "description";
	static final int MSG_POST_SUCCEED = 0xF0000010; 

	protected String categoryEnglishName = "";
	private String categoryName = "";
	private String json = "";
	protected LinearLayout layout_txt;
	private LinkedHashMap<String, PostGoodsBean> postList = new LinkedHashMap<String, PostGoodsBean>();
	private static final int NONE = 0;
	private static final int PHOTORESOULT = 3;
	private static final int MSG_CATEGORY_SEL_BACK = 11;
	private static final int MSG_DIALOG_BACK_WITH_DATA = 12;
	protected PostParamsHolder params = new PostParamsHolder();
	private PostParamsHolder originParams = new PostParamsHolder();
	private String mobile, password;
	private UserBean user;
	protected boolean editMode = false;
	protected ArrayList<String> listUrl = new ArrayList<String>();
	protected Bundle imgSelBundle = null;
	private ImageSelectionDialog imgSelDlg = null;	
	private View locationView = null;
	private BXLocation detailLocation = null;
    protected List<String> bmpUrls = new ArrayList<String>();
    private EditText etDescription = null;
    private EditText etContact = null;
    private PostLocationService postLBS;
    
    @Override
	public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		if (resultCode == NONE) {
			return;
		}
		
		FragmentManager fm = getActivity().getSupportFragmentManager();

		Fragment fg = fm.getFragment(this.imgSelBundle, "imageFragment");
		if(fg != null && (fg instanceof ImageSelectionDialog)){
			this.imgSelDlg = (ImageSelectionDialog)fg;
		}
		if(this.imgSelDlg != null &&
				(requestCode == CommonIntentAction.PhotoReqCode.PHOTOHRAPH
				|| requestCode == CommonIntentAction.PhotoReqCode.PHOTOZOOM
				|| requestCode == PHOTORESOULT)){
			imgSelDlg.setMsgOutHandler(handler);
			if(imgSelBundle == null){
				imgSelBundle = new Bundle();
			}
			imgSelDlg.setMsgOutBundle(this.imgSelBundle);
			imgSelDlg.onActivityResult(requestCode, resultCode, data);
		}
    }
    
    private static final String []texts = {"物品交易", "车辆买卖", "房屋租售", "全职招聘", "兼职招聘", "求职简历", "交友活动", "宠物", "生活服务", "教育培训"};
    
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
		super.onCreate(savedInstanceState);
		
		String categoryNames = this.getArguments().getString(KEY_INIT_CATEGORY);
		initWithCategoryNames(categoryNames);
				
		if (savedInstanceState != null){
			postList.putAll( (HashMap<String, PostGoodsBean>)savedInstanceState.getSerializable("postList"));
			params = (PostParamsHolder) savedInstanceState.getSerializable("params");
			listUrl.addAll((List<String>) savedInstanceState.getSerializable("listUrl"));
			imgHeight = savedInstanceState.getInt("imgHeight");
			imgSelBundle = savedInstanceState.getBundle(KEY_IMG_BUNDLE);
		}
		
		if(imgSelBundle == null){
			imgSelBundle =  new Bundle();
		}

		if(imgSelDlg == null){
			imgSelDlg = new ImageSelectionDialog(imgSelBundle);
			imgSelDlg.setMsgOutHandler(handler);
		}
		
		user = Util.getCurrentUser();//(UserBean) Util.loadDataFromLocate(this.getActivity(), "user", UserBean.class);
		if(user != null && user.getPhone() != null && !user.getPhone().equals("")){
			mobile = user.getPhone();
			password = user.getPassword();
		}
		String appPhone = GlobalDataManager.getApplication().getPhoneNumber();
		if(!editMode && (appPhone == null || appPhone.length() == 0)){
			GlobalDataManager.getApplication().setPhoneNumber(mobile);
		}
		
		this.postLBS = new PostLocationService(this.handler);
	}
		
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		PostUtil.extractInputData(layout_txt, params);
		outState.putSerializable("params", params);
		outState.putSerializable("postList", postList);
		outState.putSerializable("listUrl", listUrl);
		outState.putInt("imgHeight", imgHeight);
		outState.putBundle(KEY_IMG_BUNDLE, imgSelBundle);
	}

	@Override
	public boolean handleBack() {
		if(imgSelDlg != null)
			if(imgSelDlg.handleBack()){
				return true;
		}		
		return super.handleBack();
	}	

	@Override
	public void onResume() {
		super.onResume();
		postLBS.start();
		if(!editMode) {
			this.pv = PV.POST;
			Tracker.getInstance()
			.pv(this.pv)
			.append(Key.SECONDCATENAME, categoryEnglishName)
			.end();
		}		
	}
	
	@Override
	public void onPause() {
		postLBS.stop();
		PostUtil.extractInputData(layout_txt, params);
		setPhoneAndAddress();
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
	}	

	@Override
	public void onViewCreated (View view, Bundle savedInstanceState){
		super.onViewCreated(view, savedInstanceState);
		showPost();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {		
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
	
	protected void startImgSelDlg(ImageSelectionDialog.ImageContainer[] container){
		if(container != null){
			imgSelBundle.putSerializable(ImageSelectionDialog.KEY_IMG_CONTAINER, container);
		}
		imgSelDlg.setMsgOutBundle(imgSelBundle);
		imgSelDlg.show(getFragmentManager(), null);
	}
	
	
	
	private boolean inPosting = false;
	
	private void doPost(boolean registered, BXLocation location){
		if(inPosting) return;
		showProgress(R.string.dialog_title_info, R.string.dialog_message_waiting, false);
		new Thread(new UpdateThread(registered, location)).start();		
	}
	private boolean usercheck() {
		return (user != null && user.getPhone() != null && !user.getPhone().equals(""));
	}
		
	private void deployDefaultLayout(){
		addCategoryItem();
		HashMap<String, PostGoodsBean> pl = new HashMap<String, PostGoodsBean>();
		for(int i = 1; i < fixedItemNames.length; ++ i){
			PostGoodsBean bean = new PostGoodsBean();
			bean.setControlType("input");
			bean.setDisplayName(fixedItemDisplayNames[i]);
			bean.setName(fixedItemNames[i]);
			bean.setUnit("");
			if(fixedItemNames[i].equals("价格")){
				bean.setNumeric(1);//.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
				bean.setUnit("元");
			}
			pl.put(fixedItemNames[i], bean);
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
		resetData(true);
		Util.saveDataToLocate(getActivity(), FILE_LAST_CATEGORY, cateNames);
		this.showPost();
	}
	
	protected String getCityEnglishName(){
		return GlobalDataManager.getApplication().cityEnglishName;
	}
	
	private void showPost(){
		if(this.categoryEnglishName == null || categoryEnglishName.length() == 0){
			deployDefaultLayout();
			return;
		}

		String cityEnglishName = getCityEnglishName();
		
		Pair<Long, String> pair = Util.loadJsonAndTimestampFromLocate(this.getActivity(), categoryEnglishName + cityEnglishName);
		json = pair.second;
		if (json != null && json.length() > 0) {			
			if (pair.first + (24 * 3600) < System.currentTimeMillis()/1000) {
				showSimpleProgress();
				new Thread(new GetCategoryMetaThread(cityEnglishName)).start();
			} else {
				if(postList == null || postList.size() == 0){
					postList = JsonUtil.getPostGoodsBean(json);
				}
				addCategoryItem();
				buildPostLayout(postList);
				loadCachedData();
			}
		} else {
			showSimpleProgress();
			new Thread(new GetCategoryMetaThread(cityEnglishName)).start();
		}
	}
	
	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.iv_post_finish){
			postFinish();
		}else if(v.getId() == R.id.location){
			Tracker.getInstance().event((!editMode)?BxEvent.POST_INPUTING:BxEvent.EDITPOST_INPUTING).append(Key.ACTION, STRING_DETAIL_POSITION).end();
			
			if(this.detailLocation != null && locationView != null){
				setDetailLocationControl(detailLocation);
			}else if(detailLocation == null){
				Toast.makeText(this.getActivity(), "无法获得当前位置", 0).show();
			}
		}else if(v.getId() == R.id.myImg){
			Tracker.getInstance().event((!editMode)?BxEvent.POST_INPUTING:BxEvent.EDITPOST_INPUTING).append(Key.ACTION, "image").end();
			
			if(!editMode){
				startImgSelDlg(null);
			}
		}else if(v.getId() == R.id.img_description){
			final View et = v.findViewById(R.id.description_input);
			if(et != null){
				et.postDelayed(new Runnable(){
					@Override
					public void run(){
						if (et != null){
							Tracker.getInstance().event((!editMode)?BxEvent.POST_INPUTING:BxEvent.EDITPOST_INPUTING).append(Key.ACTION, STRING_DESCRIPTION).end();
							et.requestFocus();
							InputMethodManager inputMgr = 
									(InputMethodManager) et.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
							inputMgr.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);
						}
					}			
				}, 100);
			}
		}
	}
	
	private void postFinish() {
		Tracker.getInstance()
		.event(!editMode ? BxEvent.POST_POSTBTNCONTENTCLICKED:BxEvent.EDITPOST_POSTBTNCONTENTCLICKED)
		.append(Key.SECONDCATENAME, categoryEnglishName)
		.end();
		
		this.postAction();
	}

	private void setPhoneAndAddress(){
		String phone = params.getData("contact");
		if(phone != null && phone.length() > 0 && !editMode){
			GlobalDataManager.getApplication().setPhoneNumber(phone);
		}
		String address = params.getData(STRING_DETAIL_POSITION);
		if(address != null && address.length() > 0){
			GlobalDataManager.getApplication().setAddress(address);
		}		
	}
	
	private void postAction() {
		PostUtil.extractInputData(layout_txt, params);
		setPhoneAndAddress();
		if(!this.checkInputComplete()){
			return;
		}
		String detailLocationValue = params.getUiData(STRING_DETAIL_POSITION);
		if(this.detailLocation != null && (detailLocationValue == null || detailLocationValue.length() == 0)){
			doPost(usercheck(), detailLocation);
		}else{
			this.sendMessageDelay(MSG_GEOCODING_TIMEOUT, null, 5000);
			this.showSimpleProgress();
			postLBS.retreiveLocation(GlobalDataManager.getApplication().cityName, getFilledLocation());			
		}
	}

	private boolean checkInputComplete() {
		if(this.categoryEnglishName == null || this.categoryEnglishName.equals("")){
			Toast.makeText(this.getActivity(), "请选择分类" ,0).show();
			popupCategorySelectionDialog();
			return false;
		}
		
		for (int i = 0; i < postList.size(); i++) {
			String key = (String) postList.keySet().toArray()[i];
			PostGoodsBean postGoodsBean = postList.get(key);
			if (postGoodsBean.getName().equals(STRING_DESCRIPTION) || 
					(postGoodsBean.getRequired().endsWith("required") && !PostUtil.inArray(postGoodsBean.getName(), hiddenItemNames) && !postGoodsBean.getName().equals("title") && !postGoodsBean.getName().equals(STRING_AREA))) {
				if(!params.containsKey(postGoodsBean.getName()) 
						|| params.getData(postGoodsBean.getName()).equals("")
						|| (postGoodsBean.getUnit() != null && params.getData(postGoodsBean.getName()).equals(postGoodsBean.getUnit()))){
					if(postGoodsBean.getName().equals("images"))continue;
					postResultFail("please entering " + postGoodsBean.getDisplayName() + "!");
					Toast.makeText(this.getActivity(), "请填写" + postGoodsBean.getDisplayName() + "!", 0).show();
					return false;
				}
			}
		}
		return true;
	}
	
	private String getFilledLocation(){
		String toRet = "";
		for(int m = 0; m < layout_txt.getChildCount(); ++ m){
			View v = layout_txt.getChildAt(m);
			PostGoodsBean bean = (PostGoodsBean)v.getTag(PostUtil.HASH_POST_BEAN);
			if(bean == null) continue;
			if(bean.getName().equals(STRING_DETAIL_POSITION)){
				TextView tv = (TextView)v.getTag(PostUtil.HASH_CONTROL);
				if(tv != null && !tv.getText().toString().equals("")){
					toRet = tv.getText().toString();
				}
				break;
			}
		}
		return toRet;
	}
	
	protected void mergeParams(List<String> list){
		
	}

	private class UpdateThread implements Runnable {
		private boolean registered = false;
		private BXLocation location = null;
		private UpdateThread(boolean registered, BXLocation location){
			this.registered = registered;
			this.location = location;
		}
		public void run() {
			inPosting = true;
			String apiName = "ad_add";
			ArrayList<String> list = new ArrayList<String>();

			if(registered){
				list.add("mobile=" + mobile);
				list.add("userToken=" + Util.generateUsertoken(password));	
			}
			
			list.add("categoryEnglishName=" + categoryEnglishName);
			list.add("cityEnglishName=" + GlobalDataManager.getApplication().cityEnglishName);
			list.add("rt=1");
			mergeParams(list);
			if(editMode){
				apiName = "ad_update";
			}
			setDistrictByLocation(location);			
			Pair<Double, Double> coorGoogle = postLBS.retreiveCoorFromGoogle(getFilledLocation());
			list.add("lat=" + coorGoogle.first);
			list.add("lng=" + coorGoogle.second);
			
			Iterator<String> keyIte = params.keyIterator();
			while(keyIte.hasNext()){
				String key = keyIte.next();
				String value = params.getData(key);
				if (value != null && value.length() > 0 && postList.get(key) != null) {
					try{
						list.add(URLEncoder.encode(postList.get(key).getName(), "UTF-8")
								+ "=" + URLEncoder.encode(value, "UTF-8").replaceAll("%7E", "~"));//ugly, replace, what's that? 
						if(postList.get(key).getName().equals(STRING_DESCRIPTION)){//generate title from description
							list.add("title"
									+ "=" + URLEncoder.encode(value.substring(0, Math.min(25, value.length())), "UTF-8").replaceAll("%7E", "~"));
						}
					}catch(UnsupportedEncodingException e){
						e.printStackTrace();
					}
				}
				
			}

			String images = "";
			for (int i = 0; i < bmpUrls.size(); i++) {				
				if(bmpUrls.get(i) != null && bmpUrls.get(i).contains("http:")){
					images += "," + bmpUrls.get(i);
				}
			}
			if(images != null && images.length() > 0 && images.charAt(0) == ','){
				images = images.substring(1);
			}
			if(images != null && images.length() > 0){
				list.add("images=" + images);
			}
			
			String errorMsg = "内部错误，发布失败";
			String url = Communication.getApiUrl(apiName, list);
			try {
				json = Communication.getDataByUrl(url, true);
				if (json != null) {
					JSONObject jsonObject = new JSONObject(json);
					JSONObject json = jsonObject.getJSONObject("error");
					code = json.getInt("code");
					message = replaceTitleToDescription(json.getString("message"));
					sendMessage(3, null);
					errorMsg = message;
				}else {
					errorMsg = "解析错误";
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				errorMsg = "解析错误";
			} catch (Communication.BXHttpException e) {
				if(e.errorCode == 414){
					errorMsg = "内容超出规定长度，请修改后重试";
				}
				else{
					errorMsg = replaceTitleToDescription(e.msg);
				}
				
			} catch(Exception e){
				e.printStackTrace();
				errorMsg = "发布失败";
			}
			
			if (errorMsg.equals("发布成功"))
				postResultSuccess();
			else
				postResultFail(errorMsg);
						
			hideProgress();
			final String fmsg = errorMsg;
			if(getActivity() != null){
				((BaseActivity)getActivity()).runOnUiThread(new Runnable(){
					@Override
					public void run(){
						if(getActivity() != null && fmsg != null){
							Toast.makeText(getActivity(), fmsg, 0).show();
						}
					}
				});
			}
			inPosting = false;
		}
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
		.append(Key.POSTCONTACTTEXTCOUNT, getContactLength())
		.append(Key.POSTDETAILPOSITIONAUTO, autoLocated)
		.end();
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
				.append(Key.POSTCONTACTTEXTCOUNT, getContactLength())
				.append(Key.POSTDETAILPOSITIONAUTO, autoLocated)
				.end();
	}
	
	private int code = -1;
	private String message = "";

	private class GetCategoryMetaThread implements Runnable {
		private String cityEnglishName = null;

		private GetCategoryMetaThread(String cityEnglishName) {
			this.cityEnglishName = cityEnglishName;
		}

		@Override
		public void run() {

			String apiName = "category_meta_post";
			ArrayList<String> list = new ArrayList<String>();
			this.cityEnglishName = (this.cityEnglishName == null ? GlobalDataManager.getApplication().cityEnglishName : this.cityEnglishName);
			list.add("categoryEnglishName=" + categoryEnglishName);
			list.add("cityEnglishName=" + this.cityEnglishName);

			String url = Communication.getApiUrl(apiName, list);
			try {
				json = Communication.getDataByUrl(url, false);
				if (json != null) {
					postList = JsonUtil.getPostGoodsBean(json);
					if(postList == null || postList.size() == 0){
						sendMessage(10, null);
						return;
					}
					Activity activity = getActivity();
					if (activity != null)
					{
						Util.saveJsonAndTimestampToLocate(activity, categoryEnglishName + this.cityEnglishName, json, System.currentTimeMillis()/1000);
						sendMessage(1, null);
					}
				} else {
					sendMessage(2, null);
				}
				return;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				sendMessage(10, null);
				e.printStackTrace();
			} catch (Communication.BXHttpException e){
				
			}
			sendMessage(10, null);
		}
	}
		
	private void loadCachedData()
	{
		if(params.size() == 0) return;

		Iterator<String> it = params.keyIterator();
		while (it.hasNext()){
			String name = it.next();
			for (int i=0; i<layout_txt.getChildCount(); i++)
			{
				View v = layout_txt.getChildAt(i);
				PostGoodsBean bean = (PostGoodsBean)v.getTag(PostUtil.HASH_POST_BEAN);
				if(bean == null || 
						!bean.getName().equals(name)//check display name 
						) continue;
				View control = (View)v.getTag(PostUtil.HASH_CONTROL);
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
			if(!PostUtil.inArray(key, this.fixedItemNames)){
				params.remove(key);
				ite = params.keyIterator();
			}
		}
	}
	
	private void resetData(boolean clearImgs){
		if(this.layout_txt != null){
			View v = layout_txt.findViewById(R.id.img_description);
			layout_txt.removeAllViews();
			layout_txt.addView(v);
		}
		postList.clear();
		
		
		if(null != Util.loadDataFromLocate(getActivity(), FILE_LAST_CATEGORY, String.class)){
			clearCategoryParameters();
			if(clearImgs){
				listUrl.clear();
				this.bmpUrls.clear();
				if(this.imgSelDlg != null){
					imgSelDlg.clearResource();
				}
				this.imgSelBundle.clear();// = null;
				
				layout_txt.findViewById(R.id.imgCout).setVisibility(View.INVISIBLE);
				
				params.remove(STRING_DESCRIPTION);
				params.remove("价格");
			}
		}
	}

	private void handleBackWithData(int message, Object obj) {
		if(message == PostGoodsFragment.VALUE_LOGIN_SUCCEEDED){
			postAction();
			return;
		}else if(message == MSG_CATEGORY_SEL_BACK && obj != null){
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
		if (postBean.getName().equals("contact") &&
			(postBean.getValues() == null || postBean.getValues().isEmpty()) &&
			(user != null && user.getPhone() != null && user.getPhone().length() > 0)){
			List<String> valueList = new ArrayList<String>(1);
			valueList.add(user.getPhone());
			postBean.setValues(valueList);
			postBean.setLabels(valueList);
		}	
		
		ViewGroup layout = createItemByPostBean(postBean);//FIXME:

		if(layout != null && !postBean.getName().equals(STRING_DETAIL_POSITION)){
			ViewGroup.LayoutParams lp = layout.getLayoutParams();
			lp.height = getResources().getDimensionPixelOffset(R.dimen.post_item_height);
			layout.setLayoutParams(lp);
		}

		if(postBean.getName().equals(STRING_DETAIL_POSITION)){
			layout.findViewById(R.id.location).setOnClickListener(this);
			((TextView)layout.findViewById(R.id.postinput)).setHint("请输入");
			locationView = layout;
			
			String address = GlobalDataManager.getApplication().getAddress();
			if(address != null && address.length() > 0){
				((TextView)layout.findViewById(R.id.postinput)).setText(address);
			}
		}else if(postBean.getName().equals("contact") && layout != null){
			etContact = ((EditText)layout.getTag(PostUtil.HASH_CONTROL));
			etContact.setFilters(new InputFilter[]{new InputFilter.LengthFilter(15)});
			String phone = GlobalDataManager.getApplication().getPhoneNumber();
			if(editMode){
				etContact.setText(getAdContact());
			}else{
				if(phone != null && phone.length() > 0){
					etContact.setText(phone);
				}
			}
		}else if (postBean.getName().equals(STRING_DESCRIPTION) && layout != null){
			etDescription = (EditText) layout.getTag(PostUtil.HASH_CONTROL);
		}
		
		if(layout != null){
			layout_txt.addView(layout);
		}
	}

	private void popupCategorySelectionDialog(){
		Bundle bundle = createArguments(null, null);
		bundle.putSerializable("items", (Serializable) Arrays.asList(texts));
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
		if(editMode)return;
		if(layout_txt != null){
			if(layout_txt.findViewById(R.id.arrow_down) != null) return;
		}
		LayoutInflater inflater = LayoutInflater.from(activity);
		View categoryItem = inflater.inflate(R.layout.item_post_select, null);
		
		categoryItem.setTag(PostUtil.HASH_CONTROL, categoryItem.findViewById(R.id.posthint));//tag
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
		}
		
		LinearLayout.LayoutParams layoutParams = (LayoutParams) categoryItem.getLayoutParams();
		if (layoutParams == null)
			layoutParams = new LinearLayout.LayoutParams(
			     LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
		layoutParams.bottomMargin = categoryItem.getContext().getResources().getDimensionPixelOffset(R.dimen.post_marginbottom);		
		layoutParams.height = getResources().getDimensionPixelOffset(R.dimen.post_item_height);
		categoryItem.setLayoutParams(layoutParams);
		
		layout_txt.addView(categoryItem);
	}
	
	private String[] fixedItemNames = {"images", STRING_DESCRIPTION, "价格", "contact", STRING_DETAIL_POSITION};
	private String[] fixedItemDisplayNames = {"", "描述", "价格", "联系电话", STRING_DETAIL_POSITION};
	private String[] hiddenItemNames = {"wanted", "faburen"};
	private boolean autoLocated;

	private void buildFixedPostLayout(HashMap<String, PostGoodsBean> pl){
		if(pl == null || pl.size() == 0) return;
		
		HashMap<String, PostGoodsBean> pm = new HashMap<String, PostGoodsBean>();
		Object[] postListKeySetArray = pl.keySet().toArray();
		for(int i = 0; i < pl.size(); ++ i){
			for(int j = 0; j < fixedItemNames.length; ++ j){
				PostGoodsBean bean = pl.get(postListKeySetArray[i]);
				if(bean.getName().equals(fixedItemNames[j])){					
					pm.put(fixedItemNames[j], bean);
					break;
				}
			}
		}
		
		if(pm.containsKey(STRING_DESCRIPTION)){
			PostGoodsBean bean = pm.get(STRING_DESCRIPTION);
			if(bean != null){
				View v = layout_txt.findViewById(R.id.img_description);
				EditText text = (EditText)v.findViewById(R.id.description_input);
				text.setText("");
				text.setOnTouchListener(new OnTouchListener() {
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						if (event.getAction() == MotionEvent.ACTION_DOWN) {
							Tracker.getInstance().event((editMode)?BxEvent.POST_INPUTING:BxEvent.EDITPOST_INPUTING).append(Key.ACTION, STRING_DESCRIPTION).end();
						}
						return false;
					}
				});

				text.setHint("请输入" + bean.getDisplayName());
				v.setTag(PostUtil.HASH_POST_BEAN, bean);
				v.setTag(PostUtil.HASH_CONTROL, text);
				v.setOnClickListener(this);
				
				v.findViewById(R.id.myImg).setOnClickListener(this);
				((ImageView)v.findViewById(R.id.myImg)).setImageResource(R.drawable.btn_add_picture);
				if(imgSelBundle != null){
		    		Object[] container = (Object[])imgSelBundle.getSerializable(ImageSelectionDialog.KEY_IMG_CONTAINER);
					if(container != null && container.length > 0
							&& ((ImageSelectionDialog.ImageContainer)container[0]).status == ImageSelectionDialog.ImageStatus.ImageStatus_Normal){
						Bitmap bp = ImageSelectionDialog.getThumbnailWithPath(((ImageSelectionDialog.ImageContainer)container[0]).thumbnailPath);
						if(bp != null){
							((ImageView)v.findViewById(R.id.myImg)).setImageBitmap(bp);
							((TextView)v.findViewById(R.id.imgCout)).setVisibility(View.VISIBLE);
							int count = 0;
							for(int i = 0; i < container.length; ++ i){
								if(((ImageSelectionDialog.ImageContainer)container[i]).status == ImageSelectionDialog.ImageStatus.ImageStatus_Unset){
									break;
								}
								++ count;
							}
							((TextView)v.findViewById(R.id.imgCout)).setText(String.valueOf(count));
						}
					}
				}				
			}			
		}
		
		for(int i = 0; i < fixedItemNames.length; ++ i){
			if(pm.containsKey(fixedItemNames[i]) && !fixedItemNames[i].equals(STRING_DESCRIPTION)){
				this.appendBeanToLayout(pm.get(fixedItemNames[i]));
			}else if(!pm.containsKey(fixedItemNames[i])){
				params.remove(fixedItemNames[i]);
			}
		}
	}
	
	private void addHiddenItemsToParams(){
		if (postList == null || postList.isEmpty())
			return ;
		Set<String> keySet = postList.keySet();
		for (String key : keySet){
			PostGoodsBean bean = postList.get(key);
			for (int i = 0; i<  hiddenItemNames.length; i++)
			{
				if (bean.getName().equals(hiddenItemNames[i]))
				{
					String defaultValue = bean.getDefaultValue();
					if (defaultValue != null && defaultValue.length() > 0) {
						//String key, String uiValue, String data
						this.params.put(bean.getName(), defaultValue, defaultValue);
					} else {
						this.params.put(bean.getName(), bean.getLabels().get(0), bean.getValues().get(0));
					}
					break;
				}
			}
		}
	}
	
	protected void editPostUI(){
		
	}
	
	private void buildPostLayout(HashMap<String, PostGoodsBean> pl){
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
			
			if(PostUtil.inArray(postBean.getName(), fixedItemNames) || postBean.getName().equals("title") || PostUtil.inArray(postBean.getName(), hiddenItemNames))
				continue;
			
			if(postBean.getName().equals(STRING_AREA)){
				continue;
			}
			this.appendBeanToLayout(postBean);
		}
		if(editMode){
			editPostUI();
		}
		originParams.merge(params);
		PostUtil.extractInputData(layout_txt, originParams);	
	}

	@Override
	protected void handleMessage(Message msg, final Activity activity, View rootView) {

		if(msg.what != MSG_GETLOCATION_TIMEOUT){
			hideProgress();
		}
		
		switch (msg.what) {
		case MSG_DIALOG_BACK_WITH_DATA:{
			Bundle bundle = (Bundle)msg.obj;
			handleBackWithData(bundle.getInt(ARG_COMMON_REQ_CODE), bundle.getSerializable("lastChoise"));
			break;
		}		
		case ImageSelectionDialog.MSG_IMG_SEL_DISMISSED:{
			if(imgSelBundle != null){
				ImageSelectionDialog.ImageContainer[] container = 
						(ImageSelectionDialog.ImageContainer[])imgSelBundle.getSerializable(ImageSelectionDialog.KEY_IMG_CONTAINER);
				if(getView() != null && container != null){
					ImageView iv = (ImageView)this.getView().findViewById(R.id.myImg);
					if(iv != null){						
						if(container != null 
								&& container.length > 0
								&& container[0].status == ImageSelectionDialog.ImageStatus.ImageStatus_Normal
								&& container[0].bitmapPath != null){
							Bitmap thumbnail = ImageSelectionDialog.getThumbnailWithPath(container[0].thumbnailPath);
							if(iv != null && thumbnail != null){
								iv.setImageBitmap(thumbnail);
							}else{
								iv.setImageResource(R.drawable.btn_add_picture);
							}
						}else{
							iv.setImageResource(R.drawable.btn_add_picture);
						}
					}
					
					TextView tv = (TextView)getView().findViewById(R.id.imgCout);
					if(iv != null){
						int containerCount = 0;
						for(int i = 0; i < container.length; ++ i){
							if(container[i].status == ImageSelectionDialog.ImageStatus.ImageStatus_Unset){
								break;
							}else if(container[i].status == ImageSelectionDialog.ImageStatus.ImageStatus_Normal){
								++ containerCount;
							}
						}
						if(containerCount > 0){
							tv.setText(String.valueOf(containerCount));
							tv.setVisibility(View.VISIBLE);
						}else{
							tv.setVisibility(View.INVISIBLE);
						}
					}
					
					bmpUrls.clear();
					if(container != null){
						for(int i = 0; i < container.length; ++ i){
							if(container[i].status == ImageSelectionDialog.ImageStatus.ImageStatus_Normal){
								bmpUrls.add(container[i].bitmapUrl);
							}
						}
					}
					
				}
			}
		}
			break;
		case 1:
			addCategoryItem();
			buildPostLayout(postList);
			loadCachedData();
			break;

		case 2:
			hideProgress();
			AlertDialog.Builder builder = new AlertDialog.Builder(activity);
			builder.setTitle("提示:")
					.setMessage(message)
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
							});
			builder.create().show();
			this.getView().findViewById(R.id.goodscontent).setVisibility(View.GONE);
			this.getView().findViewById(R.id.networkErrorView).setVisibility(View.VISIBLE);
			this.reCreateTitle();
			this.refreshHeader();

			break;
		case 3:
			try {
				hideProgress();
				JSONObject jsonObject = new JSONObject(json);
				String id;
				boolean isRegisteredUser = false;
				try {
					id = jsonObject.getString("id");
					isRegisteredUser = jsonObject.getBoolean("contactIsRegisteredUser");
				} catch (Exception e) {
					id = "";
					e.printStackTrace();
				}
				JSONObject json = jsonObject.getJSONObject("error");
				String message = replaceTitleToDescription(json.getString("message"));			
				if (!id.equals("") && code == 0) {
					Toast.makeText(activity, message, 0).show();
					final Bundle args = createArguments(null, null);
					args.putInt("forceUpdate", 1);
					resetData(!editMode);
					if(!editMode){
						showPost();
						String lp = getArguments().getString("lastPost");
						if(lp != null && !lp.equals("")){
							lp += "," + id;
						}else{
							lp = id;
						}
						args.putString("lastPost", lp);
						
						args.putString("cateEnglishName", categoryEnglishName);
						args.putBoolean(KEY_IS_EDITPOST, editMode);
						
						args.putBoolean(KEY_LAST_POST_CONTACT_USER,  isRegisteredUser);
//						PostGoodsFragment.this.finishFragment(PostGoodsFragment.MSG_POST_SUCCEED, null);
						if(activity != null){							
							args.putInt(PersonalPostFragment.TYPE_KEY, PersonalPostFragment.TYPE_MYPOST);
							
							Intent intent = new Intent(CommonIntentAction.ACTION_BROADCAST_POST_FINISH);
							intent.putExtras(args);
							activity.sendBroadcast(intent);
//							((BaseActivity)activity).pushFragment(new PersonalPostFragment(), args, false);
						}						
					}else{
						PostGoodsFragment.this.finishFragment(PostGoodsFragment.MSG_POST_SUCCEED, null);
					}
				}else{
					if(code == 505){
						AlertDialog.Builder bd = new AlertDialog.Builder(this.getActivity());
		                bd.setTitle("")
		                        .setMessage(message)
		                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
		                            @Override
		                            public void onClick(DialogInterface dialog, int which) {
		                                dialog.dismiss();
		        						if(activity != null){
		        							resetData(true);
		        							showPost();
		        							Bundle args = createArguments(null, null);
		        							args.putInt(PersonalPostFragment.TYPE_KEY, PersonalPostFragment.TYPE_MYPOST);
//		        							args.putString("505id", id);
//		        							((BaseActivity)activity).pushFragment(new PersonalPostFragment(), args, false);
		        							Intent intent = new Intent(CommonIntentAction.ACTION_BROADCAST_POST_FINISH);
		        							intent.putExtras(args);
		        							activity.sendBroadcast(intent);							
		        						}
		                            }
		                        });
		                AlertDialog alert = bd.create();
		                alert.show();	
					}
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			break;
		case 10:
			hideProgress();
			Toast.makeText(activity, "网络连接失败，请检查设置！", 3).show();
			this.getView().findViewById(R.id.goodscontent).setVisibility(View.GONE);
			this.getView().findViewById(R.id.networkErrorView).setVisibility(View.VISIBLE);		
			this.reCreateTitle();
			this.refreshHeader();
			break;
		case MSG_GEOCODING_TIMEOUT:
		case PostLocationService.MSG_GEOCODING_FETCHED:			
				showSimpleProgress();
				(new Thread(new UpdateThread(usercheck(), msg.obj == null ? null : (BXLocation)msg.obj))).start();
			break;
		case PostLocationService.MSG_GPS_LOC_FETCHED:
			detailLocation = (BXLocation)msg.obj;
			break;
		}
	}

	private String replaceTitleToDescription(String msg) {
		// replace title to description in message
		PostGoodsBean titleBean = null, descriptionBean = null;
		for (String key : postList.keySet()) {
			PostGoodsBean bean = postList.get(key);
			if (bean.getName().equals("title")) 
				titleBean = bean;
			if (bean.getName().equals(STRING_DESCRIPTION))
				descriptionBean = bean;
		}
		if (titleBean != null && descriptionBean != null)
			msg = msg.replaceAll(titleBean.getDisplayName(), descriptionBean.getDisplayName());
		return msg;
	}
	
	
	private int imgHeight = 0;
	
	////to fix stupid system error. all text area will be the same content after app is brought to front when activity not remain is checked
	private void setInputContent(){		
		if(layout_txt == null) return;
		for(int i = 0; i < layout_txt.getChildCount(); ++ i){
			View v = layout_txt.getChildAt(i);
			PostGoodsBean bean = (PostGoodsBean)v.getTag(PostUtil.HASH_POST_BEAN);
			if(bean == null) continue;
			View control = (View)v.getTag(PostUtil.HASH_CONTROL);
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
							String phone = GlobalDataManager.getApplication().getPhoneNumber();
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
	private void updateUserData(){
		user = Util.getCurrentUser();
		if(user != null){
			this.mobile = Util.getCurrentUser().getPhone();
			this.password = Util.getCurrentUser().getPassword();
		}
	}
	@Override
	public void onStart(){
		super.onStart();
		setInputContent();
		updateUserData();
	}
	
	@Override
	public void initTitle(TitleDef title){
		title.m_visible = true;
		title.m_title = "免费发布";//(categoryName == null || categoryName.equals("")) ? "发布" : categoryName;
	}
	
	private ViewGroup createItemByPostBean(PostGoodsBean postBean){
		Activity activity = getActivity();
		ViewGroup layout = PostUtil.createItemByPostBean(postBean, activity);

		if (layout == null)
			return null;

		if(postBean.getControlType().equals("select") || postBean.getControlType().equals("checkbox")){
			final String actionName = ((PostGoodsBean)layout.getTag(PostUtil.HASH_POST_BEAN)).getDisplayName();
			layout.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					Tracker.getInstance().event((!editMode) ? BxEvent.POST_INPUTING:BxEvent.EDITPOST_INPUTING).append(Key.ACTION, actionName).end();

					PostGoodsBean postBean = (PostGoodsBean) v.getTag(PostUtil.HASH_POST_BEAN);

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
								TextView txview = (TextView)v.getTag(PostUtil.HASH_CONTROL);
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
							TextView txview = (TextView)v.getTag(PostUtil.HASH_CONTROL);
							if (txview !=  null)
							{
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
			final String actionName = ((PostGoodsBean)layout.getTag(PostUtil.HASH_POST_BEAN)).getDisplayName();
			((View)layout.getTag(PostUtil.HASH_CONTROL)).setOnTouchListener(new OnTouchListener() {
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
					View ctrl = (View) v.getTag(PostUtil.HASH_CONTROL);
					ctrl.requestFocus();
					InputMethodManager inputMgr = 
							(InputMethodManager) ctrl.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
					inputMgr.showSoftInput(ctrl, InputMethodManager.SHOW_IMPLICIT);
				}
			});			
		}

		LinearLayout.LayoutParams layoutParams = (LayoutParams) layout.getLayoutParams();
		if (layoutParams == null)
			layoutParams = new LinearLayout.LayoutParams(
			     LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
		layoutParams.bottomMargin = layout.getContext().getResources().getDimensionPixelOffset(R.dimen.post_marginbottom);
		layout.setLayoutParams(layoutParams);
		
		return layout;
	}
	
	private void setDistrictByLocation(BXLocation location){
		if(location == null || location.subCityName == null) return;
		if(this.postList != null && postList.size() > 0){
			Object[] postListKeySetArray = postList.keySet().toArray();
			for(int i = 0; i < postList.size(); ++ i){
				PostGoodsBean bean = postList.get(postListKeySetArray[i]);
				if(bean.getName().equals(STRING_AREA)){
					if(bean.getLabels() != null){
						for(int t = 0; t < bean.getLabels().size(); ++ t){
							if(location.subCityName.contains(bean.getLabels().get(t))){
								params.put(bean.getName(), bean.getLabels().get(t), bean.getValues().get(t));
								originParams.put(bean.getName(), bean.getLabels().get(t), bean.getValues().get(t));
								return;
							}
						}
					}						
				}
			}
		}		
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
}
