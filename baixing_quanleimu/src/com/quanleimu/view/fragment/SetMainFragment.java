package com.quanleimu.view.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.quanleimu.activity.BaseFragment;
import com.quanleimu.activity.BaseFragment.TabDef;
import com.quanleimu.activity.BaseFragment.TitleDef;
import com.quanleimu.activity.QuanleimuApplication;
import com.quanleimu.activity.R;
import com.quanleimu.entity.AuthDialogListener;
import com.quanleimu.entity.UserBean;
import com.quanleimu.entity.WeiboAccessTokenWrapper;
import com.quanleimu.util.Helper;
import com.quanleimu.util.Util;
import com.weibo.net.AccessToken;
import com.weibo.net.Oauth2AccessTokenHeader;
import com.weibo.net.Utility;
import com.weibo.net.Weibo;
import com.weibo.net.WeiboParameters;

public class SetMainFragment extends BaseFragment implements View.OnClickListener {


	// 定义控件
	public Dialog changePhoneDialog;
	private UserBean user;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View setmain = inflater.inflate(R.layout.setmain, null);
		
//		((RelativeLayout) setmain.findViewById(R.id.rlWeibo)).setOnClickListener(this);
		((RelativeLayout) setmain.findViewById(R.id.rlClearCache)).setOnClickListener(this);
		( (RelativeLayout) setmain.findViewById(R.id.rlAbout)).setOnClickListener(this);
		((RelativeLayout) setmain.findViewById(R.id.rlMark)).setOnClickListener(this);
		((RelativeLayout) setmain.findViewById(R.id.rlTextImage)).setOnClickListener(this);
		((RelativeLayout) setmain.findViewById(R.id.rlBack)).setOnClickListener(this);
		
		WeiboAccessTokenWrapper tokenWrapper = (WeiboAccessTokenWrapper)Helper.loadDataFromLocate(this.getActivity(), "weiboToken");
		AccessToken token = null;
		if(tokenWrapper != null && tokenWrapper.getToken() != null){
			token = new AccessToken(tokenWrapper.getToken(), QuanleimuApplication.kWBBaixingAppSecret);
			token.setExpiresIn(tokenWrapper.getExpires());
		}
//		String nick = (String)Helper.loadDataFromLocate(this.getActivity(), "weiboNickName");
//		if(token != null && nick != null){
//			((TextView)setmain.findViewById(R.id.tvWeiboNick)).setText(nick);
//			if(QuanleimuApplication.getWeiboAccessToken() == null){
//				QuanleimuApplication.setWeiboAccessToken(token);
//			}
//		}
		
		final TextView textImg = (TextView)setmain.findViewById(R.id.textView3);
		if(QuanleimuApplication.isTextMode()){
			textImg.setText("文字");
		}
		else{
			textImg.setText("图片");
		}
		((RelativeLayout)setmain.findViewById(R.id.rlTextImage)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(textImg.getText().equals("图片")){
					textImg.setText("文字");
					QuanleimuApplication.setTextMode(true);
				}
				else{
					textImg.setText("图片");
					QuanleimuApplication.setTextMode(false);
				}				
			}
		});
		
		((TextView)setmain.findViewById(R.id.personMark)).setText(QuanleimuApplication.getApplication().getPersonMark());
		
		user = (UserBean) Util.loadDataFromLocate(getActivity(), "user");
		
		return setmain;
	}
	
	public void onResume(){
		super.onResume();
		((TextView)getView().findViewById(R.id.personMark)).setText(QuanleimuApplication.getApplication().getPersonMark());
		
		user = (UserBean) Util.loadDataFromLocate(getActivity(), "user");
	}
	
	@Override
	public void initTitle(TitleDef title){
		title.m_visible = true;
		title.m_title = "设置";
		title.m_leftActionHint = "完成";
		title.m_leftActionStyle = EBUTT_STYLE.EBUTT_STYLE_NORMAL;
		}
	
	@Override	
	public void initTab(TabDef tab){
		tab.m_visible = false;		
	}
	
	@Override
	public void onClick(View v) {
		// 手机号码
//		if(v.getId() == R.id.rlWeibo){
//			if(QuanleimuApplication.getWeiboAccessToken() != null){
//				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//				builder.setTitle("提示:")
//						.setMessage("是否解除绑定？")
//						.setNegativeButton("否", null)
//						.setPositiveButton("是",
//								new DialogInterface.OnClickListener() {
//	
//									@Override
//									public void onClick(DialogInterface dialog,
//											int which) {
//										
//										Helper.saveDataToLocate(getActivity(), "weiboToken", null);
//										Helper.saveDataToLocate(getActivity(), "weiboNickName", null);
//										QuanleimuApplication.setWeiboAccessToken(null);
//										Weibo.getInstance().setAccessToken(null);
//										Weibo.getInstance().setRequestToken(null);
//										Weibo.getInstance().setupConsumerConfig("", "");
//										((TextView)findViewById(R.id.tvWeiboNick)).setText("");
//									}
//								});
//				builder.create().show();	
//			}
//			else{
//				Weibo weibo = Weibo.getInstance();
//				weibo.setupConsumerConfig(QuanleimuApplication.kWBBaixingAppKey, QuanleimuApplication.kWBBaixingAppSecret);
//				weibo.setRedirectUrl("http://www.baixing.com");
////				weibo.authorize((BaseActivity)this.getContext(), new AuthDialogListener());
//                WeiboParameters parameters=new WeiboParameters();
//                parameters.add("forcelogin", "true");
//                Utility.setAuthorization(new Oauth2AccessTokenHeader());
//                AuthDialogListener lsn = new AuthDialogListener(getActivity(), new AuthDialogListener.AuthListener(){
//                	@Override
//                	public void onComplete(){
//                		String nick = (String)Helper.loadDataFromLocate(getActivity(), "weiboNickName");
//                		((TextView)findViewById(R.id.tvWeiboNick)).setText(nick);
//                	}
//                }); 
//                weibo.dialog(getActivity(), 
//                		parameters, lsn);
//                lsn.setInAuthrize(true);
//			}
//		}

		final View root = getView();
		// 签名档
		if (v.getId() == ((RelativeLayout) root.findViewById(R.id.rlMark)).getId()) {
			pushFragment(new MarkLableFragment(), null );
		}

		// 清空缓存
		else if (v.getId() == ((RelativeLayout) root.findViewById(R.id.rlClearCache)).getId()) {

			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle("提示:")
					.setMessage("是否清空缓存？")
					.setNegativeButton("否", null)
					.setPositiveButton("是",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									String[] files = getActivity().fileList();
									for(int i=0;i<files.length;i++){
										String file_path = files[i];
										getActivity().deleteFile(file_path);
									}
									
									QuanleimuApplication.getApplication().ClearCache();
									
									//清空签名档
									((TextView)root.findViewById(R.id.personMark)).setText("");
								}
							});
			builder.create().show();
		}
		
		//aboutus
		else if(v.getId() == ((RelativeLayout) root.findViewById(R.id.rlAbout)).getId()){
			pushFragment(new AboutUsFragment(), null);
		}
		
		// 反馈
		else if (v.getId() ==((RelativeLayout) root.findViewById(R.id.rlBack)).getId()) {
			pushFragment(new FeedbackFragment(), createArguments(null, null));
		}
	}

}
