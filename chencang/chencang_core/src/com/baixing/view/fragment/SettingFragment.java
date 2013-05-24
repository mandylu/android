package com.baixing.view.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baixing.activity.BaseFragment;
import com.baixing.data.GlobalDataManager;
import com.baixing.entity.UserBean;
import com.baixing.entity.UserProfile;
import com.baixing.message.BxMessageCenter;
import com.baixing.message.IBxNotificationNames;
import com.baixing.network.api.ApiParams;
import com.baixing.network.api.BaseApiCommand;
import com.baixing.tracking.TrackConfig.TrackMobile.BxEvent;
import com.baixing.tracking.TrackConfig.TrackMobile.PV;
import com.baixing.tracking.Tracker;
import com.baixing.util.Util;
import com.baixing.util.ViewUtil;
import com.chencang.core.R;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;

public class SettingFragment extends BaseFragment implements View.OnClickListener {
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
//        ((RelativeLayout) setmain.findViewById(R.id.setBindID)).setOnClickListener(this);
//        ((RelativeLayout) setmain.findViewById(R.id.setCheckUpdate)).setOnClickListener(this);
//        ((RelativeLayout) setmain.findViewById(R.id.setAbout)).setOnClickListener(this);
        ((RelativeLayout) setmain.findViewById(R.id.setFeedback)).setOnClickListener(this);
//        ((RelativeLayout) setmain.findViewById(R.id.bindSharingAccount)).setOnClickListener(this);
//        ((RelativeLayout) setmain.findViewById(R.id.setChangeUserName)).setOnClickListener(this);
//        ((RelativeLayout) setmain.findViewById(R.id.resetPassword)).setOnClickListener(this);
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
//                        pushFragment(new DebugFragment(), null);
                    }
                }
            }
        });
        
        refreshUI(setmain);
        
        final boolean isLogin = GlobalDataManager.getInstance().getAccountManager().isUserLogin();
        if (profile == null && isLogin) {
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

//        TextView bindIdTextView = (TextView) rootView.findViewById(R.id.setBindIdtextView);
//        if (user == null || user.getPhone() == null || user.getPhone().equals("")) {
//            bindIdTextView.setText(R.string.label_login);
//        } else {
//            bindIdTextView.setText(R.string.label_logout);
//        }

        TextView flowOptimizeTw = (TextView)rootView.findViewById(R.id.setFlowOptimizeTw);
        String res = getResources().getStringArray(R.array.item_flow_optimize)[GlobalDataManager.isTextMode() ? 1 : 0];;
        flowOptimizeTw.setText(res);
        
//        if (profile != null) {
//        	rootView.findViewById(R.id.setChangeUserName).setVisibility(View.VISIBLE);
//        	TextView userNameTxt = (TextView) rootView.findViewById(R.id.userNameTxt);
//        	userNameTxt.setText(profile.nickName);
//        } else {
//        	rootView.findViewById(R.id.setChangeUserName).setVisibility(View.GONE);
//        }
        
//        final boolean isLogin = GlobalDataManager.getInstance().getAccountManager().isUserLogin();
//        ((RelativeLayout) rootView.findViewById(R.id.resetPassword)).setVisibility(isLogin ? View.VISIBLE : View.GONE);
//        ((RelativeLayout) rootView.findViewById(R.id.bindSharingAccount)).setVisibility(isLogin ? View.VISIBLE : View.GONE);
        
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
    	int id = v.getId();
        if(id == R.id.setFlowOptimize){
            showFlowOptimizeDialog();
            Tracker.getInstance().event(BxEvent.SETTINGS_PICMODE).end();
        }else if(id == R.id.setFeedback){
            pushFragment(new FeedbackFragment(), createArguments("反馈信息", null));
            Tracker.getInstance().event(BxEvent.SETTINGS_FEEDBACK).end();
        }
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
