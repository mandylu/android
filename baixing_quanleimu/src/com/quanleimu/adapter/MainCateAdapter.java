package com.quanleimu.adapter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.quanleimu.activity.QuanleimuApplication;
import com.quanleimu.activity.R;
import com.quanleimu.entity.FirstStepCate;
import com.quanleimu.imageCache.SimpleImageLoader;
import com.quanleimu.util.Communication;
import com.quanleimu.util.Helper;
import com.quanleimu.util.Util;
import android.os.Handler;
import android.graphics.Typeface;
public class MainCateAdapter extends BaseAdapter {

	private Context context;
	private TextView tvDes, tvPrice, tvDateAndAddress;
	private ImageView ivInfo;
	private List<FirstStepCate> list = new ArrayList<FirstStepCate>();
	private Handler handler = null;
	private int messageWhat = -1;
	
	public void setMessageOutOnDelete(Handler h, int messageWhat){
		this.handler = h;
		this.messageWhat = messageWhat;
	}

	public List<FirstStepCate> getList() {
		return list;
	}

	public void setList(List<FirstStepCate> list) {
		this.list = list;
	}

	public MainCateAdapter(Context context, List<FirstStepCate> list) {
		super();
		this.context = context;
		this.list = list;
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
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		
		View v = convertView;
		if(v == null){
			v = inflater.inflate(R.layout.item_maincate, null);
			v.setTag("");
		}
		
		FirstStepCate cate = this.list.get(position);
		if(!v.getTag().equals(cate.englishName)){
			int resId = R.drawable.cheliang;
			
			if(cate.englishName.equals("cheliang")){
				resId = R.drawable.cheliang;
			}else if(cate.englishName.equals("chongwuleimu")){
				resId = R.drawable.chongwuleimu;
			}else if(cate.englishName.equals("ershou")){
				resId = R.drawable.ershou;
			}else if(cate.englishName.equals("fuwu")){
				resId = R.drawable.fuwu;
			}else if(cate.englishName.equals("fang")){
				resId = R.drawable.fang;
			}else if(cate.englishName.equals("gongzuo")){
				resId = R.drawable.gongzuo;
			}else if(cate.englishName.equals("huodong")){
				resId = R.drawable.huodong;
			}else if(cate.englishName.equals("jianzhi")){
				resId = R.drawable.jianzhi;
			}else if(cate.englishName.equals("qiuzhi")){
				resId = R.drawable.qiuzhi;
			}else if(cate.englishName.equals("jiaoyupeixun")){
				resId = R.drawable.jiaoyupeixun;
			}
				
			ImageView imgView = (ImageView)v.findViewById(R.id.ivInfo);
			imgView.setImageResource(resId);
			
			
			((TextView)v.findViewById(R.id.tvName)).setText(cate.getName());
			
			String subCateString = "";
			for(int i = 0; i < cate.getChildren().size(); ++i){
				subCateString += cate.getChildren().get(i).getName();
				subCateString += " ";
			}
			((TextView)v.findViewById(R.id.tvSubNames)).setText(subCateString);
		}
		
		return v;
	}
		
		
}