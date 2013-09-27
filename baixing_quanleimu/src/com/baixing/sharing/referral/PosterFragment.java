// zengjin@baixing.net
package com.baixing.sharing.referral;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.baixing.activity.BaseFragment;
import com.baixing.broadcast.CommonIntentAction;
import com.baixing.data.GlobalDataManager;
import com.baixing.entity.AdList;
import com.baixing.entity.BXLocation;
import com.baixing.entity.BXThumbnail;
import com.baixing.entity.PostGoodsBean;
import com.baixing.entity.UserBean;
import com.baixing.jsonutil.JsonUtil;
import com.baixing.network.api.ApiError;
import com.baixing.network.api.ApiParams;
import com.baixing.network.api.BaseApiCommand;
import com.baixing.tracking.TrackConfig.TrackMobile.BxEvent;
import com.baixing.tracking.TrackConfig.TrackMobile.Key;
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
import com.baixing.util.post.PostNetworkService.PostResultData;
import com.baixing.util.post.PostUtil;
import com.baixing.view.fragment.ContactAndAddressDetailFragment;
import com.baixing.view.fragment.LoginFragment;
import com.baixing.view.fragment.PostParamsHolder;
import com.baixing.view.fragment.RegisterFragment;
import com.quanleimu.activity.R;
import com.umeng.common.Log;

public class PosterFragment extends BaseFragment implements OnClickListener,
		Callback {

	private static final int IMG_STATE_UPLOADING = 1;
	private static final int IMG_STATE_UPLOADED = 2;
	private static final int IMG_STATE_FAIL = 3;
	private static final int MSG_GEOCODING_TIMEOUT = 0x00010011;
	static final String KEY_IS_EDITPOST = "isEditPost";
	static final int MSG_POST_SUCCEED = 0xF0000010;
	protected String cityEnglishName = "";
	protected LinearLayout layout_txt;
	private LinkedHashMap<String, PostGoodsBean> postList = new LinkedHashMap<String, PostGoodsBean>();
	private static final int NONE = 0;
	private static final int MSG_UPDATE_IMAGE_LIST = 13;
	private static final int MSG_IMAGE_STATE_CHANGE = 14;
	private static final int MSG_GET_AD_FAIL = 15;
	private static final int MSG_GET_AD_SUCCED = 16;
	protected PostParamsHolder params = new PostParamsHolder();
	private BXLocation detailLocation = null;
	protected List<String> bmpUrls = new ArrayList<String>();
	private EditText etContact = null;
	private PostLocationService postLBS;

	protected ArrayList<String> photoList = new ArrayList<String>();
	protected boolean isNewPost = true;
	private boolean finishRightNow = false;
	private long lastClickPostTime = 0;
	
	private Button scanQRCode;

	@Override
	public void onActivityResult(final int requestCode, final int resultCode,
			final Intent data) {
		if (resultCode == NONE) {
			return;
		} else if (resultCode == Activity.RESULT_FIRST_USER) {
			finishRightNow = true;
			return;
		}
		
		Log.d(TAG, requestCode + "");
		if (resultCode == Activity.RESULT_OK && requestCode == CommonIntentAction.QRCodeReqCode.SCAN) {
			String id = getQRCodeId(data.getExtras().getString("qrcode"));
			if (id != null) {
				scanQRCode.setText(id);
			}
			return;
		}
		
		if (resultCode == Activity.RESULT_OK) {
			photoList.clear();
			if (data.getExtras().containsKey(
					CommonIntentAction.EXTRA_IMAGE_LIST)) {
				ArrayList<String> result = data
						.getStringArrayListExtra(CommonIntentAction.EXTRA_IMAGE_LIST);
				photoList.addAll(result);
			}
		}

		handler.sendEmptyMessage(MSG_UPDATE_IMAGE_LIST);
	}
	
	private String getQRCodeId(String qrCodeStr) {
		int start = qrCodeStr.indexOf("codeId=") + "codeId=".length();
		if (start != "codeId=".length() - 1) {
			return qrCodeStr.substring(start);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		PerformanceTracker.stamp(Event.E_PGFrag_OnCreate_Start);
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			postList.putAll((HashMap<String, PostGoodsBean>) savedInstanceState
					.getSerializable("postList"));
			params = (PostParamsHolder) savedInstanceState
					.getSerializable("params");
			photoList.addAll((List<String>) savedInstanceState
					.getSerializable("listUrl"));
		}

		this.postLBS = new PostLocationService(this.handler);
		cityEnglishName = GlobalDataManager.getInstance().getCityEnglishName();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		PostUtil.extractInputData(layout_txt, params);
		outState.putSerializable("params", params);
		outState.putSerializable("postList", postList);
		outState.putSerializable("listUrl", photoList);
	}

	private void doClearUpImages() {
		this.photoList.clear();
		ImageUploader.getInstance().clearAll();
	}

	@Override
	public boolean handleBack() {
		AlertDialog.Builder builder = new Builder(getActivity());
		builder.setMessage("退出发布？");
		builder.setNegativeButton("否", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// Do nothing.
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
		if (finishRightNow) {
			finishRightNow = false;
			getView().post(new Runnable() {
				@Override
				public void run() {
					doClearUpImages();
					finishFragment();
				}
			});
			return;
		}
		isActive = true;
		postLBS.start();

		paused = false;

		if (getView() != null) {
			getView().post(new Runnable() {
				@Override
				public void run() {
					int titleWidth = getView().findViewById(R.id.linearTop)
							.getWidth();
					int leftWidth = getView().findViewById(R.id.left_action)
							.getWidth();
					int rightIconWidth = getView()
							.findViewById(R.id.imageView1).getWidth();
					int padding = getView().findViewById(R.id.ll_post_title)
							.getPaddingRight();
					int maxWidth = titleWidth - 2 * leftWidth - rightIconWidth
							- 4 * padding;
					if (maxWidth > 0) {
						((TextView) getView().findViewById(R.id.tv_title_post))
								.setMaxWidth(maxWidth);
					}
				}
			});
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
		paused = true;
	}

	@Override
	public void onDestroy() {
		if (layout_txt != null) {
			for (int i = 0; i < layout_txt.getChildCount(); ++i) {
				View child = layout_txt.getChildAt(i);
				if (child != null) {
					child.setTag(PostCommonValues.HASH_CONTROL, null);
				}
			}
		}
		super.onDestroy();
	}

	@Override
	public void onStackTop(boolean isBack) {
		if (isBack) {
			final ScrollView scroll = (ScrollView) this.getView().findViewById(
					R.id.goodscontent);
			scroll.post(new Runnable() {
				@Override
				public void run() {
					scroll.fullScroll(View.FOCUS_DOWN);
				}
			});
		}

		if (isNewPost) {
			this.startImgSelDlg(Activity.RESULT_FIRST_USER, "跳过\n拍照");
		}
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		showPost();
	}

	@Override
	public View onInitializeView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		ViewGroup v = (ViewGroup) inflater.inflate(R.layout.referralpostview,
				null);
		layout_txt = (LinearLayout) v.findViewById(R.id.layout_txt);
		Button button = (Button) v.findViewById(R.id.iv_post_finish);
		button.setOnClickListener(this);
		button.setText("立即免费发布");
		
		scanQRCode = (Button) v.findViewById(R.id.btn_qrcode_scan);
		scanQRCode.setOnClickListener(this);
		scanQRCode.setText("扫描二维码");

		return v;
	}

	protected void startImgSelDlg(final int cancelResultCode,
			String finishActionLabel) {
		PerformanceTracker.stamp(Event.E_Send_Camera_Bootup);
		Intent backIntent = new Intent();
		backIntent.setClass(getActivity(), getActivity().getClass());

		Intent goIntent = new Intent();
		goIntent.putExtra(CommonIntentAction.EXTRA_COMMON_INTENT, backIntent);
		goIntent.setAction(CommonIntentAction.ACTION_IMAGE_CAPTURE);
		goIntent.putExtra(CommonIntentAction.EXTRA_COMMON_REQUST_CODE,
				CommonIntentAction.PhotoReqCode.PHOTOHRAPH);
		goIntent.putStringArrayListExtra(CommonIntentAction.EXTRA_IMAGE_LIST,
				this.photoList);
		goIntent.putExtra(CommonIntentAction.EXTRA_FINISH_ACTION_LABEL,
				finishActionLabel);
		goIntent.putExtra(CommonIntentAction.EXTRA_COMMON_FINISH_CODE,
				cancelResultCode);
		getActivity().startActivity(goIntent);
	}

	private void deployDefaultLayout() {
		HashMap<String, PostGoodsBean> pl = new HashMap<String, PostGoodsBean>();
		for (int i = 1; i < PostCommonValues.fixedItemNames.length; ++i) {
			if (PostCommonValues.fixedItemNames[i]
					.equals(PostCommonValues.STRING_DESCRIPTION)
					|| PostCommonValues.fixedItemNames[i].equals("价格")) {
				continue;
			}
			PostGoodsBean bean = new PostGoodsBean();
			bean.setControlType("input");
			bean.setDisplayName(PostCommonValues.fixedItemDisplayNames[i]);
			bean.setName(PostCommonValues.fixedItemNames[i]);
			bean.setUnit("");
			pl.put(PostCommonValues.fixedItemNames[i], bean);
		}
		buildPostLayout(pl);
	}

	protected String getCityEnglishName() {
		return cityEnglishName;
	}

	private void showPost() {
		deployDefaultLayout();
	}

	@Override
	public void onClick(final View v) {
		switch (v.getId()) {
		case R.id.btn_qrcode_scan:
			Intent backIntent = new Intent();
			backIntent.setClass(getActivity(), getActivity().getClass());
			
			Intent goIntent = new Intent();
			goIntent.putExtra(CommonIntentAction.EXTRA_COMMON_INTENT, backIntent);
			goIntent.setAction(CommonIntentAction.EXTRA_QRCODE_SCAN_REQUEST);
			goIntent.putExtra(CommonIntentAction.EXTRA_COMMON_REQUST_CODE, CommonIntentAction.QRCodeReqCode.SCAN);
			getActivity().startActivity(goIntent);
			break;
		case R.id.delete_btn:
			final String img = (String) v.getTag();

			this.showAlert(null, "是否删除该照片", new DialogAction(R.string.yes) {
				public void doAction() {
					ImageUploader.getInstance().cancel(img);
					if (photoList.remove(img)) {
						ViewGroup parent = (ViewGroup) getView().findViewById(
								R.id.image_list_parent);
						View v = findImageViewByTag(img);
						if (v != null)
							parent.removeView(v);
						showAddImageButton(parent,
								LayoutInflater.from(v.getContext()), true);
					}
				}
			}, null);

			break;
		case R.id.iv_post_finish:
			if (Math.abs(System.currentTimeMillis() - lastClickPostTime) > 500) {
				this.postAction();
				lastClickPostTime = System.currentTimeMillis();
			}
			break;
		case R.id.add_post_image:
			startImgSelDlg(Activity.RESULT_CANCELED, "完成");
			break;
		case R.id.img_description:
			final View et = v.findViewById(R.id.description_input);
			if (et != null) {
				et.postDelayed(new Runnable() {
					@Override
					public void run() {
						if (et != null) {
							InputMethodManager inputMgr = (InputMethodManager) et
									.getContext().getSystemService(
											Context.INPUT_METHOD_SERVICE);
							inputMgr.showSoftInput(et,
									InputMethodManager.SHOW_IMPLICIT);
						}
					}
				}, 100);
			}
			break;
		case R.id.postinputlayout:
			final View et2 = v.findViewById(R.id.postinput);
			if (et2 != null) {
				et2.postDelayed(new Runnable() {
					@Override
					public void run() {
						if (et2 != null) {
							et2.requestFocus();
							InputMethodManager inputMgr = (InputMethodManager) et2
									.getContext().getSystemService(
											Context.INPUT_METHOD_SERVICE);
							inputMgr.showSoftInput(et2,
									InputMethodManager.SHOW_IMPLICIT);
						}
					}
				}, 100);
			}
			break;
		}
	}

	private void setPhoneAndAddress() {
		String phone = params.getData("contact");
		if (phone != null && phone.length() > 0) {
			GlobalDataManager.getInstance().setPhoneNumber(phone);
		}
		String address = params
				.getData(PostCommonValues.STRING_DETAIL_POSITION);
		if (address != null && address.length() > 0) {
			GlobalDataManager.getInstance().setAddress(address);
		}
	}

	private void postAction() {
		String mobile = "13661812345";
		String addr = "上海市浦东新区蔡伦路1159号";
		String images = "http://sd.jpg,http://di.jpg";
		String qrcode = "8dj30du3";
		savePosterCmd(mobile, addr, images, qrcode);
	}

	private void savePosterCmd(String businessMobile, String businessAddr, String imageUrls,
			String qrCodeID) {
		String promoterMobile = GlobalDataManager.getInstance().getAccountManager().getCurrentUser().getPhone();
		String promoterUdid = Util.getDeviceUdid(getAppContext());
		String gpsAddress = getDetailLocation(detailLocation);
		
		ApiParams params = new ApiParams();
		params.addParam("promoterMobile", promoterMobile);
		params.addParam("promoterUdid", promoterUdid);
		params.addParam("images", imageUrls);
		params.addParam("storeMobile", businessMobile);
		params.addParam("storeAddr", businessAddr);
		params.addParam("gpsAddr", gpsAddress);
		params.addParam("qrcodeId", qrCodeID);
		String jsonResponse = BaseApiCommand.createCommand("save_promo_haibao", true, params).executeSync(getAppContext());
		String posterId = getPosterId(jsonResponse);
		if (posterId != null) {
			if (savePosterLog(promoterMobile, ReferralUtil.TASK_HAIBAO, businessMobile)) {
				Toast.makeText(getAppContext(), "海报推广成功！", Toast.LENGTH_SHORT).show();
				finishFragment();
			} else {
				Toast.makeText(getAppContext(), "推广记录保存失败", Toast.LENGTH_SHORT).show();
			}
		} else {
			Toast.makeText(getAppContext(), "发送失败，请检查网络后重试", Toast.LENGTH_SHORT).show();
		}
	}
	
	private boolean savePosterLog(String promoterMobile, int taskType, String userMobile) {
		ApiParams logParams = new ApiParams();
		logParams.addParam("promoterMobile", promoterMobile);
		logParams.addParam("taskType", ReferralUtil.TASK_HAIBAO);
		logParams.addParam("userMobile", userMobile);
		String logResponse = BaseApiCommand.createCommand("save_promo_log", true, logParams).executeSync(getAppContext());
		try {
			JSONObject obj = new JSONObject(logResponse);
			if (obj != null) {
				JSONObject error = obj.getJSONObject("error");
				if (error != null) {
					String code = error.getString("code");
					if (code != null && code.equals("0")) {
						return true;
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return false;
		}
		return false;
	}
	
	private String getPosterId(String jsonResult) {
		try {
			JSONObject obj = new JSONObject(jsonResult);
			if (obj != null) {
				JSONObject error = obj.getJSONObject("error");
				if (error != null) {
					String code = error.getString("code");
					if (code != null && code.equals("0")) {
						return obj.getString("id"); 
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return null;
	}
	
	protected void postAd(BXLocation location) {
		ApiParams apiParam = new ApiParams();

		HashMap<String, String> mapParams = new HashMap<String, String>();
		Iterator<String> ite = params.keyIterator();
		while (ite.hasNext()) {
			String key = ite.next();
			String value = params.getData(key);
			mapParams.put(key, value);
		}	
		
		bmpUrls.clear();
		bmpUrls.addAll(ImageUploader.getInstance().getServerUrlList());
		if(bmpUrls != null){
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
				apiParam.addParam("images", images);
			}
		}

		String phone = mapParams.get("contact");

		UserBean curUser = GlobalDataManager.getInstance().getAccountManager()
				.getCurrentUser();

		if (curUser != null && curUser.getPhone() != null
				&& curUser.getPhone().length() > 0) {
			phone = curUser.getPhone();
		}
	}
	
	private int getContactLength() {
		return etContact != null ? etContact.getText().length() : 0;
	}

	private int getImgCount() {
		int imgCount = 0;
		for (int i = 0; i < bmpUrls.size(); i++) {
			if (bmpUrls.get(i) != null && bmpUrls.get(i).contains("http:")) {
				imgCount++;
			}
		}
		return imgCount;
	}

	private void postResultSuccess() {
		BxEvent event = BxEvent.EDITPOST_POSTRESULT;
		boolean autoPosition = false;
		if (detailLocation != null
				&& PostUtil
						.getLocationSummary(detailLocation)
						.equals(params
								.getData(PostCommonValues.STRING_DETAIL_POSITION))) {
			autoPosition = true;
		}
		Tracker.getInstance().event(event)
				.append(Key.POSTSTATUS, 1)
				.append(Key.POSTPICSCOUNT, getImgCount())
				.append(Key.POSTCONTACTTEXTCOUNT, getContactLength())
				.append(Key.POSTDETAILPOSITIONAUTO, autoPosition).end();
	}

	private void postResultFail(String errorMsg) {
		BxEvent event = BxEvent.EDITPOST_POSTRESULT;
		Tracker.getInstance().event(event)
				.append(Key.POSTSTATUS, 0).append(Key.POSTFAILREASON, errorMsg)
				.append(Key.POSTPICSCOUNT, getImgCount())
				.append(Key.POSTCONTACTTEXTCOUNT, getContactLength()).end();
	}

	private void resetData(boolean clearImgs) {
		if (this.layout_txt != null) {
			View imgView = layout_txt.findViewById(R.id.image_list);
			layout_txt.removeAllViews();
			layout_txt.addView(imgView);
		}
		postList.clear();
		
		if (clearImgs) {
			this.doClearUpImages();
			this.bmpUrls.clear();
		}
	}

	protected String getAdContact() {
		return "";
	}

	private void appendBeanToLayout(PostGoodsBean postBean) {
		UserBean user = GlobalDataManager.getInstance().getAccountManager()
				.getCurrentUser();
		if (postBean.getName().equals("contact")
				&& (postBean.getValues() == null || postBean.getValues()
						.isEmpty())
				&& (user != null && user.getPhone() != null && user.getPhone()
						.length() > 0)) {
			List<String> valueList = new ArrayList<String>(1);
			valueList.add(user.getPhone());
			postBean.setValues(valueList);
			postBean.setLabels(valueList);
		}

		ViewGroup layout = createItemByPostBean(postBean);// FIXME:
		if (layout != null && layout.findViewById(R.id.postinputlayout) != null) {
			layout.setClickable(true);
			layout.setOnClickListener(this);
		}

		if (postBean.getName().equals("faburen")) {
			List<String> labels = postBean.getLabels();
			List<String> values = postBean.getValues();
			if (labels != null) {
				for (int i = 0; i < labels.size(); ++i) {
					if (labels.get(i).equals("个人")) {
						((TextView) layout.findViewById(R.id.posthint))
								.setText(labels.get(i));
						params.put(postBean.getName(), labels.get(i),
								values.get(i));
					}
				}
			}
		}

		if (layout != null) {
			layout_txt.addView(layout);
		}
	}

	private void buildFixedPostLayout(HashMap<String, PostGoodsBean> pl) {
		if (pl == null || pl.size() == 0)
			return;

		HashMap<String, PostGoodsBean> pm = new HashMap<String, PostGoodsBean>();
		Object[] postListKeySetArray = pl.keySet().toArray();
		for (int i = 0; i < pl.size(); ++i) {
			for (int j = 0; j < PostCommonValues.fixedItemNames.length; ++j) {
				PostGoodsBean bean = pl.get(postListKeySetArray[i]);
				if (bean.getName().equals(PostCommonValues.fixedItemNames[j])) {
					pm.put(PostCommonValues.fixedItemNames[j], bean);
					break;
				}
			}
		}
		
		this.updateImageInfo(layout_txt);

		boolean contactAndPhoneVisible = false;

		for (int i = 0; i < PostCommonValues.fixedItemNames.length; ++i) {
			if (pm.containsKey(PostCommonValues.fixedItemNames[i])
					&& !PostCommonValues.fixedItemNames[i]
							.equals(PostCommonValues.STRING_DESCRIPTION)) {
				if (contactAndPhoneVisible
						&& (PostCommonValues.fixedItemNames[i]
								.equals("contact") || PostCommonValues.fixedItemNames[i]
								.equals(PostCommonValues.STRING_DETAIL_POSITION))) {
					continue;
				}
				this.appendBeanToLayout(pm
						.get(PostCommonValues.fixedItemNames[i]));
			} else if (!pm.containsKey(PostCommonValues.fixedItemNames[i])) {
				params.remove(PostCommonValues.fixedItemNames[i]);
			}
		}

		getView().findViewById(R.id.ll_contactAndAddress).setVisibility(
				View.VISIBLE);
		if (contactAndPhoneVisible) {
			getView().findViewById(R.id.ll_contactAndAddress).setVisibility(
					View.VISIBLE);
			setPhoneAndAddrLayout();
		} else {
			getView().findViewById(R.id.ll_contactAndAddress).setVisibility(
					View.GONE);
		}
	}

	protected void setPhoneAndAddrLeftIcon() {
		Button pBtn = getView() == null ? null : (Button) getView()
				.findViewById(R.id.btn_contact);
		Button aBtn = getView() == null ? null : (Button) getView()
				.findViewById(R.id.btn_address);
		if (aBtn == null || pBtn == null)
			return;
		String text = pBtn.getText().toString();
		int resId;
		if (text != null && text.length() > 0) {
			resId = R.drawable.icon_post_call;
		} else {
			resId = R.drawable.icon_post_call_disable;
		}

		Bitmap img = GlobalDataManager.getInstance().getImageManager()
				.loadBitmapFromResource(resId);
		BitmapDrawable bd = new BitmapDrawable(img);
		bd.setBounds(0, 0, 45, 45);

		pBtn.setCompoundDrawables(bd, null, null, null);

		text = aBtn.getText().toString();
		if (text != null && text.length() > 0) {
			resId = R.drawable.icon_location;
		} else {
			resId = R.drawable.icon_location_disable;
		}

		img = GlobalDataManager.getInstance().getImageManager()
				.loadBitmapFromResource(resId);
		bd = new BitmapDrawable(img);
		bd.setBounds(0, 0, 45, 45);

		aBtn.setCompoundDrawables(bd, null, null, null);

	}

	private void setPhoneAndAddrLayout() {
		String phone = GlobalDataManager.getInstance().getPhoneNumber();
		if (phone == null || phone.length() == 0) {
			((Button) getView().findViewById(R.id.btn_contact)).setHint("联系方式");
		}

		String address = GlobalDataManager.getInstance().getAddress();
		if (address == null || address.length() == 0) {
			if (detailLocation != null) {
				this.setDetailLocationControl(detailLocation);
			} else {
				((Button) getView().findViewById(R.id.btn_address))
						.setHint("张贴地点");
			}
		}

		setPhoneAndAddrLeftIcon();

		getView().findViewById(R.id.btn_contact).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Bundle temp = createArguments("填写联系方式", "");
						temp.putString("edittype", "contact");
						temp.putString("defaultValue", ((Button) getView()
								.findViewById(R.id.btn_contact)).getText()
								.toString());
						pushFragment(new ContactAndAddressDetailFragment(),
								temp);
					}

				});
		getView().findViewById(R.id.btn_address).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Bundle temp = createArguments("填写张贴地点", "");
						temp.putString("edittype", "address");
						temp.putString("defaultValue", ((Button) getView()
								.findViewById(R.id.btn_address)).getText()
								.toString());
						if (detailLocation != null) {
							temp.putSerializable("location", detailLocation);
						}
						pushFragment(new ContactAndAddressDetailFragment(),
								temp);
					}

				});

	}

	protected void buildPostLayout(HashMap<String, PostGoodsBean> pl) {
		this.getView().findViewById(R.id.goodscontent)
				.setVisibility(View.VISIBLE);
		this.getView().findViewById(R.id.networkErrorView)
				.setVisibility(View.GONE);
		this.reCreateTitle();
		this.refreshHeader();
		if (pl == null || pl.size() == 0) {
			return;
		}
		buildFixedPostLayout(pl);
		
		this.showInputMethod();
	}

	private View searchEditText(View parent, int resourceId) {
		View v = parent.findViewById(resourceId);
		if (v != null && v instanceof EditText) {
			if (((EditText) v).getText() == null
					|| ((EditText) v).getText().length() == 0) {
				return v;
			}
		}
		return null;
	}

	private View getEmptyEditText() {
		View edit = null;
		for (int i = 0; i < layout_txt.getChildCount(); ++i) {
			View child = layout_txt.getChildAt(i);
			if (child == null)
				continue;
			edit = searchEditText(child, R.id.description_input);
			if (edit != null) {
				break;
			}
			edit = searchEditText(child, R.id.postinput);
			if (edit != null) {
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
					// EditText ed = (EditText)
					// root.findViewById(R.id.description_input);
					View ed = getEmptyEditText();
					if (ed != null) {// && ed.getText().length() == 0) {
						ed.requestFocus();
						InputMethodManager mgr = (InputMethodManager) root
								.getContext().getSystemService(
										Context.INPUT_METHOD_SERVICE);
						mgr.showSoftInput(ed, InputMethodManager.SHOW_IMPLICIT);
					}
				}
			}, 200);
		}
	}

	final protected void updateImageInfo(View rootView) {
		if (rootView != null) {
			ViewGroup list = (ViewGroup) rootView
					.findViewById(R.id.image_list_parent);
			if (list == null) {
				return;
			}
			list.removeAllViews();

			LayoutInflater inflator = LayoutInflater
					.from(rootView.getContext());
			for (String img : this.photoList) {
				View imgParent = inflator.inflate(R.layout.post_image, null);
				imgParent.setTag(img);

				imgParent.setOnClickListener(this);
				imgParent.setId(R.id.delete_btn);

				final int margin = (int) getResources().getDimension(
						R.dimen.post_img_margin);
				final int wh = (int) getResources().getDimension(
						R.dimen.post_img_size);
				MarginLayoutParams layParams = new MarginLayoutParams(wh
						+ margin, wh + 2 * margin);
				layParams.setMargins(0, margin, margin, margin);
				list.addView(imgParent, layParams);

				ImageUploader.getInstance().registerCallback(img, this);
			}

			if (this.photoList == null || this.photoList.size() < 6) {
				showAddImageButton(list, inflator, false);
			}
		}
	}

	private void showAddImageButton(ViewGroup parent, LayoutInflater inflator,
			boolean scroolNow) {

		try {
			View addBtn = parent.getChildAt(parent.getChildCount() - 1);
			if (addBtn != null && addBtn.getId() == R.id.add_post_image) {
				return;
			}

			addBtn = inflator.inflate(R.layout.post_image, null);
			((ImageView) addBtn.findViewById(R.id.result_image))
					.setImageResource(R.drawable.btn_add_picture);
			addBtn.setOnClickListener(this);
			addBtn.setId(R.id.add_post_image);

			final int margin = (int) getResources().getDimension(
					R.dimen.post_img_margin);
			final int wh = (int) getResources().getDimension(
					R.dimen.post_img_size);
			MarginLayoutParams layParams = new MarginLayoutParams(wh, wh + 2
					* margin);
			layParams.setMargins(0, margin, 0, margin);
			parent.addView(addBtn, layParams);
		} finally {
			final HorizontalScrollView hs = (HorizontalScrollView) parent
					.getParent();
			hs.postDelayed(new Runnable() {
				public void run() {
					hs.scrollBy(1000, 0);
				}
			}, scroolNow ? 0 : 300);
		}
	}

	@Override
	protected void handleMessage(Message msg, final Activity activity,
			final View rootView) {
		if (msg.what != PostCommonValues.MSG_GPS_LOC_FETCHED) {
			hideProgress();
		}

		switch (msg.what) {
		case MSG_GET_AD_SUCCED:
			this.hideSoftKeyboard();
			this.hideProgress();
			try {
				AdList gl = JsonUtil.getGoodsListFromJson((String) msg.obj);
				if (gl != null && gl.getData() != null
						&& gl.getData().size() > 0) {

					GlobalDataManager.getInstance().updateMyAd(
							gl.getData().get(0));

					VadListLoader glLoader = new VadListLoader(null, null,
							null, gl);
					glLoader.setGoodsList(gl);
					glLoader.setHasMore(false);
					Bundle bundle2 = createArguments(null, null);
					bundle2.putSerializable("loader", glLoader);
					bundle2.putInt("index", 0);
					finishFragment(PostCommonValues.MSG_POST_EDIT_SUCCEED, gl
							.getData().get(0));
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
			this.finishFragment();// FIXME: should tell user network fail.
			break;
		case MSG_IMAGE_STATE_CHANGE: {
			BXThumbnail img = (BXThumbnail) msg.obj;
			int state = msg.arg1;
			ViewGroup imgParent = (ViewGroup) this.findImageViewByTag(img
					.getLocalPath());
			if (imgParent != null) {
				ImageView imgView = (ImageView) imgParent
						.findViewById(R.id.result_image);
				View loadingState = imgParent.findViewById(R.id.loading_status);
				if (state == IMG_STATE_UPLOADING || state == IMG_STATE_UPLOADED) {
					imgView.setImageBitmap(img.getThumbnail());
					loadingState
							.setVisibility(state == IMG_STATE_UPLOADING ? View.VISIBLE
									: View.INVISIBLE);
				} else {
					imgView.setImageResource(R.drawable.icon_load_fail);
					loadingState.setVisibility(View.GONE);
				}
			}

			break;
		}
		case MSG_UPDATE_IMAGE_LIST: {
			updateImageInfo(rootView);

			showInputMethod();

			break;
		}
		case PostCommonValues.MSG_POST_SUCCEED:
			PerformanceTracker.stamp(Event.E_POST_SUCCEEDED);
			hideProgress();

			doClearUpImages();

			String id = ((PostResultData) msg.obj).id;
			String message = ((PostResultData) msg.obj).message;
			int code = ((PostResultData) msg.obj).error;
			if (!id.equals("") && code == 0) {
				postResultSuccess();
				final Bundle args = createArguments(null, null);
				args.putInt("forceUpdate", 1);
				if (isActive) {
					resetData(false);
				}
				handlePostFinish(id);
			} else {
				postResultFail(message);
				if (msg.obj != null) {
					handlePostFail((PostResultData) msg.obj);
				}
			}
			break;
		case PostCommonValues.MSG_POST_FAIL:
			hideProgress();
			if (msg.obj != null) {
				if (msg.obj instanceof String) {
					ViewUtil.showToast(activity, (String) msg.obj, false);
					this.changeFocusAfterPostError((String) msg.obj);
					postResultFail((String) msg.obj);
				} else if (msg.obj instanceof PostResultData) {
					handlePostFail((PostResultData) msg.obj);
					postResultFail(((PostResultData) msg.obj).message);
				}
			}
			break;
		case PostCommonValues.MSG_POST_EXCEPTION:
			hideProgress();
			if (msg.obj != null && msg.obj instanceof String) {
				ViewUtil.showToast(activity, (String) msg.obj, false);
			} else {
				ViewUtil.showToast(activity, "网络连接异常", false);
			}
			break;
		case ErrorHandler.ERROR_SERVICE_UNAVAILABLE:
			hideProgress();
			ErrorHandler.getInstance().handleMessage(msg);
			break;
		case MSG_GEOCODING_TIMEOUT:
		case PostCommonValues.MSG_GEOCODING_FETCHED:
			Event evt = msg.what == MSG_GEOCODING_TIMEOUT ? Event.E_GeoCoding_Timeout
					: Event.E_GeoCoding_Fetched;
			PerformanceTracker.stamp(evt);
			showProgress(R.string.dialog_title_info,
					R.string.dialog_message_waiting, false);
			handler.removeMessages(MSG_GEOCODING_TIMEOUT);
			handler.removeMessages(PostCommonValues.MSG_GEOCODING_FETCHED);
			postAd(msg.obj == null ? null : (BXLocation) msg.obj);
			break;
		case PostCommonValues.MSG_GPS_LOC_FETCHED:
			detailLocation = (BXLocation) msg.obj;
			break;
		case PostCommonValues.MSG_POST_NEED_LOGIN:
			Bundle tmpBundle = createArguments("登录", "");
			if (msg.obj != null) {
				tmpBundle.putString("defaultNumber", (String) msg.obj);
			}
			this.pushFragment(new LoginFragment(), tmpBundle);
			doingAccountCheck = true;
			break;
		case PostCommonValues.MSG_POST_NEED_REGISTER:
			Bundle tmpRegBundle = createArguments("", "");
			if (msg.obj != null) {
				tmpRegBundle.putString("defaultNumber", (String) msg.obj);
			}
			this.pushFragment(new RegisterFragment(), tmpRegBundle);
			doingAccountCheck = true;
			break;
		case PostCommonValues.MSG_ACCOUNT_CHECK_FAIL:
			if (msg.obj != null && msg.obj instanceof String) {
				ViewUtil.showToast(activity, (String) msg.obj, false);
			}
			break;
		}
	}

	private boolean paused = false;

	private boolean doingAccountCheck = false;

	private void changeFocusAfterPostError(String errMsg) {
		if (postList == null)
			return;
		Set<String> keys = postList.keySet();
		if (keys == null)
			return;
		for (String key : keys) {
			PostGoodsBean bean = postList.get(key);
			if (errMsg.contains(bean.getDisplayName())) {
				for (int j = 0; j < layout_txt.getChildCount(); ++j) {
					final View child = layout_txt.getChildAt(j);
					if (child != null) {
						PostGoodsBean tag = (PostGoodsBean) child
								.getTag(PostCommonValues.HASH_POST_BEAN);
						if (tag != null
								&& tag.getName().equals(
										postList.get(key).getName())) {
							View et = child.findViewById(R.id.postinput);
							if (et == null) {
								et = child.findViewById(R.id.description_input);
							}
							if (et != null) {
								final View inputView = et;
								inputView.postDelayed(new Runnable() {
									@Override
									public void run() {
										inputView.requestFocus();
										InputMethodManager inputMgr = (InputMethodManager) inputView
												.getContext()
												.getSystemService(
														Context.INPUT_METHOD_SERVICE);
										inputMgr.showSoftInput(
												inputView,
												InputMethodManager.SHOW_IMPLICIT);
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

	private void handlePostFail(final PostResultData result) {
		if (result == null)
			return;
		if (result.error == 505) {
			AlertDialog.Builder bd = new AlertDialog.Builder(this.getActivity());
			bd.setTitle("")
					.setMessage(result.message)
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
									handlePostFinish(result.id);
								}
							});
			AlertDialog alert = bd.create();
			alert.show();
		} else if (result.message != null && !result.message.equals("")) {
			ViewUtil.showToast(getActivity(), result.message, false);
		}
	}

	// //to fix stupid system error. all text area will be the same content
	// after app is brought to front when activity not remain is checked
	private void setInputContent() {
		if (layout_txt == null)
			return;
		for (int i = 0; i < layout_txt.getChildCount(); ++i) {
			View v = layout_txt.getChildAt(i);
			PostGoodsBean bean = (PostGoodsBean) v
					.getTag(PostCommonValues.HASH_POST_BEAN);
			if (bean == null)
				continue;
			View control = (View) v.getTag(PostCommonValues.HASH_CONTROL);
			if (control != null && control instanceof TextView) {
				if (params != null && params.containsKey(bean.getName())) {
					String value = params.getUiData(bean.getName());
					if (value == null) {
						value = params.getUiData(bean.getName());
					}
					if (bean.getName().equals("contact")) {
						String phone = GlobalDataManager.getInstance()
								.getPhoneNumber();
						if (phone != null && phone.length() > 0) {
							// ((TextView)control).setText(phone);
							continue;
						}
					}
					// ((TextView)control).setText(value);
				}
			}
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		setInputContent();
	}

	@Override
	public void initTitle(TitleDef title) {
		title.m_leftActionHint = "返回";
		title.m_leftActionImage = R.drawable.icon_close;

		LayoutInflater inflator = LayoutInflater.from(getActivity());
		title.m_titleControls = inflator.inflate(R.layout.title_post, null);
		title.m_titleControls.setClickable(false);
		
		((TextView) title.m_titleControls.findViewById(R.id.ll_post_title).findViewById(R.id.tv_title_post)).setText("海报推广");
		((ImageView) title.m_titleControls.findViewById(R.id.ll_post_title).findViewById(R.id.imageView1)).setVisibility(View.GONE);
	}

	private ViewGroup createItemByPostBean(PostGoodsBean postBean) {
		Activity activity = getActivity();
		final ViewGroup layout = PostUtil.createItemByPostBean(postBean,
				activity);

		if (layout == null)
			return null;
		
		((View) layout.getTag(PostCommonValues.HASH_CONTROL))
				.setOnTouchListener(new OnTouchListener() {
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						return false;
					}
				});

		layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				View ctrl = (View) v.getTag(PostCommonValues.HASH_CONTROL);
				ctrl.requestFocus();
				InputMethodManager inputMgr = (InputMethodManager) ctrl
						.getContext().getSystemService(
								Context.INPUT_METHOD_SERVICE);
				inputMgr.showSoftInput(ctrl,
						InputMethodManager.SHOW_IMPLICIT);
			}
		});

		if (postBean.getName().equals(
				PostCommonValues.STRING_DETAIL_POSITION)) {
			layout.findViewById(R.id.location).setVisibility(View.GONE);
			}
		
		PostUtil.adjustMarginBottomAndHeight(layout);

		return layout;
	}
	
	private String getDetailLocation(BXLocation location) {
		if (location == null) {
			return "";
		}
		String latlon = null;
		try {
			latlon = "(" + location.fLat + "," + location.fLon + "); ";
		} catch (Exception e) {
			latlon = "";
		}
		String address = (location.detailAddress == null || location.detailAddress
				.equals("")) ? ((location.subCityName == null || location.subCityName
				.equals("")) ? "" : location.subCityName)
				: location.detailAddress;
		return (TextUtils.isEmpty(latlon) ? "" : latlon) + (TextUtils.isEmpty(address) ? "" : address);
	}

	private void setDetailLocationControl(BXLocation location) {
		if (location == null)
			return;
		Button addrBtn = getView() == null ? null : (Button) getView()
				.findViewById(R.id.btn_address);
		if (addrBtn != null) {
			String address = (location.detailAddress == null || location.detailAddress
					.equals("")) ? ((location.subCityName == null || location.subCityName
					.equals("")) ? "" : location.subCityName)
					: location.detailAddress;
			if (address == null || address.length() == 0)
				return;
			if (location.adminArea != null && location.adminArea.length() > 0) {
				address = address.replaceFirst(location.adminArea, "");
			}
			if (location.cityName != null && location.cityName.length() > 0) {
				address = address.replaceFirst(location.cityName, "");
			}
			// addrBtn.setText(address);

			setPhoneAndAddrLeftIcon();
		}
	}

	private void handlePostFinish(String adId) {
		ApiParams param = new ApiParams();
		param.addParam("newAdIds", adId);
		param.addParam("start", 0);
		param.addParam("rt", 1);
		param.addParam("rows", 1);
		param.addParam("wanted", 0);
		this.showProgress("", "正在获取您发布信息的状态，请耐心等候", false);

		BaseApiCommand.createCommand("ad_user_list", true, param).execute(
				getActivity(), new BaseApiCommand.Callback() {
					public void onNetworkFail(String apiName, ApiError error) {
						PosterFragment.this.sendMessage(MSG_GET_AD_FAIL, "");
					}

					public void onNetworkDone(String apiName,
							String responseData) {
						PosterFragment.this.sendMessage(MSG_GET_AD_SUCCED,
								responseData);
					}
				});
	}

	public boolean hasGlobalTab() {
		return false;
	}

	@Override
	public void onUploadDone(String imagePath, String serverUrl,
			Bitmap thumbnail) {
		Message msg = this.handler.obtainMessage(MSG_IMAGE_STATE_CHANGE,
				IMG_STATE_UPLOADED, 0,
				BXThumbnail.createThumbnail(imagePath, thumbnail));
		handler.sendMessage(msg);
	}

	@Override
	public void onUploading(String imagePath, Bitmap thumbnail) {
		Message msg = this.handler.obtainMessage(MSG_IMAGE_STATE_CHANGE,
				IMG_STATE_UPLOADING, 0,
				BXThumbnail.createThumbnail(imagePath, thumbnail));
		handler.sendMessage(msg);
	}

	@Override
	public void onUploadFail(String imagePath, Bitmap thumbnail) {
		Message msg = this.handler.obtainMessage(MSG_IMAGE_STATE_CHANGE,
				IMG_STATE_FAIL, 0,
				BXThumbnail.createThumbnail(imagePath, thumbnail));
		handler.sendMessage(msg);
	}

	private View findImageViewByTag(String imagePath) {
		ViewGroup root = (ViewGroup) this.getView().findViewById(
				R.id.image_list_parent);
		if (root == null) {
			return null;
		}

		int c = root.getChildCount();

		for (int i = 0; i < c; i++) {
			View child = root.getChildAt(i);
			if (imagePath.equals(child.getTag())) {
				return child;
			}
		}

		return null;
	}
}
