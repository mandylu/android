package com.quanleimu.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.quanleimu.entity.GoodsDetail;
import com.quanleimu.entity.GoodsInfo;
import com.quanleimu.entity.GoodsList;
import com.quanleimu.util.Communication;
import com.quanleimu.util.Helper;
import com.quanleimu.util.Util;

public class MyCenter_History extends BaseActivity implements OnScrollListener{

	//定义控件
	public TextView tvTitle,tvInfo;
	public ListView lvGoodsList;
	public Button btnRefresh,btnEdit,btnFinish,btnClear;
	public ImageView ivMyads,ivMyfav,ivMyhistory;
	
	//定义变量
	public List<GoodsInfo> list = new ArrayList<GoodsInfo>();
	public MyCenterAdapter adapter = null;
	public int tag = 0;
	public List<GoodsDetail> listLookHistory = new ArrayList<GoodsDetail>();			//历史list
	public GoodsList goodsList = new GoodsList();
	
	public String historyIds = "";
	public int isConnect = -1;
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume() {
		myApp.setActivity_type("mycenter");
		super.onResume();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.mycenter_history);
		super.onCreate(savedInstanceState);

		//findViewById
		tvTitle = (TextView)findViewById(R.id.tvTitle);
		tvInfo = (TextView)findViewById(R.id.tvInfo);
		btnRefresh = (Button)findViewById(R.id.btnRefresh);
		btnEdit = (Button)findViewById(R.id.btnEdit);
		btnFinish = (Button)findViewById(R.id.btnFinish);
		btnClear = (Button)findViewById(R.id.btnClear);
		lvGoodsList = (ListView)findViewById(R.id.lvGoodsList);
		
		ivMyads = (ImageView)findViewById(R.id.ivMyads);
		ivMyfav = (ImageView)findViewById(R.id.ivMyfav);
		ivMyhistory = (ImageView)findViewById(R.id.ivMyhistory);
		
		//设置标题
		tvTitle.setText("浏览历史");
		
		lvGoodsList.setOnScrollListener(this);
		
		//设置监听器
		btnRefresh.setOnClickListener(this);
		btnEdit.setOnClickListener(this);
		btnFinish.setOnClickListener(this);
		btnClear.setOnClickListener(this);
		ivMyads.setOnClickListener(this);
		ivMyfav.setOnClickListener(this);
		ivMyhistory.setOnClickListener(this);
		
		//默认完成和全部清空不显示
		btnFinish.setVisibility(View.GONE);
		btnRefresh.setVisibility(View.GONE);
		btnClear.setVisibility(View.GONE);
		
		ivHomePage = (ImageView)findViewById(R.id.ivHomePage);
		ivCateMain = (ImageView)findViewById(R.id.ivCateMain);
		ivPostGoods = (ImageView)findViewById(R.id.ivPostGoods);
		ivMyCenter = (ImageView)findViewById(R.id.ivMyCenter);
		ivSetMain = (ImageView)findViewById(R.id.ivSetMain);
		ivHomePage.setOnClickListener(this);
		ivCateMain.setOnClickListener(this);
		ivPostGoods.setOnClickListener(this);
		ivMyCenter.setOnClickListener(this);
		ivSetMain.setOnClickListener(this);
		
		ivMyCenter.setImageResource(R.drawable.iv_mycenter_press);
		ivMyhistory.setImageResource(R.drawable.btn_my_myhistory_press);
		
		lvGoodsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) 
			{
				if(tag == 1)
				{
					Toast.makeText(MyCenter_History.this, "请先完成编辑操作", 3).show();
				}
				else
				{
					intent.setClass(MyCenter_History.this, GetGoodsDetail3.class);
					bundle.putString("backPageName", "浏览历史");
					bundle.putInt("mycenter_pos", arg2);
					bundle.putString("mycenter_type", "mycenter_history");
					intent.putExtras(bundle);
					startActivity(intent);
				}
			}
		});
		
		listLookHistory = myApp.getListLookHistory();
		
		if(listLookHistory == null || listLookHistory.size() == 0)
		{
			listLookHistory = new ArrayList<GoodsDetail>();
			tvInfo.setVisibility(View.VISIBLE);
		}
		else
		{
			tvInfo.setVisibility(View.GONE);
		}
		adapter = new MyCenterAdapter(MyCenter_History.this, listLookHistory);
		lvGoodsList.setAdapter(adapter);
		
		try {
			if(JadgeConnection() == false)
			{
				Toast.makeText(MyCenter_History.this, "网络连接异常", 3).show();
				isConnect = 0;
				myHandler.sendEmptyMessage(10);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onClick(View v) {
		switch(v.getId())
		{
			case R.id.btnEdit:
				btnEdit.setVisibility(View.GONE);
				btnFinish.setVisibility(View.VISIBLE);
				btnClear.setVisibility(View.VISIBLE);
				tag = 1;
				adapter.notifyDataSetChanged();
				break;
			case R.id.btnFinish:
				btnFinish.setVisibility(View.GONE);
				btnClear.setVisibility(View.GONE);
				btnEdit.setVisibility(View.VISIBLE);
				tag = 0;
				if(adapter != null)
				{
					adapter.notifyDataSetChanged();
				}
				break;
			case R.id.btnRefresh:
				break; 
			case R.id.btnClear:
				if (listLookHistory == null || listLookHistory.size() == 0) {

				} else {
					listLookHistory.clear();
					listLookHistory = null;
					myApp.setListLookHistory(listLookHistory);
					Helper.saveDataToLocate(MyCenter_History.this, "listLookHistory", listLookHistory);
					adapter = null;
					lvGoodsList.setVisibility(View.GONE);
					tvInfo.setVisibility(View.VISIBLE);
				}
				break; 
			case R.id.ivMyads:
				if(tag == 1)
				{
					Toast.makeText(MyCenter_History.this, "请先完成编辑操作", 3).show();
				}
				else
				{
					intent.setClass(MyCenter_History.this, MyCenter.class);
					bundle.putString("backPageName", "");
					intent.putExtras(bundle);
					startActivity(intent);
					overridePendingTransition(0, 0);
					MyCenter_History.this.finish();
				}
				break;
			case R.id.ivMyfav:
				if(tag == 1)
				{
					Toast.makeText(MyCenter_History.this, "请先完成编辑操作", 3).show();
				}
				else
				{
					intent.setClass(MyCenter_History.this, MyCenter_Store.class);
					bundle.putString("backPageName", "");
					intent.putExtras(bundle);
					startActivity(intent);
					overridePendingTransition(0, 0);
					MyCenter_History.this.finish();
				}
				break;
			case R.id.ivMyhistory:
				break;
			case R.id.ivHomePage:
				if(tag == 1)
				{
					Toast.makeText(MyCenter_History.this, "请先完成编辑操作", 3).show();
				}
				else
				{
					intent.setClass(this, HomePage.class);
					intent.putExtras(bundle);
					startActivity(intent);
				}
				break;
			case R.id.ivCateMain:
				if(tag == 1)
				{
					Toast.makeText(MyCenter_History.this, "请先完成编辑操作", 3).show();
				}
				else
				{
					intent.setClass(this, CateMain.class);
					intent.putExtras(bundle);
					startActivity(intent);
				}
				break;
			case R.id.ivPostGoods:
				if(tag == 1)
				{
					Toast.makeText(MyCenter_History.this, "请先完成编辑操作", 3).show();
				}
				else
				{
					intent.setClass(this, PostGoodsCateMain.class);
					intent.putExtras(bundle);
					startActivity(intent);
				}
				break;
			case R.id.ivMyCenter:
				break;
			case R.id.ivSetMain:
				if(tag == 1)
				{
					Toast.makeText(MyCenter_History.this, "请先完成编辑操作", 3).show();
				}
				else
				{
					intent.setClass(this, SetMain.class);
					intent.putExtras(bundle);
					startActivity(intent);
				}
				break;
		}
		super.onClick(v);
	}
	
	public int pos = -1;
	public class MyCenterAdapter extends BaseAdapter {

		public Context context;
		public TextView tvDes,tvPrice,tvDateAndAddress;
		public ImageView ivInfo;
		public List<GoodsDetail> list = new ArrayList<GoodsDetail>();
		public Button btnDelete;

		public List<GoodsDetail> getList() {
			return list;
		}


		public void setList(List<GoodsDetail> list) {
			this.list = list;
		}


		public MyCenterAdapter() {
			super();
			// TODO Auto-generated constructor stub
		}
		

		public MyCenterAdapter(Context context,List<GoodsDetail> list) {
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
		public View getView(final int position, View convertView, ViewGroup parent) {
			pos = position;
			LayoutInflater inflater = LayoutInflater.from(context);
			View v = null;
//			if(convertView == null)
//			{
				v = inflater.inflate(R.layout.item_mycenter, null);
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
			btnDelete = (Button) v.findViewById(R.id.btnDelete);
			
			
			if(tag == 0)
			{
				btnDelete.setVisibility(View.GONE);
			}
			else if(tag == 1)
			{
				btnDelete.setVisibility(View.VISIBLE);
			}
			
			
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
				if(isConnect == 0)
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
						}
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
			
			
			btnDelete.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					pos = position;
					System.out.println(pos+" dfdf");
					myHandler.sendEmptyMessage(3);
					
				}
			});
			return v;
		}

	}
	
	public Handler myHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch(msg.what)
			{
				case 3:
					System.out.println("pos--->"+pos);
					listLookHistory.remove(pos);
					adapter.setList(listLookHistory);
					adapter.notifyDataSetChanged();
					
					myApp.setListLookHistory(listLookHistory);
					Helper.saveDataToLocate(MyCenter_History.this, "listLookHistory", listLookHistory);
					break;
				case 10:
					if(adapter != null)
					{
						adapter.notifyDataSetChanged();
					}
					break;
			}
			super.handleMessage(msg);
		}
		
	};
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if(scrollState == SCROLL_STATE_IDLE)
		{
			LoadImage.doTask();
		}
	}
	
}
