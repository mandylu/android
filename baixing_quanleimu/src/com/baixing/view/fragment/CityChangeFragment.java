package com.baixing.view.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.baixing.activity.BaseFragment;
import com.baixing.data.GlobalDataManager;
import com.baixing.data.LocationManager;
import com.baixing.entity.BXLocation;
import com.baixing.entity.CityDetail;
import com.baixing.imageCache.ImageCacheManager;
import com.baixing.jsonutil.LocateJsonData;
import com.baixing.tracking.TrackConfig.TrackMobile.BxEvent;
import com.baixing.tracking.TrackConfig.TrackMobile.Key;
import com.baixing.tracking.TrackConfig.TrackMobile.PV;
import com.baixing.tracking.Tracker;
import com.baixing.util.Util;
import com.quanleimu.activity.R;

public class CityChangeFragment extends BaseFragment  implements LocationManager.onLocationFetchedListener, View.OnClickListener {
	// 定义控件名
	public ScrollView parentView;
	
	public EditText searchField;
	public LinearLayout linearListInfo;
	public LinearLayout linearProvinces;
	private RelativeLayout relativeProvinces;
	public ImageView ivGPSChoose;
	
	public String cityName = "";
	public String cityEnglishName = "";
	
	public String backPageName = "返回";
	public String title = "选择城市";
	public List<String> listCityName = new ArrayList<String>();
	public List<CityDetail> listHotCity = new ArrayList<CityDetail>();
	
	protected class chooseStage extends Object {
		public View effectiveView;
		public String titleString;
		public String backString;
	};
	protected Stack<chooseStage> stackStage = new Stack<chooseStage>();
	protected View activeView;
	
	
	
	public void initTitle(TitleDef title){
		title.m_visible = true;
		String cityName = GlobalDataManager.getInstance().cityName;
		if (cityName != null && cityName.length() > 0)
			title.m_leftActionHint = "返回";
		title.m_title = "选择城市";		
	}
	
	@Override
	public int[] excludedOptionMenus() {
		return new int[]{OPTION_CHANGE_CITY};
	}
	
	@Override
	public boolean handleBack(){
		if(stackStage.size() == 0){
			if (null == cityName || "".equals(cityName))
			{
				Builder builder = new AlertDialog.Builder(getActivity());
				builder.setTitle(R.string.dialog_title_info)
					.setMessage(R.string.dialog_message_confirm_exit)
					.setNegativeButton(R.string.no, null)
					.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() 
				{
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//System.exit(0);
						CityChangeFragment.this.getActivity().finish();
					}
				});
				builder.create().show();
				return true;
			}
			
			return false;
		}else{
			parentView.removeView(activeView);
			
			chooseStage stage = stackStage.pop();
			activeView = stage.effectiveView;
			parentView.addView(activeView);
			backPageName = stage.backString;
			title = stage.titleString;
			
			TitleDef title = getTitleDef();
			title.m_leftActionHint = "返回";
			title.m_title = this.title;
			this.refreshHeader();
			
			if (stackStage.size() == 0)
				searchField.setVisibility(View.VISIBLE);
		}
		return true;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setHasOptionsMenu(false);
	}
	
	@Override
	public View onInitializeView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.citychange, null);
		rootView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		// 获取热门城市列表数据
		listHotCity = LocateJsonData.hotCityList();
		GlobalDataManager.getInstance().setListHotCity(listHotCity);
 
		cityName = GlobalDataManager.getInstance().getCityName();
		
		// 通过或ID获取控件
		parentView = (ScrollView)rootView.findViewById(R.id.llParentView);
		
		linearListInfo = (LinearLayout) rootView.findViewById(R.id.linearList);
		activeView = linearListInfo;

		TextView tvGPSCityName = (TextView) rootView.findViewById(R.id.tvGPSCityName);
		tvGPSCityName.setText("定位中...");
		
		ivGPSChoose = (ImageView) rootView.findViewById(R.id.ivGPSChoose);
		ivGPSChoose.setVisibility(View.INVISIBLE);
		
		final LinearLayout linearHotCities = (LinearLayout)rootView.findViewById(R.id.linearHotCities); 
		for (int i = 0; i < listHotCity.size(); i++) {
			CityDetail city = listHotCity.get(i);
			
			View v = null;
			v = inflater.inflate(R.layout.item_citychange, null);

			TextView tvCityName = (TextView) v.findViewById(R.id.tvCateName);
			ImageView ivChoose = (ImageView) v.findViewById(R.id.ivChoose);
			ivChoose.setImageBitmap(ImageCacheManager.getInstance().loadBitmapFromResource(R.drawable.gou, -1, -1));

			tvCityName.setText(city.getName());
			ivChoose.setVisibility(View.INVISIBLE);
			v.setTag(new Pair<CityDetail, String>(city, "hotcity"));
			v.setOnClickListener(this);
			if (i == listHotCity.size() - 1) {
				v.findViewById(R.id.citychange_border).setVisibility(View.GONE);
			}
			linearHotCities.addView(v);
		}

		for (int i = 0; i < listHotCity.size(); i++) {
			if (cityName.equals(listHotCity.get(i).getName())) {
				linearHotCities.getChildAt(i).findViewById(R.id.ivChoose).setVisibility(View.VISIBLE);
			} else {
				linearHotCities.getChildAt(i).findViewById(R.id.ivChoose).setVisibility(View.GONE);
			}
		}
		
		// other cities
		((RelativeLayout)rootView.findViewById(R.id.linear2Other)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				searchField.setVisibility(View.GONE);
				
				parentView.scrollTo(0, 0);
				
				chooseStage stage = new chooseStage();
				stage.effectiveView = activeView;
				stage.titleString = title;
				stage.backString = backPageName;
				stackStage.push(stage);
				parentView.removeView(activeView);
				
//				backPageName = "选择城市";
				title = "选择省份";
				TitleDef titleDef = getTitleDef();
				titleDef.m_leftActionHint = backPageName;
				titleDef.m_title = CityChangeFragment.this.title;
				refreshHeader();				
				
				if(null == linearProvinces){
					LayoutInflater inflater = LayoutInflater.from(getActivity());
					relativeProvinces = (RelativeLayout)inflater.inflate(R.layout.citylist, null);
					linearProvinces = (LinearLayout)relativeProvinces.findViewById(R.id.llcitylist);
				
					initShengMap();
					
					HashMap<String,  List<CityDetail>> shengMap = GlobalDataManager.getInstance().getShengMap();
					String[] shengArray= shengMap.keySet().toArray(new String[0]);
					for (int i = 0; i < shengMap.size(); i++) {
						// 添加新的视图，循环添加到ScrollView中
						View vTemp = null;
						vTemp = inflater.inflate(R.layout.item_citychange, null);
						
						TextView tvCityName = (TextView) vTemp.findViewById(R.id.tvCateName);
						ImageView ivChoose = (ImageView) vTemp.findViewById(R.id.ivChoose);
						ivChoose.setImageBitmap(ImageCacheManager.getInstance().loadBitmapFromResource(R.drawable.arrow, -1, -1));
						tvCityName.setText(shengArray[i]);

						// 设置标志位
						vTemp.setTag(shengArray[i]);
						vTemp.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {								
								parentView.scrollTo(0, 0);
								
								chooseStage stage = new chooseStage();
								stage.effectiveView = activeView;
								stage.titleString = title;
								stage.backString = backPageName;
								stackStage.push(stage);
								parentView.removeView(activeView);
								
								title = "选择城市";
								
								TitleDef title = getTitleDef();
								title.m_leftActionHint = backPageName;
								title.m_title = CityChangeFragment.this.title;
								refreshHeader();
								
								LayoutInflater inflater = LayoutInflater.from(getActivity());
								RelativeLayout relativeCitys = (RelativeLayout)inflater.inflate(R.layout.citylist, null);
								LinearLayout linearCities = (LinearLayout)relativeCitys.findViewById(R.id.llcitylist);
																
								String province = v.getTag().toString();
								final List<CityDetail> list2Sheng = GlobalDataManager.getInstance().getShengMap().get(province);
								for (int i = 0; i < list2Sheng.size(); i++) {
									CityDetail city = list2Sheng.get(i);
									// 添加新的视图，循环添加到ScrollView中
									View vCity = null;
									vCity = inflater.inflate(R.layout.item_citychange, null);

									TextView tvCityName = (TextView) vCity.findViewById(R.id.tvCateName);
									tvCityName.setText(city.getName());
									
									ImageView ivChoose = (ImageView) vCity.findViewById(R.id.ivChoose);
									ivChoose.setVisibility(View.GONE);						

									// 设置标志位
									vCity.setTag(new Pair<CityDetail, String>(city, "othercity"));
									vCity.setOnClickListener(CityChangeFragment.this);
									linearCities.addView(vCity);
								}
								
								activeView = relativeCitys;
								parentView.addView(activeView);
							}
						});
						
						linearProvinces.addView(vTemp);
					}					
				}
				
				activeView = relativeProvinces;
				parentView.addView(activeView);
			}
		});
		
		
		// filter cities by search
		searchField = (EditText) ( rootView.findViewById(R.id.etSearchCity) );
		parentView.findViewById(R.id.filteredList).setVisibility(View.GONE);
		searchField.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) 
			{
				ViewGroup filteredList = (ViewGroup)parentView.findViewById(R.id.filteredList);
				View unfilteredList = parentView.findViewById(R.id.unfilteredList);
				String filterKeyword = s.toString().trim();
				filteredList.removeAllViews();
				
				if (filterKeyword.length() == 0) 
				{
					unfilteredList.setVisibility(View.VISIBLE);
					filteredList.setVisibility(View.GONE);
				}
				else
				{
					unfilteredList.setVisibility(View.GONE);
					filteredList.setVisibility(View.VISIBLE);
					
					LayoutInflater inflater = LayoutInflater.from(getActivity());					
					List<CityDetail> filteredCityDetails = getFilteredCityDetails(filterKeyword);
					for (int i = 0; i < filteredCityDetails.size(); i++)
					{
						CityDetail city = filteredCityDetails.get(i);
						
						View v = null;
						v = inflater.inflate(R.layout.item_citychange, null);
	
						TextView tvCityName = (TextView) v.findViewById(R.id.tvCateName);
						ImageView ivChoose = (ImageView) v.findViewById(R.id.ivChoose);
						ivChoose.setImageBitmap(ImageCacheManager.getInstance().loadBitmapFromResource(R.drawable.arrow, -1, -1));
						tvCityName.setText(city.getName());
						ivChoose.setVisibility(View.INVISIBLE);
						v.setTag(new Pair<CityDetail, String>(city, "search"));
						v.setOnClickListener(CityChangeFragment.this);
						filteredList.addView(v);						
					}
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) 
			{
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) 
			{
			}
			
		});
		
		return rootView;
	}
	
	@Override
	public void onStackTop(boolean isBack) {
		GlobalDataManager.getInstance().getLocationManager().addLocationListener(this);
	}
	
	@Override
	public void onResume() {
		this.pv = PV.SELECTCITY;
		Tracker.getInstance().pv(this.pv).end();
		super.onResume();
	}

	@Override
	public void onPause() {
		GlobalDataManager.getInstance().getLocationManager().removeLocationListener(this);
		super.onPause();
	}
	


	@Override
	public void onLocationFetched(BXLocation location) {
	}
	
	
	private boolean located = false;
	@Override
	public void onGeocodedLocationFetched(BXLocation location) {
		final View rootView = getView();
		if (rootView != null)
		{
			TextView tvGPSCityName = (TextView) rootView.findViewById(R.id.tvGPSCityName);
		
			if(null == location || !location.geocoded) {
				tvGPSCityName.setText("定位失败");
				located = false;
				return;
			}
			located = true;
			
			

			if(null == tvGPSCityName) return;
			

			RelativeLayout linearGpsCity = (RelativeLayout)rootView.findViewById(R.id.linearGpsCityItem);
			String subCity = "";
			String cityName = "";
			CityDetail cityDetail = null;
			for(CityDetail city : GlobalDataManager.getInstance().getListCityDetails()){
				if(city.getName() == null) continue;
				if (location.cityName.contains(city.getName())) {
					cityName = city.getName();//location.cityName;
					cityDetail = city;
//					break;
				}else if(location.subCityName != null && location.subCityName.contains(city.getName())){
					subCity = city.getName();
					cityDetail = city;
					break;
				}
			}
			if(cityDetail != null){
				
				linearGpsCity.setTag(new Pair<CityDetail, String>(cityDetail, "gpscity"));
				linearGpsCity.setOnClickListener(this);
				
				if(!subCity.equals("")){
					tvGPSCityName.setText(subCity);
				}else{
					if(!cityName.equals("")){
						tvGPSCityName.setText(cityName);
					}else{
						tvGPSCityName.setText(cityDetail.name);
					}
				}
			}
			
//
//			linearGpsCity.setOnClickListener(new View.OnClickListener() {
//				
//				@Override
//				public void onClick(View v) {
//
//					for(int i=0;i<QuanleimuApplication.getApplication().getListCityDetails().size();i++){
//						if(((TextView) rootView.findViewById(R.id.tvGPSCityName)).getText().toString().equals(QuanleimuApplication.getApplication().getListCityDetails().get(i).getName()))
//						{
//							QuanleimuApplication.getApplication().setCityEnglishName(QuanleimuApplication.getApplication().getListCityDetails().get(i).getEnglishName());
//							break;
//						}
//					}
//					QuanleimuApplication.getApplication().setCityName(((TextView) rootView.findViewById(R.id.tvGPSCityName)).getText().toString());
//					Helper.saveDataToLocate(getActivity(), "cityName", ((TextView) rootView.findViewById(R.id.tvGPSCityName)).getText().toString());
//					
//					finishFragment();
//				}
//			});	
		}
		
	}

	/**
	 * 
	 */
	private static void initShengMap() {
		if(null == GlobalDataManager.getInstance().getShengMap() ||
				GlobalDataManager.getInstance().getShengMap().size() == 0){
			List<String> listShengName = new ArrayList<String>();
			HashMap<String, List<CityDetail>> shengMap = new HashMap<String, List<CityDetail>>();
			
			List<CityDetail> cityDetails = GlobalDataManager.getInstance().getListCityDetails();
			// 获取所有省份列表
			for (int i = 0; i < cityDetails.size(); i++) {
				String sheng = cityDetails.get(i).getSheng();
				if (!(sheng.equals("直辖市"))) {
					if (listShengName == null || listShengName.size() == 0) {
						listShengName.add(sheng);
					} else {
						if (!listShengName.contains(sheng)) {
							listShengName.add(sheng);
						}
					}
				}
			}
			
			// 将对应城市添加到对应的省里面去 shengMap
			for (int j = 0; j < listShengName.size(); j++) {
				List<CityDetail> listCD = new ArrayList<CityDetail>();
				for (int i = 0; i < cityDetails.size(); i++) {
					if (cityDetails.get(i).getSheng()
							.equals(listShengName.get(j))) {
						listCD.add(cityDetails.get(i));
					}
				}
				shengMap.put(listShengName.get(j), listCD);
			}
	
			GlobalDataManager.getInstance().setShengMap(shengMap);
		}
	}
	
	private static boolean checkContainsShortPinyin(String englishName, String shortPinyin)
	{
		int shortPos = 0;
		for (int i = 0; i < englishName.length(); i++)
		{
			if (englishName.charAt(i) == shortPinyin.charAt(shortPos))
			{
				if (++shortPos >= shortPinyin.length())
					return true;
			}
		}
		return false;
	}
	
	private List<CityDetail> getFilteredCityDetails (String filterKeyword)
	{
		List<CityDetail> allCities = GlobalDataManager.getInstance().getListCityDetails();
		List<CityDetail> filteredCities = new ArrayList<CityDetail>(16);
		List<CityDetail> shortFilteredCities = new ArrayList<CityDetail>(8);
		for (CityDetail city : allCities)
		{
			if (city.name.startsWith(filterKeyword) || city.englishName.startsWith(filterKeyword))
			{
				filteredCities.add(city);
				continue;
			}
			
			if (filterKeyword.length() < 6)
			{
				if (checkContainsShortPinyin(city.englishName, filterKeyword))
				{
					shortFilteredCities.add(city);
				}
			}
		}
		
		filteredCities.addAll(shortFilteredCities);
		
		return filteredCities;
	}

	@Override
	public void onClick(View v) {
		Pair<CityDetail, String> pair = (Pair<CityDetail, String>) v.getTag();
		CityDetail city = pair.first;
		String block = pair.second;
		if (city.getClass().equals(CityDetail.class))
		{
			GlobalDataManager.getInstance().setCityEnglishName(city.getEnglishName());
			GlobalDataManager.getInstance().setCityName(city.getName());		
//			Helper.saveDataToLocate(getActivity(), "cityName", city.getName());
			Util.saveDataToFile(getActivity(),null, "cityName", city.getName().getBytes());

			this.finishFragment();
			
			String searchText = searchField.getText().toString().trim();
			
			Tracker.getInstance().event(BxEvent.CITY_SELECT).append(Key.CITY, city.getEnglishName()).append(Key.BLOCK, block).append(Key.GPS_RESULT, located).append(Key.SEARCHKEYWORD, searchText).end();
			
		}
	}

	public boolean hasGlobalTab()
	{
		return false;
	}
	
}
