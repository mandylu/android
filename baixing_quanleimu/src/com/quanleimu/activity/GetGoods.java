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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
	public CommonAdapter adapter;
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
		
		// 自定义底部按钮
		btnMore = new Button(this);
		btnMore.setTextSize(22);
		btnMore.setText("点  击  获  取  更  多");
		btnMore.setGravity(Gravity.CENTER);
		loadingLayout = new LinearLayout(this);
		loadingLayout.addView(btnMore, FFlayoutParams);
		loadingLayout.setGravity(Gravity.CENTER);

		btnMore.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 点击获取更多 按钮布局消失
				loadingLayout.setVisibility(View.GONE);
				isFirst = -1;
				startRow = listGoods.size();
				new Thread(new GetGoodsListThread()).start();
			}
		});

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

		lvGoodsList
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						intent.setClass(GetGoods.this, GetGoodsDetail2.class);
						bundle.putString("backPageName", name);
						bundle.putString("detail_type", "getgoods");
						bundle.putInt("detail_pos", arg2);
						intent.putExtras(bundle);
						startActivity(intent);
  
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

	// 管理线程的Handler
	Handler myHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				if (pd != null) {
					pd.dismiss();
				}
				goodsList = JsonUtil.getGoodsListFromJson(json);
				if (goodsList == null || goodsList.getCount() == 0) {
					Toast.makeText(GetGoods.this, "未获取到数据", 3).show();
				} else {
					listGoods = goodsList.getData();
					myApp.setListGoods(listGoods);
					// 判断总数是不是已经超出当前集合长度
					if (goodsList.getCount() > listGoods.size()) {
						loadingLayout.setVisibility(View.VISIBLE);
					} else {
						loadingLayout.setVisibility(View.GONE);
					}

					adapter = new CommonAdapter(GetGoods.this, listGoods);
					lvGoodsList.setAdapter(adapter);

				}

				break;
			case 2:
				if (pd != null) {
					pd.dismiss();
				}
				Toast.makeText(GetGoods.this, "未获取到数据", 3).show();
				break;
			case 3:
				goodsList = JsonUtil.getGoodsListFromJson(json);
				if (goodsList == null || goodsList.getCount() == 0) {
					Toast.makeText(GetGoods.this, "未获取到数据", 3).show();
				} else {
					listCommonGoods =  goodsList.getData();
					for(int i=0;i<listCommonGoods.size();i++)
					{
						listGoods.add(listCommonGoods.get(i));
					}
					myApp.setListGoods(listGoods);
					
					adapter.setList(listGoods);
					adapter.notifyDataSetChanged();
					
					// 判断总数是不是已经超出当前集合长度
					if (goodsList.getCount() > listGoods.size()) {
						loadingLayout.setVisibility(View.VISIBLE);
					} else {
						loadingLayout.setVisibility(View.GONE);
					}
				}
				
				break;
			case 4:
				break;
			case 10:
				if (pd != null) {
					pd.dismiss();
				}
				Toast.makeText(GetGoods.this, "网络连接异常", 3).show();
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

	
	public class CommonAdapter extends BaseAdapter {

		public Context context;
		public TextView tvDes,tvPrice,tvDateAndAddress;
		public ImageView ivInfo;
		public List<GoodsDetail> list = new ArrayList<GoodsDetail>();
		
		

		public List<GoodsDetail> getList() {
			return list;
		}


		public void setList(List<GoodsDetail> list) {
			this.list = list;
		}


		public CommonAdapter() {
			super();
			// TODO Auto-generated constructor stub
		}
		

		public CommonAdapter(Context context,List<GoodsDetail> list) {
			super();
			this.context = context;
			this.list = list;
		}


		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return list.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = LayoutInflater.from(context);
			View v = null;
//			if(convertView == null)
//			{
				v = inflater.inflate(R.layout.item_common, null);
//			}
//			else{
//				v = (View)convertView;
//			}
			
			if(list.size() == 1){
				v.setBackgroundResource(R.drawable.btn_s_bg);
			}
			else{
				if(position==0){ 
					v.setBackgroundResource(R.drawable.btn_top_bg);
				}else if(position==list.size()-1){
					v.setBackgroundResource(R.drawable.btn_down_bg);
				}else{
					v.setBackgroundResource(R.drawable.btn_m_bg);
				}
			}
			tvDes = (TextView)v.findViewById(R.id.tvDes);
			tvPrice = (TextView)v.findViewById(R.id.tvPrice);
			tvPrice.setTextColor(Color.RED);
			tvDateAndAddress = (TextView)v.findViewById(R.id.tvDateAndAddress);
			tvDateAndAddress.setTextColor(R.color.hui);
			ivInfo = (ImageView) v.findViewById(R.id.ivInfo);
			
			Bitmap mb = BitmapFactory.decodeResource(context.getResources(),R.drawable.moren1);
			mb= Helper.toRoundCorner(mb, 20);
			
			Bitmap mb1 = BitmapFactory.decodeResource(context.getResources(),R.drawable.moren);
			mb1= Helper.toRoundCorner(mb1, 20);
			
			ivInfo.setImageBitmap(mb);
			
			int type = Util.getWidthByContext(context);
			RelativeLayout.LayoutParams lp = null;
			switch(type)
			{
				case 240:
					 lp= new RelativeLayout.LayoutParams(45,45);
					break;
				case 320:
					 lp= new RelativeLayout.LayoutParams(60,60);
					break;
				case 480:
					 lp= new RelativeLayout.LayoutParams(90,90);
					break;
				case 540:
					 lp= new RelativeLayout.LayoutParams(100,100);
					break;
				case 640:
					 lp= new RelativeLayout.LayoutParams(120,120);
					break;
				default:
					 lp= new RelativeLayout.LayoutParams(140,140);
					break;
			}
			
			lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			ivInfo.setLayoutParams(lp);
			

			if(list.get(position).getImageList() == null || list.get(position).getImageList().equals("") || list.get(position).getImageList().getResize180() == null || list.get(position).getImageList().getResize180().equals(""))
			{
				ivInfo.setImageBitmap(mb1);
			}
			else
			{
				String b = (list.get(position).getImageList().getResize180()).substring(1, (list.get(position).getImageList().getResize180()).length() - 1);
				b = Communication.replace(b);
				
				if (b.contains(",")) {
					String[] c = b.split(",");
					if(c[0] == null || c[0].equals(""))
					{
						ivInfo.setImageBitmap(mb1);
					}
					else
					{
						ivInfo.setTag(c[0]);
						LoadImage.addTask(c[0], ivInfo);
						
						if(position <= 5)
						{
							LoadImage.doTask();
						}
						
//						SimpleImageLoader.showImg(ivInfo, c[0],context);
					}
				}
				else
				{
					if(b == null || b.equals(""))
					{
						ivInfo.setImageBitmap(mb1);
					}
					else
					{
						ivInfo.setTag(b);
						LoadImage.addTask(b, ivInfo);
						
						if(position <= 5)
						{
							LoadImage.doTask();
						}
//						SimpleImageLoader.showImg(ivInfo, b,context);
					}
				}
			}
			 
			
//			SimpleImageLoader.showImg(ivHead, list.get(position).UserAvatar,0);
			 
			String price = "";
			try {
				price = list.get(position).getMetaData().get("价格") + "";
			} catch (Exception e) {
				price = "";
			}
			if (price.equals("null") || price.equals("")) {
				tvPrice.setVisibility(View.GONE);
			} else {
				tvPrice.setText(price);
			}
			tvDes.setText(list.get(position).getTitle());

			if(list.get(position).getDate() != null && !list.get(position).getDate().equals(""))
			{
				Date date = new Date(list.get(position).getDate() * 1000);
				SimpleDateFormat df = new SimpleDateFormat("MM月dd日",
						Locale.SIMPLIFIED_CHINESE);
				
				if(list.get(position).getAreaNames() != null && !list.get(position).getAreaNames().equals(""))
				{
					tvDateAndAddress.setText(df.format(date) + " "
							+ list.get(position).getAreaNames());
				}
				else
				{
					tvDateAndAddress.setText(df.format(date));
				}
			}
			else
			{
				if(list.get(position).getAreaNames() != null && !list.get(position).getAreaNames().equals(""))
				{
					tvDateAndAddress.setText(list.get(position).getAreaNames());
				}
				else
				{
					tvDateAndAddress.setVisibility(View.GONE);
				}
			}
			
			return v;
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
