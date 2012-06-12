package com.quanleimu.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.quanleimu.entity.FirstStepCate;
import com.quanleimu.entity.SecondStepCate;
import com.quanleimu.view.CategorySelectionView;
import com.quanleimu.view.PostGoodsCateMainView;
import com.quanleimu.activity.BaseActivity;
import com.quanleimu.activity.R;

public class PostGoodsCateMainView extends BaseView implements CategorySelectionView.ICateSelectionListener{
	private LinearLayout lvCateArea;
	protected CategorySelectionView selectionView;
	private BaseActivity baseActivity;
	private Bundle bundle;
	private int message;
	private boolean subSelected = false;
	
	String title_str;
	String back_str;
	
	
	public PostGoodsCateMainView(BaseActivity context, Bundle bundle, int msgBack){
		super(context, bundle);
		baseActivity = context;
		this.bundle = bundle;
		message = msgBack;
		init();
	}
		
	@Override
	public void OnMainCategorySelected(FirstStepCate selectedMainCate){
		if(null != m_viewInfoListener){
			back_str = title_str;
			title_str = selectedMainCate.getName();
			m_viewInfoListener.onTitleChanged(getTitleDef());
		}
	}
	
	@Override
	public void OnSubCategorySelected(SecondStepCate selectedSubCate){
		
		if(null != m_viewInfoListener){
			subSelected = true;
			m_viewInfoListener.onBack(message, selectedSubCate.getEnglishName() + "," + selectedSubCate.getName());			
		}
	}
	
	protected void init() {
		LayoutInflater inflater = LayoutInflater.from(this.getContext());
		View v = inflater.inflate(R.layout.catemain, null);
		this.addView(v);

		selectionView = new CategorySelectionView(baseActivity);
		selectionView.setSelectionListener(this);
		lvCateArea = (LinearLayout) findViewById(R.id.linearListView);		
		lvCateArea.addView(selectionView);		
		
		title_str = "选择发布类目";
		back_str = "";
	}
	
	@Override
	public boolean onBack(){
		if(subSelected || selectionView == null || !selectionView.OnBack()){
			subSelected = false;
			return false;
		}else
		{
			if(null != m_viewInfoListener){				
				this.title_str = back_str;
				back_str = "";
				
				m_viewInfoListener.onTitleChanged(getTitleDef());
			}
		}
		return true;
	}//called when back button/key pressed
	
	@Override
	public boolean onLeftActionPressed(){
		return onBack();
	}//called when left button on title bar pressed, return true if handled already, false otherwise

	@Override
	public TitleDef getTitleDef(){
		TitleDef title = new TitleDef();
		title.m_visible = true;
		title.m_title = title_str;
		title.m_leftActionHint = back_str;
		return title;
	}
	
	@Override
	public TabDef getTabDef(){
		TabDef tab = new TabDef();
		tab.m_visible = true;
		tab.m_tabSelected = ETAB_TYPE.ETAB_TYPE_PUBLISH;
		return tab;
	}
}
