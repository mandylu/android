package com.quanleimu.view.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.quanleimu.activity.BaseFragment;
import com.quanleimu.activity.R;
import com.quanleimu.view.CustomizePagerManager;
import com.quanleimu.view.CustomizePagerManager.PageProvider;
import com.quanleimu.view.CustomizePagerManager.PageSelectListener;

/**
 * 
 * @author liuchong
 *
 */
public class TabExampleFragment2 extends BaseFragment implements PageProvider, PageSelectListener {

	public static final String[] TAB_LABELS = new String[] {
		"第一页", "第二页", "第三页"
	};
	
	private CustomizePagerManager pageMgr;
	private int defaultSelect = 1;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		pageMgr = CustomizePagerManager.createManager(TAB_LABELS, defaultSelect);
	}

	@Override
	protected void initTitle(TitleDef title) {
		title.m_visible = true;
		title.m_title = "测试Tab切换2";
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		 View root = inflater.inflate(R.layout.tab_test, null);
		 
		 pageMgr.attachView(root, this, this);
		 
		 return root;
	}

	@Override
	public void onPageSelect(int index) {
		Toast.makeText(getActivity(), "page select " + index, Toast.LENGTH_SHORT).show();
	}

	@Override
	public View onCreateView(Context context, int index) {
		TextView text =  new TextView(context);
		text.setText("#" + index);
		
		return text;
	}
	
}
