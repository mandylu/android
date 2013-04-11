package com.baixing.view.fragment;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baixing.activity.BaseFragment;
import com.baixing.adapter.CommonItemAdapter;
import com.baixing.data.GlobalDataManager;
import com.baixing.tracking.TrackConfig.TrackMobile.BxEvent;
import com.baixing.tracking.TrackConfig.TrackMobile.Key;
import com.baixing.tracking.TrackConfig.TrackMobile.PV;
import com.baixing.tracking.Tracker;
import com.baixing.util.Util;
import com.baixing.util.ViewUtil;
import com.quanleimu.activity.R;

public class KeywordSelectFragment extends BaseFragment {
	private EditText etSearch;
	private List<String> listRemark = new ArrayList<String>();
	private TextView tvClear;
	private ListView lvSearchHistory;
	
	public KeywordSelectFragment() {
		this.defaultEnterAnim = R.anim.zoom_enter;
		this.defaultExitAnim = R.anim.zoom_exit;
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		listRemark = GlobalDataManager.getInstance().getListRemark();
	}

	protected void initTitle(TitleDef title) {
		title.m_visible = true;
		title.m_titleControls = LayoutInflater.from(getActivity()).inflate(
				R.layout.title_search, null);
		etSearch = (EditText) title.m_titleControls.findViewById(R.id.etSearch);
		title.m_leftActionHint = "返回";
		
		title.m_rightActionHint = "搜索";
		etSearch.setOnKeyListener(new View.OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) 
				{
					String searchContent = etSearch.getText().toString().trim();
					if (searchContent.equals("")) {
						ViewUtil.showToast(getActivity(), "搜索内容不能为空", false);
					} else {
						finishFragment(fragmentRequestCode, searchContent);
					}
					return true;
				} 
				else
				{
					return false;
				}
			}
		});
		
		etSearch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (listRemark != null && listRemark.size() != 0) {
					
					CommonItemAdapter adapter = new CommonItemAdapter(getActivity(),
							listRemark, 0x1FFFFFFF, false);
					adapter.setHasArrow(false);
					lvSearchHistory.setAdapter(adapter);
				}
			}
		});;
		
	}
	
	/* (non-Javadoc)
	 * @see com.quanleimu.activity.BaseFragment#handleBack()
	 */
	@Override
	public boolean handleBack() {
		finishFragment(fragmentRequestCode, null);
		return true;
	}

	
	public View onInitializeView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		ViewGroup rootV = (ViewGroup) inflater.inflate(R.layout.keyword_selector, null);

		// 通过ID获取控件
		lvSearchHistory = (ListView) rootV.findViewById(R.id.lvSearchHistory);

		// 添加自定义布局
//		LinearLayout layout = new LinearLayout(getActivity());
//		layout.setOrientation(LinearLayout.HORIZONTAL);
		
		tvClear = (TextView) inflater.inflate(R.layout.button_clear, null);
		tvClear.setHeight(getResources().getDimensionPixelSize(R.dimen.tab_height));
		lvSearchHistory.addFooterView(tvClear);
		
//		tvClear.setTextSize(22);
//		tvClear.setText("清除历史记录");
//		tvClear.setGravity(Gravity.CENTER_VERTICAL);
//		layout.setGravity(Gravity.CENTER);

		lvSearchHistory.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				if (arg2 <= (listRemark.size() - 1)) {
					String searchContent = listRemark.get(arg2);
					etSearch.setText(searchContent);
					etSearch.setSelection(searchContent.length(), searchContent.length());
					finishFragment(fragmentRequestCode, searchContent);
				} else {
					listRemark.clear();
					GlobalDataManager.getInstance().updateRemark((String[])null);
					lvSearchHistory.setVisibility(View.GONE);
					tvClear.setVisibility(View.GONE);
				}
			}
		});		
		
		tvClear.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				listRemark.clear();
				GlobalDataManager.getInstance().updateRemark(listRemark.toArray(new String[listRemark.size()]));
				lvSearchHistory.setVisibility(View.GONE);
				v.setVisibility(View.GONE);

				// 将搜索记录保存本地
				Util.saveDataToLocate(getActivity(), "listRemark", listRemark);
				
				ViewUtil.postShortToastMessage(getView(), R.string.tip_history_cleared, 0);
			}
		});

		listRemark = GlobalDataManager.getInstance().getListRemark();
		return rootV;
	}
	
	private void showSearchHistory() {
		
		if (listRemark != null && listRemark.size() != 0) {
			lvSearchHistory.setVisibility(View.VISIBLE);
			tvClear.setVisibility(View.VISIBLE);
			
			CommonItemAdapter adapter = new CommonItemAdapter(getActivity(),
					listRemark, 0x1FFFFFFF, false);
			adapter.setHasArrow(false);
			lvSearchHistory.setAdapter(adapter);
		}
		else
		{
			tvClear.setVisibility(View.GONE);
		}
	}
	
	public void onStackTop(boolean isBack) {
		Log.d("ooo","keywordselector->onstacktop");
		this.showSearchHistory();
		etSearch.postDelayed(new Runnable(){
			@Override
			public void run(){
				if (etSearch != null)
				{
					etSearch.requestFocus();
					InputMethodManager inputMgr = 
							(InputMethodManager) etSearch.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
					inputMgr.showSoftInput(etSearch, InputMethodManager.SHOW_IMPLICIT);
				}
			}			
		}, 100);
		
	}
	
	@Override
	public void onPause() {
		InputMethodManager inputMgr = (InputMethodManager) getActivity()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMgr.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);

		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		Tracker.getInstance().pv(PV.SEARCH).end();
		Log.d("ooo","keywordselector->onresume");
		etSearch.postDelayed(new Runnable() {
			@Override
			public void run() {
				etSearch.requestFocus();
			}
		}, 300);
	}
	
	private void doSearch() {
		
		Log.d("ooo","keywordselector->dosearch");
		String searchContent = etSearch.getText().toString().trim();
		Tracker.getInstance().event(BxEvent.HEADERSEARCHRESULT)
		.append(Key.SEARCHKEYWORD, searchContent)
		.end();
		
		if (searchContent.equals("")) {
			ViewUtil.showToast(getActivity(), "搜索内容不能为空", false);
		} else {
			
			addToListRemark(searchContent);
			finishFragment(fragmentRequestCode, searchContent);
		}
	}
	
	public void handleRightAction() {
		this.doSearch();
	}
	
	/**
	 * @param searchContent
	 * 
	 */
	private void addToListRemark(String searchContent) {
		if (listRemark == null || listRemark.size() == 0) {
			listRemark = new ArrayList<String>();
//			listRemark.add(searchContent);
		}
		listRemark.remove(searchContent);
		listRemark.add(0, searchContent);
		GlobalDataManager.getInstance().updateRemark(listRemark);
		// 将搜索记录保存本地
		Util.saveDataToLocate(getActivity(), "listRemark", listRemark);
	}
}
