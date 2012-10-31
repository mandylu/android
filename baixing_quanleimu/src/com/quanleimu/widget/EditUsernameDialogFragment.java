package com.quanleimu.widget;

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
import com.quanleimu.activity.QuanleimuMainActivity;
import com.quanleimu.activity.R;
import com.quanleimu.entity.UserProfile;
import com.quanleimu.util.Communication;
import com.quanleimu.util.ParameterHolder;
import com.quanleimu.util.Util;
import com.quanleimu.view.fragment.HomeFragment;
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
        userProfile = (UserProfile) Util.loadDataFromLocate(getActivity(), "userProfile");
        LayoutInflater inflater = getActivity().getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View v = inflater.inflate(R.layout.dialog_edit_username, null);
        EditText editUsernameEt = (EditText) v.findViewById(R.id.dialog_edit_username_et);
        editUsernameEt.setText(userProfile.nickName);
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
                        String x = userProfile.nickName;

                    }
                });
        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface di) {
                Button b = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        updateUsername();
                        String x = userProfile.nickName;
                    }
                });

            }
        });
        return dialog;
    }


    private void updateUsername() {
        EditText editUsernameEt = (EditText) getDialog().findViewById(R.id.dialog_edit_username_et);

        ParameterHolder params = new ParameterHolder();
        params.addParameter("nickname", editUsernameEt.getText().toString());
        params.addParameter("userId", userProfile.userId);
        Message msg = handler.obtainMessage();
        msg.what = HomeFragment.MSG_SHOW_PROGRESS;
        handler.sendMessage(msg);

        Communication.executeAsyncPostTask("user_profile_update", params, new Communication.CommandListener() {

            @Override
            public void onServerResponse(String serverMessage) {
                Message msg = handler.obtainMessage();
                try {
                    JSONObject obj = new JSONObject(serverMessage).getJSONObject("error");
                    if (!"0".equals(obj.getString("code"))) {
                        msg.what = HomeFragment.MSG_SHOW_TOAST;
                        msg.obj = obj.get("message");
                    } else {
                        Util.clearData(getActivity(), "userProfile");
                        msg.what = HomeFragment.MSG_EDIT_USERNAME_SUCCESS;
                    }
                    handler.sendMessage(msg);
                } catch (JSONException e) {
                    msg.what = HomeFragment.MSG_SHOW_TOAST;
                    msg.obj = "请求失败";
                    handler.sendMessage(msg);
                }

            }

            @Override
            public void onException(Exception ex) {
                Message msg = handler.obtainMessage();
                msg.what = HomeFragment.MSG_SHOW_TOAST;
                msg.obj = "网络异常，请稍后再试";
                handler.sendMessage(msg);
            }
        });
    }



}