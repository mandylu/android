package com.baixing.sharing;

import java.io.File;
import java.io.IOException;

import com.baixing.activity.BaseFragment;
import com.baixing.broadcast.CommonIntentAction;
import com.baixing.data.GlobalDataManager;
import com.quanleimu.activity.R;
import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.api.StatusesAPI;
import com.weibo.sdk.android.net.RequestListener;
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

public class WeiboSharingFragment extends BaseFragment implements OnClickListener{
    private TextView mTextNum;
//    private Button mSend;
    private EditText mEdit;
    private FrameLayout mPiclayout;

    private String mPicPath = "";
    private String mContent = "";
    private String mAccessToken = "";
    private String mExpires_in = "";


    public static final String EXTRA_WEIBO_CONTENT = "com.weibo.android.content";
    public static final String EXTRA_PIC_URI = "com.weibo.android.pic.uri";
    public static final String EXTRA_ACCESS_TOKEN = "com.weibo.android.accesstoken";
    public static final String EXTRA_EXPIRES_IN = "com.weibo.android.expires";

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

			showToast("分享成功");
			SharingCenter.trackShareResult("weibo", true, null);
		}

		@Override
		public void onError(WeiboException arg0) {
			// TODO Auto-generated method stub
			if(mPd != null){
				mPd.dismiss();
			}			
			showToast(arg0.getMessage());
			SharingCenter.trackShareResult("weibo", false, "code:" + arg0.getStatusCode() + " msg:" + arg0.getMessage());
		}

		@Override
		public void onIOException(IOException arg0) {
			// TODO Auto-generated method stub
			if(mPd != null){
				mPd.dismiss();
			}
			showToast(arg0.getMessage());
			SharingCenter.trackShareResult("weibo", false, " msg:" + arg0.getMessage());
		}
    	
    }
    
	private void doShare2Weibo(){
		Oauth2AccessToken accessToken = new Oauth2AccessToken(mAccessToken, mExpires_in);
		StatusesAPI statusApi = new StatusesAPI(accessToken);
		if(mPicPath == null || mPicPath.length() == 0){
			statusApi.update(mContent, "", "", new ShareListener());
		}else{
			statusApi.upload(mContent, mPicPath, "", "", new ShareListener());
		}
		mPd = ProgressDialog.show(this.getActivity(), "", "请稍候");
		mPd.setCancelable(true);
	}

	@Override
	public void handleRightAction(){
		doShare2Weibo();
	}

    @Override
    public void onClick(View v) {
        int viewId = v.getId();

        if (viewId == R.id.ll_text_limit_unit) {
            Dialog dialog = new AlertDialog.Builder(this.getActivity()).setTitle("注意")
                    .setMessage("是否要删除这条微博？")
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            mEdit.setText("");
                        }
                    }).setNegativeButton(R.string.cancel, null).create();
            dialog.show();
        } else if (viewId == R.id.ivDelPic) {
            Dialog dialog = new AlertDialog.Builder(this.getActivity()).setTitle("注意")
                    .setMessage("是否删除图片？")
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            mPiclayout.setVisibility(View.GONE);
                        }
                    }).setNegativeButton(R.string.cancel, null).create();
            dialog.show();
        }
    }
    
    private void showToast(final String text){
		this.getActivity().runOnUiThread(new Runnable(){
			@Override
			public void run(){
				Toast.makeText(getActivity(), text, 0).show();
			}
		});    
		if(mPd != null){
			mPd.dismiss();
		}
    }

	@Override
	protected View onInitializeView(LayoutInflater inflater,
			ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
        View layout = inflater.inflate(R.layout.share_mblog_view, null);

        Bundle bundle = this.getArguments();
        if(bundle != null){
	        mPicPath = bundle.getString(EXTRA_PIC_URI);
	        mContent = bundle.getString(EXTRA_WEIBO_CONTENT);
	        mAccessToken = bundle.getString(EXTRA_ACCESS_TOKEN);
	        mExpires_in = bundle.getString(EXTRA_EXPIRES_IN);
        }

        LinearLayout total = (LinearLayout) layout.findViewById(R.id.ll_text_limit_unit);
        total.setOnClickListener(this);
        mTextNum = (TextView) layout.findViewById(R.id.tv_text_limit);
        ImageView picture = (ImageView) layout.findViewById(R.id.ivDelPic);
        picture.setOnClickListener(this);

        mEdit = (EditText) layout.findViewById(R.id.etEdit);
        mEdit.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String mText = mEdit.getText().toString();
                int len = mText.length();
                View right = getView() != null && getView().getRootView() != null ? 
                		getView().getRootView().findViewById(R.id.right_action) : null;
                if (len <= WEIBO_MAX_LENGTH) {
                    len = WEIBO_MAX_LENGTH - len;
//                    mTextNum.setTextColor(R.color.text_num_gray);                   
                    if(right != null){
                        right.setEnabled(true);
                    }
                } else {
                    len = len - WEIBO_MAX_LENGTH;

                    mTextNum.setTextColor(Color.RED);
                    if(right != null){
                        right.setEnabled(false);
                    }
                }
                mTextNum.setText(String.valueOf(len));
            }
        });
        mEdit.setText(mContent);
        mPiclayout = (FrameLayout) layout.findViewById(R.id.flPic);
        if (TextUtils.isEmpty(this.mPicPath)) {
            mPiclayout.setVisibility(View.GONE);
        } else {
            mPiclayout.setVisibility(View.VISIBLE);
            File file = new File(mPicPath);
            if (file.exists()) {
                Bitmap pic = BitmapFactory.decodeFile(this.mPicPath);
                ImageView image = (ImageView) layout.findViewById(R.id.ivImage);
                image.setImageBitmap(pic);
            } else {
                mPiclayout.setVisibility(View.GONE);
            }
        }		
		return layout;
	}

	public void initTitle(TitleDef title){
		title.m_visible = true;
		title.m_leftActionHint = "返回";
		title.m_title = "新浪微博";
		title.m_rightActionHint = "发布";
	}

}
