package com.quanleimu.view.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.SimpleAdapter;

import com.quanleimu.activity.BaseFragment;
import com.quanleimu.activity.R;
import com.quanleimu.entity.FirstStepCate;
import com.quanleimu.entity.SecondStepCate;

public class SecondCateFragment extends BaseFragment implements OnItemClickListener{
	
	private boolean isPost = false;
	private FirstStepCate cate = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		isPost = getArguments().getBoolean("isPost", false);
		cate = (FirstStepCate) getArguments().getSerializable("cates");
	}
	
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v =  inflater.inflate(R.layout.secondcate, null);
		
		GridView gridView = (GridView) v.findViewById(R.id.gridSecCategory);
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		List<SecondStepCate> children = cate.getChildren();
		for (SecondStepCate cate : children)
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
		
		getView().findViewById(R.id.gridSecCategory).requestFocus();
	}
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if (cate == null || cate.getChildren() == null
				|| cate.getChildren().size() <= arg2)
			return;
		SecondStepCate secCate = cate.getChildren().get(arg2);
		if (!isPost) {
			Bundle bundle = createArguments(secCate.getName(), "返回");
			bundle.putString("categoryEnglishName", secCate.getEnglishName());
			bundle.putString("siftresult", "");
			if (requestCode != INVALID_REQUEST_CODE) {
				String toRet = secCate.englishName + "," + secCate.name;
				finishFragment(requestCode, toRet);
			} else {
				bundle.putString("categoryName", secCate.getName());
				pushFragment(new GetGoodFragment(), bundle);
			}
		} else {
			String names = secCate.englishName + "," + secCate.name;
			if (requestCode != INVALID_REQUEST_CODE) {
				finishFragment(requestCode, names);
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
			title.m_rightActionHint = "发布";
			title.m_rightActionBg = R.drawable.bg_post_selector;
		}
	}
	
	@Override
	public void initTab(TabDef tab){
		tab.m_visible = true;
		tab.m_tabSelected = ETAB_TYPE.ETAB_TYPE_PUBLISH;
	}
	
	@Override
	public void handleSearch() {
		Bundle bundle = new Bundle();
		bundle.putString("categoryEnglishName", this.cate.getEnglishName());
		bundle.putString("categoryName", this.cate.getName());
		this.pushFragment(new SearchFragment(), bundle);
	}
	
	@Override
	public void handleRightAction() {

	}

}