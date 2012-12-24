package com.baixing.view.fragment;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baixing.activity.BaseFragment;
import com.baixing.activity.GlobalDataManager;
import com.baixing.entity.Category;
import com.baixing.jsonutil.JsonUtil;
import com.baixing.tracking.TrackConfig.TrackMobile.BxEvent;
import com.baixing.tracking.TrackConfig.TrackMobile.Key;
import com.baixing.tracking.TrackConfig.TrackMobile.PV;
import com.baixing.tracking.Tracker;
import com.baixing.util.Communication;
import com.baixing.util.ViewUtil;
import com.quanleimu.activity.R;

public class SearchFragment extends BaseFragment {

//	private static final int REQ_SELECT_SEARCH_CATEGORY = 147;
	private static final int MSG_SEARCH_RESULT = 1;
	private static final int MSG_START_SERACH = 2;

//	private EditText etSearch;
//	private TextView tvClear;
//	private ListView lvSearchHistory;
	private View searchResult;
	private ListView lvSearchResultList;
	private View noResultView;
	private View loadingView;
	
	private static final  int REQ_GETKEYWORD = 1;

	private Runnable searachJob;
	

	/**
	 * 设置布局显示为目标有多大就多大
	 */
	private LayoutParams WClayoutParams = new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.WRAP_CONTENT,
			LinearLayout.LayoutParams.WRAP_CONTENT);
	/**
	 * 设置布局显示目标最大化
	 */
	private LayoutParams FFlayoutParams = new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.FILL_PARENT,
			LinearLayout.LayoutParams.FILL_PARENT);

	public String searchContent = "";

	List<Pair<Category, Integer>> categoryResultCountList;

//	private List<String> listRemark = new ArrayList<String>();

	protected void initTitle(TitleDef title) {
		title.m_visible = true;
		title.m_title = searchContent == null ? "" : searchContent;
		title.m_leftActionHint = "返回";
		title.hasGlobalSearch = true;
	}
	
	
	

	@Override
	public void handleSearch() {
		Bundle args = createArguments(null, null);
		args.putInt(ARG_COMMON_REQ_CODE, REQ_GETKEYWORD);
		pushFragment(new KeywordSelector(), args);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootV = inflater.inflate(R.layout.search, null);

		rootV.setLayoutParams(FFlayoutParams);
		
		// 通过ID获取控件
		lvSearchResultList = (ListView) rootV
				.findViewById(R.id.lvSearchResultList);
		searchResult = rootV.findViewById(R.id.searchResult);
		noResultView = rootV.findViewById(R.id.noResultView);
		loadingView = rootV.findViewById(R.id.searching_parent);
		// 添加自定义布局
		LinearLayout layout = new LinearLayout(getActivity());
		layout.setOrientation(LinearLayout.HORIZONTAL);
		layout.setGravity(Gravity.CENTER);

		LinearLayout loadingLayout = new LinearLayout(getActivity());
		loadingLayout.addView(layout, WClayoutParams);
		loadingLayout.setGravity(Gravity.CENTER);
		
		lvSearchResultList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Category cate = (Category) arg1.getTag();

				final Bundle bundle = new Bundle();
				bundle.putString("searchContent", searchContent);
				bundle.putString("actType", "search");
				bundle.putString("name", "");
				bundle.putString("categoryEnglishName", cate.getEnglishName());
				
				
				if (!GlobalDataManager.isTextMode() && GlobalDataManager.needNotifySwitchMode() && !Communication.isWifiConnection())
				{
					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					builder.setTitle(R.string.dialog_title_info)
					.setMessage(R.string.label_warning_flow_optimize)
					.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							GlobalDataManager.setTextMode(false);
							pushFragment(new GetGoodFragment(), bundle);
						}
					})
					.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							GlobalDataManager.setTextMode(true);
							ViewUtil.postShortToastMessage(getView(), R.string.label_warning_switch_succed, 100);
							pushFragment(new GetGoodFragment(), bundle);
						}
						
					}).create().show();
				}
				else
				{
					pushFragment(new GetGoodFragment(), bundle);
				}
				
			}
		});
		
		return rootV;
	}
	
	@Override
	public void onStackTop(boolean isBack) {
		if (isBack)
		{
			this.showSearchResult(false);
		}
		else if (null == searchContent || searchContent.length() == 0) {
			handleSearch();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();

		if (searchContent.equals(""))
			Tracker.getInstance().pv(PV.SEARCH).end();
	}

	@Override
	protected void onFragmentBackWithData(int requestCode, Object result) {
		if (requestCode == REQ_GETKEYWORD) {
			if (result == null && (categoryResultCountList == null || categoryResultCountList.size() == 0))
			{
				finishFragment();
			}
			else if (result == null)
			{
				showSearchResult(false);
			}
			else
			{
				categoryResultCountList = null;
				this.searchContent = (String) result;
				getTitleDef().m_title = this.searchContent;
				refreshHeader();
				
				showSearchResult(true);
			}
		}
	}


	/**
	 * 
	 */
	private void showSearchResult(boolean search) {
		this.pv = PV.SEARCHRESULTCATEGORY;
		Tracker.getInstance().pv(PV.SEARCHRESULTCATEGORY).append(Key.SEARCHKEYWORD, searchContent).end();

		this.hideSoftKeyboard();
		if (search)
		{
//			this.showProgress("消息", "搜索中...", true);
			this.sendMessage(MSG_START_SERACH, null);
			new Thread(new SearchCategoryListThread()).start();
		}
		else
		{
			loadingView.setVisibility(View.GONE);
			this.sendMessage(MSG_SEARCH_RESULT, null);
		}
	}

	@Override
	protected void handleMessage(Message msg, Activity activity, View rootView) {
		switch (msg.what) {
		case MSG_START_SERACH:
			noResultView.setVisibility(View.GONE);
			loadingView.setVisibility(View.VISIBLE);
			break;
		case MSG_SEARCH_RESULT:

			
			int resultCatesCount = 0;
			int totalAdsCount = 0;
			int maxAdsCount = 0;
			loadingView.setVisibility(View.GONE);
			
			if (categoryResultCountList == null || categoryResultCountList.isEmpty())
			{
				noResultView.setVisibility(View.VISIBLE);
				searchResult.setVisibility(View.GONE);
			}
			else
			{
				searchResult.setVisibility(View.VISIBLE);
				noResultView.setVisibility(View.GONE);

				ResultListAdapter adapter = new ResultListAdapter(activity,
						R.layout.item_categorysearch, categoryResultCountList);
				lvSearchResultList.setAdapter(adapter);
				
				resultCatesCount = categoryResultCountList.size();
				for (Pair<Category, Integer> pair : categoryResultCountList)
				{
					totalAdsCount += pair.second;
					maxAdsCount = Math.max(maxAdsCount, pair.second);
				}
			}
			
			Tracker.getInstance().event(BxEvent.HEADERSEARCHRESULT)
					.append(Key.SEARCHKEYWORD, searchContent)
					.append(Key.RESULTCATESCOUNT, ""+resultCatesCount)
					.append(Key.TOTAL_ADSCOUNT, ""+totalAdsCount)
					.append(Key.MAXCATE_ADSCOUNT, ""+maxAdsCount).end();
			
			this.hideProgress();
			break;
		}
	}
	
	

	class SearchCategoryListThread implements Runnable {
		@Override
		public void run() {
			String apiName = "ad_search";
			List<String> parameters = new ArrayList<String>();
			parameters.add("query="
					+ URLEncoder.encode(SearchFragment.this.searchContent));
		    parameters.add("cityEnglishName=" + URLEncoder.encode(GlobalDataManager.getApplication().getCityEnglishName()));
			String apiUrl = Communication.getApiUrl(apiName, parameters);
			try {
				String json = Communication.getDataByUrl(apiUrl, true);
				categoryResultCountList = JsonUtil
						.parseAdSearchCategoryCountResult(json);
			} catch (Exception e) {
				categoryResultCountList = null;
				Log.e(TAG, e.getMessage()==null?"网络请求失败":e.getMessage());
				getActivity().runOnUiThread(new Runnable(){
					@Override
					public void run(){
						Toast.makeText(getActivity(), "网络请求失败,请稍后重试",
								Toast.LENGTH_SHORT).show();
					}
				});
			}
			SearchFragment.this.sendMessage(MSG_SEARCH_RESULT,
					categoryResultCountList);			
		}
	}

	class ResultListAdapter extends ArrayAdapter<Pair<Category, Integer>> {
		List<Pair<Category, Integer>> objects;

		public ResultListAdapter(Context context, int textViewResourceId,
				List<Pair<Category, Integer>> objects) {
			super(context, textViewResourceId, objects);
			this.objects = objects;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(
						SearchFragment.this.getActivity()).inflate(
						R.layout.item_categorysearch, null);
			}

			TextView tvCategoryName = (TextView) convertView
					.findViewById(R.id.tvCategoryName);
			TextView tvCategoryCount = (TextView) convertView
					.findViewById(R.id.tvCategoryCount);
			Pair<Category, Integer> pair = objects.get(position);
			convertView.setTag(pair.first);
			tvCategoryName.setText(pair.first.getName());
			tvCategoryCount.setText(String.format("(%d)",
					pair.second.intValue()));

			return convertView;
		}
	}
	
	public int getEnterAnimation()
	{
		return getArguments() == null ? R.anim.zoom_enter : getArguments().getInt(ARG_COMMON_ANIMATION_IN, R.anim.zoom_enter);
	}
	
	public int getExitAnimation()
	{
		return getArguments() == null ? R.anim.zoom_exit : getArguments().getInt(ARG_COMMON_ANIMATION_EXIT, R.anim.zoom_exit);
	}

}
