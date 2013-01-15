package com.baixing.sharing;

import java.io.File;
import com.quanleimu.activity.R;
import com.weibo.net.AccessToken;
import com.weibo.net.Weibo;
import com.weibo.net.WeiboException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class WeiboSharingActivity extends Activity implements OnClickListener{
    private TextView mTextNum;
    private Button mSend;
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
    
	private void doShare2Weibo(){
		Log.d("doShar2Weibo", "doshare2weibo in sharingActivity" + "  accessToken: " + mAccessToken + "   expires_in:  " + mExpires_in);
		AccessToken accessToken = new AccessToken(mAccessToken, mExpires_in);
		try{ 
			Weibo.getInstance().share2weibo(this,
					accessToken.getToken(),
					accessToken.getSecret(), 
					"我在#百姓网#发布" + mContent,
					(mPicPath == null || mPicPath.length() == 0) ? "" : mPicPath);
		}
		catch(WeiboException e){
			e.printStackTrace();
		}
		mPd = ProgressDialog.show(this, "", "请稍候");
		mPd.setCancelable(true);
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.share_mblog_view);

        Intent in = this.getIntent();
        mPicPath = in.getStringExtra(EXTRA_PIC_URI);
        mContent = in.getStringExtra(EXTRA_WEIBO_CONTENT);
        mAccessToken = in.getStringExtra(EXTRA_ACCESS_TOKEN);
        mExpires_in = in.getStringExtra(EXTRA_EXPIRES_IN);
        Log.d("weibosharingActivity", "accessToken: " + mAccessToken + "   expires_in:  " + mExpires_in);

        Button close = (Button) this.findViewById(R.id.btnClose);
        close.setOnClickListener(this);
        mSend = (Button) this.findViewById(R.id.btnSend);
        mSend.setOnClickListener(this);
        LinearLayout total = (LinearLayout) this.findViewById(R.id.ll_text_limit_unit);
        total.setOnClickListener(this);
        mTextNum = (TextView) this.findViewById(R.id.tv_text_limit);
        ImageView picture = (ImageView) this.findViewById(R.id.ivDelPic);
        picture.setOnClickListener(this);

        mEdit = (EditText) this.findViewById(R.id.etEdit);
        mEdit.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String mText = mEdit.getText().toString();
                String mStr;
                int len = mText.length();
                if (len <= WEIBO_MAX_LENGTH) {
                    len = WEIBO_MAX_LENGTH - len;
                    mTextNum.setTextColor(R.color.text_num_gray);
                    if (!mSend.isEnabled())
                        mSend.setEnabled(true);
                } else {
                    len = len - WEIBO_MAX_LENGTH;

                    mTextNum.setTextColor(Color.RED);
                    if (mSend.isEnabled())
                        mSend.setEnabled(false);
                }
                mTextNum.setText(String.valueOf(len));
            }
        });
        mEdit.setText(mContent);
        mPiclayout = (FrameLayout) findViewById(R.id.flPic);
        if (TextUtils.isEmpty(this.mPicPath)) {
            mPiclayout.setVisibility(View.GONE);
        } else {
            mPiclayout.setVisibility(View.VISIBLE);
            File file = new File(mPicPath);
            if (file.exists()) {
                Bitmap pic = BitmapFactory.decodeFile(this.mPicPath);
                ImageView image = (ImageView) this.findViewById(R.id.ivImage);
                image.setImageBitmap(pic);
            } else {
                mPiclayout.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();

        if (viewId == R.id.btnClose) {
            finish();
        } else if (viewId == R.id.btnSend) {
        	Log.d("btnSend clicked", "btn clicked");
        	doShare2Weibo();
        } else if (viewId == R.id.ll_text_limit_unit) {
            Dialog dialog = new AlertDialog.Builder(this).setTitle("注意")
                    .setMessage("是否要删除这条微薄？")
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            mEdit.setText("");
                        }
                    }).setNegativeButton(R.string.cancel, null).create();
            dialog.show();
        } else if (viewId == R.id.ivDelPic) {
            Dialog dialog = new AlertDialog.Builder(this).setTitle("注意")
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
		this.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				Toast.makeText(getApplicationContext(), text, 0).show();
			}
		});    
		if(mPd != null){
			mPd.dismiss();
		}
    }

//	@Override
//	public void onComplete(String arg0) {
//		// TODO Auto-generated method stub
//		Log.d("weiboshare", "weiboshare oncomplete");
//		showToast("分享成功");
//		this.finish();
//	}
//
//	@Override
//	public void onError(WeiboException arg0) {
//		// TODO Auto-generated method stub
//		Log.d("weiboshare", "weiboshare onError: " + arg0.getMessage());
//		showToast("分享失败：" + arg0.getMessage());
//	}
//
//	@Override
//	public void onIOException(IOException arg0) {
//		// TODO Auto-generated method stub
//		Log.d("weiboshare", "weiboshare onIOException");
//		showToast("分享错误：" + arg0.getMessage());
//	}
}
