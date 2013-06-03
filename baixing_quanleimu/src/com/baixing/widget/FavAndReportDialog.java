package com.baixing.widget;

import java.util.List;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.baixing.activity.BaseActivity;
import com.baixing.data.GlobalDataManager;
import com.baixing.entity.Ad;
import com.baixing.entity.Ad.EDATAKEYS;
import com.baixing.util.Util;
import com.baixing.util.ViewUtil;
import com.baixing.view.fragment.FeedbackFragment;
import com.baixing.view.vad.VadLogger;
import com.quanleimu.activity.R;

public class FavAndReportDialog extends Dialog implements
		android.view.View.OnClickListener {

	final public static int MSG_PROSECUTE = 0x1234F0F0;
	private Ad mAd;
	private BaseActivity activity;
	private Handler handler;
	public FavAndReportDialog(BaseActivity activity, Ad ad, Handler handler) {
		super(activity);
		this.handler = handler;
		this.activity = activity;
		// TODO Auto-generated constructor stub
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setBackgroundDrawable(new ColorDrawable(0));

		View v = LayoutInflater.from(getContext()).inflate(R.layout.popup_fav_report, null);
		v.findViewById(R.id.favButton).setOnClickListener(this);
		v.findViewById(R.id.reportBtn).setOnClickListener(this);
		this.setContentView(v);
//		
		if(GlobalDataManager.getInstance().isFav(ad)){
			
			Drawable left = activity.getResources().getDrawable(R.drawable.viewad_icon_fav);
			left.setBounds(0, 0, left.getIntrinsicWidth(), left.getIntrinsicHeight());
			((Button)v.findViewById(R.id.favButton)).setCompoundDrawables(left, null, null, null);
			((Button)v.findViewById(R.id.favButton)).setText("取消收藏");
		}

		Window window = getWindow();
		window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

		setCancelable(true);
		setCanceledOnTouchOutside(true);
		
		mAd = ad;
	}

	public void show(int x, int y) {
		WindowManager.LayoutParams wmlp = getWindow().getAttributes();
		wmlp.gravity = Gravity.TOP | Gravity.LEFT;
		wmlp.x = x;
		wmlp.y = y;
		show();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v.getId() == R.id.favButton){
			VadLogger.trackLikeUnlike(mAd);
			
			if(!GlobalDataManager.getInstance().isFav(mAd)){			
				List<Ad> myStore = GlobalDataManager.getInstance().addFav(mAd); 
				
				if (myStore != null)
				{
					Util.saveDataToLocate(GlobalDataManager.getInstance().getApplicationContext(), "listMyStore", myStore);
				}
				((Button)v).setText("取消收藏");		
				Drawable left = activity.getResources().getDrawable(R.drawable.viewad_icon_fav);
				left.setBounds(0, 0, left.getIntrinsicWidth(), left.getIntrinsicHeight());

				((Button)v).setCompoundDrawables(left, null, null, null);
//				updateTitleBar(getTitleDef());
				ViewUtil.showToast(this.getContext(), "收藏成功", true);
			}
			else  {
				List<Ad> favList = GlobalDataManager.getInstance().removeFav(mAd);
				Util.saveDataToLocate(this.getContext(), "listMyStore", favList);
//				updateTitleBar(getTitleDef());
				ViewUtil.showToast(this.getContext(), "取消收藏", true);
				((Button)v).setText("收藏");
				
				Drawable left = activity.getResources().getDrawable(R.drawable.viewad_icon_unfav);
				left.setBounds(0, 0, left.getIntrinsicWidth(), left.getIntrinsicHeight());				
				((Button)v).setCompoundDrawables(left, null, null, null);
			}
			this.dismiss();
		}else if(v.getId() == R.id.reportBtn){
			handler.sendEmptyMessage(MSG_PROSECUTE);
			this.dismiss();
		}
	}
}