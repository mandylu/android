package com.quanleimu.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import com.quanleimu.entity.CityDetail;
import com.quanleimu.jsonutil.LocateJsonData;
import com.quanleimu.util.Helper;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class CityChange extends BaseActivity {

	// 定义控件名
	public TextView tvTitle, tvGPSCityName;
	public Button btnBack;
	public ScrollView scrollListContainer;
	
	public LinearLayout linearListInfo;
	public LinearLayout linearProvinces;
	public ImageView ivGPSChoose;

	public String cityName = "";
	public String cityName1 = "";

	// 定义变量
	public String backPageName = "";
	public List<String> listCityName = new ArrayList<String>();
	public List<CityDetail> listHotCity = new ArrayList<CityDetail>();
	
	protected class chooseStage extends Object {
		public View effectiveView;
		public String titleString;
		public String backString;
	};
	protected Stack<chooseStage> stackStage = new Stack<chooseStage>();
	protected View activeView;

	@Override
	protected void onPause() {
		super.onPause();
	}
 
	@Override
	protected void onResume() { 
		bundle.putString("backPageName", backPageName);
		super.onResume();
	}  
	
	@Override
	public void onBackPressed() {
		if(stackStage.size() == 0){
			CityChange.this.finish();				
		}else{
			scrollListContainer.removeView(activeView);
			
			chooseStage stage = stackStage.pop();
			activeView = stage.effectiveView;
			scrollListContainer.addView(activeView);
			btnBack.setText(stage.backString);
			tvTitle.setText(stage.titleString);
		}
	}
 
	@Override 
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.citychange);
		super.onCreate(savedInstanceState);
 
		// 获取热门城市列表数据
		listHotCity = LocateJsonData.hotCityList();
		myApp.setListHotCity(listHotCity);
 
		backPageName = intent.getExtras().getString("backPageName");
		cityName = intent.getExtras().getString("cityName");
		// 通过或ID获取控件
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		
		tvGPSCityName = (TextView) findViewById(R.id.tvGPSCityName);
		tvGPSCityName.setText(myApp.getGpsCityName());

		scrollListContainer = (ScrollView)findViewById(R.id.scrollList);
		
		linearListInfo = (LinearLayout) findViewById(R.id.linearList);
		activeView = linearListInfo;
//		linearProvinces = (LinearLayout) findViewById(R.id.linearCityInProvinceInfo);
//		linearProvinces.setVisibility(View.GONE);
//		linearChooseCity = (LinearLayout) findViewById(R.id.linearCityInProvinceInfo);
//		linearChooseCity.setVisibility(View.GONE);
		
		
		
		btnBack = (Button) findViewById(R.id.btnBack);
		ivGPSChoose = (ImageView) findViewById(R.id.ivGPSChoose);
		
		//ivGPSChoose.setClickable(true); 
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
					intent.setClass(CityChange.this, HomePage.class);
					bundle.putString("backPageName", "");
					bundle.putString("cityName", tvGPSCityName.getText().toString());
					intent.putExtras(bundle);
					startActivity(intent);
	
					for(int i=0;i<myApp.getListCityDetails().size();i++)
					{
						if(tvGPSCityName.getText().toString().equals(myApp.getListCityDetails().get(i).getName()))
						{
							myApp.setCityEnglishName(myApp.getListCityDetails().get(i).getEnglishName());
	//						System.out.println("CityChange cityName1----->" +cityName1);
							break;
						}
					}
					myApp.setCityName(tvGPSCityName.getText().toString());
					Helper.saveDataToLocate(CityChange.this, "cityName", tvGPSCityName.getText().toString());
	
					CityChange.this.finish();
					
				}
			});
		}
		//ivGPSChoose.setOnClickListener
		
		
		if(myApp.getGpsCityName() == null || myApp.getGpsCityName().equals(""))
		{
			myApp.setGpsCityName("上海");
		}
		
		if (!cityName.equals(myApp.getGpsCityName())) {
			ivGPSChoose.setVisibility(View.INVISIBLE);
		} else {
			ivGPSChoose.setVisibility(View.VISIBLE);
		}

		LayoutInflater inflater = LayoutInflater.from(this);
		
		// hot city list
		tvTitle.setText("切换城市");
		btnBack.setText(backPageName);		
		final List<ImageView> listImageViews = new ArrayList<ImageView>();
		
		LinearLayout linearHotCities = (LinearLayout)findViewById(R.id.linearHotCities); 
		for (int i = 0; i < listHotCity.size(); i++) {
			View v = null;
			v = inflater.inflate(R.layout.item_hotcity, null);

			if (i == 0) {
				v.setBackgroundResource(R.drawable.btn_top_bg);
			} else if (i == listHotCity.size() - 1) {
				v.setBackgroundResource(R.drawable.btn_down_bg);
			} else {
				v.setBackgroundResource(R.drawable.btn_m_bg);
			}

			TextView tvCityName = (TextView) v.findViewById(R.id.tvItemName);
			ImageView ivChoose = (ImageView) v.findViewById(R.id.ivItemIcon);
			ivChoose.setImageResource(R.drawable.gou);
			ivChoose.setTag(i);
			listImageViews.add(ivChoose);
			tvCityName.setText(listHotCity.get(i).getName());
			ivChoose.setVisibility(View.INVISIBLE);
			v.setTag(i);
			v.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					int a = Integer.valueOf(v.getTag().toString());
					for (int j = 0; j < listImageViews.size(); j++) {
						if (a != j) {
							listImageViews.get(j).setVisibility(View.INVISIBLE);

						} else {
							listImageViews.get(a).setVisibility(View.VISIBLE);
						}
						if (!cityName.equals(listHotCity.get(a).getName())) {
							ivGPSChoose.setVisibility(View.INVISIBLE);
						}
						intent.setClass(CityChange.this, HomePage.class);
						bundle.putString("backPageName", "");
						bundle.putString("cityName", listHotCity.get(a)
								.getName());
						System.out.println("您选择的城市是：------------>"
								+ listHotCity.get(a).getName());
						intent.putExtras(bundle);
						startActivity(intent);
						
//						String cityName1 = cn2Spell(listHotCity.get(a).getName());
						for(int i=0;i<myApp.getListCityDetails().size();i++)
						{
							if(listHotCity.get(a).getName().equals(myApp.getListCityDetails().get(i).getName()))
							{
								cityName1 = myApp.getListCityDetails().get(i).getEnglishName();
								myApp.setCityEnglishName(cityName1);
								System.out.println("CityChange cityName1----->" +cityName1);
								break;
							}
						}
						
						myApp.setCityEnglishName(cityName1);
						myApp.setCityName(listHotCity.get(a).getName());
						Helper.saveDataToLocate(CityChange.this, "cityName", listHotCity.get(a).getName());

						CityChange.this.finish();
					}
				}
			});
			linearHotCities.addView(v);
		}

		for (int i = 0; i < listHotCity.size(); i++) {
			if (cityName.equals(listHotCity.get(i).getName())) {
				listImageViews.get(i).setVisibility(View.VISIBLE);
			} else {
				listImageViews.get(i).setVisibility(View.INVISIBLE);
			}
		}
		
		//

		// 设置监听器
		btnBack.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		
		((RelativeLayout)findViewById(R.id.linear2Other)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				scrollListContainer.scrollTo(0, 0);
				
				chooseStage stage = new chooseStage();
				stage.effectiveView = activeView;
				stage.titleString = tvTitle.getText().toString();
				stage.backString = btnBack.getText().toString();
				stackStage.push(stage);
				scrollListContainer.removeView(activeView);
				
				btnBack.setText("选择城市");
				tvTitle.setText("选择省份");
				
				if(null == linearProvinces){
					
					linearProvinces = new LinearLayout(CityChange.this);
					linearProvinces.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
					linearProvinces.setOrientation(LinearLayout.VERTICAL);
					
					if(null == myApp.getShengMap() || myApp.getShengMap().size() == 0){
						
						List<String> listShengName = new ArrayList<String>();
						HashMap<String, List<CityDetail>> shengMap = new HashMap<String, List<CityDetail>>();
						
						// 获取所有省份列表
						for (int i = 0; i < myApp.getListCityDetails().size(); i++) {
							if (!(myApp.getListCityDetails().get(i).getSheng().equals("直辖市"))) {
								if (listShengName == null || listShengName.size() == 0) {
									listShengName.add(myApp.getListCityDetails().get(i)
											.getSheng());
								} else {
									if (!listShengName.contains(myApp.getListCityDetails()
											.get(i).getSheng())) {
										listShengName.add(myApp.getListCityDetails().get(i)
												.getSheng());
									}
								}
							}
						}
	
						// 将对应城市添加到对应的省里面去 shengMap
						for (int j = 0; j < listShengName.size(); j++) {
							List<CityDetail> listCD = new ArrayList<CityDetail>();
							for (int i = 0; i < myApp.getListCityDetails().size(); i++) {
								if (myApp.getListCityDetails().get(i).getSheng()
										.equals(listShengName.get(j))) {
									listCD.add(myApp.getListCityDetails().get(i));
								}
							}
							shengMap.put(listShengName.get(j), listCD);
						}
	
						myApp.setShengMap(shengMap);
					}
					
					LayoutInflater inflater = LayoutInflater.from(CityChange.this);
					Object[] keyArray= myApp.getShengMap().keySet().toArray();
					for (int i = 0; i < myApp.getShengMap().size(); i++) {
						// 添加新的视图，循环添加到ScrollView中
						View vTemp = null;
						vTemp = inflater.inflate(R.layout.item_hotcity, null);
						
						if (i == 0) {
							vTemp.setBackgroundResource(R.drawable.btn_top_bg);
						} else if (i == myApp.getShengMap().size() - 1) {
							vTemp.setBackgroundResource(R.drawable.btn_down_bg);
						} else {
							vTemp.setBackgroundResource(R.drawable.btn_m_bg);
						}
						
						
						TextView tvCityName = (TextView) vTemp.findViewById(R.id.tvItemName);
						ImageView ivChoose = (ImageView) vTemp.findViewById(R.id.ivItemIcon);
						ivChoose.setImageResource(R.drawable.arrow);
						tvCityName.setText(keyArray[i].toString());

						// 设置标志位
						vTemp.setTag(keyArray[i].toString());
						vTemp.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {								
								scrollListContainer.scrollTo(0, 0);
								
								chooseStage stage = new chooseStage();
								stage.effectiveView = activeView;
								stage.titleString = tvTitle.getText().toString();
								stage.backString = btnBack.getText().toString();
								stackStage.push(stage);
								scrollListContainer.removeView(activeView);
								
								btnBack.setText("选择省份");
								tvTitle.setText("选择城市");
								
								LinearLayout linearCities = new LinearLayout(CityChange.this);
								linearCities.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
								linearCities.setOrientation(LinearLayout.VERTICAL);
																
								LayoutInflater inflater = LayoutInflater.from(CityChange.this);
								String province = v.getTag().toString();
								final List<CityDetail> list2Sheng = myApp.getShengMap().get(province);
								for (int i = 0; i < list2Sheng.size(); i++) {
									// 添加新的视图，循环添加到ScrollView中
									View vCity = null;
									vCity = inflater.inflate(R.layout.item_hotcity, null);
									
									if (i == 0) {
										vCity.setBackgroundResource(R.drawable.btn_top_bg);
									} else if (i == list2Sheng.size() - 1) {
										vCity.setBackgroundResource(R.drawable.btn_down_bg);
									} else {
										vCity.setBackgroundResource(R.drawable.btn_m_bg);
									}
									
									TextView tvCityName = (TextView) vCity.findViewById(R.id.tvItemName);
									tvCityName.setText(list2Sheng.get(i).getName());
									
									ImageView ivChoose = (ImageView) vCity.findViewById(R.id.ivItemIcon);
									ivChoose.setImageResource(R.drawable.arrow);
									

									// 设置标志位
									vCity.setTag(i);
									vCity.setOnClickListener(new View.OnClickListener() {

										@Override
										public void onClick(View v) {
											int a = Integer.valueOf(v.getTag().toString());
											
//											String cityName1 = cn2Spell(list2Sheng.get(a).getName());
											for(int i=0;i<myApp.getListCityDetails().size();i++)
											{
												if(list2Sheng.get(a).getName().equals(myApp.getListCityDetails().get(i).getName()))
												{
													cityName1 = myApp.getListCityDetails().get(i).getEnglishName();
													myApp.setCityEnglishName(cityName1);
													break;
												}
											}
											
											myApp.setCityEnglishName(cityName1);
											myApp.setCityName(list2Sheng.get(a).getName());
											
											Helper.saveDataToLocate(CityChange.this, "cityName", list2Sheng.get(a).getName());
											
											intent.setClass(CityChange.this, HomePage.class);
											bundle.putString("backPageName", "");
											bundle.putString("cityName", list2Sheng.get(a).getName());
											intent.putExtras(bundle);
											startActivity(intent);
										}
									});
									linearCities.addView(vCity);
								}
								
								activeView = linearCities;
								scrollListContainer.addView(activeView);
							}
						});
						
						linearProvinces.addView(vTemp);
					}					
				}
				
				activeView = linearProvinces;
				scrollListContainer.addView(activeView);
			}
		});
	}
}
