package com.baixing.view.fragment;

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
		String[] names = {"分享到新浪微薄", "分享到微信", "分享到QQ空间"};
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).setTitle("请选择")
		.setItems(names, new DialogInterface.OnClickListener(){
			
			@Override
			public void onClick(DialogInterface dialog, int which){
				switch(which){
				case 0:
					SharingCenter.share2Weibo(getActivity(), mAd);
					break;
				case 1:
					SharingCenter.share2Weixin(getActivity(), mAd);
					break;
				case 2:
					SharingCenter.share2QZone(getActivity(), mAd);
					break;
				}
				dialog.dismiss();
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