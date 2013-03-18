package com.baixing.sharing;

import org.json.JSONException;
import org.json.JSONObject;

import com.baixing.broadcast.CommonIntentAction;
import com.baixing.data.GlobalDataManager;
import com.baixing.util.ViewUtil;
import com.tencent.tauth.Constants;
import com.tencent.tauth.Tencent;

import android.R;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class QZoneSharingFragment extends BaseSharingFragment implements OnClickListener{
    private Tencent mTencent;

    public static final String EXTRA_OPEN_ID = "com.qzone.android.openid";
    public static final String EXTRA_LINK = "com.qzone.android.link";
    public static final String EXTRA_TITLE = "com.qzone.android.title";
    public static final String EXTRA_SUMMARY = "com.qzone.android.summary";
    
    private ProgressDialog mPd;
    
    private void doShare2QZone(){
		Bundle bundle = new Bundle();

		if(getArguments() != null){
			bundle.putString("title", getArguments().getString(EXTRA_TITLE));		
			bundle.putString("url", getArguments().getString(EXTRA_LINK));		
			bundle.putString("comment", mEdit != null ? mEdit.getText().toString() : "");
			bundle.putString("summary", getArguments().getString(EXTRA_SUMMARY));
			bundle.putString("type", "4");
			if(mTencent == null){
				mTencent = Tencent.createInstance(QZoneSharingManager.mAppid, getActivity().getApplicationContext());
				mTencent.setAccessToken(getArguments().getString(EXTRA_ACCESS_TOKEN), getArguments().getString(EXTRA_EXPIRES_IN));
				mTencent.setOpenId(getArguments().getString(EXTRA_OPEN_ID));
			}
			if(mTencent != null && mTencent.isSessionValid() && mTencent.getOpenId() != null){
				mTencent.setOpenId(mTencent.getOpenId() + mTencent.getOpenId());
		        JSONObject result = null;
		        try{
		        	result = mTencent.request(Constants.GRAPH_ADD_SHARE, bundle, Constants.HTTP_POST);
		        }catch(Exception e){
//		        	ViewUtil.showToast(getActivity(), e.getMessage(), false);
		        }
		        final boolean succeed = result != null;
	        	getActivity().runOnUiThread(new Runnable(){
	        		@Override
	        		public void run(){
	    		        if(!succeed){
	    		        	ViewUtil.showToast(getActivity(), "分享失败", false);
	    		        }
	    		        if(mPd != null){
	    		        	mPd.dismiss();
	    		        }
	        		}
	        	});
	        	if(!succeed){
	        		return;
	        	}
		        try {
					final int code = result.getInt("ret");
					String msg = code == 0 ? "分享成功" : "分享失败";
					final String msgShow = msg;
					getActivity().runOnUiThread(new Runnable(){
						@Override
						public void run(){
							if(code == 0){
								finishFragment();
							}
							ViewUtil.showToast(getActivity(), msgShow, false);
						}
					});
					if(0 == code){
						Context ctx = GlobalDataManager.getInstance().getApplicationContext();
						if(ctx != null){
							ctx.sendBroadcast(new Intent(CommonIntentAction.ACTION_BROADCAST_SHARE_SUCCEED));
						}
						SharingCenter.trackShareResult("qzone", true, null);
					}else{						
						SharingCenter.trackShareResult("qzone", false, "code:" + code + " msg:" + result.getString("msg"));
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}   	
    }

	@Override
	public void handleRightAction(){
		if(mEdit == null || mEdit.getText() == null || mEdit.getText().length() == 0){
			ViewUtil.showToast(getActivity(), "内容不能为空", false);
			return;
		}
		((new Thread(new Runnable(){
			@Override
			public void run(){
				doShare2QZone();
			}
		}))).start();
		mPd = ProgressDialog.show(getActivity(), "", "请稍候");
		mPd.setCancelable(true);
		mPd.setCanceledOnTouchOutside(true);
	}
    
	public void initTitle(TitleDef title){
		super.initTitle(title);
		title.m_title = "QQ空间";
	}

}
