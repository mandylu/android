package com.quanleimu.activity;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.quanleimu.adapter.SecondCatesAdapter;
import com.quanleimu.entity.FirstStepCate;
import com.quanleimu.entity.SecondStepCate;

public class SecondCate extends BaseActivity {

	// 定义控件
	public TextView tvTitle;
	public ListView lvSecondCates;
	public Button btnBack;
	public ImageView ivHomePage, ivCateMain, ivPostGoods, ivMyCenter,
			ivSetMain;

	// 定义变量
	public SecondCatesAdapter adapter;
	public List<SecondStepCate> listSecond = new ArrayList<SecondStepCate>();
	public List<FirstStepCate> listFirst = new ArrayList<FirstStepCate>();
	public String backPageName = "";
	public String firstCateName = "";
	public int pos = -1;

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume() {
		myApp.setActivity_type("secondcate");
		bundle.putString("backPageName", backPageName);
		super.onResume();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.secondcate);
		super.onCreate(savedInstanceState);

		// findViewById
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		lvSecondCates = (ListView) findViewById(R.id.lvSecondCates);
		btnBack = (Button) findViewById(R.id.btnBack);
		ivHomePage = (ImageView) findViewById(R.id.ivHomePage);
		ivCateMain = (ImageView) findViewById(R.id.ivCateMain);
		ivPostGoods = (ImageView) findViewById(R.id.ivPostGoods);
		ivMyCenter = (ImageView) findViewById(R.id.ivMyCenter);
		ivSetMain = (ImageView) findViewById(R.id.ivSetMain);
		ivCateMain.setImageResource(R.drawable.iv_cate_press);

		firstCateName = intent.getExtras().getString("firstCateName");
		backPageName = intent.getExtras().getString("backPageName");
		pos = intent.getExtras().getInt("firstCatePos");
		listFirst = myApp.getListFirst();

		// 设置标题
		tvTitle.setText(firstCateName);
		btnBack.setText(backPageName);

		lvSecondCates
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						intent.setClass(SecondCate.this, GetGoods.class);

						bundle.putString("name", listSecond.get(arg2).getName());
						bundle.putString("categoryEnglishName",
								listSecond.get(arg2).getEnglishName());
						bundle.putString("backPageName", firstCateName);
						intent.putExtras(bundle);
						String english = listSecond.get(arg2).getEnglishName();
						if (check(english)) {
							MyApplication.listUsualCates.remove(0);
							SecondStepCate secondStepCate = listSecond
									.get(arg2);
							MyApplication.listUsualCates.add(0, secondStepCate);
						}
						startActivity(intent);

					}
				});

		// 设置监听器
		ivHomePage.setOnClickListener(this);
		ivCateMain.setOnClickListener(this);
		ivPostGoods.setOnClickListener(this);
		ivMyCenter.setOnClickListener(this);
		ivSetMain.setOnClickListener(this);
		btnBack.setOnClickListener(this);

		// 为适配器赋值
		if(listFirst != null && listFirst.size() != 0)
		{
			if(listFirst.get(pos) != null && listFirst.get(pos).getChildren() != null && listFirst.get(pos).getChildren().size() != 0)
			{
				listSecond = listFirst.get(pos).getChildren(); 
				myApp.setListSecond(listSecond);

				adapter = new SecondCatesAdapter(this, listSecond);
				lvSecondCates.setAdapter(adapter);
			}
		}
	}

	private boolean check(String english) {
		boolean bool = true;
		if(MyApplication.listUsualCates != null && MyApplication.listUsualCates.size() != 0)
		{
			for (SecondStepCate s : MyApplication.listUsualCates) {
				if (s.getEnglishName().equals(english)) {
					return false;
				}
			}
		}
		return bool;
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == btnBack.getId()) {
			SecondCate.this.finish();
		}

		switch (v.getId()) {
		case R.id.ivHomePage:
			intent.setClass(this, HomePage.class);
			intent.putExtras(bundle);
			startActivity(intent);
			break;
		case R.id.ivCateMain:
			break;
		case R.id.ivPostGoods:
			intent.setClass(this, PostGoodsCateMain.class);
			intent.putExtras(bundle);
			startActivity(intent);
			break;
		case R.id.ivMyCenter:
			intent.setClass(this, MyCenter.class);
			intent.putExtras(bundle);
			startActivity(intent);
			break;
		case R.id.ivSetMain:
			intent.setClass(this, SetMain.class);
			intent.putExtras(bundle);
			startActivity(intent);
			break;
		}
		super.onClick(v);
	}

}
