package com.baixing.view.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.baixing.activity.BaseFragment;
import com.baixing.android.api.ApiClient;
import com.baixing.android.api.ApiError;
import com.baixing.android.api.ApiParams;
import com.baixing.android.api.cmd.BaseCommand;
import com.baixing.android.api.cmd.HttpGetCommand;
import com.baixing.broadcast.XMPPManager;
import com.baixing.data.GlobalDataManager;
import com.baixing.util.Communication;
import com.baixing.util.Util;
import com.quanleimu.activity.R;

/**
 * Created with IntelliJ IDEA.
 * User: zengming
 * Date: 13-1-30
 * Time: AM10:02
 */
public class DebugFragment extends BaseFragment implements View.OnClickListener, BaseCommand.Callback {
    private Button hostBtn;
    private final int MSG_pushTestSuccess = 101;
    private final int MSG_pushTestFail = 102;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initTitle(TitleDef title) {
        title.m_visible = true;
        title.m_title = "Debug";
    }

    @Override
    protected View onInitializeView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout debugLayout = (RelativeLayout)inflater.inflate(R.layout.debug_layout, null);

        hostBtn = (Button)debugLayout.findViewById(R.id.hostBtn);
        hostBtn.setOnClickListener(this);
        hostBtn.setText(ApiClient.host);

        debugLayout.findViewById(R.id.pushTestBtn).setOnClickListener(this);

        TextView xmppTv = (TextView)debugLayout.findViewById(R.id.xmppConTv);
        if (XMPPManager.getInstance(this.getAppContext()).isConnected()) {
            xmppTv.setText("XMPP已连通");
            xmppTv.setTextColor(Color.GREEN);
        } else {
            xmppTv.setText("XMPP无法连接");
            xmppTv.setTextColor(Color.RED);
        }

        ToggleButton showPushBtn = (ToggleButton)debugLayout.findViewById(R.id.showPushBtn);
        Boolean showDebugPush = (Boolean) Util.loadDataFromLocate(getAppContext(), "showDebugPush", Boolean.class);
        if (showDebugPush != null) {
            showPushBtn.setChecked(showDebugPush);
        }

        showPushBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Util.saveDataToLocate(getAppContext(), "showDebugPush", new Boolean(isChecked));
            }
        });

        TextView infoTv = (TextView)debugLayout.findViewById(R.id.infoTv);
        infoTv.setText("udid:" + Util.getDeviceUdid(getAppContext()));

        return debugLayout;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.hostBtn:
                this.hostBtnClicked();
                break;
            case R.id.pushTestBtn:
                this.pushTestBtnClicked();
                break;
            default:
                break;
        }
    }

    private void hostBtnClicked() {
        final CharSequence[] hosts = new CharSequence[]{
            "www.baixing.com",
            "www.xumengyi.baixing.com",
            "www.liuchong.baixing.com",
            "www.zengming.baixing.com",
            "www.zhongjiawu.com"
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("选择 api")
            .setItems(hosts, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    ApiClient.host = hosts[which].toString();
                    hostBtn.setText(ApiClient.host);
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

    private void pushTestBtnClicked() {
        Toast.makeText(getActivity(), "sssssss", 3);
        ApiParams params = new ApiParams();
        params.addParam("type", "push");
        HttpGetCommand.createCommand(1, "debug", params).execute(this);
    }


    @Override
    public void onNetworkDone(int requstCode, String responseData) {
        sendMessage(MSG_pushTestSuccess, responseData);
    }

    @Override
    public void onNetworkFail(int requstCode, ApiError error) {
        sendMessage(MSG_pushTestFail, error);
    }

    @Override
    protected void handleMessage(Message msg, Activity activity, View rootView) {
        switch (msg.what) {
            case MSG_pushTestSuccess:

                break;
            case MSG_pushTestFail:
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("提示")
                    .setMessage(msg.obj.toString())
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        }
                        }).create().show();
                break;
            default:
                break;
        }
    }
}