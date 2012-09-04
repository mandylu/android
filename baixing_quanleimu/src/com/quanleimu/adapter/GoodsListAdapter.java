package com.quanleimu.adapter;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.os.Handler;
import android.graphics.Typeface;
import android.database.DataSetObserver;

import com.quanleimu.activity.QuanleimuApplication;
import com.quanleimu.activity.R;
import com.quanleimu.entity.GoodsDetail;
import com.quanleimu.imageCache.SimpleImageLoader;
import com.quanleimu.util.Communication;
import com.quanleimu.util.Util;
import com.quanleimu.widget.AnimatingImageView;


public class GoodsListAdapter extends BaseAdapter {

	private Context context;
	private List<GoodsDetail> list = new ArrayList<GoodsDetail>();
	private boolean hasDelBtn = false;
	private Bitmap defaultBk2;
//	private AnimationDrawable loadingBK;
	private Handler handler = null;
	private int messageWhat = -1;
	private boolean uiHold = false;
	private RelativeLayout.LayoutParams lp = null;
	
	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
	    if (observer != null) {
	        super.unregisterDataSetObserver(observer);
	    }
	}

	public void setUiHold(boolean hold){
		uiHold = hold;
	}
	

	public void setMessageOutOnDelete(Handler h, int messageWhat){
		this.handler = h;
		this.messageWhat = messageWhat;
	}
	
	public void setHasDelBtn(boolean has){
		hasDelBtn = has;
	}

	public List<GoodsDetail> getList() {
		return list;
	}

	public void setList(List<GoodsDetail> list) {
		this.list = list;
	}

	public GoodsListAdapter(Context context, List<GoodsDetail> list) {
		super();
		this.context = context;
		this.list = list;
		int type = Util.getWidthByContext(context);
		
		switch (type) {
		case 240:
			lp = new RelativeLayout.LayoutParams(45, 45);
			break;
		case 320:
			lp = new RelativeLayout.LayoutParams(60, 60);
			break;
		case 480:
			lp = new RelativeLayout.LayoutParams(90, 90);
			break;
		case 540:
			lp = new RelativeLayout.LayoutParams(100, 100);
			break;
		case 640:
			lp = new RelativeLayout.LayoutParams(120, 120);
			break;
		default:
			 lp= new RelativeLayout.LayoutParams(140,140);
			break;
		}
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return (list == null || 0 == list.size()) ? 1 : list.size();
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
	
	static class ViewHolder{
		TextView tvDes;
		TextView tvPrice;
		TextView tvDateAndAddress;
		Button btnDelete;
		ImageView ivInfo;
		View pbView;
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		if(list == null || 0 == list.size()){
			View v = null;
			
			if(0 == position){			
				
				LayoutInflater inflater = LayoutInflater.from(context);
				v = inflater.inflate(R.layout.goodslist_empty_hint, null);
			}
			
			return v;
		}else{
			ViewHolder holder;
			View v = convertView;
			if(v == null || v.getTag() == null || !(v.getTag() instanceof ViewHolder)){
				LayoutInflater inflater = LayoutInflater.from(context);
				v = inflater.inflate(R.layout.item_goodslist, null);
				holder = new ViewHolder();
				holder.tvDes = (TextView) v.findViewById(R.id.tvDes);
				holder.tvPrice = (TextView) v.findViewById(R.id.tvPrice);
				holder.tvDateAndAddress = (TextView) v.findViewById(R.id.tvDateAndAddress);
				holder.btnDelete = (Button) v.findViewById(R.id.btnDelete);
				holder.ivInfo = (ImageView) v.findViewById(R.id.ivInfo);
				holder.pbView = v.findViewById(R.id.pbLoadingProgress);
				((AnimatingImageView)holder.ivInfo).setForefrontView(holder.pbView);
				v.setTag(holder);
			}
			else{
				holder = (ViewHolder)v.getTag();
			}
	
			if (!hasDelBtn) {
				holder.btnDelete.setVisibility(View.GONE);
			} 
			else{
				holder.btnDelete.setVisibility(View.VISIBLE);
			}
	
			if(null == defaultBk2){
				BitmapFactory.Options o =  new BitmapFactory.Options();
		        o.inPurgeable = true;
				Bitmap tmb1 = BitmapFactory.decodeResource(context.getResources(),R.drawable.home_bg_thumb_2x, o);
				defaultBk2 = tmb1;
			}
			
			holder.ivInfo.setScaleType(ImageView.ScaleType.CENTER_CROP);
				
			if(QuanleimuApplication.isTextMode()){
				holder.ivInfo.setVisibility(View.GONE);
			}
			
			lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			if(!QuanleimuApplication.isTextMode()){
				
				List<String> listUrlsToCancel = new ArrayList<String>();
				
				String strTag = null;
				if(null != holder.ivInfo.getTag()) strTag = holder.ivInfo.getTag().toString();
				
				holder.ivInfo.setLayoutParams(lp);
				
				if (list.get(position).getImageList() == null
						|| list.get(position).getImageList().equals("")
						|| list.get(position).getImageList().getResize180() == null
						|| list.get(position).getImageList().getResize180()
								.equals("")) {
					
					if(null != strTag && strTag.length() > 0)
						listUrlsToCancel.add(strTag);
					
					if(strTag == null || strTag.length() > 0){
						holder.ivInfo.setTag("");
						holder.ivInfo.setImageBitmap(defaultBk2);	
					}
				} else {
						String b = (list.get(position).getImageList().getResize180());
//						.substring(1, (list.get(position).getImageList()
//								.getResize180()).length() - 1);
						b = Communication.replace(b);
				
						if (b.contains(",")) {
							String[] c = b.split(",");
							if (c[0] == null || c[0].equals("")) {
								
								if(null != v.getTag() && v.getTag().toString().length() > 0)
									listUrlsToCancel.add(v.getTag().toString());
								
								if(strTag == null || strTag.length() > 0){
									holder.ivInfo.setTag("");
									holder.ivInfo.setImageBitmap(defaultBk2);
		//							ivInfo.invalidate();
								}
							} else {
								
								if(null != strTag && strTag.length() > 0 && !strTag.equals(c[0]))
									listUrlsToCancel.add(strTag);
								
								if(null == strTag || !strTag.equals(c[0])){
									holder.ivInfo.setTag(c[0]);
									holder.ivInfo.setVisibility(View.INVISIBLE);
									holder.pbView.setVisibility(View.VISIBLE);
									SimpleImageLoader.showImg(holder.ivInfo, c[0], this.context, R.drawable.home_bg_thumb_2x);
									//Log.d("GoodsListAdapter load image", "showImg for : "+position+", @url:"+c[0]);
								}
							}
						} else {
							if (b == null || b.equals("")) {
								
								if(null != strTag && strTag.length() > 0)
									listUrlsToCancel.add(strTag);
								
								if(strTag == null || strTag.length() > 0){
									holder.ivInfo.setTag("");
									holder.ivInfo.setImageBitmap(defaultBk2);
								}
							} else {
								
								if(null != strTag && strTag.length() > 0 && !strTag.equals(b))
									listUrlsToCancel.add(strTag);
								
								if(null == strTag || !strTag.equals(b)){
									holder.ivInfo.setTag(b);
									holder.ivInfo.setVisibility(View.INVISIBLE);
									holder.pbView.setVisibility(View.VISIBLE);
									SimpleImageLoader.showImg(holder.ivInfo, b, this.context, R.drawable.home_bg_thumb_2x);
									//Log.d("GoodsListAdapter load image", "showImg: "+position+", @url:"+b);
								}
							}
						}
		//			}
				}
				
				if(listUrlsToCancel.size() > 0){
					
					for(String url : listUrlsToCancel){
						//Log.d("GoodsListAdapter canceled image", "canceled: "+url);
						
						SimpleImageLoader.Cancel(url, holder.ivInfo);
					}
				}
			}
			String price = list.get(position).getMetaValueByKey("价格");
			if (price == null || price.equals("")) {
				holder.tvPrice.setVisibility(View.GONE);
			} else {
				holder.tvPrice.setVisibility(View.VISIBLE);
				holder.tvPrice.setText(price);
			}
//			String title = list.get(position).getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_TITLE);
//			TextPaint tp = holder.tvDes.getPaint();
//			int chars = tp.breakText(title, true, holder.tvDes.getWidth(), null);
//			if(chars < title.length()){
//				title = title.substring(0, chars);
//			}
			holder.tvDes.setText(list.get(position).getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_TITLE));
//			holder.tvDes.setText(title);
			holder.tvDes.setTypeface(null, Typeface.BOLD);
	
			String dateV = list.get(position).getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_DATE);
			if(dateV != null && !dateV.equals(""))
			{
				Date date = new Date(Long.parseLong(dateV) * 1000);
				SimpleDateFormat df = new SimpleDateFormat("MM月dd日",
						Locale.SIMPLIFIED_CHINESE);
				
				String areaV = list.get(position).getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_AREANAME);
				if(areaV != null && !areaV.equals(""))
				{
					holder.tvDateAndAddress.setText(df.format(date) + " "
							+ areaV);
				}
				else
				{
					holder.tvDateAndAddress.setText(df.format(date));
				}
			}
			else
			{
				String areaV = list.get(position).getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_AREANAME);
				if(areaV != null && !areaV.equals(""))
				{
					holder.tvDateAndAddress.setText(areaV);
				}
				else
				{
					holder.tvDateAndAddress.setVisibility(View.GONE);
				}
			}
	
			holder.btnDelete.setOnClickListener(new View.OnClickListener() {
	
				@Override
				public void onClick(View v) {
					if(uiHold) return;
					uiHold = true;
					if(GoodsListAdapter.this.handler == null) return;
					Message msg = GoodsListAdapter.this.handler.obtainMessage();
					msg.arg2 = position;
					msg.what = GoodsListAdapter.this.messageWhat;
	//				list.remove(position);
					GoodsListAdapter.this.notifyDataSetChanged();
					GoodsListAdapter.this.handler.sendMessage(msg);
				}
			});
			return v;
		}
	}
}