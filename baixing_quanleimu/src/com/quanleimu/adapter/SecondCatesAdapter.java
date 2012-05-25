package com.quanleimu.adapter;

import java.util.ArrayList;
import java.util.List;

import com.quanleimu.activity.R;
import com.quanleimu.entity.SecondStepCate;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class SecondCatesAdapter extends BaseAdapter {

	public Context context;
	public List<SecondStepCate> list = new ArrayList<SecondStepCate>();
	public String cateName;
	

	public SecondCatesAdapter() {
		super();
		// TODO Auto-generated constructor stub
	}
	

	public SecondCatesAdapter(Context context, String cateName_, List<SecondStepCate> list) {
		super();
		this.context = context;
		this.list = list;
		this.cateName = cateName_;
	}
	
	public void SetCateName(String cateName_){
		this.cateName = cateName_;
	}

	public void SetSubCateList( List<SecondStepCate> listSubCate){
		this.list = listSubCate;
		notifyDataSetChanged();
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
		}
		else{
			v = (View)convertView;
		}
		if(position==0){ 
			v.setBackgroundResource(R.drawable.btn_top_bg);
		}else if(position==list.size()-1){
			v.setBackgroundResource(R.drawable.btn_down_bg);
		}else{
			v.setBackgroundResource(R.drawable.btn_m_bg);
		}
		
		TextView tvCateName = (TextView)v.findViewById(R.id.tvCateName);
		tvCateName.setText(list.get(position).getName());
		
		return v;
	}

}
