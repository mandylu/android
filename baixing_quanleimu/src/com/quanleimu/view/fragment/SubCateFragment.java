package com.quanleimu.view.fragment;

import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.quanleimu.activity.BaseFragment;
import com.quanleimu.activity.BaseFragment.TabDef;
import com.quanleimu.activity.BaseFragment.TitleDef;
import com.quanleimu.activity.R;
import com.quanleimu.entity.FirstStepCate;
import com.quanleimu.entity.SecondStepCate;
import com.quanleimu.view.CategorySelectionView;

public class SubCateFragment extends BaseFragment implements CategorySelectionView.ICateSelectionListener {
	protected void initTitle(TitleDef title) {
		title.m_title = getArguments().containsKey(ARG_COMMON_TITLE) ? getArguments().getString(ARG_COMMON_TITLE) : "";
		title.m_leftActionHint = getArguments().containsKey(ARG_COMMON_BACK_HINT) ? getArguments().getString(ARG_COMMON_BACK_HINT) : "";
		
		title.m_visible = true;
	}
	
	public void initTab(TabDef tab){
		tab.m_visible = true;
		tab.m_tabSelected = ETAB_TYPE.ETAB_TYPE_MAINPAGE;
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		logCreateView(savedInstanceState);
		
		View v = inflater.inflate(R.layout.catesub, null);

		CategorySelectionView catesView = (CategorySelectionView)v.findViewById(R.id.cateSelection);
		catesView.setSelectionListener(this);
		catesView.setRootCateList((List) this.getArguments().getSerializable("cates") );
		
		return v;
	}

	@Override
	public void OnMainCategorySelected(FirstStepCate selectedMainCate) {
		// TODO Auto-generated method stub
		throw new RuntimeException("you should nerver call main category.");
	}

	@Override
	public void OnSubCategorySelected(SecondStepCate selectedSubCate) {
		Bundle bundle = createArguments(selectedSubCate.getName(), "返回");
		bundle.putString("categoryEnglishName",	selectedSubCate.getEnglishName());
		bundle.putString("siftresult", "");
		bundle.putString("categoryName", selectedSubCate.getName());
		pushFragment(new GetGoodFragment(), bundle);
	}
}
