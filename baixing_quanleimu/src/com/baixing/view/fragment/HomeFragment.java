//liuchong@baixing.com
package com.baixing.view.fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baixing.activity.BaseFragment;
import com.baixing.data.GlobalDataManager;
import com.baixing.data.LocationManager.onLocationFetchedListener;
import com.baixing.entity.BXLocation;
import com.baixing.entity.Category;
import com.baixing.entity.CityDetail;
import com.baixing.sharing.referral.ReferralEntrance;
import com.baixing.sharing.referral.ReferralUtil;
import com.baixing.sharing.referral.RegisterOrLoginDlg;
import com.baixing.tracking.TrackConfig.TrackMobile.BxEvent;
import com.baixing.tracking.TrackConfig.TrackMobile.Key;
import com.baixing.tracking.TrackConfig.TrackMobile.PV;
import com.baixing.tracking.Tracker;
import com.baixing.util.PerformEvent.Event;
import com.baixing.util.PerformanceTracker;
import com.baixing.util.Util;
import com.baixing.util.ViewUtil;
import com.baixing.widget.CustomizeGridView;
import com.baixing.widget.CustomizeGridView.GridInfo;
import com.baixing.widget.CustomizeGridView.ItemClickListener;
import com.quanleimu.activity.R;

public class HomeFragment extends BaseFragment implements ItemClickListener, onLocationFetchedListener{

	public static final String NAME = "HomeFragment";
	
	public static final int INDEX_POSTED = 0;
	public static final int INDEX_LIMITED = 1;
	public static final int INDEX_DELETED = 2;
	public static final int INDEX_FAVORITE = 3;
	public static final int INDEX_MESSAGE = 4;
	public static final int INDEX_HISTORY = 5;
	public static final int INDEX_SETTING = 6;	
	
	public int postNum = 0;
	public int limitedNum = 0;
	public int deletedNum = 0;
	public int favoriteNum = 0;
	public int unreadMessageNum = 0;
	public int historyNum = 0;

    public static final int MSG_GETPERSONALPROFILE = 99;
    public static final int MSG_EDIT_USERNAME_SUCCESS = 100;
    public static final int MSG_SHOW_TOAST = 101;
    public static final int MSG_SHOW_PROGRESS = 102;
    
    private List<Category> allCates = null;

	protected void initTitle(TitleDef title) {
		LayoutInflater inflator = LayoutInflater.from(getActivity());
		title.m_titleControls = inflator.inflate(R.layout.title_home, null);

		title.hasGlobalSearch = true;
		
		View logoRoot = title.m_titleControls.findViewById(R.id.logo_root);
		
		logoRoot.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				pushFragment(new CityChangeFragment(), createArguments("切换城市", "首页"));
				GlobalDataManager.getInstance().getLocationManager().removeLocationListener(HomeFragment.this);
			}
		});
	}
	
	public void onAddTitleControl(View titleControl)
	{
		View logoRoot = titleControl.findViewById(R.id.logo_root);
		if (logoRoot != null)
		{
			logoRoot.setPadding(logoRoot.getPaddingLeft(), 0, logoRoot.getPaddingRight(), 0); //Fix padding issue for nine-patch.
		}
	}

	@Override
	public int[] includedOptionMenus() {
		return new int[]{OPTION_CHANGE_CITY};
	}
	
	@Override
	public void handleSearch() {
		Log.d(TAG, "Home.handleSearch: " + this.getArguments());
		this.pushFragment(new SearchFragment(), this.getArguments());
	};
	
	static boolean switchCityPrompted = false;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//lumengdi@baixing.net
		SharedPreferences status = getActivity().getSharedPreferences(ReferralUtil.REFERRAL_STATUS, Context.MODE_PRIVATE); 
		if(!status.contains(ReferralUtil.DLGSHOWN_KEY) || !status.getBoolean(ReferralUtil.DLGSHOWN_KEY, false)){
			Log.v("registertest", "false");
			new RegisterOrLoginDlg().show(getFragmentManager(), "EasyRegister");
			
			SharedPreferences.Editor editor=status.edit();
			editor.putBoolean(ReferralUtil.DLGSHOWN_KEY, true);
			editor.commit();
		}else{
			Log.v("registertest", "true");
		}
		
		if(!switchCityPrompted){
			GlobalDataManager.getInstance().getLocationManager().addLocationListener(this);
		}
		allCates = GlobalDataManager.getInstance().getFirstLevelCategory();
		if (allCates == null || allCates.size() == 0)
		{
			Log.d(TAG, "Reload category");
			GlobalDataManager.getInstance().loadCategorySync();//reload
			allCates = GlobalDataManager.getInstance().getFirstLevelCategory();//.getListFirst();//recheck
			if(allCates == null || allCates.size() == 0){
				Log.e(TAG, "Failed to reload category");
				return;
			}
		}
//		this.pv = PV.HOME;
	}
	
	public boolean hasGlobalTab()
	{
		return true;
	}
	
	private void setViewContent(){
		if(getView() == null) return;
		int []icons 	= {R.drawable.icon_category_wupinjiaoyi, R.drawable.icon_category_car, 		R.drawable.icon_category_house, 	R.drawable.icon_category_quanzhi, 
				   R.drawable.icon_category_jianzhi,     R.drawable.icon_category_vita, 	R.drawable.icon_category_friend, 	R.drawable.icon_category_pet,
				   R.drawable.icon_category_service,     R.drawable.icon_category_education};

		List<GridInfo> gitems = new ArrayList<GridInfo>();
		for (int i = 0; i < icons.length; i++)
		{
			GridInfo gi = new GridInfo();
			gi.img = GlobalDataManager.getInstance().getImageManager().loadBitmapFromResource(icons[i]);//bmpCaches.get(i).get();
			gi.text = allCates.get(i).getName(); 
//			gi.resId = icons[i];
			gitems.add(gi);
		}
		
		// zengjin@baixing.net
		ReferralEntrance.getInstance().addAppShareGrid(getActivity(), gitems);

		CustomizeGridView gv = (CustomizeGridView) getView().findViewById(R.id.gridcategory);
		gv.setData(gitems, 3);
		gv.setItemClickListener(this);
		
		List<String> lastUsedCategories = GlobalDataManager.getInstance().getLastUsedCategory();
		if(lastUsedCategories == null || lastUsedCategories.size() == 0){
			getView().findViewById(R.id.ll_everUsed).setVisibility(View.GONE);
		}
		else{
			String recentNames = "";
			for(String cate : lastUsedCategories){
				recentNames += cate.split(",")[1] + " ";
			}
			final String finalNames = recentNames.trim();
			
			Tracker.getInstance().event(BxEvent.RECENTCATEGORY_CHOW)
			.append(Key.RECENTCATEGORY_COUNT, lastUsedCategories.size())
			.append(Key.RECENTCATEGORY_NAMES, finalNames).end();
			
			LinearLayout llCategory = (LinearLayout)getView().findViewById(R.id.ll_categories);
			for(int i = 0; i < lastUsedCategories.size(); ++ i){
				((Button)llCategory.getChildAt(i)).setText(lastUsedCategories.get(i).split(",")[0]);
				llCategory.getChildAt(i).setTag(lastUsedCategories.get(i));
				llCategory.getChildAt(i).setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						String value = (String)v.getTag();
						if(value != null){
							String[] category = value.split(",");
							if(category != null && category.length == 2){
								Tracker.getInstance().event(BxEvent.RECENTCATEGORY_CLICK)
								.append(Key.RECENTCATEGORY_COUNT, finalNames.split(" ").length)
								.append(Key.RECENTCATEGORY_NAMES, finalNames)
								.append(Key.SECONDCATENAME, category[1]).end();
								
								Bundle bundle = createArguments(category[0], "返回");
								bundle.putString("categoryEnglishName", category[1]);
								bundle.putString("siftresult", "");
								bundle.putString("categoryName", category[0]);
								pushFragment(new ListingFragment(), bundle);
							}
						}
					}
					
				});
			}
		}
	}

	@Override
	public View onInitializeView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		logCreateView(savedInstanceState);

		View v = inflater.inflate(R.layout.homepageview, null);
		v.findViewById(R.id.linearLayout1).setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
//		pageMgr.attachView(v, this, this);
		
		return v;
	}
	@Override
	public void onStackTop(boolean isBack) {
//		//Mobile Track Config入口
//		TrackConfig.getInstance().getConfig();//获取config
		
		String cityName = GlobalDataManager.getInstance().getCityName();
		if (null == cityName || "".equals(cityName)) {
			
			// zengjin@baixing.net
			
			
			this.pushFragment(new CityChangeFragment(), createArguments("切换城市", "首页"));
		}else
		{
			TextView titleLabel = (TextView) getTitleDef().m_titleControls.findViewById(R.id.title_label_city);
			titleLabel.setText(GlobalDataManager.getInstance().getCityName());			
		}
	}
	@Override
	protected void handleMessage(Message msg, Activity activity, View rootView) {
		switch (msg.what) {
			case 1:
				break;
			case 2:
				hideProgress();
				break;
			case 4:
				hideProgress();
				ViewUtil.showToast(getActivity(), "网络连接失败，请检查设置！", false);
				break;
	        case MSG_USER_LOGIN:
	        	getView().findViewById(R.id.userInfoLayout).setVisibility(View.VISIBLE);
	            break;
	        case MSG_SHOW_TOAST:
	            hideProgress();
	            ViewUtil.showToast(getActivity(), msg.obj.toString(), false);
	            break;
	        case MSG_SHOW_PROGRESS:
	            showProgress(R.string.dialog_title_info, R.string.dialog_message_updating, true);
	            break;
		}
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		GlobalDataManager.getInstance().getLocationManager().removeLocationListener(this);
	}
	
	@Override
	public void onResume(){
		PerformanceTracker.stamp(Event.E_HomeFragment_Showup);
		super.onResume();
		synchronized(HomeFragment.this){
			setViewContent();
		}
		Tracker.getInstance().pv(PV.HOME).end();
		
		PerformanceTracker.stamp(Event.E_HomeFragment_Showup_done);
		PerformanceTracker.flush();
	}
	
	@Override
	public void onPause(){
		super.onPause();
	}

	@Override
	public void onItemClick(GridInfo info, int index) {	
//		List<FirstStepCate> allCates = QuanleimuApplication.getApplication()
//				.getListFirst();
		if (allCates == null || allCates.size() == 0)
			return;
		
		// zengjin@baixing.net
		if (index >= allCates.size()) {
			ReferralEntrance.getInstance().pushFragment(this, info);
			return;
		}
		
//		FirstStepCate cate = allCates.get(index);
		Category cate = allCates.get(index);
		Bundle bundle = new Bundle();
		bundle.putInt(ARG_COMMON_REQ_CODE, this.fragmentRequestCode);
		bundle.putSerializable("cates", cate);
		bundle.putBoolean("isPost", false);
		pushFragment(new SecondCateFragment(), bundle);
	}

	public int getEnterAnimation()
	{
		return 0;
	}
	
	public int getExitAnimation()
	{
		return 0;
	}

	@Override
	public void onLocationFetched(BXLocation location) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onGeocodedLocationFetched(BXLocation location) {
		// TODO Auto-generated method stub
		if(!switchCityPrompted){
			final String currentCity = GlobalDataManager.getInstance().getCityName();
			final String gpsCity = GlobalDataManager.getInstance().getLocationManager().getCurrentCity();
			if(!TextUtils.isEmpty(currentCity) && !TextUtils.isEmpty(gpsCity)){
				if(!currentCity.equals(gpsCity) && !gpsCity.startsWith(currentCity)){
			        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			        builder.setTitle("是否切换城市")
			        .setMessage("猜您在" + gpsCity + "，是否切换到所在城市？")
	                .setPositiveButton("切换", new DialogInterface.OnClickListener() {
	                    @Override
	                    public void onClick(DialogInterface dialogInterface, int i) {
	                    	List<CityDetail> cities = GlobalDataManager.getInstance().getListCityDetails();
	                    	for(CityDetail city : cities){
	                    		if(gpsCity.startsWith(city.name)){
	                    			GlobalDataManager.getInstance().setCityEnglishName(city.englishName);
	                    			GlobalDataManager.getInstance().setCityName(city.name);
	                    			Util.saveDataToFile(getActivity(), null, "cityName", city.getName().getBytes());
	                    			TextView titleLabel = (TextView) getTitleDef().m_titleControls.findViewById(R.id.title_label_city);
	                    			titleLabel.setText(city.name);
	                    			Tracker.getInstance().event(BxEvent.City_postSelect)
	                    			.append(Key.CURRENTCITY, currentCity)
	                    			.append(Key.GEOCITY, gpsCity)
	                    			.append(Key.ACCEPT, true);
	                    			break;
	                    			
	                    		}
	                    	}
	                    }
	                })
	                .setNegativeButton("不了", new DialogInterface.OnClickListener() {
	                    @Override
	                    public void onClick(DialogInterface dialog, int id) {
                			Tracker.getInstance().event(BxEvent.City_postSelect)
                			.append(Key.CURRENTCITY, currentCity)
                			.append(Key.GEOCITY, gpsCity)
                			.append(Key.ACCEPT, false);

	                    }
	                }).create().show();
			        switchCityPrompted = true;
				}
			}
		}		
	}
}


