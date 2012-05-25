package com.quanleimu.activity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.quanleimu.adapter.AllCatesAdapter;
import com.quanleimu.entity.AllCates;
import com.quanleimu.entity.FirstStepCate;
import com.quanleimu.entity.PostMu;
import com.quanleimu.entity.SaveFirstStepCate;
import com.quanleimu.entity.SecondStepCate;
import com.quanleimu.jsonutil.JsonUtil;
import com.quanleimu.util.Communication;
import com.quanleimu.util.Util;
import com.quanleimu.view.CategorySelectionView;
import com.quanleimu.view.SetMain;

public class PostGoodsCateMain extends BaseActivity implements CategorySelectionView.ICateSelectionListener{

	// 定义控件
	public TextView tvTitle;
	public LinearLayout lvCateArea;
	public ProgressDialog pd;
	public ImageView ivHomePage, ivCateMain, ivPostGoods, ivMyCenter,
			ivSetMain;

	protected Button btBack;
	protected CategorySelectionView selectionView;
	

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	
	@Override
	public void OnMainCategorySelected(FirstStepCate selectedMainCate){
		btBack.setText("选择类目");
		btBack.setVisibility(View.VISIBLE);
		
		tvTitle.setText(selectedMainCate.getName());
	}
	
	@Override
	public void OnSubCategorySelected(SecondStepCate selectedSubCate){
		//btBack.setText(tvTitle.getText().toString());
		
		tvTitle.setText(selectedSubCate.getName());
		
		intent.setClass(PostGoodsCateMain.this, PostGoods.class);
		bundle.putString("name", selectedSubCate.getName());
		bundle.putString("categoryEnglishName", selectedSubCate.getEnglishName());
		bundle.putString("backPageName", "选择类目");
		intent.putExtras(bundle);
		startActivity(intent);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.catemain);
		super.onCreate(savedInstanceState);

		// findViewById
		tvTitle = (TextView) findViewById(R.id.tvTitle);

		selectionView = new CategorySelectionView(this);
		selectionView.setSelectionListener(this);
		lvCateArea = (LinearLayout) findViewById(R.id.linearListView);		
		lvCateArea.addView(selectionView);
		
		btBack = (Button) findViewById(R.id.btnBack);
		btBack.setVisibility(View.GONE);
		btBack.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				PostGoodsCateMain.this.onBackPressed();
			}
		});
		
		ivHomePage = (ImageView) findViewById(R.id.ivHomePage);
		ivCateMain = (ImageView) findViewById(R.id.ivCateMain);
		ivPostGoods = (ImageView) findViewById(R.id.ivPostGoods);
		ivMyCenter = (ImageView) findViewById(R.id.ivMyCenter);
		ivSetMain = (ImageView) findViewById(R.id.ivSetMain);
		ivPostGoods.setImageResource(R.drawable.iv_postgoods_press);

		// 设置标题
		tvTitle.setText("选择类目");

		// 设置监听器
		ivHomePage.setOnClickListener(this);
		ivCateMain.setOnClickListener(this);
		ivPostGoods.setOnClickListener(this);
		ivMyCenter.setOnClickListener(this);
		ivSetMain.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ivHomePage:
			intent.setClass(this, HomePage.class);
			intent.putExtras(bundle);
			startActivity(intent);
			overridePendingTransition(0, 0);
			break;
		case R.id.ivCateMain:
			intent.setClass(this, CateMain.class);
			intent.putExtras(bundle);
			startActivity(intent);
			overridePendingTransition(0, 0);
			break;
		case R.id.ivPostGoods:
			break;
		case R.id.ivMyCenter:
			intent.setClass(this, MyCenter.class);
			intent.putExtras(bundle);
			startActivity(intent);
			overridePendingTransition(0, 0);
			break;
		case R.id.ivSetMain:
			intent.setClass(this, SetMain.class);
			intent.putExtras(bundle);
			startActivity(intent);
			overridePendingTransition(0, 0);
			break;
		}
		super.onClick(v);
	}
	
	
	@Override 
	public void onBackPressed(){
		if(selectionView == null || !selectionView.OnBack()){
			finish();
		}else
		{
			btBack.setVisibility(View.GONE);
		}
	}
}
