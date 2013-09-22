package com.baixing.widget;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chencang.core.R;

/**
 *
 * TODO: this class is temp use for main screen, as android's grid view cannot match our requirement and we do not want to spent much time on Customize View implementation.
 * Fixme later when you have time. Thanks.
 *
 */
public class CustomizeGridView extends LinearLayout implements View.OnClickListener {

	static public class GridInfo{
		public Bitmap img;
		public String text;
		public int number = 0;
//		public int resId;
//		public boolean starred = false;
	}
	
	static public interface ItemClickListener
	{
		public void onItemClick(GridInfo info, int index);
	}
	
    static private class GridHolder {  
        ImageButton imageBtn;  
        public TextView text;  
//        public View starIcon;
        public GridInfo info;
        public int index;
    }  
	
	private int columnCount = 3;
	private List<GridInfo> gridItems = new ArrayList<CustomizeGridView.GridInfo>();
	private ItemClickListener itemClickListener;
	
	public CustomizeGridView(Context context) {
		super(context);
	}

	public CustomizeGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public void setData(List<GridInfo> items, int columnCount)
	{
		gridItems.clear();
		gridItems.addAll(items);
		long t1 = System.currentTimeMillis();
		this.costruct();
		long t2 = System.currentTimeMillis();
		Log.d("time", "time:   " + (t2 - t1));
	}
	
	public void setItemClickListener(ItemClickListener listener)
	{
		this.itemClickListener = listener;
	}
	
	private void costruct()
	{
		WindowManager winMgr = (WindowManager) this.getContext().getSystemService(Context.WINDOW_SERVICE);
		final int fixWidth = winMgr.getDefaultDisplay().getWidth() / columnCount;
		int lineCount = gridItems.size() / columnCount;
		if (gridItems.size() > lineCount * columnCount)
		{
			lineCount++;
		}
		
		this.removeAllViews();
		for (int i=0; i<lineCount; i++)
		{
			LinearLayout line = new LinearLayout(this.getContext());
			line.setOrientation(LinearLayout.HORIZONTAL);
			
			for (int j=0; j<columnCount; j++)
			{
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(fixWidth, fixWidth);
				line.addView(getView(i * columnCount + j), params);
			}
			
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
			this.addView(line, params);
		}
	}
		
//	public void releaseResource(){
//		Collection<Bitmap> bmps = images.values();
//		if(bmps != null){
//			Iterator<Bitmap> ite = bmps.iterator();
//			while(ite.hasNext()){
//				Bitmap bmp = ite.next();
//				if(bmp != null){
//					bmp.recycle();
//				}
//			}
//		}
//		images.clear();
//	}
	
	public View getView(int index) {
		LayoutInflater inflater = LayoutInflater.from(this.getContext());
        GridHolder holder;  
        View convertView = inflater.inflate(R.layout.categorygriditem, null);     
        holder = new GridHolder();  
    	holder.imageBtn = (ImageButton)convertView.findViewById(R.id.itemicon);  
    	holder.imageBtn.setClickable(false);
    	holder.imageBtn.setFocusable(false);
    	holder.text = (TextView)convertView.findViewById(R.id.itemtext);  
//    	holder.starIcon = convertView.findViewById(R.id.star);
        convertView.setTag(holder);
        convertView.setOnClickListener(this);
        holder.index = index;
        
        GridInfo info = index < gridItems.size() ? gridItems.get(index) : null;  
        holder.info = info;
        if (info != null) {     
        	String text = info.text;
        	if (info.number > 0) 
        	{
        		text = String.format("%s(%d)", text, info.number);
        	}
            holder.text.setText(text);
            holder.imageBtn.setImageBitmap(info.img);
//            holder.imageBtn.setImageResource(info.resId);
//            holder.starIcon.setVisibility(info.starred ? View.VISIBLE : View.GONE);
            convertView.setEnabled(true);
        }  
        else
        {
//        	holder.starIcon.setVisibility(View.GONE);
        	convertView.setEnabled(false);
        }
        
        if (index != 0 && (index + 1)%columnCount == 0)
        {
        	convertView.setBackgroundResource(R.drawable.bg_grid_selector_2);
        }
        else
        {
        	convertView.setBackgroundResource(R.drawable.bg_grid_selector);
        }
        return convertView;  
    }

	@Override
	public void onClick(View v) {
		if (v != null && v.getTag() instanceof GridHolder && this.itemClickListener != null)
		{
			GridHolder holder = (GridHolder) v.getTag();
			itemClickListener.onItemClick(holder.info, holder.index);
		}
	}  
	
	public void updateNumber(int whichItem, int number)
	{
		//TODO: add implementation here. update the badge count of the specified item.
	}

}