package com.quanleimu.activity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.quanleimu.entity.GoodsDetail;
import com.quanleimu.entity.GoodsList;
import com.quanleimu.entity.UserBean;
import com.quanleimu.jsonutil.JsonUtil;
import com.quanleimu.util.Communication;
import com.quanleimu.util.Helper;
import com.quanleimu.util.Util;
import com.quanleimu.view.SetMain;
import com.quanleimu.adapter.GoodsListAdapter;
import com.quanleimu.activity.GoodDetail;

public class MyCenter extends BaseActivity implements OnScrollListener{
	private final int MCMESSAGE_MYPOST_SUCCESS = 0;
	private final int MCMESSAGE_MYPOST_FAIL = 1;
	private final int MCMESSAGE_DELETE = 2;
	private final int MCMESSAGE_DELETE_SUCCESS = 3;
	private final int MCMESSAGE_DELETE_FAIL = 4;
	private final int MCMESSAGE_DELETEALL = 5;
	private final int MCMESSAGE_MYHISTORY_UPDATE_SUCCESS = 6;
	private final int MCMESSAGE_MYHISTORY_UPDATE_FAIL = 7;
	private final int MCMESSAGE_MYFAV_UPDATE_SUCCESS = 8;
	private final int MCMESSAGE_MYFAV_UPDATE_FAIL = 9;	
	private final int MCMESSAGE_NETWORKERROR = 10;

	public TextView tvTitle;
	public ListView lvGoodsList;
	public Button btnRefresh, btnEdit;
	public ImageView ivMyads, ivMyfav, ivMyhistory;

	private List<GoodsDetail> listMyPost = new ArrayList<GoodsDetail>();
	private List<GoodsDetail> goodsList = new ArrayList<GoodsDetail>();
	public GoodsListAdapter adapter = null;
	private String mobile;
	private String json;
	private String password;
	UserBean user;
	private int currentPage = -1;//-1:mypost, 0:myfav, 1:history

	private void rebuildPage(){
		if(-1 == currentPage){
			ivMyads.setImageResource(R.drawable.btn_my_myads_press);
			ivMyfav.setImageResource(R.drawable.btn_my_myfav);
			ivMyhistory.setImageResource(R.drawable.btn_my_myhistory);
			tvTitle.setText("我发布的信息");
			if(listMyPost.size() != 0){
				adapter.setList(listMyPost);
				adapter.notifyDataSetChanged();
			}
			else{
				if (user != null) {
					mobile = user.getPhone();
					password = user.getPassword();
					pd = ProgressDialog.show(MyCenter.this, "提示", "请稍候...");
					pd.setCancelable(true);
					new Thread(new UpdateThread(currentPage)).start();
				} else {
					bundle.putInt("type", 1);
					intent.putExtras(bundle);
					intent.setClass(this, Login.class);
					startActivity(intent);
					finish();
				}
			}
		}
		else if(0 == currentPage){
			ivMyads.setImageResource(R.drawable.btn_my_myads);
			ivMyfav.setImageResource(R.drawable.btn_my_myfav_press);
			ivMyhistory.setImageResource(R.drawable.btn_my_myhistory);
			
			tvTitle.setText("我的收藏");
			goodsList = myApp.getListMyStore();
			adapter.setList(goodsList);
			adapter.notifyDataSetChanged();
		}
		else{
			ivMyads.setImageResource(R.drawable.btn_my_myads);
			ivMyfav.setImageResource(R.drawable.btn_my_myfav);
			ivMyhistory.setImageResource(R.drawable.btn_my_myhistory_press);
			
			tvTitle.setText("我的历史");
			goodsList = myApp.getListLookHistory();
			adapter.setList(goodsList);
			adapter.notifyDataSetChanged();
		}
		

		lvGoodsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {			
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				if(currentPage == -1){
					bundle.putSerializable("currentGoodsDetail", listMyPost.get(arg2));
				}
				else if(0 == currentPage || 1 == currentPage){
					bundle.putSerializable("currentGoodsDetail", goodsList.get(arg2));
				}
				intent.setClass(MyCenter.this, GoodDetail.class);
				bundle.putString("backPageName", "个人中心");
				intent.putExtras(bundle);
				startActivity(intent);
			}

		});
		
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume() {
//		myApp.setActivity_type("mycenter");
		super.onResume();
		rebuildPage();		
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.mycenter);
		super.onCreate(savedInstanceState);
		user = (UserBean) Util.loadDataFromLocate(MyCenter.this, "user");
		try {
			if (JadgeConnection() == false) {
				Toast.makeText(MyCenter.this, "网络连接异常", 3).show();
//				isConnect = 0;
				myHandler.sendEmptyMessage(MCMESSAGE_NETWORKERROR);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// findViewById
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		btnRefresh = (Button) findViewById(R.id.btnRefresh);
		btnEdit = (Button) findViewById(R.id.btnEdit);
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

//		tvTitle.setText("我发布的信息");
		lvGoodsList.setDivider(null);
		lvGoodsList.setOnScrollListener(this);

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
		
		adapter = new GoodsListAdapter(MyCenter.this, this.listMyPost);
		adapter.setMessageOutOnDelete(myHandler, MCMESSAGE_DELETE);
		lvGoodsList.setAdapter(adapter);
	}
	
	class UpdateThread implements Runnable{
		private int currentPage = -1;
		public UpdateThread(int currentPage){
			this.currentPage = currentPage;
		}
		
		@Override
		public void run(){
			String apiName = "ad_list";
			ArrayList<String>list = new ArrayList<String>();
			list.add("rt=1");
			list.add("start=0");							
			int msgToSend = -1;
			int msgToSendOnFail = -1;
			if(currentPage == -1){
				list.add("query=userId:" + user.getId() + " AND status:0");
				list.add("rows=45");
				msgToSend = MCMESSAGE_MYPOST_SUCCESS;
				msgToSendOnFail = MCMESSAGE_MYPOST_FAIL;
			}
			else{
				List<GoodsDetail> details = null;
				if(currentPage == 0){
					details = myApp.getListMyStore();
					msgToSend = MCMESSAGE_MYFAV_UPDATE_SUCCESS;
					msgToSendOnFail = MCMESSAGE_MYFAV_UPDATE_FAIL;
				}
				else if(currentPage == 1){
					details = myApp.getListLookHistory();
					msgToSend = MCMESSAGE_MYHISTORY_UPDATE_SUCCESS;
					msgToSendOnFail = MCMESSAGE_MYHISTORY_UPDATE_FAIL;					
				}
				if(details != null && details.size() > 0){
					String ids = "id:" + details.get(0).getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID);
					for(int i = 1; i < details.size(); ++ i){
						ids += " OR " + "id:" + details.get(i).getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID);  
					}
					list.add("query=(" + ids + ")");
					list.add("rows=100");
				}
			}
			String url = Communication.getApiUrl(apiName, list);
			try {
				json = Communication.getDataByUrl(url);
				if (json != null) {
					myHandler.sendEmptyMessage(msgToSend);
				} else {
					myHandler.sendEmptyMessage(msgToSendOnFail);
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
				myHandler.sendEmptyMessage(MCMESSAGE_NETWORKERROR);
			}			
		}
	}

	Handler myHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MCMESSAGE_MYPOST_SUCCESS:
				if (pd != null) {
					pd.dismiss();
				}
				GoodsList gl = JsonUtil.getGoodsListFromJson(json); 
				if (gl == null || gl.getCount() == 0) {
					Toast.makeText(MyCenter.this, "您尚未发布信息，", 0).show();
				} else {
					listMyPost = gl.getData();
					myApp.setListMyPost(listMyPost);
					rebuildPage();
				}
				break;
			case MCMESSAGE_MYFAV_UPDATE_SUCCESS:
				if (pd != null) {
					pd.dismiss();
				}
				GoodsList glFav = JsonUtil.getGoodsListFromJson(json); 
				if (glFav != null && glFav.getCount() > 0) {
					myApp.setListMyStore(glFav.getData());
					rebuildPage();
				}
				break;
			case MCMESSAGE_MYHISTORY_UPDATE_SUCCESS:
				if (pd != null) {
					pd.dismiss();
				}
				GoodsList glHistory = JsonUtil.getGoodsListFromJson(json); 
				if (glHistory != null && glHistory.getCount() > 0) {
					myApp.setListLookHistory(glHistory.getData());
					rebuildPage();
				}				
				break;
			case MCMESSAGE_MYPOST_FAIL:
			case MCMESSAGE_MYFAV_UPDATE_FAIL:
			case MCMESSAGE_MYHISTORY_UPDATE_FAIL:				
				if (pd != null) {
					pd.dismiss();
				}
				Toast.makeText(MyCenter.this, "未获取到数据", 3).show();
				break;
			case MCMESSAGE_DELETE:
				int pos = msg.arg2;
				if(MyCenter.this.currentPage == -1){
					new Thread(new MyMessageDeleteThread(pos)).start();
				}
				else if(0 == MyCenter.this.currentPage){
					goodsList.remove(pos);
					myApp.setListMyStore(goodsList);
					Helper.saveDataToLocate(MyCenter.this, "listMyStore", goodsList);
					adapter.setList(goodsList);
					adapter.notifyDataSetChanged();
				}
				else if(1 == MyCenter.this.currentPage){
					goodsList.remove(pos);
					myApp.setListLookHistory(goodsList);
					Helper.saveDataToLocate(MyCenter.this, "listLookHistory", goodsList);
					adapter.setList(goodsList);
					adapter.notifyDataSetChanged();					
				}

				break;
			case MCMESSAGE_DELETEALL:
				break;
			case MCMESSAGE_DELETE_SUCCESS:
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
						Toast.makeText(MyCenter.this, message, 0).show();
					} else {
						// 删除失败
						Toast.makeText(MyCenter.this, "删除失败,请稍后重试！", 0).show();
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case MCMESSAGE_DELETE_FAIL:
				Toast.makeText(MyCenter.this, "删除失败，请稍后重试！", 0).show();
				break;
			case MCMESSAGE_NETWORKERROR:
				if (pd != null) {
					pd.dismiss();
				}
				Toast.makeText(MyCenter.this, "网络连接失败，请检查设置！", 3).show();
				break;
			}
			super.handleMessage(msg);
		}
	};

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
			list.add("adId=" + listMyPost.get(position).getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID));

			String url = Communication.getApiUrl(apiName, list);
			try {
				json = Communication.getDataByUrl(url);
				if (json != null) {
					Message msg = myHandler.obtainMessage();
					msg.arg2 = position;
					msg.what = MCMESSAGE_DELETE_SUCCESS;
					myHandler.sendMessage(msg);
					// myHandler.sendEmptyMessageDelayed(5, 3000);5
				} else {
					myHandler.sendEmptyMessage(MCMESSAGE_DELETE_FAIL);
				} 

			} catch (UnsupportedEncodingException e) {
				myHandler.sendEmptyMessage(MCMESSAGE_NETWORKERROR);
			} catch (IOException e) {
				myHandler.sendEmptyMessage(MCMESSAGE_NETWORKERROR);
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnEdit:
			if(btnEdit.getText().equals("编辑")){
				btnEdit.setBackgroundResource(R.drawable.btn_clearall);
				btnEdit.setText("完成");
//				btnRefresh.setBackgroundResource(R.drawable.btn_clearall);
//				btnRefresh.setText("清空");
				if(adapter != null){
					adapter.setHasDelBtn(true);
				}
			}
			else{
				btnEdit.setBackgroundResource(R.drawable.btn_search);
				btnEdit.setText("编辑");
//				btnRefresh.setBackgroundResource(R.drawable.btn_search);
//				btnRefresh.setText("更新");				
				adapter.setHasDelBtn(false);
			}
			if(adapter != null)
			{
				adapter.notifyDataSetChanged();
			}
			break;
		case R.id.btnRefresh:
			pd = ProgressDialog.show(MyCenter.this, "提示", "请稍候...");
			pd.setCancelable(true);
			new Thread(new UpdateThread(currentPage)).start();
			break;
		case R.id.ivMyads:
			this.currentPage = -1;
			rebuildPage();
			break;
		case R.id.ivMyfav:
			this.currentPage = 0;
			rebuildPage();
			break;
		case R.id.ivMyhistory:
			this.currentPage = 1;
			rebuildPage();
			break;
		case R.id.ivHomePage:
			intent.setClass(this, HomePage.class);
			intent.putExtras(bundle);
			startActivity(intent);
			overridePendingTransition(0, 0);
			break;
		case R.id.ivCateMain:
			intent.setClass(this, CateMain.class);
			intent.putExtras(bundle);
			startActivity(intent);
			overridePendingTransition(0, 0);
			break;
		case R.id.ivPostGoods:
			intent.setClass(this, PostGoodsCateMain.class);
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
