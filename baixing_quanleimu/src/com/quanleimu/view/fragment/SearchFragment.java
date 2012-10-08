package com.quanleimu.view.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.quanleimu.activity.BaseFragment;
import com.quanleimu.activity.BaseFragment.TabDef;
import com.quanleimu.activity.BaseFragment.TitleDef;
import com.quanleimu.activity.QuanleimuApplication;
import com.quanleimu.activity.R;
import com.quanleimu.adapter.CommonItemAdapter;
import com.quanleimu.entity.GoodsDetail;
import com.quanleimu.entity.GoodsList;
import com.quanleimu.util.BXStatsHelper;
import com.quanleimu.util.Helper;

public class SearchFragment extends BaseFragment implements View.OnClickListener {

	//定义控件
		public Button btnCancel;
		public EditText etSearch;
		private TextView tvClear;
		public ListView lvSearchHistory;
		private CommonItemAdapter adapter;
		//参数
		private String fields = "mobile,id,link,title,description,date,areaNames,categoryEnglishName,lat,lng,images_big,images_resize180,metaDat";
		
		/** 
		  * 设置布局显示为目标有多大就多大 
		  */  
		 private LayoutParams WClayoutParams =new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT); 
		 /** 
		 * 设置布局显示目标最大化 
		 */  
		 private LayoutParams FFlayoutParams =new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,LinearLayout.LayoutParams.FILL_PARENT);  
		
		//定义变量
		public String searchType = "";
		public String json = "";
		public int startRow = 0;
		public List<Bitmap> listBm = new ArrayList<Bitmap>();
		public List<GoodsDetail> listSearchGoods = new ArrayList<GoodsDetail>();
		public GoodsList goodsList = new GoodsList();
		public String searchContent = "";
		
		private List<String> listRemark = new ArrayList<String>();
		
		protected void initTitle(TitleDef title) {
			title.m_visible = false;
		}
		
		public void initTab(TabDef tab){
			tab.m_visible = false;
		}
		
		
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootV = inflater.inflate(R.layout.search, null);
		
		//通过ID获取控件
		//btnSearch = (Button)findViewById(R.id.btnSearch);
		btnCancel = (Button)rootV.findViewById(R.id.btnCancel);
		
		etSearch = (EditText)rootV.findViewById(R.id.etSearch);
		etSearch.setFocusableInTouchMode(true);

		
		lvSearchHistory = (ListView) rootV.findViewById(R.id.lvSearchHistory);
		
		btnCancel.setOnClickListener(this);
		
		listRemark = QuanleimuApplication.getApplication().getListRemark();
		
		adapter = new CommonItemAdapter(getActivity(), listRemark, 0x1FFFFFFF, false);
		adapter.setHasArrow(false);
		
		//添加自定义布局
		 LinearLayout layout = new LinearLayout(getActivity());  
		 layout.setOrientation(LinearLayout.HORIZONTAL);  
		 tvClear = new TextView(getActivity());
		 tvClear.setTextSize(22);
		 tvClear.setText("清除历史记录");
		 tvClear.setGravity(Gravity.CENTER_VERTICAL);  
		 
		 layout.addView(tvClear,FFlayoutParams);  
		 layout.setGravity(Gravity.CENTER);  
		 
		 LinearLayout loadingLayout = new LinearLayout(getActivity());  
		 loadingLayout.addView(layout,WClayoutParams);  
		 loadingLayout.setGravity(Gravity.CENTER);  
		 lvSearchHistory.addFooterView(loadingLayout);
		 
		 lvSearchHistory.setVisibility(View.GONE);
		 
		 tvClear.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				listRemark.clear();
				QuanleimuApplication.getApplication().setListRemark(listRemark);
				lvSearchHistory.setVisibility(View.GONE);
				v.setVisibility(View.GONE);
				
				//将搜索记录保存本地
				Helper.saveDataToLocate(getActivity(), "listRemark", listRemark);
			}
		 });
		 
		 if(listRemark != null && listRemark.size() != 0)
		 {
			 lvSearchHistory.setVisibility(View.VISIBLE);
			 tvClear.setVisibility(View.VISIBLE);
			 lvSearchHistory.setAdapter(adapter);
				lvSearchHistory.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
							long arg3) {
						// TODO Auto-generated method stub
						if(arg2 <=  (listRemark.size()-1))
						{
							 searchContent = listRemark.get(arg2);
							 // 调用搜索接口获取搜索结果，跳转搜索界面
	
							Bundle bundle = new Bundle();
							bundle.putString("backPageName", "首页");
							bundle.putString("searchContent", searchContent);
							bundle.putString("actType", "search");
							bundle.putString("name", "");
							pushAndFinish(new SearchGoodsFragment(), bundle);
						}
						else
						{
							listRemark.clear();
							QuanleimuApplication.getApplication().setListRemark(listRemark);
							lvSearchHistory.setVisibility(View.GONE);
							tvClear.setVisibility(View.GONE);
						}
					}
				});
		 }
	
		 return rootV;
	}

	@Override
	public void onClick(View v) {
		switch(v.getId())
		{
			case R.id.btnCancel:
				doSearch();
				break;
		}
	}
	
	private void doSearch(){
		if(etSearch.getText().toString().equals(""))
		{
			Toast.makeText(getActivity(), "搜索内容不能为空", 3).show();
		}
		else
		{
			searchContent = etSearch.getText().toString();
			if(listRemark == null || listRemark.size() == 0)
			{
				listRemark = new ArrayList<String>();
				listRemark.add(searchContent);
			}
			else if(!listRemark.contains(searchContent))
			{
				listRemark.add(searchContent);
			}
			QuanleimuApplication.getApplication().setListRemark(listRemark);
			//将搜索记录保存本地
			Helper.saveDataToLocate(getActivity(), "listRemark", listRemark);
			
			Bundle bundle = new Bundle();
			bundle.putString("backPageName", "首页");
			bundle.putString("searchContent", searchContent);
			bundle.putString("actType", "search");
			bundle.putString("name", "");

			BXStatsHelper.getInstance().increase(BXStatsHelper.TYPE_HOMESEARCH_SEND, null);
			
			pushAndFinish(new SearchGoodsFragment(), bundle);
		}
	}
}
