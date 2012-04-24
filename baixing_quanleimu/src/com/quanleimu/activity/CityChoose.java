package com.quanleimu.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.quanleimu.entity.CityDetail;
import com.quanleimu.util.Helper;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CityChoose extends BaseActivity {

	// 定义控件名
	public TextView tvTitle;
	public Button btnBack;
	public LinearLayout linearNotHotCities;
	public List<ImageView> listImageViews = new ArrayList<ImageView>();
	public HashMap<String, List<CityDetail>> shengMap = new HashMap<String, List<CityDetail>>();

	// 定义变量
	public String backPageName = "";
	public String provinceName = "";
	public List<String> listCityName = new ArrayList<String>();
	public List<CityDetail> list2Sheng = new ArrayList<CityDetail>();
	public String cityName1 = "";
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume() {
		bundle.putString("backPageName", backPageName);
		super.onResume();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.citychoose);
		super.onCreate(savedInstanceState);

		// linearNotHotCities
		backPageName = intent.getExtras().getString("backPageName");
		provinceName = intent.getExtras().getString("provinceName");
		System.out.println("您选择的省份是：----------- >" + provinceName);
		shengMap = myApp.getShengMap(); 
		list2Sheng = shengMap.get(provinceName);

		// 通过或ID获取控件
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		linearNotHotCities = (LinearLayout) findViewById(R.id.linearNotHotCities);
		btnBack = (Button) findViewById(R.id.btnBack);

		// 设置标题
		tvTitle.setText("切换城市");
		btnBack.setText(backPageName);

		LayoutInflater inflater = LayoutInflater.from(this);

		for (int i = 0; i < list2Sheng.size(); i++) {
			// 添加新的视图，循环添加到ScrollView中
			View v = null;
			v = inflater.inflate(R.layout.item_hotcity, null);
			
			if (i == 0) {
				v.setBackgroundResource(R.drawable.btn_top_bg);
			} else if (i == list2Sheng.size() - 1) {
				v.setBackgroundResource(R.drawable.btn_down_bg);
			} else {
				v.setBackgroundResource(R.drawable.btn_m_bg);
			}
			
			TextView tvCityName = (TextView) v.findViewById(R.id.tvCityName);
			ImageView ivChoose = (ImageView) v.findViewById(R.id.ivChoose);
			ivChoose.setImageResource(R.drawable.arrow);
			tvCityName.setText(list2Sheng.get(i).getName());

			// 设置标志位
			v.setTag(i);
			v.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					int a = Integer.valueOf(v.getTag().toString());
					intent.setClass(CityChoose.this, HomePage.class);
					bundle.putString("backPageName", "");
					bundle.putString("cityName", list2Sheng.get(a).getName());
					System.out.println("您选择的城市是：------------>"
							+ list2Sheng.get(a).getName());
					intent.putExtras(bundle);
					startActivity(intent);
					
//					String cityName1 = cn2Spell(list2Sheng.get(a).getName());
					for(int i=0;i<myApp.getListCityDetails().size();i++)
					{
						if(list2Sheng.get(a).getName().equals(myApp.getListCityDetails().get(i).getName()))
						{
							cityName1 = myApp.getListCityDetails().get(i).getEnglishName();
							myApp.setCityEnglishName(cityName1);
							System.out.println("CityChoose cityName1----->" +cityName1);
							break;
						}
					}
					
					myApp.setCityEnglishName(cityName1);
					myApp.setCityName(list2Sheng.get(a).getName());
					
					Helper.saveDataToLocate(CityChoose.this, "cityName", list2Sheng.get(a).getName());
					CityChoose.this.finish();
				}
			});
			linearNotHotCities.addView(v);
		}

		// 设置监听器
		btnBack.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		CityChoose.this.finish();
		super.onClick(v);
	}

}
