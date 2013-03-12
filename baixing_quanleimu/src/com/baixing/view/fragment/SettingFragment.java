package com.baixing.view.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.text.method.DateTimeKeyListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.baixing.activity.BaseFragment;
import com.baixing.activity.MainActivity;
import com.baixing.data.GlobalDataManager;
import com.baixing.entity.UserBean;
import com.baixing.entity.UserProfile;
import com.baixing.message.BxMessageCenter;
import com.baixing.message.IBxNotificationNames;
import com.baixing.network.api.ApiParams;
import com.baixing.network.api.BaseApiCommand;
import com.baixing.tracking.Tracker;
import com.baixing.tracking.TrackConfig.TrackMobile.BxEvent;
import com.baixing.tracking.TrackConfig.TrackMobile.PV;
import com.baixing.util.*;
import com.baixing.widget.EditUsernameDialogFragment;
import com.baixing.widget.EditUsernameDialogFragment.ICallback;
import com.quanleimu.activity.R;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;

import java.util.Date;

public class SettingFragment extends BaseFragment implements View.OnClickListener, ICallback {
    private UserBean user;
    private UserProfile profile;
    private long debugShowFlagTime = 0;
    private long debugShowFlag = 0;
    
    public static final int MSG_PROFILE_UPDATE = 1;

    @Override
    public View onInitializeView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View setmain = inflater.inflate(R.layout.setmain, null);
        ((RelativeLayout) setmain.findViewById(R.id.setFlowOptimize)).setOnClickListener(this);
        ((RelativeLayout) setmain.findViewById(R.id.setBindID)).setOnClickListener(this);
        ((RelativeLayout) setmain.findViewById(R.id.setCheckUpdate)).setOnClickListener(this);
        ((RelativeLayout) setmain.findViewById(R.id.setAbout)).setOnClickListener(this);
        ((RelativeLayout) setmain.findViewById(R.id.setFeedback)).setOnClickListener(this);
        ((RelativeLayout) setmain.findViewById(R.id.bindSharingAccount)).setOnClickListener(this);
        ((RelativeLayout) setmain.findViewById(R.id.setChangeUserName)).setOnClickListener(this);
        ((Button) setmain.findViewById(R.id.debugBtn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long nowTime = System.currentTimeMillis();
                if (nowTime - debugShowFlagTime > 1000) {
                    debugShowFlagTime = nowTime;
                    debugShowFlag = 0;
                } else {
                    debugShowFlag++;
                    if (debugShowFlag>1) {
                        pushFragment(new DebugFragment(), null);
                    }
                }
            }
        });
        
        refreshUI(setmain);
        
        if (profile == null && GlobalDataManager.getInstance().getAccountManager().isUserLogin()) {
        	loadProfile();
		}


        return setmain;
    }
    
    private void loadProfile() {
    	Thread t = new Thread(new Runnable() {
    		public void run() {
    			UserProfile profile = (UserProfile) Util.loadDataFromLocate(getActivity(), "userProfile", UserProfile.class);
    			if (profile == null) {
    				ApiParams param = new ApiParams();
    				param.addParam("rt", 1);
    				param.addParam("userId", user.getId());
    				
    				String upString = BaseApiCommand.createCommand("user_profile", true, param).executeSync(getAppContext());
    				if (!TextUtils.isEmpty(upString)) {
    					profile = UserProfile.from(upString);
    					if (profile != null)
    					{
    						Util.saveDataToLocate(getActivity(), "userProfile", profile);
    						SettingFragment.this.profile = profile;
    						sendMessageDelay(MSG_PROFILE_UPDATE, null, 100);
    					}
    				}
    				
    			} else {
    				SettingFragment.this.profile = profile;
    				sendMessageDelay(MSG_PROFILE_UPDATE, null, 100);
    			}
    			
    		}
    	});
    	t.start();
    }

    private void refreshUI(View rootView) {
    	if(rootView == null) return;
        user = GlobalDataManager.getInstance().getAccountManager().getCurrentUser();

        TextView bindIdTextView = (TextView) rootView.findViewById(R.id.setBindIdtextView);
        if (user == null || user.getPhone() == null || user.getPhone().equals("")) {
            bindIdTextView.setText(R.string.label_login);
        } else {
            bindIdTextView.setText(R.string.label_logout);
        }

        TextView flowOptimizeTw = (TextView)rootView.findViewById(R.id.setFlowOptimizeTw);
        String res = getResources().getStringArray(R.array.item_flow_optimize)[GlobalDataManager.isTextMode() ? 1 : 0];;
        flowOptimizeTw.setText(res);
        
        if (profile != null) {
        	rootView.findViewById(R.id.setChangeUserName).setVisibility(View.VISIBLE);
        	TextView userNameTxt = (TextView) rootView.findViewById(R.id.userNameTxt);
        	userNameTxt.setText(profile.nickName);
        } else {
        	rootView.findViewById(R.id.setChangeUserName).setVisibility(View.GONE);
        }
        

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
                        profile = null;
                        refreshUI(getView());
                        ViewUtil.showToast(getActivity(), "已退出", false);
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
        	case R.id.setChangeUserName:
        		EditUsernameDialogFragment editUserDlg = new EditUsernameDialogFragment();
                editUserDlg.callback = this;
                editUserDlg.show(getFragmentManager(), null);
        	break;
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
                UmengUpdateAgent.update(GlobalDataManager.getInstance().getApplicationContext());
                UmengUpdateAgent.setUpdateAutoPopup(false);
                UmengUpdateAgent.setUpdateListener(new UmengUpdateListener() {
                    @Override
                    public void onUpdateReturned(int updateStatus,UpdateResponse updateInfo) {
                    	if(getActivity() == null) return;
                    	String msgToShow = null;
                        switch (updateStatus) {
                            case 0: // has update
                                UmengUpdateAgent.showUpdateDialog(getActivity(), updateInfo);
                                break;
                            case 1: // has no update
                            	msgToShow = "你所使用的就是最新版本";
                                break;
                            case 2: // none wifi
                               msgToShow =  "为了节省您的流量，请在wifi下更新";
                                break;
                            case 3: // time out
                                msgToShow = "网络超时，请检查网络";
                                break;
                        }
                        if (msgToShow != null) {
                        	ViewUtil.showToast(getActivity(), msgToShow, false);
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
            case R.id.bindSharingAccount:
            	pushFragment(new BindSharingFragment(), createArguments("绑定转发帐号", null));
            	break;
            default:
                ViewUtil.showToast(getActivity(), "no action", false);
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
        int checkedIdx = GlobalDataManager.isTextMode() ? 1 : 0;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.label_flow_optimize)
                .setSingleChoiceItems(R.array.item_flow_optimize, checkedIdx, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        GlobalDataManager.setTextMode(i == 1);
                        refreshUI(getView());
                        dialog.dismiss();
                        String tip =getResources().getStringArray(R.array.item_flow_optimize)[i];
                        ViewUtil.showToast(getActivity(), "已切换至" + tip, false);
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

	@Override
	public void onEditSucced(String newUserName) {
		if (profile != null) {
			profile.nickName = newUserName;
		}
		BxMessageCenter.defaultMessageCenter().postNotification(IBxNotificationNames.NOTIFICATION_PROFILE_UPDATE, profile);
		this.sendMessage(MSG_PROFILE_UPDATE, null);
	}

	@Override
	protected void handleMessage(Message msg, Activity activity, View rootView) {
		switch (msg.what) {
		case MSG_PROFILE_UPDATE:
			refreshUI(rootView);
			break;
			default :
				super.handleMessage(msg, activity, rootView);
		}
	}
	
	

}
