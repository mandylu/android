package com.quanleimu.view.fragment;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.quanleimu.activity.BaseFragment;
import com.quanleimu.activity.BaseFragment.TabDef;
import com.quanleimu.activity.BaseFragment.TitleDef;
import com.quanleimu.activity.QuanleimuApplication;
import com.quanleimu.activity.R;
import com.quanleimu.adapter.GoodsListAdapter;
import com.quanleimu.entity.BXLocation;
import com.quanleimu.entity.GoodsDetail;
import com.quanleimu.entity.GoodsList;
import com.quanleimu.imageCache.SimpleImageLoader;
import com.quanleimu.jsonutil.JsonUtil;
import com.quanleimu.util.Communication;
import com.quanleimu.util.ErrorHandler;
import com.quanleimu.util.GoodsListLoader;
import com.quanleimu.widget.PullToRefreshListView;
import com.quanleimu.widget.PullToRefreshListView.E_GETMORE;

public class SearchGoodsFragment extends BaseFragment implements OnScrollListener, View.OnClickListener, PullToRefreshListView.OnRefreshListener, PullToRefreshListView.OnGetmoreListener  {
	
	// 定义控件
	public Button btnSearch, btnBack;
	public PullToRefreshListView lvSearchResult;
	public Button btnMore;

	public String title = "";
	
	private View titleControl = null;
	
	private boolean mRefreshUsingLocal = false;
	
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
	
	private List<String> basicParams = null;
	
	private int titleControlStatus = 1;//0: Left(Recent), 1: Right(Nearby)
	
	private BXLocation curLocation = null;
	
	public void initTitle(TitleDef title){
		title.m_visible = true;
		title.m_leftActionHint = backPageName;
		title.m_title = this.title;
//		title.m_rightActionHint = "重新搜索";
		title.m_rightActionImg = -1;//FIXME:
		
//		if(null == titleControl){
//			LayoutInflater inflater = LayoutInflater.from(this.getActivity());
//			titleControl = inflater.inflate(R.layout.recent_or_nearby, null);
//			titleControl.findViewById(R.id.btnNearby).setOnClickListener(this);
//			titleControl.findViewById(R.id.btnRecent).setOnClickListener(this);
//		}
		title.m_titleControls = titleControl;
	}
	
	@Override
	public void initTab(TabDef tab){
		tab.m_visible = true;
	}
	
	
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.searchgoods, null);
		
		// 参数 用来过滤
		fields = "";//"mobile,id,link,title,description,date,areaNames,categoryEnglishName,lat,lng,images_big,images_resize180,metaData";
		String url = "query="
				+ Communication.urlEncode(URLEncoder
						.encode("cityEnglishName:"
								+ QuanleimuApplication.getApplication().getCityEnglishName() + " AND "
								+ searchContent));
		basicParams = new ArrayList<String>();
		basicParams.add(url);
        mListLoader = new GoodsListLoader(basicParams, handler, fields, null);
        
		LayoutParams WClayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		
		// findViewById
		lvSearchResult = (PullToRefreshListView) v.findViewById(R.id.lvSearchResult);
		lvSearchResult.setOnRefreshListener(this);
		lvSearchResult.setOnGetMoreListener(this);
		
		titleControl = inflater.inflate(R.layout.recent_or_nearby, null);
		titleControl.findViewById(R.id.btnNearby).setOnClickListener(this);
		titleControl.findViewById(R.id.btnRecent).setOnClickListener(this);
		
		//线性布局  
        LinearLayout layout = new LinearLayout(getActivity());  
        //设置布局 水平方向  
        layout.setOrientation(LinearLayout.HORIZONTAL);  
         //进度条  
        progressBar = new ProgressBar(getActivity(), null, android.R.attr.progressBarStyleSmall);
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

//						if(null != m_viewInfoListener){
							Bundle bundle = createArguments(null, title);
							bundle.putSerializable("loader", mListLoader);
							bundle.putInt("index", index);
//							bundle.putString("backPageName", title);
//							bundle.putString("detail_type", "searchgoods");
//							bundle.putInt("detail_pos", arg2);
//							m_viewInfoListener.onNewView(new GoodDetailView(getContext(), bundle, mListLoader, index, null));
							pushFragment(new GoodDetailFragment(), bundle);
//						}
					}
				});
		
		((TextView)v.findViewById(R.id.tvSearchKeyword)).setText(title);

		showSimpleProgress();
		
		curLocation = QuanleimuApplication.getApplication().getCurrentPosition(true);
		if(curLocation == null){
			((Button)titleControl.findViewById(R.id.btnNearby)).setBackgroundResource(R.drawable.bg_nav_seg_left_normal);
			((Button)titleControl.findViewById(R.id.btnRecent)).setBackgroundResource(R.drawable.bg_nav_seg_right_pressed);
			this.titleControlStatus = 0;
		}else{
			basicParams.add("lat="+curLocation.fLat);
			basicParams.add("lng="+curLocation.fLon);
			mListLoader.setNearby(true);
		}
		
		mListLoader.startFetching(true, Communication.E_DATA_POLICY.E_DATA_POLICY_ONLY_LOCAL);
	
		return v;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle = getArguments();
		backPageName = bundle.getString(ARG_COMMON_BACK_HINT);
		searchContent = bundle.getString("searchContent");
		act_type = bundle.getString("actType");
		if(act_type.equals("search")){
			title = searchContent;
		}
		else{
			title = bundle.getString(ARG_COMMON_TITLE);
		}
	}
	
	
	

	@Override
	public void onPause() {
		super.onPause();
		
		for(int i = 0; i < lvSearchResult.getChildCount(); ++i){
			ImageView imageView = (ImageView)lvSearchResult.getChildAt(i).findViewById(R.id.ivInfo);
			
			if(	null != imageView	
					&& null != imageView.getTag() && imageView.getTag().toString().length() > 0
					/*&& null != imageView.getDrawable()
					&& imageView.getDrawable() instanceof AnimationDrawable*/){
				SimpleImageLoader.Cancel(imageView.getTag().toString(), imageView);
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		
		for(int i = 0; i < lvSearchResult.getChildCount(); ++i){
			ImageView imageView = (ImageView)lvSearchResult.getChildAt(i).findViewById(R.id.ivInfo);
			
			if(	null != imageView	
					&& null != imageView.getTag() && imageView.getTag().toString().length() > 0
					/*&& null != imageView.getDrawable()
					&& imageView.getDrawable() instanceof AnimationDrawable*/){
				SimpleImageLoader.showImg(imageView, imageView.getTag().toString(), null, getActivity());
			}
		}
		
		if(null != mListLoader){
			mListLoader.setHasMoreListener(null);
			mListLoader.setHandler(handler);
			if(null != adapter){
				adapter.setList(mListLoader.getGoodsList().getData());
			}
			lvSearchResult.setSelectionFromHeader(mListLoader.getSelection());
		}
	}
	



	@Override
	protected void handleMessage(Message msg, Activity activity, View rootView) {

		switch (msg.what) {
		case GoodsListLoader.MSG_FIRST_FAIL:
			if(GoodsListLoader.E_LISTDATA_STATUS.E_LISTDATA_STATUS_OFFLINE == mListLoader.getRequestDataStatus())
				mListLoader.startFetching(true, Communication.E_DATA_POLICY.E_DATA_POLICY_NETWORK_CACHEABLE);
			else{
				hideProgress();
				Toast.makeText(activity, "没有符合条件的结果，请重新输入！", Toast.LENGTH_LONG).show();
			}
			break;
		case GoodsListLoader.MSG_FINISH_GET_FIRST:
			GoodsList goodsList = JsonUtil.getGoodsListFromJson(mListLoader.getLastJson());

			if (goodsList == null || goodsList.getData().size() == 0) {
				hideProgress();
				Toast.makeText(activity, "没有符合条件的结果，请重新输入！", Toast.LENGTH_LONG).show();
			} else {
				List<GoodsDetail> listSearchGoods = goodsList.getData();

				//QuanleimuApplication.getApplication().setSearchCount(listSearchGoods.size());

				//QuanleimuApplication.getApplication().setListSearchGoods(listSearchGoods);

				adapter = new GoodsListAdapter(activity, listSearchGoods);
				adapter.setHasDelBtn(false);
				lvSearchResult.setAdapter(adapter);
				hideProgress();
				
				mListLoader.setGoodsList(goodsList);
				mListLoader.setHasMore(true);
				
				lvSearchResult.onRefreshComplete();
			}
			
			//if currently using offline data, start fetching online data
			if(GoodsListLoader.E_LISTDATA_STATUS.E_LISTDATA_STATUS_OFFLINE == mListLoader.getDataStatus())
				lvSearchResult.fireRefresh();
				//mListLoader.startFetching(true, Communication.E_DATA_POLICY.E_DATA_POLICY_NETWORK_CACHEABLE);

			break;
		case GoodsListLoader.MSG_NO_MORE:
			hideProgress();
			
			mListLoader.setHasMore(false);
			lvSearchResult.onGetMoreCompleted(E_GETMORE.E_GETMORE_NO_MORE);
			break;
			
		case GoodsListLoader.MSG_FINISH_GET_MORE:
			hideProgress();
			
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
				//QuanleimuApplication.getApplication().setListSearchGoods(mListLoader.getGoodsList().getData());

				adapter.setList(mListLoader.getGoodsList().getData());
				adapter.notifyDataSetChanged();	
				
				mListLoader.setHasMore(true);
				
				lvSearchResult.onGetMoreCompleted(PullToRefreshListView.E_GETMORE.E_GETMORE_OK);
			}
			break;
		case ErrorHandler.ERROR_NETWORK_UNAVAILABLE:
			hideProgress();
			
			progressBar.setVisibility(View.GONE);

			Toast.makeText(activity, "网络连接失败，请检查设置！", Toast.LENGTH_LONG).show();
			
			lvSearchResult.onFail();
			break;
		}

	}




	private static final int HOUR_MS = 60 * 60 * 1000;
	private static final int MINUTE_MS = 60 * 1000;

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		if (null == mListLoader
				|| null == mListLoader.getGoodsList()
				|| null == mListLoader.getGoodsList().getData()
				|| mListLoader.getGoodsList().getData().size() <= firstVisibleItem) {
			return;
		}

		String number = "";
		String unit = "";

		firstVisibleItem -= ((PullToRefreshListView) view)
				.getHeaderViewsCount();
		if (firstVisibleItem < 0)
			firstVisibleItem = 0;

		if (0 == titleControlStatus) {// time-sequenced
			Date date = new Date(
					Long.parseLong(mListLoader
							.getGoodsList()
							.getData()
							.get(firstVisibleItem)
							.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_DATE)) * 1000);
			long time_first_item = date.getTime();

			long time_diff = System.currentTimeMillis() - time_first_item;

			long nHours = time_diff / HOUR_MS;
			time_diff %= HOUR_MS;
			long nMinutes = time_diff / MINUTE_MS;
			time_diff %= MINUTE_MS;

			if (nHours > 0) {
				unit = "小时";
				number += nHours;
				int fractorHours = (int) (nMinutes / 6.0f);
				if (fractorHours > 0) {
					number += "." + fractorHours;
				}
			} else {
				unit = "分钟";
				number += nMinutes;
	    		if(number.contains("-")){
	    			number = "1";
	    		}
	    		else if(number.equals("0")){
	    			number = "1";
	    		}	    	
			}
		} else {
			GoodsDetail detail = mListLoader.getGoodsList().getData()
					.get(firstVisibleItem);
			String lat = detail
					.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_LAT);
			String lon = detail
					.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_LON);

			if (null == lat || 0 == lat.length() || null == lon
					|| 0 == lon.length()) {
				Log.d("GetGoodView", "ad nearby lacks lat & lon");
				unit = "米";
				number = "0";
			} else {

				float results[] = { 0.0f, 0.0f, 0.0f };
				Location.distanceBetween(Double.valueOf(lat),
						Double.valueOf(lon), curLocation.fLat,
						curLocation.fLon, results);

				if (results[0] < 1000) {
					unit = "米";
					number += (int) (results[0]);
				} else {
					unit = "公里";
					int kilo_number = (int) (results[0] / 1000);
					int fractor_kilo_number = (int) ((results[0] - (kilo_number * 1000)) / 100);
					number = "" + kilo_number + "." + fractor_kilo_number;
				}
			}
		}

		((TextView) getView().findViewById(R.id.tvSpaceOrTimeNumber)).setText(number);
		((TextView) getView().findViewById(R.id.tvSpaceOrTimeUnit)).setText(unit);
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (scrollState == SCROLL_STATE_IDLE) {
			ArrayList<String> urls = new ArrayList<String>();
			for (int index = 0; index < view.getChildCount(); ++index) {
				View curView = view.getChildAt(+index);
				if (null != curView) {
					View curIv = curView.findViewById(R.id.ivInfo);

					if (null != curIv && null != curIv.getTag())
						urls.add(curIv.getTag().toString());
				}
			}

			SimpleImageLoader.AdjustPriority(urls);
		}
	}

	@Override
	public void onGetMore() {
		mListLoader
				.startFetching(
						false,
						((GoodsListLoader.E_LISTDATA_STATUS.E_LISTDATA_STATUS_ONLINE == mListLoader
								.getDataStatus()) ? Communication.E_DATA_POLICY.E_DATA_POLICY_NETWORK_CACHEABLE
								: Communication.E_DATA_POLICY.E_DATA_POLICY_ONLY_LOCAL));
	}

	@Override
	public void onRefresh() {
		mListLoader
				.startFetching(
						true,
						mRefreshUsingLocal ? Communication.E_DATA_POLICY.E_DATA_POLICY_ONLY_LOCAL
								: Communication.E_DATA_POLICY.E_DATA_POLICY_NETWORK_CACHEABLE);
		mRefreshUsingLocal = false;
	}

	@Override
	public void handleRightAction(){
		pushAndFinish(new SearchFragment(), createArguments(null, null));
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnRecent:
			if (titleControlStatus != 0) {
				View btnNearBy = titleControl.findViewById(R.id.btnNearby);
				int paddingLeft = btnNearBy.getPaddingLeft(), paddingRight = btnNearBy
						.getPaddingRight(), paddingTop = btnNearBy
						.getPaddingTop(), paddingBottom = btnNearBy
						.getPaddingBottom();
				btnNearBy
						.setBackgroundResource(R.drawable.bg_nav_seg_left_normal);
				btnNearBy.setPadding(paddingLeft, paddingTop, paddingRight,
						paddingBottom);

				View btnRecent = titleControl.findViewById(R.id.btnRecent);
				paddingLeft = btnRecent.getPaddingLeft();
				paddingRight = btnRecent.getPaddingRight();
				paddingTop = btnRecent.getPaddingTop();
				paddingBottom = btnRecent.getPaddingBottom();
				btnRecent
						.setBackgroundResource(R.drawable.bg_nav_seg_right_pressed);
				btnRecent.setPadding(paddingLeft, paddingTop, paddingRight,
						paddingBottom);

				((TextView) getView().findViewById(R.id.tvSpaceOrTimeUnit)).setText("小时");

				mListLoader.setNearby(false);
				mListLoader.cancelFetching();
				mListLoader.setParams(basicParams);

				mRefreshUsingLocal = true;
				lvSearchResult.onFail();
				lvSearchResult.fireRefresh();

				titleControlStatus = 0;
			}
			break;
		case R.id.btnNearby:
			if (titleControlStatus != 1) {
				View btnNearBy = titleControl.findViewById(R.id.btnNearby);
				int paddingLeft = btnNearBy.getPaddingLeft(), paddingRight = btnNearBy
						.getPaddingRight(), paddingTop = btnNearBy
						.getPaddingTop(), paddingBottom = btnNearBy
						.getPaddingBottom();
				btnNearBy
						.setBackgroundResource(R.drawable.bg_nav_seg_left_pressed);
				btnNearBy.setPadding(paddingLeft, paddingTop, paddingRight,
						paddingBottom);

				View btnRecent = titleControl.findViewById(R.id.btnRecent);
				paddingLeft = btnRecent.getPaddingLeft();
				paddingRight = btnRecent.getPaddingRight();
				paddingTop = btnRecent.getPaddingTop();
				paddingBottom = btnRecent.getPaddingBottom();
				btnRecent
						.setBackgroundResource(R.drawable.bg_nav_seg_right_normal);
				btnRecent.setPadding(paddingLeft, paddingTop, paddingRight,
						paddingBottom);

				((TextView) getView().findViewById(R.id.tvSpaceOrTimeUnit)).setText("米");

				mListLoader.cancelFetching();

				List<String> params = new ArrayList<String>();
				params.addAll(basicParams);
				params.add("nearby=true");
				mListLoader.setNearby(true);
				curLocation = QuanleimuApplication.getApplication()
						.getCurrentPosition(false);
				// Log.d("kkkkkk",
				// "search goods nearby: ("+location.fLat+", "+location.fLon+") !!");
				params.add("lat=" + curLocation.fLat);
				params.add("lng=" + curLocation.fLon);
				mListLoader.setParams(params);

				mRefreshUsingLocal = true;
				lvSearchResult.onFail();
				lvSearchResult.fireRefresh();

				titleControlStatus = 1;
			}
			break;
		}
	}
	
}
