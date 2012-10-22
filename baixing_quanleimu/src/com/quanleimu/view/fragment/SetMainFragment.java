package com.quanleimu.view.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.quanleimu.activity.BaseFragment;
import com.quanleimu.activity.QuanleimuApplication;
import com.quanleimu.activity.R;
import com.quanleimu.entity.UserBean;
import com.quanleimu.util.Util;

//import com.quanleimu.entity.AuthDialogListener;
//import com.quanleimu.entity.WeiboAccessTokenWrapper;
//import com.weibo.net.AccessToken;
//import com.weibo.net.Oauth2AccessTokenHeader;
//import com.weibo.net.Utility;
//import com.weibo.net.Weibo;
//import com.weibo.net.WeiboParameters;

public class SetMainFragment extends BaseFragment implements View.OnClickListener {


    // 定义控件
    public Dialog changePhoneDialog;
    private UserBean user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View setmain = inflater.inflate(R.layout.setmain, null);
        ((RelativeLayout) setmain.findViewById(R.id.setFlowOptimize)).setOnClickListener(this);
        ((RelativeLayout) setmain.findViewById(R.id.setBindID)).setOnClickListener(this);
        ((RelativeLayout) setmain.findViewById(R.id.setCheckUpdate)).setOnClickListener(this);
        ((RelativeLayout) setmain.findViewById(R.id.setAbout)).setOnClickListener(this);
        ((RelativeLayout) setmain.findViewById(R.id.setFeedback)).setOnClickListener(this);

//		WeiboAccessTokenWrapper tokenWrapper = (WeiboAccessTokenWrapper)Helper.loadDataFromLocate(this.getActivity(), "weiboToken");
//		AccessToken token = null;
//		if(tokenWrapper != null && tokenWrapper.getToken() != null){
//			token = new AccessToken(tokenWrapper.getToken(), QuanleimuApplication.kWBBaixingAppSecret);
//			token.setExpiresIn(tokenWrapper.getExpires());
//		}
//		String nick = (String)Helper.loadDataFromLocate(this.getActivity(), "weiboNickName");
//		if(token != null && nick != null){
//			((TextView)setmain.findViewById(R.id.tvWeiboNick)).setText(nick);
//			if(QuanleimuApplication.getWeiboAccessToken() == null){
//				QuanleimuApplication.setWeiboAccessToken(token);
//			}
//		}

//		final TextView textImg = (TextView)setmain.findViewById(R.id.textView3);
//		if(QuanleimuApplication.isTextMode()){
//			textImg.setText("文字");
//		}
//		else{
//			textImg.setText("图片");
//		}
//		((RelativeLayout)setmain.findViewById(R.id.rlTextImage)).setOnClickListener(new View.OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				if(textImg.getText().equals("图片")){
//					textImg.setText("文字");
//					QuanleimuApplication.setTextMode(true);
//				}
//				else{
//					textImg.setText("图片");
//					QuanleimuApplication.setTextMode(false);
//				}
//			}
//		});

//		((TextView)setmain.findViewById(R.id.personMark)).setText(QuanleimuApplication.getApplication().getPersonMark());

        refreshUI(setmain);

        return setmain;
    }

    private void refreshUI(View rootView) {

        user = Util.getCurrentUser();

        TextView bindIdTextView = (TextView) rootView.findViewById(R.id.setBindIdtextView);
        if (user == null) {
            bindIdTextView.setText(R.string.label_login);
        } else {
            bindIdTextView.setText(R.string.label_logout);
        }


    }


    public void onResume() {
        super.onResume();
//		((TextView)getView().findViewById(R.id.personMark)).setText(QuanleimuApplication.getApplication().getPersonMark());
        this.refreshUI(getView());
    }

    @Override
    public void initTitle(TitleDef title) {
        title.m_visible = true;
        title.m_title = "设置";
        title.m_leftActionHint = "完成";
        title.m_leftActionStyle = EBUTT_STYLE.EBUTT_STYLE_NORMAL;
    }

    @Override
    public void initTab(TabDef tab) {
        tab.m_visible = false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.setFlowOptimize:
                showFlowOptimizeDialog();
                break;
            case R.id.setBindID:
                if (user == null) {
                    Bundle bundle = createArguments(null, "用户中心");
                    pushFragment(new LoginFragment(), bundle);
                } else {
                    //TODO jiawu 加入确认退出过程
                    Util.logout();
                    Toast.makeText(getAppContext(), "已退出", 1).show();
                    refreshUI(getView());
                }

                break;
            case R.id.setCheckUpdate:
                Toast.makeText(getAppContext(), "todo 检查更新", 1).show();
                break;
            case R.id.setAbout:
                pushFragment(new AboutUsFragment(), null);
                break;
            case R.id.setFeedback:
                pushFragment(new FeedbackFragment(), createArguments(null, null));
                break;
            default:
                Toast.makeText(getAppContext(), "no action", 1).show();
                break;
        }
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
                                    /*
        final View root = getView();
		// 签名档
		if (v.getId() == ((RelativeLayout) root.findViewById(R.id.rlMark)).getId()) {
			pushFragment(new MarkLableFragment(), null );
		}

		// 清空缓存
		else if (v.getId() == ((RelativeLayout) root.findViewById(R.id.rlClearCache)).getId()) {

			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(R.string.dialog_title_info)
					.setMessage(R.string.dialog_message_confirm_clear_cache)
					.setNegativeButton(R.string.no, null)
					.setPositiveButton(R.string.yes,
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
//									((TextView)root.findViewById(R.id.personMark)).setText("");
								}
							});
			builder.create().show();
		}
		
 */
    }

    private void showFlowOptimizeDialog() {
        int checkedIdx = QuanleimuApplication.isTextMode() ? 1 : 0;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.label_flow_optimize)
                .setSingleChoiceItems(R.array.item_flow_optimize, checkedIdx, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        QuanleimuApplication.setTextMode(i == 1);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                }).create().show();
    }

}
