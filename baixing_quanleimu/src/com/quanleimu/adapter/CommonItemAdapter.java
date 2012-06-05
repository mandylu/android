package com.quanleimu.adapter;

import java.util.ArrayList;
import java.util.List;

import com.quanleimu.activity.R;
import com.quanleimu.entity.FirstStepCate;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class CommonItemAdapter extends BaseAdapter {

	private Context context;
	private List<? extends Object> list = new ArrayList<Object>();
	private Object tag;
	
	public CommonItemAdapter() {
		super();
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
		return arg0; 
	} 

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position; 
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
		if(position==0){ 
			v.setBackgroundResource(R.drawable.btn_top_bg);
		}else if(position==list.size()-1){
//			v.setBackgroundResource(R.drawable.btn_m_bg);
			v.setBackgroundResource(R.drawable.btn_down_bg);
		}else{
			v.setBackgroundResource(R.drawable.btn_m_bg);
		}
		
		TextView tvCateName = (TextView)v.findViewById(R.id.tvCateName);
		tvCateName.setText(list.get(position).toString());
		
		return v;
	}

}
