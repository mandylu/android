package com.quanleimu.view;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
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
import com.quanleimu.adapter.GoodsListAdapter;
import com.quanleimu.view.BaseView;
import com.quanleimu.view.BaseView.TitleDef;
import com.quanleimu.activity.BaseActivity;
import com.quanleimu.activity.MyApplication;
import com.quanleimu.activity.R;
public class PersonalCenterView extends BaseView implements OnScrollListener, View.OnClickListener{
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

	public ListView lvGoodsList;
	public ImageView ivMyads, ivMyfav, ivMyhistory;

	private List<GoodsDetail> listMyPost = new ArrayList<GoodsDetail>();
	private List<GoodsDetail> goodsList = new ArrayList<GoodsDetail>();
	public GoodsListAdapter adapter = null;
	private String mobile;
	private String json;
	private String password;
	UserBean user;
	private int currentPage = -1;//-1:mypost, 0:myfav, 1:history
	private Bundle bundle;
	private int buttonStatus = -1;//-1:edit 0:finish
	
	public PersonalCenterView(Context context, Bundle bundle){
		super(context, bundle);
		this.bundle = bundle;
		init();
	}

	private void rebuildPage(){
		if(-1 == currentPage){
			ivMyads.setImageResource(R.drawable.btn_my_myads_press);
			ivMyfav.setImageResource(R.drawable.btn_my_myfav);
			ivMyhistory.setImageResource(R.drawable.btn_my_myhistory);
			if(m_viewInfoListener != null){
				TitleDef title = getTitleDef();
				title.m_title = "我发布的信息";
				m_viewInfoListener.onTitleChanged(title);
			}
			if(listMyPost.size() != 0){
				adapter.setList(listMyPost);
				adapter.notifyDataSetChanged();
			}
			else{
				if (user != null) {
					mobile = user.getPhone();
					password = user.getPassword();
					pd = ProgressDialog.show(this.getContext(), "提示", "请稍候...");
					pd.setCancelable(true);
					new Thread(new UpdateThread(currentPage)).start();
				} else {
//					bundle.putInt("type", 1);
//					intent.putExtras(bundle);
//					intent.setClass(this, Login.class);
//					startActivity(intent);
//					finish();
				}
			}
		}
		else if(0 == currentPage){
			ivMyads.setImageResource(R.drawable.btn_my_myads);
			ivMyfav.setImageResource(R.drawable.btn_my_myfav_press);
			ivMyhistory.setImageResource(R.drawable.btn_my_myhistory);
			
			if(m_viewInfoListener != null){
				TitleDef title = getTitleDef();
				title.m_rightActionHint = "我的收藏";
				m_viewInfoListener.onTitleChanged(title);
			}
			goodsList = MyApplication.getApplication().getListMyStore();
			adapter.setList(goodsList);
			adapter.notifyDataSetChanged();
		}
		else{
			ivMyads.setImageResource(R.drawable.btn_my_myads);
			ivMyfav.setImageResource(R.drawable.btn_my_myfav);
			ivMyhistory.setImageResource(R.drawable.btn_my_myhistory_press);
			if(m_viewInfoListener != null){
				TitleDef title = getTitleDef();
				title.m_rightActionHint = "我的历史";
				m_viewInfoListener.onTitleChanged(title);
			}
			goodsList = MyApplication.getApplication().getListLookHistory();
			adapter.setList(goodsList);
			adapter.notifyDataSetChanged();
		}
		

		lvGoodsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {			
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				GoodsDetail detail = null;
				if(currentPage == -1){
					detail = listMyPost.get(arg2);
				}
				else if(0 == currentPage || 1 == currentPage){
					detail = goodsList.get(arg2);
				}
				if(null != detail){
					GoodDetailView detailView = new GoodDetailView(detail, getContext(), bundle);
					detailView.setInfoChangeListener(m_viewInfoListener);
					m_viewInfoListener.onNewView(detailView);
				}
			}

		});
		
	}
	
	@Override
	protected void onAttachedToWindow(){
		this.rebuildPage();
		super.onAttachedToWindow();
	}

	private void init(){
		LayoutInflater inflater = LayoutInflater.from(this.getContext());
		View v = inflater.inflate(R.layout.personalcenterview, null);
		this.addView(v);
		user = (UserBean) Util.loadDataFromLocate(this.getContext(), "user");
		try {
			if (Util.JadgeConnection(this.getContext()) == false) {
				Toast.makeText(this.getContext(), "网络连接异常", 3).show();
//				isConnect = 0;
				myHandler.sendEmptyMessage(MCMESSAGE_NETWORKERROR);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		lvGoodsList = (ListView) findViewById(R.id.lvGoodsList);

		ivMyads = (ImageView) findViewById(R.id.ivMyads);
		ivMyfav = (ImageView) findViewById(R.id.ivMyfav);
		ivMyhistory = (ImageView) findViewById(R.id.ivMyhistory);

		lvGoodsList.setDivider(null);
		lvGoodsList.setOnScrollListener(this);

		ivMyads.setOnClickListener(this);
		ivMyfav.setOnClickListener(this);
		ivMyhistory.setOnClickListener(this);
		adapter = new GoodsListAdapter(this.getContext(), this.listMyPost);
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
					details = MyApplication.getApplication().getListMyStore();
					msgToSend = MCMESSAGE_MYFAV_UPDATE_SUCCESS;
					msgToSendOnFail = MCMESSAGE_MYFAV_UPDATE_FAIL;
				}
				else if(currentPage == 1){
					details = MyApplication.getApplication().getListLookHistory();
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
					Toast.makeText(PersonalCenterView.this.getContext(), "您尚未发布信息，", 0).show();
				} else {
					listMyPost = gl.getData();
					MyApplication.getApplication().setListMyPost(listMyPost);
					rebuildPage();
				}
				break;
			case MCMESSAGE_MYFAV_UPDATE_SUCCESS:
				if (pd != null) {
					pd.dismiss();
				}
				GoodsList glFav = JsonUtil.getGoodsListFromJson(json); 
				if (glFav != null && glFav.getCount() > 0) {
					MyApplication.getApplication().setListMyStore(glFav.getData());
					rebuildPage();
				}
				break;
			case MCMESSAGE_MYHISTORY_UPDATE_SUCCESS:
				if (pd != null) {
					pd.dismiss();
				}
				GoodsList glHistory = JsonUtil.getGoodsListFromJson(json); 
				if (glHistory != null && glHistory.getCount() > 0) {
					MyApplication.getApplication().setListLookHistory(glHistory.getData());
					rebuildPage();
				}				
				break;
			case MCMESSAGE_MYPOST_FAIL:
			case MCMESSAGE_MYFAV_UPDATE_FAIL:
			case MCMESSAGE_MYHISTORY_UPDATE_FAIL:				
				if (pd != null) {
					pd.dismiss();
				}
				Toast.makeText(PersonalCenterView.this.getContext(), "未获取到数据", 3).show();
				break;
			case MCMESSAGE_DELETE:
				int pos = msg.arg2;
				if(PersonalCenterView.this.currentPage == -1){
					new Thread(new MyMessageDeleteThread(pos)).start();
				}
				else if(0 == PersonalCenterView.this.currentPage){
					goodsList.remove(pos);
					MyApplication.getApplication().setListMyStore(goodsList);
					Helper.saveDataToLocate(PersonalCenterView.this.getContext(), "listMyStore", goodsList);
					adapter.setList(goodsList);
					adapter.notifyDataSetChanged();
				}
				else if(1 == PersonalCenterView.this.currentPage){
					goodsList.remove(pos);
					MyApplication.getApplication().setListLookHistory(goodsList);
					Helper.saveDataToLocate(PersonalCenterView.this.getContext(), "listLookHistory", goodsList);
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
						MyApplication.getApplication().setListMyPost(listMyPost);
						adapter.setList(listMyPost);
						adapter.notifyDataSetChanged();
						Toast.makeText(PersonalCenterView.this.getContext(), message, 0).show();
					} else {
						// 删除失败
						Toast.makeText(PersonalCenterView.this.getContext(), "删除失败,请稍后重试！", 0).show();
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case MCMESSAGE_DELETE_FAIL:
				Toast.makeText(PersonalCenterView.this.getContext(), "删除失败，请稍后重试！", 0).show();
				break;
			case MCMESSAGE_NETWORKERROR:
				if (pd != null) {
					pd.dismiss();
				}
				Toast.makeText(PersonalCenterView.this.getContext(), "网络连接失败，请检查设置！", 3).show();
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
	public boolean onLeftActionPressed(){
		pd = ProgressDialog.show(this.getContext(), "提示", "请稍候...");
		pd.setCancelable(true);
		new Thread(new UpdateThread(currentPage)).start();
		return true;
	}

	@Override
	public boolean onRightActionPressed(){
		if(-1 == buttonStatus){
//		btnEdit.setBackgroundResource(R.drawable.btn_clearall);
			if(this.m_viewInfoListener != null){
				TitleDef title = getTitleDef();
				title.m_rightActionHint = "完成";
				m_viewInfoListener.onTitleChanged(title);
			}
			if(adapter != null){
				adapter.setHasDelBtn(true);
			}
			buttonStatus = 0;
		}
		else{
//			btnEdit.setBackgroundResource(R.drawable.btn_search);
			if(this.m_viewInfoListener != null){
				TitleDef title = getTitleDef();
				title.m_rightActionHint = "编辑";
				m_viewInfoListener.onTitleChanged(title);
			}
			adapter.setHasDelBtn(false);
			buttonStatus = -1;
		}
		if(adapter != null)
		{
			adapter.notifyDataSetChanged();
		}		
		return true;
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
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
		default:
			break;
		}
//		super.onClick(v);
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
//			LoadImage.doTask();
		}
		
	}
	@Override
	public TitleDef getTitleDef(){
		TitleDef title = new TitleDef();
		title.m_leftActionHint = "更新";
		title.m_rightActionHint = "编辑";
		title.m_title = "个人中心";
		title.m_visible = true;
		return title;
	}
	
	@Override
	public TabDef getTabDef(){
		TabDef tab = new TabDef();
		tab.m_visible = true;
		tab.m_tabSelected = ETAB_TYPE.ETAB_TYPE_MINE;
		return tab;
	}
	
}
