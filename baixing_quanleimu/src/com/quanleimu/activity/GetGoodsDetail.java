package com.quanleimu.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class GetGoodsDetail extends BaseActivity {

	//定义控件
	public TextView tvTitle;
	public Button btnBack,btnStore;
	
	//定义变量
	public String backPageName = "";
	public int tag = 0;
	
	
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
		setContentView(R.layout.detail);
		super.onCreate(savedInstanceState);
		
//		v.setVisibility(View.GONE);
		
		backPageName = intent.getExtras().getString("backPageName");
		
		//findviewbyid
		tvTitle = (TextView)findViewById(R.id.tvTitle);
		btnBack = (Button)findViewById(R.id.btnBack);
		btnStore = (Button)findViewById(R.id.btnStore);
		
		//赋值
		tvTitle.setText("详细信息");
		btnBack.setText(backPageName);
		
		btnBack.setOnClickListener(this);
		btnStore.setOnClickListener(this);
		
	}

	@Override
	public void onClick(View v) {
		switch(v.getId())
		{
			case R.id.btnBack:
				GetGoodsDetail.this.finish();
				break;
			case R.id.btnStore:
				if(tag == 0)
				{
					Toast.makeText(GetGoodsDetail.this, "收藏成功", 3).show();
					tag = 1;
				}
				else if(tag == 1)
				{
					Toast.makeText(GetGoodsDetail.this, "取消收藏", 3).show();
					tag = 0;
				}
				break;
		}
		super.onClick(v);
	}

	
}
