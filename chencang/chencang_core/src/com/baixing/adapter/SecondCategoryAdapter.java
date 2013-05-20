package com.baixing.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.chencang.core.R;

public class SecondCategoryAdapter extends BaseAdapter {
	public static class SecondCategoryInfo {
		public boolean selected;
		public String  name;
	}
	
	private static class SecondCategoryHolder {
		public ImageView checkImage;
		public TextView  categoryView;
	}
	
    private Context context;  
    private LayoutInflater mInflater;
    private List<SecondCategoryInfo> list;  
	
    public SecondCategoryAdapter(Context c) {
		this.context = c;
	}
    
    
    public void setList(List<SecondCategoryInfo> list) {
		this.list = list;
        mInflater = (LayoutInflater) context  
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
	}
    
	@Override
	public int getCount() {
		return this.list.size();
	}

	@Override
	public Object getItem(int position) {
		return this.list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		SecondCategoryHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.item_seccategory, null);
			holder = new SecondCategoryHolder();
			holder.categoryView = (TextView) convertView.findViewById(R.id.tvCategoryName);
			holder.checkImage = (ImageView) convertView.findViewById(R.id.ivCheckImage);
			
			convertView.setTag(holder);
		} else {
			holder = (SecondCategoryHolder) convertView.getTag();
		}
		
		SecondCategoryInfo info = this.list.get(position);
		holder.categoryView.setText(info.name);
		holder.checkImage.setVisibility(info.selected ? ImageView.VISIBLE : ImageView.GONE);
		return convertView;
	}


}
