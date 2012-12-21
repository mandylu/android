package com.baixing.view.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.baixing.entity.UserBean;
import com.baixing.util.*;
import com.baixing.util.TrackConfig.TrackMobile.BxEvent;
import com.baixing.util.TrackConfig.TrackMobile.PV;
import com.quanleimu.activity.BaseFragment;
import com.quanleimu.activity.QuanleimuApplication;
import com.quanleimu.activity.R;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;

public class SettingFragment extends BaseFragment implements View.OnClickListener {
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
    	if(rootView == null) return;
        user = Util.getCurrentUser();

        TextView bindIdTextView = (TextView) rootView.findViewById(R.id.setBindIdtextView);
        if (user == null || user.getPhone() == null || user.getPhone().equals("")) {
            bindIdTextView.setText(R.string.label_login);
        } else {
            bindIdTextView.setText(R.string.label_logout);
        }

        TextView flowOptimizeTw = (TextView)rootView.findViewById(R.id.setFlowOptimizeTw);
        String res = getResources().getStringArray(R.array.item_flow_optimize)[QuanleimuApplication.isTextMode() ? 1 : 0];;
        flowOptimizeTw.setText(res);

    }


    public void onResume() {
        super.onResume();
        this.pv = PV.SETTINGS;
		Tracker.getInstance().pv(PV.SETTINGS).end();
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
    public int[] excludedOptionMenus() {
    	return new int[]{OPTION_SETTING};
    }

    private void logoutAction() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_confirm_logout)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Util.logout();
                        refreshUI(getView());
                        Toast.makeText(getAppContext(), "已退出", 1).show();
                        Tracker.getInstance().event(BxEvent.SETTINGS_LOGOUT_CONFIRM).end();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        Tracker.getInstance().event(BxEvent.SETTINGS_LOGOUT_CANCEL).end();
                    }
                }).create().show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.setFlowOptimize:
                showFlowOptimizeDialog();
                Tracker.getInstance().event(BxEvent.SETTINGS_PICMODE).end();
                break;
            case R.id.setBindID:
                if (user == null || user.getPhone() == null || user.getPhone().equals("")) {
                    Bundle bundle = createArguments(null, "用户中心");
                    pushFragment(new LoginFragment(), bundle);
                    Tracker.getInstance().event(BxEvent.SETTINGS_LOGIN).end();
                } else {
                    Tracker.getInstance().event(BxEvent.SETTINGS_LOGOUT).end();
                    logoutAction();
                }

                break;
            case R.id.setCheckUpdate:
//                Intent updateIntent =new Intent(getAppContext(), BXUpdateService.class);
//                updateIntent.putExtra("titleId",R.string.app_name);
//                updateIntent.putExtra("apkUrl", "3");
//                getAppContext().startService(updateIntent);
//                UpdateHelper.getInstance().checkNewVersion(getActivity());
                UmengUpdateAgent.update(QuanleimuApplication.getApplication().getApplicationContext());
                UmengUpdateAgent.setUpdateAutoPopup(false);
                UmengUpdateAgent.setUpdateListener(new UmengUpdateListener() {
                    @Override
                    public void onUpdateReturned(int updateStatus,UpdateResponse updateInfo) {
                    	if(getActivity() == null) return;
                        switch (updateStatus) {
                            case 0: // has update
                                UmengUpdateAgent.showUpdateDialog(getActivity(), updateInfo);
                                break;
                            case 1: // has no update
                                Toast.makeText(getActivity(), "你所使用的就是最新版本", Toast.LENGTH_SHORT)
                                        .show();
                                break;
                            case 2: // none wifi
                                Toast.makeText(getActivity(), "为了节省您的流量，请在wifi下更新", Toast.LENGTH_SHORT)
                                        .show();
                                break;
                            case 3: // time out
                                Toast.makeText(getActivity(), "网络超时，请检查网络", Toast.LENGTH_SHORT)
                                        .show();
                                break;
                        }
                    }
                });
                Tracker.getInstance().event(BxEvent.SETTINGS_CHECKUPDATE).end();
                break;
            case R.id.setAbout:
                pushFragment(new AboutUsFragment(), null);
                Tracker.getInstance().event(BxEvent.SETTINGS_ABOUT).end();
                break;
            case R.id.setFeedback:
                pushFragment(new FeedbackFragment(), createArguments("反馈信息", null));
                Tracker.getInstance().event(BxEvent.SETTINGS_FEEDBACK).end();
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

    /**
     * 省流量设置
     */
    private void showFlowOptimizeDialog() {
        int checkedIdx = QuanleimuApplication.isTextMode() ? 1 : 0;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.label_flow_optimize)
                .setSingleChoiceItems(R.array.item_flow_optimize, checkedIdx, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        QuanleimuApplication.setTextMode(i == 1);
                        refreshUI(getView());
                        dialog.dismiss();
                        String tip =getResources().getStringArray(R.array.item_flow_optimize)[i];
                        Toast.makeText(getActivity(), "已切换至" + tip, 1).show();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                }).create().show();
    }

    public boolean hasGlobalTab()
	{
		return false;
	}

}
