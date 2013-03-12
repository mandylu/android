package com.baixing.widget;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.baixing.entity.UserProfile;
import com.baixing.message.BxMessageCenter;
import com.baixing.network.api.ApiError;
import com.baixing.network.api.ApiParams;
import com.baixing.network.api.BaseApiCommand;
import com.baixing.network.api.BaseApiCommand.Callback;
import com.baixing.tracking.TrackConfig.TrackMobile.BxEvent;
import com.baixing.tracking.TrackConfig.TrackMobile.Key;
import com.baixing.tracking.Tracker;
import com.baixing.util.Util;
import com.baixing.util.ViewUtil;
import com.quanleimu.activity.R;

/**
 * Created with IntelliJ IDEA.
 * User: zengming
 * Date: 12-10-30
 * Time: PM2:09
 * To change this template use File | Settings | File Templates.
 */
public class EditUsernameDialogFragment extends DialogFragment {
	
	public static interface ICallback {
		public void onEditSucced(String newUserName);
	}

//    public Handler handler;
	
	public ICallback callback;

    private UserProfile userProfile;

//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.dialog_edit_username, container);
//        editUsernameEt = (EditText) view.findViewById(R.id.dialog_edit_username_et);
//        userProfile = (UserProfile) Util.loadDataFromLocate(getActivity(), "userProfile");
//        editUsernameEt.setText(userProfile.nickName);
//        getDialog().setTitle("修改用户名");
//        return view;
//    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        userProfile = (UserProfile) Util.loadDataFromLocate(getActivity(), "userProfile", UserProfile.class);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View v = inflater.inflate(R.layout.dialog_edit_username, null);
        EditText editUsernameEt = (EditText) v.findViewById(R.id.dialog_edit_username_et);
        editUsernameEt.setText(userProfile.nickName);
        editUsernameEt.setSelection(editUsernameEt.getText().length());
        builder.setView(v)
                .setTitle("修改用户名")
                .setPositiveButton("修改", new Dialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int which) {
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        Tracker.getInstance().event(BxEvent.EDITPROFILE_CANCEL).end();
                    }
                });
        final AlertDialog dialog = builder.create();
        // setOnShowListener 需要 2.2 之上版本，不过测试 2.1 也没有问题
        //楼上的：你测试的应该是2.1 update1，而不是2.1 不过现在2.1的设备几乎可以忽略 
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface di) {
                Button b = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        updateUsername();
                    }
                });
            }
        });
        return dialog;
    }


    private void updateUsername() {
        final EditText editUsernameEt = (EditText) getDialog().findViewById(R.id.dialog_edit_username_et);
        if (editUsernameEt.getText().toString().trim().length() <= 0) {
            ViewUtil.showToast(getActivity(), "请输入用户名", false);
            return;
        }

        final String newUserName = editUsernameEt.getText().toString();
        ApiParams params = new ApiParams();
        params.addParam("nickname", newUserName);
        params.addParam("userId", userProfile.userId);
//        Message msg = handler.obtainMessage();
//        msg.what = PersonalProfileFragment.MSG_SHOW_PROGRESS;
//        handler.sendMessage(msg);
        final ProgressDialog pr = ProgressDialog.show(getActivity(), getString(R.string.dialog_title_info), getString(R.string.dialog_message_waiting));
        pr.show();
        
        
        BaseApiCommand.createCommand("user_profile_update", false, params).execute(editUsernameEt.getContext(), new Callback() {
			
			@Override
			public void onNetworkFail(String apiName, ApiError error) {
//                Message msg = handler.obtainMessage();
//                msg.what = PersonalProfileFragment.MSG_SHOW_TOAST;
//                msg.obj = "网络异常，请稍后再试";
//                handler.sendMessage(msg);
				String errorMsg = "网络异常，请稍后再试";
				pr.dismiss();
				ViewUtil.showToast(getActivity(), errorMsg, false);
                Tracker.getInstance().event(BxEvent.EDITPROFILE_SAVE)
                        .append(Key.EDIT_PROFILE_STATUS, false)
                        .append(Key.EDIT_RPOFILE_FAIL_REASON, errorMsg)
                        .end();
            }
			
			@Override
			public void onNetworkDone(String apiName, String responseData) {
//                Message msg = handler.obtainMessage();
                try {
                    JSONObject obj = new JSONObject(responseData).getJSONObject("error");
                    if (!"0".equals(obj.getString("code"))) {
//                        msg.what = PersonalProfileFragment.MSG_SHOW_TOAST;
//                        msg.obj = obj.get("message");
                    	pr.dismiss();
                    	String errorMsg = (String) obj.get("message");
                    	ViewUtil.showToast(getActivity(), errorMsg, false);
                        Tracker.getInstance().event(BxEvent.EDITPROFILE_SAVE)
                                .append(Key.EDIT_PROFILE_STATUS, false)
                                .append(Key.EDIT_RPOFILE_FAIL_REASON, errorMsg)
                                .end();
                    } else {
//                        Util.clearData(getActivity(), "userProfile");
                    	userProfile.nickName = editUsernameEt.getText().toString();
                    	Activity activity = getActivity();
                    	if (activity != null)
                    	{
                    		Util.saveDataToLocate(activity, "userProfile", userProfile);
                    	}
                    
//                        msg.what = PersonalProfileFragment.MSG_EDIT_USERNAME_SUCCESS;
                    	pr.dismiss();
                    	callback.onEditSucced(newUserName);
                        Tracker.getInstance().event(BxEvent.EDITPROFILE_SAVE)
                                .append(Key.EDIT_PROFILE_STATUS, true)
                                .end();
                        EditUsernameDialogFragment.this.dismiss();
                    }
//                    handler.sendMessage(msg);
                } catch (JSONException e) {
                	pr.dismiss();
//                    msg.what = PersonalProfileFragment.MSG_SHOW_TOAST;
                    String errorMsg = "请求失败";
//                    handler.sendMessage(msg);
                    ViewUtil.showToast(getActivity(), errorMsg, false);
                    Tracker.getInstance().event(BxEvent.EDITPROFILE_SAVE)
                            .append(Key.EDIT_PROFILE_STATUS, false)
                            .append(Key.EDIT_RPOFILE_FAIL_REASON, errorMsg)
                            .end();
                }

            }
		});

    }



}
