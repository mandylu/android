package com.baixing.sharing;

import java.io.File;
import java.io.IOException;

import com.baixing.activity.BaseFragment;
import com.baixing.broadcast.CommonIntentAction;
import com.baixing.data.GlobalDataManager;
import com.baixing.sharing.weibo.Oauth2AccessToken;
import com.baixing.sharing.weibo.RequestListener;
import com.baixing.sharing.weibo.StatusesAPI;
import com.baixing.sharing.weibo.WeiboException;
import com.baixing.util.ViewUtil;
import com.quanleimu.activity.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class WeiboSharingFragment extends BaseSharingFragment implements OnClickListener{
    private String mAccessToken = "";
    private String mExpires_in = "";

    public static final int WEIBO_MAX_LENGTH = 140;
    
    private ProgressDialog mPd;
    
    class ShareListener implements RequestListener{

		@Override
		public void onComplete(String arg0) {
			if(mPd != null){
				mPd.dismiss();
			}
			// TODO Auto-generated method stub
			WeiboSharingFragment.this.getActivity().runOnUiThread(new Runnable(){
				@Override
				public void run(){
					finishFragment();
				}
			});
			Context ctx = GlobalDataManager.getInstance().getApplicationContext();
			if(ctx != null){
				Intent intent = new Intent(CommonIntentAction.ACTION_BROADCAST_SHARE_SUCCEED);
				ctx.sendBroadcast(intent);
			}

			ViewUtil.showToast(getActivity(), "分享成功");
			if(mPd != null){
				mPd.dismiss();
			}
			SharingCenter.trackShareResult("weibo", true, null);
		}

		@Override
		public void onError(WeiboException arg0) {
			// TODO Auto-generated method stub
			if(mPd != null){
				mPd.dismiss();
			}			
			ViewUtil.showToast(getActivity(), arg0.getMessage());
			if(mPd != null){
				mPd.dismiss();
			}
			SharingCenter.trackShareResult("weibo", false, "code:" + arg0.getStatusCode() + " msg:" + arg0.getMessage());
		}

		@Override
		public void onIOException(IOException arg0) {
			// TODO Auto-generated method stub
			if(mPd != null){
				mPd.dismiss();
			}
			ViewUtil.showToast(getActivity(), arg0.getMessage());
			if(mPd != null){
				mPd.dismiss();
			}
			SharingCenter.trackShareResult("weibo", false, " msg:" + arg0.getMessage());
		}
    	
    }
    
	private void doShare2Weibo(){
		Oauth2AccessToken accessToken = new Oauth2AccessToken(mAccessToken, mExpires_in);
		StatusesAPI statusApi = new StatusesAPI(accessToken);
		String content = mEdit != null ? mEdit.getText().toString() : "";
		if(mPicPath == null || mPicPath.length() == 0){
			statusApi.update(content, "", "", new ShareListener());
		}else{
			statusApi.upload(content, mPicPath, "", "", new ShareListener());
		}
		mPd = ProgressDialog.show(this.getActivity(), "", "请稍候");
		mPd.setCancelable(true);
	}

	@Override
	public void handleRightAction(){
		doShare2Weibo();
	}
    
	@Override
	protected View onInitializeView(LayoutInflater inflater,
			ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub

        Bundle bundle = this.getArguments();
        if(bundle != null){
	        mAccessToken = bundle.getString(EXTRA_ACCESS_TOKEN);
	        mExpires_in = bundle.getString(EXTRA_EXPIRES_IN);
        }
		return super.onInitializeView(inflater, container, savedInstanceState);
	}

	public void initTitle(TitleDef title){
		super.initTitle(title);
		title.m_title = "新浪微博";
	}

}
