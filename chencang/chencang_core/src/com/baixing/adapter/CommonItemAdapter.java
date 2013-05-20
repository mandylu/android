package com.baixing.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.baixing.imageCache.ImageCacheManager;
import com.chencang.core.R;

public class CommonItemAdapter extends BXAlphabetSortableAdapter {

//	private Context context;
//	private List<? extends Object> list = new ArrayList<Object>();
	private Object tag;
	private boolean hasArrow = true;
	private int iconId = R.drawable.arrow;
	private int left = -1, right = -1, top = -1, bottom = -1;
	private boolean sorted = false;
	private boolean hasSearchBar = true;
	
	public void setPadding(int left, int right, int top, int bottom){
		this.left = left;
		this.right = right;
		this.top = top;
		this.bottom = bottom;
	}

	public void setTag(Object obj){
		tag = obj;
	}
	
	public Object getTag(){
		return tag;
	}
	

	public CommonItemAdapter(Context context,List<? extends Object> list, int sortIfMoreThan, boolean hasSearchBar) {
		super(context, list, list != null && list.size() > sortIfMoreThan);
		sorted = (list != null && list.size() > sortIfMoreThan);
		if(sorted && hasSearchBar){
			this.list.add(0, "placeholder");
		}
		this.hasSearchBar = hasSearchBar;
//		this.context = context;
//		this.list = list;
	}
	
	public void setHasArrow(boolean has){
		this.hasArrow = has;
	}
	
	public void setList(List<? extends Object> list_){
		this.list.clear();
		this.list.addAll(list_);
		this.notifyDataSetChanged();
	}
	
	public List<? extends Object> getList(){
		return this.list;
	}


	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return list.get(arg0); 
	} 

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position; 
	}
	
	public void setRightIcon(int resourceId){
		iconId = resourceId;
	}
	
	class ViewHolder{
		public TextView tv;
		public ImageView iv;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(position == 0 && sorted && hasSearchBar){
			LayoutInflater inflater = LayoutInflater.from(context);
			return inflater.inflate(R.layout.searchbar, null);
		}
		
		if(sorted){
			View header = getHeaderIfItIs(position, convertView);
			if(header != null){
				return header;
			}
		}
		
		ViewHolder holder;
		View v = null;
		if(convertView == null || convertView.getTag() == null || !(convertView.getTag() instanceof ViewHolder))
		{
			LayoutInflater inflater = LayoutInflater.from(context);
			v = inflater.inflate(R.layout.item_common, null);
			holder = new ViewHolder();
			holder.tv = (TextView)v.findViewById(R.id.tvCateName);
			holder.iv = (ImageView)v.findViewById(R.id.ivChoose);
			v.setTag(holder);
		}
		else{
			v = (View)convertView;
			holder = (ViewHolder)convertView.getTag();
		}
		
		if(left >= 0 && right >= 0 && top >= 0 && bottom >= 0){
			v.setPadding(left, top, right, bottom);
		}
		
		holder.tv.setText(list.get(position).toString());
		
		if(this.hasArrow){
			holder.iv.setVisibility(View.VISIBLE);
			Bitmap bmp = ImageCacheManager.getInstance().loadBitmapFromResource(iconId);
			holder.iv.setImageBitmap(bmp);
		}
		else{
			holder.iv.setVisibility(View.GONE);
		}
		
		return v;
	}

}
