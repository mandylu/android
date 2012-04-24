package com.quanleimu.activity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

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
import com.quanleimu.entity.GoodsList;
import com.quanleimu.entity.UserBean;
import com.quanleimu.jsonutil.JsonUtil;
import com.quanleimu.util.Communication;
import com.quanleimu.util.Helper;
import com.quanleimu.util.Util;

public class MyCenter extends BaseActivity implements OnScrollListener{

	// 定义控件
	public TextView tvTitle;
	public ListView lvGoodsList;
	public Button btnRefresh, btnEdit, btnFinish;
	public ImageView ivMyads, ivMyfav, ivMyhistory;
	public ImageView ivHomePage, ivCateMain, ivPostGoods, ivMyCenter,
			ivSetMain;

	// 定义变量
	public List<Bitmap> listBm = new ArrayList<Bitmap>();
	public List<GoodsDetail> listMyPost = new ArrayList<GoodsDetail>();
	public GoodsList goodsList = new GoodsList();
	public MyCenterAdapter adapter = null;
	public int tag = 0;
	private String mobile;
	private String json;
	private String password;
	UserBean user;

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume() {
		myApp.setActivity_type("mycenter");
		user = (UserBean) Util.loadDataFromLocate(MyCenter.this, "user");
		if (user != null) {
			mobile = user.getPhone();
			password = user.getPassword();
			// pd = ProgressDialog.show(MyCenter.this, "提示", "请稍候...");
			// pd.setCancelable(true);
			new Thread(new MyMessageThread()).start();
		} else {
			bundle.putInt("type", 1);
			intent.putExtras(bundle);
			intent.setClass(this, Login.class);
			startActivity(intent);
			finish();
		}
		super.onResume();
	}

	public int isConnect = -1;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.mycenter);
		super.onCreate(savedInstanceState);
		
		try {
			if (JadgeConnection() == false) {
				Toast.makeText(MyCenter.this, "网络连接异常", 3).show();
				isConnect = 0;
				myHandler.sendEmptyMessage(10);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// findViewById
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		btnRefresh = (Button) findViewById(R.id.btnRefresh);
		btnEdit = (Button) findViewById(R.id.btnEdit);
		btnFinish = (Button) findViewById(R.id.btnFinish);
		lvGoodsList = (ListView) findViewById(R.id.lvGoodsList);

		ivMyads = (ImageView) findViewById(R.id.ivMyads);
		ivMyfav = (ImageView) findViewById(R.id.ivMyfav);
		ivMyhistory = (ImageView) findViewById(R.id.ivMyhistory);

		ivHomePage = (ImageView) findViewById(R.id.ivHomePage);
		ivCateMain = (ImageView) findViewById(R.id.ivCateMain);
		ivPostGoods = (ImageView) findViewById(R.id.ivPostGoods);
		ivMyCenter = (ImageView) findViewById(R.id.ivMyCenter);
		ivSetMain = (ImageView) findViewById(R.id.ivSetMain);
		ivMyCenter.setImageResource(R.drawable.iv_mycenter_press);

		btnFinish.setVisibility(View.GONE);

		// 设置标题
		tvTitle.setText("我的发布信息");
		
		lvGoodsList.setOnScrollListener(this);

		// 设置监听器
		btnRefresh.setOnClickListener(this);
		btnEdit.setOnClickListener(this);
		ivMyads.setOnClickListener(this);
		ivMyfav.setOnClickListener(this);
		ivMyhistory.setOnClickListener(this);
		ivHomePage.setOnClickListener(this);
		ivCateMain.setOnClickListener(this);
		ivPostGoods.setOnClickListener(this);
		ivMyCenter.setOnClickListener(this);
		ivSetMain.setOnClickListener(this);
		btnFinish.setOnClickListener(this);

		lvGoodsList
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						intent.setClass(MyCenter.this, CenterDetail2.class);
						bundle.putString("backPageName", "我的发布消息");
						bundle.putInt("mycenter_pos", arg2);
						intent.putExtras(bundle);
						startActivity(intent);

					}

				});

		lvGoodsList.setAdapter(adapter);
	}

	class MyMessageThread implements Runnable {
		@Override
		public void run() {
			// TODO Auto-generated method stub

			String apiName = "ad_list";
			ArrayList<String> list = new ArrayList<String>();
			list.add("query=userId:" + user.getId() + " AND status:0");
			list.add("rows=45");
			list.add("rt=1");
			list.add("start=0");
			String url = Communication.getApiUrl(apiName, list);
			try {
				json = Communication.getDataByUrl(url);
				try {
					JSONObject jb = new JSONObject(json);
					if (json != null) {
						myHandler.sendEmptyMessage(1);
					} else {
						myHandler.sendEmptyMessage(2);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
				myHandler.sendEmptyMessage(10);
			}
		}
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
					Toast.makeText(MyCenter.this, "未获取到数据", 0).show();
				} else {
					listMyPost = goodsList.getData();
					myApp.setListMyPost(listMyPost);
					// 判断总数是不是已经超出当前集合长度
					// if (goodsList.getCount() > listGoods.size()) {
					// loadingLayout.setVisibility(View.VISIBLE);
					// } else {
					// loadingLayout.setVisibility(View.GONE);
					// }

					adapter = new MyCenterAdapter(MyCenter.this, listMyPost);
					lvGoodsList.setAdapter(adapter);

					// 开启线程下载图片
					new Thread(new Imagethread()).start();
				}

				break;
			case 2:
				if (pd != null) {
					pd.dismiss();
				}
				Toast.makeText(MyCenter.this, "未获取到数据", 3).show();
				break;
			case 3:
				int pos = msg.arg2;
				new Thread(new MyMessageDeleteThread(pos)).start();

				break;
			case 5:
				int pos2 = msg.arg2;
				try {
					JSONObject jb = new JSONObject(json);
					JSONObject js = jb.getJSONObject("error");
					String message = js.getString("message");
					int code = js.getInt("code");
					if (code == 0) {
						// 删除成功
						listMyPost.remove(pos2);
						myApp.setListMyPost(listMyPost);
						adapter.setList(listMyPost);
						adapter.notifyDataSetChanged();
						// listMyPost = myApp.getListMyPost();
						// listMyPost.remove(pos);
						// myApp.setListMyPost(listMyPost);
						// finish();
						Toast.makeText(MyCenter.this, message, 0).show();
					} else {
						// 删除失败
						Toast.makeText(MyCenter.this, "删除失败", 0).show();
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case 6:
				Toast.makeText(MyCenter.this, "删除失败", 0).show();
				break;
			case 10:
				if (pd != null) {
					pd.dismiss();
				}
				Toast.makeText(MyCenter.this, "网络连接异常", 3).show();
				break;
			}
			super.handleMessage(msg);
		}
	};

	// {"error":{"message":"删除信息成功。","code":0}}
	// {"error":{"message":"can not update deleted Ad","code":504}}

	class MyMessageDeleteThread implements Runnable {
		private int position;

		public MyMessageDeleteThread(int position) {
			this.position = position;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			json = "";
			String apiName = "ad_delete";
			ArrayList<String> list = new ArrayList<String>();
			list.add("mobile=" + mobile);
			String password1 = Communication.getMD5(password);
			password1 += Communication.apiSecret;
			String userToken = Communication.getMD5(password1);
			list.add("userToken=" + userToken);
			list.add("adId=" + listMyPost.get(position).getId());

			String url = Communication.getApiUrl(apiName, list);
			try {
				json = Communication.getDataByUrl(url);
				try {
					JSONObject jb = new JSONObject(json);
					if (json != null) {
						Message msg = myHandler.obtainMessage();
						msg.arg2 = position;
						msg.what = 5;
						myHandler.sendMessage(msg);
						// myHandler.sendEmptyMessageDelayed(5, 3000);5
					} else {
						myHandler.sendEmptyMessage(6);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	class Imagethread implements Runnable {
		@Override
		public void run() {
			try {
				for (int i = 0; i < listMyPost.size(); i++) {
					String b = (listMyPost.get(i).getImageList().getResize180())
							.substring(1, (listMyPost.get(i).getImageList()
									.getResize180()).length() - 1);
					b = Communication.replace(b);
					Bitmap bitmap = null;
					if (b.contains(",")) {
						String[] c = b.split(",");
						for (int j = 0; j < c.length; j++) {
							// imageUrl.add(c[j]);
							bitmap = Util.getImage(c[0]);
							if (bitmap == null) {
							} else {
								listBm.set(i, bitmap);
								Util.saveImage2File(MyCenter.this, bitmap, c[0]);
								Message msg = myHandler.obtainMessage();
								msg.arg1 = i;
								msg.what = 3;
								myHandler.sendMessage(msg);
							}
						}

					} else {
						bitmap = Util.getImage(b);
						if (bitmap == null) {
						} else {
							listBm.set(i, bitmap);
							Util.saveImage2File(MyCenter.this, bitmap, b);
							Message msg = myHandler.obtainMessage();
							msg.arg1 = i;
							msg.what = 3;
							myHandler.sendMessage(msg);
						}
					}

				}
			} catch (Exception e) {
				e.printStackTrace();
			} catch (Error ee) {
				ee.printStackTrace();
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnEdit:
			btnEdit.setVisibility(View.GONE);
			btnFinish.setVisibility(View.VISIBLE);
			// btnClear.setVisibility(View.VISIBLE);
			tag = 1;
			if(adapter != null)
			{
				adapter.notifyDataSetChanged();
			}
			break;
		case R.id.btnFinish:
			btnFinish.setVisibility(View.GONE);
			// btnClear.setVisibility(View.GONE);
			btnEdit.setVisibility(View.VISIBLE);
			tag = 0;
			if (adapter != null) {
				adapter.notifyDataSetChanged();
			}
			break;
		case R.id.btnRefresh:
			pd = ProgressDialog.show(MyCenter.this, "提示", "请稍候...");
			pd.setCancelable(true);
			new Thread(new MyMessageThread()).start();
			break;
		case R.id.ivMyads:
			break;
		case R.id.ivMyfav:
			intent.setClass(MyCenter.this, MyCenter_Store.class);
			bundle.putString("backPageName", "");
			intent.putExtras(bundle);
			startActivity(intent);
			overridePendingTransition(0, 0);
			MyCenter.this.finish();
			break;
		case R.id.ivMyhistory:
			intent.setClass(MyCenter.this, MyCenter_History.class);
			bundle.putString("backPageName", "");
			intent.putExtras(bundle);
			startActivity(intent);
			overridePendingTransition(0, 0);
			MyCenter.this.finish();

			break;
		case R.id.ivHomePage:
			intent.setClass(this, HomePage.class);
			intent.putExtras(bundle);
			startActivity(intent);
			break;
		case R.id.ivCateMain:
			intent.setClass(this, CateMain.class);
			intent.putExtras(bundle);
			startActivity(intent);
			break;
		case R.id.ivPostGoods:
			intent.setClass(this, PostGoodsCateMain.class);
			intent.putExtras(bundle);
			startActivity(intent);
			break;
		case R.id.ivMyCenter:
			break;
		case R.id.ivSetMain:
			intent.setClass(this, SetMain.class);
			intent.putExtras(bundle);
			startActivity(intent);
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

//			Bitmap mb = BitmapFactory.decodeResource(context.getResources(),
//					R.drawable.moren);
//			mb = Helper.toRoundCorner(mb, 20);
			if (list.get(position).getImageList() == null
					|| list.get(position).getImageList().equals("")
					|| list.get(position).getImageList().getResize180() == null
					|| list.get(position).getImageList().getResize180()
							.equals("")) {
				ivInfo.setImageBitmap(mb1);
			} else {
				if (isConnect == 0) {
					ivInfo.setImageBitmap(mb1);
				}else
				{
					String b = (list.get(position).getImageList().getResize180())
					.substring(1, (list.get(position).getImageList()
							.getResize180()).length() - 1);
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
					
//					SimpleImageLoader.showImg(ivInfo, c[0], context);
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
					
//					SimpleImageLoader.showImg(ivInfo, b, context);
				}
			}
				}
				
			}

			// SimpleImageLoader.showImg(ivHead,
			// list.get(position).UserAvatar,0);

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
					Message msg = myHandler.obtainMessage();
					msg.arg2 = pos;
					msg.what = 3;
					myHandler.sendMessage(msg);

				}
			});

			return v;
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
