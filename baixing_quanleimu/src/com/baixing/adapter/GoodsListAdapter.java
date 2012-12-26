//liuchong@baixing.com
package com.baixing.adapter;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.os.Handler;
import android.graphics.Typeface;
import android.database.DataSetObserver;

import com.baixing.data.GlobalDataManager;
import com.baixing.entity.Ad;
import com.baixing.imageCache.ImageLoaderManager;
import com.baixing.util.Communication;
import com.baixing.util.TextUtil;
import com.baixing.util.Util;
import com.baixing.view.AdViewHistory;
import com.baixing.widget.AnimatingImageView;
import com.baixing.widget.ContextMenuItem;
import com.quanleimu.activity.R;


public class GoodsListAdapter extends BaseAdapter {

	public static final class GroupItem
	{
		public String filterHint;
		public int resultCount;
	}
	
	private Context context;
	private List<Ad> list = new ArrayList<Ad>();
	private List<GroupItem> groups  = new ArrayList<GoodsListAdapter.GroupItem>();
	private boolean hasDelBtn = false;
	private Bitmap defaultBk2;
	private Bitmap downloadFailBk;
//	private AnimationDrawable loadingBK;
	private Handler handler = null;
	private int messageWhat = -1;
	private boolean uiHold = false; 
	private boolean showImage = true;
	private AdViewHistory vadHistory;
	
//	private RelativeLayout.LayoutParams lp = null;
	
	public void releaseResource(){
		Thread t = new Thread(new Runnable(){
			public void run(){
//				Helper.saveDataToLocate(QuanleimuApplication.getApplication().getApplicationContext(), "listLookHistory", QuanleimuApplication.getApplication().getListLookHistory());
				try{
					Thread.sleep(2000);
//					if(defaultBk2 != null && defaultBk2.get() != null){
					if(defaultBk2 != null){
//						defaultBk2.get().recycle();
						defaultBk2.recycle();
						defaultBk2 = null;
					}
//					if(null != downloadFailBk && null != downloadFailBk.get()){
					if(null != downloadFailBk){
//						downloadFailBk.get().recycle();
						downloadFailBk.recycle();
						downloadFailBk = null;
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		});
		t.start();

	}
	
	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
	    if (observer != null) {
	        super.unregisterDataSetObserver(observer);
	    }
	}

	public void setUiHold(boolean hold){
		uiHold = hold;
	}

    public void setOperateMessage(Handler h, int messageWhat){
        this.handler = h;
        this.messageWhat = messageWhat;
    }
	
	public void setHasDelBtn(boolean has){
		hasDelBtn = has;
	}

	public List<Ad> getList() {
		return list;
	}

	public void setList(List<Ad> list) {
		this.list = list;
	}
	
	public void updateGroups(List<GroupItem> outerGroup)
	{
		this.groups.clear();
		if (outerGroup != null)
		{
			this.groups.addAll(outerGroup);
		}
	}
	
	public void setList(List<Ad> list, List<GroupItem> outerGroup) {
		this.list = list;
		updateGroups(outerGroup);
	}

	public GoodsListAdapter(Context context, List<Ad> list, AdViewHistory adViewHistory) {
		super();
		this.context = context;
		this.list = list;
		showImage = !GlobalDataManager.isTextMode() || Communication.isWifiConnection();
		vadHistory = adViewHistory;
	}
	
	public void setImageVisible(boolean showImage)
	{
		this.showImage = showImage;
	}

	@Override
	public int getCount() {
		return this.getGroupCount()  + ((list == null || 0 == list.size()) ? 0 : list.size());
	}
	
	private int getGroupCount()
	{
		return groups == null ? 0 : groups.size();
	}

	@Override
	public Object getItem(int arg0) {
		return arg0;
	}

	@Override
	public long getItemId(int position) {
		
		return this.getRealIndex(position);
	}
	
	static class ViewHolder{
		TextView tvDes;
		TextView tvPrice;
		TextView tvDateAndAddress;
		TextView tvUpdateDate;
        View operateView;
		View actionLine;
		ImageView ivInfo;
		View pbView;
		View divider;
	}
	
	private boolean isGroupPosition(int pos)
	{
		if (groups == null  || groups.size() == 0)
		{
			return false;
		}

		int skip = 0;
		for (int i=0; i<groups.size(); skip += groups.get(i).resultCount + 1, i++)
		{
			final int expIndex = skip;//i == 0 ? 0 : groups.get(i-1).resultCount + skip;
			if (expIndex == pos)
			{
				Log.d("LIST", "position is group " + pos);
				return true;
			}
		}
		
		return false;
	}
	
	private GroupItem findGroupByPos(int position)
	{
		int skip = 0;
		for (int i=0; i<groups.size(); skip += groups.get(i).resultCount + 1, i++)
		{
			final int expIndex = skip;//i == 0 ? 0 : groups.get(i-1).resultCount + skip;
			if (expIndex == position)
			{
				return groups.get(i);
			}
		}
		
		return null;
	}
	
	private int getRealIndex(int position)
	{
		if (groups == null || groups.size() == 0)
		{
			return position;
		}
		
		if (isGroupPosition(position))
		{
			return -1;
		}
		
		int skip = 0;
		for (int i=0; i<groups.size(); skip += (groups.get(i).resultCount + 1), i++)
		{
			if (position <= (skip + groups.get(i).resultCount))
			{
				return position - i - 1;
			}
		}
		
		return -1;
	}
	
	
	@Override
	public View getView(final int pos, View convertView, ViewGroup parent) {
//		Log.d("goodslistadapter", "hahaha, position: " + position);
//		if(list == null || 0 == list.size()){
//			View v = null;
//			
//			if(0 == pos){			
//				
//				LayoutInflater inflater = LayoutInflater.from(context);
//				v = inflater.inflate(R.layout.goodslist_empty_hint, null);
//			}
//			return v;
//		}else
		{
			ViewHolder holder;
			View v = convertView;
			if(v == null || v.getTag() == null || !(v.getTag() instanceof ViewHolder)){
				LayoutInflater inflater = LayoutInflater.from(context);
				v = inflater.inflate(R.layout.item_goodlist_with_title, null);
				holder = new ViewHolder();
				holder.tvDes = (TextView) v.findViewById(R.id.tvDes);
				holder.tvPrice = (TextView) v.findViewById(R.id.tvPrice);
				holder.tvDateAndAddress = (TextView) v.findViewById(R.id.tvDateAndAddress);
				holder.operateView =  v.findViewById(R.id.rlListOperate);
				holder.actionLine = v.findViewById(R.id.lineView);
				holder.ivInfo = (ImageView) v.findViewById(R.id.ivInfo);
				holder.pbView = v.findViewById(R.id.pbLoadingProgress);
				holder.tvUpdateDate = (TextView) v.findViewById(R.id.tvUpdateDate);
				holder.divider = v.findViewById(R.id.vad_divider);
				((AnimatingImageView)holder.ivInfo).setForefrontView(holder.pbView);
				v.setTag(holder);
			}
			else{
				holder = (ViewHolder)v.getTag();
			}
			
			if (isGroupPosition(pos))
			{
				v.findViewById(R.id.filter_view_root).setVisibility(View.VISIBLE);
				v.findViewById(R.id.goods_item_view_root).setVisibility(View.GONE);
				GroupItem g = findGroupByPos(pos);
				TextView text = (TextView) v.findViewById(R.id.filter_view_root).findViewById(R.id.filter_string);
				text.setText(g.filterHint);
				TextView countTxt = (TextView) v.findViewById(R.id.filter_view_root).findViewById(R.id.filter_result_count);
				countTxt.setText(g.resultCount + "");
				holder.divider.setVisibility(View.GONE);
				v.setEnabled(false);	
				return v;
			}
			else
			{
				v.setEnabled(true);
				holder.divider.setVisibility(View.VISIBLE);
				v.findViewById(R.id.filter_view_root).setVisibility(View.GONE);
				v.findViewById(R.id.goods_item_view_root).setVisibility(View.VISIBLE);
			}
			
			final int position = getRealIndex(pos);
//			Log.e("LIST", "position translate from " + pos + "-->" + position);
	
			if(null == defaultBk2){
				BitmapFactory.Options o =  new BitmapFactory.Options();
		        o.inPurgeable = true;
//				defaultBk2 = new WeakReference<Bitmap>(BitmapFactory.decodeResource(context.getResources(),R.drawable.icon_listing_nopic, o));
		        defaultBk2 = BitmapFactory.decodeResource(context.getResources(),R.drawable.icon_listing_nopic, o);
			}
			
			if(null == downloadFailBk){				
				BitmapFactory.Options o =  new BitmapFactory.Options();
		        o.inPurgeable = true;
//				downloadFailBk = new WeakReference<Bitmap>(BitmapFactory.decodeResource(context.getResources(), R.drawable.home_bg_thumb_2x, o));				
		        downloadFailBk = BitmapFactory.decodeResource(context.getResources(), R.drawable.home_bg_thumb_2x, o);
			}
			
			holder.ivInfo.setScaleType(ImageView.ScaleType.CENTER_CROP);
				
//			boolean showImage = !QuanleimuApplication.isTextMode() || Communication.isWifiConnection();
			if(!showImage){
				holder.ivInfo.setVisibility(View.GONE);
			}
			
//			lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			if(showImage){
				
				List<String> listUrlsToCancel = new ArrayList<String>();
				
				String strTag = null;
				if(null != holder.ivInfo.getTag()) strTag = holder.ivInfo.getTag().toString();
				
//				holder.ivInfo.setLayoutParams(lp);
				
				if (list.get(position).getImageList() == null
						|| list.get(position).getImageList().equals("")
						|| list.get(position).getImageList().getSquare() == null
						|| list.get(position).getImageList().getSquare().equals("")) {
					
					if(null != strTag && strTag.length() > 0)
						listUrlsToCancel.add(strTag);
					
					if(strTag == null || strTag.length() > 0){
						holder.ivInfo.setTag("");
//						holder.ivInfo.setImageBitmap(defaultBk2.get());
						holder.ivInfo.setImageBitmap(defaultBk2);
					}
				} else {
						String b = (list.get(position).getImageList().getSquare());
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
//									holder.ivInfo.setImageBitmap(defaultBk2.get());
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
//									SimpleImageLoader.showImg(holder.ivInfo, c[0], strTag, this.context, downloadFailBk);//R.drawable.home_bg_thumb_2x);
									ImageLoaderManager.getInstance().showImg(holder.ivInfo, c[0], strTag, this.context, 
											new WeakReference<Bitmap>(downloadFailBk));//R.drawable.home_bg_thumb_2x);
									//Log.d("GoodsListAdapter load image", "showImg for : "+position+", @url:"+c[0]);
								}
							}
						} else {
							if (b == null || b.equals("")) {
								
								if(null != strTag && strTag.length() > 0)
									listUrlsToCancel.add(strTag);
								
								if(strTag == null || strTag.length() > 0){
									holder.ivInfo.setTag("");
//									holder.ivInfo.setImageBitmap(defaultBk2.get());
									holder.ivInfo.setImageBitmap(defaultBk2);
								}
							} else {
								
								if(null != strTag && strTag.length() > 0 && !strTag.equals(b))
									listUrlsToCancel.add(strTag);
								
								if(null == strTag || !strTag.equals(b)){
									holder.ivInfo.setTag(b);
									holder.ivInfo.setVisibility(View.INVISIBLE);
									holder.pbView.setVisibility(View.VISIBLE);
//									SimpleImageLoader.showImg(holder.ivInfo, b, strTag, this.context, downloadFailBk);//R.drawable.home_bg_thumb_2x);
									ImageLoaderManager.getInstance().showImg(holder.ivInfo, b, strTag, this.context, 
											new WeakReference<Bitmap>(downloadFailBk));//R.drawable.home_bg_thumb_2x);
									//Log.d("GoodsListAdapter load image", "showImg: "+position+", @url:"+b);
								}
							}
						}
		//			}
				}
				
				if(listUrlsToCancel.size() > 0){
					
					for(String url : listUrlsToCancel){
						//Log.d("GoodsListAdapter canceled image", "canceled: "+url);
						
						ImageLoaderManager.getInstance().Cancel(url, holder.ivInfo);
					}
				}
			}
			
			final Ad detailObj = list.get(position);
			final boolean isValidMessage = this.isValidMessage(detailObj);
			if (hasDelBtn) {
				holder.operateView.setVisibility(View.VISIBLE);
				holder.actionLine.setVisibility(View.VISIBLE);
				holder.operateView.findViewById(R.id.btnListOperate).setBackgroundResource(isValidMessage ? R.drawable.btn_circle_arrow : R.drawable.icon_warning);
			} 
			else{
				holder.operateView.setVisibility(View.GONE);
				holder.actionLine.setVisibility(View.GONE);
			}
			
//			String price = list.get(position).getMetaValueByKey("价格");
			String price = detailObj.getValueByKey("价格");
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
			holder.tvDes.setText(detailObj.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_TITLE));
//			holder.tvDes.setText(title);
			holder.tvDes.setTypeface(null, Typeface.BOLD);
			if (vadHistory != null && vadHistory.isReaded(detailObj.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_ID)))
			{
				holder.tvDes.setTextColor(context.getResources().getColor(R.color.vad_meta_label));
			}	
			else
			{
				holder.tvDes.setTextColor(context.getResources().getColor(R.color.common_black));
			}
			
			String dateV = detailObj.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_DATE);
			if(dateV != null && !dateV.equals(""))
			{
				Date date = new Date(Long.parseLong(dateV) * 1000);
				holder.tvUpdateDate.setText(TextUtil.timeTillNow(date.getTime(), v.getContext()));
				
			}
			else
			{
				holder.tvUpdateDate.setText("");
				holder.tvUpdateDate.setVisibility(View.INVISIBLE);
			}
			
			
			String areaV = list.get(position).getValueByKey(Ad.EDATAKEYS.EDATAKEYS_AREANAME);
			if(areaV != null && !areaV.equals(""))
			{
				holder.tvDateAndAddress.setText(areaV);
			}
			else
			{
				holder.tvDateAndAddress.setText("");
			}
			
			
			if (isValidMessage){
				if(price != null && price.length() > 0){
					holder.tvPrice.setVisibility(View.VISIBLE);
				}
				holder.tvUpdateDate.setVisibility(View.VISIBLE);
				holder.tvDateAndAddress.setTextColor(context.getResources().getColor(R.color.vad_list_sub_info));
			}
			else
			{
				holder.tvPrice.setVisibility(View.GONE);
				holder.tvUpdateDate.setVisibility(View.GONE);
				holder.tvDateAndAddress.setText("审核未通过");
				holder.tvDateAndAddress.setTextColor(Color.RED);
			}
			
			
	
			holder.operateView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
                    Message msg = handler.obtainMessage();
                    msg.arg2 = position;
                    msg.what = messageWhat;
                    handler.sendMessage(msg);
				}
			});	
			return v;
		}
		
	}
	
	private boolean isValidMessage(Ad detail)
	{
		return !detail.getValueByKey("status").equals("4") && !detail.getValueByKey("status").equals("20");
	}
}