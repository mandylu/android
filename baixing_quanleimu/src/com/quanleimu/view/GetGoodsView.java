package com.quanleimu.view;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.quanleimu.entity.GoodsDetail;
import com.quanleimu.entity.GoodsList;
import com.quanleimu.jsonutil.JsonUtil;
import com.quanleimu.util.Communication;
import com.quanleimu.util.Helper;
import com.quanleimu.util.Util;
import com.quanleimu.view.SetMain;
import com.quanleimu.activity.QuanleimuApplication;
import com.quanleimu.activity.R;
import com.quanleimu.adapter.GoodsListAdapter;
import com.quanleimu.view.BaseView;
import com.quanleimu.view.BaseView.TabDef;
import com.quanleimu.view.BaseView.TitleDef;
import com.quanleimu.activity.BaseActivity;

public class GetGoodsView extends BaseView implements OnScrollListener{

	private ListView lvGoodsList;
	private ProgressDialog pd;
	private ProgressBar progressBar;
	private LinearLayout loadingLayout;

	private String categoryEnglishName = "";
	private String siftResult = "";

	private String json = "";
	private int startRow = 0;
	//private List<Bitmap> listBm = new ArrayList<Bitmap>();
	private List<GoodsDetail> listGoods = new ArrayList<GoodsDetail>();
	private List<GoodsDetail> listCommonGoods = new ArrayList<GoodsDetail>();
	private GoodsList goodsList = new GoodsList();
	private GoodsListAdapter adapter;
	private int isFirst = 0;
	private String mUrl = "";
	
	TextView tvAddMore;

	Bundle bundle;
	
		
	public GetGoodsView(Context context, Bundle bundle, String categoryEnglishName){
		super(context, bundle);
		this.categoryEnglishName = categoryEnglishName;

		this.bundle = bundle;
		
		init();
	}

	public GetGoodsView(Context context, Bundle bundle, String categoryEnglishName, String siftResult){
		super(context, bundle);
		this.categoryEnglishName = categoryEnglishName;
		this.siftResult = siftResult;
		this.bundle = bundle;	
		
		init();
	}
	
	public boolean onRightActionPressed(){
	
		bundle.putString("backPageName", bundle.getString("backPageName"));
		bundle.putString("searchType", "goodslist");
		bundle.putString("categoryEnglishName", categoryEnglishName);

		if(null != m_viewInfoListener){
			m_viewInfoListener.onNewView(new SiftTest(getContext(), bundle));
		}
		
		return true;
	}//called when right button on title bar pressed, return true if handled already, false otherwise
	
	public TitleDef getTitleDef(){
		TitleDef title = new TitleDef();
		title.m_visible = true;
		title.m_leftActionHint = bundle.getString("backPageName");
		title.m_title = bundle.getString("name");
		title.m_rightActionHint = "筛选";
		return title;
	}
	public TabDef getTabDef(){
		TabDef tab = new TabDef();
		tab.m_visible = false;
		return tab;}

	protected void init() {
		LayoutInflater inflater = LayoutInflater.from(this.getContext());
		View v = inflater.inflate(R.layout.goodslist, null);
		this.addView(v);

		lvGoodsList = (ListView) findViewById(R.id.lvGoodsList);

        LinearLayout layout = new LinearLayout(this.getContext());  
        layout.setOrientation(LinearLayout.HORIZONTAL);  
        progressBar = new ProgressBar(this.getContext(), null, android.R.attr.progressBarStyleSmall);
         //进度条显示位置  
        progressBar.setVisibility(View.GONE);
        
        LayoutParams WClayoutParams =
        		new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        layout.addView(progressBar, WClayoutParams);  
        tvAddMore = new TextView(this.getContext());  
        tvAddMore.setTextSize(18);
        tvAddMore.setText("更多...");  
        tvAddMore.setGravity(Gravity.CENTER_VERTICAL);  
        layout.addView(tvAddMore, WClayoutParams);  
        layout.setGravity(Gravity.CENTER);  
        loadingLayout = new LinearLayout(this.getContext());  
        loadingLayout.setBackgroundResource(R.drawable.alpha_bg);
        loadingLayout.addView(layout, WClayoutParams);  
        loadingLayout.setGravity(Gravity.CENTER); 
        
        tvAddMore.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				progressBar.setVisibility(View.VISIBLE);
				tvAddMore.setText("加载中...");
				
				//点击获取更多 按钮布局消失
				isFirst = -1;
				startRow = listGoods.size();
				new Thread(new GetGoodsListThread()).start();
			}
		});
		
        lvGoodsList.setDivider(null);
		lvGoodsList.addFooterView(loadingLayout);
		
		lvGoodsList.setOnScrollListener(this);

		lvGoodsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				if(arg2 == listGoods.size())
				{
					progressBar.setVisibility(View.VISIBLE);
					tvAddMore.setText("加载中...");
					
					//点击获取更多 按钮布局消失
					isFirst = -1;
					startRow = listGoods.size();
					new Thread(new GetGoodsListThread()).start();
				}
				else
				{
					if(GetGoodsView.this.m_viewInfoListener != null){
						bundle.putSerializable("currentGoodsDetail", listGoods.get(arg2));
						bundle.putString("detail_type", "getgoods");
						m_viewInfoListener.onNewView(new GoodDetailView(listGoods.get(arg2), getContext(), bundle));
					}
				}
				
			}
		});

		pd = ProgressDialog.show(this.getContext(), "提示", "请稍候...");
		pd.setCancelable(true);

		if (siftResult != null && !siftResult.equals("")) {
			mUrl = "query="
					+ "cityEnglishName:"+QuanleimuApplication.getApplication().getCityEnglishName()+" AND categoryEnglishName:"
					+ categoryEnglishName + " " + siftResult;
		} else {
			mUrl = "query="
					+ "cityEnglishName:"+QuanleimuApplication.getApplication().getCityEnglishName()+" AND categoryEnglishName:"
					+ categoryEnglishName + " AND status:0";
		}

		new Thread(new GetGoodsListThread()).start();

	}

		// 管理线程的Handler
	Handler myHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				 
				goodsList = JsonUtil.getGoodsListFromJson(json);
				if (goodsList == null || goodsList.getCount() == 0) {
					if (pd != null) {
						pd.dismiss();
					}
					Toast.makeText(GetGoodsView.this.getContext(), "没有符合的结果，请更改条件并重试！", 3).show();
				} else {
					listGoods = goodsList.getData();
					QuanleimuApplication.getApplication().setListGoods(listGoods);
					// 判断总数是不是已经超出当前集合长度
					if (goodsList.getCount() > listGoods.size()) {
						loadingLayout.setVisibility(View.VISIBLE);
					} else {
						loadingLayout.setVisibility(View.GONE);
					}
					
					adapter = new GoodsListAdapter(GetGoodsView.this.getContext(), listGoods);
					lvGoodsList.setAdapter(adapter);
					if (pd != null) {
						pd.dismiss();
					}
				}

				break;
			case 2:
				if (pd != null) {
					pd.dismiss();
				}
				progressBar.setVisibility(View.GONE);
				tvAddMore.setText("更多...");
				loadingLayout.setVisibility(View.GONE);
				Toast.makeText(GetGoodsView.this.getContext(), "没有符合的结果，请更改条件并重试！", 3).show();
				// 判断总数是不是已经超出当前集合长度
				if (goodsList.getCount() > listGoods.size()) {
					loadingLayout.setVisibility(View.VISIBLE);
				} else {
					loadingLayout.setVisibility(View.GONE);
				}
				break;
			case 3:
				progressBar.setVisibility(View.GONE);
				tvAddMore.setText("更多...");
				loadingLayout.setVisibility(View.GONE);
				
				goodsList = JsonUtil.getGoodsListFromJson(json);
				if (goodsList == null || goodsList.getCount() == 0) {
					Toast.makeText(GetGoodsView.this.getContext(), "没有符合的结果，请更改条件并重试！", 3).show();
				} else {
					listCommonGoods =  goodsList.getData();
					for(int i=0;i<listCommonGoods.size();i++)
					{
						listGoods.add(listCommonGoods.get(i));
					}
					QuanleimuApplication.getApplication().setListGoods(listGoods);
					
					adapter.setList(listGoods);
					adapter.notifyDataSetChanged();
					
					
				}
				// 判断总数是不是已经超出当前集合长度
				if (goodsList.getCount() > listGoods.size()) {
					loadingLayout.setVisibility(View.VISIBLE);
				} else {
					loadingLayout.setVisibility(View.GONE);
				}
				break;
			case 4:
				break;
			case 10:
				if (pd != null) {
					pd.dismiss();
				}
				progressBar.setVisibility(View.GONE);
				tvAddMore.setText("更多...");
				loadingLayout.setVisibility(View.GONE);
				Toast.makeText(GetGoodsView.this.getContext(), "网络连接失败，请检查设置！", 3).show();
				// 判断总数是不是已经超出当前集合长度
				if (goodsList.getCount() > listGoods.size()) {
					loadingLayout.setVisibility(View.VISIBLE);
				} else {
					loadingLayout.setVisibility(View.GONE);
				}
				break;
			}
			
			super.handleMessage(msg);
		}
	};

	class GetGoodsListThread implements Runnable {
		@Override
		public void run() {
			String apiName = "ad_list";
			ArrayList<String> list = new ArrayList<String>();

//			list.add("fields=" + URLEncoder.encode(fields));
			list.add(mUrl);
			list.add("start=" + startRow);
			list.add("rows=" + 30);

			String url = Communication.getApiUrl(apiName, list);
			try {
				json = Communication.getDataByUrl(url);

				if (json != null) {
					if (isFirst == -1) {
						isFirst = 0;
						myHandler.sendEmptyMessage(3);
					} else {
						myHandler.sendEmptyMessage(1);
					}

				} else {
					myHandler.sendEmptyMessage(2);
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				myHandler.sendEmptyMessage(10);
				e.printStackTrace();
			} 

		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
	}
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if(scrollState == SCROLL_STATE_IDLE)
		{
//			LoadImage.doTask();
		}
		
	}
}
