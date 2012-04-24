package com.quanleimu.activity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
import com.quanleimu.jsonutil.JsonUtil;
import com.quanleimu.util.Communication;
import com.quanleimu.util.Helper;
import com.quanleimu.util.Util;

public class MyCenter_Store extends BaseActivity implements OnScrollListener{

	// 定义控件
	public TextView tvTitle, tvInfo;
	public ListView lvGoodsList;
	public Button btnRefresh, btnEdit, btnFinish, btnClear;
	public ImageView ivMyads, ivMyfav, ivMyhistory;

	// 定义变量
	public List<GoodsInfo> list = new ArrayList<GoodsInfo>();
	public MyCenterAdapter adapter = null;
	public int tag = 0;
	public List<GoodsDetail> listMyStore = new ArrayList<GoodsDetail>(); // 历史list
	public GoodsList goodsList = new GoodsList();

	public String storeIds = "";
	public int isConnect = -1;

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override 
	protected void onResume() {
		super.onResume();
		if (adapter != null) {
			adapter.notifyDataSetChanged();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.mycenter_store);
		super.onCreate(savedInstanceState);

		// findViewById
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvInfo = (TextView) findViewById(R.id.tvInfo); 
		btnRefresh = (Button) findViewById(R.id.btnRefresh);
		btnEdit = (Button) findViewById(R.id.btnEdit);
		btnFinish = (Button) findViewById(R.id.btnFinish);
		btnClear = (Button) findViewById(R.id.btnClear);
		lvGoodsList = (ListView) findViewById(R.id.lvGoodsList);

		ivMyads = (ImageView) findViewById(R.id.ivMyads);
		ivMyfav = (ImageView) findViewById(R.id.ivMyfav);
		ivMyhistory = (ImageView) findViewById(R.id.ivMyhistory);

		// 设置标题
		tvTitle.setText("我的收藏"); 
		
		lvGoodsList.setOnScrollListener(this);

		// 设置监听器
		btnRefresh.setOnClickListener(this);
		btnEdit.setOnClickListener(this);
		btnFinish.setOnClickListener(this);
		btnClear.setOnClickListener(this);
		ivMyads.setOnClickListener(this);
		ivMyfav.setOnClickListener(this);
		ivMyhistory.setOnClickListener(this);

		// 默认完成和全部清空不显示
		btnFinish.setVisibility(View.GONE);

		btnClear.setVisibility(View.GONE);

		ivHomePage = (ImageView) findViewById(R.id.ivHomePage);
		ivCateMain = (ImageView) findViewById(R.id.ivCateMain);
		ivPostGoods = (ImageView) findViewById(R.id.ivPostGoods);
		ivMyCenter = (ImageView) findViewById(R.id.ivMyCenter);
		ivSetMain = (ImageView) findViewById(R.id.ivSetMain);
		ivHomePage.setOnClickListener(this);
		ivCateMain.setOnClickListener(this);
		ivPostGoods.setOnClickListener(this);
		ivMyCenter.setOnClickListener(this);
		ivSetMain.setOnClickListener(this);

		ivMyCenter.setImageResource(R.drawable.iv_mycenter_press);
		ivMyfav.setImageResource(R.drawable.btn_my_myads_press);

		lvGoodsList
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						System.out.println("tag ---->" + tag);
						if (tag == 1) {
							Toast.makeText(MyCenter_Store.this, "请先完成编辑操作", 3)
									.show();
						} else {
							intent.setClass(MyCenter_Store.this,
									GetGoodsDetail3.class);
							bundle.putString("backPageName", "我的收藏");
							bundle.putInt("mycenter_pos", arg2);
							bundle.putString("mycenter_type", "mycenter_store");
							intent.putExtras(bundle);
							startActivity(intent);
						}
					}

				});

		listMyStore = myApp.getListMyStore();

		if (listMyStore == null || listMyStore.size() == 0) {
			listMyStore = new ArrayList<GoodsDetail>();
			tvInfo.setVisibility(View.VISIBLE);
		} else {
			tvInfo.setVisibility(View.GONE);
		}
		adapter = new MyCenterAdapter(MyCenter_Store.this, listMyStore);
		lvGoodsList.setAdapter(adapter);

		try {
			if (JadgeConnection() == false) {
				Toast.makeText(MyCenter_Store.this, "网络连接异常", 3).show();
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
		switch (v.getId()) {
		case R.id.btnEdit:
			btnEdit.setVisibility(View.GONE);
			btnFinish.setVisibility(View.VISIBLE);
			btnRefresh.setVisibility(View.GONE);
			tag = 1;
			adapter.notifyDataSetChanged();
			break;
		case R.id.btnFinish:
			btnRefresh.setVisibility(View.VISIBLE);
			btnFinish.setVisibility(View.GONE);
			btnEdit.setVisibility(View.VISIBLE);
			tag = 0;
			adapter.notifyDataSetChanged();
			break;
		case R.id.btnRefresh:
			if (listMyStore != null && listMyStore.size() != 0) {
				for (int i = 0; i < listMyStore.size(); i++) {
					if (i != listMyStore.size() - 1) {
						storeIds = storeIds + "id:"
								+ listMyStore.get(i).getId() + " OR ";
					} else {
						storeIds = storeIds + "id:"
								+ listMyStore.get(i).getId();
					}
				}
				System.out.println("stoIds ------ >" + storeIds);
				pd = ProgressDialog.show(this, "提示", "请稍候...");
				pd.setCancelable(true);
				new Thread(new MyHistoryThread()).start();
			} else {
				tvInfo.setVisibility(View.VISIBLE);
				lvGoodsList.setVisibility(View.GONE);
			}

			break;
		case R.id.ivMyads:
			if (tag == 1) {
				Toast.makeText(MyCenter_Store.this, "请先完成编辑操作", 3).show();
			} else {
				intent.setClass(MyCenter_Store.this, MyCenter.class);
				bundle.putString("backPageName", "");
				intent.putExtras(bundle);
				startActivity(intent);
				overridePendingTransition(0, 0);
				MyCenter_Store.this.finish();
			}
			break;
		case R.id.ivMyfav:

			break;
		case R.id.ivMyhistory:
			if (tag == 1) {
				Toast.makeText(MyCenter_Store.this, "请先完成编辑操作", 3).show();
			} else {
				intent.setClass(MyCenter_Store.this, MyCenter_History.class);
				bundle.putString("backPageName", "");
				intent.putExtras(bundle);
				startActivity(intent);
				overridePendingTransition(0, 0);
				MyCenter_Store.this.finish();
			}
			break;
		case R.id.ivHomePage:
			if (tag == 1) {
				Toast.makeText(MyCenter_Store.this, "请先完成编辑操作", 3).show();
			} else {
				intent.setClass(this, HomePage.class);
				intent.putExtras(bundle);
				startActivity(intent);
			}
			break;
		case R.id.ivCateMain:
			if (tag == 1) {
				Toast.makeText(MyCenter_Store.this, "请先完成编辑操作", 3).show();
			} else {
				intent.setClass(this, CateMain.class);
				intent.putExtras(bundle);
				startActivity(intent);
			}
			break;
		case R.id.ivPostGoods:
			if (tag == 1) {
				Toast.makeText(MyCenter_Store.this, "请先完成编辑操作", 3).show();
			} else {
				intent.setClass(this, PostGoodsCateMain.class);
				intent.putExtras(bundle);
				startActivity(intent);
			}
			break;
		case R.id.ivMyCenter:
			break;
		case R.id.ivSetMain:
			if (tag == 1) {
				Toast.makeText(MyCenter_Store.this, "请先完成编辑操作", 3).show();
			} else {
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
		public TextView tvDes, tvPrice, tvDateAndAddress;
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

		public MyCenterAdapter(Context context, List<GoodsDetail> list) {
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
//			if (convertView == null) {
				v = inflater.inflate(R.layout.item_mycenter, null);
//			} else {
//				v = (View) convertView;
//			}

			if (list.size() == 1) {
				v.setBackgroundResource(R.drawable.btn_s_bg);
			} else {
				if (position == 0) {
					v.setBackgroundResource(R.drawable.btn_top_bg);
				} else if (position == list.size() - 1) {
					v.setBackgroundResource(R.drawable.btn_down_bg);
				} else {
					v.setBackgroundResource(R.drawable.btn_m_bg);
				}
			}
			tvDes = (TextView) v.findViewById(R.id.tvDes);
			tvPrice = (TextView) v.findViewById(R.id.tvPrice);
			tvPrice.setTextColor(Color.RED);
			tvDateAndAddress = (TextView) v.findViewById(R.id.tvDateAndAddress);
			tvDateAndAddress.setTextColor(R.color.hui);
			btnDelete = (Button) v.findViewById(R.id.btnDelete);

			if (tag == 0) {
				btnDelete.setVisibility(View.GONE);
			} else if (tag == 1) {
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
			switch (type) {
			case 240:
				lp = new RelativeLayout.LayoutParams(45, 45);
				break;
			case 320:
				lp = new RelativeLayout.LayoutParams(60, 60);
				break;
			case 480:
				lp = new RelativeLayout.LayoutParams(90, 90);
				break;
			case 540:
				lp = new RelativeLayout.LayoutParams(100, 100);
				break;
			case 640:
				lp = new RelativeLayout.LayoutParams(120, 120);
				break;
			default:
				 lp= new RelativeLayout.LayoutParams(140,140);
				break;
			}

			lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			ivInfo.setLayoutParams(lp);

			

			if (list.get(position).getImageList() == null
					|| list.get(position).getImageList().equals("")
					|| list.get(position).getImageList().getResize180() == null
					|| list.get(position).getImageList().getResize180()
							.equals("")) {
				ivInfo.setImageBitmap(mb1);
			} else {
				if (isConnect == 0) {
					ivInfo.setImageBitmap(mb1);
				} else {
					String b = (list.get(position).getImageList()
							.getResize180()).substring(1, (list.get(position)
							.getImageList().getResize180()).length() - 1);
					b = Communication.replace(b);

					if (b.contains(",")) {
						String[] c = b.split(",");
						if (c[0] == null || c[0].equals("")) {
							ivInfo.setImageBitmap(mb1);
						} else {
							
							ivInfo.setTag(c[0]);
							LoadImage.addTask(c[0], ivInfo);
							
							if(position <= 5)
							{
								LoadImage.doTask();
							}
//							SimpleImageLoader.showImg(ivInfo, c[0], context);
						}
					} else {
						if (b == null || b.equals("")) {
							ivInfo.setImageBitmap(mb1);
						} else {
							ivInfo.setTag(b);
							LoadImage.addTask(b, ivInfo);
							
							if(position <= 5)
							{
								LoadImage.doTask();
							}
//							SimpleImageLoader.showImg(ivInfo, b, context);
						}
					}
				}
			}

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
					myHandler.sendEmptyMessage(3);

				}
			});

			return v;
		}

	}

	public Handler myHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				if (pd != null) {
					pd.dismiss();
				}
				goodsList = JsonUtil.getGoodsListFromJson(Communication
						.decodeUnicode(json));
				if (goodsList == null || goodsList.equals("")
						|| goodsList.getCount() == 0) {
					tvInfo.setVisibility(View.VISIBLE);
					lvGoodsList.setVisibility(View.GONE);
				} else {
					tvInfo.setVisibility(View.GONE);
					lvGoodsList.setVisibility(View.VISIBLE);
					listMyStore = goodsList.getData();
					myApp.setListMyStore(listMyStore);
					// 重新更新本地收藏记录
					Helper.saveDataToLocate(MyCenter_Store.this, "listMyStore",
							listMyStore);

					adapter.setList(listMyStore);
					adapter.notifyDataSetChanged();
				}

				break;
			case 2:
				if (pd != null) {
					pd.dismiss();
				}

				tvInfo.setVisibility(View.VISIBLE);
				lvGoodsList.setVisibility(View.GONE);
				break;
			case 3:
				// 删除当前收藏并要删除其对应的本地的图片

				listMyStore.remove(pos);
				myApp.setListMyStore(listMyStore);
				Helper.saveDataToLocate(MyCenter_Store.this, "listMyStore",
						listMyStore);
				adapter.setList(listMyStore);
				adapter.notifyDataSetChanged();
				break;
			case 10:
				isConnect = 0;
				break;
			}
			super.handleMessage(msg);
		}

	};

	public String json = "";

	// 读取 浏览历史
	class MyHistoryThread implements Runnable {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			String apiName = "ad_list";
			ArrayList<String> list = new ArrayList<String>();
			list.add("query=(" + storeIds + ")");
			list.add("rows=100");
			list.add("start=0");
			String url = Communication.getApiUrl(apiName, list);
			try {
				json = Communication.getDataByUrl(url);
				if (json != null && !json.equals("")) {
					myHandler.sendEmptyMessage(1);
				} else {
					myHandler.sendEmptyMessage(2);
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

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
