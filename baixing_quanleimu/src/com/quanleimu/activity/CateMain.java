package com.quanleimu.activity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.quanleimu.adapter.AllCatesAdapter;
import com.quanleimu.adapter.GoodsListAdapter;
import com.quanleimu.entity.AllCates;
import com.quanleimu.entity.FirstStepCate;
import com.quanleimu.entity.PostMu;
import com.quanleimu.entity.SecondStepCate;
import com.quanleimu.jsonutil.JsonUtil;
import com.quanleimu.util.Communication;
import com.quanleimu.util.ShortcutUtil;
import com.quanleimu.util.Util;
import com.quanleimu.view.CategorySelectionView;

public class CateMain extends BaseActivity implements CategorySelectionView.ICateSelectionListener{

	// 定义控件
	public TextView tvTitle; 
	public LinearLayout lvCateArea;
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
				CateMain.this.onBackPressed();
			}
		});

		ivHomePage = (ImageView) findViewById(R.id.ivHomePage);
		ivCateMain = (ImageView) findViewById(R.id.ivCateMain);
		ivPostGoods = (ImageView) findViewById(R.id.ivPostGoods);
		ivMyCenter = (ImageView) findViewById(R.id.ivMyCenter);
		ivSetMain = (ImageView) findViewById(R.id.ivSetMain);
		ivCateMain.setImageResource(R.drawable.iv_cate_press);
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
			break;
		case R.id.ivPostGoods:
			intent.setClass(this, PostGoodsCateMain.class);
			intent.putExtras(bundle);
			startActivity(intent);
			overridePendingTransition(0, 0);
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
	public void OnMainCategorySelected(FirstStepCate selectedMainCate){
		btBack.setText("选择类目");
		btBack.setVisibility(View.VISIBLE);
		
		tvTitle.setText(selectedMainCate.getName());
	}
	
	@Override
	public void OnSubCategorySelected(SecondStepCate selectedSubCate){
		//btBack.setText(tvTitle.getText().toString());
		
		tvTitle.setText(selectedSubCate.getName());
		
		intent.setClass(this, GetGoods.class);
		bundle.putString("name", selectedSubCate.getName());
		bundle.putString("categoryEnglishName",	selectedSubCate.getEnglishName());
		bundle.putString("siftresult", "");
		bundle.putString("backPageName", "选择类目");
		intent.putExtras(bundle);
		startActivity(intent);
	}
	
//	@Override
//    public boolean onKeyDown(int keyCode, KeyEvent event)
//    {
//        if (keyCode == KeyEvent.KEYCODE_BACK)
//        {
//        	
//        }
//        return super.onKeyDown(keyCode, event);
//    }
	
	@Override 
	public void onBackPressed(){
		if(CateMain.this.selectionView == null || !CateMain.this.selectionView.OnBack()){
			CateMain.this.finish();
		}else
		{
			btBack.setVisibility(View.GONE);
		}
	}
}
