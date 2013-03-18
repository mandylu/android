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
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.baixing.activity.BaseFragment;
import com.baixing.broadcast.XMPPManager;
import com.baixing.network.api.ApiConfiguration;
import com.baixing.network.api.ApiError;
import com.baixing.network.api.ApiParams;
import com.baixing.network.api.BaseApiCommand;
import com.baixing.network.api.BaseApiCommand.Callback;
import com.baixing.util.Util;
import com.baixing.util.ViewUtil;
import com.quanleimu.activity.R;

/**
 * Created with IntelliJ IDEA.
 * User: zengming
 * Date: 13-1-30
 * Time: AM10:02
 */
public class DebugFragment extends BaseFragment implements View.OnClickListener, Callback {
    private Button hostBtn;
    private EditText pushActionEt;
    private EditText pushTitleEt;
    private EditText pushDataEt;

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
        hostBtn.setText(ApiConfiguration.getHost());

        pushActionEt = (EditText) debugLayout.findViewById(R.id.pushActionEt);
        pushTitleEt = (EditText) debugLayout.findViewById(R.id.pushTitleEt);
        pushDataEt = (EditText) debugLayout.findViewById(R.id.pushDataEt);

        Button pushTestBtn = (Button) debugLayout.findViewById(R.id.pushTestBtn);
        pushTestBtn.setOnClickListener(this);

        if (XMPPManager.getInstance(this.getAppContext()).isConnected()) {
            pushTestBtn.setText("push");
            pushTestBtn.setTextColor(Color.GREEN);
        } else {
            pushTestBtn.setText("XMPP无法连接");
            pushTestBtn.setTextColor(Color.RED);
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
//                    ApiClient.host = hosts[which].toString();
                	ApiConfiguration.setHost(hosts[which].toString());
                    hostBtn.setText(ApiConfiguration.getHost());
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
        ApiParams params = new ApiParams();
        params.addParam("type", "push");
        params.addParam("action", pushActionEt.getText().toString());
        params.addParam("title", pushTitleEt.getText().toString());
        params.addParam("data", "{" + pushDataEt.getText().toString() + "}" );
//        HttpPostCommand.createCommand(1, "debug", params).execute(this);
        BaseApiCommand.createCommand("debug", false, params).execute(getActivity(), this);
    }


    @Override
    public void onNetworkDone(String apiName, String responseData) {
        sendMessage(MSG_pushTestSuccess, responseData);
    }

    @Override
    public void onNetworkFail(String apiName, ApiError error) {
        sendMessage(MSG_pushTestFail, error);
    }

    @Override
    protected void handleMessage(Message msg, Activity activity, View rootView) {
        switch (msg.what) {
            case MSG_pushTestSuccess:
                ViewUtil.showToast(getActivity(), msg.obj.toString(), false);
//                break;
            case MSG_pushTestFail:
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("提示")
                    .setMessage( DebugFragment.decode2( msg.obj.toString() ) )
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

    /*
     * /uXXXX -> 中文
     */
    public static String decode2(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c == '\\' && chars[i + 1] == 'u') {
                char cc = 0;
                for (int j = 0; j < 4; j++) {
                    char ch = Character.toLowerCase(chars[i + 2 + j]);
                    if ('0' <= ch && ch <= '9' || 'a' <= ch && ch <= 'f') {
                        cc |= (Character.digit(ch, 16) << (3 - j) * 4);
                    } else {
                        cc = 0;
                        break;
                    }
                }
                if (cc > 0) {
                    i += 5;
                    sb.append(cc);
                    continue;
                }
            }
            sb.append(c);
        }
        return sb.toString();
    }
}