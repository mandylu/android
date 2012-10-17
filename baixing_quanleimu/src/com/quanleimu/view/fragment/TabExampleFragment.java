package com.quanleimu.view.fragment;

import java.util.List;

import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.quanleimu.activity.BaseFragment;
import com.quanleimu.activity.R;
import com.quanleimu.view.CustomizeTabHost;
import com.quanleimu.view.CustomizeTabHost.TabSelectListener;

/**
 * 
 * @author liuchong
 *
 */
public class TabExampleFragment extends BaseFragment implements TabSelectListener {

	public static final String[] TAB_LABELS = new String[] {
		"第一页", "第二页", "第三页"
	};
	
	private CustomizeTabHost tabHost;
	private int defaultSelect = 1;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		tabHost = CustomizeTabHost.createTabHost(TAB_LABELS.length, defaultSelect, TAB_LABELS); //Create tab host instance.
		tabHost.setTabSelectListener(this);
	}

	@Override
	protected void initTitle(TitleDef title) {
		title.m_visible = true;
		title.m_title = "测试Tab切换";
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		 View root = inflater.inflate(R.layout.tab_test, null);
		 
		 tabHost.attachView(root.findViewById(R.id.common_tab_layout)); //Start to manage the tab host.
		 
		 ViewPager pager = (ViewPager) root.findViewById(R.id.tab_content);
		 pager.setAdapter(new PagerAdapter() {
				
				public Object instantiateItem(View arg0, int position) 
				{
					TextView textV = new TextView(arg0.getContext());
					textV.setText("#" + position);
					
					((ViewPager) arg0).addView(textV, 0);
					
					return textV;
				}
				
	            public void destroyItem(View arg0, int index, Object arg2)
	            {
	            }

				public boolean isViewFromObject(View arg0, Object arg1) {
					return arg0 == arg1;
				}
				
				public int getCount() {
					return TAB_LABELS.length;
				}
			});
		 pager.setCurrentItem(1);
		 pager.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int arg0) {
				tabHost.showTab(arg0); //Force switch to specified tab.
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
				
			}
		});
		 
		 return root;
	}

	@Override
	public void beforeChange(int currentIndex, int nextIndex) {
		
	}

	@Override
	/**
	 * When user click tab, callback here to let you control UI update.
	 */
	public void afterChange(int newSelectIndex) { 
		ViewPager pager = (ViewPager) getView().findViewById(R.id.tab_content);
		pager.setCurrentItem(newSelectIndex);
	}
	
}
