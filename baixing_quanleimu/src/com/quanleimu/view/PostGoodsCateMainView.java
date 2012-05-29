package com.quanleimu.view;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.quanleimu.adapter.AllCatesAdapter;
import com.quanleimu.entity.AllCates;
import com.quanleimu.entity.FirstStepCate;
import com.quanleimu.entity.PostMu;
import com.quanleimu.entity.SaveFirstStepCate;
import com.quanleimu.entity.SecondStepCate;
import com.quanleimu.jsonutil.JsonUtil;
import com.quanleimu.util.Communication;
import com.quanleimu.util.Util;
import com.quanleimu.view.CategorySelectionView;
import com.quanleimu.view.SetMainView;
import com.quanleimu.view.PostGoodsCateMainView;
import com.quanleimu.view.BaseView.ETAB_TYPE;
import com.quanleimu.view.BaseView.TabDef;
import com.quanleimu.view.BaseView.TitleDef;
import com.quanleimu.activity.BaseActivity;
import com.quanleimu.activity.R;

public class PostGoodsCateMainView extends BaseView implements CategorySelectionView.ICateSelectionListener{
	private LinearLayout lvCateArea;
	protected CategorySelectionView selectionView;
	private BaseActivity baseActivity;
	private Bundle bundle;
	
	public PostGoodsCateMainView(BaseActivity context, Bundle bundle){
		super(context, bundle);
		baseActivity = context;
		this.bundle = bundle;
		init();
	}
		
	@Override
	public void OnMainCategorySelected(FirstStepCate selectedMainCate){
		if(null != m_viewInfoListener){
			TitleDef title = getTitleDef();
			title.m_leftActionHint = "选择类目";
			title.m_title = selectedMainCate.getName();
			m_viewInfoListener.onTitleChanged(title);
		}
	}
	
	@Override
	public void OnSubCategorySelected(SecondStepCate selectedSubCate){
		if(null != m_viewInfoListener){
			m_viewInfoListener.onNewView(new PostGoodsView(baseActivity, bundle, selectedSubCate.getEnglishName()));			
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
	}
	
	@Override
	public boolean onBack(){
		if(selectionView == null || !selectionView.OnBack()){
			return false;
		}else
		{
			if(null != m_viewInfoListener){
				TitleDef title = getTitleDef();
				title.m_leftActionHint = "";
				m_viewInfoListener.onTitleChanged(title);
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
		title.m_title = "选择类目";
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