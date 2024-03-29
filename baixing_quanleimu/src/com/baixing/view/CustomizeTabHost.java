//liuchong@baixing.com
package com.baixing.view;

import java.io.Serializable;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.baixing.data.GlobalDataManager;
import com.quanleimu.activity.R;


/**
 * 
 * @author chongliu
 *
 */
public final class CustomizeTabHost implements Serializable
{
    
    /**
	 * 
	 */
	private static final long serialVersionUID = -7127389061287304936L;


	public static interface TabSelectListener
    {
        public void beforeChange(int currentIndex, int nextIndex);
        public void afterChange(int newSelectIndex);
        public void deprecatSelect(int currentIndex);
    }
	
	public static final class TabIconRes implements Serializable {
	/**
		 * 
		 */
		private static final long serialVersionUID = 6132394089130310112L;
		public static final TabIconRes NO_ICON = new TabIconRes(-1, -1);
		private int selectIconRes;
		private int unselectIconRes;
		public TabIconRes(int select, int unselect) {
			this.selectIconRes = select;
			this.unselectIconRes = unselect;
		}
		
		public int getRes(boolean isSelect) {
			return isSelect ? selectIconRes : unselectIconRes;
		}
	}
    
    private transient TabSelectListener tabChangeListener;
    
    private transient ViewGroup tabBarRoot;
    
    private String[] tabLabels;
    private TabIconRes[] tabIconsRes;
    
    private int currentFocusIndex;
    private int tabCount;
    
    
    private CustomizeTabHost()
    {
        
    }
    
    public static CustomizeTabHost createTabHost(int focusIndex, String[] tabString, TabIconRes[] tabIcons)
    {
        CustomizeTabHost tabHost = new CustomizeTabHost();
        tabHost.tabCount = tabString.length;
        tabHost.currentFocusIndex = focusIndex;
        tabHost.tabLabels = tabString;
        tabHost.tabIconsRes = tabIcons;
        
        return tabHost;
    }
    
    public void attachView(View v, TabSelectListener listener)
    {
    	this.setTabSelectListener(listener);
    	
    	this.tabBarRoot = (ViewGroup) v;
    	
    	this.initTabButton(v, this.tabCount);
    	this.showTab(this.currentFocusIndex);
    	
    	Resources res = v.getContext().getResources();
    	for (int i=0; i<tabLabels.length; i++)
    	{
    		this.setTabText(i, tabLabels[i]);
    		this.setTabIcon(i, tabIconsRes[i]);
    		if (i==currentFocusIndex) {
    			getTabItem(i).setBackgroundColor(res.getColor(R.color.tab_bg_select));
    		}
    		else {
    			getTabItem(i).setBackgroundResource(R.drawable.bg_camera_header);
    		}
//    		getTabItem(i).setBackgroundColor(res.getColor(i==currentFocusIndex ? R.color.tab_bg_select : R.color.tab_bg));
    	}
    }
    
    public int getCurrentIndex()
    {
        return currentFocusIndex;
    }
    
    private void setTabText(int index, CharSequence text)
    {
    	View rootView = getTabItem(index);
    	TextView textView = (TextView) rootView.findViewById(R.id.tab_text);
    	textView.setText(text);
    }
    
    private void setTabIcon(int index, TabIconRes res)
    {
//    	ImageView icon = (ImageView) getTabItem(index).findViewById(R.id.tab_icon);
    	TextView tv = (TextView)getTabItem(index).findViewById(R.id.tab_text);
    	if (res == TabIconRes.NO_ICON){
    		tv.setCompoundDrawables(null, null, null, null);
//    		icon.setVisibility(View.GONE);
    	}
    	else{
    		Bitmap bmp = GlobalDataManager.getInstance().getImageManager().loadBitmapFromResource(res.getRes(index == this.currentFocusIndex));
    		Drawable top = new BitmapDrawable(bmp);
    		
    		int width = 
    				GlobalDataManager.getInstance().getApplicationContext().getResources().getDimensionPixelSize(R.dimen.tab_icon_width);
    		int height = 
    				GlobalDataManager.getInstance().getApplicationContext().getResources().getDimensionPixelSize(R.dimen.tab_icon_height);

    		top.setBounds(0, 0, width, height);
    		tv.setCompoundDrawables(null, top, null, null);
//    		icon.setImageBitmap();
    	}
    }
    
    public void setTabSelectListener(TabSelectListener listener)
    {
        tabChangeListener = listener;
    }
    
    private void initTabButton(View rootView, int num)
    {
    	for (int i=0; i<num; i++)
    	{
    		getTabItem(i).setTag(i);
    	}
        
        for (int i = num; i < 4; i++)
        {
        	getTabItem(i).setVisibility(View.GONE);
        }
        
        initTabSelectAction(num);
    }
    
    private View getTabItem(int index) {
    	return tabBarRoot.getChildAt(index);
    }
    
    private void initTabSelectAction(int count)
    {
        View.OnClickListener listener = new View.OnClickListener()
        {
            public void onClick(View v)
            {
            	switchTab((Integer) v.getTag());
            }
        };
        
        for (int i=0; i<count; i++)
        {
            getTabItem(i).setOnClickListener(listener);
        }
    }
    
    private void switchTab(final int newIndex)
    {
        if (newIndex == currentFocusIndex)
        {
        	if (tabChangeListener != null)
        	{
        		tabChangeListener.deprecatSelect(currentFocusIndex);
        	}
            return;
        }
        
        if (tabChangeListener != null)
        {
            tabChangeListener.beforeChange(currentFocusIndex, newIndex);
        }
        
        showTab(newIndex);
        
        currentFocusIndex = newIndex;
        if (tabChangeListener != null)
        {
            tabChangeListener.afterChange(currentFocusIndex);
        }
    }
    
    
    public void setCurrentFocusIndex(int index)
    {
        currentFocusIndex = index;
    }
            
    
    public int getTabCount()
    {
    	return this.tabCount;
    }
    
    public void showTab(final int index)
    {
    	setCurrentFocusIndex(index);
    	
    	Resources res = this.tabBarRoot.getResources();
    	for (int i = 0; i<tabCount; i++)
    	{
    		View tabItem = getTabItem(i);
    		
    		//Tab Arrow indicator visibility 
    		tabItem.findViewById(R.id.tab_arrow).setVisibility(i == index ? View.VISIBLE : View.INVISIBLE);
    		
    		//Tab text color
    		int textColor = res.getColor(i == index ? R.color.tab_font_foucs : R.color.tab_font);
    		((TextView)tabItem.findViewById(R.id.tab_text)).setTextColor(textColor);
    		
    		//Tab icon 
    		this.setTabIcon(i, tabIconsRes[i]);
    		
    		//Tab bg
    		if (i==currentFocusIndex) {
    			tabItem.setBackgroundColor(res.getColor(R.color.tab_bg_select));
    		}
    		else {
    			tabItem.setBackgroundResource(R.drawable.bg_camera_header);
    		}
//    		tabItem.setBackgroundColor(res.getColor(i==currentFocusIndex ? R.color.tab_bg_select : R.color.tab_bg));
    	}
    	
    }
    
}