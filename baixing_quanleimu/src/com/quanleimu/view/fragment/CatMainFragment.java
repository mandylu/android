package com.quanleimu.view.fragment;

import com.quanleimu.activity.BaseFragment;
import com.quanleimu.activity.BaseFragment.TabDef;
import com.quanleimu.activity.BaseFragment.TitleDef;
import com.quanleimu.activity.R;
import com.quanleimu.entity.FirstStepCate;
import com.quanleimu.entity.SecondStepCate;
import com.quanleimu.view.CategorySelectionView;
//import com.quanleimu.view.GetGoodsView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;


public class CatMainFragment extends BaseFragment implements CategorySelectionView.ICateSelectionListener {

	@Override
	public void initTitle(TitleDef title){
		title.m_visible = true;
		title.m_title = "返回";//this.getArguments().getString("title_str");
		title.m_leftActionHint = "";//this.getArguments().getString("back_str");
	}
	
	@Override
	public void initTab(TabDef tab){
		tab.m_visible = true;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View v = (RelativeLayout)inflater.inflate(R.layout.catemain, null);

		CategorySelectionView selectionView = new CategorySelectionView(getActivity(), null, null);
		selectionView.setSelectionListener(this);

		((LinearLayout)v.findViewById(R.id.linearListView)).addView(selectionView);
		
	
		return v;
	}

	@Override
	public void OnMainCategorySelected(FirstStepCate selectedMainCate){
		
		getTitleDef().m_title = selectedMainCate.getName();
//		back_str = title_str;
//		title_str = selectedMainCate.getName();			
//		m_viewInfoListener.onTitleChanged(getTitleDef());
		
		refreshHeader();
	}
	
	@Override
	public void OnSubCategorySelected(SecondStepCate selectedSubCate){

		Bundle bundle = createArguments(selectedSubCate.getName(), "返回");
		bundle.putString("categoryEnglishName",	selectedSubCate.getEnglishName());
		bundle.putString("siftresult", "");
		bundle.putString("categoryName", selectedSubCate.getName());
		
		pushFragment(new GetGoodFragment(), bundle);
	}
	
	

}
