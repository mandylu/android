package com.baixing.sharing.referral;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.quanleimu.activity.R;

public class ReferralAdapter extends BaseAdapter {
	
	private LayoutInflater referralInflater;
	String[] list = null;
	
	public ReferralAdapter(Context context, String[] list) {
		referralInflater = LayoutInflater.from(context);
		this.list = list;
	}
	
	public void refresh(String[] list) {
		this.list = list;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return list.length;
	}

	@Override
	public Object getItem(int position) {
		return list[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		
		if (convertView == null) {
			convertView = referralInflater.inflate(R.layout.referral_adapter, null);
			viewHolder = new ViewHolder((TextView)convertView.findViewById(R.id.referral_adapter_phone), (TextView)convertView.findViewById(R.id.referral_adapter_posts), (TextView)convertView.findViewById(R.id.referral_adapter_promotes));
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		
		String[] content = list[position].split(",");
		viewHolder.phoneNum.setText(content[0]);
		viewHolder.postNum.setText("发帖(" + content[1] + ")");
		viewHolder.promoteNum.setText("推广(" + content[2] + ")");
		
		return convertView;
	}
	
	static class ViewHolder {
        TextView phoneNum;
        TextView postNum;
        TextView promoteNum;
    
        public ViewHolder(TextView phoneNum, TextView postNum, TextView promoteNum){
            this.phoneNum = phoneNum;
            this.postNum = postNum;
            this.promoteNum = promoteNum;
        }
    }

}
