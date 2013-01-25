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
	public SharingFragment(Ad ad){
		mAd = ad;
	}
	
	public SharingFragment(){
		
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceBundle){
		String[] names = {"转发到新浪微博", "转发到微信", "转发到QQ空间"};
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).setTitle("请选择")
		.setItems(names, new DialogInterface.OnClickListener(){
			
			@Override
			public void onClick(DialogInterface dialog, int which){
				dismiss();
				switch(which){
				case 0:
					SharingCenter.share2Weibo((BaseActivity)getActivity(), mAd);
					break;
				case 1:
					SharingCenter.share2Weixin((BaseActivity)getActivity(), mAd);
					break;
				case 2:
					SharingCenter.share2QZone((BaseActivity)getActivity(), mAd);
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