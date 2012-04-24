package com.quanleimu.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.quanleimu.entity.CityDetail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ProvinceChoose extends BaseActivity {

	// 定义控件名
	public TextView tvTitle;
	public Button btnBack;
	public LinearLayout linearProvince;
	public List<ImageView> listImageViews = new ArrayList<ImageView>();

	// 定义变量
	public String backPageName = "";
	public List<String> listCityName = new ArrayList<String>();
	public List<String> listShengName = new ArrayList<String>();
	public HashMap<String, List<CityDetail>> shengMap = new HashMap<String, List<CityDetail>>();

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
		setContentView(R.layout.provincechange);
		super.onCreate(savedInstanceState);  

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

		backPageName = intent.getExtras().getString("backPageName");

		// 通过或ID获取控件
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		linearProvince = (LinearLayout) findViewById(R.id.linearProvince);
		btnBack = (Button) findViewById(R.id.btnBack);

		// 设置标题
		tvTitle.setText("切换省份");
		btnBack.setText(backPageName);

		LayoutInflater inflater = LayoutInflater.from(this);

		for (int i = 0; i < listShengName.size(); i++) {
			// 添加新的视图，循环添加到ScrollView中
			View v = null;
			v = inflater.inflate(R.layout.item_hotcity, null);
			
			if (i == 0) {
				v.setBackgroundResource(R.drawable.btn_top_bg);
			} else if (i == listShengName.size() - 1) {
				v.setBackgroundResource(R.drawable.btn_down_bg);
			} else {
				v.setBackgroundResource(R.drawable.btn_m_bg);
			}
			
			
			TextView tvCityName = (TextView) v.findViewById(R.id.tvCityName);
			ImageView ivChoose = (ImageView) v.findViewById(R.id.ivChoose);
			ivChoose.setImageResource(R.drawable.arrow);
			tvCityName.setText(listShengName.get(i));

			// 设置标志位
			v.setTag(i);
			v.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					int a = Integer.valueOf(v.getTag().toString());
					intent.setClass(ProvinceChoose.this, CityChoose.class);
					bundle.putString("backPageName", "切换省份");
					bundle.putString("provinceName", listShengName.get(a));
					intent.putExtras(bundle);
					startActivity(intent);
				}
			});
			linearProvince.addView(v);
		}

		// 设置监听器
		btnBack.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnBack:// 点击返回
			ProvinceChoose.this.finish();
			break;
		}
		super.onClick(v);
	}

}
