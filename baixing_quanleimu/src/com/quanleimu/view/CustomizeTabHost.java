package com.quanleimu.view;

import java.io.Serializable;

import com.quanleimu.activity.R;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;


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
    }
    
    private transient TabSelectListener tabChangeListener;
    
    //TODO: should do refactor later. Hold view instance may cause memory issue.
    protected transient View[] tabButtons = new View[4];

    private transient View[] tabArrows = new View[4];

    protected transient TextView[] tabTexts = new TextView[4];
    
    private String[] tabLabels;
    
    private int currentFocusIndex;
    private int tabCount;
    
    
    private CustomizeTabHost()
    {
        
    }
    
    public static CustomizeTabHost createTabHost(int tabCount, int focusIndex, String[] tabString)
    {
        CustomizeTabHost tabHost = new CustomizeTabHost();
        tabHost.tabCount = tabCount;
        tabHost.currentFocusIndex = focusIndex;
        tabHost.tabLabels = tabString;
        
        return tabHost;
    }
    
    public void attachView(View v)
    {
    	this.initTabButton(v, this.tabCount);
    	this.showTab(this.currentFocusIndex);
    	
    	int i = 0;
    	for (String s : tabLabels)
    	{
    		this.setTabText(i++, s);
    	}
    }
    
    public int getCurrentIndex()
    {
        return currentFocusIndex;
    }
    
    private void setTabText(int index, CharSequence text)
    {
        tabTexts[index].setText(text);
    }
    
    private void setTabText(int index, int resId)
    {
        tabTexts[index].setText(resId);
    }
    
    public void setTabSelectListener(TabSelectListener listener)
    {
        tabChangeListener = listener;
    }
    
    
    private void initTabButton(View rootView, int num)
    {
        tabButtons[0] = rootView.findViewById(R.id.tab_button_1);
        tabButtons[1] = rootView.findViewById(R.id.tab_button_2);
        tabButtons[2] = rootView.findViewById(R.id.tab_button_3);
        tabButtons[3] = rootView.findViewById(R.id.tab_button_4);

        tabArrows[0] = rootView.findViewById(R.id.tab_arrow_1);
        tabArrows[1] = rootView.findViewById(R.id.tab_arrow_2);
        tabArrows[2] = rootView.findViewById(R.id.tab_arrow_3);
        tabArrows[3] = rootView.findViewById(R.id.tab_arrow_4);

        tabTexts[0] = (TextView) rootView.findViewById(R.id.tab_text_1);
        tabTexts[1] = (TextView) rootView.findViewById(R.id.tab_text_2);
        tabTexts[2] = (TextView) rootView.findViewById(R.id.tab_text_3);
        tabTexts[3] = (TextView) rootView.findViewById(R.id.tab_text_4);
        
        for (int i = num; i < 4; i++)
        {
            tabButtons[i].setVisibility(View.GONE);
        }
        
        initTabSelectAction();
    }
    
    private void initTabSelectAction()
    {
        View.OnClickListener listener = new View.OnClickListener()
        {
            public void onClick(View v)
            {
                switch(v.getId())
                {
                    case R.id.tab_button_1:
                        switchTab(0);
                        break;
                    case R.id.tab_button_2:
                        switchTab(1);
                        break;
                    case R.id.tab_button_3:
                        switchTab(2);
                        break;
                    case R.id.tab_button_4:
                        switchTab(3);
                        break;
                }
            }
        };
        
        for (View tabBtn : tabButtons)
        {
            tabBtn.setOnClickListener(listener);
        }
    }
    
    private void switchTab(final int newIndex)
    {
        if (newIndex == currentFocusIndex)
        {
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
            
    
    public void showTab(int index)
    {
        for (int i = 0; i < tabArrows.length; i++)
        {
            if (tabArrows[i] != null)
            {
                if (i == index)
                {
                    tabArrows[i].setVisibility(View.VISIBLE);
                }
                else
                {
                    tabArrows[i].setVisibility(View.INVISIBLE);
                }
            }
        }

        for (int i = 0; i < tabTexts.length; i++)
        {
            if (tabTexts[i] != null)
            {
                if (i == index)
                {
                    tabTexts[i].setTextColor(0xFFFFF153);
                }
                else
                {
                    tabTexts[i].setTextColor(0XFFECECE4);
                }
            }
        }
    }
    
}