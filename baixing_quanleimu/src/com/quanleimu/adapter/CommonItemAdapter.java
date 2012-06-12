package com.quanleimu.adapter;

import java.util.ArrayList;
import java.util.List;

import com.quanleimu.activity.R;
import com.quanleimu.entity.FirstStepCate;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.ImageView;
public class CommonItemAdapter extends BaseAdapter {

	private Context context;
	private List<? extends Object> list = new ArrayList<Object>();
	private Object tag;
	private boolean hasArrow = true;
	private int iconId = R.drawable.arrow;
	private int left = -1, right = -1, top = -1, bottom = -1;
	
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
	

	public CommonItemAdapter(Context context,List<? extends Object> list) {
		super();
		this.context = context;
		this.list = list;
	}
	
	public void setHasArrow(boolean has){
		this.hasArrow = has;
	}
	
	public void setList(List<? extends Object> list_){
		this.list = list_;
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

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View v = null;
		if(convertView == null)
		{
			v = inflater.inflate(R.layout.item_common, null);
		}else{
			v = (View)convertView; 
		}
		
		if(left >= 0 && right >= 0 && top >= 0 && bottom >= 0){
			v.setPadding(left, top, right, bottom);
		}
		
		TextView tvCateName = (TextView)v.findViewById(R.id.tvCateName);
		tvCateName.setText(list.get(position).toString());
		
		ImageView arrow = (ImageView)v.findViewById(R.id.ivChoose);
		if(this.hasArrow){
			arrow.setVisibility(View.VISIBLE);
			arrow.setImageResource(iconId);
		}
		else{
			arrow.setVisibility(View.GONE);
		}
		
		return v;
	}

}
