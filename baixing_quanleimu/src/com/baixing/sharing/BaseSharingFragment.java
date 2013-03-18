package com.baixing.sharing;

import java.io.File;

import com.baixing.activity.BaseFragment;
import com.quanleimu.activity.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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

public class BaseSharingFragment extends BaseFragment implements OnClickListener{
    private TextView mTextNum;
    protected EditText mEdit;
    private FrameLayout mPiclayout;

    protected String mPicPath = "";
    private String mContent = "";

    public static final String EXTRA_WEIBO_CONTENT = "com.sharing.android.content";
    public static final String EXTRA_PIC_URI = "com.sharing.android.pic.uri";
    public static final String EXTRA_ACCESS_TOKEN = "com.sharing.android.accesstoken";
    public static final String EXTRA_EXPIRES_IN = "com.sharing.android.expires";

    public static final int WEIBO_MAX_LENGTH = 140;
    
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
                            mPicPath = "";
                        }
                    }).setNegativeButton(R.string.cancel, null).create();
            dialog.show();
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

	@Override
	public void initTitle(TitleDef title){
		title.m_visible = true;
		title.m_leftActionHint = "返回";
		title.m_rightActionHint = "发布";
	}

}
