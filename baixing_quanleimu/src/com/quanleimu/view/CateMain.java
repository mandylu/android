package com.quanleimu.view;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

import com.quanleimu.activity.R;
import com.quanleimu.activity.R.id;
import com.quanleimu.activity.R.layout;
import com.quanleimu.adapter.AllCatesAdapter;
import com.quanleimu.adapter.GoodsListAdapter;
import com.quanleimu.entity.AllCates;
import com.quanleimu.entity.FirstStepCate;
import com.quanleimu.entity.PostMu;
import com.quanleimu.entity.SecondStepCate;
import com.quanleimu.jsonutil.JsonUtil;
import com.quanleimu.util.Communication;
import com.quanleimu.util.ShortcutUtil;
import com.quanleimu.util.Util;

public class CateMain extends BaseView implements CategorySelectionView.ICateSelectionListener{

	// 定义控件
	protected CategorySelectionView selectionView;

	protected void Init(){
		LayoutInflater inflater = LayoutInflater.from(getContext());
		this.addView((RelativeLayout)inflater.inflate(R.layout.catemain, null));

		selectionView = new CategorySelectionView(getContext());
		selectionView.setSelectionListener(this);

		((LinearLayout)findViewById(R.id.linearListView)).addView(selectionView);
	}
	
	public CateMain(Context context){
		super(context);
		
		Init();
	}
	public CateMain(Context context, Bundle bundle){
		super(context);
		
		Init();
	}
	
	@Override
	public boolean onBack(){
		if(CateMain.this.selectionView == null || !CateMain.this.selectionView.OnBack()){
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
	
	public boolean onRightActionPressed(){return false;}//called when right button on title bar pressed, return true if handled already, false otherwise
	
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
		tab.m_tabSelected = ETAB_TYPE.ETAB_TYPE_CATEGORY;
		return tab;
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
		
		Bundle bundle = new Bundle();
		bundle.putString("name", selectedSubCate.getName());
		bundle.putString("categoryEnglishName",	selectedSubCate.getEnglishName());
		bundle.putString("siftresult", "");
		bundle.putString("backPageName", "选择类目");
		if(null != m_viewInfoListener){
			m_viewInfoListener.onNewView(new GetGoodsView(getContext(), bundle, selectedSubCate.getEnglishName()));			
		}
	}
}
