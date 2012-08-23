package com.quanleimu.adapter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.quanleimu.activity.R;
import com.quanleimu.adapter.GridAdapter.GridHolder;
import com.quanleimu.adapter.GridAdapter.GridInfo;
import com.quanleimu.entity.ChatSession;
import com.quanleimu.entity.FirstStepCate;
import com.quanleimu.imageCache.SimpleImageLoader;

public class SessionListAdapter extends BaseAdapter {
	private List<ChatSession> list = null;
	private Context context;
	private LayoutInflater mInflater;

	public List<ChatSession> getList() {
		return list;
	}

	public void setList(List<ChatSession> list) {
		this.list = list;
	}

	public SessionListAdapter(Context context, List<ChatSession> list) {
		super();
		this.context = context;
		this.list = list;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list == null ? 0 : list.size();
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

	class SessionHolder {
		public TextView userAndAd;
		public TextView lastChat;
		public TextView lastTime;
		public ImageView image;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		SessionHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.item_sessionlist, null);
			holder = new SessionHolder();
			holder.userAndAd = (TextView) convertView.findViewById(R.id.tvUserAndAd);
			holder.lastChat = (TextView) convertView.findViewById(R.id.tvLastMsg);
			holder.lastTime = (TextView) convertView.findViewById(R.id.tvTimeAndDate);
			holder.image = (ImageView) convertView.findViewById(R.id.userImage);
			convertView.setTag(holder);

		} else {
			holder = (SessionHolder) convertView.getTag();

		}
		ChatSession info = list.get(position);
		if (info != null) {
			holder.userAndAd.setText(info.getOppositeNick() + "-" + info.getAdTitle());
			holder.lastChat.setText(info.getLastMsg());
			if(info.getImageUrl() != null && !info.getImageUrl().equals("")){
				holder.image.setTag(info.getImageUrl());
				SimpleImageLoader.showImg(holder.image, info.getImageUrl(), this.context);
			}
			SimpleDateFormat sf = new SimpleDateFormat("MM-dd HH:mm", Locale.SIMPLIFIED_CHINESE);
			Date date = new Date(Long.parseLong(info.getTimeStamp()) * 1000);
			String time = sf.format(date);
			holder.lastTime.setText(time);
		}
		return convertView;
	}
}