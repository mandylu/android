package com.quanleimu.activity;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.quanleimu.adapter.AllCatesAdapter;

public class PostSecondCate extends BaseActivity {

	//定义控件
	public TextView tvTitle;
	public ListView lvSecondCates;
	public Button btnBack;
	
	//定义变量
	public AllCatesAdapter adapter;
	public List<String> listSecondCatesName = new ArrayList<String>();
	public String backPageName = "";
	public String firstCateName = "";
	
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
		
		//findViewById
		tvTitle = (TextView)findViewById(R.id.tvTitle);
		lvSecondCates = (ListView)findViewById(R.id.lvSecondCates);
		btnBack = (Button)findViewById(R.id.btnBack);
		
		firstCateName = intent.getExtras().getString("firstCateName");
		backPageName = intent.getExtras().getString("backPageName");
		
		//设置标题
		tvTitle.setText(firstCateName);
		btnBack.setText(backPageName);
		
		//测试数据插入
		listSecondCatesName.add("二手车买卖");
		listSecondCatesName.add("二手房");
		listSecondCatesName.add("车辆收购");
		listSecondCatesName.add("新车优惠/4S店");
		listSecondCatesName.add("货车/工程车");
		listSecondCatesName.add("面包车/客车");
		listSecondCatesName.add("面包车/客车");
		listSecondCatesName.add("自行车");
		listSecondCatesName.add("电动车");
		listSecondCatesName.add("评估师");
		
		
		lvSecondCates.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				
			}
		});
		
//		adapter = new AllCatesAdapter(this, listSecondCatesName);
//		lvSecondCates.setAdapter(adapter);
		
		btnBack.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if(v.getId() == btnBack.getId())
		{
			PostSecondCate.this.finish();
		}
		super.onClick(v);
	}
	
	
}
