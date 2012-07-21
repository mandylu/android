package com.quanleimu.view;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import com.quanleimu.entity.GoodsDetail;
import com.quanleimu.entity.GoodsList;
import com.quanleimu.imageCache.SimpleImageLoader;
import com.quanleimu.jsonutil.JsonUtil;
import com.quanleimu.util.ErrorHandler;
import com.quanleimu.util.GoodsListLoader;
import com.quanleimu.activity.QuanleimuApplication;
import com.quanleimu.activity.R;
import com.quanleimu.adapter.GoodsListAdapter;
import com.quanleimu.view.BaseView;
import com.quanleimu.widget.PullToRefreshListView;

public class GetGoodsView extends BaseView implements OnScrollListener, PullToRefreshListView.OnRefreshListener, PullToRefreshListView.OnGetmoreListener{

	private PullToRefreshListView lvGoodsList;
	private ProgressDialog pd;
	private ProgressBar progressBar;

	private String categoryEnglishName = "";
	private String siftResult = "";

	private GoodsListAdapter adapter;
	private final static int ERROR_FIRST = 0;
	private final static int ERROR_MORE = 1;
	private final static int ERROR_NOMORE = 2;

	Bundle bundle;
	
	private GoodsListLoader goodsListLoader = null;
	
	
	
	@Override
	public void onResume(){
		this.lvGoodsList.requestFocus();//force cate view has focus
		
		for(int i = 0; i < lvGoodsList.getChildCount(); ++i){
			ImageView imageView = (ImageView)lvGoodsList.getChildAt(i).findViewById(R.id.ivInfo);
			
			if(	null != imageView	
					&& null != imageView.getTag() && imageView.getTag().toString().length() > 0
					/*&& null != imageView.getDrawable()
					&& imageView.getDrawable() instanceof AnimationDrawable*/){
				SimpleImageLoader.showImg(imageView, imageView.getTag().toString(), getContext());
			}
		}
		
		lvGoodsList.setSelection(goodsListLoader.getSelection());
		goodsListLoader.setHandler(myHandler);
	}
	
	@Override
	public void onPause(){
		super.onPause();
		
		for(int i = 0; i < lvGoodsList.getChildCount(); ++i){
			ImageView imageView = (ImageView)lvGoodsList.getChildAt(i).findViewById(R.id.ivInfo);
			
			if(	null != imageView	
					&& null != imageView.getTag() && imageView.getTag().toString().length() > 0
					/*&& null != imageView.getDrawable()
					&& imageView.getDrawable() instanceof AnimationDrawable*/){
				SimpleImageLoader.Cancel(imageView.getTag().toString(), imageView);
			}
		}
	}
	
		
	public GetGoodsView(Context context, Bundle bundle, String categoryEnglishName){
		super(context, bundle);
		this.categoryEnglishName = categoryEnglishName;

		this.bundle = bundle;
//		Debug.startMethodTracing();
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
			m_viewInfoListener.onNewView(new SiftView(getContext(), bundle));
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

		lvGoodsList = (PullToRefreshListView) findViewById(R.id.lvGoodsList);
		lvGoodsList.setOnRefreshListener(this);
		lvGoodsList.setOnGetMoreListener(this);

        LinearLayout layout = new LinearLayout(this.getContext());  
        layout.setOrientation(LinearLayout.HORIZONTAL);  
        progressBar = new ProgressBar(this.getContext(), null, android.R.attr.progressBarStyleSmall);
         //进度条显示位置  
        progressBar.setVisibility(View.GONE);
        
        LayoutParams WClayoutParams =
        		new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        layout.addView(progressBar, WClayoutParams);  
        layout.setGravity(Gravity.CENTER);  
		
		lvGoodsList.setOnScrollListener(this);

		pd = ProgressDialog.show(this.getContext(), "提示", "请稍候...");
		pd.setCancelable(true);

		List<String> params = new ArrayList<String>();
		if (siftResult != null && !siftResult.equals("")) {
			params.add("query="
					+ "cityEnglishName:"+QuanleimuApplication.getApplication().getCityEnglishName()+" AND categoryEnglishName:"
					+ categoryEnglishName + " " + siftResult);
		} else {
			params.add("query="
					+ "cityEnglishName:"+QuanleimuApplication.getApplication().getCityEnglishName()+" AND categoryEnglishName:"
					+ categoryEnglishName + " AND status:0");
		}
		
		goodsListLoader = new GoodsListLoader(params, myHandler, null, new GoodsList());
		goodsListLoader.startFetching(true);
		
		lvGoodsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				
				int index = arg2 - lvGoodsList.getHeaderViewsCount();
				if(index < 0 || index > goodsListLoader.getGoodsList().getData().size() - 1)
					return;

				if(GetGoodsView.this.m_viewInfoListener != null){
					bundle.putSerializable("currentGoodsDetail", goodsListLoader.getGoodsList().getData().get(index));
					bundle.putString("detail_type", "getgoods");
					m_viewInfoListener.onNewView(new GoodDetailView(getContext(), bundle, goodsListLoader, index, null));
				}				
			}
		});
	}

		// 管理线程的Handler
	Handler myHandler = new Handler() {
		
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case GoodsListLoader.MSG_FINISH_GET_FIRST:				 
				GoodsList goodsList = JsonUtil.getGoodsListFromJson(goodsListLoader.getLastJson());
				goodsListLoader.setGoodsList(goodsList);
				if (goodsList == null || goodsListLoader.getGoodsList().getCount() == 0) {
					Message msg1 = Message.obtain();
					msg1.what = ErrorHandler.ERROR_COMMON_FAILURE;
					Bundle bundle = new Bundle();
					bundle.putString("popup_message", "没有符合的结果，请更改条件并重试！");
					msg1.setData(bundle);
					QuanleimuApplication.getApplication().getErrorHandler().sendMessage(msg1);
				} else {
					QuanleimuApplication.getApplication().setListGoods(goodsListLoader.getGoodsList().getData());
					
					adapter = new GoodsListAdapter(GetGoodsView.this.getContext(), goodsListLoader.getGoodsList().getData());
					lvGoodsList.setAdapter(adapter);
				}
				
				lvGoodsList.onRefreshComplete();
				
				break;
			case GoodsListLoader.MSG_NO_MORE:
				progressBar.setVisibility(View.GONE);
				
				Message msg1 = Message.obtain();
				msg1.what = ErrorHandler.ERROR_COMMON_FAILURE;
				Bundle bundle = new Bundle();
				bundle.putString("popup_message", "数据下载失败，请重试！");
				msg1.setData(bundle);
				QuanleimuApplication.getApplication().getErrorHandler().sendMessage(msg1);
				
				lvGoodsList.onGetMoreCompleted(PullToRefreshListView.E_GETMORE.E_GETMORE_NO_MORE);
				
				break;
			case GoodsListLoader.MSG_FINISH_GET_MORE:
				progressBar.setVisibility(View.GONE);
				
				GoodsList moreGoodsList = JsonUtil.getGoodsListFromJson(goodsListLoader.getLastJson());
				if (moreGoodsList == null || moreGoodsList.getCount() == 0) {
					Message msg2 = Message.obtain();
					msg2.what = ErrorHandler.ERROR_COMMON_WARNING;
					Bundle bundle1 = new Bundle();
					bundle1.putString("popup_message", "没有更多啦！");
					msg2.setData(bundle1);
					QuanleimuApplication.getApplication().getErrorHandler().sendMessage(msg2);
				} else {
					List<GoodsDetail> listCommonGoods =  moreGoodsList.getData();
					for(int i=0;i<listCommonGoods.size();i++)
					{
						goodsListLoader.getGoodsList().getData().add(listCommonGoods.get(i));
					}
					QuanleimuApplication.getApplication().setListGoods(goodsListLoader.getGoodsList().getData());
					
					adapter.setList(goodsListLoader.getGoodsList().getData());
					adapter.notifyDataSetChanged();					
				}
				
				lvGoodsList.onGetMoreCompleted(PullToRefreshListView.E_GETMORE.E_GETMORE_OK);
				
				break;
			case ErrorHandler.ERROR_NETWORK_UNAVAILABLE:
				progressBar.setVisibility(View.GONE);

				Message msg2 = Message.obtain();
				msg2.what = ErrorHandler.ERROR_NETWORK_UNAVAILABLE;
				QuanleimuApplication.getApplication().getErrorHandler().sendMessage(msg2);
				break;
			}
			
			if (pd != null) {
				pd.dismiss();
			}
			
			super.handleMessage(msg);
		}
	};

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		//Log.d("GetGoodsView: ", "on scroll called!");
	}
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if(scrollState == SCROLL_STATE_IDLE)
		{
			ArrayList<String> urls = new ArrayList<String>();
			for(int index = 0; index < view.getChildCount(); ++index){
				View curView = view.getChildAt(+index);
				if(null != curView){
					View curIv = curView.findViewById(R.id.ivInfo);
					
					if(null != curIv && null != curIv.getTag())	urls.add(curIv.getTag().toString());
				}			
			}
			
			SimpleImageLoader.AdjustPriority(urls);			
		}		
	}

	@Override
	public void onGetMore() {
		goodsListLoader.startFetching(false);
	}

	@Override
	public void onRefresh() {
		goodsListLoader.startFetching(true);	
	}
}
