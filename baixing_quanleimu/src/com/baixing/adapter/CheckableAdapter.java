package com.baixing.adapter;

import java.util.ArrayList;
import java.util.List;

import com.baixing.adapter.BXAlphabetSortableAdapter;
import com.baixing.adapter.BXAlphabetSortableAdapter.BXPinyinSortItem;
import com.baixing.adapter.CommonItemAdapter.ViewHolder;
import com.quanleimu.activity.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.CheckBox;

public class CheckableAdapter extends BXAlphabetSortableAdapter {
	public static class CheckableItem extends Object{
		public String txt;
		public boolean checked;
		public String id;
		@Override
		public String toString(){
			return txt;
		}
	}
//	private Context context;
//	private List<? extends CheckableItem> list = new ArrayList<CheckableItem>();
	private Object tag;
	private int checkedResourceId = -1;//R.drawable.pic_radio_normal_2x;
	private int uncheckedResourceId = -1;//R.drawable.pic_radio_selected_2x;
	private boolean sorted = false;
	private boolean hasSearchBar = false;
	
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
	
	public void setItemCheckStatus(int position, boolean check){
		CheckableItem item = getItem(position) instanceof BXPinyinSortItem ? 
				(CheckableItem)((BXPinyinSortItem)getItem(position)).obj
				: (CheckableItem)getItem(position);
		item.checked = check;
		this.list.remove(position);
		this.list.add(position, item);

	}
	

	public CheckableAdapter(Context context,List<? extends CheckableItem> list, int sortIfMoreThan, boolean hasSearchBar) {
		super(context, list, list != null && list.size() > sortIfMoreThan);
		sorted = (list != null && list.size() > sortIfMoreThan);
		if(sorted && hasSearchBar){
			this.list.add(0, "placeholder");
		}
		this.hasSearchBar = hasSearchBar;
//		this.context = context;
//		this.list = list;
	}
		
	public void setList(List<? extends CheckableItem> list_){
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
	
	public void setIconId(int checkedId, int uncheckedId){
		this.checkedResourceId = checkedId;
		this.uncheckedResourceId = uncheckedId;
	}
	
	class ViewHolder{
		TextView tvCateName;
		CheckBox box;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		
		if(position == 0 && sorted && hasSearchBar){
			return inflater.inflate(R.layout.searchbar, null);
		}
		
		View header = getHeaderIfItIs(position, convertView);
		if(header != null){
			return header;
		}

		View v = null;
		ViewHolder holder;
		if(convertView == null || convertView.getTag() == null || !(convertView.getTag() instanceof ViewHolder))
		{
			v = inflater.inflate(R.layout.item_text_checkbox, null);
			holder = new ViewHolder();
			holder.tvCateName = (TextView)v.findViewById(R.id.checktext);
			holder.box = (CheckBox)v.findViewById(R.id.checkitem);
			v.setTag(holder);
		}else{
			v = (View)convertView; 
			holder = (ViewHolder)v.getTag();
		}
		
		if(left >= 0 && right >= 0 && top >= 0 && bottom >= 0){
			v.setPadding(left, top, right, bottom);
		}
		
		holder.tvCateName.setText(list.get(position).toString());
		
		CheckableItem item = (list.get(position) instanceof BXPinyinSortItem) ? 
				(CheckableItem)((BXPinyinSortItem)list.get(position)).obj : (CheckableItem)list.get(position);
		holder.box.setChecked(item.checked);
		if(this.checkedResourceId > 0 && this.uncheckedResourceId > 0){
			if(item.checked){
				holder.box.setButtonDrawable(this.checkedResourceId);
			}
			else{
				holder.box.setButtonDrawable(this.uncheckedResourceId);
			}
		}
		return v;
	}

}
