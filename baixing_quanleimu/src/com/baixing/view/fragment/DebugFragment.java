package com.baixing.view.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.baixing.activity.BaseFragment;
import com.baixing.broadcast.XMPPManager;
import com.baixing.util.Communication;
import com.baixing.util.Util;
import com.quanleimu.activity.R;

/**
 * Created with IntelliJ IDEA.
 * User: zengming
 * Date: 13-1-30
 * Time: AM10:02
 */
public class DebugFragment extends BaseFragment implements View.OnClickListener {
    private Button hostBtn;

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
        debugLayout.findViewById(R.id.pushTestBtn).setOnClickListener(this);

        hostBtn.setText(Communication.host);

        TextView xmppTv = (TextView)debugLayout.findViewById(R.id.xmppConTv);
        if (XMPPManager.getInstance(this.getAppContext()).isConnected()) {
            xmppTv.setText("XMPP已连通");
            xmppTv.setTextColor(Color.GREEN);
        } else {
            xmppTv.setText("XMPP无法连接");
            xmppTv.setTextColor(Color.RED);
        }

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
                    Communication.host = hosts[which].toString();
                    hostBtn.setText(Communication.host);
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

    }
}