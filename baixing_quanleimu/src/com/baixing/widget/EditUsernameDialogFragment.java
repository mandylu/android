package com.baixing.widget;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.baixing.entity.UserProfile;
import com.baixing.tracking.Tracker;
import com.baixing.tracking.TrackConfig.TrackMobile.BxEvent;
import com.baixing.tracking.TrackConfig.TrackMobile.Key;
import com.baixing.util.Communication;
import com.baixing.util.ParameterHolder;
import com.baixing.util.Util;
import com.baixing.view.fragment.PersonalInfoFragment;
import com.quanleimu.activity.R;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created with IntelliJ IDEA.
 * User: zengming
 * Date: 12-10-30
 * Time: PM2:09
 * To change this template use File | Settings | File Templates.
 */
public class EditUsernameDialogFragment extends DialogFragment {

    public Handler handler;

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
            Toast.makeText(getActivity(), "请输入用户名", 1).show();
            return;
        }

        ParameterHolder params = new ParameterHolder();
        params.addParameter("nickname", editUsernameEt.getText().toString());
        params.addParameter("userId", userProfile.userId);
        Message msg = handler.obtainMessage();
        msg.what = PersonalInfoFragment.MSG_SHOW_PROGRESS;
        handler.sendMessage(msg);

        Communication.executeAsyncPostTask("user_profile_update", params, new Communication.CommandListener() {

            @Override
            public void onServerResponse(String serverMessage) {
                Message msg = handler.obtainMessage();
                try {
                    JSONObject obj = new JSONObject(serverMessage).getJSONObject("error");
                    if (!"0".equals(obj.getString("code"))) {
                        msg.what = PersonalInfoFragment.MSG_SHOW_TOAST;
                        msg.obj = obj.get("message");
                        Tracker.getInstance().event(BxEvent.EDITPROFILE_SAVE)
                                .append(Key.EDIT_PROFILE_STATUS, false)
                                .append(Key.EDIT_RPOFILE_FAIL_REASON, msg.obj.toString())
                                .end();
                    } else {
//                        Util.clearData(getActivity(), "userProfile");
                    	userProfile.nickName = editUsernameEt.getText().toString();
                    	Activity activity = getActivity();
                    	if (activity != null)
                    	{
                    		Util.saveDataToLocate(activity, "userProfile", userProfile);
                    	}
                    
                        msg.what = PersonalInfoFragment.MSG_EDIT_USERNAME_SUCCESS;
                        Tracker.getInstance().event(BxEvent.EDITPROFILE_SAVE)
                                .append(Key.EDIT_PROFILE_STATUS, true)
                                .end();
                    }
                    handler.sendMessage(msg);
                } catch (JSONException e) {
                    msg.what = PersonalInfoFragment.MSG_SHOW_TOAST;
                    msg.obj = "请求失败";
                    handler.sendMessage(msg);
                    Tracker.getInstance().event(BxEvent.EDITPROFILE_SAVE)
                            .append(Key.EDIT_PROFILE_STATUS, false)
                            .append(Key.EDIT_RPOFILE_FAIL_REASON, msg.obj.toString())
                            .end();
                }

            }

            @Override
            public void onException(Exception ex) {
                Message msg = handler.obtainMessage();
                msg.what = PersonalInfoFragment.MSG_SHOW_TOAST;
                msg.obj = "网络异常，请稍后再试";
                handler.sendMessage(msg);
                Tracker.getInstance().event(BxEvent.EDITPROFILE_SAVE)
                        .append(Key.EDIT_PROFILE_STATUS, false)
                        .append(Key.EDIT_RPOFILE_FAIL_REASON, msg.obj.toString())
                        .end();
            }
        });
    }



}
