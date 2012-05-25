package com.quanleimu.activity;

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
import com.quanleimu.adapter.GoodsListAdapter;
public class GetGoods extends BaseActivity implements OnScrollListener{

	// 定义控件
	public TextView tvTitle;
	public ListView lvGoodsList;
	public Button btnBack, btnSearch;
	public ImageView ivHomePage, ivCateMain, ivPostGoods, ivMyCenter,
			ivSetMain;
	public ProgressDialog pd;
	public Button btnMore;
	public LinearLayout loadingLayout;
	/**
	 * 设置布局显示目标最大化
	 */
	private LayoutParams FFlayoutParams = new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.FILL_PARENT,
			LinearLayout.LayoutParams.FILL_PARENT);

	// 定义变量
	public String backPageName = "";
	public String name = "";
	public String categoryEnglishName = "";

	public String json = "";
	public String fields = "";
	public int startRow = 0;
	public List<Bitmap> listBm = new ArrayList<Bitmap>();
	public List<GoodsDetail> listGoods = new ArrayList<GoodsDetail>();
	public List<GoodsDetail> listCommonGoods = new ArrayList<GoodsDetail>();
	public GoodsList goodsList = new GoodsList();
	public GoodsListAdapter adapter;
	public int isFirst = 0;

	public String temp = "";
	public String mUrl = "";

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
		setContentView(R.layout.goodslist);
		super.onCreate(savedInstanceState);
		// ~~~~~~~~~~获取筛选
		temp = intent.getExtras().getString("siftresult");

		// 参数 用来过滤
		fields = "mobile,id,link,title,description,date,areaNames,categoryEnglishName,lat,lng,images_big,images_resize180,metaData";

		name = intent.getExtras().getString("name");
		categoryEnglishName = intent.getExtras().getString(
				"categoryEnglishName");

		backPageName = intent.getExtras().getString("backPageName");

		// findViewById
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		btnBack = (Button) findViewById(R.id.btnBack);
		btnSearch = (Button) findViewById(R.id.btnSearch);
		lvGoodsList = (ListView) findViewById(R.id.lvGoodsList);

		ivHomePage = (ImageView) findViewById(R.id.ivHomePage);
		ivCateMain = (ImageView) findViewById(R.id.ivCateMain);
		ivPostGoods = (ImageView) findViewById(R.id.ivPostGoods);
		ivMyCenter = (ImageView) findViewById(R.id.ivMyCenter);
		ivSetMain = (ImageView) findViewById(R.id.ivSetMain);
		ivCateMain.setImageResource(R.drawable.iv_cate_press);
		
		//线性布局  
        LinearLayout layout = new LinearLayout(this);  
        //设置布局 水平方向  
        layout.setOrientation(LinearLayout.HORIZONTAL);  
         //进度条  
        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleSmall);
         //进度条显示位置  
        progressBar.setVisibility(View.GONE);
        
        layout.addView(progressBar, WClayoutParams);  
        tvAddMore = new TextView(this);  
        tvAddMore.setTextSize(18);
        tvAddMore.setText("更多...");  
        tvAddMore.setGravity(Gravity.CENTER_VERTICAL);  
        layout.addView(tvAddMore, WClayoutParams);  
        layout.setGravity(Gravity.CENTER);  
        loadingLayout = new LinearLayout(this);  
        loadingLayout.setBackgroundResource(R.drawable.alpha_bg);
//        loadingLayout.setBackgroundColor(R.color.white);
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

		btnBack.setText(backPageName);
		tvTitle.setText(name);

		// 设置监听器
		btnBack.setOnClickListener(this);
		btnSearch.setOnClickListener(this);

		ivHomePage.setOnClickListener(this);
		ivCateMain.setOnClickListener(this);
		ivPostGoods.setOnClickListener(this);
		ivMyCenter.setOnClickListener(this);
		ivSetMain.setOnClickListener(this);

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
					intent.setClass(GetGoods.this, GoodDetail.class);
					bundle.putSerializable("currentGoodsDetail", listGoods.get(arg2));
					bundle.putString("backPageName", name);
					bundle.putString("detail_type", "getgoods");
//					bundle.putInt("detail_pos", arg2);
					intent.putExtras(bundle);
					startActivity(intent);
				}
				
			}
		});

		pd = ProgressDialog.show(GetGoods.this, "提示", "请稍候...");
		pd.setCancelable(true);

		if (temp != null && !temp.equals("")) {
			mUrl = "query="
					+ "cityEnglishName:"+myApp.getCityEnglishName()+" AND categoryEnglishName:"
					+ categoryEnglishName + " " + temp;
		} else {
			mUrl = "query="
					+ "cityEnglishName:"+myApp.getCityEnglishName()+" AND categoryEnglishName:"
					+ categoryEnglishName + " AND status:0";
		}

		new Thread(new GetGoodsListThread()).start();

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnBack:
			GetGoods.this.finish();
			break;
		case R.id.btnSearch:
			intent.setClass(GetGoods.this, SiftTest.class);
			bundle.putString("backPageName", name);
			bundle.putString("searchType", "goodslist");
			intent.putExtras(bundle);
			startActivity(intent);
			break;
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
					Toast.makeText(GetGoods.this, "没有符合的结果，请更改条件并重试！", 3).show();
				} else {
					listGoods = goodsList.getData();
					myApp.setListGoods(listGoods);
					// 判断总数是不是已经超出当前集合长度
					if (goodsList.getCount() > listGoods.size()) {
						loadingLayout.setVisibility(View.VISIBLE);
					} else {
						loadingLayout.setVisibility(View.GONE);
					}
					
					adapter = new GoodsListAdapter(GetGoods.this, listGoods);
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
				Toast.makeText(GetGoods.this, "没有符合的结果，请更改条件并重试！", 3).show();
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
					Toast.makeText(GetGoods.this, "没有符合的结果，请更改条件并重试！", 3).show();
				} else {
					listCommonGoods =  goodsList.getData();
					for(int i=0;i<listCommonGoods.size();i++)
					{
						listGoods.add(listCommonGoods.get(i));
					}
					myApp.setListGoods(listGoods);
					
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
				Toast.makeText(GetGoods.this, "网络连接失败，请检查设置！", 3).show();
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

			list.add("fields=" + URLEncoder.encode(fields));
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
			LoadImage.doTask();
		}
		
	}
}
