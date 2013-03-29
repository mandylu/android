package com.baixing.view.fragment;

import com.baixing.activity.BaseActivity;
import com.baixing.entity.Ad;
import com.baixing.sharing.SharingCenter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

class SharingFragment extends DialogFragment{
	private Ad mAd;
	public SharingFragment(Ad ad, String shareFrom){
		mAd = ad;
		SharingCenter.shareFrom = shareFrom;
		SharingCenter.adId = mAd.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_ID);
		SharingCenter.categoryName = mAd.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_CATEGORYENGLISHNAME);
	}
	
	public SharingFragment(){
		
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceBundle){
		final boolean wxInstalled = SharingCenter.isWeixinInstalled(getActivity());
		String[] namesInstalled = {"转发到微信朋友圈", "转发到微信好友", "转发到QQ空间", "转发到新浪微博"};
		String[] namesNotInstalled = {"转发到QQ空间", "转发到新浪微博"};
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).setTitle("发布成功！请朋友帮忙转发")
		.setItems(wxInstalled ? namesInstalled : namesNotInstalled, new DialogInterface.OnClickListener(){
			
			@Override
			public void onClick(DialogInterface dialog, int which){
				dismiss();
				switch(which){
				case 0:
					if(wxInstalled){
						SharingCenter.share2Weixin((BaseActivity)getActivity(), mAd, true);
					}else{
						SharingCenter.share2QZone((BaseActivity)getActivity(), mAd);
					}					
					break;
				case 1:
					if(wxInstalled){
						SharingCenter.share2Weixin((BaseActivity)getActivity(), mAd, false);						
					}else{
						SharingCenter.share2Weibo((BaseActivity)getActivity(), mAd);
					}					
					break;
				case 2:
					SharingCenter.share2QZone((BaseActivity)getActivity(), mAd);
					break;
				case 3:
					SharingCenter.share2Weibo((BaseActivity)getActivity(), mAd);
					break;
				}				
			}
		}).setNegativeButton("取消", new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
			
		});
		return builder.create();
	}
	
}