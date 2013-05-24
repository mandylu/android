package com.baixing.view.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.baixing.activity.BaseFragment;
import com.baixing.adapter.SecondCategoryAdapter;
import com.baixing.adapter.SecondCategoryAdapter.SecondCategoryInfo;
import com.baixing.data.GlobalDataManager;
import com.baixing.entity.Category;
import com.baixing.network.NetworkUtil;
import com.baixing.tracking.TrackConfig.TrackMobile.BxEvent;
import com.baixing.tracking.TrackConfig.TrackMobile.Key;
import com.baixing.tracking.TrackConfig.TrackMobile.PV;
import com.baixing.tracking.TrackConfig.TrackMobile.Value;
import com.baixing.tracking.Tracker;
import com.baixing.util.PerformEvent.Event;
import com.baixing.util.PerformanceTracker;
import com.baixing.util.Util;
import com.baixing.util.ViewUtil;
import com.chencang.core.R;

public class SecondCateFragment extends BaseFragment implements OnItemClickListener{
	
	private boolean isPost = false;
	private Category cate = null;
	
	public static final String BROADCAST_SEL_CATEGORY_SUCCEED = "broadcast_sel_category_succeed";
	public static final int MSG_SEL_CATEGORY_SUCCEED = 0xff121234;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		isPost = getArguments().getBoolean("isPost", false);
		cate = (Category) getArguments().getSerializable("cates");
	}
	
	

	@Override
	public View onInitializeView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v =  inflater.inflate(R.layout.secondcate, null);
		v.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		
		GridView gridView = (GridView) v.findViewById(R.id.gridSecCategory);
		
		List<Category> children = cate.getChildren();
		List<SecondCategoryInfo> infoList = new ArrayList<SecondCategoryInfo>();
		for (Category cate : children) 
		{
			SecondCategoryInfo info = new SecondCategoryInfo();
			info.name = cate.getName();
			info.selected = cate.getEnglishName().equals(GlobalDataManager.getInstance().getCategoryEnglishName());
			infoList.add(info);
		}
		
		SecondCategoryAdapter adapter = new SecondCategoryAdapter(this.getActivity());
		adapter.setList(infoList);
		gridView.setAdapter(adapter);
		gridView.setOnItemClickListener(this);
		return v;
	}

	@Override
	public void onResume(){
		super.onResume();
//		Log.d("secondcatefragment","secondcatefragment->isPost"+isPost);
		if (!isPost) {//post
			this.pv = PV.CATEGORIES;
			Tracker.getInstance().pv(PV.CATEGORIES).append(Key.FIRSTCATENAME, cate.getEnglishName()).end();
		}else {
//			this.pv = PV.POSTCATE2;
//			Tracker.getInstance().pv(PV.POSTCATE2).end();
		}
//		getView().findViewById(R.id.gridSecCategory).requestFocus();
	}
	
	@Override
	public void onStackTop(boolean isBack) {
		super.onStackTop(isBack);
	}



	protected void onFragmentBackWithData(int requestCode, Object result){
	}
	
	private void onCategorySelected(Category secCate){
		GlobalDataManager.getInstance().setCategoryEnglishName(secCate.getEnglishName());
		GlobalDataManager.getInstance().setCategoryName(secCate.getName());
//		finishFragment(MSG_SEL_CATEGORY_SUCCEED, secCate.getEnglishName() + "," + secCate.getName());
		Util.saveDataToLocate(getAppContext(), "lastCategory", secCate.getEnglishName() + "," + secCate.getName());
		Intent intent = new Intent(BROADCAST_SEL_CATEGORY_SUCCEED);
		getActivity().sendBroadcast(intent);		
	}
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if (cate == null || cate.getChildren() == null
				|| cate.getChildren().size() <= arg2)
			return;
		PerformanceTracker.stamp(Event.E_Category_Clicked);
		final Category secCate = cate.getChildren().get(arg2);
		if (!isPost) {
			final Bundle bundle = createArguments(secCate.getName(), "返回");
			bundle.putString("categoryEnglishName", secCate.getEnglishName());
			bundle.putString("siftresult", "");
			if (fragmentRequestCode != INVALID_REQUEST_CODE) {
				String toRet = secCate.getEnglishName() + "," + secCate.getName();
				finishFragment(fragmentRequestCode, toRet);
			} else {
				bundle.putString("categoryName", secCate.getName());
				if (!GlobalDataManager.isTextMode() && GlobalDataManager.needNotifySwitchMode() && !NetworkUtil.isWifiConnection(arg0.getContext()))
				{
					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					builder.setTitle(R.string.dialog_title_info)
					.setMessage(R.string.label_warning_flow_optimize)
					.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Tracker.getInstance().event(BxEvent.BROWSEMODENOIMAGE).append(Key.RESULT, Value.NO).end();
							GlobalDataManager.setTextMode(false);
							PerformanceTracker.stamp(Event.E_Start_ListingFragment);
							onCategorySelected(secCate);
						}
					})
					.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							Tracker.getInstance().event(BxEvent.BROWSEMODENOIMAGE).append(Key.RESULT, Value.YES).end();
							GlobalDataManager.setTextMode(true);
							ViewUtil.postShortToastMessage(getView(), R.string.label_warning_switch_succed, 100);
							PerformanceTracker.stamp(Event.E_Start_ListingFragment);
							onCategorySelected(secCate);
						}
						
					}).create().show();
				}
				else
				{
					PerformanceTracker.stamp(Event.E_Start_ListingFragment);
					onCategorySelected(secCate);
				}
			}
		} else {
			String names = secCate.getEnglishName() + "," + secCate.getName();
			if (fragmentRequestCode != INVALID_REQUEST_CODE) {
				finishFragment(fragmentRequestCode, names);
			} else {
				Bundle bundle = createArguments(null, null);
				bundle.putSerializable("cateNames", names);
//				pushFragment(new PostGoodsFragment(), bundle);
				// m_viewInfoListener.onNewView(new
				// PostGoodsView((BaseActivity)getContext(), bundle,
				// names));//FIXME:
			}
		}
	}

	@Override
	public void initTitle(TitleDef title){
		title.m_visible = true;
		title.m_title = cate.getName();
		title.m_leftActionHint = "返回";
		if (isPost)
		{
			title.hasGlobalSearch = false;
			title.m_rightActionHint = null;
		}
		else
		{
//			title.hasGlobalSearch = true;
//			title.m_rightActionHint = "发布";
//			title.m_rightActionBg = R.drawable.bg_post_selector;
		}
	}
	
	@Override
	public void handleSearch() {
		Bundle bundle = new Bundle();
		bundle.putString("categoryEnglishName", this.cate.getEnglishName());
		bundle.putString("categoryName", this.cate.getName());
//		this.pushFragment(new SearchFragment(), bundle);
	}

}