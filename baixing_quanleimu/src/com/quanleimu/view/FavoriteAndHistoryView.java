package com.quanleimu.view;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import com.quanleimu.activity.BaseActivity;
import com.quanleimu.activity.QuanleimuApplication;
import com.quanleimu.activity.R;
import com.quanleimu.adapter.GoodsListAdapter;
import com.quanleimu.entity.CityDetail;
import com.quanleimu.entity.GoodsDetail;
import com.quanleimu.entity.GoodsList;
import com.quanleimu.jsonutil.JsonUtil;
import com.quanleimu.jsonutil.LocateJsonData;
import com.quanleimu.util.Communication;
import com.quanleimu.util.GoodsListLoader;
import com.quanleimu.util.Helper;
import com.quanleimu.view.BaseView.TitleDef;
import com.quanleimu.view.PersonalCenterView.MyMessageDeleteThread;
import com.quanleimu.view.PersonalCenterView.UpdateAndGetmoreThread;
import com.quanleimu.widget.PullToRefreshListView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class FavoriteAndHistoryView extends BaseView implements PullToRefreshListView.OnRefreshListener{
	private boolean isFav = false;
	private String json = null;
	static final int MSG_UPDATEFAV = 1;
	static final int MSG_UPDATEHISTORY = 2;
	static final int MSG_DELETEAD = 3;
	static final int MSG_DELETEALL = 4;
	private GoodsListAdapter adapter = new GoodsListAdapter(this.getContext(), null);
	private PullToRefreshListView pullListView = null;
	private Bundle bundle = null;
	private int buttonStatus = -1;//-1:edit 0:finish
	private GoodsListLoader glLoader = null;
	public FavoriteAndHistoryView(Context context, Bundle bundle, boolean isFav){
		super(context);
		this.isFav = isFav;
		this.bundle = bundle;
		Init();
	}
	protected void Init() {
		LayoutInflater inflater = LayoutInflater.from(getContext());
		this.addView(inflater.inflate(R.layout.personallistview, null));
		pullListView = (PullToRefreshListView)this.findViewById(R.id.plvlist);
		pullListView.setOnRefreshListener(this);
		pullListView.setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
				position = position - pullListView.getHeaderViewsCount();
				if(position < 0 || (isFav && position >= QuanleimuApplication.getApplication().getListMyStore().size())
						|| (!isFav && position >= QuanleimuApplication.getApplication().getListLookHistory().size())) return;
//				GoodsDetail detail = isFav ? QuanleimuApplication.getApplication().getListMyStore().get(position)
//						: QuanleimuApplication.getApplication().getListLookHistory().get(position);
				m_viewInfoListener.onNewView(new GoodDetailView(FavoriteAndHistoryView.this.getContext(), bundle, glLoader, position));
			}
			
		});
		adapter.setMessageOutOnDelete(myHandler, MSG_DELETEAD);
		glLoader = new GoodsListLoader(null, myHandler, null, null);
	}
	
	@Override
	public void onAttachedToWindow(){			
		adapter.setList(isFav ? 
				QuanleimuApplication.getApplication().getListMyStore() : QuanleimuApplication.getApplication().getListLookHistory());
		GoodsList gl = new GoodsList();
		gl.setData(adapter.getList());
		glLoader.setGoodsList(gl);
		((ListView)this.findViewById(R.id.plvlist)).setAdapter(adapter);
	}
	
	@Override
	public boolean onRightActionPressed(){
		if(-1 == buttonStatus){
			if(this.m_viewInfoListener != null){
				TitleDef title = getTitleDef();
				title.m_rightActionHint = "完成";
				title.m_leftActionHint = "清空";
				m_viewInfoListener.onTitleChanged(title);
			}
			if(adapter != null){
				adapter.setHasDelBtn(true);
			}
			buttonStatus = 0;
		}
		else{
			if(this.m_viewInfoListener != null){
				TitleDef title = getTitleDef();
				title.m_rightActionHint = "编辑";
				m_viewInfoListener.onTitleChanged(title);
			}
			adapter.setHasDelBtn(false);
			buttonStatus = -1;
		}
		adapter.notifyDataSetChanged();
		return true;
	}

	@Override
	public boolean onLeftActionPressed() {
		if(0 == buttonStatus){
			myHandler.sendEmptyMessage(MSG_DELETEALL);
			return true;
		}
		else{
			return onBack();
		}
	}

	@Override
	public TitleDef getTitleDef() {
		TitleDef title = new TitleDef();
		title.m_visible = true;
		title.m_leftActionHint = "返回";
		title.m_title = isFav ? "收藏得信息" : "浏览历史";
		title.m_rightActionHint = "编辑";
		return title;
	}

	@Override
	public TabDef getTabDef() {
		TabDef tab = new TabDef();
		tab.m_visible = false;
		return tab;
	}
	
	Handler myHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_UPDATEFAV:
				if (pd != null) {
					pd.dismiss();
				}
				if (json != null) {
					GoodsList gl = JsonUtil.getGoodsListFromJson(json);
					QuanleimuApplication.getApplication().setListMyStore(gl.getData());
					adapter.setList(gl.getData());
				}
				break;
			case MSG_UPDATEHISTORY:
				if(pd != null){
					pd.dismiss();
				}
				if(json != null){
					GoodsList gl = JsonUtil.getGoodsListFromJson(json);
					QuanleimuApplication.getApplication().setListLookHistory(gl.getData());
					adapter.setList(gl.getData());					
				}
				break;
			case MSG_DELETEAD:
				int pos = msg.arg2;
				if(isFav){
					List<GoodsDetail> goodsList = QuanleimuApplication.getApplication().getListMyStore();
					goodsList.remove(pos);
					QuanleimuApplication.getApplication().setListMyStore(goodsList);
					Helper.saveDataToLocate(FavoriteAndHistoryView.this.getContext(), "listMyStore", goodsList);
					adapter.setList(goodsList);
					adapter.notifyDataSetChanged();
					adapter.setUiHold(false);
				}
				else{
					List<GoodsDetail> goodsList = QuanleimuApplication.getApplication().getListLookHistory();
					goodsList.remove(pos);
					QuanleimuApplication.getApplication().setListLookHistory(goodsList);
					Helper.saveDataToLocate(FavoriteAndHistoryView.this.getContext(), "listLookHistory", goodsList);
					adapter.setList(goodsList);
					adapter.notifyDataSetChanged();			
					adapter.setUiHold(false);
				}				
				break;
			case MSG_DELETEALL:
				if(isFav){
					QuanleimuApplication.getApplication().setListMyStore(new ArrayList<GoodsDetail>());
					Helper.saveDataToLocate(FavoriteAndHistoryView.this.getContext(), "listMyStore", new ArrayList<GoodsDetail>());
				}
				else{
					QuanleimuApplication.getApplication().setListLookHistory(new ArrayList<GoodsDetail>());
					Helper.saveDataToLocate(FavoriteAndHistoryView.this.getContext(), "listLookHistory", new ArrayList<GoodsDetail>());
				}
				adapter.setList(new ArrayList<GoodsDetail>());
				adapter.notifyDataSetChanged();
				
				if(FavoriteAndHistoryView.this.m_viewInfoListener != null){
					TitleDef title = getTitleDef();
					title.m_rightActionHint = "编辑";
					title.m_leftActionHint = "设置";
					m_viewInfoListener.onTitleChanged(title);
				}
				adapter.setHasDelBtn(false);
				buttonStatus = -1;
				break;
			}
		}
	};
	
	class UpdateAdsThread implements Runnable{
		private boolean isFav = false;
		public UpdateAdsThread(boolean isFav){
			this.isFav = isFav;
		}
		@Override
		public void run(){
			String apiName = "ad_list";
			ArrayList<String> list = new ArrayList<String>();
			List<GoodsDetail> details = isFav ? QuanleimuApplication.getApplication().getListMyStore() : 
				QuanleimuApplication.getApplication().getListLookHistory();
			list.add("start=0");
			if (details != null && details.size() > 0) {
				String ids = "id:" + details.get(0).getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID);
				for (int i = 1; i < details.size(); ++i) {
					ids += " OR " + "id:" + details.get(i).getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID);
				}
				list.add("query=(" + ids + ")");
			}
			list.add("rt=1");
			list.add("rows=30");

			String url = Communication.getApiUrl(apiName, list);
			try {
				json = Communication.getDataByUrl(url);
				myHandler.sendEmptyMessage(isFav ? MSG_UPDATEFAV : MSG_UPDATEHISTORY);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Communication.BXHttpException e) {

			}
			((BaseActivity)FavoriteAndHistoryView.this.getContext()).runOnUiThread(new Runnable(){
				@Override
				public void run()
				{
					pullListView.onRefreshComplete();
				}
			});
			
		}
	}
	
	@Override
	public void onRefresh() {
		if((isFav && QuanleimuApplication.getApplication().getListMyStore() != null 
				&& QuanleimuApplication.getApplication().getListMyStore().size() > 0)
			|| (!isFav && QuanleimuApplication.getApplication().getListLookHistory() != null
				&& QuanleimuApplication.getApplication().getListLookHistory().size() > 0)){
			new Thread(new UpdateAdsThread(isFav)).start();		
		}
		else{
			this.pullListView.onRefreshComplete();
		}
	}
}
