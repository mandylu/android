package com.quanleimu.adapter;  
  
import java.util.List;  

import com.quanleimu.activity.R;
  
import android.content.Context;  
import android.view.LayoutInflater;  
import android.view.View;  
import android.view.ViewGroup;  
import android.widget.BaseAdapter;  
import android.widget.ImageButton;
import android.widget.TextView;  
  
public class GridAdapter extends BaseAdapter {
	static public class GridInfo{
		public int imgResourceId;
		public String text;
		public int number = 0;
		public boolean starred = false;
	}
  
    static public class GridHolder {  
        ImageButton imageBtn;  
        public TextView text;  
        public View starIcon;
    }  
  
    private Context context;  
    private int colCount;
  
    private List<GridInfo> list;  
    private LayoutInflater mInflater;  
  
    public GridAdapter(Context c) {  
        super();  
        this.context = c;  
    }  
  
    public void setList(List<GridInfo> list, int columnCount) {  
        this.list = list;  
        this.colCount = columnCount;
        mInflater = (LayoutInflater) context  
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
  
    }  
  
    public int getCount() {  
        // TODO Auto-generated method stub  
        return list.size();  
    }  
  
    @Override  
    public Object getItem(int index) {  
  
        return list.get(index);  
    }  
  
    @Override  
    public long getItemId(int index) {  
        return index;  
    }  
  
    @Override  
    public View getView(int index, View convertView, ViewGroup parent) {  
        GridHolder holder;  
        if (convertView == null) {     
            convertView = mInflater.inflate(R.layout.categorygriditem, null);     
            holder = new GridHolder();  
            holder.imageBtn = (ImageButton)convertView.findViewById(R.id.itemicon);  
            holder.imageBtn.setClickable(false);
            holder.imageBtn.setFocusable(false);
            holder.text = (TextView)convertView.findViewById(R.id.itemtext);  
            holder.starIcon = convertView.findViewById(R.id.star);
            convertView.setTag(holder);
  
        }else{  
             holder = (GridHolder) convertView.getTag();     
  
        }  
        GridInfo info = list.get(index);  
        if (info != null) {     
        	String text = info.text;
        	if (info.number > 0) 
        	{
        		text = String.format("%s(%d)", text, info.number);
        	}
            holder.text.setText(text);
            holder.imageBtn.setImageResource(info.imgResourceId);
            holder.starIcon.setVisibility(info.starred ? View.VISIBLE : View.GONE);
        }  
        
        if (index != 0 && (index + 1)%colCount == 0)
        {
        	convertView.setBackgroundResource(R.drawable.bg_grid_selector_2);
        }
        else
        {
        	convertView.setBackgroundResource(R.drawable.bg_grid_selector);
        }
        return convertView;  
    }  
  
}  