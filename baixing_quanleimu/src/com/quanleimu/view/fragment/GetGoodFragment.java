package com.quanleimu.view.fragment;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.Location;
import android.os.Bundle;
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
import android.widget.HeaderViewListAdapter;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.quanleimu.activity.BaseFragment;
import com.quanleimu.activity.QuanleimuApplication;
import com.quanleimu.activity.R;
import com.quanleimu.adapter.GoodsListAdapter;
import com.quanleimu.entity.BXLocation;
import com.quanleimu.entity.Filterss;
import com.quanleimu.entity.GoodsDetail;
import com.quanleimu.entity.GoodsList;
import com.quanleimu.entity.PostMu;
import com.quanleimu.imageCache.SimpleImageLoader;
import com.quanleimu.jsonutil.JsonUtil;
import com.quanleimu.util.Communication;
import com.quanleimu.util.ErrorHandler;
import com.quanleimu.util.GoodsListLoader;
import com.quanleimu.util.ParameterHolder;
import com.quanleimu.util.Util;
import com.quanleimu.view.FilterUtil;
import com.quanleimu.view.FilterUtil.CustomizeItem;
import com.quanleimu.view.FilterUtil.FilterSelectListener;
import com.quanleimu.view.fragment.MultiLevelSelectionFragment.MultiLevelItem;
import com.quanleimu.widget.PullToRefreshListView;
import com.quanleimu.widget.PullToRefreshListView.E_GETMORE;

public class GetGoodFragment extends BaseFragment implements View.OnClickListener, OnScrollListener, PullToRefreshListView.OnRefreshListener, PullToRefreshListView.OnGetmoreListener {

	public static final int MSG_UPDATE_FILTER = 1000;
	
	
	private static final int REQ_SIFT = 1;
	
	private PullToRefreshListView lvGoodsList;
	private ProgressBar progressBar;
	private int titleControlStatus = 0;
	
	private String categoryEnglishName = "";
	private String searchContent = "";
//	private String siftResult = "";
	private PostParamsHolder filterParamHolder;
	
	private List<String> basicParams = null;
	
	private GoodsListLoader goodsListLoader = null;
	
	private boolean mRefreshUsingLocal = false;
	private BXLocation curLocation = null;
	
	private List<Filterss> listFilterss;
	
    private static final int HOUR_MS = 60*60*1000;
    private static final int MINUTE_MS = 60*1000;
	
	
	@Override
	protected void initTitle(TitleDef title) {
		title.m_visible = true;
		title.m_leftActionHint = "返回";//this.getArguments().getString("backPageName");
		title.m_title = getArguments().getString("categoryName");//getArguments().getString("name");
		title.m_rightActionHint = "发布";
		title.m_rightActionBg = R.drawable.bg_post_selector;
		title.hasGlobalSearch = true;
//		title.m_rightActionHint = (this.categoryEnglishName == null || this.categoryEnglishName.equals("")) ? "搜索" : "筛选";
		
//		LayoutInflater inflater = LayoutInflater.from(this.getActivity());
//		View titleControl = inflater.inflate(R.layout.recent_or_nearby, null);
//		titleControl.findViewById(R.id.btnNearby).setOnClickListener(this);
//		titleControl.findViewById(R.id.btnRecent).setOnClickListener(this);
		
//		title.m_titleControls = titleControl;
	}
	
	public void initTab(TabDef tab){
		tab.m_visible = false;
	}
	
	public GoodsListAdapter findGoodListAdapter()
	{
		ListAdapter adapter = lvGoodsList.getAdapter();
		if (adapter instanceof HeaderViewListAdapter)
		{
			HeaderViewListAdapter realAdapter = (HeaderViewListAdapter) adapter;
			return (GoodsListAdapter) realAdapter.getWrappedAdapter();
		}
		else
		{
			return (GoodsListAdapter) adapter;
		}
	}
	
	public void handleRightAction(){
		String categoryName = getArguments().getString("categoryName");
		categoryName = categoryEnglishName + "," + categoryName;
		Bundle bundle = createArguments(null, null);
		bundle.putSerializable("cateNames", categoryName);
		pushFragment(new PostGoodsFragment(), bundle);
		
//		if(this.categoryEnglishName == null || this.categoryEnglishName.equals("")){
//			pushAndFinish(new SearchFragment(), createArguments(null, null));
//		}else{
//			Bundle args = createArguments(null, getArguments().getString(ARG_COMMON_BACK_HINT));
//			args.putAll(getArguments());
//	//		bundle.putString("backPageName", bundle.getString("backPageName"));
//			args.putInt(ARG_COMMON_REQ_CODE, REQ_SIFT);
//			args.putString("searchType", "goodslist");
//			args.putString("categoryEnglishName", categoryEnglishName);
//	
//			pushFragment(new SiftFragment(), args);
//		}
	}
	
	
	
	@Override
	protected void onFragmentBackWithData(int requestCode, Object result) {
		if (requestCode == REQ_SIFT && result instanceof PostParamsHolder)
		{
			this.filterParamHolder.merge((PostParamsHolder) result);
			this.updateSearchParams();
			this.resetSearch(this.titleControlStatus == 0, this.curLocation);
			
			lvGoodsList.fireRefresh();
//			pushAndFinish(new GetGoodFragment(), (Bundle) result);
		}
	}

	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		this.categoryEnglishName = getArguments().getString("categoryEnglishName");
		this.searchContent = getArguments().getString("searchContent");
//		if (getArguments().containsKey("siftresult")) //FIXME:CHONG siftresult is removed by chong.
//		{
//			this.siftResult = getArguments().getString("siftresult");
//		}
		filterParamHolder = (PostParamsHolder)getArguments().getSerializable("filterResult");
		if (filterParamHolder == null)
		{
			filterParamHolder = new PostParamsHolder();
			getArguments().putSerializable("filterResult", filterParamHolder);
		}
		
		updateSearchParams();
		
//		if (siftResult != null && !siftResult.equals("")) {
//		} else {
//			basicParams.add("query="
//					+ "cityEnglishName:"+QuanleimuApplication.getApplication().getCityEnglishName()+" AND categoryEnglishName:"
//					+ categoryEnglishName + " AND status:0");
//		}
		
		goodsListLoader = new GoodsListLoader(basicParams, handler, null, new GoodsList());
	}
	
	private  void updateSearchParams()
	{
		basicParams = new ArrayList<String>();
		if(this.categoryEnglishName != null && !this.categoryEnglishName.equals("")){
			String siftResult = filterParamHolder.toUrlString(); 
			basicParams.add("query="
					+ "cityEnglishName:" + QuanleimuApplication.getApplication().getCityEnglishName()
					+ " AND categoryEnglishName:" + categoryEnglishName
					+ ((siftResult != null && !siftResult.equals("")) ? (" " + siftResult) : " AND status:0"));
			
		}else if(this.searchContent != null && !this.searchContent.equals("")){
			basicParams.add("query=" 
					+ Communication.urlEncode(URLEncoder.encode("cityEnglishName:" 
								+ QuanleimuApplication.getApplication().getCityEnglishName() + " AND "
								+ searchContent)));
		}
	}
	

	@Override
	public void onResume() {
		super.onResume();
		goodsListLoader.setHandler(handler);
	}
	
	@Override
	public void onStackTop(boolean isBack) {
		super.onStackTop(isBack);
		if (goodsListLoader.getGoodsList().getData() != null && goodsListLoader.getGoodsList().getData().size() > 0)
		{
			GoodsListAdapter adapter = new GoodsListAdapter(getActivity(), goodsListLoader.getGoodsList().getData());
			updateData(adapter, goodsListLoader.getGoodsList().getData());
			lvGoodsList.setAdapter(adapter);
			lvGoodsList.setSelectionFromHeader(goodsListLoader.getSelection());
		}
		else
		{
			showSimpleProgress();
			goodsListLoader.startFetching(true, Communication.E_DATA_POLICY.E_DATA_POLICY_ONLY_LOCAL);
		}
		
		
		//Update filter bar.
		PostMu postMu = (PostMu) Util
				.loadDataFromLocate(
						getActivity(),
						"saveFilterss"
								+ categoryEnglishName
								+ QuanleimuApplication.getApplication().cityEnglishName);
		if (postMu == null || postMu.getJson().equals(""))
		{
			new Thread(new GetGoodsListThread(true)).start();
		}
		else
		{
			showFilterBar(getView().findViewById(R.id.filter_bar_root), postMu.getJson());
		}
		
	}
	
	class GetGoodsListThread implements Runnable {
		private boolean isUpdate;
		public GetGoodsListThread(boolean isUpdate){
			this.isUpdate = isUpdate;
		}
		@Override
		public void run() {
			String apiName = "category_meta_filter";
			ArrayList<String> list = new ArrayList<String>();

			list.add("categoryEnglishName=" + categoryEnglishName);
			list.add("cityEnglishName=" + QuanleimuApplication.getApplication().cityEnglishName);

			String url = Communication.getApiUrl(apiName, list);
			try {
				String json = Communication.getDataByUrl(url, false);
				if (json != null) {
					PostMu postMu = new PostMu();
					postMu.setJson(json);
					postMu.setTime(System.currentTimeMillis());
					Util.saveDataToLocate(getAppContext(), "saveFilterss"+categoryEnglishName+QuanleimuApplication.getApplication().cityEnglishName, postMu);
					if(isUpdate){
						sendMessage(MSG_UPDATE_FILTER, json);
					}
				} 
//				else {
//					sendMessage(2, null);
//				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Communication.BXHttpException e){
				
			}
			
			hideProgress();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		logCreateView(savedInstanceState);
		
		View v = inflater.inflate(R.layout.goodslist, null);
		
//		View titleControl = inflater.inflate(R.layout.recent_or_nearby, null);
//		titleControl.findViewById(R.id.btnNearby).setOnClickListener(this);
//		titleControl.findViewById(R.id.btnRecent).setOnClickListener(this);
//		this.getTitleDef().m_titleControls = titleControl;
		
		lvGoodsList = (PullToRefreshListView) v.findViewById(R.id.lvGoodsList);
		lvGoodsList.setOnRefreshListener(this);
		lvGoodsList.setOnGetMoreListener(this);

        LinearLayout layout = new LinearLayout(this.getActivity());  
        layout.setOrientation(LinearLayout.HORIZONTAL);  
        progressBar = new ProgressBar(this.getActivity(), null, android.R.attr.progressBarStyleSmall);
         //进度条显示位置  
        progressBar.setVisibility(View.GONE);
        
        LayoutParams WClayoutParams =
        		new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        layout.addView(progressBar, WClayoutParams);  
        layout.setGravity(Gravity.CENTER);  
		
		lvGoodsList.setOnScrollListener(this);

	
		curLocation = QuanleimuApplication.getApplication().getCurrentPosition(true);
		List<String> addParams = new ArrayList<String>(basicParams);
		if(curLocation == null || titleControlStatus == 0){
//			((Button)titleControl.findViewById(R.id.btnNearby)).setBackgroundResource(R.drawable.bg_nav_seg_left_normal);
//			((Button)titleControl.findViewById(R.id.btnRecent)).setBackgroundResource(R.drawable.bg_nav_seg_right_pressed);
//			((TextView)v.findViewById(R.id.tvSpaceOrTimeNumber)).setText("0");
//			((TextView)v.findViewById(R.id.tvSpaceOrTimeUnit)).setText("小时");
			this.titleControlStatus = 0;
		}else{
//			((TextView)v.findViewById(R.id.tvSpaceOrTimeNumber)).setText("0");
//			((TextView)v.findViewById(R.id.tvSpaceOrTimeUnit)).setText("公里");
			addParams.add("lat="+curLocation.fLat);
			addParams.add("lng="+curLocation.fLon);			
		}
		
		goodsListLoader.setParams(addParams); //= new GoodsListLoader(addParams, myHandler, null, new GoodsList());
		if(curLocation != null && titleControlStatus != 0){
			goodsListLoader.setNearby(true);
			goodsListLoader.setRuntime(false);
		}
		
		lvGoodsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int pos, long arg3) {
				
				int index = (int) arg3;//(int) (arg3 - lvGoodsList.getHeaderViewsCount());
				if(index < 0 || index > goodsListLoader.getGoodsList().getData().size() - 1)
					return;

					Bundle bundle = createArguments(null, null);
					bundle.putSerializable("loader", goodsListLoader);
					bundle.putInt("index", index);
					pushFragment(new GoodDetailFragment(), bundle);
//				}				
			}
		});
		
//		((TextView)v.findViewById(R.id.tvSubCateName)).setText(getArguments().getString("name"));
		
		
		String categoryName = getArguments().getString("categoryName");
		if(categoryName == null || categoryName.equals("")){
			if(categoryEnglishName != null){
				categoryName = QuanleimuApplication.getApplication().queryCategoryDisplayName(categoryEnglishName);
				if(categoryName != null) getArguments().putString("categoryName", categoryName);
			}
		}
		
//		if(categoryName == null || categoryName.equals("") 
//				|| categoryEnglishName == null || categoryEnglishName.equals("")){
//			v.findViewById(R.id.publishBtn).setVisibility(View.GONE);
//		}else{
//			v.findViewById(R.id.publishBtn).setOnClickListener(this);
//			v.findViewById(R.id.publishBtn).setVisibility(View.VISIBLE);
//		}
		
		return v;
	
	}



	@Override
	public void onClick(View v) {
		GoodsListAdapter adapter = this.findGoodListAdapter();
		switch(v.getId()){
//		case R.id.publishBtn:
//			String categoryName = getArguments().getString("categoryName");
//			categoryName = categoryEnglishName + "," + categoryName;
//			Bundle bundle = createArguments(null, null);
//			bundle.putSerializable("cateNames", categoryName);
//			pushFragment(new PostGoodsFragment(), bundle);
////			m_viewInfoListener.onNewView(new PostGoodsView((BaseActivity)this.getContext(), this.bundle, categoryName));
//			break;
		case R.id.btnRecent:
			if(titleControlStatus != 0){
				View btnNearBy = getActivity().findViewById(R.id.btnNearby);
				int paddingLeft = btnNearBy.getPaddingLeft(), paddingRight = btnNearBy.getPaddingRight(), paddingTop=btnNearBy.getPaddingTop(), paddingBottom=btnNearBy.getPaddingBottom();
				btnNearBy.setBackgroundResource(R.drawable.bg_nav_seg_left_normal);
				btnNearBy.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
				
				View btnRecent = getActivity().findViewById(R.id.btnRecent);
				paddingLeft = btnRecent.getPaddingLeft(); paddingRight = btnRecent.getPaddingRight(); paddingTop=btnRecent.getPaddingTop();paddingBottom=btnRecent.getPaddingBottom();
				btnRecent.setBackgroundResource(R.drawable.bg_nav_seg_right_pressed);
				btnRecent.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
				
//				((TextView)getActivity().findViewById(R.id.tvSpaceOrTimeNumber)).setText("0");
//				((TextView)getActivity().findViewById(R.id.tvSpaceOrTimeUnit)).setText("小时");
				
//				goodsListLoader.cancelFetching();
//				goodsListLoader.setNearby(false);
//				goodsListLoader.setParams(basicParams);
//				goodsListLoader.setRuntime(true);
				this.resetSearch(false, curLocation);
				
				mRefreshUsingLocal = true;
				lvGoodsList.onFail();
				lvGoodsList.fireRefresh();
				if(adapter != null){
					adapter.setList(new ArrayList<GoodsDetail>(), null);
					adapter.notifyDataSetChanged();
//					((TextView)getView().findViewById(R.id.tvSpaceOrTimeNumber)).setText("0");
//					((TextView)getView().findViewById(R.id.tvSpaceOrTimeUnit)).setText("小时");
				}
				
				titleControlStatus = 0;
			}
			break;
		case R.id.btnNearby:
			if(titleControlStatus != 1){
				curLocation = QuanleimuApplication.getApplication().getCurrentPosition(true);
				if(curLocation == null){
					new AlertDialog.Builder(this.getActivity()).setTitle("提醒").setMessage("无法确定当前位置")
					.setPositiveButton("确定", new DialogInterface.OnClickListener(){
						@Override
						public void onClick(DialogInterface dialog, int which){
							dialog.dismiss();
						}
					}).show();
					return;
				}
				
				View btnNearBy = getActivity().findViewById(R.id.btnNearby);
				int paddingLeft = btnNearBy.getPaddingLeft(), paddingRight = btnNearBy.getPaddingRight(), paddingTop=btnNearBy.getPaddingTop(), paddingBottom=btnNearBy.getPaddingBottom();
				btnNearBy.setBackgroundResource(R.drawable.bg_nav_seg_left_pressed);
				btnNearBy.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
				
				View btnRecent = getActivity().findViewById(R.id.btnRecent);
				paddingLeft = btnRecent.getPaddingLeft(); paddingRight = btnRecent.getPaddingRight(); paddingTop=btnRecent.getPaddingTop();paddingBottom=btnRecent.getPaddingBottom();
				btnRecent.setBackgroundResource(R.drawable.bg_nav_seg_right_normal);
				btnRecent.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
				
//				((TextView)getActivity().findViewById(R.id.tvSpaceOrTimeNumber)).setText("0");
//				((TextView)getActivity().findViewById(R.id.tvSpaceOrTimeUnit)).setText("米");
				
//				goodsListLoader.cancelFetching();
//				
//				List<String> params = new ArrayList<String>();
//				params.addAll(basicParams);
////				params.add("nearby=true");
//				goodsListLoader.setNearby(true);
////				curLocation = QuanleimuApplication.getApplication().getCurrentPosition(false);
//				//Log.d("kkkkkk", "get goods nearby: ("+curLocation.fLat+", "+curLocation.fLon+") !!");
//				params.add("lat="+curLocation.fLat);
//				params.add("lng="+curLocation.fLon);
//				goodsListLoader.setParams(params);
//				goodsListLoader.setRuntime(false);
				this.resetSearch(true, curLocation);
				
				mRefreshUsingLocal = true;
				lvGoodsList.onFail();
				lvGoodsList.fireRefresh();
				
				if(adapter != null){
					adapter.setList(new ArrayList<GoodsDetail>(), null);
					adapter.notifyDataSetChanged();
//					((TextView)getView().findViewById(R.id.tvSpaceOrTimeNumber)).setText("0");
//					((TextView)getView().findViewById(R.id.tvSpaceOrTimeUnit)).setText("公里");
				}
				
				titleControlStatus = 1;
			}
			break;
		}
	}
	
	@Override
	public void onPause(){
		this.lvGoodsList.setOnScrollListener(null);
		super.onPause();
	}
	
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		GoodsListAdapter adapter = this.findGoodListAdapter();
		if(null == goodsListLoader || 
				null == goodsListLoader.getGoodsList() || 
				null == goodsListLoader.getGoodsList().getData() ||
				(adapter == null || adapter.getList() == null || adapter.getList().size() == 0) ||
				goodsListLoader.getGoodsList().getData().size() <= firstVisibleItem){
			return;
		}
		
    	String number = "";
    	String unit = "";
    	
    	firstVisibleItem -= ((PullToRefreshListView)view).getHeaderViewsCount();
    	if(firstVisibleItem < 0)	firstVisibleItem = 0;
    	
		if(0 == titleControlStatus){//time-sequenced
			Date date = new Date(Long.parseLong(goodsListLoader.getGoodsList().getData().get(firstVisibleItem).getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_DATE)) * 1000);
			long time_first_item = date.getTime();
			//Log.d("kkkkkk", "item:"+firstVisibleItem+", dateTime: "+time_first_item);

	    	long time_diff = System.currentTimeMillis() - time_first_item;
	    	
	    	long nHours = time_diff / HOUR_MS;
	    	time_diff %= HOUR_MS;
	    	long nMinutes = time_diff / MINUTE_MS;
	    	time_diff %= MINUTE_MS;

	    	if(nHours > 0){
	    		unit = "小时";
	    		number += nHours;
	    		int fractorHours = (int)(nMinutes/6.0f);
	    		if(fractorHours > 0){
	    			number += "."+fractorHours;
	    		}
	    	}else{
	    		unit = "分钟";
	    		number += nMinutes;
	    		if(number.contains("-")){
	    			number = "1";
	    		}
	    		else if(number.equals("0")){
	    			number = "1";
	    		}
	    	}
		}else{
			GoodsDetail detail = goodsListLoader.getGoodsList().getData().get(firstVisibleItem);
			String lat = detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_LAT);
			String lon = detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_LON);
			
			double latD = 0;
			double lonD = 0;
			try
			{
				latD = Double.parseDouble(lat);
				lonD = Double.parseDouble(lon);
			}
			catch(Throwable t)
			{
				Log.d("GetGoodView", "ad nearby lacks lat & lon");
			}
			
			if(latD == 0  || lonD == 0){
				unit = "米";
				number = "0";
			}else{

				float results[] = {0.0f, 0.0f, 0.0f};
				Location.distanceBetween(latD, lonD, curLocation.fLat, curLocation.fLon, results);
				
				if(results[0] < 1000){
					unit = "米";
					number += (int)(results[0]);
				}else{
					unit = "公里";
					int kilo_number = (int)(results[0]/1000);
					int fractor_kilo_number = (int)((results[0]-(kilo_number*1000))/100);
					number = ""+kilo_number+"."+fractor_kilo_number;
				}
			}
		}
		
//		((TextView)getActivity().findViewById(R.id.tvSpaceOrTimeNumber)).setText(number);
//		((TextView)getActivity().findViewById(R.id.tvSpaceOrTimeUnit)).setText(unit);
		
		//Log.d("kkkkkk", "first visible item: "+firstVisibleItem+", visibleItemCount: "+visibleItemCount+", totalItemCount: "+totalItemCount);
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
		goodsListLoader.startFetching(false, ((GoodsListLoader.E_LISTDATA_STATUS.E_LISTDATA_STATUS_ONLINE == goodsListLoader.getDataStatus()) ? 
												Communication.E_DATA_POLICY.E_DATA_POLICY_NETWORK_CACHEABLE :
												Communication.E_DATA_POLICY.E_DATA_POLICY_ONLY_LOCAL));
	}

	@Override
	public void onRefresh() {
		goodsListLoader.startFetching(true, mRefreshUsingLocal ? Communication.E_DATA_POLICY.E_DATA_POLICY_ONLY_LOCAL : Communication.E_DATA_POLICY.E_DATA_POLICY_NETWORK_CACHEABLE);	
		mRefreshUsingLocal = false;
	}

	private void updateData(GoodsListAdapter adapter, List<GoodsDetail> data)
	{
		adapter.setList(data, titleControlStatus == 1 ? FilterUtil.createDistanceGroup(listFilterss, data, this.curLocation, new int[] {500, 1500}) : 
			FilterUtil.createFilterGroup(listFilterss, filterParamHolder, data) );
	}
	
	@Override
	protected void handleMessage(Message msg, Activity activity, View rootView) {

		switch (msg.what) {
		case GoodsListLoader.MSG_FIRST_FAIL:
			if(goodsListLoader == null) break;
			if(GoodsListLoader.E_LISTDATA_STATUS.E_LISTDATA_STATUS_OFFLINE == goodsListLoader.getRequestDataStatus())
				goodsListLoader.startFetching(true, Communication.E_DATA_POLICY.E_DATA_POLICY_NETWORK_CACHEABLE);
			else{
				Toast.makeText(getActivity(), "没有符合条件的结果，请重新输入！", Toast.LENGTH_LONG).show();
				hideProgress();
			}
			break;
		case GoodsListLoader.MSG_FINISH_GET_FIRST:
			if(goodsListLoader == null) break;
			GoodsList goodsList = JsonUtil.getGoodsListFromJson(goodsListLoader.getLastJson());
			goodsListLoader.setGoodsList(goodsList);
			if (goodsList == null || goodsList.getData() == null || goodsList.getData().size() == 0) {
				Message msg1 = Message.obtain();
				msg1.what = ErrorHandler.ERROR_COMMON_FAILURE;
				Bundle bundle = new Bundle();
				bundle.putString("popup_message", "没有符合的结果，请更改条件并重试！");
				msg1.setData(bundle);
				QuanleimuApplication.getApplication().getErrorHandler().sendMessage(msg1);
			} else {
				//QuanleimuApplication.getApplication().setListGoods(goodsListLoader.getGoodsList().getData());
				GoodsListAdapter adapter = new GoodsListAdapter(getActivity(), goodsListLoader.getGoodsList().getData());
				updateData(adapter, goodsListLoader.getGoodsList().getData());
				lvGoodsList.setAdapter(adapter);
				goodsListLoader.setHasMore(true);
			}
			
			lvGoodsList.onRefreshComplete();
			
			//if currently using offline data, start fetching online data
			if(GoodsListLoader.E_LISTDATA_STATUS.E_LISTDATA_STATUS_OFFLINE == goodsListLoader.getDataStatus())
				lvGoodsList.fireRefresh();
				//goodsListLoader.startFetching(true, Communication.E_DATA_POLICY.E_DATA_POLICY_NETWORK_CACHEABLE);
			
			hideProgress();
			break;
		case GoodsListLoader.MSG_NO_MORE:
			if(goodsListLoader == null) break;
			progressBar.setVisibility(View.GONE);
			
//			Message msg1 = Message.obtain();
//			msg1.what = ErrorHandler.ERROR_COMMON_FAILURE;
//			Bundle bundle = new Bundle();
//			bundle.putString("popup_message", "数据下载失败，请重试！");
//			msg1.setData(bundle);
//			QuanleimuApplication.getApplication().getErrorHandler().sendMessage(msg1);
			
			lvGoodsList.onGetMoreCompleted(PullToRefreshListView.E_GETMORE.E_GETMORE_NO_MORE);
			goodsListLoader.setHasMore(false);
			
			hideProgress();			
			break;
		case GoodsListLoader.MSG_FINISH_GET_MORE:
			if(goodsListLoader == null) break;
			progressBar.setVisibility(View.GONE);
			
			GoodsList moreGoodsList = JsonUtil.getGoodsListFromJson(goodsListLoader.getLastJson());
			if (moreGoodsList == null || moreGoodsList.getData() == null || moreGoodsList.getData().size() == 0) {
//				Message msg2 = Message.obtain();
//				msg2.what = ErrorHandler.ERROR_COMMON_WARNING;
//				Bundle bundle1 = new Bundle();
//				bundle1.putString("popup_message", "没有更多啦！");
//				msg2.setData(bundle1);
//				QuanleimuApplication.getApplication().getErrorHandler().sendMessage(msg2);
				
				lvGoodsList.onGetMoreCompleted(E_GETMORE.E_GETMORE_NO_MORE);
				goodsListLoader.setHasMore(false);
			} else {
				List<GoodsDetail> listCommonGoods =  moreGoodsList.getData();
				for(int i=0;i<listCommonGoods.size();i++)
				{
					goodsListLoader.getGoodsList().getData().add(listCommonGoods.get(i));
				}
				//QuanleimuApplication.getApplication().setListGoods(goodsListLoader.getGoodsList().getData());
				
				GoodsListAdapter adapter = findGoodListAdapter();
				updateData(adapter, goodsListLoader.getGoodsList().getData());
				adapter.notifyDataSetChanged();		
				
				lvGoodsList.onGetMoreCompleted(PullToRefreshListView.E_GETMORE.E_GETMORE_OK);
				goodsListLoader.setHasMore(true);
			}
			
			hideProgress();			
			break;
		case ErrorHandler.ERROR_NETWORK_UNAVAILABLE:
			if(goodsListLoader == null) break;
			progressBar.setVisibility(View.GONE);

			Message msg2 = Message.obtain();
			msg2.what = ErrorHandler.ERROR_NETWORK_UNAVAILABLE;
			QuanleimuApplication.getApplication().getErrorHandler().sendMessage(msg2);
			
			lvGoodsList.onFail();
			
			hideProgress();			
			break;
		case MSG_UPDATE_FILTER:
			showFilterBar(rootView, (String) msg.obj);
			break;
		}
		
	}
	
	public void showFilterBar(View root, String json)
	{
		View[] actionViews = findAllFilterView();
		
		if (actionViews == null)
		{
			return;
		}
		
		listFilterss = JsonUtil.getFilters(json).getFilterssList();
		
		
		View.OnClickListener listener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (v.getId() == R.id.filter_item_more)
				{
					if(categoryEnglishName == null || categoryEnglishName.equals("")){
						pushAndFinish(new SearchFragment(), createArguments(null, null));
					}else{
						Bundle args = createArguments(null, getArguments().getString(ARG_COMMON_BACK_HINT));
						args.putAll(getArguments());
				//		bundle.putString("backPageName", bundle.getString("backPageName"));
						args.putInt(ARG_COMMON_REQ_CODE, REQ_SIFT);
						args.putString("searchType", "goodslist");
						args.putString("categoryEnglishName", categoryEnglishName);
				
						pushFragment(new SiftFragment(), args);
					}
				}
				else
				{
					final Filterss fss = (Filterss) v.getTag();
					final boolean isLocation = fss.getName().equals("地区_s");
					
					CustomizeItem cItem = new CustomizeItem();
					cItem.id = "";
					cItem.txt = "附近500米";
					
					CustomizeItem[] cs = isLocation && curLocation != null ? new CustomizeItem[] {cItem} : null; 
					
					FilterUtil.startSelect(getActivity(), cs, fss, new FilterSelectListener() {
						
						@Override
						public void onItemSelect(MultiLevelItem item) {
							if (item instanceof CustomizeItem && isLocation)
							{
								titleControlStatus = 1; //Nearby
							}
							else
							{
								if (isLocation)
								{
									titleControlStatus = 0; //
								}
								FilterUtil.updateFilter(filterParamHolder, item, fss.getName());
							}
							
							FilterUtil.updateFilterLabel(findAllFilterView(), item.txt, fss);
							updateSearchParams();
							
							resetSearch(titleControlStatus == 1, curLocation);
							lvGoodsList.fireRefresh();
						}
						
						@Override
						public void onCancel() {
							//
						}
					});
				}
			}
		};
		
		for (View v : actionViews)
		{
			v.setOnClickListener(listener);
		}
		
		if (listFilterss != null)
		{
			FilterUtil.loadFilterBar(listFilterss, filterParamHolder, actionViews);
		}
		else
		{
			for (View view : actionViews)
			{
				view.setVisibility(View.GONE);
			}
		}
		
		getView().findViewById(R.id.filter_item_more).setOnClickListener(listener);
		
		//Search nearby is special.
		if (this.titleControlStatus == 1 && listFilterss != null) //If is searching nearby, set the filter label to "neayby."
		{
			for (Filterss fs : listFilterss)
			{
				if (fs.getName().equals("地区_s"))
				{
					FilterUtil.updateFilterLabel(actionViews, "附近500米", fs);
					break;
				}
			}
		}
		
	}
	
	private View[] findAllFilterView()
	{
		View filterParent =getView() == null ? null : getView().findViewById(R.id.filter_bar_root);
		if (filterParent == null)
		{
			return null;
		}
		
		View[] actionViews = new View[] {filterParent.findViewById(R.id.filter_item_1), 
				filterParent.findViewById(R.id.filter_item_2),
				filterParent.findViewById(R.id.filter_item_3)};
		
		return actionViews;
	}
	
	private void resetSearch(boolean isNeryBy, BXLocation location)
	{
		if (!isNeryBy)
		{
			goodsListLoader.cancelFetching();
			goodsListLoader.setNearby(false);
			goodsListLoader.setParams(basicParams);
			goodsListLoader.setRuntime(true);
		}
		else
		{
			goodsListLoader.cancelFetching();
			
			List<String> params = new ArrayList<String>();
			params.addAll(basicParams);
//			params.add("nearby=true");
			goodsListLoader.setNearby(true);
//			curLocation = QuanleimuApplication.getApplication().getCurrentPosition(false);
			//Log.d("kkkkkk", "get goods nearby: ("+curLocation.fLat+", "+curLocation.fLon+") !!");
			params.add("lat="+location.fLat);
			params.add("lng="+location.fLon);
			goodsListLoader.setParams(params);
			goodsListLoader.setRuntime(false);
		}
	}

}
