package com.baixing.view.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.SimpleAdapter;

import com.baixing.activity.BaseFragment;
import com.baixing.data.GlobalDataManager;
import com.baixing.entity.Category;
import com.baixing.tracking.TrackConfig.TrackMobile.BxEvent;
import com.baixing.tracking.TrackConfig.TrackMobile.Key;
import com.baixing.tracking.TrackConfig.TrackMobile.PV;
import com.baixing.tracking.TrackConfig.TrackMobile.Value;
import com.baixing.tracking.Tracker;
import com.baixing.util.Communication;
import com.baixing.util.PerformEvent.Event;
import com.baixing.util.PerformanceTracker;
import com.baixing.util.ViewUtil;
import com.quanleimu.activity.R;

public class SecondCateFragment extends BaseFragment implements OnItemClickListener{
	
	private boolean isPost = false;
	private Category cate = null;
	
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
		v.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		
		GridView gridView = (GridView) v.findViewById(R.id.gridSecCategory);
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		List<Category> children = cate.getChildren();
		for (Category cate : children)
		{
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("tvCategoryName", cate.getName());
			list.add(map);
		}
		
		SimpleAdapter adapter = new SimpleAdapter(getActivity(), list, R.layout.item_seccategory, 
				new String[]{"tvCategoryName"}, new int[]{R.id.tvCategoryName});
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
		if(PostGoodsFragment.MSG_POST_SUCCEED == requestCode){
			this.finishFragment(requestCode, result);
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if (cate == null || cate.getChildren() == null
				|| cate.getChildren().size() <= arg2)
			return;
		PerformanceTracker.stamp(Event.E_Category_Clicked);
		Category secCate = cate.getChildren().get(arg2);
		if (!isPost) {
			final Bundle bundle = createArguments(secCate.getName(), "返回");
			bundle.putString("categoryEnglishName", secCate.getEnglishName());
			bundle.putString("siftresult", "");
			if (fragmentRequestCode != INVALID_REQUEST_CODE) {
				String toRet = secCate.getEnglishName() + "," + secCate.getName();
				finishFragment(fragmentRequestCode, toRet);
			} else {
				bundle.putString("categoryName", secCate.getName());
				if (!GlobalDataManager.isTextMode() && GlobalDataManager.needNotifySwitchMode() && !Communication.isWifiConnection())
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
							pushFragment(new ListingFragment(), bundle);
						}
					})
					.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							Tracker.getInstance().event(BxEvent.BROWSEMODENOIMAGE).append(Key.RESULT, Value.YES).end();
							GlobalDataManager.setTextMode(true);
							ViewUtil.postShortToastMessage(getView(), R.string.label_warning_switch_succed, 100);
							PerformanceTracker.stamp(Event.E_Start_ListingFragment);
							pushFragment(new ListingFragment(), bundle);
						}
						
					}).create().show();
				}
				else
				{
					PerformanceTracker.stamp(Event.E_Start_ListingFragment);
					pushFragment(new ListingFragment(), bundle);
				}
			}
		} else {
			String names = secCate.getEnglishName() + "," + secCate.getName();
			if (fragmentRequestCode != INVALID_REQUEST_CODE) {
				finishFragment(fragmentRequestCode, names);
			} else {
				Bundle bundle = createArguments(null, null);
				bundle.putSerializable("cateNames", names);
				pushFragment(new PostGoodsFragment(), bundle);
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
			title.hasGlobalSearch = true;
//			title.m_rightActionHint = "发布";
//			title.m_rightActionBg = R.drawable.bg_post_selector;
		}
	}
	
	@Override
	public void handleSearch() {
		Bundle bundle = new Bundle();
		bundle.putString("categoryEnglishName", this.cate.getEnglishName());
		bundle.putString("categoryName", this.cate.getName());
		this.pushFragment(new SearchFragment(), bundle);
	}

}