package com.baixing.widget;

import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;

public class ContextMenuItem extends View {
	
	private String title;
	private int[] optionIds;
	private String[] options;
	
	public static interface ContextHandler
	{
		public void onItemSelect(int index);
	}

	public ContextMenuItem(Context context) {
		super(context);
	}
	
	public ContextMenuItem(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public ContextMenuItem(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public void updateOptionList(String optionTitle, String[] optionList, int[] ids)
	{
		this.title = optionTitle;
		this.options = optionList;
		this.optionIds = ids;
	}

	@Override
	protected void onCreateContextMenu(ContextMenu menu) {
		super.onCreateContextMenu(menu);
		if (title != null && options != null && options.length > 0)
		{
			menu.setHeaderTitle(title);  
			for (int i=0; i<options.length; i++)
			{
				MenuItem item = menu.add(0, optionIds[i], 0, options[i]);
			}
		}
	}
	
}
