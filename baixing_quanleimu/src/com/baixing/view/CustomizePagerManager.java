package com.baixing.view;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;

import com.baixing.view.CustomizeTabHost.TabIconRes;
import com.baixing.view.CustomizeTabHost.TabSelectListener;
import com.quanleimu.activity.R;

public class CustomizePagerManager implements TabSelectListener {

	public static interface PageProvider
	{
		public View onCreateView(Context context, int index);
	}
	
	public static interface PageSelectListener
	{
		public void onPageSelect(int index);
	}
	
	private CustomizeTabHost tabHost;
	private transient PageProvider pProvider;
	private transient PageSelectListener pSelectListener;
	private WeakReference<ViewPager> pagerWrapper;
	
	private CustomizePagerManager()
	{
		
	}
	
	public static CustomizePagerManager createManager(String[] tabTitles, int[][] tabIcons, int selectIndex)
	{
		CustomizePagerManager instance = new CustomizePagerManager();
		TabIconRes[] tabIconDef = new TabIconRes[tabTitles.length];
		for (int i=0; i<tabTitles.length; i++)
		{
			tabIconDef[i] = i < tabIcons.length ? new TabIconRes(tabIcons[i][1],	tabIcons[i][0]) : TabIconRes.NO_ICON;
		}
		instance.tabHost = CustomizeTabHost.createTabHost(selectIndex, tabTitles, tabIconDef);
		
		return instance;
	}
	
	public void attachView(View rootView, PageProvider pageProvider, PageSelectListener selectListener)
	{
		this.pProvider = pageProvider;
		this.pSelectListener = selectListener;
		
		this.tabHost.attachView(rootView.findViewById(R.id.common_tab_layout), this);
		ViewPager pager = (ViewPager) rootView.findViewById(R.id.tab_content);
		pagerWrapper = new WeakReference<ViewPager>(pager);
		
		pager.setAdapter(new PagerAdapter() {
			
			public Object instantiateItem(View arg0, int position) 
			{
				
				View page = pProvider.onCreateView(arg0.getContext(), position);
				
				((ViewPager) arg0).addView(page, 0);
				
				return page;
			}
			
            public void destroyItem(View arg0, int index, Object arg2)
            {
            }

			public boolean isViewFromObject(View arg0, Object arg1) {
				return arg0 == arg1;
			}
			
			public int getCount() {
				return tabHost.getTabCount();
			}
		});
		
		pager.setCurrentItem(tabHost.getCurrentIndex());
		 pager.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int arg0) {
				tabHost.showTab(arg0); //Force switch to specified tab.
				pSelectListener.onPageSelect(arg0);
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
				
			}
		});
		
	}

	@Override
	public void beforeChange(int currentIndex, int nextIndex) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterChange(int newSelectIndex) {
		ViewPager p = pagerWrapper.get();
		if  (p != null)
		{
			p.setCurrentItem(newSelectIndex);
		}
	}
	
}
