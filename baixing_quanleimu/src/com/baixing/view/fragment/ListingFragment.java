//liuchong@baixing.com
package com.baixing.view.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.HeaderViewListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListAdapter;
import android.widget.ProgressBar;

import com.baixing.activity.BaseFragment;
import com.baixing.adapter.VadListAdapter;
import com.baixing.adapter.VadListAdapter.GroupItem;
import com.baixing.data.GlobalDataManager;
import com.baixing.entity.Ad;
import com.baixing.entity.Ad.EDATAKEYS;
import com.baixing.entity.AdList;
import com.baixing.entity.AdSeperator;
import com.baixing.entity.BXLocation;
import com.baixing.entity.CityDetail;
import com.baixing.entity.Filterss;
import com.baixing.jsonutil.JsonUtil;
import com.baixing.network.api.ApiError;
import com.baixing.network.api.ApiParams;
import com.baixing.network.api.BaseApiCommand;
import com.baixing.network.api.BaseApiCommand.Callback;
import com.baixing.tracking.TrackConfig.TrackMobile.BxEvent;
import com.baixing.tracking.TrackConfig.TrackMobile.Key;
import com.baixing.tracking.TrackConfig.TrackMobile.PV;
import com.baixing.tracking.Tracker;
import com.baixing.util.ErrorHandler;
import com.baixing.util.PerformEvent.Event;
import com.baixing.util.PerformanceTracker;
import com.baixing.util.Util;
import com.baixing.util.VadListLoader;
import com.baixing.util.VadListLoader.SEARCH_POLICY;
import com.baixing.util.ViewUtil;
import com.baixing.view.AdViewHistory;
import com.baixing.view.FilterUtil;
import com.baixing.view.FilterUtil.FilterSelectListener;
import com.baixing.view.fragment.MultiLevelSelectionFragment.MultiLevelItem;
import com.baixing.widget.PullToRefreshListView;
import com.baixing.widget.PullToRefreshListView.E_GETMORE;
import com.quanleimu.activity.R;

public class ListingFragment extends BaseFragment implements OnScrollListener, PullToRefreshListView.OnRefreshListener, PullToRefreshListView.OnGetmoreListener,Callback, VadListLoader.Callback {

	public static final int MSG_UPDATE_FILTER = 1000;
	
	
	private static final int REQ_SIFT = 1;
	
	private PullToRefreshListView lvGoodsList;
	private ProgressBar progressBar;
	private String actType = null;
	private String categoryEnglishName = "";
	private String searchContent = "";
	private PostParamsHolder filterParamHolder;
	
//	private List<String> basicParams = null;
	
	private VadListLoader goodsListLoader = null;
	
	private boolean mRefreshUsingLocal = false;
	private BXLocation curLocation = null;
	
	private List<Filterss> listFilterss;
	
	
	@Override
	protected void initTitle(TitleDef title) {
		title.m_visible = true;
		title.m_leftActionHint = "返回";//this.getArguments().getString("backPageName");
		title.m_title = getArguments().getString("categoryName");//getArguments().getString("name");
		title.m_rightActionHint = "筛选";
	}
	
	public VadListAdapter findGoodListAdapter()
	{
		ListAdapter adapter = lvGoodsList == null ? null : lvGoodsList.getAdapter();
		if (adapter instanceof HeaderViewListAdapter)
		{
			HeaderViewListAdapter realAdapter = (HeaderViewListAdapter) adapter;
			return (VadListAdapter) realAdapter.getWrappedAdapter();
		}
		else
		{
			return (VadListAdapter) adapter;
		}
	}
	
	public void handleRightAction(){
//		String categoryName = getArguments().getString("categoryName");
//		categoryName = categoryEnglishName + "," + categoryName;
//		Bundle bundle = createArguments(null, null);
//		bundle.putSerializable("cateNames", categoryName);
//		pushFragment(new PostGoodsFragment(), bundle);
		if(categoryEnglishName == null || categoryEnglishName.equals("")){
			pushAndFinish(new SearchFragment(), createArguments(null, null));
		} else {
			Bundle args = createArguments(null, getArguments().getString(ARG_COMMON_BACK_HINT));
			args.putAll(getArguments());
			args.putInt(ARG_COMMON_REQ_CODE, REQ_SIFT);
			args.putString("searchType", "goodslist");
			args.putString("categoryEnglishName", categoryEnglishName);
	
			pushFragment(new FilterFragment(), args);
		}
	}
	
	public String getCategoryNames(){
		String categoryName = getArguments().getString("categoryName");
		return categoryEnglishName + "," + categoryName;		
	}
	
	@Override
	protected void onFragmentBackWithData(int requestCode, Object result) {
		if (requestCode == REQ_SIFT && result instanceof PostParamsHolder)
		{
			this.filterParamHolder.clear(); //Replace old params with new.
			this.filterParamHolder.merge((PostParamsHolder) result);
			this.showFilterBar(getView(), listFilterss);
			this.resetSearch(this.curLocation);
			
			lvGoodsList.fireRefresh();
		}
	}
	
	public ListingFragment() {
//		defaultEnterAnim = 0;
//		defaultExitAnim = 0;
	}

	public void onCreate(Bundle savedInstanceState)
	{
		PerformanceTracker.stamp(Event.E_ListingFrag_begin);
		super.onCreate(savedInstanceState);
		
		this.categoryEnglishName = getArguments().getString("categoryEnglishName");
		this.searchContent = getArguments().getString("searchContent");
		this.actType = getArguments().getString("actType");
		
		filterParamHolder = (PostParamsHolder)getArguments().getSerializable("filterResult");
		if (filterParamHolder == null)
		{
			filterParamHolder = new PostParamsHolder();
			getArguments().putSerializable("filterResult", filterParamHolder);
			filterParamHolder.put("cityEnglishName", GlobalDataManager.getInstance().getCityEnglishName(), GlobalDataManager.getInstance().getCityEnglishName());
			filterParamHolder.put("categoryEnglishName", categoryEnglishName, categoryEnglishName);
			filterParamHolder.put("status", "" + 0, "" + 0);
			if (searchContent != null)
			{
				filterParamHolder.put("", searchContent, searchContent);
			}
		}
		
		goodsListLoader = new VadListLoader(getSearchParams(), this, null, new AdList());
		goodsListLoader.setRuntime(true);
		PerformanceTracker.stamp(Event.E_ListingFrag_create_end);
	}
	
	@Override
	public void onResume() {
		PerformanceTracker.stamp(Event.E_Listing_Showup);
		super.onResume();
		if (actType != null && actType.equals("search"))//from headersearch
		{	
			this.pv = PV.SEARCHRESULT;
			Tracker.getInstance()
				.pv(this.pv)
				.append(Key.SEARCHKEYWORD, searchContent)
				.append(Key.SECONDCATENAME, categoryEnglishName)
				.end();
		}else {//normal
			this.pv = PV.LISTING;
			Tracker.getInstance()
			.pv(this.pv)
			.append(Key.SECONDCATENAME, categoryEnglishName)
			.end();
		}
		goodsListLoader.setCallback(this);
	}
	
	@Override
	public void onStackTop(boolean isBack) {
		super.onStackTop(isBack);
		if (goodsListLoader.getGoodsList().getData() != null && goodsListLoader.getGoodsList().getData().size() > 0)
		{
			VadListAdapter adapter = new VadListAdapter(getActivity(), goodsListLoader.getGoodsList().getData(), AdViewHistory.getInstance());
			lvGoodsList.setAdapter(adapter);
			updateData(adapter, goodsListLoader.getGoodsList().getData());
			lvGoodsList.setSelectionFromHeader(goodsListLoader.getSelection());
		}
		else
		{
			VadListAdapter adapter = new VadListAdapter(getActivity(), new ArrayList<Ad>(), AdViewHistory.getInstance());
			lvGoodsList.setAdapter(adapter);
//			goodsListLoader.startFetching(true, Communication.E_DATA_POLICY.E_DATA_POLICY_ONLY_LOCAL);
			if (!isBack){
				mRefreshUsingLocal = true;
				PerformanceTracker.stamp(Event.E_FireRefresh_OnShowup);
				lvGoodsList.fireRefresh();
			}
		}
		
		
		if (listFilterss == null)
		{
			Pair<Long, String> pair = Util.loadJsonAndTimestampFromLocate(
									getActivity(),
									"saveFilterss"
									+ categoryEnglishName
									+ GlobalDataManager.getInstance().getCityEnglishName());
			String json = pair.second;
			if (json != null && json.length() > 0 && (pair.first + (24 * 3600) >= System.currentTimeMillis()/1000))
			{
				listFilterss = JsonUtil.getFilters(json).getFilterssList();
			}
		}
		//Update filter bar.
		
		if (listFilterss == null)
		{
//			new Thread(new GetGoodsListThread(true)).start();
			executeGetAdsCommand();
		}
		else
		{
			showFilterBar(getView().findViewById(R.id.filter_bar_root), listFilterss);
		}
		
	}
	
	private void executeGetAdsCommand() {
		ApiParams params = new ApiParams();
		params.addParam("categoryEnglishName", categoryEnglishName);
		params.addParam("cityEnglishName", GlobalDataManager.getInstance().getCityEnglishName());
		
		BaseApiCommand.createCommand("category_meta_filter", true, params).execute(getActivity(), this);
	}
	
	private ApiParams getSearchParams()
	{
		ApiParams basicParams = new ApiParams();
		basicParams.addParam("query", filterParamHolder.toUrlString());
		// keyword 单独处理，放到ad_list的keyword参数里。
		String keyword = filterParamHolder.getData("");
		if (keyword != null && keyword.length() > 0)
		{
			basicParams.addParam("keyword", keyword);
		}
		
		if (isSerchNearBy() && this.curLocation != null)
		{
			basicParams.addParam("lat" , "" + curLocation.fLat);
			basicParams.addParam("lng" , "" + curLocation.fLon);	
		}
		
		return basicParams;
	}
	
	private boolean isSerchNearBy()
	{
		return PostParamsHolder.INVALID_VALUE.equals(this.filterParamHolder.getData("地区_s"));
	}

	@Override
	public View onInitializeView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		logCreateView(savedInstanceState);
		PerformanceTracker.stamp(Event.E_InitListingFragView_Begin);
		View v = inflater.inflate(R.layout.goodslist, null);
		
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

	
		curLocation = GlobalDataManager.getInstance().getLocationManager().getCurrentPosition(true);
		if(curLocation == null && isSerchNearBy()){
			filterParamHolder.remove("地区_s");
		}

		goodsListLoader.setParams(getSearchParams()); //= new GoodsListLoader(addParams, myHandler, null, new GoodsList());
		if(curLocation != null && /*searchType != SEARCH_RECENT*/ isSerchNearBy()){
//			goodsListLoader.setNearby(true);
			goodsListLoader.setSearchType(SEARCH_POLICY.SEARCH_NEARBY);
			goodsListLoader.setRuntime(true);
		}
		
		lvGoodsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int pos, long arg3) {
				int index = (int) arg3;//(int) (arg3 - lvGoodsList.getHeaderViewsCount());
				if(index < 0 || index > goodsListLoader.getGoodsList().getData().size() - 1)
					return;

                    Tracker.getInstance().event(BxEvent.LISTING_SELECTEDROWINDEX)
                            .append(Key.SELECTEDROWINDEX, index)
                            .end();
                    if(goodsListLoader.getGoodsList().getData().get(index) instanceof AdSeperator){
                    	return;
                    }
					Bundle bundle = createArguments(null, null);
					bundle.putSerializable("loader", goodsListLoader);
					bundle.putInt("index", index);
					pushFragment(new VadFragment(), bundle);
//				}				
			}
		});
		
		String categoryName = getArguments().getString("categoryName");
		if(categoryName == null || categoryName.equals("")){
			if(categoryEnglishName != null){
				categoryName = GlobalDataManager.getInstance().queryCategoryDisplayName(categoryEnglishName);
				if(categoryName != null) getArguments().putString("categoryName", categoryName);
			}
		}
		PerformanceTracker.stamp(Event.E_InitListingFragView_End);
		return v;
	
	}
	
	@Override
	public void onDestroy(){
		this.lvGoodsList = null;
//		this.adapter = null;
		AdList goodData = this.goodsListLoader.getGoodsList();
		this.goodsListLoader.reset();
		this.goodsListLoader = null;
		
//		if(goodData != null){
//			 List<Ad> list = goodData.getData();
//			 if(list != null){
//				 for(int i = 0; i < list.size(); ++ i){
//					 Ad gd = list.get(i);
//					 if(gd != null){
//						 ImageList il = gd.getImageList();
//						 if(il != null){
//							 if(il.getSquare() != null){
//								 String b = il.getSquare();
//								 if (b.contains(",")) {
//									String[] c = b.split(",");
//									if (c[0] != null && !c[0].equals("")) {
////										Log.d("ondestroy of getgoodsview", "hahahaha recycle in getgoodsview ondestroy");
//										ImageCacheManager.getInstance().forceRecycle(c[0], false);
////										Log.d("ondestroy of getgoodsview", "hahahaha end recycle in getgoodsview ondestroy");
//									}
//								 }
//							 }
//						 }
//					 }
//				 }
//			 }
//		}
//
//		ImageCacheManager.getInstance().postRecycle();
		super.onDestroy();
	}
	
	
	@Override
	public void onPause(){
		this.lvGoodsList.setOnScrollListener(null);
		super.onPause();

		for(int i = 0; i < lvGoodsList.getChildCount(); ++i){
			ImageView imageView = (ImageView)lvGoodsList.getChildAt(i).findViewById(R.id.ivInfo);
			
			if(	null != imageView){	
				if(null != imageView.getTag() && imageView.getTag().toString().length() > 0
				/*&& null != imageView.getDrawable()
				&& imageView.getDrawable() instanceof AnimationDrawable*/){
					GlobalDataManager.getInstance().getImageLoaderMgr().Cancel(imageView.getTag().toString(), imageView);
				}
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
			
			GlobalDataManager.getInstance().getImageLoaderMgr().AdjustPriority(urls);			
		}		
	}

	@Override
	public void onGetMore() {
//		goodsListLoader.startFetching(false, ((VadListLoader.E_LISTDATA_STATUS.E_LISTDATA_STATUS_ONLINE == goodsListLoader.getDataStatus()) ? 
//												Communication.E_DATA_POLICY.E_DATA_POLICY_NETWORK_CACHEABLE :
//												Communication.E_DATA_POLICY.E_DATA_POLICY_ONLY_LOCAL));
		goodsListLoader.startFetching(getAppContext(), false, VadListLoader.E_LISTDATA_STATUS.E_LISTDATA_STATUS_OFFLINE == goodsListLoader.getDataStatus());
	}

	@Override
	public void onRefresh() {
		PerformanceTracker.stamp(Event.E_Listing_StartFetching);
		goodsListLoader.startFetching(getAppContext(), true, mRefreshUsingLocal);
		mRefreshUsingLocal = false;
	}

	private void updateData(VadListAdapter adapter, List<Ad> data)
	{
		if(isSerchNearBy()){
			List<GroupItem> gi = FilterUtil.createDistanceGroupAndResortAds(data, this.curLocation, new int[] {500, 1500});
			adapter.setList(data, gi);
		}else{
			adapter.setList(data, FilterUtil.createFilterGroup(listFilterss, filterParamHolder, data));
		}
	}
	
	static private String getAroundCityName(List<Ad> ads){
		String ret = "";
		String preCityEnglishName = GlobalDataManager.getInstance().getCityEnglishName();
		String cityCombine = preCityEnglishName;
		List<CityDetail> cities = GlobalDataManager.getInstance().getListCityDetails();
		for(int i = 0; i < ads.size(); ++ i){
			String curCityEnglishName = ads.get(i).getValueByKey("cityEnglishName");
			if(!curCityEnglishName.equals(preCityEnglishName) && !cityCombine.contains(curCityEnglishName)){
				preCityEnglishName = ads.get(i).getValueByKey("cityEnglishName");
				cityCombine += " " + preCityEnglishName;
				for(int j = 0; j < cities.size(); ++ j){
					if(cities.get(j).englishName.equals(preCityEnglishName)){
						ret += cities.get(j).name + " ";
						break;
					}
				}
			}
		}
		return ret;
	}
	
	@Override
	protected void handleMessage(Message msg, Activity activity, View rootView) {

		switch (msg.what) {
		case VadListLoader.MSG_FIRST_FAIL:
			if(goodsListLoader == null) break;
			if(VadListLoader.E_LISTDATA_STATUS.E_LISTDATA_STATUS_OFFLINE == goodsListLoader.getRequestDataStatus())
				goodsListLoader.startFetching(getAppContext(), true, false);
			else{
				ViewUtil.showToast(getActivity(), "没有符合条件的结果，请重新输入！", true);
				hideProgress();
			}
			break;
		case VadListLoader.MSG_FINISH_GET_FIRST:
			PerformanceTracker.stamp(Event.E_Listing_Got_First);
			if(goodsListLoader == null) break;
			PerformanceTracker.stamp(Event.E_Listing_Start_ParseJson);
			AdList goodsList = JsonUtil.getGoodsListFromJson(goodsListLoader.getLastJson());
			PerformanceTracker.stamp(Event.E_Listing_End_ParseJson);
			boolean prevSearchAround = (goodsListLoader.getSearchType() == SEARCH_POLICY.SEARCH_AROUND);
			
			if(!prevSearchAround){
				goodsListLoader.setGoodsList(goodsList);
			}else{
				if(goodsList != null && goodsList.getData() != null && goodsList.getData().size() > 0){
					AdSeperator seperator = new AdSeperator();
					String cities = getAroundCityName(goodsList.getData());
					if(!TextUtils.isEmpty(cities)){
						cities = "附近城市:" + cities;
					}else{
						cities = "附近城市";
					}
					seperator.setValueByKey(EDATAKEYS.EDATAKEYS_TITLE, cities);
					
					AdList preList = goodsListLoader.getGoodsList();
					preList.getData().add(seperator);
					preList.getData().addAll(goodsList.getData());
//					preList.setCount(preList.getData().size());
					goodsListLoader.setGoodsList(preList);
				}
			}
			
			boolean needAroundAds = false;
			int newRows = 0;
			if (goodsListLoader.getGoodsList() == null || goodsListLoader.getGoodsList().getData() == null || goodsListLoader.getGoodsList().getData().size() == 0) {
				
				if(!prevSearchAround && !isSerchNearBy()){
					needAroundAds = true;
					newRows = goodsListLoader.getRows();
				}else{
					ErrorHandler.getInstance().handleError(ErrorHandler.ERROR_COMMON_FAILURE, "没有符合的结果，请更改条件并重试！");
				}

				VadListAdapter adapter = findGoodListAdapter();
				if (adapter != null)
				{
					adapter.setList(new ArrayList<Ad>());
					adapter.updateGroups(null);
					adapter.notifyDataSetChanged();
				}
			} else {
				if(!isSerchNearBy() && !prevSearchAround && goodsList.getData().size() < goodsListLoader.getRows()){
					needAroundAds = true;
					newRows = goodsListLoader.getRows() - goodsList.getData().size();
				}else{
				//QuanleimuApplication.getApplication().setListGoods(goodsListLoader.getGoodsList().getData());
					VadListAdapter adapter = new VadListAdapter(getActivity(), goodsListLoader.getGoodsList().getData(), AdViewHistory.getInstance());
					updateData(adapter, goodsListLoader.getGoodsList().getData());
					lvGoodsList.setAdapter(adapter);
					goodsListLoader.setHasMore(true);
				}
			}
			
			if(needAroundAds){
				goodsListLoader.setSearchType(SEARCH_POLICY.SEARCH_AROUND);
				goodsListLoader.setRows(newRows);
				goodsListLoader.startFetching(getAppContext(), true, mRefreshUsingLocal);
				break;
			}
			
			lvGoodsList.onRefreshComplete();
			
			//if currently using offline data, start fetching online data
			if(VadListLoader.E_LISTDATA_STATUS.E_LISTDATA_STATUS_OFFLINE == goodsListLoader.getDataStatus()) {
                lvGoodsList.fireRefresh();
            } else { //非缓存情况下才加此 log
    			HashMap<String, String> tmp = new HashMap<String, String>();
    			Iterator<String> ite = filterParamHolder.keyIterator();
    			while(ite.hasNext()){
    				String key = ite.next();
    				tmp.put(key, filterParamHolder.getData(key));
    			}
                if (tmp.containsKey("")) {
                	tmp.put(Key.LISTINGFILTERKEYWORD.getName(), tmp.get(""));
                	tmp.remove("");
                }

                Tracker.getInstance().event(BxEvent.LISTING)
                        .append(Key.SEARCHKEYWORD, searchContent)
                        .append(tmp)
                        .append(Key.TOTAL_ADSCOUNT, goodsListLoader.getGoodsList().getData().size())
                        .end();
            }

				//goodsListLoader.startFetching(true, Communication.E_DATA_POLICY.E_DATA_POLICY_NETWORK_CACHEABLE);

			hideProgress();
			PerformanceTracker.stamp(Event.E_Listing_Got_First_Leave);
			PerformanceTracker.flush();
			
			if(goodsListLoader.getSearchType() == SEARCH_POLICY.SEARCH_AROUND){
				boolean nearby = this.isSerchNearBy();
				goodsListLoader.setSearchType(nearby ? SEARCH_POLICY.SEARCH_NEARBY : SEARCH_POLICY.SEARCH_LISTING);
				goodsListLoader.setRows(-1);
//				VadListAdapter adapter = findGoodListAdapter();
//				adapter.updateGroups(null);
			}

			break;
		case VadListLoader.MSG_NO_MORE:
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
		case VadListLoader.MSG_FINISH_GET_MORE:
			if(goodsListLoader == null) break;
			progressBar.setVisibility(View.GONE);
			
			AdList moreGoodsList = JsonUtil.getGoodsListFromJson(goodsListLoader.getLastJson());
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
				List<Ad> listCommonGoods =  moreGoodsList.getData();
				if(goodsListLoader.getGoodsList().getData() == null){
					goodsListLoader.getGoodsList().setData(listCommonGoods);
				}else{
					goodsListLoader.getGoodsList().getData().addAll(listCommonGoods);
				}
				//QuanleimuApplication.getApplication().setListGoods(goodsListLoader.getGoodsList().getData());
				
				VadListAdapter adapter = findGoodListAdapter();
				if (adapter != null)
				{
					updateData(adapter, goodsListLoader.getGoodsList().getData());
					adapter.notifyDataSetChanged();		
				}
				
				lvGoodsList.onGetMoreCompleted(PullToRefreshListView.E_GETMORE.E_GETMORE_OK);
				goodsListLoader.setHasMore(true);
			}

			HashMap<String, String> tmp = new HashMap<String, String>();
			Iterator<String> ite = filterParamHolder.keyIterator();
			while(ite.hasNext()){
				String key = ite.next();
				tmp.put(key, filterParamHolder.getData(key));
			}
            if (tmp.containsKey("")) {
            	tmp.put(Key.LISTINGFILTERKEYWORD.getName(), tmp.get(""));
            	tmp.remove("");
            }

            Tracker.getInstance().event(BxEvent.LISTING_MORE)
                    .append(Key.SEARCHKEYWORD, searchContent)
                    .append(tmp)
                    .append(Key.TOTAL_ADSCOUNT, goodsListLoader.getGoodsList().getData().size())
                    .end();
			
			hideProgress();			
			break;
		case ErrorHandler.ERROR_COMMON_FAILURE:
		case ErrorHandler.ERROR_NETWORK_UNAVAILABLE:
			if(goodsListLoader == null) break;
			progressBar.setVisibility(View.GONE);

			if(msg.what == ErrorHandler.ERROR_COMMON_FAILURE && msg.obj != null){
				ViewUtil.showToast(getActivity(), (String)msg.obj, false);
			}else{
				ErrorHandler.getInstance().handleError(ErrorHandler.ERROR_NETWORK_UNAVAILABLE, null);
			}
			
			lvGoodsList.onFail();
			
			hideProgress();			
			break;
		case MSG_UPDATE_FILTER:
			showFilterBar(rootView, listFilterss);
			break;
		}
		
	}
	
	private boolean isCurrentCity() {
		String geoCity = GlobalDataManager.getInstance().getLocationManager().getCurrentCity();
		String selectCity = GlobalDataManager.getInstance().getCityName();
		
		return geoCity != null && geoCity.contains(selectCity);
	}
	
	public void showFilterBar(View root, final List<Filterss> fss)
	{
		View[] actionViews = findAllFilterView();
		
		if (actionViews == null)
		{
			return;
		}
		
		
		View.OnClickListener listener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
//				if (v.getId() == R.id.filter_item_single)
//				{
//					if(categoryEnglishName == null || categoryEnglishName.equals("")){
//						pushAndFinish(new SearchFragment(), createArguments(null, null));
//					}else{
//						Bundle args = createArguments(null, getArguments().getString(ARG_COMMON_BACK_HINT));
//						args.putAll(getArguments());
//				//		bundle.putString("backPageName", bundle.getString("backPageName"));
//						args.putInt(ARG_COMMON_REQ_CODE, REQ_SIFT);
//						args.putString("searchType", "goodslist");
//						args.putString("categoryEnglishName", categoryEnglishName);
//				
//						pushFragment(new FilterFragment(), args);
//					}
//				}
//				else
				{
					final Filterss fss = (Filterss) v.getTag();
					final boolean isLocation = fss.getName().equals("地区_s");
					
					MultiLevelItem cItem = new MultiLevelItem();
					cItem.id = PostParamsHolder.INVALID_VALUE; //FIXME:  this is special for current location.
					cItem.txt = "附近500米";
					
					MultiLevelItem[] cs = isLocation && curLocation != null && isCurrentCity() ? new MultiLevelItem[] {cItem} : null; 

					Tracker.getInstance().event(BxEvent.LISTING_TOPFILTEROPEN)
							.append(Key.SECONDCATENAME, categoryEnglishName)
							.append(Key.FILTERNAME, fss.getDisplayName())
							.end();

					FilterUtil.startSelect(getActivity(), cs, fss, new FilterSelectListener() {
						
						@Override
						public void onItemSelect(MultiLevelItem item) {
							Tracker.getInstance().event(BxEvent.LISTING_TOPFILTERSUBMIT)
									.append(Key.SECONDCATENAME, categoryEnglishName)
									.append(Key.FILTERNAME, fss.getDisplayName())
									.append(Key.FILTERVALUE, item.txt)
									.end();
							FilterUtil.updateFilter(filterParamHolder, item, fss.getName());
							
							if (filterParamHolder.containsKey(fss.getName()))
							{
								FilterUtil.updateFilterLabel(findAllFilterView(), item.txt, fss);
							}
							else
							{
								FilterUtil.updateFilterLabel(findAllFilterView(), fss.getDisplayName(), fss);
							}
//							updateSearchParams();
							
							resetSearch(curLocation);
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
		
		View fItemParent = getView().findViewById(R.id.filter_parent);
//		View singleFilter = getView().findViewById(R.id.filter_item_single);
		if (fss != null)
		{
			FilterUtil.loadFilterBar(fss, filterParamHolder, actionViews);
//			singleFilter.setVisibility(View.GONE);
			fItemParent.setVisibility(View.VISIBLE);
		}
		else
		{
			for (View view : actionViews)
			{
				view.setVisibility(View.GONE);
			}
//			singleFilter.setVisibility(View.VISIBLE);
			fItemParent.setVisibility(View.GONE);
		}
		
//		singleFilter.setOnClickListener(listener);
		
		View filterParent =getView() == null ? null : getView().findViewById(R.id.filter_bar_root);
		if (filterParent != null)
		{
			filterParent.setVisibility(fss != null ? View.VISIBLE : View.GONE);
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
	
	private void resetSearch(BXLocation location)
	{
		boolean isNeryBy = this.isSerchNearBy();
		
		goodsListLoader.cancelFetching();
		if (!isNeryBy)
		{
			goodsListLoader.setSearchType(SEARCH_POLICY.SEARCH_LISTING);
			goodsListLoader.setParams(getSearchParams());
			goodsListLoader.setRuntime(true);
		}
		else
		{
			goodsListLoader.setSearchType(SEARCH_POLICY.SEARCH_NEARBY);
			goodsListLoader.setParams(getSearchParams());
			goodsListLoader.setRuntime(true);
		}
	}

	@Override
	public void onRequestComplete(int respCode, Object data) {
		this.sendMessage(respCode, data);
	}

	@Override
	public void onNetworkDone(String apiName, String responseData) {

		if ("category_meta_filter".equals(apiName) && responseData != null) {
			Util.saveJsonAndTimestampToLocate(getAppContext(), "saveFilterss"
					+ categoryEnglishName
					+ GlobalDataManager.getInstance().getCityEnglishName(),
					responseData, System.currentTimeMillis() / 1000);

			listFilterss = JsonUtil.getFilters(responseData).getFilterssList();
			sendMessage(MSG_UPDATE_FILTER, responseData);
		}
	}

	@Override
	public void onNetworkFail(String apiName, ApiError error) {
		
	}

}