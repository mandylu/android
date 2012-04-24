package com.quanleimu.activity;

import java.util.ArrayList;
import java.util.List;

import com.quanleimu.entity.CityDetail;
import com.quanleimu.jsonutil.LocateJsonData;
import com.quanleimu.util.Helper;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CityChange extends BaseActivity {

	// 定义控件名
	public TextView tvTitle, tvGPSCityName;
	public Button btnBack;
	public LinearLayout linearHotCities;
	public RelativeLayout linear2Other;
	public ImageView ivGPSChoose;
	public List<ImageView> listImageViews = new ArrayList<ImageView>();

	public String cityName = "";
	public String cityName1 = "";

	// 定义变量
	public String backPageName = "";
	public List<String> listCityName = new ArrayList<String>();
	public List<CityDetail> listHotCity = new ArrayList<CityDetail>();

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

		linearHotCities = (LinearLayout) findViewById(R.id.linearHotCities);
		linear2Other = (RelativeLayout) findViewById(R.id.linear2Other);
		btnBack = (Button) findViewById(R.id.btnBack);
		ivGPSChoose = (ImageView) findViewById(R.id.ivGPSChoose);
		
		
		if(myApp.getGpsCityName() == null || myApp.getGpsCityName().equals(""))
		{
			myApp.setGpsCityName("上海");
		}
		
		if (!cityName.equals(myApp.getGpsCityName())) {
			ivGPSChoose.setVisibility(View.INVISIBLE);
		} else {
			ivGPSChoose.setVisibility(View.VISIBLE);
		}

		// 设置标题
		tvTitle.setText("切换城市");
		tvGPSCityName.setText(myApp.getGpsCityName());
		btnBack.setText(backPageName);

		LayoutInflater inflater = LayoutInflater.from(this);

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

			TextView tvCityName = (TextView) v.findViewById(R.id.tvCityName);
			ImageView ivChoose = (ImageView) v.findViewById(R.id.ivChoose);
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

		// 设置监听器
		btnBack.setOnClickListener(this);
		linear2Other.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnBack:
			CityChange.this.finish();
			break;
		// linear2Other
		case R.id.linear2Other:
			intent.setClass(CityChange.this, ProvinceChoose.class);
			bundle.putString("backPageName", "切换城市");
			intent.putExtras(bundle);
			startActivity(intent);
			break;
		}
		super.onClick(v);
	}

}
