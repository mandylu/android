package com.quanleimu.view.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.quanleimu.activity.BaseFragment;
import com.quanleimu.activity.BaseFragment.TabDef;
import com.quanleimu.activity.BaseFragment.TitleDef;
import com.quanleimu.activity.QuanleimuApplication;
import com.quanleimu.activity.R;
import com.quanleimu.entity.BXLocation;
import com.quanleimu.entity.CityDetail;
import com.quanleimu.jsonutil.LocateJsonData;
import com.quanleimu.util.Helper;

public class CityChangeFragment extends BaseFragment  implements QuanleimuApplication.onLocationFetchedListener {
	// 定义控件名
	public ScrollView parentView;
	
	public LinearLayout linearListInfo;
	public LinearLayout linearProvinces;
	private RelativeLayout relativeProvinces;
	public ImageView ivGPSChoose;
	
	public String cityName = "";
	public String cityName1 = "";
	
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
		title.m_leftActionHint = "返回";//backPageName;
		title.m_title = "选择城市";//this.title;
	}
	
	public void initTab(TabDef tab){
		tab.m_visible = false;
	}
	
	@Override
	public boolean handleBack(){
		if(stackStage.size() == 0){
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
		}
		return true;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.citychange, null);
		
		// 获取热门城市列表数据
		listHotCity = LocateJsonData.hotCityList();
		QuanleimuApplication.getApplication().setListHotCity(listHotCity);
 
		cityName = QuanleimuApplication.getApplication().getCityName();
		
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
			View v = null;
			v = inflater.inflate(R.layout.item_citychange, null);

			TextView tvCityName = (TextView) v.findViewById(R.id.tvCateName);
			ImageView ivChoose = (ImageView) v.findViewById(R.id.ivChoose);
			ivChoose.setImageResource(R.drawable.gou);
			ivChoose.setTag(i);
			tvCityName.setText(listHotCity.get(i).getName());
			ivChoose.setVisibility(View.INVISIBLE);
			v.setTag(i);
			v.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					int a = Integer.valueOf(v.getTag().toString());
					for (int j = 0; j < linearHotCities.getChildCount(); j++) {
						if (!cityName.equals(listHotCity.get(a).getName())) {
							ivGPSChoose.setVisibility(View.INVISIBLE);
						}

						for(int i=0;i<QuanleimuApplication.getApplication().getListCityDetails().size();i++)
						{
							if(listHotCity.get(a).getName().equals(QuanleimuApplication.getApplication().getListCityDetails().get(i).getName()))
							{
								cityName1 = QuanleimuApplication.getApplication().getListCityDetails().get(i).getEnglishName();
								QuanleimuApplication.getApplication().setCityEnglishName(cityName1);
								break;
							}
						}
						
						QuanleimuApplication.getApplication().setCityEnglishName(cityName1);
						QuanleimuApplication.getApplication().setCityName(listHotCity.get(a).getName());
						Helper.saveDataToLocate(getActivity(), "cityName", listHotCity.get(a).getName());

					}
					
					finishFragment();
				}
			});
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
		
		((RelativeLayout)rootView.findViewById(R.id.linear2Other)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
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
					
					if(null == QuanleimuApplication.getApplication().getShengMap() || QuanleimuApplication.getApplication().getShengMap().size() == 0){
						
						List<String> listShengName = new ArrayList<String>();
						HashMap<String, List<CityDetail>> shengMap = new HashMap<String, List<CityDetail>>();
						
						// 获取所有省份列表
						for (int i = 0; i < QuanleimuApplication.getApplication().getListCityDetails().size(); i++) {
							if (!(QuanleimuApplication.getApplication().getListCityDetails().get(i).getSheng().equals("直辖市"))) {
								if (listShengName == null || listShengName.size() == 0) {
									listShengName.add(QuanleimuApplication.getApplication().getListCityDetails().get(i)
											.getSheng());
								} else {
									if (!listShengName.contains(QuanleimuApplication.getApplication().getListCityDetails()
											.get(i).getSheng())) {
										listShengName.add(QuanleimuApplication.getApplication().getListCityDetails().get(i)
												.getSheng());
									}
								}
							}
						}
	
						// 将对应城市添加到对应的省里面去 shengMap
						for (int j = 0; j < listShengName.size(); j++) {
							List<CityDetail> listCD = new ArrayList<CityDetail>();
							for (int i = 0; i < QuanleimuApplication.getApplication().getListCityDetails().size(); i++) {
								if (QuanleimuApplication.getApplication().getListCityDetails().get(i).getSheng()
										.equals(listShengName.get(j))) {
									listCD.add(QuanleimuApplication.getApplication().getListCityDetails().get(i));
								}
							}
							shengMap.put(listShengName.get(j), listCD);
						}
	
						QuanleimuApplication.getApplication().setShengMap(shengMap);
					}
					
					Object[] keyArray= QuanleimuApplication.getApplication().getShengMap().keySet().toArray();
					for (int i = 0; i < QuanleimuApplication.getApplication().getShengMap().size(); i++) {
						// 添加新的视图，循环添加到ScrollView中
						View vTemp = null;
						vTemp = inflater.inflate(R.layout.item_citychange, null);
						
						TextView tvCityName = (TextView) vTemp.findViewById(R.id.tvCateName);
						ImageView ivChoose = (ImageView) vTemp.findViewById(R.id.ivChoose);
						ivChoose.setImageResource(R.drawable.arrow);
						tvCityName.setText(keyArray[i].toString());

						// 设置标志位
						vTemp.setTag(keyArray[i].toString());
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
								
//								backPageName = "选择省份";
								title = "选择城市";
								
								TitleDef title = getTitleDef();
								title.m_leftActionHint = backPageName;
								title.m_title = CityChangeFragment.this.title;
								refreshHeader();
								
								LayoutInflater inflater = LayoutInflater.from(getActivity());
								RelativeLayout relativeCitys = (RelativeLayout)inflater.inflate(R.layout.citylist, null);
								LinearLayout linearCities = (LinearLayout)relativeCitys.findViewById(R.id.llcitylist);
																
								String province = v.getTag().toString();
								final List<CityDetail> list2Sheng = QuanleimuApplication.getApplication().getShengMap().get(province);
								for (int i = 0; i < list2Sheng.size(); i++) {
									// 添加新的视图，循环添加到ScrollView中
									View vCity = null;
									vCity = inflater.inflate(R.layout.item_citychange, null);

									TextView tvCityName = (TextView) vCity.findViewById(R.id.tvCateName);
									tvCityName.setText(list2Sheng.get(i).getName());
									
									ImageView ivChoose = (ImageView) vCity.findViewById(R.id.ivChoose);
									ivChoose.setVisibility(View.GONE);
									//ivChoose.setImageResource(R.drawable.arrow);									
									

									// 设置标志位
									vCity.setTag(i);
									vCity.setOnClickListener(new View.OnClickListener() {

										@Override
										public void onClick(View v) {
											int a = Integer.valueOf(v.getTag().toString());
											
//											String cityName1 = cn2Spell(list2Sheng.get(a).getName());
											for(int i=0;i<QuanleimuApplication.getApplication().getListCityDetails().size();i++)
											{
												if(list2Sheng.get(a).getName().equals(QuanleimuApplication.getApplication().getListCityDetails().get(i).getName()))
												{
													cityName1 = QuanleimuApplication.getApplication().getListCityDetails().get(i).getEnglishName();
													QuanleimuApplication.getApplication().setCityEnglishName(cityName1);
													break;
												}
											}
											
											QuanleimuApplication.getApplication().setCityEnglishName(cityName1);
											QuanleimuApplication.getApplication().setCityName(list2Sheng.get(a).getName());
											
											Helper.saveDataToLocate(getActivity(), "cityName", list2Sheng.get(a).getName());
											
//											m_viewInfoListener.onExit(CityChangeView.this);//FIXME:
											finishFragment();
										}
									});
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
		
		QuanleimuApplication.getApplication().getCurrentLocation(this);
		
		return rootView;
	}
	
	@Override
	public void onLocationFetched(BXLocation location) {
		if(null == location || !location.geocoded)
			return;
		
		if (!cityName.equals(location.cityName)) {
			ivGPSChoose.setVisibility(View.INVISIBLE);
		} else {
			ivGPSChoose.setVisibility(View.VISIBLE);
		}

		final View rootView = getView();
		if (rootView != null)
		{
			TextView tvGPSCityName = (TextView) rootView.findViewById(R.id.tvGPSCityName);
			if(null == tvGPSCityName) return;
			tvGPSCityName.setText(location.cityName);
			
			ivGPSChoose.setVisibility(View.VISIBLE);

			RelativeLayout linearGpsCity = (RelativeLayout)rootView.findViewById(R.id.linearGpsCityItem);
			linearGpsCity.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {

					for(int i=0;i<QuanleimuApplication.getApplication().getListCityDetails().size();i++){
						if(((TextView) rootView.findViewById(R.id.tvGPSCityName)).getText().toString().equals(QuanleimuApplication.getApplication().getListCityDetails().get(i).getName()))
						{
							QuanleimuApplication.getApplication().setCityEnglishName(QuanleimuApplication.getApplication().getListCityDetails().get(i).getEnglishName());
							break;
						}
					}
					QuanleimuApplication.getApplication().setCityName(((TextView) rootView.findViewById(R.id.tvGPSCityName)).getText().toString());
					Helper.saveDataToLocate(getActivity(), "cityName", ((TextView) rootView.findViewById(R.id.tvGPSCityName)).getText().toString());
					
					finishFragment();
				}
			});	
		}
		
	}
}
