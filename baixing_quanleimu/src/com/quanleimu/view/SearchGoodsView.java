package com.quanleimu.view;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.quanleimu.activity.QuanleimuApplication;
import com.quanleimu.activity.R;
import com.quanleimu.entity.GoodsDetail;
import com.quanleimu.entity.GoodsList;
import com.quanleimu.imageCache.SimpleImageLoader;
import com.quanleimu.jsonutil.JsonUtil;
import com.quanleimu.util.Communication;
import com.quanleimu.util.GoodsListLoader;
import com.quanleimu.widget.PullToRefreshListView;
import com.quanleimu.adapter.GoodsListAdapter;
public class SearchGoodsView extends BaseView implements OnScrollListener, PullToRefreshListView.OnRefreshListener, PullToRefreshListView.OnGetmoreListener {

	// 定义控件
	public Button btnSearch, btnBack;
	public PullToRefreshListView lvSearchResult;
	public Button btnMore;

	public ProgressDialog pd;
	public String title = "";
	/**
	 * 设置布局显示目标最大化
	 */
	public LayoutParams FFlayoutParams = new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.FILL_PARENT,
			LinearLayout.LayoutParams.FILL_PARENT);

	// 定义变量
	public String searchContent = "";
	public String act_type = "";

//	public List<GoodsDetail> listSearchGoods = new ArrayList<GoodsDetail>();
//	public List<GoodsDetail> listCommonSearchGoods = new ArrayList<GoodsDetail>();
//	public GoodsList goodsList = new GoodsList();
	
	private GoodsListLoader mListLoader = null;

	public String fields = "";

	public GoodsListAdapter adapter;
	//public int totalCount = -1;
	
	private String backPageName = "";
	private ProgressBar progressBar;
	
	protected void Init(){
		LayoutInflater inflater = LayoutInflater.from(getContext());
		this.addView(inflater.inflate(R.layout.searchgoods, null));
		
		// 参数 用来过滤
		fields = "";//"mobile,id,link,title,description,date,areaNames,categoryEnglishName,lat,lng,images_big,images_resize180,metaData";
		String url = "query="
				+ Communication.urlEncode(URLEncoder
						.encode("cityEnglishName:"
								+ QuanleimuApplication.getApplication().getCityEnglishName() + " AND "
								+ searchContent));
        mListLoader = new GoodsListLoader(url, myHandler, fields, null);
        
		LayoutParams WClayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		
		// findViewById
		lvSearchResult = (PullToRefreshListView) findViewById(R.id.lvSearchResult);
		lvSearchResult.setOnRefreshListener(this);
		lvSearchResult.setOnGetMoreListener(this);
		
		//线性布局  
        LinearLayout layout = new LinearLayout(getContext());  
        //设置布局 水平方向  
        layout.setOrientation(LinearLayout.HORIZONTAL);  
         //进度条  
        progressBar = new ProgressBar(getContext(), null, android.R.attr.progressBarStyleSmall);
         //进度条显示位置  
        progressBar.setVisibility(View.GONE);
        
        layout.addView(progressBar, WClayoutParams);  
        layout.setGravity(Gravity.CENTER);  
        
		lvSearchResult.setOnScrollListener(this);

		lvSearchResult
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						int index = arg2 - lvSearchResult.getHeaderViewsCount();
						if(index < 0 || index > mListLoader.getGoodsList().getData().size() - 1)
							return;

						if(null != m_viewInfoListener){
							Bundle bundle = new Bundle();
							bundle.putString("backPageName", title);
							bundle.putString("detail_type", "searchgoods");
							bundle.putInt("detail_pos", arg2);
							m_viewInfoListener.onNewView(new GoodDetailView(getContext(), bundle, mListLoader, index));
						}
					}
				});

		pd = ProgressDialog.show(getContext(), "提示", "请稍后...");
		pd.setCancelable(true);
		
		mListLoader.startFetching(true);
	}
	
	public SearchGoodsView(Context context, Bundle bundle){
		super(context, bundle);
		
		backPageName = bundle.getString("backPageName");
		searchContent = bundle.getString("searchContent");
		act_type = bundle.getString("actType");
		if(act_type.equals("search")){
			title = searchContent;
		}
		else{
			title = bundle.getString("name");
		}
		
		Init();
	}

//	@Override
//	public boolean onBack(){
//		m_viewInfoListener.onPopView(SearchView.class.getName());
//		return false;
//	}
//	
//	@Override
//	public boolean onLeftActionPressed(){
//		return onBack();
//	}
	
	@Override
	public void onResume(){
		for(int i = 0; i < lvSearchResult.getChildCount(); ++i){
			ImageView imageView = (ImageView)lvSearchResult.getChildAt(i).findViewById(R.id.ivInfo);
			
			if(	null != imageView	
					&& null != imageView.getTag() && imageView.getTag().toString().length() > 0
					/*&& null != imageView.getDrawable()
					&& imageView.getDrawable() instanceof AnimationDrawable*/){
				SimpleImageLoader.showImg(imageView, imageView.getTag().toString(), getContext());
			}
		}
		
		lvSearchResult.setSelection(mListLoader.getSelection());
	}	
	
	@Override
	public boolean onRightActionPressed(){
		m_viewInfoListener.onExit(this);
		m_viewInfoListener.onNewView(new SearchView(getContext(), new Bundle()));
		return true;
	}//called when right button on title bar pressed, return true if handled already, false otherwise
	
	@Override
	public TitleDef getTitleDef(){
		TitleDef title = new TitleDef();
		title.m_visible = true;
		title.m_leftActionHint = backPageName;
		title.m_title = this.title;
		title.m_rightActionHint = "重新搜索";
		return title;
	}
	
	@Override
	public TabDef getTabDef(){
		TabDef tab = new TabDef();
		tab.m_visible = true;
		return tab;
	}

	// 管理线程的Handler
	Handler myHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case GoodsListLoader.MSG_FINISH_GET_FIRST:
				GoodsList goodsList = JsonUtil.getGoodsListFromJson(mListLoader.getLastJson());

				if (goodsList == null || goodsList.getCount() == 0) {
					if (pd != null) {
						pd.dismiss();
					}
					Toast.makeText(getContext(), "没有符合条件的结果，请重新输入！", 3).show();
				} else {
					List<GoodsDetail> listSearchGoods = goodsList.getData();

					QuanleimuApplication.getApplication().setSearchCount(listSearchGoods.size());

					QuanleimuApplication.getApplication().setListSearchGoods(listSearchGoods);

					adapter = new GoodsListAdapter(getContext(), listSearchGoods);
					adapter.setHasDelBtn(false);
					lvSearchResult.setAdapter(adapter);
					if (pd != null) {
						pd.dismiss();
					}
					
					mListLoader.setGoodsList(goodsList);
					mListLoader.setHasMore(true);
					
					lvSearchResult.onRefreshComplete();
				}

				break;
			case GoodsListLoader.MSG_NO_MORE:
				if (pd != null) {
					pd.dismiss();
				}
				progressBar.setVisibility(View.GONE);
				Toast.makeText(getContext(), "没有符合条件的结果，请重新输入！", 3).show();
				
				mListLoader.setHasMore(false);
				break;
				
			case GoodsListLoader.MSG_FINISH_GET_MORE:
				if (pd != null) {
					pd.dismiss();
				}
				progressBar.setVisibility(View.GONE);
				
				GoodsList goodsListMore = JsonUtil.getGoodsListFromJson(mListLoader.getLastJson());

				if (goodsListMore == null || goodsListMore.getData().size() == 0) {
					//Toast.makeText(getContext(), "没有符合条件的结果，请重新输入！", 3).show();
					
					mListLoader.setHasMore(false);
					lvSearchResult.onGetMoreCompleted(PullToRefreshListView.E_GETMORE.E_GETMORE_NO_MORE);
				} else {
					List<GoodsDetail> listCommonSearchGoods = goodsListMore.getData();
					for (int i = 0; i < listCommonSearchGoods.size(); i++) {
						mListLoader.getGoodsList().getData().add(listCommonSearchGoods.get(i));
					}
					QuanleimuApplication.getApplication().setListSearchGoods(mListLoader.getGoodsList().getData());

					adapter.setList(mListLoader.getGoodsList().getData());
					adapter.notifyDataSetChanged();	
					
					mListLoader.setHasMore(true);
					
					lvSearchResult.onGetMoreCompleted(PullToRefreshListView.E_GETMORE.E_GETMORE_OK);
				}
				break;
			case 10:
				if (pd != null) {
					pd.dismiss();
				}
				progressBar.setVisibility(View.GONE);

				Toast.makeText(getContext(), "网络连接失败，请检查设置！", 3).show();
				break;
			}

			super.handleMessage(msg);
		}
	};

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
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
		mListLoader.startFetching(false);
	}

	@Override
	public void onRefresh() {
		mListLoader.startFetching(true);
	}
}
