package com.quanleimu.view;

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
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.quanleimu.activity.QuanleimuApplication;
import com.quanleimu.activity.R;
import com.quanleimu.entity.GoodsDetail;
import com.quanleimu.entity.GoodsList;
import com.quanleimu.imageCache.SimpleImageLoader;
import com.quanleimu.jsonutil.JsonUtil;
import com.quanleimu.util.Communication;
import com.quanleimu.util.Helper;
import com.quanleimu.util.Util;
import com.quanleimu.view.GetGoodsView.GetGoodsListThread;
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

//	public GoodsListAdapter commonAdapter = null;
	public List<GoodsDetail> listSearchGoods = new ArrayList<GoodsDetail>();
	public List<GoodsDetail> listCommonSearchGoods = new ArrayList<GoodsDetail>();
	public GoodsList goodsList = new GoodsList();

	public String json = "";
	public String fields = "";
	public int startRow = 0;

	public int isFirst = 0;
	public GoodsListAdapter adapter;
	public int totalCount = -1;
	
	private String backPageName = "";
	private ProgressBar progressBar;
	private TextView tvAddMore;
	
	protected void Init(){
		LayoutInflater inflater = LayoutInflater.from(getContext());
		this.addView(inflater.inflate(R.layout.searchgoods, null));
		
		// 参数 用来过滤
		fields = "mobile,id,link,title,description,date,areaNames,categoryEnglishName,lat,lng,images_big,images_resize180,metaData";
		
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
        
        tvAddMore = new TextView(getContext());  
        tvAddMore.setTextSize(18);
        tvAddMore.setText("更多...");  
        tvAddMore.setGravity(Gravity.CENTER_VERTICAL);  
        layout.addView(tvAddMore, WClayoutParams);  
        layout.setGravity(Gravity.CENTER);  
        
        tvAddMore.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				progressBar.setVisibility(View.VISIBLE);
				tvAddMore.setText("加载中...");
				
				//点击获取更多 按钮布局消失
				isFirst = -1;
				startRow = listSearchGoods.size();
				new Thread(new GetGoodsListThread()).start();
			}
		});

		listSearchGoods = QuanleimuApplication.getApplication().getListSearchGoods();
		totalCount = QuanleimuApplication.getApplication().getSearchCount();

		lvSearchResult.setOnScrollListener(this);

		lvSearchResult
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						int index = arg2 - lvSearchResult.getHeaderViewsCount();
						if(index < 0 || index > listSearchGoods.size() - 1)
							return;

						if(null != m_viewInfoListener){
							Bundle bundle = new Bundle();
//							bundle.putString("backPageName", title);
//							bundle.putString("detail_type", "searchgoods");
//							bundle.putInt("detail_pos", arg2);
							m_viewInfoListener.onNewView(new GoodDetailView(listSearchGoods.get(index), getContext(), bundle));
						}
					}
				});

		pd = ProgressDialog.show(getContext(), "提示", "请稍后...");
		pd.setCancelable(true);
		new Thread(new GetGoodsListThread()).start();

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
			case 1://new
				goodsList = JsonUtil.getGoodsListFromJson(json);
				totalCount = goodsList.getCount();

				if (goodsList == null || goodsList.getCount() == 0) {
					if (pd != null) {
						pd.dismiss();
					}
					Toast.makeText(getContext(), "没有符合条件的结果，请重新输入！", 3).show();
				} else {
					listSearchGoods = goodsList.getData();

					QuanleimuApplication.getApplication().setSearchCount(totalCount);

					QuanleimuApplication.getApplication().setListSearchGoods(listSearchGoods);

					adapter = new GoodsListAdapter(getContext(), listSearchGoods);
					adapter.setHasDelBtn(false);
					lvSearchResult.setAdapter(adapter);
					if (pd != null) {
						pd.dismiss();
					}
					
					lvSearchResult.onRefreshComplete();
				}

				break;
			case 2:
				if (pd != null) {
					pd.dismiss();
				}
				progressBar.setVisibility(View.GONE);
				tvAddMore.setText("更多...");
				Toast.makeText(getContext(), "没有符合条件的结果，请重新输入！", 3).show();
				break;
				
			case 3://more
				if (pd != null) {
					pd.dismiss();
				}
				progressBar.setVisibility(View.GONE);
				tvAddMore.setText("更多...");
				goodsList = JsonUtil.getGoodsListFromJson(json);

				if (goodsList == null || goodsList.getCount() == 0) {
					Toast.makeText(getContext(), "没有符合条件的结果，请重新输入！", 3).show();
					
					lvSearchResult.onGetMoreCompleted(PullToRefreshListView.E_GETMORE.E_GETMORE_NO_MORE);
				} else {
					listCommonSearchGoods = goodsList.getData();
					for (int i = 0; i < listCommonSearchGoods.size(); i++) {
						listSearchGoods.add(listCommonSearchGoods.get(i));
					}
					QuanleimuApplication.getApplication().setListSearchGoods(listSearchGoods);

					adapter.setList(listSearchGoods);
					adapter.notifyDataSetChanged();	
					
					lvSearchResult.onGetMoreCompleted(PullToRefreshListView.E_GETMORE.E_GETMORE_OK);
				}
				break;
			case 10:
				if (pd != null) {
					pd.dismiss();
				}
				progressBar.setVisibility(View.GONE);
				tvAddMore.setText("更多...");
				Toast.makeText(getContext(), "网络连接失败，请检查设置！", 3).show();
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
			list.add("query="
					+ Communication.urlEncode(URLEncoder
							.encode("cityEnglishName:"
									+ QuanleimuApplication.getApplication().getCityEnglishName() + " AND "
									+ searchContent)));
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
			} catch (IOException e) {
				myHandler.sendEmptyMessage(10);
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
		// TODO Auto-generated method stub
		isFirst = -1;
		startRow = listSearchGoods.size();
		new Thread(new GetGoodsListThread()).start();		
	}

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub
		isFirst = 0;
		startRow = 0;
		new Thread(new GetGoodsListThread()).start();	
	}
}
