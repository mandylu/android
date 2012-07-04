package com.quanleimu.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import com.quanleimu.activity.QuanleimuApplication;
import com.quanleimu.activity.R;
import com.quanleimu.entity.CityDetail;
import com.quanleimu.jsonutil.LocateJsonData;
import com.quanleimu.util.Helper;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class CityChangeView extends BaseView {

	// 定义控件名
	public ScrollView parentView;
	
	public LinearLayout linearListInfo;
	public LinearLayout linearProvinces;
	public ImageView ivGPSChoose;

	public String cityName = "";
	public String cityName1 = "";

	// 定义变量
	public String backPageName = "";
	public String title = "";
	public List<String> listCityName = new ArrayList<String>();
	public List<CityDetail> listHotCity = new ArrayList<CityDetail>();
	
	protected class chooseStage extends Object {
		public View effectiveView;
		public String titleString;
		public String backString;
	};
	protected Stack<chooseStage> stackStage = new Stack<chooseStage>();
	protected View activeView;

	protected void Init(){
		LayoutInflater inflater = LayoutInflater.from(getContext());
		this.addView(inflater.inflate(R.layout.citychange, null));
		
		// 获取热门城市列表数据
		listHotCity = LocateJsonData.hotCityList();
		QuanleimuApplication.getApplication().setListHotCity(listHotCity);
 
		cityName = QuanleimuApplication.getApplication().getCityName();
		
		// 通过或ID获取控件
		parentView = (ScrollView)findViewById(R.id.llParentView);
		
		linearListInfo = (LinearLayout) findViewById(R.id.linearList);
		activeView = linearListInfo;
		
		ivGPSChoose = (ImageView) findViewById(R.id.ivGPSChoose);
		
		//ivGPSChoose.setClickable(true); 
		TextView tvGPSCityName = (TextView) findViewById(R.id.tvGPSCityName);
		tvGPSCityName.setText(QuanleimuApplication.getApplication().getGpsCityName());
		boolean isLocated = !tvGPSCityName.getText().toString().equals("");
		if(!isLocated){
			tvGPSCityName.setText("定位中...");
		}
		ivGPSChoose.setVisibility(isLocated ? View.VISIBLE : View.INVISIBLE);
//		linearGpsCity.setClickable(false);
		if(isLocated){
			RelativeLayout linearGpsCity = (RelativeLayout)findViewById(R.id.linearGpsCityItem);
			linearGpsCity.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {

					for(int i=0;i<QuanleimuApplication.getApplication().getListCityDetails().size();i++)
					{
						if(((TextView) findViewById(R.id.tvGPSCityName)).getText().toString().equals(QuanleimuApplication.getApplication().getListCityDetails().get(i).getName()))
						{
							QuanleimuApplication.getApplication().setCityEnglishName(QuanleimuApplication.getApplication().getListCityDetails().get(i).getEnglishName());
	//						System.out.println("CityChange cityName1----->" +cityName1);
							break;
						}
					}
					QuanleimuApplication.getApplication().setCityName(((TextView) findViewById(R.id.tvGPSCityName)).getText().toString());
					Helper.saveDataToLocate(getContext(), "cityName", ((TextView) findViewById(R.id.tvGPSCityName)).getText().toString());
					
					if(null != m_viewInfoListener){
						m_viewInfoListener.onExit(CityChangeView.this);
					}		
				}
			});
		}
		//ivGPSChoose.setOnClickListener
		
		
		if(QuanleimuApplication.getApplication().getGpsCityName() == null || QuanleimuApplication.getApplication().getGpsCityName().equals(""))
		{
			QuanleimuApplication.getApplication().setGpsCityName("上海");
		}
		
		if (!cityName.equals(QuanleimuApplication.getApplication().getGpsCityName())) {
			ivGPSChoose.setVisibility(View.INVISIBLE);
		} else {
			ivGPSChoose.setVisibility(View.VISIBLE);
		}
		
		final LinearLayout linearHotCities = (LinearLayout)findViewById(R.id.linearHotCities); 
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
						Helper.saveDataToLocate(getContext(), "cityName", listHotCity.get(a).getName());

					}
					
					if(null != m_viewInfoListener){
						m_viewInfoListener.onExit(CityChangeView.this);
					}
				}
			});
			if (i != listHotCity.size() - 1) {
				TextView border = new TextView(CityChangeView.this.getContext());
				border.setLayoutParams(new LayoutParams(
						LayoutParams.FILL_PARENT, 1, 1));
				border.setBackgroundResource(R.drawable.list_divider);
				((LinearLayout)v.findViewById(R.id.ll_item_cityChange)).addView(border);
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
		
		((RelativeLayout)findViewById(R.id.linear2Other)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				parentView.scrollTo(0, 0);
				
				chooseStage stage = new chooseStage();
				stage.effectiveView = activeView;
				stage.titleString = title;
				stage.backString = backPageName;
				stackStage.push(stage);
				parentView.removeView(activeView);
				
				backPageName = "选择城市";
				title = "选择省份";
				
				
				if(null == linearProvinces){
					
					linearProvinces = new LinearLayout(getContext());
					linearProvinces.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
					linearProvinces.setOrientation(LinearLayout.VERTICAL);
					
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
					
					LayoutInflater inflater = LayoutInflater.from(getContext());
					Object[] keyArray= QuanleimuApplication.getApplication().getShengMap().keySet().toArray();
					for (int i = 0; i < QuanleimuApplication.getApplication().getShengMap().size(); i++) {
						// 添加新的视图，循环添加到ScrollView中
						View vTemp = null;
						vTemp = inflater.inflate(R.layout.item_citychange, null);
						
//						if (i == 0) {
//							vTemp.setBackgroundResource(R.drawable.btn_top_bg);
//						} else if (i == QuanleimuApplication.getApplication().getShengMap().size() - 1) {
//							vTemp.setBackgroundResource(R.drawable.btn_down_bg);
//						} else {
							vTemp.setBackgroundResource(R.drawable.btn_m_bg);
//						}
						
						
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
								
								backPageName = "选择省份";
								title = "选择城市";
								if(null != m_viewInfoListener){
									TitleDef title = getTitleDef();
									title.m_leftActionHint = backPageName;
									title.m_title = CityChangeView.this.title;
									m_viewInfoListener.onTitleChanged(title);
								}
								
								LinearLayout linearCities = new LinearLayout(getContext());
								linearCities.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
								linearCities.setOrientation(LinearLayout.VERTICAL);
																
								LayoutInflater inflater = LayoutInflater.from(getContext());
								String province = v.getTag().toString();
								final List<CityDetail> list2Sheng = QuanleimuApplication.getApplication().getShengMap().get(province);
								for (int i = 0; i < list2Sheng.size(); i++) {
									// 添加新的视图，循环添加到ScrollView中
									View vCity = null;
									vCity = inflater.inflate(R.layout.item_citychange, null);
									
//									if (i == 0) {
//										vCity.setBackgroundResource(R.drawable.btn_top_bg);
//									} else if (i == list2Sheng.size() - 1) {
//										vCity.setBackgroundResource(R.drawable.btn_down_bg);
//									} else {
										vCity.setBackgroundResource(R.drawable.btn_m_bg);
//									}
									
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
											
											Helper.saveDataToLocate(getContext(), "cityName", list2Sheng.get(a).getName());
											
											if(null != m_viewInfoListener){
												m_viewInfoListener.onExit(CityChangeView.this);
											}
										}
									});
									linearCities.addView(vCity);
								}
								
								activeView = linearCities;
								parentView.addView(activeView);
							}
						});
						
						linearProvinces.addView(vTemp);
					}					
				}
				
				activeView = linearProvinces;
				parentView.addView(activeView);
			}
		});
	}
	
	public CityChangeView(Context context, String backPageName_){
		super(context); 
		
		this.backPageName = backPageName_;
		this.title = "切换城市";
		
		Init();
	}
	public CityChangeView(Context context, Bundle bundle){
		super(context);
		
		Init();
	}
	
	//public void onResume(){}
	
	@Override
	public boolean onBack(){
		if(stackStage.size() == 0){
			return false;
		}else{
			parentView.removeView(activeView);
			
			chooseStage stage = stackStage.pop();
			activeView = stage.effectiveView;
			parentView.addView(activeView);
			backPageName = stage.backString;
			title = stage.titleString;
			
			if(null != m_viewInfoListener){
				TitleDef title = getTitleDef();
				title.m_leftActionHint = backPageName;
				title.m_title = CityChangeView.this.title;
				m_viewInfoListener.onTitleChanged(title);
			}
		}
		
		return true;
	}

	@Override
	public boolean onLeftActionPressed(){
		return onBack();
	}
	
	public TitleDef getTitleDef(){
		TitleDef title = new TitleDef();
		title.m_visible = true;
		title.m_leftActionHint = backPageName;
		title.m_title = this.title;
		return title;
	}
	
	public TabDef getTabDef(){
		TabDef tab = new TabDef();
		tab.m_visible = false;
		return tab;
	}
}
