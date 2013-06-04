package com.baixing.widget;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.TextUtils;
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
import com.baixing.entity.UserBean;
import com.baixing.util.FavoriteNetworkUtil;
import com.baixing.util.Util;
import com.baixing.util.ViewUtil;
import com.baixing.view.vad.VadLogger;
import com.quanleimu.activity.R;

public class FavAndReportDialog extends Dialog implements
		android.view.View.OnClickListener {

	final public static int MSG_PROSECUTE = 0x1234F0F0;
	private Ad mAd;
	private BaseActivity activity;
	private Handler outHandler;
	public FavAndReportDialog(BaseActivity activity, Ad ad, Handler handler) {
		super(activity);
		this.outHandler = handler;
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
	
	private void addAdToFavorite(){
		List<Ad> myStore = GlobalDataManager.getInstance().addFav(mAd); 
		
//		if (myStore != null){
//			Util.saveDataToLocate(GlobalDataManager.getInstance().getApplicationContext(), "listMyStore", myStore);
//		}
		
		((Button)findViewById(R.id.favButton)).setText("取消收藏");		
		Drawable left = activity.getResources().getDrawable(R.drawable.viewad_icon_fav);
		left.setBounds(0, 0, left.getIntrinsicWidth(), left.getIntrinsicHeight());
		((Button)findViewById(R.id.favButton)).setCompoundDrawables(left, null, null, null);
	}
	
	private void removeAdFromFavorite(){
//		List<Ad> favList = 
				GlobalDataManager.getInstance().removeFav(mAd);
//		Util.saveDataToLocate(this.getContext(), "listMyStore", favList);
		
		((Button)findViewById(R.id.favButton)).setText("收藏");
		Drawable left = activity.getResources().getDrawable(R.drawable.viewad_icon_unfav);
		left.setBounds(0, 0, left.getIntrinsicWidth(), left.getIntrinsicHeight());				
		((Button)findViewById(R.id.favButton)).setCompoundDrawables(left, null, null, null);
	}
	
	private void handleRequestSuccess(String response){
		try {
			JSONObject jb = new JSONObject(response);
			JSONObject js = jb.getJSONObject("error");
			String message = js.getString("message");
			ViewUtil.showToast(activity, message, false);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	Handler handler = new Handler(){
		@Override
		public void handleMessage(android.os.Message msg){
			ViewUtil.hideSimpleProgress(pd);
			switch(msg.what){
			case FavoriteNetworkUtil.MSG_ADD_FAVORITE_FAIL:
				ViewUtil.showToast(activity, (String)msg.obj, false);
				break;
			case FavoriteNetworkUtil.MSG_ADD_FAVORITE_SUCCESS:
				addAdToFavorite();
				handleRequestSuccess(((FavoriteNetworkUtil.ReplyData)msg.obj).response);
				break;
			case FavoriteNetworkUtil.MSG_CANCEL_FAVORITE_FAIL:
				ViewUtil.showToast(activity, (String)msg.obj, false);
				break;
			case FavoriteNetworkUtil.MSG_CANCEL_FAVORITE_SUCCESS:
				removeAdFromFavorite();
				handleRequestSuccess(((FavoriteNetworkUtil.ReplyData)msg.obj).response);
				break;

			}
		}
	};
	
	private ProgressDialog pd; 
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v.getId() == R.id.favButton){
			VadLogger.trackLikeUnlike(mAd);
			
			UserBean user = GlobalDataManager.getInstance().getAccountManager().getCurrentUser();
			boolean isValidUser = (user != null && !TextUtils.isEmpty(user.getPhone()));
			if(!GlobalDataManager.getInstance().isFav(mAd)){
				if(isValidUser){
					ViewUtil.hideSimpleProgress(pd);
					pd = ViewUtil.showSimpleProgress(activity);
					FavoriteNetworkUtil.addFavorite(activity, mAd.getValueByKey(EDATAKEYS.EDATAKEYS_ID), user, this.handler);
				}else{
					addAdToFavorite();
					ViewUtil.showToast(this.getContext(), "收藏成功", true);
				}
			}
			else  {
				if(isValidUser){
					ViewUtil.hideSimpleProgress(pd);
					pd = ViewUtil.showSimpleProgress(activity);					
					FavoriteNetworkUtil.cancelFavorite(activity, mAd.getValueByKey(EDATAKEYS.EDATAKEYS_ID), user, this.handler);
				}else{
					removeAdFromFavorite();
					ViewUtil.showToast(this.getContext(), "取消收藏", true);
				}
			}
			this.dismiss();
		}else if(v.getId() == R.id.reportBtn){
			outHandler.sendEmptyMessage(MSG_PROSECUTE);
			this.dismiss();
		}
	}
}