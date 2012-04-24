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

public class AllCatesAdapter extends BaseAdapter {

	public Context context;
	public TextView tvCateName;
	public List<FirstStepCate> list = new ArrayList<FirstStepCate>();
	
	 

	public AllCatesAdapter() {
		super();
		// TODO Auto-generated constructor stub
	}
	

	public AllCatesAdapter(Context context,List<FirstStepCate> list) {
		super();
		this.context = context;
		this.list = list;
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
			v = inflater.inflate(R.layout.item_allcates, null);
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
		
		tvCateName = (TextView)v.findViewById(R.id.tvCateName);
		tvCateName.setText(list.get(position).getName());
		
		return v;
	}

}
